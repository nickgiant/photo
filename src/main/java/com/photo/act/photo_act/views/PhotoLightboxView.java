package com.photo.act.photo_act.views;



import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.PhotoViewService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.LikeButton;
import com.photo.act.photo_act.views.components.PhotoFrameComponent;
import com.photo.act.photo_act.views.components.ThumbnailStrip;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.*;

import static com.photo.act.photo_act.views.AlbumsView.subPathThumbs;
import static com.photo.act.photo_act.views.HomeView.subPathLarge;
import static com.photo.act.photo_act.views.MainLayout.PROP_PHOTOS;

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
//@RouteAlias(value = "photo/:photo-id?", layout = MainLayout.class)
@PageTitle("Photo Viewer")
@Slf4j

public class PhotoLightboxView extends VerticalLayout
        implements BeforeEnterObserver {

    private static final Logger logger = LoggerFactory.getLogger(PhotoLightboxView.class);
    private String dirChar = FileSystems.getDefault().getSeparator();
    private RecordService recordService;

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final PhotoViewService  photoViewService;

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
    private final Button downloadBtn = new Button("Download");
    private final Button shareBtn    = new Button("Share");
    private final Div   commentsDiv  = new Div();

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
    private String sqlReadGallery1OrderBy = " ORDER BY pm.date_inserted DESC  ";
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
                             PhotoViewService photoViewService) {
        this.recordService    = recordService;
        this.photoViewService = photoViewService;

        // Outer VerticalLayout: full screen, no padding/gap
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("overflow", "hidden");

        utilsDate = new UtilsDate();
        genericView = new GenericView(recordService);



    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        currentSlug = event.getRouteParameters().get("slug").orElse("");
        strPhotoId = event.getRouteParameters().get("photo-id").orElse("");

        String[] arrNames = null;
        String sqlRead = "";

        getUserClientInfo();

        String sqlReadAppConfig = "SELECT app, host, propName, propValue FROM dbinfo WHERE host like '" + hostname + "' ";
        String[] arrCols = {"propName", "propValue"};
        recProps = recordService.findAll(sqlReadAppConfig, arrCols);


        String strFilterColumn = "";

        int isType = 2;
        String strSelection = "";
        String sqlReadOrderBy = " ORDER BY pm.date_inserted DESC";

        if (isType == 1) {
/*            arrNames = arrAlbumNames;
            sqlRead = sqlReadAlbums + " AND usr.username = '" + strAlbumUsername + "' " + sqlReadAlbumsOrderby;
            strFilterColumn = "a.title";*/
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

        if (strSelection.isEmpty()) {
            if (isType == 2 || isType == 3) {
                sqlReadPhotos = sqlRead;
            } else {
                sqlReadPhotos = sqlRead + " " + sqlReadOrderBy;
            }
        } else {
            if (isType == 2 || isType == 3) {
                sqlReadPhotos = sqlRead + " AND " + strFilterColumn + " LIKE '" + strSelection + "' ";
            } else if (isType == 1) {
                sqlReadPhotos = sqlRead + " AND " + strFilterColumn + " LIKE '" + strSelection + "' ";
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

        if (!photos.isEmpty()) {
            try {
                currentPhotoId = Long.parseLong(photos.get(currentIndex).getColumnData("id"));
                loadInfoPanel(currentPhotoId);
            } catch (NumberFormatException ignored) {}
        }
    }

    // ── Build the full layout ─────────────────────────────────────────────────

    private void buildView() {
        removeAll();

        String dirPhotos = getAppProps("dir-photos");
        strPathLargePhotos = (dirPhotos != null ? dirPhotos : "") + dirChar + subPathLarge;
        String strPathThumbs = (dirPhotos != null ? dirPhotos : "") + dirChar + subPathThumbs;

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
        infoPanel.setWidth("220px");
        infoPanel.setHeightFull();
        infoPanel.getStyle()
                .set("flex-shrink", "0")
                .set("border-left", "0.5px solid var(--lumo-contrast-10pct)")
                .set("overflow-y", "auto");

        HorizontalLayout topSection = new HorizontalLayout(photoFrame, infoPanel);
        topSection.setSizeFull();
        topSection.setPadding(false);
        topSection.setSpacing(false);
        topSection.getStyle()
                .set("min-height", "0")
                .set("flex", "1 1 auto");

        // ── Bottom: thumbnail filmstrip ───────────────────────────────────────
        thumbnailStrip = new ThumbnailStrip(photos, strPathThumbs, (index, photoId) -> {
            int dir = Integer.signum(index - currentIndex);
            currentIndex = index;
            currentPhotoId = photoId;
            updatePhotoImage(dir != 0 ? dir : +1);
            loadInfoPanel(photoId);
        });
        thumbnailStrip.setWidthFull();

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
        if (nameNew != null) {
            File file = Paths.get(strPathLargePhotos + dirChar + nameNew).toFile();
            if (file.exists()) {
                photoFrame.setPhoto(file, photo.getColumnData("title"), w, h);
                if (direction != 0) photoFrame.animateEnter(direction);
                return;
            }
            log.warn("Photo file not found: {}", strPathLargePhotos + dirChar + nameNew);
        }
        photoFrame.setFallback("/static/photographerM.jpg");
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
        btn.getStyle()
                .set("position", "absolute")
                .set("top", "50%")
                .set("transform", "translateY(-50%)")
                .set(side, "0")
                .set("z-index", "10")
                .set("background", "rgba(0,0,0,0.45)")
                .set("color", "#fff")
                .set("width", "44px")
                .set("height", "80px")
                .set("font-size", "30px")
                .set("cursor", "pointer")
                .set("border-radius", "3px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("user-select", "none");
        return btn;
    }

    /** Close (×) button overlaid in the top-right corner of the viewer. */
    private Div closeDiv() {
        Div btn = new Div();
        btn.setText("✕");
        btn.getStyle()
                .set("position", "absolute")
                .set("top", "10px")
                .set("right", "10px")
                .set("z-index", "10")
                .set("background", "rgba(0,0,0,0.55)")
                .set("color", "#fff")
                .set("width", "38px")
                .set("height", "38px")
                .set("font-size", "20px")
                .set("cursor", "pointer")
                .set("border-radius", "50%")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("user-select", "none");
        return btn;
    }

    // ── Info panel (right VerticalLayout) ─────────────────────────────────────

    private VerticalLayout buildInfoPanel() {
        // Like button
        likeButton = new LikeButton(0); // count is refreshed in loadInfoPanel()
        likeButton.setTitle("Like this photo");
        likeButton.addLikeClickListener(e -> handleLike());

        downloadBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        downloadBtn.setWidthFull();
        downloadBtn.addClickListener(e -> handleDownload());

        shareBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        shareBtn.setWidthFull();
        shareBtn.addClickListener(e -> handleShare());

        photoTitle.getStyle().set("margin", "0").set("font-size", "1rem");
        authorSpan.getStyle().set("font-size", "0.8rem")
                .set("color", "var(--lumo-secondary-text-color)");

        exifGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "auto 1fr")
                .set("gap", "2px 8px")
                .set("font-size", "0.75rem");

        tagsRow.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "4px");

        commentsDiv.setWidthFull();

        Select<String> effectSelect = new Select<>();
        effectSelect.setLabel("Transition effect");
        effectSelect.setItems("fade", "zoom", "slide", "none");
        effectSelect.setValue("fade");
        effectSelect.setWidthFull();
        effectSelect.addValueChangeListener(e -> {
            if (photoFrame != null) photoFrame.setEffect(e.getValue());
        });

        VerticalLayout panel = new VerticalLayout(
                photoTitle, authorSpan,
                new Hr(),
                likeButton, downloadBtn, shareBtn,
                new Hr(),
                effectSelect,
                new Hr(),
                new H4("Camera info"), exifGrid,
                new H4("Tags"), tagsRow,
                new Hr(),
                new H4("Comments"), commentsDiv
        );
        panel.setPadding(true);
        panel.setSpacing(false);
        panel.getStyle().set("gap", "6px");

        return panel;
    }

    // ── Load info panel data for a given photoId ──────────────────────────────

    private void loadInfoPanel(long photoId) {
        if (photoViewService != null && likeButton != null) {
            likeButton.setCount(photoViewService.getLikeCount((int) photoId));
        }
    }

/*    private void populateExif(PhotoMetadata m) {
        exifGrid.removeAll();
        row("Camera",   safe(m.getCameraMake()) + " " + safe(m.getCameraModel()));
        row("Lens",     safe(m.getLens()));
        row("Focal",    m.getFocalLength() != null ? m.getFocalLength() + " mm" : "—");
        row("Aperture", m.getAperture()    != null ? "f/" + m.getAperture()     : "—");
        row("Shutter",  safe(m.getShutterSpeed()) + " s");
        row("ISO",      m.getIso()         != null ? String.valueOf(m.getIso()) : "—");
        if (m.getShootDate() != null)
            row("Date", m.getShootDate().toLocalDate().toString());
    }*/

    private void row(String label, String value) {
        if (value == null || value.isBlank() || "null null".equals(value)) return;
        Span lbl = new Span(label);
        lbl.getStyle().set("color", "var(--lumo-secondary-text-color)");
        exifGrid.add(lbl, new Span(value));
    }

    private void populateTags(String keywords) {
        tagsRow.removeAll();
        if (keywords == null) return;
        for (String t : keywords.split(",")) {
            String tag = t.trim();
            if (tag.isEmpty()) continue;
            Span chip = new Span(tag);
            chip.getElement().getThemeList().add("badge pill");
            chip.getStyle().set("cursor", "pointer");
            chip.addClickListener(e ->
                    getUI().ifPresent(ui -> ui.navigate("search?tag=" + tag)));
            tagsRow.add(chip);
        }
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



    public String getAppProps(String prop) {
        for (int r = 0; r < recProps.size(); r++) {
            String strProp = recProps.get(r).getColumnData("propName");
            if (prop.equalsIgnoreCase(strProp)) {
                return recProps.get(r).getColumnData("propValue");
            }
        }
        return null;
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