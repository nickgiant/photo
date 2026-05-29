package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.PhotoRatingService;
import com.photo.act.photo_act.services.PhotoStatisticsService;
import com.photo.act.photo_act.services.PhotoViewService;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.PhotographersView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reusable hero slider component for showcasing photos with transitions.
 *
 * <p>Features:
 * <ul>
 *   <li>Three filter tabs: Most Likes (default), Best Rating, Most Views</li>
 *   <li>Photo description or location fallback at the bottom of each slide</li>
 *   <li>Photographer name and "View Profile" button per slide</li>
 *   <li>Prev / Next navigation arrows</li>
 *   <li>Right-side vertical action bar: Full View, Like, Rate, Meta Info</li>
 *   <li>4-second auto-advance (client-side timer) with CSS slide transitions</li>
 *   <li>Filter change restarts sequence from the first photo</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 *   HeroSliderComponent hero = new HeroSliderComponent(
 *       recordService, photoStatisticsService,
 *       photoViewService, photoRatingService,
 *       photosDir, isMobile, userId, publicIp);
 *   layout.addComponentAsFirst(hero);
 * </pre>
 */
public class HeroSliderComponent extends Div {

    private static final Logger logger = LoggerFactory.getLogger(HeroSliderComponent.class);

    public static final String FILTER_LIKES  = "Most Likes";
    public static final String FILTER_RATING = "Best Rating";
    public static final String FILTER_VIEWS  = "Most Views";

    private static final int    SLIDE_COUNT      = 10;
    private static final String SUBPATH_MEDIUM   = "photo-medium";
    private static final String SUBPATH_SMALL    = "photo-small";
    private static final String SUBPATH_LARGE    = "photo-large";

    private final RecordService           recordService;
    private final PhotoStatisticsService  photoStatisticsService;
    private final PhotoViewService        photoViewService;
    private final PhotoRatingService      photoRatingService;
    private final String                  photosDir;
    private final String                  dirChar;
    private final boolean                 isMobile;
    private final int                     userId;
    private final String                  publicIp;
    private final String                  sessionId;
    private final LocalDateTime           sessionDateTime;

    public HeroSliderComponent(
            RecordService recordService,
            PhotoStatisticsService photoStatisticsService,
            PhotoViewService photoViewService,
            PhotoRatingService photoRatingService,
            String photosDir,
            boolean isMobile,
            int userId,
            String publicIp) {

        this.recordService          = recordService;
        this.photoStatisticsService = photoStatisticsService;
        this.photoViewService       = photoViewService;
        this.photoRatingService     = photoRatingService;
        this.photosDir              = photosDir;
        this.dirChar                = FileSystems.getDefault().getSeparator();
        this.isMobile               = isMobile;
        this.userId                 = userId;
        this.publicIp               = publicIp;

        VaadinSession session = VaadinSession.getCurrent();
        this.sessionId       = session.getSession().getId();
        long creationMs      = session.getSession().getCreationTime();
        this.sessionDateTime = new UtilsDate().calcDateTimeFromLongInLDT(creationMs, "UTC");

        setId("hero-slider");
        addClassName("hero-slider");

        buildContent(FILTER_LIKES);
    }

    // ─── Build ────────────────────────────────────────────────────────────

    private void buildContent(String activeFilter) {
        removeAll();

        add(buildFilterBar(activeFilter));

        List<Record> photos = loadPhotos(activeFilter);
        if (photos.isEmpty()) {
            Div empty = new Div("No photos available yet.");
            empty.addClassName("hero-slider__empty");
            add(empty);
            return;
        }

        Div body = new Div();
        body.addClassName("hero-slider__body");

        Div prevBtn = buildNavButton("prev");
        Div slidesContainer = buildSlides(photos);
        Div nextBtn = buildNavButton("next");

        body.add(prevBtn, slidesContainer, nextBtn);
        add(body);

        injectClientJs(photos.size());
    }

    // ─── Filter bar ───────────────────────────────────────────────────────

    private HorizontalLayout buildFilterBar(String activeFilter) {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassName("hero-slider__filter-bar");
        bar.setSpacing(false);

        for (String label : new String[]{FILTER_LIKES, FILTER_RATING, FILTER_VIEWS}) {
            Button btn = new Button(label);
            btn.addClassName("hero-filter-btn");
            if (label.equals(activeFilter)) btn.addClassName("hero-filter-btn--active");

            String filter = label;
            btn.addClickListener(e -> buildContent(filter));
            bar.add(btn);
        }
        return bar;
    }

    // ─── Navigation buttons (pure div, handled by JS) ─────────────────────

    private Div buildNavButton(String direction) {
        Div nav = new Div();
        nav.addClassNames("hero-slider__nav", "hero-slider__nav--" + direction);
        nav.add(direction.equals("prev") ? VaadinIcon.ANGLE_LEFT.create() : VaadinIcon.ANGLE_RIGHT.create());
        return nav;
    }

    // ─── Slides container ─────────────────────────────────────────────────

    private Div buildSlides(List<Record> photos) {
        Div container = new Div();
        container.addClassName("hero-slider__slides");

        for (int i = 0; i < photos.size(); i++) {
            container.add(buildSlide(photos.get(i), i == 0));
        }
        return container;
    }

    // ─── Individual slide ─────────────────────────────────────────────────

    private Div buildSlide(Record rec, boolean isActive) {
        Div slide = new Div();
        slide.addClassName("hero-slide");
        if (isActive) slide.addClassName("hero-slide--active");

        // Photo section (image + info overlay)
        Div photoSection = new Div();
        photoSection.addClassName("hero-slide__photo-section");
        photoSection.add(buildPhotoImage(rec));
        photoSection.add(buildInfoOverlay(rec));

        // Vertical action bar
        Div actionBar = buildActionBar(rec);

        slide.add(photoSection, actionBar);
        return slide;
    }

    private Div buildPhotoImage(Record rec) {
        Div wrapper = new Div();
        wrapper.addClassName("hero-slide__photo");

        String nameNew = nvl(rec.getColumnData("name_new"));
        Image img = loadImage(nameNew, SUBPATH_MEDIUM, SUBPATH_SMALL);
        if (img != null) {
            String alt = nvl(rec.getColumnData("title"));
            img.setAlt(alt.isEmpty() ? "photo" : alt);
            img.addClassName("hero-slide__img");
            wrapper.add(img);
        }
        return wrapper;
    }

    private Div buildInfoOverlay(Record rec) {
        Div overlay = new Div();
        overlay.addClassName("hero-slide__info");

        String title    = nvl(rec.getColumnData("title"));
        String subtitle = nvl(rec.getColumnData("subtitle"));
        String city     = nvl(rec.getColumnData("city_name"));
        String locUser  = nvl(rec.getColumnData("location_by_user"));

        Div descriptionDiv = new Div();
        descriptionDiv.addClassName("hero-slide__description");

        if (!title.isEmpty()) {
            Span titleSpan = new Span(title);
            titleSpan.addClassName("hero-slide__title");
            descriptionDiv.add(titleSpan);

            if (!subtitle.isEmpty()) {
                Span subSpan = new Span(subtitle);
                subSpan.addClassName("hero-slide__subtitle");
                descriptionDiv.add(subSpan);
            }
        } else {
            // Fallback to location
            String loc = !city.isEmpty() ? city : locUser;
            if (!loc.isEmpty()) {
                Span locSpan = new Span(VaadinIcon.MAP_MARKER.create(), new Span(" " + loc));
                locSpan.addClassName("hero-slide__location");
                descriptionDiv.add(locSpan);
            }
        }

        overlay.add(descriptionDiv, buildPhotographerRow(rec));
        return overlay;
    }

    private HorizontalLayout buildPhotographerRow(Record rec) {
        String name     = nvl(rec.getColumnData("name"));
        String surname  = nvl(rec.getColumnData("surname"));
        String username = nvl(rec.getColumnData("username"));

        String displayName = (!name.isEmpty() || !surname.isEmpty())
                ? (name + " " + surname).trim()
                : (!username.isEmpty() ? "@" + username : "");

        HorizontalLayout row = new HorizontalLayout();
        row.addClassName("hero-slide__photographer");
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);

        if (!displayName.isEmpty()) {
            Span nameSpan = new Span(displayName);
            nameSpan.addClassName("hero-slide__photographer-name");
            row.add(nameSpan);
        }

        if (!username.isEmpty()) {
            Button profileBtn = new Button("View Profile");
            profileBtn.addClassNames("hero-slide__profile-btn");
            String un = username;
            profileBtn.addClickListener(e ->
                profileBtn.getUI().ifPresent(ui ->
                    ui.navigate(PhotographersView.class,
                        new RouteParameters(new RouteParam("member", un)))
                )
            );
            row.add(profileBtn);
        }

        return row;
    }

    // ─── Action bar ───────────────────────────────────────────────────────

    private Div buildActionBar(Record rec) {
        Div bar = new Div();
        bar.addClassName("hero-slide__actions");

        String nameNew    = nvl(rec.getColumnData("name_new"));
        int    photoId    = parseInt(rec.getColumnData("id"));

        // Full view
        Button fullViewBtn = new Button(VaadinIcon.EXPAND_FULL.create());
        fullViewBtn.addClassNames("hero-action-btn", "hero-action-btn--fullview");
        fullViewBtn.setTooltipText("Full View");
        fullViewBtn.addClickListener(e -> openFullViewDialog(rec));

        // Like
        long likeCount = photoId > 0 ? safeGetLikeCount(photoId) : 0;
        LikeButton likeBtn = new LikeButton(likeCount);
        likeBtn.addClassName("hero-action-btn");
        likeBtn.addLikeClickListener(e -> {
            if (photoId > 0) {
                Integer uid = userId > 0 ? userId : null;
                photoViewService.recordLike(photoId, nameNew, uid, publicIp, sessionId, sessionDateTime);
                likeBtn.setCount(safeGetLikeCount(photoId));
            }
        });

        // Rate
        long ratingCount = photoId > 0 ? safeGetRatingCount(photoId) : 0;
        RateButton rateBtn = new RateButton(ratingCount);
        rateBtn.addClassName("hero-action-btn");
        rateBtn.addRateClickListener(e -> openRatingDialog(photoId, nameNew, rateBtn));

        // Meta info
        Button metaBtn = new Button(VaadinIcon.INFO_CIRCLE_O.create());
        metaBtn.addClassNames("hero-action-btn", "hero-action-btn--meta");
        metaBtn.setTooltipText("Photo Info");
        metaBtn.addClickListener(e -> openMetaDialog(rec));

        bar.add(fullViewBtn, likeBtn, rateBtn, metaBtn);
        return bar;
    }

    // ─── Dialogs ──────────────────────────────────────────────────────────

    private void openFullViewDialog(Record rec) {
        String nameNew = nvl(rec.getColumnData("name_new"));
        int photoId    = parseInt(rec.getColumnData("id"));

        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);
        dialog.addClassName("hero-fullview-dialog");

        Image img = loadImage(nameNew, SUBPATH_LARGE, SUBPATH_MEDIUM);
        if (img != null) {
            img.addClassName("hero-fullview-img");
            dialog.add(img);

            if (photoId > 0) {
                Integer uid = userId > 0 ? userId : null;
                photoViewService.recordView(photoId, nameNew, uid, publicIp,
                        PhotoViewService.TYPE_FULL, sessionId, sessionDateTime);
            }
        } else {
            dialog.add(new Span("Image not available."));
        }
        dialog.open();
    }

    private void openRatingDialog(int photoId, String nameNew, RateButton rateBtn) {
        if (photoId <= 0) return;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Rate this Photo");
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);

        Select<Integer> ratingSelect = new Select<>();
        ratingSelect.setItems(1, 2, 3, 4, 5, 6, 7);
        ratingSelect.setLabel("Your Rating (1 = poor, 7 = excellent)");

        if (userId > 0) {
            int existing = photoRatingService.getUserRating(photoId, userId);
            ratingSelect.setValue(existing > 0 ? existing : 5);
        } else {
            ratingSelect.setValue(5);
        }

        Button submitBtn = new Button("Submit", e -> {
            if (userId <= 0) {
                Notification.show("Please log in to rate photos.", 2500, Notification.Position.MIDDLE);
                dialog.close();
                return;
            }
            Integer rating = ratingSelect.getValue();
            if (rating != null) {
                photoRatingService.saveOrUpdateRating(photoId, userId, rating,
                        nameNew, publicIp, sessionId, sessionDateTime);
                rateBtn.setCount(safeGetRatingCount(photoId));
                Notification n = Notification.show("Rating saved!", 1800, Notification.Position.BOTTOM_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            dialog.close();
        });
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(submitBtn, cancelBtn);
        buttons.setSpacing(true);

        VerticalLayout content = new VerticalLayout(ratingSelect, buttons);
        content.setSpacing(true);
        content.setPadding(false);
        dialog.add(content);
        dialog.open();
    }

    private void openMetaDialog(Record rec) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Photo Information");
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);
        dialog.addClassName("hero-meta-dialog");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);

        addMetaRow(content, "Title",         rec.getColumnData("title"));
        addMetaRow(content, "Date Taken",    rec.getColumnData("photo_time_shot"));
        addMetaRow(content, "Camera",        joinNonEmpty(rec.getColumnData("meta_camera_make"),
                                                           rec.getColumnData("meta_camera_model")));
        addMetaRow(content, "Lens",          rec.getColumnData("meta_lens_model"));
        addMetaRow(content, "Focal Length",  rec.getColumnData("meta_focal_length_ff"));
        addMetaRow(content, "ISO",           rec.getColumnData("meta_iso"));
        addMetaRow(content, "Aperture",      rec.getColumnData("meta_aperture"));
        addMetaRow(content, "Shutter Speed", rec.getColumnData("meta_shutter_speed"));
        addMetaRow(content, "Location",      joinNonEmpty(rec.getColumnData("city_name"),
                                                           rec.getColumnData("location_by_user")));
        addMetaRow(content, "Photographer",  joinNonEmpty(rec.getColumnData("name"),
                                                           rec.getColumnData("surname")));

        if (content.getComponentCount() == 0) {
            content.add(new Span("No metadata available for this photo."));
        }

        dialog.add(content);
        dialog.open();
    }

    private void addMetaRow(VerticalLayout container, String label, String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim())) return;

        HorizontalLayout row = new HorizontalLayout();
        row.addClassName("hero-meta-row");
        row.setSpacing(false);

        Span labelSpan = new Span(label + ":");
        labelSpan.addClassName("hero-meta-label");

        Span valueSpan = new Span(value.trim());
        valueSpan.addClassName("hero-meta-value");

        row.add(labelSpan, valueSpan);
        container.add(row);
    }

    // ─── Data loading helpers ─────────────────────────────────────────────

    private List<Record> loadPhotos(String filter) {
        String sql = switch (filter) {
            case FILTER_RATING -> photoStatisticsService.getBestRatingSql(SLIDE_COUNT);
            case FILTER_VIEWS  -> photoStatisticsService.getMostViewedSql(SLIDE_COUNT);
            default            -> photoStatisticsService.getMostLikedSql(SLIDE_COUNT);
        };
        try {
            return recordService.findAll(sql, PhotoStatisticsService.STATS_COLUMNS);
        } catch (Exception ex) {
            logger.error("HeroSliderComponent failed to load photos for filter '{}': {}", filter, ex.getMessage());
            return List.of();
        }
    }

    // ─── Image helpers ────────────────────────────────────────────────────

    /**
     * Tries to load an image from the first available subfolder, then falls back.
     */
    private Image loadImage(String nameNew, String preferred, String fallback) {
        if (nameNew == null || nameNew.isBlank()) return null;

        File file = imageFile(nameNew, preferred);
        if (!file.exists()) file = imageFile(nameNew, fallback);
        if (!file.exists()) return null;

        Image img = new Image();
        img.setSrc(DownloadHandler.forFile(file));
        return img;
    }

    private File imageFile(String nameNew, String subPath) {
        return Paths.get(photosDir + dirChar + subPath + dirChar + nameNew).toFile();
    }

    // ─── Client-side JavaScript ───────────────────────────────────────────

    private void injectClientJs(int total) {
        if (total < 1) return;

        // language=JavaScript
        String js = """
            (function(hero) {
              if (!hero) return;
              var heroId = hero.id || 'hero-slider';

              // Clear any existing timer for this hero instance
              if (window['_heroTimer_' + heroId]) {
                clearInterval(window['_heroTimer_' + heroId]);
              }

              var slides = Array.from(
                hero.querySelectorAll('.hero-slider__slides > .hero-slide')
              );
              var total = %d;
              if (total < 2) return;

              var current  = 0;
              var animating = false;

              function goTo(newIdx, dir) {
                if (animating || newIdx === current) return;
                animating = true;

                var exitCls  = dir === 'next' ? 'hero-slide--exit-left'  : 'hero-slide--exit-right';
                var enterCls = dir === 'next' ? 'hero-slide--enter-right' : 'hero-slide--enter-left';
                var fromSlide = slides[current];
                var toSlide   = slides[newIdx];

                toSlide.classList.add(enterCls, 'hero-slide--active');
                fromSlide.classList.add(exitCls);

                setTimeout(function() {
                  fromSlide.classList.remove('hero-slide--active', exitCls);
                  toSlide.classList.remove(enterCls);
                  current   = newIdx;
                  animating = false;
                }, 650);
              }

              function advance(dir) {
                var n = dir === 'next'
                  ? (current + 1) %% total
                  : (current - 1 + total) %% total;
                goTo(n, dir);
              }

              function resetTimer() {
                clearInterval(window['_heroTimer_' + heroId]);
                window['_heroTimer_' + heroId] = setInterval(function() {
                  advance('next');
                }, 4000);
              }

              var prevEl = hero.querySelector('.hero-slider__nav--prev');
              var nextEl = hero.querySelector('.hero-slider__nav--next');

              if (prevEl) prevEl.addEventListener('click', function() {
                advance('prev');
                resetTimer();
              });
              if (nextEl) nextEl.addEventListener('click', function() {
                advance('next');
                resetTimer();
              });

              resetTimer();
            })(this);
            """.formatted(total);

        getElement().executeJs(js);
    }

    // ─── Utility ──────────────────────────────────────────────────────────

    private long safeGetLikeCount(int photoId) {
        try { return photoViewService.getLikeCount(photoId); }
        catch (Exception e) { return 0; }
    }

    private long safeGetRatingCount(int photoId) {
        try { return photoRatingService.getRatingCount(photoId); }
        catch (Exception e) { return 0; }
    }

    private static String joinNonEmpty(String a, String b) {
        String na = nvl(a).trim();
        String nb = nvl(b).trim();
        if (na.isEmpty() && nb.isEmpty()) return "";
        if (na.isEmpty()) return nb;
        if (nb.isEmpty()) return na;
        return na + " " + nb;
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(nvl(s)); }
        catch (NumberFormatException e) { return 0; }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
