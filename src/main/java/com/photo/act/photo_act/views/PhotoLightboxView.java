package com.photo.act.photo_act.views;



import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.PhotoViewService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.utils.UtilsString;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.LikeButton;
import com.photo.act.photo_act.views.components.PhotoFrameComponent;
import com.photo.act.photo_act.views.components.ThumbnailStrip;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.photo.act.photo_act.views.AlbumsView.subPathThumbs;
import static com.photo.act.photo_act.views.HomeView.subPathLarge;
import static com.photo.act.photo_act.views.MainLayout.PROP_PHOTOS;
import static com.photo.act.photo_act.views.MainLayout.SUB_PATH_AVATARS_THUMBS;
import com.photo.act.photo_act.services.PhotoRatingService;
import com.photo.act.photo_act.views.components.RateButton;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.time.LocalDateTime;

/**
 * Full-screen photo viewer.
 *
 * ── Overall layout (VerticalLayout, full height) ──────────────────────────────
 *
 *   ┌──────────────────────────────────────────────┬──────────────┐
 *   │  [❮]  Vaadin Image (DownloadHandler)  [❯]  │ info panel   │
 *   │        object-fit: contain                 │  (220 px)    │
 *   │        [✕] top-right close button          │              │
 *   ├──────────────────────────────────────────────┴──────────────┤
 *   │       Thumbnail filmstrip   ← → scrollable  (100 px)       │
 *   └─────────────────────────────────────────────────────────────┘
 *
 * Navigation is fully server-side: clicking ❮/❯ updates Image.setSrc()
 * via DownloadHandler.forFile(). No URL strings are passed to JavaScript.
 */

@AnonymousAllowed
@Route(value ="photo/:photo-id", autoLayout = false)
//@RouteAlias(value = "photo/:photo-id?", layout = MainLayout.class)  not this. it shows it inside side menu
@PageTitle("photo · PhotoAct.net")
@Slf4j

public class PhotoLightboxView extends VerticalLayout
        implements BeforeEnterObserver {

    private static final Logger logger = LoggerFactory.getLogger(PhotoLightboxView.class);
    private String dirChar = FileSystems.getDefault().getSeparator();
    private RecordService recordService;

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final PhotoViewService  photoViewService;
    private final PhotoRatingService photoRatingService;

    // ── Components ────────────────────────────────────────────────────────────
    private PhotoFrameComponent photoFrame;
    private ThumbnailStrip thumbnailStrip;

    // State
    private int    currentIndex       = 0;
    private String strPathLargePhotos = "";  // set in buildView

    // Right panel elements
    private final H2    photoTitle   = new H2();
    private final Span  authorSpan   = new Span();
    private final Div   exifGrid     = new Div();
    private final Div   tagsRow      = new Div();
    private LikeButton  likeButton;
    private RateButton  rateButton;
    private final Button downloadBtn = new Button("Download");
    private final Button shareBtn    = new Button("Share");
    private final Div   commentsDiv  = new Div();

    // Right-panel tab components
    private VerticalLayout metaContent;
    private VerticalLayout ratingContent;
    private Tab            tabMeta;
    private Tab            tabRate;
    private Tabs           panelTabs;
    private boolean        initialLoad = true;

    // State
    private List<Record> photos   = new ArrayList<>();
    private long                currentPhotoId = 0L;
    private String              currentSlug    = "";

    private List<Record> recProps;

    private String strPhotoId;


    private String[] arrGenreNames = {"id", "title"};
    private String sqlReadGenre = "SELECT id,  title " +
            " FROM  photo_genres " +
            " ORDER BY title ASC ";

    private String[] arrDestinationAllNames = {"id", "city_name", "prefecture", "country"};
    private String sqlReadDestinationAll = "SELECT distinct city_name, id, prefecture, country " +
            " FROM destination d " +
            " ORDER BY country ASC, city_name ASC ";

    private String[] arrDestinationAssignedNames = {"id", "city_name", "prefecture", "country"};
    private String sqlReadDestinationAssigned = "SELECT distinct city_name, d.id, prefecture, country " +
            " FROM photo_meta pm LEFT JOIN destination d ON pm.destination_id = d.id " +
            " ORDER BY country ASC, city_name ASC ";


    private String[] arrSubjectNames = {"id", "subject_name", "subject_description", "subject_type"};
    private String sqlReadSubject = "SELECT distinct subject_name, id,  subject_description, subject_type " +
            " FROM subject s " +
            " ORDER BY subject_name ASC ";

    private String[] arrSubjectAssignedNames = {"id", "subject_name", "subject_description", "subject_type"};
    private String sqlReadSubjectAssigned = "SELECT distinct subject_name, s.id, subject_description, subject_type " +
            " FROM photo_meta pm LEFT JOIN subject s ON pm.subject_id = s.id " +
            " ORDER BY subject_name ASC ";

    private String[] arrAlbumNames = new String[]{"user_id", "id", "title", "description", "city_name", "country"};
    private String sqlReadAlbums = "SELECT distinct a.title , a.id, a.description, a.user_id, d.city_name, d.country " +
            " FROM  destination d RIGHT JOIN photo_album a  ON (d.id = a.destination_id )  LEFT JOIN photo_album_photo pap ON (pap.photo_album_id = a.id AND a.user_id = pap.user_id), dbuser usr " +
            " WHERE usr.userId = a.user_id ";
    //     "  AND usr.username = '" + strAlbumUsername + "' " +
    private String sqlReadAlbumsOrderby = " ORDER BY title ASC ";


    private String[] arrColumnNamesGallery = {"id", "name_new", "title", "subtitle", "notes", "photo_type", "uploader", "creator", "visible_to", "meta_date", "photo_date", "photo_time", "photo_time_shot"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso", "meta_aperture", "meta_shutter_speed", "meta_orientation", "meta_i_height", "meta_i_length", "meta_i_width"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon"
            , "city_name"
            , "subject_name", "subject_description", "subject_type"
            , "date_inserted_diff_from_now"
            , "username", "surname", "name", "resident", "resident_country", "date_joined", "member_since", "avatar_path", "short_bio"
            , "count_photos", "count_stories"
    };

    private String sqlReadGalleryDestinations =
            " SELECT pm.id, pm.name_new, pm.title, pm.subtitle, pm.notes, pm.photo_type, pm.uploader, pm.creator, pm.visible_to,  DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, DATE_FORMAT(pm.meta_date, '%M %Y') AS photo_date, DATE_FORMAT(pm.meta_date, '%H:%i') AS photo_time " +
                    " , DATE_FORMAT(pm.meta_date, '%d/%m/%Y - %H:%i:%S') AS photo_time_shot,  pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model,  pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed, meta_orientation ,  pm.meta_i_height, pm.meta_i_length, pm.meta_i_width , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
                    " , getDateDiffFromNow(pm.date_inserted) AS date_inserted_diff_from_now " +
                    " , d.city_name, d.prefecture, d.country " +
                    " , usr.username, usr.surname, usr.name, usr.resident, usr.resident_country, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since, usr.avatar_path " +
                    " , usr.short_bio " +
                    " , ux.count_photos, ux.count_stories " +
                    " FROM dbuser usr, dbuser_extra ux, photo_meta pm" +
                    " LEFT JOIN destination d ON pm.destination_id = d.id " +
                    " WHERE pm.uploaderId = usr.userId AND pm.visible_to = 'ALL' " +
                    " AND usr.userId = ux.user_id ";
    private String sqlReadGalleryDestinationsOrderBy = " ORDER BY pm.date_inserted DESC  LIMIT 80 ";
    private String strBrowser;
    private String hostname;
    private String hostAddress;
    private String canonicalHostname;
    private String strOS;

    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String publicIp;
    private String strPath;
    private String locale;
    private String localeName;
    private String sessionid;
    private long sessionCreation;
    private UtilsDate utilsDate;
    private String sessionDateTime;
    private GenericView genericView;
    private VerticalLayout filtersContainer;

    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";


    // ── View setup ────────────────────────────────────────────────────────────

    public PhotoLightboxView(RecordService recordService,
                             PhotoViewService photoViewService,
                             PhotoRatingService photoRatingService) {
        this.recordService      = recordService;
        this.photoViewService   = photoViewService;
        this.photoRatingService = photoRatingService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("plv-root");

        utilsDate = new UtilsDate();
        genericView = new GenericView(recordService);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        currentSlug = event.getRouteParameters().get("slug").map(UtilsString::decodeRouteParam).orElse("");
        strPhotoId = event.getRouteParameters().get("photo-id").map(UtilsString::decodeRouteParam).orElse("");

        // Detect ?tab=rate — set when arriving from a Rate It button on a card or hero
        boolean openOnRateTab = event.getLocation().getQueryParameters()
                .getParameters().getOrDefault("tab", List.of()).contains("rate");

        String[] arrNames = null;
        String sqlRead = "";

        getUserClientInfo();

        String sqlReadAppConfig = "SELECT app, host, propName, propValue FROM dbinfo WHERE host like '" + hostname + "' ";
        String[] arrCols = {"propName", "propValue"};
        recProps = recordService.findAll(sqlReadAppConfig, arrCols);


        String strFilterColumn = "";

        int isType = 1;
        String strSelection = "";
        String sqlReadOrderBy = sqlReadGalleryDestinationsOrderBy; //" ORDER BY pm.date_inserted DESC";

        if (isType == 1) {
            arrNames = arrColumnNamesGallery;
            sqlRead = sqlReadGalleryDestinations;
            strFilterColumn = "pm.id";
        } else if (isType == 2) {
            arrNames = arrColumnNamesGallery;
            sqlRead = sqlReadGalleryDestinations;
            strFilterColumn = "city_name";
        } else if (isType == 3) {
            arrNames = arrSubjectAssignedNames;
            sqlRead = sqlReadSubjectAssigned;
            strFilterColumn = "subject_name";
        } else {
            logger.error(" isType in not defined");
        }

        String sqlReadPhotos = "";

        if (strPhotoId.isEmpty()) {
            if (isType == 2 || isType == 3) {
                sqlReadPhotos = sqlRead  + " " +  sqlReadOrderBy;
            } else {
                sqlReadPhotos = sqlRead + " " + sqlReadOrderBy;
            }
        } else {
            if (isType == 2 || isType == 3) {
                sqlReadPhotos = sqlRead + " AND " + strFilterColumn + " LIKE '" + strSelection + "' "+ " " + sqlReadOrderBy;
            } else if (isType == 1) {
                sqlReadPhotos = sqlRead + " AND pm.date_inserted  " +
                        " <= (\n" +
                        "    SELECT date_inserted\n" +
                        "    FROM photo_meta \n" +
                        "    WHERE id = " +strPhotoId+" ) "+
                        " "+ sqlReadOrderBy;
            }
        }

//        arrColumnsGallery = arrColumnNames;

        // Load all photos in the album (ordered)
        photos = getRecordsFromDb(sqlReadPhotos, arrNames); //contentRepo.findBySlug(currentSlug);//.findByAlbumSlugOrderByPosition(currentSlug);

        if (photos.isEmpty()) {
            event.rerouteToError(IllegalArgumentException.class,
                    "Album not found: " + currentSlug);
            return;
        }

        buildView();

        // Position viewer on the photo that matches the URL parameter
        currentIndex = findInitialIndex();
        updatePhotoImage(0);
        thumbnailStrip.setActiveIndex(currentIndex);

        initialLoad = true;
        if (!photos.isEmpty()) {
            try {
                currentPhotoId = Long.parseLong(photos.get(currentIndex).getColumnData("id"));
                loadInfoPanel(currentPhotoId);
            } catch (NumberFormatException ignored) {}
        }

        // If opened via "Rate it!" from a card or hero, switch to Rate tab immediately
        if (openOnRateTab && panelTabs != null) {
            panelTabs.setSelectedTab(tabRate);
            metaContent.setVisible(false);
            ratingContent.setVisible(true);
        }

        initialLoad = false;
    }

    // ── Build the full layout ─────────────────────────────────────────────────

    private void buildView() {
        removeAll();

//        String dirPhotos = getAppProps("dir-photos");

        DIR_PHOTOS_SERVER = genericView.getAppProps(PROP_PHOTOS);

        strPathLargePhotos = DIR_PHOTOS_SERVER + dirChar + subPathLarge;
        String strPathThumbs = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;

        // ── Photo frame: orientation-aware viewer with overlay nav/close ──────
        photoFrame = new PhotoFrameComponent();

        Div prevBtn  = navDiv("❮", "left");
        Div nextBtn  = navDiv("❯", "right");
        Div closeBtn = closeDiv();

        prevBtn.addClickListener(e -> {
            currentIndex = (currentIndex - 1 + photos.size()) % photos.size();
            updatePhotoImage(-1);
            updateNavState();
        });
        nextBtn.addClickListener(e -> {
            currentIndex = (currentIndex + 1) % photos.size();
            updatePhotoImage(+1);
            updateNavState();
        });
        closeBtn.addClickListener(e ->
                getUI().ifPresent(ui -> ui.getPage().executeJs("window.history.back()")));

        // Nav and close buttons are position:absolute — they overlay the image
        photoFrame.add(prevBtn, nextBtn, closeBtn);

        // ── Right info panel ──────────────────────────────────────────────────
        VerticalLayout infoPanel = buildInfoPanel();
        infoPanel.addClassName("plv-info-panel");
        infoPanel.setWidth("330px");
        infoPanel.setHeightFull();

        HorizontalLayout topSection = new HorizontalLayout(photoFrame, infoPanel);
        topSection.setSizeFull();
        topSection.setPadding(false);
        topSection.setSpacing(false);
        topSection.addClassName("plv-top-section");

        // ── Bottom: thumbnail filmstrip ───────────────────────────────────────
        thumbnailStrip = new ThumbnailStrip(photos, strPathThumbs, (index, photoId) -> {
            int dir = Integer.signum(index - currentIndex);
            currentIndex = index;
            currentPhotoId = photoId;
            updatePhotoImage(dir != 0 ? dir : +1);
            loadInfoPanel(photoId);
        });

        add(topSection, thumbnailStrip);
        setFlexGrow(1, topSection);
    }

    /** Updates the photo frame to show the photo at currentIndex. direction: +1 forward, -1 backward, 0 no animation. */
    private void updatePhotoImage(int direction) {
        if (photos.isEmpty()) return;
        Record photo = photos.get(currentIndex);
        String nameNew = photo.getColumnData("name_new");
        int w = parseIntSafe(photo.getColumnData("meta_i_width"));
        int h = parseIntSafe(photo.getColumnData("meta_i_height"));

        String notes = photo.getColumnData("notes");
        String subtitle = photo.getColumnData("subtitle");
        String description = (notes != null && !notes.isBlank() && !"null".equalsIgnoreCase(notes.trim()))
                ? notes
                : subtitle;
        photoFrame.setDescription(description);

        if (nameNew != null) {
            File file = Paths.get(strPathLargePhotos + dirChar + nameNew).toFile();
            if (file.exists()) {
                photoFrame.setPhoto(file, photo.getColumnData("title"), w, h);
                if (direction != 0) photoFrame.animateEnter(direction);
                return;
            }
            log.warn("Photo file not found: {}", strPathLargePhotos + dirChar + nameNew);
        }
        photoFrame.setFallback("/static/photographer.jpg");
    }

    private static int parseIntSafe(String s) {
        if (s == null) return 0;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    /** Syncs thumbnail highlight and info panel after arrow navigation. */
    private void updateNavState() {
        thumbnailStrip.setActiveIndex(currentIndex);
        if (!photos.isEmpty()) {
            try {
                currentPhotoId = Long.parseLong(photos.get(currentIndex).getColumnData("id"));
                loadInfoPanel(currentPhotoId);
            } catch (NumberFormatException ignored) {}
        }
    }

    /** Returns the index of the photo matching strPhotoId, or 0 if not found. */
    private int findInitialIndex() {
        if (!strPhotoId.isBlank()) {
            for (int i = 0; i < photos.size(); i++) {
                if (strPhotoId.equals(photos.get(i).getColumnData("id"))) return i;
            }
        }
        return 0;
    }

    /** Prev / next overlay button (left or right edge of the viewer). */
    private Div navDiv(String symbol, String side) {
        Div btn = new Div();
        btn.setText(symbol);
        btn.addClassNames("plv-nav-btn", "plv-nav-btn--" + side);
        return btn;
    }

    /** Close (✕) button overlaid in the top-right corner of the viewer. */
    private Div closeDiv() {
        Div btn = new Div();
        btn.setText("✕");
        btn.addClassName("plv-close-btn");
        return btn;
    }

    // ── Info panel (right VerticalLayout) ─────────────────────────────────────

    private VerticalLayout buildInfoPanel() {
        // Like button
        likeButton = new LikeButton(0);
        likeButton.setTitle("Like this photo");
        likeButton.addLikeClickListener(e -> handleLike());

        // Rate button — clicking it switches the panel to the Rate tab
        rateButton = new RateButton(0);
        rateButton.setTitle("Rate this photo");

        photoTitle.addClassName("plv-photo-title");
        authorSpan.addClassName("plv-author");
        exifGrid.addClassName("plv-exif-grid");
        tagsRow.addClassName("plv-tags-row");
        commentsDiv.setWidthFull();



        // ── Tabs (below the photographer card) ────────────────────────────────
        tabMeta = new Tab(FontAwesome.Solid.INFO.create(), new Span("Info"));
        tabRate = new Tab(FontAwesome.Solid.STAR.create(), new Span("Rate It"));
        panelTabs = new Tabs(tabMeta, tabRate);
        panelTabs.addClassName("plv-panel-tabs");

        // ── Info tab content: only EXIF + tags (title/author always visible above) ─
        metaContent = new VerticalLayout(exifGrid, tagsRow);
        metaContent.setPadding(false);
        metaContent.setSpacing(false);
        metaContent.addClassName("plv-meta-content");
        metaContent.setWidthFull();

        // ── Rating content pane (populated per photo in loadInfoPanel) ─────────
        ratingContent = new VerticalLayout();
        ratingContent.setPadding(false);
        ratingContent.setSpacing(false);
        ratingContent.addClassName("plv-rating-content");
        ratingContent.setWidthFull();
        ratingContent.setVisible(false);

        // Tab switch — show/hide content panes
        panelTabs.addSelectedChangeListener(e -> {
            boolean showRate = panelTabs.getSelectedTab() == tabRate;
            metaContent.setVisible(!showRate);
            ratingContent.setVisible(showRate);
        });

        // Rate button click → switch to Rate tab
        rateButton.addRateClickListener(e -> panelTabs.setSelectedTab(tabRate));

        // Panel: title + author card always on top, then tabs, then tab content
        VerticalLayout panel = new VerticalLayout(
                photoTitle, authorSpan,
                panelTabs,
                metaContent, ratingContent);
        panel.setWidthFull();
        panel.setPadding(true);
        panel.setSpacing(false);
        panel.addClassName("plv-info-panel-inner");

        if (isMobile) {
            panel.setVisible(false);
        } else {
            panel.setVisible(true);
        }

        return panel;
    }

    // ── Load info panel data for a given photoId ──────────────────────────────

    private void loadInfoPanel(long photoId) {

        Record photo = photos.stream()
                .filter(p -> String.valueOf(photoId).equals(p.getColumnData("id")))
                .findFirst()
                .orElse(photos.isEmpty() ? null : photos.get(currentIndex));

        String photoNameNew = "";
        if (photo != null) {
            String title = photo.getColumnData("title");
            photoTitle.setText(title != null ? title : "");

            String strUsername = photo.getColumnData("username");
            String strName     = photo.getColumnData("name");
            String strSurname  = photo.getColumnData("surname");
            String strAvatarPath   = photo.getColumnData("avatar_path");
            String strCountPhotos  = photo.getColumnData("count_photos");
            String strCountStories = photo.getColumnData("count_stories");

            photoNameNew = safe(photo.getColumnData("name_new"));

            authorSpan.removeAll();
            authorSpan.setWidthFull();
            authorSpan.add(fetchPhotographer(strUsername, strName, strSurname, strAvatarPath, strCountPhotos, strCountStories, false));
            populateTags(photo.getColumnData("contains_tags"));
            populateExif(photo);
        }

        if (photoViewService != null && likeButton != null) {
            likeButton.setCount(photoViewService.getLikeCount((int) photoId));
        }

        // Reset to Info tab and rebuild rating panel for the current photo
        if (panelTabs != null) {
            panelTabs.setSelectedTab(tabMeta);
            metaContent.setVisible(true);
            ratingContent.setVisible(false);
        }
        buildRatingPanel(photoId, photoNameNew);
        if (rateButton != null && photoRatingService != null) {
            rateButton.setCount(photoRatingService.getRatingCount((int) photoId));
        }

        // On navigation (not direct URL load): auto-select Rate tab if user already rated this photo
        if (!initialLoad && panelTabs != null && photoRatingService != null) {
            String authUserId = genericView.checkIfAuthMemberId();
            if (authUserId != null) {
                try {
                    int existingRating = photoRatingService.getUserRating(
                            (int) photoId, Integer.parseInt(authUserId));
                    if (existingRating > 0) {
                        panelTabs.setSelectedTab(tabRate);
                        metaContent.setVisible(false);
                        ratingContent.setVisible(true);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private VerticalLayout fetchPhotographer(String strUsername, String strName, String strSurname, String strAvatarPath,
                                             String strCountPhotos, String strCountStories, boolean showMinimum) {

        VerticalLayout layoutCreatorInfo = new VerticalLayout();
        layoutCreatorInfo.addClassNames(
                LumoUtility.Width.FULL, LumoUtility.Height.FULL,
                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.AlignItems.START, LumoUtility.JustifyContent.START);
//        layoutCreatorInfo.addClassNames("member-profile-design");
//        layoutCreatorInfo.addClassName("info-to-show");
        layoutCreatorInfo.setMaxHeight("160px");
//        layoutCreatorInfo.getStyle().setOpacity("1");



        Div divImgAvatar = new Div();
        divImgAvatar.addClassNames(LumoUtility.Padding.NONE, LumoUtility.Margin.NONE);

        String strAvatarSize = "50px";
        Image imageAvatar = getAvatarThumbImage(strAvatarPath, strUsername, strAvatarSize, strAvatarSize);
        divImgAvatar.add(imageAvatar);


        HorizontalLayout horizontalLayout = new HorizontalLayout();

        layoutCreatorInfo.getStyle().setOpacity("1");


        H4 objMember = new H4(strUsername);
        objMember.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.NORMAL, LumoUtility.FontSize.SMALL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.Gap.XSMALL);

        H4 objName = new H4(strName + " " + strSurname);
        objName.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.SMALL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.Gap.XSMALL);

//        Div divMemberSince = new Div("Member since "+strMemberSince);
//        divMemberSince.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.EXTRALIGHT, LumoUtility.FontSize.XSMALL,
//                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
//                LumoUtility.Gap.XSMALL);



        Icon iconPhoto = VaadinIcon.PICTURE.create();
        Icon iconAlbum = FontAwesome.Solid.PHOTO_FILM.create();
//        Span spPhotos = new Span(" Photos");
//        spPhotos.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.FontSize.SMALL);
        Span divPhotos = new Span(strCountPhotos);
//        divPhotos.add(spPhotos);
        divPhotos.addClassNames(LumoUtility.TextColor.SECONDARY);
//        Span spAlbums = new Span(" Albums");
//        spAlbums.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.FontSize.SMALL);
        Span divAlbums = new Span(strCountStories);
        divAlbums.addClassNames(LumoUtility.TextColor.SECONDARY);
//        divAlbums.add(spAlbums);

        HorizontalLayout layoutCounts = new HorizontalLayout();
        layoutCounts.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                LumoUtility.Padding.SMALL, LumoUtility.Margin.NONE,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.LARGE, LumoUtility.Background.CONTRAST_5,
                LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);
        layoutCounts.add(iconPhoto, divPhotos, iconAlbum, divAlbums);

        VerticalLayout layoutMemberCard = new VerticalLayout();
//            layoutMemberCard.getStyle().setMaxWidth("300px");
//            layoutMemberCard.getStyle().set("border", "lightgrey 1px solid");
        layoutMemberCard.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        layoutMemberCard.setMaxWidth("60px");
        layoutMemberCard.add(divImgAvatar);

//        Div divResidentCaption = new Div("Resident");
//        Div divResident = new Div(strResident);
//        divResident.addClassNames(LumoUtility.FontWeight.BOLD);

        VerticalLayout layoutAdditional = new VerticalLayout();
        layoutAdditional.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
                LumoUtility.Gap.XSMALL);
        layoutAdditional.add(objMember, objName); //, divBioTitle, divBio);//, divResidentCaption, divResident);

        horizontalLayout.add(layoutMemberCard, layoutAdditional);

        if(showMinimum){
            layoutCreatorInfo.add(horizontalLayout);
        }else {
            layoutCreatorInfo.add(horizontalLayout, layoutCounts);
        }

        return layoutCreatorInfo;
    }

    private Image getAvatarThumbImage(String strAvatarPath, String altDescr, String width, String height) {

        DIR_PHOTOS_SERVER = genericView.getAppProps(PROP_PHOTOS);

        String strAvatarFullPath = DIR_PHOTOS_SERVER + dirChar + SUB_PATH_AVATARS_THUMBS + dirChar + strAvatarPath;
        Path path = Paths.get(strAvatarFullPath);
        File file = path.toFile();

        Image image = new Image();
        image.setWidth(width);
        image.setHeight(height);
        image.addClassNames(LumoUtility.BorderRadius.FULL);
        image.setAlt(altDescr);
        image.setSrc(DownloadHandler.forFile(file));

        return image;
    }

    private void populateExif(Record photo) {
        exifGrid.removeAll();
        String cameraMake  = photo.getColumnData("meta_camera_make");
        String cameraModel = photo.getColumnData("meta_camera_model");
        String lensMake    = photo.getColumnData("meta_lens_make");
        String lensModel   = photo.getColumnData("meta_lens_model");
        String focal       = photo.getColumnData("meta_focal_length");
        String focalFF     = photo.getColumnData("meta_focal_length_ff");
        String iso         = photo.getColumnData("meta_iso");
        String aperture    = photo.getColumnData("meta_aperture");
        String shutter     = photo.getColumnData("meta_shutter_speed");
        String shot        = photo.getColumnData("photo_time_shot");
        String city        = photo.getColumnData("city_name");
        String width       = photo.getColumnData("meta_i_width");
        String height      = photo.getColumnData("meta_i_height");

        String camera = (safe(cameraMake) + " " + safe(cameraModel)).trim();
        if (!camera.isEmpty()) row("Camera", camera);
        String lens = (safe(lensMake) + " " + safe(lensModel)).trim();
        if (!lens.isEmpty()) row("Lens", lens);

        if (isPresent(focal)) {
            row("Focal", isPresent(focalFF) && !focalFF.equalsIgnoreCase(focal)
                    ? focal + " mm  (" + focalFF + " mm FF)"
                    : focal + " mm");
        }
        if (isPresent(aperture)) row("Aperture", "f/" + aperture);
        if (isPresent(shutter))  row("Shutter",  shutter + " s");
        if (isPresent(iso))      row("ISO",       iso);
        if (isPresent(shot))     row("Shot",      shot);
        if (isPresent(city))     row("Location",  city);
        if (isPresent(width) && isPresent(height))
            row("Size", width + " × " + height + " px");
    }

    private static boolean isPresent(String s) {
        return s != null && !s.isBlank() && !"null".equalsIgnoreCase(s);
    }

    private void row(String label, String value) {
        if (value == null || value.isBlank() || "null null".equals(value)) return;
        Span lbl = new Span(label);
        lbl.addClassName("plv-exif-label");
        Span val = new Span(value);
        val.addClassName("plv-exif-value");
        exifGrid.add(lbl, val);
    }

    private void populateTags(String keywords) {
        tagsRow.removeAll();
        if (keywords == null) return;
        for (String t : keywords.split(",")) {
            String tag = t.trim();
            if (tag.isEmpty()) continue;
            Span chip = new Span(tag);
            chip.getElement().getThemeList().add("badge pill");
            chip.addClassName("plv-tag-chip");
            chip.addClickListener(e ->
                    getUI().ifPresent(ui -> ui.navigate("search?tag=" + tag)));
            tagsRow.add(chip);
        }
    }

    // ── Rating panel ──────────────────────────────────────────────────────────

    private void buildRatingPanel(long photoId, String nameNew) {
        ratingContent.removeAll();

        int photoIdInt = (int) photoId;
        double avgRating  = photoRatingService != null ? photoRatingService.getAverageRating(photoIdInt)  : 0.0;
        long   ratingCount = photoRatingService != null ? photoRatingService.getRatingCount(photoIdInt) : 0;

        // ── Average summary row ───────────────────────────────────────────────
        Icon starIcon = VaadinIcon.STAR_O.create();
        starIcon.addClassName("plv-rate-star-icon");
        Span spanAvg = new Span(ratingCount > 0
                ? String.format("%.1f  (%d ratings)", avgRating, ratingCount)
                : "No ratings yet");
        spanAvg.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        HorizontalLayout layoutSummary = new HorizontalLayout(starIcon, spanAvg);
        layoutSummary.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Gap.SMALL, LumoUtility.Padding.XSMALL);

        // ── Auth check — panel is always visible but submit requires login ────
        String authUserId = genericView.checkIfAuthMemberId();
        if (authUserId == null) {
            Span loginMsg = new Span("Please log in to rate this photo.");
            loginMsg.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
                    LumoUtility.Padding.SMALL);
            loginMsg.addClassName("plv-login-to-rate");
            ratingContent.add(layoutSummary, loginMsg);
            return;
        }

        // ── Rating options ────────────────────────────────────────────────────
        String[] str1 = {"1 Snapshot",   "Unplanned capture with minimal intent, weak composition, and technical flaws."};
        String[] str2 = {"2 Basic",      "Technically acceptable image but lacks clear subject and visual direction."};
        String[] str3 = {"3 Competent",  "Clear subject, balanced exposure, showing emerging compositional awareness and control."};
        String[] str4 = {"4 Polished",   "Intentional composition, effective lighting, strong clarity, and cohesive visual storytelling."};
        String[] str5 = {"5 Compelling", "Powerful imagery with distinct vision, emotional impact, and confident artistic execution."};
        String[][] allRatings = {str5, str4, str3, str2, str1};

        RadioButtonGroup<String[]> radioGroup = new RadioButtonGroup<>();
        radioGroup.setWidthFull();
        radioGroup.setRenderer(new ComponentRenderer<>(strings -> {
            Span title = new Span(strings[0]);
            title.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.SMALL);
            Span desc = new Span(strings[1]);
            desc.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);
            Div col = new Div(title, desc);
            col.addClassName("plv-rating-option");
            return col;
        }));
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setItems(str5, str4, str3, str2, str1);

        // Pre-select the user's existing rating if any
        if (photoRatingService != null) {
            try {
                int userIdInt = Integer.parseInt(authUserId);
                int existing  = photoRatingService.getUserRating(photoIdInt, userIdInt);
                if (existing > 0 && existing <= allRatings.length) {
                    radioGroup.setValue(allRatings[allRatings.length - existing]);
                }
            } catch (NumberFormatException ignored) {}
        }

        Span spanStatus = new Span();
        spanStatus.addClassNames(LumoUtility.FontSize.SMALL);
        spanStatus.setVisible(false);

        final int    finalPhotoId    = photoIdInt;
        final String finalAuthUserId = authUserId;
        final String finalIp         = (publicIp != null && !publicIp.isBlank()) ? publicIp : "unknown";
        final String finalNameNew    = (nameNew != null) ? nameNew : "";

        Button btnRate = new Button("Submit Rating");
        btnRate.addClassName("btn-rate");
        btnRate.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        btnRate.setWidthFull();
        btnRate.addClickListener(event -> {
            String[] selected = radioGroup.getValue();
            if (selected == null) {
                spanStatus.setText("Please select a rating first.");
                spanStatus.getStyle().set("color", "var(--lumo-error-color)");
                spanStatus.setVisible(true);
                return;
            }
            int ratingValue = Character.getNumericValue(selected[0].charAt(0));
            if (photoRatingService != null) {
                try {
                    int userIdInt = Integer.parseInt(finalAuthUserId);
                    String sid         = VaadinSession.getCurrent().getSession().getId();
                    long   sessionMs   = VaadinSession.getCurrent().getSession().getCreationTime();
                    LocalDateTime sessionDt = utilsDate.calcDateTimeFromLongInLDT(sessionMs, "UTC");
                    photoRatingService.saveOrUpdateRating(finalPhotoId, userIdInt, ratingValue,
                            finalNameNew, finalIp, sid, sessionDt);
                    double newAvg   = photoRatingService.getAverageRating(finalPhotoId);
                    long   newCount = photoRatingService.getRatingCount(finalPhotoId);
                    spanAvg.setText(String.format("%.1f  (%d ratings)", newAvg, newCount));
                    rateButton.setCount(newCount);
                    spanStatus.setText("Rating saved!");
                    spanStatus.getStyle().set("color", "var(--lumo-success-color)");
                    spanStatus.setVisible(true);
                } catch (Exception ex) {
                    logger.error("Error saving rating: {}", ex.getMessage());
                    spanStatus.setText("Could not save rating. Please try again.");
                    spanStatus.getStyle().set("color", "var(--lumo-error-color)");
                    spanStatus.setVisible(true);
                }
            }
        });

        ratingContent.add(layoutSummary, radioGroup, btnRate, spanStatus);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void handleLike() {
        if (photoViewService == null || currentPhotoId == 0) return;
        int photoIdInt = (int) currentPhotoId;
        String nameNew = photos.stream()
                .filter(p -> String.valueOf(currentPhotoId).equals(p.getColumnData("id")))
                .map(p -> p.getColumnData("name_new"))
                .findFirst().orElse("");
        java.time.LocalDateTime sessionLdt =
                utilsDate.calcDateTimeFromLongInLDT(sessionCreation, "UTC");
        photoViewService.recordLike(photoIdInt, nameNew, null,
                publicIp, sessionid, sessionLdt);
        likeButton.setCount(photoViewService.getLikeCount(photoIdInt));
    }

    private void handleDownload() {
        getUI().ifPresent(ui -> ui.getPage().open(
                "/api/photos/" + currentPhotoId + "/download"));
    }

    private void handleShare() {
        Dialog d = new Dialog();
        d.setHeaderTitle("Share");
        TextField urlField = new TextField();
        urlField.setValue("https://yourdomain.com/photo/" + currentSlug);
        urlField.setReadOnly(true);
        urlField.setWidthFull();
        Button copy = new Button("Copy link", e -> {
            getElement().executeJs("navigator.clipboard.writeText($0)",
                    urlField.getValue());
            Notification.show("Link copied!");
            d.close();
        });
        copy.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        d.add(urlField, copy);
        d.open();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

    private Long getCurrentUserId() {
        return 0L; // replace with SecurityContext lookup
    }





    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {

        logger.info(" photo  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

    private void getUserClientInfo() {

        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        hostname = inetAddress.getHostName();
        hostAddress = inetAddress.getHostAddress();
        canonicalHostname = inetAddress.getCanonicalHostName();

        DIR_PHOTOS_SERVER = genericView.getAppProps(PROP_PHOTOS);

        sessionid = VaadinSession.getCurrent().getSession().getId();
        sessionCreation = VaadinSession.getCurrent().getSession().getCreationTime();
        isMobile = VaadinSession.getCurrent().getBrowser().isAndroid() || VaadinSession.getCurrent().getBrowser().isIPhone() || VaadinSession.getCurrent().getBrowser().isWindowsPhone();

        if (VaadinSession.getCurrent().getBrowser().isAndroid()) {
            strOS = "Android";
        } else if (VaadinSession.getCurrent().getBrowser().isIPhone()) {
            strOS = "iPhone";
        } else if (VaadinSession.getCurrent().getBrowser().isWindows()) {
            strOS = "Windows";
        } else if (VaadinSession.getCurrent().getBrowser().isLinux()) {
            strOS = "Linux";
        } else if (VaadinSession.getCurrent().getBrowser().isMacOSX()) {
            strOS = "Mac OS X";
        } else if (VaadinSession.getCurrent().getBrowser().isChromeOS()) {
            strOS = "ChromeOS";
        } else {
            strOS = "Unknown";
        }


        if (VaadinSession.getCurrent().getBrowser().isChrome()) {
            strBrowser = "Chrome";
        } else if (VaadinSession.getCurrent().getBrowser().isFirefox()) {
            strBrowser = "Firefox";
        } else if (VaadinSession.getCurrent().getBrowser().isEdge()) {
            strBrowser = "Edge";
        } else if (VaadinSession.getCurrent().getBrowser().isSafari()) {
            strBrowser = "Safari";
        } else if (VaadinSession.getCurrent().getBrowser().isOpera()) {
            strBrowser = "Opera";
        } else if (VaadinSession.getCurrent().getBrowser().isIE()) {
            strBrowser = "IE";
        } else {
            strBrowser = "not known";
        }


        UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
            if (extendedClientDetails == null) {
                logger.info("Image gallery - error timeZoneId: Cannot retrieve client details:" + extendedClientDetails);
                return;
            }
            timeZoneId = extendedClientDetails.getTimeZoneId();
        });

        sessionDateTime = utilsDate.calcDateTimeFromLong(sessionCreation, "UTC");
        Locale loc = VaadinService.getCurrentRequest().getLocales().nextElement();
        locale = loc.getLanguage() + "." + loc.getCountry();
        localeName = loc.getDisplayName();

        NetUtils netUtils = new NetUtils();
        publicIp = netUtils.getClientPublicIp(hostname);

        final String[] urlHost = {"", "", "", "", "", "", "", ""};

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously
            urlHost[0] = currentUrl.getHost();
            urlHost[1] = currentUrl.getProtocol();
            urlHost[2] = currentUrl.getRef();
            urlHost[3] = currentUrl.getUserInfo();
            urlHost[4] = currentUrl.toExternalForm();
            urlHost[5] = currentUrl.getPort() + "";
            urlHost[6] = currentUrl.getAuthority();
            urlHost[7] = currentUrl.getQuery();

            logger.info("  url:" + urlHost[0] + "  url:" + urlHost[1] + "  url:" + urlHost[2] + "  url:" + urlHost[3] + "  url:" + urlHost[4]
                    + "  url:" + urlHost[5] + "  url:" + urlHost[6] + "  url:" + urlHost[7]);
        });

    }

}