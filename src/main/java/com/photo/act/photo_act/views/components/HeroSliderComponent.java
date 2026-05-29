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
 * Reusable hero photo slider component.
 *
 * <p><b>Features</b>
 * <ul>
 *   <li>3 filter tabs: Most Likes (default), Best Rating, Most Views</li>
 *   <li>Photo title/subtitle at the bottom; falls back to city or user-entered location</li>
 *   <li>Photographer name + "View Profile" button per slide</li>
 *   <li>← / → navigation arrows (client-side, no server round-trip)</li>
 *   <li>Right-side vertical action bar: Full View, Like, Rate, Meta Info</li>
 *   <li>4-second auto-advance; pauses on hover; resumes on mouse-out</li>
 *   <li>Keyboard ← / → support while the slider is focused</li>
 *   <li>Dot indicator strip below the slides</li>
 *   <li>Fancy CSS {@code translateX} slide transition</li>
 *   <li>Filter change restarts the sequence from slide 0</li>
 * </ul>
 *
 * <p><b>Usage</b>
 * <pre>{@code
 * HeroSliderComponent hero = new HeroSliderComponent(
 *     recordService, photoStatisticsService,
 *     photoViewService, photoRatingService,
 *     DIR_PHOTOS_SERVER, isMobile, userId, publicIp);
 * verticalLayout.addComponentAsFirst(hero);
 * }</pre>
 */
public class HeroSliderComponent extends Div {

    private static final Logger logger = LoggerFactory.getLogger(HeroSliderComponent.class);

    // ── Filter constants (public so callers can reference them) ───────────
    public static final String FILTER_LIKES  = "Most Likes";
    public static final String FILTER_RATING = "Best Rating";
    public static final String FILTER_VIEWS  = "Most Views";

    private static final int    SLIDE_COUNT    = 10;
    private static final String SUBPATH_MEDIUM = "photo-medium";
    private static final String SUBPATH_SMALL  = "photo-small";
    private static final String SUBPATH_LARGE  = "photo-large";

    // ── Dependencies ──────────────────────────────────────────────────────
    private final RecordService          recordService;
    private final PhotoStatisticsService photoStatisticsService;
    private final PhotoViewService       photoViewService;
    private final PhotoRatingService     photoRatingService;
    private final String                 photosDir;
    private final String                 dirChar;
    private final int                    userId;
    private final String                 publicIp;
    private final String                 sessionId;
    private final LocalDateTime          sessionDateTime;

    // ─────────────────────────────────────────────────────────────────────

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

    // ══════════════════════════════════════════════════════════════════════
    // Build
    // ══════════════════════════════════════════════════════════════════════

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

        // ── Body: [ ← ] [ slides ] [ → ] ─────────────────────────────────
        Div body = new Div();
        body.addClassName("hero-slider__body");
        body.add(navDiv("prev"), buildSlidesContainer(photos), navDiv("next"));
        add(body);

        // ── Dot indicator strip ────────────────────────────────────────────
        add(buildDots(photos.size()));

        // ── Client JS: timer, transitions, dots, keyboard, hover-pause ────
        injectClientJs(photos.size());
    }

    // ── Filter bar ────────────────────────────────────────────────────────

    private HorizontalLayout buildFilterBar(String activeFilter) {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassName("hero-slider__filter-bar");
        bar.setSpacing(false);

        for (String label : new String[]{FILTER_LIKES, FILTER_RATING, FILTER_VIEWS}) {
            Button btn = new Button(label);
            btn.addClassName("hero-filter-btn");
            if (label.equals(activeFilter)) btn.addClassName("hero-filter-btn--active");
            String f = label;
            btn.addClickListener(e -> buildContent(f));
            bar.add(btn);
        }
        return bar;
    }

    // ── Nav arrow (client-side div) ───────────────────────────────────────

    private Div navDiv(String direction) {
        Div nav = new Div();
        nav.addClassNames("hero-slider__nav", "hero-slider__nav--" + direction);
        nav.add("prev".equals(direction)
                ? VaadinIcon.ANGLE_LEFT.create()
                : VaadinIcon.ANGLE_RIGHT.create());
        return nav;
    }

    // ── Slides container ──────────────────────────────────────────────────

    private Div buildSlidesContainer(List<Record> photos) {
        Div container = new Div();
        container.addClassName("hero-slider__slides");
        for (int i = 0; i < photos.size(); i++) {
            container.add(buildSlide(photos.get(i), i == 0));
        }
        return container;
    }

    // ── Dot indicator row ─────────────────────────────────────────────────

    private Div buildDots(int count) {
        Div dotsBar = new Div();
        dotsBar.addClassName("hero-slider__dots");
        for (int i = 0; i < count; i++) {
            Div dot = new Div();
            dot.addClassName("hero-dot");
            if (i == 0) dot.addClassName("hero-dot--active");
            dotsBar.add(dot);
        }
        return dotsBar;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Individual slide
    // ══════════════════════════════════════════════════════════════════════

    private Div buildSlide(Record rec, boolean isActive) {
        Div slide = new Div();
        slide.addClassName("hero-slide");
        if (isActive) slide.addClassName("hero-slide--active");

        Div photoSection = new Div();
        photoSection.addClassName("hero-slide__photo-section");
        photoSection.add(buildPhotoDiv(rec), buildInfoOverlay(rec));

        slide.add(photoSection); //, buildActionBar(rec));
        return slide;
    }

    // ── Photo image ───────────────────────────────────────────────────────

    private Div buildPhotoDiv(Record rec) {
        Div wrapper = new Div();
        wrapper.addClassName("hero-slide__photo");

        String nameNew = nvl(rec.getColumnData("name_new"));
        Image img = tryLoadImage(nameNew, SUBPATH_MEDIUM, SUBPATH_SMALL);
        if (img != null) {
            String alt = nvl(rec.getColumnData("title"));
            img.setAlt(alt.isEmpty() ? "photo" : alt);
            img.addClassName("hero-slide__img");
            wrapper.add(img);
        }
        return wrapper;
    }

    // ── Info overlay (gradient at the bottom of the photo) ───────────────

    private Div buildInfoOverlay(Record rec) {
        Div overlay = new Div();
        overlay.addClassName("hero-slide__info");

        overlay.add(buildDescriptionDiv(rec), buildPhotographerRow(rec));
        return overlay;
    }

    private Div buildDescriptionDiv(Record rec) {
        String title   = nvl(rec.getColumnData("title"));
        String subtitle = nvl(rec.getColumnData("subtitle"));
        String city    = nvl(rec.getColumnData("city_name"));
        String locUser = nvl(rec.getColumnData("location_by_user"));

        Div desc = new Div();
        desc.addClassName("hero-slide__description");

        if (!title.isEmpty()) {
            Span titleSpan = new Span(title);
            titleSpan.addClassName("hero-slide__title");
            desc.add(titleSpan);

            if (!subtitle.isEmpty()) {
                Span subSpan = new Span(subtitle);
                subSpan.addClassName("hero-slide__subtitle");
                desc.add(subSpan);
            }
        } else {
            String loc = !city.isEmpty() ? city : locUser;
            if (!loc.isEmpty()) {
                // Inline icon + text
                Span locSpan = new Span(VaadinIcon.MAP_MARKER.create(), new Span(" " + loc));
                locSpan.addClassName("hero-slide__location");
                desc.add(locSpan);
            }
        }
        return desc;
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
            profileBtn.addClassName("hero-slide__profile-btn");
            String un = username;
            profileBtn.addClickListener(e ->
                profileBtn.getUI().ifPresent(ui ->
                    ui.navigate(PhotographersView.class,
                        new RouteParameters(new RouteParam("member", un))))
            );
            row.add(profileBtn);
        }
        return row;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Vertical action bar (right side of each slide)
    // ══════════════════════════════════════════════════════════════════════

    private Div buildActionBar(Record rec) {
        Div bar = new Div();
        bar.addClassName("hero-slide__actions");

        String nameNew = nvl(rec.getColumnData("name_new"));
        int    photoId = parseInt(rec.getColumnData("id"));

        // ── Full View ─────────────────────────────────────────────────────
        Button fullViewBtn = new Button(VaadinIcon.EXPAND_FULL.create());
        fullViewBtn.addClassNames("hero-action-btn", "hero-action-btn--fullview");
        fullViewBtn.setTooltipText("Full View");
        fullViewBtn.addClickListener(e -> openFullViewDialog(rec));

        // ── Like ──────────────────────────────────────────────────────────
        long likeCount = photoId > 0 ? safeCount(() -> photoViewService.getLikeCount(photoId)) : 0;
        LikeButton likeBtn = new LikeButton(likeCount);
        likeBtn.addClassName("hero-action-btn");
        likeBtn.addLikeClickListener(e -> {
            if (photoId > 0) {
                Integer uid = userId > 0 ? userId : null;
                photoViewService.recordLike(photoId, nameNew, uid, publicIp, sessionId, sessionDateTime);
                likeBtn.setCount(safeCount(() -> photoViewService.getLikeCount(photoId)));
            }
        });

        // ── Rate ──────────────────────────────────────────────────────────
        long ratingCount = photoId > 0 ? safeCount(() -> photoRatingService.getRatingCount(photoId)) : 0;
        RateButton rateBtn = new RateButton(ratingCount);
        rateBtn.addClassName("hero-action-btn");
        rateBtn.addRateClickListener(e -> openRatingDialog(photoId, nameNew, rateBtn));

        // ── Meta Info ─────────────────────────────────────────────────────
        Button metaBtn = new Button(VaadinIcon.INFO_CIRCLE_O.create());
        metaBtn.addClassNames("hero-action-btn", "hero-action-btn--meta");
        metaBtn.setTooltipText("Photo Info");
        metaBtn.addClickListener(e -> openMetaDialog(rec));

        bar.add(fullViewBtn, likeBtn, rateBtn, metaBtn);
        return bar;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Dialogs
    // ══════════════════════════════════════════════════════════════════════

    private void openFullViewDialog(Record rec) {
        String nameNew = nvl(rec.getColumnData("name_new"));
        int    photoId = parseInt(rec.getColumnData("id"));

        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);
        dialog.addClassName("hero-fullview-dialog");

        Image img = tryLoadImage(nameNew, SUBPATH_LARGE, SUBPATH_MEDIUM);
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
        ratingSelect.setLabel("Your Rating  (1 = poor · 7 = excellent)");

        if (userId > 0) {
            int existing = photoRatingService.getUserRating(photoId, userId);
            ratingSelect.setValue(existing > 0 ? existing : 5);
        } else {
            ratingSelect.setValue(5);
        }

        Button submitBtn = new Button("Submit", e -> {
            if (userId <= 0) {
                Notification.show("Please log in to rate photos.",
                        2500, Notification.Position.MIDDLE);
                dialog.close();
                return;
            }
            Integer rating = ratingSelect.getValue();
            if (rating != null) {
                photoRatingService.saveOrUpdateRating(photoId, userId, rating,
                        nameNew, publicIp, sessionId, sessionDateTime);
                rateBtn.setCount(safeCount(() -> photoRatingService.getRatingCount(photoId)));
                Notification n = Notification.show("Rating saved!",
                        1800, Notification.Position.BOTTOM_CENTER);
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
        addMetaRow(content, "Camera",        join(rec.getColumnData("meta_camera_make"),
                                                   rec.getColumnData("meta_camera_model")));
        addMetaRow(content, "Lens",          rec.getColumnData("meta_lens_model"));
        addMetaRow(content, "Focal Length",  rec.getColumnData("meta_focal_length_ff"));
        addMetaRow(content, "ISO",           rec.getColumnData("meta_iso"));
        addMetaRow(content, "Aperture",      rec.getColumnData("meta_aperture"));
        addMetaRow(content, "Shutter Speed", rec.getColumnData("meta_shutter_speed"));
        addMetaRow(content, "Location",      join(rec.getColumnData("city_name"),
                                                   rec.getColumnData("location_by_user")));
        addMetaRow(content, "Photographer",  join(rec.getColumnData("name"),
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

        Span lbl = new Span(label + ":");
        lbl.addClassName("hero-meta-label");

        Span val = new Span(value.trim());
        val.addClassName("hero-meta-value");

        row.add(lbl, val);
        container.add(row);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Data loading
    // ══════════════════════════════════════════════════════════════════════

    private List<Record> loadPhotos(String filter) {
        String sql = switch (filter) {
            case FILTER_RATING -> photoStatisticsService.getBestRatingSql(SLIDE_COUNT);
            case FILTER_VIEWS  -> photoStatisticsService.getMostViewedSql(SLIDE_COUNT);
            default            -> photoStatisticsService.getMostLikedSql(SLIDE_COUNT);
        };
        try {
            return recordService.findAll(sql, PhotoStatisticsService.STATS_COLUMNS);
        } catch (Exception ex) {
            logger.error("HeroSlider – could not load photos for filter '{}': {}", filter, ex.getMessage());
            return List.of();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Image helpers
    // ══════════════════════════════════════════════════════════════════════

    /** Tries {@code preferred} subfolder first, falls back to {@code fallback}. */
    private Image tryLoadImage(String nameNew, String preferred, String fallback) {
        if (nameNew == null || nameNew.isBlank()) return null;
        File f = imageFile(nameNew, preferred);
        if (!f.exists()) f = imageFile(nameNew, fallback);
        if (!f.exists()) return null;

        Image img = new Image();
        img.setSrc(DownloadHandler.forFile(f));
        return img;
    }

    private File imageFile(String nameNew, String subPath) {
        return Paths.get(photosDir + dirChar + subPath + dirChar + nameNew).toFile();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Client-side JavaScript
    // ══════════════════════════════════════════════════════════════════════

    private void injectClientJs(int total) {
        if (total < 1) return;

        // language=JavaScript
        String js = """
            (function(hero) {
              if (!hero) return;
              var heroId = hero.id || 'hero-slider';

              /* ── Clear any stale timer from a previous filter render ── */
              if (window['_heroTimer_' + heroId]) {
                clearInterval(window['_heroTimer_' + heroId]);
                window['_heroTimer_' + heroId] = null;
              }

              var slides = Array.from(
                hero.querySelectorAll('.hero-slider__slides > .hero-slide')
              );
              var dots = Array.from(
                hero.querySelectorAll('.hero-slider__dots > .hero-dot')
              );
              var total     = %d;
              var current   = 0;
              var animating = false;
              var paused    = false;

              /* ── Transition to a specific slide ─────────────────────── */
              function goTo(newIdx, dir) {
                if (animating || newIdx === current || total < 2) return;
                animating = true;

                var exitCls  = dir === 'next' ? 'hero-slide--exit-left'   : 'hero-slide--exit-right';
                var enterCls = dir === 'next' ? 'hero-slide--enter-right' : 'hero-slide--enter-left';
                var from = slides[current];
                var to   = slides[newIdx];

                to.classList.add(enterCls, 'hero-slide--active');
                from.classList.add(exitCls);

                /* Sync dots */
                if (dots[current]) dots[current].classList.remove('hero-dot--active');
                if (dots[newIdx])  dots[newIdx].classList.add('hero-dot--active');

                setTimeout(function() {
                  from.classList.remove('hero-slide--active', exitCls);
                  to.classList.remove(enterCls);
                  current   = newIdx;
                  animating = false;
                }, 620);
              }

              function advance(dir) {
                var n = dir === 'next'
                  ? (current + 1) %% total
                  : (current - 1 + total) %% total;
                goTo(n, dir);
              }

              /* ── Auto-advance timer ──────────────────────────────────── */
              function resetTimer() {
                clearInterval(window['_heroTimer_' + heroId]);
                window['_heroTimer_' + heroId] = setInterval(function() {
                  if (!paused) advance('next');
                }, 4000);
              }

              /* ── Navigation arrows ───────────────────────────────────── */
              var prevEl = hero.querySelector('.hero-slider__nav--prev');
              var nextEl = hero.querySelector('.hero-slider__nav--next');
              if (prevEl) prevEl.addEventListener('click', function() { advance('prev'); resetTimer(); });
              if (nextEl) nextEl.addEventListener('click', function() { advance('next'); resetTimer(); });

              /* ── Dot clicks ──────────────────────────────────────────── */
              dots.forEach(function(dot, idx) {
                dot.addEventListener('click', function() {
                  goTo(idx, idx > current ? 'next' : 'prev');
                  resetTimer();
                });
              });

              /* ── Hover: pause / resume ───────────────────────────────── */
              hero.addEventListener('mouseenter', function() { paused = true; });
              hero.addEventListener('mouseleave', function() { paused = false; });

              /* ── Keyboard navigation (when hero or child is focused) ─── */
              hero.setAttribute('tabindex', '0');
              hero.addEventListener('keydown', function(e) {
                if (e.key === 'ArrowLeft')  { advance('prev'); resetTimer(); e.preventDefault(); }
                if (e.key === 'ArrowRight') { advance('next'); resetTimer(); e.preventDefault(); }
              });

              resetTimer();
            })(this);
            """.formatted(total);

        getElement().executeJs(js);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Utility
    // ══════════════════════════════════════════════════════════════════════

    @FunctionalInterface
    private interface LongSupplier { long get(); }

    private long safeCount(LongSupplier supplier) {
        try { return supplier.get(); }
        catch (Exception e) { return 0; }
    }

    private static String join(String a, String b) {
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

    private static String nvl(String s) { return s == null ? "" : s; }
}
