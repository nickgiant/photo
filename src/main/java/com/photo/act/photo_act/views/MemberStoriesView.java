package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.dto.StoryMapPointDto;
import com.photo.act.photo_act.services.*;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.SlugUtil;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.GalleryImageViewCard;
import com.photo.act.photo_act.views.components.GenericView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.*;

import static com.photo.act.photo_act.views.HomeView.subPathSmall;
import static com.photo.act.photo_act.views.MainLayout.*;


//@RolesAllowed("Admin")
@PermitAll

@Route(value = "member-stories") //":section?")
//@RouteAlias(value = "members/name/:member?", layout = MainLayout.class)
//@RouteAlias(value = ":section/:member?", layout = MainLayout.class)
//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class MemberStoriesView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

    private static final Logger logger = LoggerFactory.getLogger(MemberStoriesView.class);
    private final PhotoStoryService photoStoryService;

    private ShareService shareService;
    private ShareMetricService shareMetricService;
    private WeatherService weatherService;
    private PhotoRatingService photoRatingService;
    private PhotoViewService photoViewService;

    public static final String subPathThumbs = "photo-thumbs";
    public static final String subPathSmall = "photo-small";
    public static final String subPathMedium = "photo-medium";
    public static final String subPathLarge = "photo-large";
    public static final String subPathUpload = "photo-upload";
    public static final String subPathShow = "photo-show";

    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
    String[] arrColumnsMemberExists = {"userId", "username", "username", "resident", "date_joined", "member_since", "avatar_path"};
    String sqlDoesMemberExist = "SELECT usr.userId, usr.username, usr.username, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined " +
            " , DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since, usr.avatar_path " +
            " FROM dbuser usr " +
            " WHERE 1 = 1 ";
    String[] arrColumnsMember = {"userId", "username", "resident", "resident_country", "date_joined", "member_since", "member_for", "avatar_path",
            "name", "surname", "short_bio", "url_insta", "url_fb", "url_flickr", "url_yt", "url_website", "email", "resident", "resident_country"};
    String sqlMember = "SELECT " +
            "  usr.userId, usr.username, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined,  DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
            " , usr.avatar_path, usr.name, usr.surname, usr.email, usr.resident, usr.resident_country " +
            " , usr.short_bio, usr.url_fb, usr.url_yt, usr.url_insta, usr.url_flickr, usr.url_website " +
            " FROM dbuser usr " +
            " WHERE 1 = 1 ";
    String[] arrColumnsMembers = {"userId", "username", "username", "resident", "date_joined", "member_since", "member_for",
            "avatar_path", "name", "surname", "short_bio", "url_insta", "url_fb", "url_flickr", "url_yt", "email", "resident", "resident_country",
            "count_photos", "count_stories", "count_learnings_ref"};
    String sqlMembers = "SELECT " +
            "  usr.userId, usr.username, usr.username, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined,  " +
            " DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since , getDateDiffFromNowGr(usr.date_joined) AS member_for " +
            " , usr.avatar_path, name, surname, short_bio, url_insta, url_fb, url_flickr, url_yt, email, resident, resident_country " +
            " , usrx.count_photos, usrx.count_stories, usrx.count_learnings_ref " +
            //     "--  , pa.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
            " FROM dbuser usr, dbuser_extra usrx " +
            " WHERE usr.userId = usrx.user_id  " +
            " AND usertype <> 'Guest' " +
            " ORDER BY username ";
    private final String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";
    private final RecordService recordService;
    //            " AND pm.visible_to  = 'ALL' ";
    String sqlMemberPhotosGroupBy =
            " GROUP BY usr.userid " +
                    " ORDER BY usr.userid ASC ";
    //    private String sqlMemberGallery1 =
//            " SELECT pm.id, pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.creator, pm.visible_to,  DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, DATE_FORMAT(pm.meta_date, '%M %Y') AS photo_date, DATE_FORMAT(pm.meta_date, '%d/%m/%Y - %H:%i:%S') AS photo_time_shot,  pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model,  pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed, meta_orientation  , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
//                    " , usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
//                    " , d.city_name , NULL AS subject_name " +
//                    " FROM dbuser usr, photo_meta pm " +
//                    " LEFT JOIN destination d ON pm.destination_id = d.id " +
//                    " WHERE pm.uploaderId = usr.userId AND pm.visible_to = 'ALL' ";
//    private String sqlMemberGallery1OrderBy = " ORDER BY pm.date_inserted DESC  ";
//
//    private String sqlMemberGallery2 =
//            " SELECT pm.id, pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.creator, pm.visible_to,  DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, DATE_FORMAT(pm.meta_date, '%M %Y') AS photo_date, DATE_FORMAT(pm.meta_date, '%d/%m/%Y - %H:%i:%S') AS photo_time_shot,  pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model,  pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed, meta_orientation  , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
//                    " , usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
//                    " , NULL as city_name, s.subject_name " +
//                    " FROM dbuser usr, photo_meta pm " +
//                    " RIGHT JOIN  subject s ON s.id = pm.subject_id " +
//                    " WHERE pm.uploaderId = usr.userId AND pm.visible_to = 'ALL' ";
//    private String sqlMemberGallery2OrderBy = " ORDER BY pm.date_inserted DESC ";
    private final String dirChar = FileSystems.getDefault().getSeparator();
    private final String[] arrColumnMemberGallery = {"id", "name_new", "title", "subtitle", "photo_type", "uploader", "creator", "visible_to", "meta_date", "photo_date", "photo_time_shot"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso", "meta_aperture", "meta_shutter_speed", "meta_orientation"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon", "date_inserted"
            , "destination_id", "subject_id", "user_rights_id"
            , "city_name"
            , "subject_name", "subject_description", "subject_type"
            , "username", "surname", "name", "resident", "date_joined", "avatar_path"
    };
    private final String sqlMemberGallery =
            " SELECT pm.id, pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.creator, pm.visible_to,  DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, " +
                    "DATE_FORMAT(pm.meta_date, '%M %Y') AS photo_date, DATE_FORMAT(pm.meta_date, '%d/%m/%Y - %H:%i:%S') AS photo_time_shot,  pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model,  pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed, meta_orientation  , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
                    " , DATE_FORMAT(pm.date_inserted, '%d/%m/%Y - %H:%i:%S') AS date_inserted " +
                    " , pm.destination_id, pm.subject_id, user_rights_id " +
                    " , usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
                    " FROM dbuser usr, photo_meta pm " +
                    " WHERE pm.uploaderId = usr.userId AND pm.visible_to = 'ALL' ";
    private final String sqlMemberGalleryOrderBy = " ORDER BY pm.date_inserted DESC  ";
    private final String section = SECTION_MEMBERS;
    private final String[] arrClubsColumnNames = {"org_name", "org_type", "org_type_parent", "city", "used_for", "country", "url", "url_local_events", "url_fb", "url_yt", "url_insta",
            "url_flickr", "url_wikipedia"};
    private VerticalLayout verticalLayout;
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String forMemberName;
    private final String sqlShowClubsSelect = "SELECT id, org_name, org_type, org_type_parent , city , used_for , country , url , city, address, pc, country, map_x, map_y, item_url, " +
            " url_local_events, url_fb, url_yt, url_insta, url_flickr, url_wikipedia, " +
            " date_inserted, dateUpdated " +
            " FROM organizations o ";
    private String strHeader;

    //    private String[] arrColumnMemberGalleryCount = {"countOfMemberPhotos"
//            , "username", "surname", "name", "resident", "date_joined", "avatar_path"};
//
//    private String sqlMemberGalleryCount =
//            " SELECT COUNT(pm.id) AS countOfMemberPhotos, " +
//                    "  usr.userId, usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
//                    " FROM dbuser usr, photo_meta pm " +
//                    " WHERE pm.uploaderId = usr.userId AND pm.visible_to = 'ALL' ";
    private final String sqlShowClubsWhere = " WHERE o.org_type LIKE 'Club' ";
    private String publicIp;
    private String strPath;
    private String hostname;
    private String hostAddress;
    private String canonicalHostname;
    private int intUserId;
    private final String sqlShowClubsOrder = " ORDER BY o.city ASC, o.org_name ASC";
    private final String[] arrColumnNamesGallery = {"name_new", "photo_title", "subtitle", "photo_type", "uploader", "city_name", "meta_date"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon"
            , "date_inserted"};
    private final String sqlReadGallery = "SELECT pm.name_new, pm.title AS photo_title, pm.subtitle, pm.photo_type, pm.uploader, d.city_name, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, " +
            " pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model, " +
            " pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, " +
            "  pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon" +
            " , DATE_FORMAT(pm.date_inserted, '%d/%m/%Y - %H:%i:%S') AS date_inserted " +
            //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
            " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id ";
    private String strUrlRequestToBeLogged;
    private final String[] arrColStoryItems = {
            "user_id", "id", "state", "title", "description",  "story_visible_to", "category_id", "photo_id1", "photo_id2", "photo_id3", "photo_id4", "date_started", "date_ended", "date_published", "date_inserted"
            , "id", "cat_title",  "cat_description_min",  "cat_description_big", "cat_order"
            , "story_item_id", "story_id", "id", "photo_id", "inc", "item_type", "item_title", "descr", "item_url", "weather_at", "weather_when"
            , "name_new", "photo_title", "subtitle", "uploader", "meta_date ", "meta_orientation"
    };
    private String strMember;
    private final String sqlReadStoryItems = "SELECT "
            + " s.user_id, s.id, state, s.title, description, story_visible_to, category_id, photo_id1, photo_id2, photo_id3, photo_id4, date_started, date_ended, date_published, s.date_inserted "
            + " , cat_title,  cat_description_min, cat_description_big, cat_order "
            + " , i.id AS story_item_id, story_id, photo_id, inc, item_type,  item_title, descr, i.item_url, weather_at, weather_when "
            + " , pm.name_new, pm.title AS photo_title, pm.subtitle, pm.photo_type, pm.uploader, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, meta_orientation "
            + " FROM photo_stories s, photo_stories_categories c, photo_stories_photo i LEFT JOIN photo_meta pm ON i.photo_id = pm.id "
            + " WHERE s.id = i.story_id AND c.id = s.category_id "
            + " AND s.user_id = i.user_id "
            + " AND c.cat_visible = 1 ";
    private final String sqlReadStoryItemsOrderby = " ORDER BY s.date_inserted, story_id, inc ";
    private final UtilsDate utilsDate;
    private final GenericView genericView;
    private final EmailSendService emailSendService;
    String[] arrColumnsMemberCountPhotos = {"photo_count", "photo_size",
            "userId", "username", "name", "surname", "resident", "resident_country", "date_joined", "member_since", "avatar_path",
            "short_bio", "url_fb", "url_yt", "url_insta", "url_flickr", "url_website",
            "count_photos", "count_stories", "count_learnings_ref"
    };
    String sqlMemberCountPhotos = "SELECT count(pm.id) AS photo_count, SUM(pm.space_size) AS photo_size " +
            " ,  usr.userId, usr.username, usr.resident, usr.resident_country, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined,  DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
            " , usr.avatar_path, usr.name, usr.surname " +
            " , usr.short_bio, usr.url_fb, usr.url_yt, usr.url_insta, usr.url_flickr, usr.url_website " +
            " , usrx.count_photos, usrx.count_stories, usrx.count_learnings_ref " +
            //     "--  , pa.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
            " FROM dbuser_extra usrx, dbuser_rights usrr, dbuser usr LEFT JOIN photo_meta pm ON pm.uploaderId = usr.userId " +
            " WHERE usr.userId = usrx.user_id  AND usrr.id = usr.user_rights_id" +
            " AND usrr.role <> 'Guest' ";
    String[] arrColumnsMemberAlbums = {"id", "title", "description",  "story_visible_to", "category_id", "user_id"
            , "username", "name", "surname", "resident", "date_joined", "member_since", "avatar_path"
    };
    String sqlMemberOfAlbums = "SELECT s.id, s.title,  s.description,  s.story_visible_to, s.category_id " +
            " , s.user_id " +
            " , usr.username, usr.name, usr.surname, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined " +
            " , DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
            " , usr.avatar_path " +
            " FROM dbuser usr, photo_stories s " +
            " WHERE s.user_id = usr.userId " +
             " AND s.is_article = 0 " +
            " AND s.story_visible_to = 'ALL' ";
    String sqlMemberOfAlbumsOrderBy =                   " ORDER BY s.date_inserted DESC ";
    private String sessionDateTime;

    private String strOS;
    private String strBrowser;
    String[] arrAlbumCategoriesColumns = {"id", "cat_title",  "cat_description_min"};
    String sqlAlbumCategories = "SELECT id, cat_title, cat_description_min " +
            " FROM photo_stories_categories pc " +
            " WHERE 1=1 AND pc.cat_visible = 1 " +
            " ORDER BY cat_title ASC";
    private String strSelectedStoryId = "";

    private ListBox<String> listBoxAlbums;

    private Grid<Map<String, Object>> grid;

    private String strSelectedPhotoId = "";
    private String strSelectedStoryItemId = "";
    private int pageSize = 5;   // must match grid.setPageSize(...)
    private int currentPage = 0;
    private int totalRows = 0;
    private int totalPages = 0;

    private Button firstBtn;
    private Button prevBtn;
    private Button nextBtn;
    private Button lastBtn;
    private Span pageInfoTop;

    private Map<String, Object> draggedItem;

    private VerticalLayout layoutStoryItems;
    private StoryMapService storyMapService;

    public MemberStoriesView(PhotoStoryService photoStoryService, RecordService recordService, EmailSendService emailSendService, ShareService shareService, ShareMetricService shareMetricService, WeatherService weatherService, PhotoRatingService photoRatingService, PhotoViewService photoViewService, StoryMapService storyMapService) {
        this.photoStoryService = photoStoryService;
        this.recordService = recordService;
        this.emailSendService = emailSendService;
        this.shareService = shareService;
        this.shareMetricService = shareMetricService;
        this.weatherService = weatherService;
        this.photoRatingService = photoRatingService;
        this.photoViewService = photoViewService;
        this.storyMapService = storyMapService;

        utilsDate = new UtilsDate();
        genericView = new GenericView(recordService);

        constructUI();

    }


    @Override
    public String getPageTitle() {
        return APP_NAME + " " + strHeader;
    }

    @Override
    public void beforeEnter(@OptionalParameter BeforeEnterEvent event) {
//        strMember = event.getRouteParameters().get("member").orElse("all-members");

        getUserClientInfo();

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously
            strUrlRequestToBeLogged = currentUrl.toExternalForm();
        });

        verticalLayout.removeAll();
        verticalLayout.add(loadHeader("Photo-Stories", "Create your Stories with Photos, Text and Video", ""));

        String sqlMembers = sqlMemberCountPhotos + " " + sqlMemberPhotosGroupBy;
        //       verticalLayout.add(getMembersPanels(sqlMembers, arrColumnsMemberPhotos, false));

        String usrName = genericView.checkIfAuthUserName();
        strMember = usrName;
        String strMemberId = genericView.checkIfAuthMemberId();
        String sqlMemberMe = sqlMember + " AND usr.username = '" + strMember + "' ";


        sqlMemberOfAlbums = sqlMemberOfAlbums + "  AND usr.username = '" + strMember + "' ";

//        sqlMemberGallery = "( " + sqlMemberGallery1 + "  AND usr.username = '" + strMember + "' " + sqlMemberGallery1OrderBy +
//                ") UNION (" + sqlMemberGallery2 + "  AND usr.username = '" + strMember + "' " + sqlMemberGallery2OrderBy + " ) ";

//        Div layoutMemberNAlbums = new Div();
//        layoutMemberNAlbums.addClassNames(
//                Display.FLEX, FlexDirection.COLUMN,
//                FlexDirection.Breakpoint.Medium.ROW, Gap.MEDIUM,
//
//                AlignItems.CENTER, JustifyContent.EVENLY,
//                Margin.MEDIUM, Padding.LARGE,
//                Width.FULL);
        // layoutMemberNAlbums.add(loadMemberInfo(sqlMemberMe, arrColumnsMember, false));
        // layoutMemberNAlbums.add(loadStoriesInfoPanel(sqlMemberOfAlbums, arrColumnsMemberAlbums, strMemberId));

//        verticalLayout.add(layoutMemberNAlbums);

        Dialog dlgStorySelection = new Dialog();
        dlgStorySelection.setCloseOnEsc(true);
        dlgStorySelection.setDraggable(true);
        dlgStorySelection.setCloseOnOutsideClick(true);
        dlgStorySelection.setResizable(true);
        dlgStorySelection.setMinWidth("350px");

        HorizontalLayout layoutTitleStory = new HorizontalLayout();
        layoutTitleStory.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.BETWEEN);
        Span spTitleStory = new Span("Επιλέξτε");

        Dialog dlgPhotoSelection = new Dialog();
        dlgPhotoSelection.setHeightFull();
        dlgPhotoSelection.setMinWidth("1000px");
        dlgPhotoSelection.setCloseOnEsc(true);
        dlgPhotoSelection.setDraggable(true);
        dlgPhotoSelection.setCloseOnOutsideClick(true);
        dlgPhotoSelection.setResizable(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.BETWEEN);
        Span spTitle = new Span("Choose a photo");

        Button btnClose = new Button();
        btnClose.setIcon(VaadinIcon.CLOSE_BIG.create());
        btnClose.addClickListener(clickEvent -> {
            dlgPhotoSelection.close();
        });

        layoutTitle.add(spTitle, btnClose);

        String sqlGallery = sqlMemberGallery + "  AND usr.username = '" + strMember + "' ";
        dlgPhotoSelection.add(layoutTitle);
        dlgPhotoSelection.add(loadPhotos(sqlGallery, arrColumnMemberGallery));

        layoutStoryItems = new VerticalLayout();
        layoutStoryItems.addClassName("stories-view");

        String sqlMemberStoriesInit = sqlMemberOfAlbums + sqlMemberOfAlbumsOrderBy;
        List<Record> lstStoriesInit = getRecordsFromDb(sqlMemberStoriesInit, arrColumnsMemberAlbums);

        String initStoryId = "0";
        String initStoryTitle = "";
        if (!lstStoriesInit.isEmpty()) {
            initStoryId = lstStoriesInit.get(0).getColumnData("id");
            initStoryTitle = lstStoriesInit.get(0).getColumnData("title");
        }

        layoutStoryItems.add(loadStoryItems(dlgStorySelection, sqlReadStoryItems, arrColStoryItems, dlgPhotoSelection, initStoryId, initStoryTitle, strMemberId));

        HorizontalLayout layoutControls = new HorizontalLayout();
        layoutControls.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);
        Button btnSelectStory = new Button("Select");
        btnSelectStory.setIcon(FontAwesome.Regular.CHECK_SQUARE.create());
        btnSelectStory.addClickListener(clickEvent -> {

            String sqlMemberStories = sqlMemberOfAlbums + sqlMemberOfAlbumsOrderBy;
            List<Record> lstStories = getRecordsFromDb(sqlMemberStories, arrColumnsMemberAlbums);
            List<String> lstStoriesTitle = new ArrayList<>();
            List<String> lstStoriesId = new ArrayList<>();
            for (int i = 0; i < lstStories.size(); i++) {
                lstStoriesTitle.add(lstStories.get(i).getColumnData("title"));
                lstStoriesId.add(lstStories.get(i).getColumnData("id"));
            }

            String strStoryTitle = "";
            for (int i = 0; i < lstStoriesTitle.size(); i++) {
                if (lstStoriesTitle.get(i).equalsIgnoreCase(listBoxAlbums.getValue())) {
                    strSelectedStoryId = lstStoriesId.get(i);
                    strStoryTitle = lstStoriesTitle.get(i);
                }
            }

            layoutStoryItems.removeAll();
            layoutStoryItems.add(loadStoryItems(dlgStorySelection, sqlReadStoryItems, arrColStoryItems, dlgPhotoSelection, strSelectedStoryId, strStoryTitle, strMemberId));
            dlgStorySelection.close();
        });

        Button btnCloseStory = new Button();
        btnCloseStory.setIcon(VaadinIcon.CLOSE_BIG.create());
        btnCloseStory.addClickListener(clickEvent -> {
            dlgStorySelection.close();
        });

        layoutTitleStory.add(spTitleStory, btnCloseStory);

        VerticalLayout layoutStories = loadStoriesPanel(sqlMemberOfAlbums, sqlMemberOfAlbumsOrderBy, arrColumnsMemberAlbums, strMemberId);


        layoutControls.add(btnSelectStory);
        dlgStorySelection.add(layoutTitleStory, layoutStories, layoutControls);

        layoutStoryItems.addClassName("member-main-layout");
/*        MemberMenu memberMenu = new MemberMenu(recordService,isMobile);
        Div divMenu = memberMenu.getMemberMenu();*/
//        divMenu.addClassName("member-menu-panel");
        Div layoutMenuWithList = new Div();
        layoutMenuWithList.addClassNames(
                Display.FLEX, FlexDirection.COLUMN,
                FlexDirection.Breakpoint.Medium.ROW, Gap.SMALL,

                Width.FULL,
                AlignItems.START, JustifyContent.AROUND,
                Margin.NONE, Padding.MEDIUM);
        layoutMenuWithList.setMaxWidth("1300px");

        layoutMenuWithList.add(layoutStoryItems);
        verticalLayout.add(layoutMenuWithList);

        this.removeAll();
        this.add(verticalLayout);
        this.add(genericView.loadFooter(isMobile));

        logVisitorToDb("");

    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String o) {
//        section = o;//beforeEvent.getRouteParameters().get("section").orElse("pictures");
    }

    private void constructUI() {
        addClassNames(Overflow.HIDDEN, Width.FULL,
                // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                Margin.NONE,
                Padding.NONE,
                Gap.MEDIUM,
                //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
                AlignItems.CENTER, JustifyContent.CENTER
        );
        this.addClassName("background");


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

        listBoxAlbums = new ListBox<>();

        verticalLayout = new VerticalLayout();
        verticalLayout.setId("verticalLayout");
        if (isMobile) {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.NONE,
                    Padding.Top.XSMALL,
                    Gap.SMALL,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        } else {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.SMALL,
                    Padding.Top.XSMALL,
                    Gap.MEDIUM,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        }


    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSection) {

        this.strHeader = strHeader;
        VerticalLayout headerContainer = new VerticalLayout();
        headerContainer.addClassName("header-layout");

        H1 header = new H1(strHeader);

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.SMALL, TextColor.SECONDARY);

        H3 headerSection = new H3(strSection);
        headerSection.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Margin.Top.MEDIUM,
                Padding.NONE
        );
        if(strSection.isBlank() || strSection.isEmpty()){
            headerSection.setVisible(false);
        }

        Div divLine = new Div();
        divLine.addClassNames(Border.NONE, Padding.NONE,Border.BOTTOM, Width.FULL);

        headerContainer.add(header, subheader, divLine, headerSection);

        return headerContainer;
    }

    private VerticalLayout loadPhotos(String sqlMemberPhotos, String[] arrColMemberPhotos) {
        return loadPhotos(sqlMemberPhotos, arrColMemberPhotos, null);
    }

    private VerticalLayout loadPhotos(String sqlMemberPhotos, String[] arrColMemberPhotos, Dialog dlgToClose) {

        VerticalLayout layoutMemberPhotos = new VerticalLayout();
        layoutMemberPhotos.addClassNames(Height.FULL,
                Padding.MEDIUM, Margin.NONE,
                AlignItems.CENTER, JustifyContent.CENTER);

//        Select<String> cmbCount = new Select<>();
//        cmbCount.setLabel("Last Uploaded");
//        cmbCount.setItems("0", "4", "10", "20", "30");
//        cmbCount.setValue("10");


//        String sqlMemberGalleryCount = sqlMemberCountPhotos + "  AND usr.username = '" + strMember + "' " + sqlMemberPhotosGroupBy;
//        List<Record> lstPhotoCount = getRecordsFromDb(sqlMemberGalleryCount, arrColumnsMemberCountPhotos); //getRecordsFromDb(sqlRead, arrColumnsLearning);
//        String strMemberPhotosCount = "0";
//        if (!lstPhotoCount.isEmpty()) {
//            strMemberPhotosCount = lstPhotoCount.get(0).getColumnData("photo_count");
//        }
//        int intMemberPhotosCount = Integer.parseInt(strMemberPhotosCount);


        String sqlAllPhotos = sqlMemberPhotos + sqlMemberGalleryOrderBy; // + " LIMIT " + intRecordsPerPage + " OFFSET  0 ";
        // List<Record> lstPhotoRec = getRecordsFromDb(sqlAllPhotos, arrColMemberPhotos); //getRecordsFromDb(sqlRead, arrColumnsLearning);

        // List<PhotoMeta> lstPhotoMeta = new ArrayList<>();

        String strPathPhotos = DIR_PHOTOS_SERVER + dirChar + subPathSmall + dirChar;


        grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setHeightFull();
        grid.setPageSize(pageSize);


        String strTablename = "photo_meta";
        String strMemberId = genericView.checkIfAuthMemberId();
        String strWhere = " AND " + strTablename + ".uploaderId = " + strMemberId;

        CallbackDataProvider<Map<String, Object>, Void> dp = new CallbackDataProvider<Map<String, Object>, Void>(
                query -> {

                    int offset = query.getOffset();
                    int limit = query.getLimit();

                    var sort = query.getSortOrders().stream().findFirst();

                    String sortField = sort.map(s -> s.getSorted())
                            .orElse("date_inserted");

                    boolean asc = sort.map(s -> s.getDirection()
                                    == SortDirection.ASCENDING)
                            .orElse(true);

                    return photoStoryService.fetch(sqlAllPhotos, arrColMemberPhotos, limit, offset, sortField, asc)
                            .stream();
                },

                query -> photoStoryService.count(sqlAllPhotos)
        );

        grid.setDataProvider(dp);


        grid.addSelectionListener(event -> {

            //            if (event.isFromClient()) {
//                event.getAllSelectedItems().forEach(item -> {
//                    // Revert selection if item cannot be selected
//                    grid.deselect(item);
//                });
//            }

            if (event.isFromClient()) {
                Set<Map<String, Object>> selection = grid.getSelectedItems();

                List<String> selectedId = selection.stream()
                        .map(m -> m.get("id").toString())
                        .toList();

                strSelectedPhotoId = selectedId.get(0);

            }
        });


        // initPaging(sqlAllPhotos);


//        grid.addSelectionListener(event -> {
//            if (event.isFromClient()) {
//                event.getAllSelectedItems().forEach(item -> {
//                    // Revert selection if item cannot be selected
//                    grid.deselect(item);
//                });
//            }
//        });

        // For convenience you could set also click listener to do the selection
//        grid.addItemClickListener(event -> {
//
//            if (grid.getSelectedItems().contains(event.getItem())) {
//                grid.deselect(event.getItem());
//            } else {
//                grid.select(event.getItem());
//            }
//        });


        Grid.Column<Map<String, Object>> colId = grid.addColumn(row -> row.get("id"))
                .setHeader("Id").setWidth("30px")
                .setEditorComponent(new TextField());
        colId.getStyle().set("color", "transparent");

        Grid.Column<Map<String, Object>> colPhoto = grid.addColumn(row -> row.get("name_new"))
                .setHeader("Photo").setWidth("170px")
                //     .setEditorComponent(new TextField())
                .setRenderer(new ComponentRenderer<>(row -> {
//                                HorizontalLayout layoutLine = new HorizontalLayout();
//                                layoutLine.setHeight("100px");
//                                layoutLine.setAlignItems(FlexComponent.Alignment.CENTER);

                            String strPhotoFile = row.get("name_new").toString();


                            Div divImage = new Div();
                            if (strPhotoFile.isEmpty()) {
                                divImage.setText("Empty");
                            } else {
                                String imagePath = strPathPhotos + strPhotoFile;

                                Image image = new Image();
                                image.setAlt("Photo");
                                divImage.add(image);
                                File imgFile = new File(imagePath);
                                image.setSrc(DownloadHandler.forFile(imgFile));

                                image.setMaxHeight("125px");
                                image.setHeight("120px");
                                image.setWidth("auto");
                                image.setMaxWidth("170px");

                                String strMetaOrientation = "";
                                String strOrientation =  row.get("meta_orientation")+"";
                                if(strOrientation == null || strOrientation.isEmpty() || strOrientation.equalsIgnoreCase("null")) {
                                    strMetaOrientation = "1";
                                }else {
                                    strMetaOrientation = row.get("meta_orientation").toString();
                                    if (strMetaOrientation.equalsIgnoreCase("8")) {
                                        image.getStyle().set("rotate", "-90deg");
                                        image.setMaxWidth("125px");
//                                    image.setWidth("120px");
//                                    image.setHeight("auto");
                                        image.setMaxHeight("170px");
                                    } else if (strMetaOrientation.equalsIgnoreCase("6")) {
                                        image.getStyle().set("rotate", "90deg");
                                        image.setMaxWidth("125px");
//                                    image.setWidth("120px");
//                                    image.setHeight("auto");
                                        image.setMaxHeight("170px");
                                    } else {

                                    }
                                }
                                image.getStyle().setBorderRadius("6px");


                            }
                            divImage.setHeight("130px");

                            return divImage;
                        })
                );

        Grid.Column<Map<String, Object>> colTitle = grid.addColumn(row -> row.get("photo_title"))
                .setHeader("Title/Description").setWidth("260px")
                //   .setEditorComponent(new TextField())
                //     .setEditorComponent(new TextField())
                .setRenderer(new ComponentRenderer<>(row -> {
                            VerticalLayout layoutLine = new VerticalLayout();
//                                layoutLine.setHeight("100px");
//                                layoutLine.setAlignItems(FlexComponent.Alignment.CENTER);

                            String strTitle = row.get("photo_title") == null ? "" : row.get("photo_title").toString();
                            String strSubtitle = row.get("subtitle") == null ? "" : row.get("subtitle").toString();
                            Div divTitle = new Div(strTitle);
                            Div divSubtitle = new Div(strSubtitle);
                            layoutLine.add(divTitle, divSubtitle);

                            return layoutLine;
                        })
                );

        Grid.Column<Map<String, Object>> colDateShoot = grid.addColumn(row -> row.get("meta_date"))
                .setHeader("Date Shoot/Upload").setWidth("250px")
                .setRenderer(new ComponentRenderer<>(row -> {
                            VerticalLayout layoutLineAll = new VerticalLayout();
                            layoutLineAll.addClassNames(Padding.NONE, Margin.NONE,
                                    Gap.XSMALL);

                            VerticalLayout layoutLine = new VerticalLayout();
                            String strTitle = row.get("meta_date") == null ? "" : row.get("meta_date").toString();
                            String strSubtitle = row.get("date_inserted") == null ? "" : row.get("date_inserted").toString();
                            Div divTitle = new Div(strTitle);
                            Div divSubtitle = new Div(strSubtitle);
                            layoutLine.add(divTitle, divSubtitle);

                            VerticalLayout layoutLineB = new VerticalLayout();
                            String strTitleB = row.get("meta_camera_model") == null ? "" : row.get("meta_camera_model").toString();
                            String strSubtitleB = row.get("meta_lens_model") == null ? "" : row.get("meta_lens_model").toString();
                            Div divTitleB = new Div(strTitleB);
                            Div divSubtitleB = new Div(strSubtitleB);
                            layoutLineB.add(divTitleB, divSubtitleB);

                            layoutLineAll.add(layoutLine, layoutLineB);
                            return layoutLineAll;
                        })
                );

//        Grid.Column<Map<String, Object>> colCamera = grid.addColumn(row -> row.get("meta_camera_model"))
//                .setHeader("Κάμερα/Φακός").setWidth("330px")
//                .setRenderer(new ComponentRenderer<>(row -> {
//                    VerticalLayout layoutLine = new VerticalLayout();
//                    String strTitle = row.get("meta_camera_model") == null ? "" : row.get("meta_camera_model").toString();
//                    String strSubtitle = row.get("meta_lens_model") == null ? "" : row.get("meta_lens_model").toString();
//                    Div divTitle = new Div(strTitle);
//                    Div divSubtitle = new Div(strSubtitle);
//                    layoutLine.add(divTitle, divSubtitle);
//
//                    return layoutLine;
//                }));
        HorizontalLayout layoutControls = new HorizontalLayout();
        layoutControls.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Padding.SMALL, Margin.NONE
        );

        Button btnSelect;
        if (dlgToClose != null) {
            btnSelect = new Button("Set Cover Photo");
            btnSelect.setIcon(VaadinIcon.PICTURE.create());
            btnSelect.addClickListener(clickEvent -> dlgToClose.close());
        } else {
            btnSelect = new Button("Insert Selected");
            btnSelect.setIcon(FontAwesome.Regular.CHECK_SQUARE.create());
            btnSelect.addClickListener(clickEvent ->
                saveStoryItemPhoto(strMemberId, strSelectedStoryId, strSelectedPhotoId));
        }

        layoutControls.add(btnSelect);

        layoutMemberPhotos.add(grid, layoutControls);
        return layoutMemberPhotos;
    }

    private VerticalLayout loadMemberInfo(String sqlRead, String[] arrColumnNames, boolean isEditable) {

        VerticalLayout layoutMember = new VerticalLayout();
        layoutMember.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
//                TextColor.TERTIARY,
                Padding.NONE,
                Gap.SMALL
//                BorderRadius.LARGE, Background.CONTRAST_5
        );
        layoutMember.addClassNames("member-profile");

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);

        if (lstRecords == null) {
            logger.warn(" lstRecords is null");
        } else if (lstRecords.isEmpty()) {
            logger.warn(" lstRecords is empty");
        } else if (lstRecords.size() == 1) {

            Record rec = lstRecords.get(0);
            String strUserId = rec.getColumnData("userId");
            intUserId = Integer.parseInt(strUserId);


            String strName = rec.getColumnData("name");
            String strSurname = rec.getColumnData("surname");

            String strUsername = rec.getColumnData("username");
            String strCountOfPhotosOfAlbums = rec.getColumnData("photo_count");
            String strMemberSince = rec.getColumnData("member_since");
            String strAvatarPath = rec.getColumnData("avatar_path");

            String strResident = rec.getColumnData("resident");
            String strResidentCountry = rec.getColumnData("resident_country");

            String strShortBio = rec.getColumnData("short_bio");
            String strFb = rec.getColumnData("url_fb");
            String strYt = rec.getColumnData("url_yt");
            String strInsta = rec.getColumnData("url_insta");
            String strFlickr = rec.getColumnData("url_flickr");
            String strWebsite = rec.getColumnData("url_website");

            Anchor linkWebsite = new Anchor();
            linkWebsite.add(FontAwesome.Solid.LINK.create());

            if (strWebsite != null && !strWebsite.equalsIgnoreCase("null") && !strWebsite.isEmpty()) {
                linkWebsite.setVisible(true);
                linkWebsite.setHref(strWebsite);
                linkWebsite.setTarget("_blank");
            }

            Anchor linkTutorYt = new Anchor();
            linkTutorYt.add(FontAwesome.Brands.YOUTUBE.create());
            // linkTutorYt.getStyle().setColor(strColorExternalweb);
            // linkTutorYt.setClassName("lazy-result-line-button");

            if (strYt != null && !strYt.equalsIgnoreCase("null") && !strYt.isEmpty()) {

                linkTutorYt.setHref(strYt);
                linkTutorYt.setTarget("_blank");
                linkTutorYt.setVisible(true);
            }

            Anchor linkTutorFacebook = new Anchor();
            linkTutorFacebook.add(FontAwesome.Brands.FACEBOOK_F.create());
            // linkTutorWikipedia.getStyle().setColor(strColorExternalweb);
            //   linkTutorWikipedia.setClassName("lazy-result-line-button");
//            linkTutorFacebook.setVisible(false);

            if (strFb != null && !strFb.equalsIgnoreCase("null") && !strFb.isEmpty()) {
                //linkTutorYt.setText("YouTube");
                //strUrlTutorWikipedia = "https://www.youtube.com/"+strUrlTutorYt;
                linkTutorFacebook.setHref(strFb);
                linkTutorFacebook.setTarget("_blank");
                linkTutorFacebook.setVisible(true);
            }

            Anchor linkTutorInsta = new Anchor();
            //  linkTutorInsta.setClassName("lazy-result-line-button");
            linkTutorInsta.add(FontAwesome.Brands.INSTAGRAM.create());
            // linkTutorInsta.getStyle().setColor(strColorExternalweb);
//            linkTutorInsta.setVisible(false);
            if (strInsta != null && !strInsta.equalsIgnoreCase("null") && !strInsta.isEmpty()) {
                linkTutorInsta.setHref(strInsta);
                linkTutorInsta.setTarget("_blank");
//                linkTutorInsta.setVisible(true);
            }

            Anchor linkFlickr = new Anchor();
            linkFlickr.add(FontAwesome.Brands.FLICKR.create());

            if (strFlickr != null && !strFlickr.equalsIgnoreCase("null") && !strFlickr.isEmpty()) {

                linkFlickr.setHref(strFlickr);
                linkFlickr.setTarget("_blank");
//                linkFlickr.setVisible(true);
            }

            Div divBioTitle = new Div("Short Bio");
            divBioTitle.addClassNames(TextColor.TERTIARY, FontWeight.BOLD);

            VerticalLayout layoutMemberLinks = new VerticalLayout();
            layoutMemberLinks.add(linkTutorFacebook, linkTutorYt, linkTutorInsta, linkTutorYt, linkFlickr, linkWebsite);

            Div divBio = new Div();
//            divBio.setVisible(false);
            if (strShortBio != null && !strShortBio.equalsIgnoreCase("null") && !strShortBio.isEmpty()) {
                divBio.setVisible(true);
                divBio.setText(strShortBio);
            } else {
//                divBio.setVisible(false);
            }

            Image imgAvatar = genericView.getAvatarThumbImage(strAvatarPath, strMember, "150px", "150px");

            H3 objName = new H3(strName + " " + strSurname);
            objName.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);
            H4 objMember = new H4(strMember);
            objMember.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);

            HorizontalLayout layoutAll = new HorizontalLayout();

            VerticalLayout layoutMemberCard = new VerticalLayout();
            layoutMemberCard.getStyle().setBorderRadius("30px");
            layoutMemberCard.getStyle().setMaxWidth("300px");
            layoutMemberCard.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER);
            layoutMemberCard.add(imgAvatar, objName, objMember);

            layoutAll.add(layoutMemberCard, layoutMemberLinks);

            layoutMember.add(layoutAll); //, divBioTitle, divBio, divResident);
        } else {
            logger.warn(" lstRecords is more than one record");
        }

        return layoutMember;
    }

    private VerticalLayout loadStoryItems(Dialog dlgStorySelection, String sqlStoryPhotos, String[] arrColMemberPhotos,
                                          Dialog dlgPhotoSelection,
                                          String strStoryId, String strStoryTitle, String strMemberId) {

        String strTablename = "i"; // photo_stories_photo i
        String strWhereStory = "";
        String strWhere = " AND " + strTablename + ".user_id = " + strMemberId + " ";
        if (!strStoryId.isEmpty()) {
            strWhereStory = " AND " + strTablename + ".story_id = " + strStoryId + " ";
        } else {

        }

        String sqlAllStoryItems = sqlStoryPhotos + strWhere + strWhereStory + sqlReadStoryItemsOrderby; // + " LIMIT " + intRecordsPerPage + " OFFSET  0 ";

        VerticalLayout layoutItems = new VerticalLayout();
        layoutItems.addClassName("story-management");



        CallbackDataProvider<Map<String, Object>, Void> dpStoryItems = new CallbackDataProvider<Map<String, Object>, Void>(
                query -> {

                    int offset = query.getOffset();
                    int limit = query.getLimit();

                    var sort = query.getSortOrders().stream().findFirst();

                    String sortField = sort.map(s -> s.getSorted())
                            .orElse("id");

                    boolean asc = sort.map(s -> s.getDirection()
                                    == SortDirection.ASCENDING)
                            .orElse(true);

                    return photoStoryService.fetch(sqlAllStoryItems, arrColMemberPhotos, limit, offset, sortField, asc)
                            .stream();
                },
                query -> photoStoryService.count(sqlAllStoryItems)
        );

        H4 dvStoryTitle = new H4();
        dvStoryTitle.setWidthFull();
        dvStoryTitle.setText(strStoryTitle);

        Button btnSelectStory = new Button("Select a Photo-Story");
        btnSelectStory.setIcon(FontAwesome.Solid.PHOTO_FILM.create());
        btnSelectStory.addClickListener(clickEvent -> {
            dlgStorySelection.open();
        });

        Button btnRefresh = new Button();
        btnRefresh.setIcon(FontAwesome.Solid.REFRESH.create());
        btnRefresh.addClickListener(clickEvent -> {

            layoutStoryItems.removeAll();
            layoutStoryItems.add(loadStoryItems(dlgStorySelection, sqlReadStoryItems, arrColStoryItems, dlgPhotoSelection, strSelectedStoryId, strStoryTitle, strMemberId));
        });

        Dialog dlgItemNew = loadStoryItemEditDialog("Add", true, sqlMemberOfAlbums, arrColumnsMemberAlbums,
                sqlReadStoryItems, arrColStoryItems,
                dlgPhotoSelection,
                strMemberId, "");

        dlgItemNew.addDialogCloseActionListener(close -> {
            dpStoryItems.refreshAll();
        });

        Button btnAddText = new Button("Add Text");
        btnAddText.setIcon(FontAwesome.Solid.PARAGRAPH.create());
        btnAddText.addClickListener(clickEvent -> {
            if (dvStoryTitle.getText().isEmpty()){
                String messageUp = "Create a Photo-Story first!";
                Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
            }else {
                dlgItemNew.open();
            }
        });

        Button btnAddPhoto = new Button("Add Photo");
        btnAddPhoto.setIcon(VaadinIcon.PICTURE.create());
        btnAddPhoto.addClickListener(clickEvent -> {
            if (dvStoryTitle.getText().isEmpty()){
                String messageUp = "Create the Photo-Story first !";
                Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
            }else {
            dlgPhotoSelection.open();
            }
        });

        Dialog dlgAddMap = loadMapEditorDialog(strMemberId, strStoryId, null);
        dlgAddMap.addDialogCloseActionListener(close -> {
            dpStoryItems.refreshAll();
        });

        Button btnAddMap = new Button("Add Map");
        btnAddMap.setIcon(VaadinIcon.MAP_MARKER.create());
        btnAddMap.addClickListener(clickEvent -> {
            if (dvStoryTitle.getText().isEmpty()) {
                Notification.show("Create the Photo-Story first!", 3000, Notification.Position.TOP_CENTER);
            } else {
                dlgAddMap.open();
            }
        });

        Button btnCreateStory = new Button("Create a Story");
        btnCreateStory.setIcon(VaadinIcon.PLUS.create());
        btnCreateStory.addClickListener(e -> {
            Dialog dlgCreate = loadStoryEditDialog(sqlAlbumCategories, arrAlbumCategoriesColumns, "New Photo-Story",
                    sqlMemberOfAlbums, arrColumnsMemberAlbums, null, strMemberId);
            if (dlgCreate != null) dlgCreate.open();
        });

        Button btnEditStory = new Button("Edit This Story");
        btnEditStory.setIcon(VaadinIcon.EDIT.create());
        btnEditStory.setEnabled(!strStoryId.isEmpty() && !strStoryId.equals("0"));
        btnEditStory.addClickListener(e -> {
            Dialog dlgEdit = loadStoryEditDialog(sqlAlbumCategories, arrAlbumCategoriesColumns, "Edit a Photo-Story",
                    sqlMemberOfAlbums, arrColumnsMemberAlbums, strStoryId, strMemberId);
            if (dlgEdit != null) dlgEdit.open();
        });

        HorizontalLayout layoutControls = new HorizontalLayout();
        layoutControls.addClassNames(
                AlignItems.CENTER, JustifyContent.EVENLY,
                Padding.SMALL, Margin.NONE,
                Gap.MEDIUM
        );
        layoutControls.add(btnCreateStory, btnEditStory, btnSelectStory, btnRefresh, btnAddText, btnAddPhoto, btnAddMap);


        layoutItems.add(layoutControls);

//        String sqlMemberGalleryCount = sqlMemberCountPhotos + "  AND usr.username = '" + strMember + "' " + sqlMemberPhotosGroupBy;
//        List<Record> lstPhotoCount = getRecordsFromDb(sqlMemberGalleryCount, arrColumnsMemberCountPhotos); //getRecordsFromDb(sqlRead, arrColumnsLearning);
//        String strMemberPhotosCount = "0";
//        if (!lstPhotoCount.isEmpty()) {
//            strMemberPhotosCount = lstPhotoCount.get(0).getColumnData("photo_count");
//        }
//        int intMemberPhotosCount = Integer.parseInt(strMemberPhotosCount);

        String strPathPhotos = DIR_PHOTOS_SERVER + dirChar + subPathSmall + dirChar;

        Grid<Map<String, Object>> gridStoryItems = new Grid<>();
        gridStoryItems.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridStoryItems.addClassNames(Padding.SMALL);
        gridStoryItems.setMinHeight("1000px");
        gridStoryItems.setPageSize(pageSize);

        gridStoryItems.setDataProvider(dpStoryItems);
        gridStoryItems.setSelectionMode(Grid.SelectionMode.SINGLE);

        gridStoryItems.setRowsDraggable(true);

        gridStoryItems.addDragStartListener(
                event -> {
                    // store current dragged item so we know what to drop
                     draggedItem = event.getDraggedItems().get(0);
                    gridStoryItems.setDropMode(GridDropMode.BETWEEN);
                }
        );

        gridStoryItems.addDragEndListener(
                event -> {
                       draggedItem = null;
                    // Once dragging has ended, disable drop mode so that
                    // it won't look like other dragged items can be dropped
                    gridStoryItems.setDropMode(null);
                }
        );

        gridStoryItems.addDropListener(
                event -> {
                    Object dropOverItem = event.getDropTargetItem().get();
               //        if (!dropOverItem.equals(draggedItem)) {
                    // reorder dragged item the backing gridItems container
             //            gridStoryItems.remove(draggedItem);
                    // calculate drop index based on the dropOverItem
             //         int dropIndex = gridStoryItems.indexOf(dropOverItem) + (event.getDropLocation() == GridDropLocation.BELOW ? 1 : 0);
             //        gridStoryItems.add(dropIndex, draggedItem);
                    gridStoryItems.getDataProvider().refreshAll();
                    //  }
                }
        );


        // For convenience you could set also click listener to do the selection
        gridStoryItems.addSelectionListener(event -> {

            //            if (event.isFromClient()) {
//                event.getAllSelectedItems().forEach(item -> {
//                    // Revert selection if item cannot be selected
//                    grid.deselect(item);
//                });
//            }

            if (event.isFromClient()) {
                Set<Map<String, Object>> selection = gridStoryItems.getSelectedItems();

                List<String> selectedId = selection.stream()
                        .map(m -> m.get("story_item_id").toString())
                        .toList();

                strSelectedStoryItemId = selectedId.get(0);
            }
        });

        Grid.Column<Map<String, Object>> colId = gridStoryItems.addColumn(row -> row.get("id"))
                .setHeader("Id").setWidth("25px");

        Grid.Column<Map<String, Object>> colPhoto = gridStoryItems.addColumn(row -> row.get("name_new"))
                .setHeader("Photo").setWidth("140px")
                //     .setEditorComponent(new TextField())
                .setRenderer(new ComponentRenderer<>(row -> {
//                                HorizontalLayout layoutLine = new HorizontalLayout();
//                                layoutLine.setHeight("100px");
//                                layoutLine.setAlignItems(FlexComponent.Alignment.CENTER);
                            String strPhotoFile = row.get("name_new") == null ? "" : row.get("name_new").toString();
                            Div divImage = new Div();
                            if (strPhotoFile.isEmpty()) {
                                divImage.setText("Empty");
                            } else {
                                String imagePath = strPathPhotos + strPhotoFile;
                                Image image = new Image();
                                image.setAlt("Photo");
                                divImage.add(image);
                                File imgFile = new File(imagePath);
                                image.setSrc(DownloadHandler.forFile(imgFile));
                                image.setMaxHeight("100px");
                                image.setHeight("95px");
                                image.setWidth("auto");
                                image.setMaxWidth("160px");
                                image.getStyle().setBorderRadius("5px");
                                String strMetaOrientation = row.get("meta_orientation") == null ? "" : row.get("meta_orientation").toString();
                                if (strMetaOrientation.equalsIgnoreCase("8")) {
                                    image.getStyle().set("rotate", "-90deg");
                                } else if (strMetaOrientation.equalsIgnoreCase("6")) {
                                    image.getStyle().set("rotate", "90deg");
                                }
                            }
                            divImage.setHeight("105px");

                            return divImage;
                        })
                );

        Grid.Column<Map<String, Object>> colDateShoot = gridStoryItems.addColumn(row -> row.get("inc"))
                .setHeader("inc / Type").setWidth("120px")
                .setRenderer(new ComponentRenderer<>(row -> {
                            VerticalLayout layoutLine = new VerticalLayout();
                            layoutLine.addClassNames(AlignItems.START, JustifyContent.START);
                            String strTitle = row.get("inc") == null ? "" : row.get("inc").toString();
                            String strSubtitle = row.get("item_type") == null ? "" : row.get("item_type").toString();
                            Div divTitle = new Div(strTitle);
                            Div divSubtitle = new Div(strSubtitle);
                            divSubtitle.addClassNames("tag");
                            layoutLine.add(divTitle, divSubtitle);

                            return layoutLine;
                        })
                );

        Grid.Column<Map<String, Object>> colTitle = gridStoryItems.addColumn(row -> row.get("item_title"))
                .setHeader("Title").setWidth("220px")
                .setEditorComponent(new TextArea())
                .setRenderer(new ComponentRenderer<>(row -> {
                            VerticalLayout layoutLine = new VerticalLayout();
                            String strTitle = row.get("item_title") == null ? "" : row.get("item_title").toString();
                            Div divTitle = new Div(strTitle);
                            divTitle.addClassName("div-wrap");
//                            TextArea txtArea = new TextArea();
//                            txtArea.setValue(strTitle);
//                            layoutLine.add(txtArea);
                            layoutLine.add(divTitle);
                            return layoutLine;
                        })
                );

        Grid.Column<Map<String, Object>> colSubTitle = gridStoryItems.addColumn(row -> row.get("descr"))
                .setHeader("Description").setWidth("420px")
                .setEditorComponent(new TextArea())
                .setRenderer(new ComponentRenderer<>(row -> {
                            VerticalLayout layoutLine = new VerticalLayout();
                            String strSubtitle = row.get("descr") == null ? "" : row.get("descr").toString();
                            Div divSubtitle = new Div(strSubtitle);
                            divSubtitle.addClassName("div-wrap");
//                            TextArea txtArea = new TextArea();
//                            txtArea.setValue(strSubtitle);
//                            layoutLine.add(txtArea);
                            layoutLine.add(divSubtitle);
                            return layoutLine;
                        })
                );

        Grid.Column<Map<String, Object>> colButtons = gridStoryItems.addColumn(row -> row.get("story_item_id"))
                .setHeader("Buttons").setWidth("55px")
                .setRenderer(new ComponentRenderer<>(row -> {
                    VerticalLayout layoutLine = new VerticalLayout();
                    layoutLine.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                            Padding.SMALL, Margin.NONE,
                            Gap.SMALL);
                    // String strTitle = row.get("inc") == null ? "" : row.get("inc").toString();

                    String strItemId = row.get("story_item_id") == null ? "" : row.get("story_item_id").toString();
                    String strRowItemType = row.get("item_type") == null ? "" : row.get("item_type").toString();
                    Div divId = new Div(strItemId);

                    boolean isMapItem = strRowItemType.equalsIgnoreCase("Map");

                    Dialog dlgItemEditSelection;
                    if (isMapItem) {
                        dlgItemEditSelection = loadMapEditorDialog(strMemberId, strStoryId, strItemId);
                    } else {
                        dlgItemEditSelection = loadStoryItemEditDialog("Επεξεργασία", false, sqlMemberOfAlbums, arrColumnsMemberAlbums,
                                sqlReadStoryItems, arrColStoryItems,
                                dlgPhotoSelection,
                                strMemberId, strItemId);
                    }

                    dlgItemEditSelection.addDialogCloseActionListener(close -> {
                        layoutStoryItems.removeAll();
                        layoutStoryItems.add(loadStoryItems(dlgStorySelection, sqlReadStoryItems, arrColStoryItems, dlgPhotoSelection, strSelectedStoryId, strStoryTitle, strMemberId));

                        dpStoryItems.refreshAll();
                    });

                    Button btnEdit = new Button("");
                    btnEdit.setTooltipText("Επεξεργασία");
                    btnEdit.setIcon(FontAwesome.Solid.PENCIL.create());
                    btnEdit.addClickListener(event -> {
                        dlgItemEditSelection.open();

                    });

                    Button btnDelete = new Button("");
                    btnDelete.setTooltipText("Διαγραφή");
                    SvgIcon iconBin = new SvgIcon(DownloadHandler.forClassResource(getClass(), "/icons/delete-bin-line.svg"));
                    btnDelete.setIcon(iconBin);
                    btnDelete.addClickListener(del -> {
                        if (isMapItem && !strItemId.isEmpty()) {
                            try {
                                storyMapService.deleteByStoryItemId(Integer.parseInt(strItemId));
                            } catch (NumberFormatException ignored) {}
                        }
                        deleteStoryItem(strStoryId, strItemId, strMemberId);


                        layoutStoryItems.removeAll();
                        layoutStoryItems.add(loadStoryItems(dlgStorySelection, sqlReadStoryItems, arrColStoryItems, dlgPhotoSelection, strSelectedStoryId, strStoryTitle, strMemberId));

                    });

                    layoutLine.add(divId, btnEdit, btnDelete);
                    return layoutLine;
                }));

        layoutItems.add(dvStoryTitle, gridStoryItems);
        return layoutItems;
    }

    private GalleryImageViewCard getImagePanelFromDb(Record record, String strPath, int intUserId, String strMember) {

        String strFileName = record.getColumnData("name_new");

        String strCity = record.getColumnData("city_name");
        String strSubject = record.getColumnData("subject_name");
        String strUploader = record.getColumnData("uploader");

        int isType = 2;

        logger.info(" Photo:" + strFileName + " Member Gallery -> city and subject:'" + strCity + "'_'" + strSubject + "'");


        String strImagePath = strPath + dirChar + strFileName;
        logger.info(" strImagePath " + strImagePath);

        String sqlMemberPhotosOrderby = " ORDER BY pm.date_inserted DESC ";
        boolean isEditable = true;
//
//        GalleryImageViewCard imageGalleryViewCard = new GalleryImageViewCard(record, strImagePath, isMobile, intUserId, strMember, sessionCreation, hostname, publicIp, isEditable,
//                recordService, isType, sqlReadGallery, sqlMemberPhotosOrderby, arrColumnNamesGallery, shareService, shareMetricService, weatherService, photoRatingService, photoViewService);


        GalleryImageViewCard imageGalleryViewCard = new GalleryImageViewCard(record, strImagePath, isMobile, intUserId, strMember, sessionCreation, hostname, publicIp, isEditable,
                recordService, isType, sqlReadGallery, sqlMemberPhotosOrderby, arrColumnNamesGallery,  shareService, shareMetricService, weatherService, photoRatingService, photoViewService);

        imageGalleryViewCard.addClassName("image-to-show");
        imageGalleryViewCard.getStyle().setOpacity("1");
        return imageGalleryViewCard;
    }

    private VerticalLayout loadStoriesPanel(String sqlMemberOfAlbums, String sqlMemberOfAlbumsOrderBy, String[] arrColumnsMemberAlbums, String strMemberId) {

        String sqlMemberStories = sqlMemberOfAlbums + sqlMemberOfAlbumsOrderBy;
        List<Record> lstStories = getRecordsFromDb(sqlMemberStories, arrColumnsMemberAlbums);
        List<String> lstStoriesTitle = new ArrayList<>();
        List<String> lstStoriesId = new ArrayList<>();
        for (int i = 0; i < lstStories.size(); i++) {
            lstStoriesTitle.add(lstStories.get(i).getColumnData("title"));
            lstStoriesId.add(lstStories.get(i).getColumnData("id"));
        }

        listBoxAlbums.addClassNames(Background.BASE, BorderRadius.SMALL);
        listBoxAlbums.setWidthFull();
        listBoxAlbums.setMinHeight("470px");
        listBoxAlbums.setItems(lstStoriesTitle);
        if (!lstStoriesTitle.isEmpty()) {
            listBoxAlbums.setValue(lstStoriesTitle.get(0));
        }

        VerticalLayout layoutAlbumsPanel = new VerticalLayout();
        layoutAlbumsPanel.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Background.CONTRAST_5, BorderRadius.LARGE);
        layoutAlbumsPanel.setMinWidth("410px");
        layoutAlbumsPanel.setMaxWidth("590px");

        Div divAlbumsCaption = new Div("Create and edit Photo-Stories");
        divAlbumsCaption.addClassNames(FontSize.SMALL,
                TextColor.BODY,
                TextAlignment.CENTER);

        HorizontalLayout layoutControls = new HorizontalLayout();
        layoutControls.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);


        Button btnCreate = new Button("Create");
        btnCreate.setIcon(VaadinIcon.PLUS.create());
        btnCreate.addClickListener(event -> {
            Dialog dlg = loadStoryEditDialog(sqlAlbumCategories, arrAlbumCategoriesColumns, "New Photo-Story", sqlMemberOfAlbums, arrColumnsMemberAlbums,
                    null, strMemberId);
            if(dlg != null) {
                dlg.open();
            }
        });

        Button btnEdit = new Button("Edit");
        btnEdit.setIcon(VaadinIcon.EDIT.create());
        btnEdit.addClickListener(event -> {
            String strAlbumId = "";
            for (int i = 0; i < lstStoriesTitle.size(); i++) {
                if (lstStoriesTitle.get(i).equalsIgnoreCase(listBoxAlbums.getValue())) {
                    strAlbumId = lstStoriesId.get(i);
                }
            }

            Dialog dlg = loadStoryEditDialog(sqlAlbumCategories, arrAlbumCategoriesColumns, "Edit a Photo-Story", sqlMemberOfAlbums, arrColumnsMemberAlbums,
                    strAlbumId, strMemberId);
            dlg.open();
        });

        Button btnDelete = new Button("Remove");
        btnDelete.setIcon(VaadinIcon.MINUS.create());
        btnDelete.addClickListener(delete -> {

            String strAlbumId = "";
            for (int i = 0; i < lstStoriesTitle.size(); i++) {
                if (lstStoriesTitle.get(i).equalsIgnoreCase(listBoxAlbums.getValue())) {
                    strAlbumId = lstStoriesId.get(i);
                }
            }

            if (strAlbumId != null) {
                String sqlAlbumInfo = "";

                if (!strAlbumId.isEmpty()) {
                    sqlAlbumInfo = sqlMemberOfAlbums + " AND id = '" + strAlbumId + "' AND s.user_id = '" + strMemberId + "' ";
                } else {
                    sqlAlbumInfo = sqlMemberOfAlbums + " AND title = '" + listBoxAlbums.getValue() + "' AND s.user_id = '" + strMemberId + "' ";
                }

                List<Record> lstAlbum = getRecordsFromDb(sqlAlbumInfo, arrColumnsMemberAlbums);
                if (lstAlbum.isEmpty()){
                    String messageUp = "There are no Photo-Stories yet!";
                    Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                    strAlbumId = "0";
                }else {
                    strAlbumId = lstAlbum.get(0).getColumnData("id");
                    deleteStoryInfo(strAlbumId, strMemberId);
                }
            }


        });

        layoutControls.add(btnCreate, btnEdit, btnDelete);
        layoutAlbumsPanel.add(divAlbumsCaption, layoutControls, listBoxAlbums);

        return layoutAlbumsPanel;
    }

    private boolean saveStoryItemPhoto(String strMemberId, String strStoryId, String strSelectedPhotoId) {

        StringBuilder strInsert = new StringBuilder("INSERT INTO photo_stories_photo (");
        StringBuilder placeholders = new StringBuilder("(");
        Object[] fieldValue = new Object[3];
        String[] fieldValueType = new String[3];
        boolean first = true;

        if (strMemberId != null && !strMemberId.isEmpty()) {
            if (!first) {
                strInsert.append(", ");
                placeholders.append(", ");
            }
            strInsert.append("user_id");
            placeholders.append("?");
            fieldValue[0] = strMemberId;
            fieldValueType[0] = "java.lang.Integer";
            first = false;
        }

        if (strStoryId != null && !strStoryId.isEmpty()) {
            if (!first) {
                strInsert.append(", ");
                placeholders.append(", ");
            }
            strInsert.append("story_id");
            placeholders.append("?");
            fieldValue[1] = strStoryId;
            fieldValueType[1] = "java.lang.Integer";
            first = false;
        }

        if (strSelectedPhotoId != null && !strSelectedPhotoId.isEmpty()) {
            if (!first) {
                strInsert.append(", ");
                placeholders.append(", ");
            }
            strInsert.append("photo_id");
            placeholders.append("?");
            fieldValue[2] = strSelectedPhotoId;
            fieldValueType[2] = "java.lang.Integer";
            first = false;
        }

        if (!first) {
            strInsert.append(", ");
            placeholders.append(", ");
        }
        strInsert.append("item_type");
        placeholders.append("'Photo'");
        first = false;
/*        if (!first) {
            strInsert.append(", ");
            placeholders.append(", ");
        }
        strInsert.append("item_type_gr");
        placeholders.append("'Φωτογραφία'");
        first = false;*/

        strInsert.append(") VALUES ");
        placeholders.append(")");
        strInsert.append(placeholders);

        if (recordService.insertOneRecordWithQuery(strInsert.toString(), fieldValue, fieldValueType) == 1) {
            String messageUp = "Story Photo Item Created !";
            Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
            notificationUp.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            reUpdateMyStoriesPhotosCount(Integer.parseInt(strMemberId));
            return true;
        }

        return false;
    }

    private boolean saveStoryItem(String strMemberId, String strStoryId, String strStoryItemId,
                                  String strCategoryGr, String strItemInc, String strTitle, String strDescription) {

        logger.info("-->" + strMemberId + "  " + strStoryId + " " + strCategoryGr + "  " + strItemInc + "  " + strTitle + "  " + strDescription);

        if ((strStoryItemId == null || strStoryItemId.isEmpty()) && !strStoryId.isEmpty() && !strMemberId.isEmpty()) {
            //            if (!strCategoryGr.isEmpty() && !strCategoryGr.isEmpty()) {
            StringBuilder strInsert = new StringBuilder("INSERT INTO photo_stories_photo (");
            StringBuilder placeholders = new StringBuilder("(");
            Object[] fieldValue = new Object[6];
            String[] fieldValueType = new String[6];
            boolean first = true;

            if (strMemberId != null && !strMemberId.isEmpty()) {
                if (!first) {
                    strInsert.append(", ");
                    placeholders.append(", ");
                }
                strInsert.append("user_id");
                placeholders.append("?");
                fieldValue[0] = strMemberId;
                fieldValueType[0] = "java.lang.Integer";
                first = false;
            }

            if (strStoryId != null && !strStoryId.isEmpty()) {
                if (!first) {
                    strInsert.append(", ");
                    placeholders.append(", ");
                }
                strInsert.append("story_id");
                placeholders.append("?");
                fieldValue[1] = strStoryId;
                fieldValueType[1] = "java.lang.Integer";
                first = false;
            }


            if (strCategoryGr != null && !strCategoryGr.isEmpty()) {
                if (!first) {
                    strInsert.append(", ");
                    placeholders.append(", ");
                }
                strInsert.append("item_type");
                placeholders.append("?");
                fieldValue[2] = strCategoryGr;
                fieldValueType[2] = "java.lang.String";
                first = false;
            }

            if (strItemInc != null && !strItemInc.isEmpty()) {
                if (!first) {
                    strInsert.append(", ");
                    placeholders.append(", ");
                }
                strInsert.append("inc");
                placeholders.append("?");
                fieldValue[3] = strItemInc;
                fieldValueType[3] = "java.lang.Integer";
                first = false;
            }

            if (strTitle != null) {
                if (!first) {
                    strInsert.append(", ");
                    placeholders.append(", ");
                }
                strInsert.append("item_title");
                placeholders.append("?");
                fieldValue[4] = strTitle;
                fieldValueType[4] = "java.lang.String";
                first = false;
            }

            if (strDescription != null) {
                if (!first) {
                    strInsert.append(", ");
                    placeholders.append(", ");
                }
                strInsert.append("descr");
                placeholders.append("?");
                fieldValue[5] = strDescription;
                fieldValueType[5] = "java.lang.String";
                first = false;
            }


            strInsert.append(") VALUES ");
            placeholders.append(")");
            strInsert.append(placeholders);

            if (recordService.insertOneRecordWithQuery(strInsert.toString(), fieldValue, fieldValueType) == 1) {
                String messageUp = "Story Text Item Created !";
                Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                notificationUp.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                reUpdateMyStoriesPhotosCount(Integer.parseInt(strMemberId));
                return true;
            } else {
                String messageUp = "Story Text Item Not Created !";
                Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return false;
            }
        } else if (!strStoryItemId.isEmpty() && !strStoryId.isEmpty() && !strMemberId.isEmpty()) {

            StringBuilder strUpdate = new StringBuilder("UPDATE photo_stories_photo SET ");

            Object[] fieldValue = new Object[4];
            String[] fieldValueType = new String[4];
            boolean first = true;

/*            if (strMemberId != null && !strMemberId.isEmpty()) {
                if (!first) {
                    strUpdate.append(", ");
                    placeholders.append(", ");
                }
                strUpdate.append("user_id");
                placeholders.append("?");
                fieldValue[0] = strMemberId;
                fieldValueType[0] = "java.lang.Integer";
                first = false;
            }

            if (strStoryId != null && !strStoryId.isEmpty()) {
                if (!first) {
                    strUpdate.append(", ");
                    placeholders.append(", ");
                }
                strUpdate.append("story_id");
                placeholders.append("?");
                fieldValue[1] = strStoryId;
                fieldValueType[1] = "java.lang.Integer";
                first = false;
            }
*/
            if (strCategoryGr != null && !strCategoryGr.isEmpty()) {
                if (!first) {
                    strUpdate.append(", ");
                }
                strUpdate.append("item_type = ?");
                fieldValue[0] = strCategoryGr;
                fieldValueType[0] = "java.lang.String";
                first = false;
            }

            if (strItemInc != null && !strItemInc.isEmpty()) {
                if (!first) {
                    strUpdate.append(", ");
                }
                strUpdate.append("inc = ?");
                fieldValue[1] = strItemInc;
                fieldValueType[1] = "java.lang.Integer";
                first = false;
            }

            if (!first) {
                strUpdate.append(", ");
            }
            strUpdate.append("item_title = ?");
            fieldValue[2] = strTitle;
            fieldValueType[2] = "java.lang.String";
            first = false;

            if (!first) {
                strUpdate.append(", ");
            }
            strUpdate.append("descr = ?");
            fieldValue[3] = strDescription;
            fieldValueType[3] = "java.lang.String";
            first = false;

            strUpdate.append(" WHERE user_id = '" + strMemberId + "' AND story_id = '" + strStoryId + "' AND id = '" + strStoryItemId + "' ");

            if (recordService.insertOneRecordWithQuery(strUpdate.toString(), fieldValue, fieldValueType) == 1) {
                String messageUp = "Story Item Updated !";
                Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                notificationUp.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                String sqlMemberAlbums = sqlMemberOfAlbums + sqlMemberOfAlbumsOrderBy;

                List<Record> lstAlbums = getRecordsFromDb(sqlMemberAlbums, arrColumnsMemberAlbums);
                List<String> lstAlbumTitle = new ArrayList<>();
                List<String> lstAlbumId = new ArrayList<>();
                for (int i = 0; i < lstAlbums.size(); i++) {
                    lstAlbumTitle.add(lstAlbums.get(i).getColumnData("title"));
                    lstAlbumId.add(lstAlbums.get(i).getColumnData("id"));
                }
                listBoxAlbums.setItems(lstAlbumTitle);

                reUpdateMyStoriesPhotosCount(Integer.parseInt(strMemberId));
                return true;
            } else {

                String messageUp = "Story Item Updated Not Updated !";
                Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return false;
            }
        }

        return false;
    }

    private void deleteStoryItem(String strStoryId, String strStoryItemId, String strMemberId) {

        String sqlDeleteStoryItem = "DELETE FROM photo_stories_photo WHERE id='" + strStoryItemId + "' AND  story_id='" + strStoryId + "' AND user_id = '" + strMemberId + "'";

        if (recordService.insertOneRecordWithQuery(sqlDeleteStoryItem, null, null) == 1) {
            String messageUp = "Διαγράφηκε!";
            Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
            notificationUp.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            String sqlMemberAlbums = sqlMemberOfAlbums + sqlMemberOfAlbumsOrderBy;

            List<Record> lstAlbums = getRecordsFromDb(sqlMemberAlbums, arrColumnsMemberAlbums);
            List<String> lstAlbumTitle = new ArrayList<>();
            List<String> lstAlbumId = new ArrayList<>();
            for (int i = 0; i < lstAlbums.size(); i++) {
                lstAlbumTitle.add(lstAlbums.get(i).getColumnData("title"));
                lstAlbumId.add(lstAlbums.get(i).getColumnData("id"));
            }
            listBoxAlbums.setItems(lstAlbumTitle);

            reUpdateMyStoriesCount(Integer.parseInt(strMemberId));
        } else {
            String messageUp = "Δεν διαγράφηκε !";
            Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
            notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

    }


    private Dialog loadMapEditorDialog(String strMemberId, String strStoryId, String strStoryItemId) {
        Dialog dlg = new Dialog();
        dlg.setWidth("700px");
        dlg.setMaxHeight("90vh");
        dlg.setResizable(true);
        dlg.setDraggable(true);

        boolean isEdit = strStoryItemId != null && !strStoryItemId.isEmpty();

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setPadding(true);
        layout.setSpacing(true);

        Div dlgTitle = new Div(isEdit ? "Edit Map Panel" : "Add Map Panel");
        dlgTitle.addClassNames(FontWeight.BOLD, FontSize.LARGE);

        TextField txtLocationArea = new TextField("Title");
        txtLocationArea.setWidthFull();
        txtLocationArea.setPlaceholder("e.g. Athens City Centre");

        TextArea txtMapDescription = new TextArea("Description");
        txtMapDescription.setWidthFull();
        txtMapDescription.setPlaceholder("Optional description for this map area");
        txtMapDescription.setMinRows(2);

        VerticalLayout pointsLayout = new VerticalLayout();
        pointsLayout.setWidthFull();
        pointsLayout.setPadding(false);
        pointsLayout.setSpacing(true);

        List<HorizontalLayout> pointRows = new ArrayList<>();

        if (isEdit) {
            String[] mapCols = {"id", "location_area"};
            String sqlMap = "SELECT id, location_area FROM photo_story_map WHERE story_item_id = " + strStoryItemId;
            List<Record> lstMap = getRecordsFromDb(sqlMap, mapCols);
            if (!lstMap.isEmpty()) {
                String area = lstMap.get(0).getColumnData("location_area");
                if (area != null && !area.equalsIgnoreCase("null")) {
                    txtLocationArea.setValue(area);
                }
                String mapId = lstMap.get(0).getColumnData("id");
                String[] ptCols = {"point_name", "lat", "lon", "description"};
                String sqlPts = "SELECT point_name, lat, lon, description FROM photo_story_map_point WHERE map_id = " + mapId + " ORDER BY point_order ASC";
                List<Record> lstPts = getRecordsFromDb(sqlPts, ptCols);
                for (Record pt : lstPts) {
                    HorizontalLayout row = buildPointRow(
                            pt.getColumnData("point_name"),
                            pt.getColumnData("lat"),
                            pt.getColumnData("lon"),
                            pt.getColumnData("description"),
                            pointRows, pointsLayout
                    );
                    pointRows.add(row);
                    pointsLayout.add(row);
                }
            }
            // Load description from photo_stories_photo
            String[] itemDescCols = {"descr"};
            List<Record> lstItemDesc = getRecordsFromDb(
                    "SELECT descr FROM photo_stories_photo WHERE id = " + strStoryItemId, itemDescCols);
            if (!lstItemDesc.isEmpty()) {
                String existingDesc = lstItemDesc.get(0).getColumnData("descr");
                if (existingDesc != null && !existingDesc.equalsIgnoreCase("null")) {
                    txtMapDescription.setValue(existingDesc);
                }
            }
        }

        if (pointRows.isEmpty()) {
            HorizontalLayout row = buildPointRow("", "", "", "", pointRows, pointsLayout);
            pointRows.add(row);
            pointsLayout.add(row);
        }

        Button btnAddPoint = new Button("+ Add Point");
        btnAddPoint.addClickListener(e -> {
            HorizontalLayout row = buildPointRow("", "", "", "", pointRows, pointsLayout);
            pointRows.add(row);
            pointsLayout.add(row);
        });

        Button btnSave = new Button("Save");
        btnSave.setIcon(FontAwesome.Regular.CHECK_SQUARE.create());
        btnSave.addClickListener(e -> {
            String locationArea = txtLocationArea.getValue();
            String mapDescription = txtMapDescription.getValue();
            List<StoryMapPointDto> points = new ArrayList<>();
            for (HorizontalLayout row : pointRows) {
                TextField tfName = (TextField) row.getComponentAt(0);
                // index 1 = search button, skip
                TextField tfLat  = (TextField) row.getComponentAt(2);
                TextField tfLon  = (TextField) row.getComponentAt(3);
                TextField tfDesc = (TextField) row.getComponentAt(4);
                if (!tfLat.getValue().isEmpty() && !tfLon.getValue().isEmpty()) {
                    try {
                        StoryMapPointDto pt = new StoryMapPointDto();
                        pt.setPointName(tfName.getValue());
                        pt.setLat(Double.parseDouble(tfLat.getValue()));
                        pt.setLon(Double.parseDouble(tfLon.getValue()));
                        pt.setDescription(tfDesc.getValue());
                        points.add(pt);
                    } catch (NumberFormatException ignored) {
                        Notification.show("Invalid lat/lon in one of the points.", 3000, Notification.Position.TOP_CENTER);
                        return;
                    }
                }
            }
            if (points.isEmpty()) {
                Notification.show("Add at least one location point with valid lat/lon.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            try {
                Integer storyItemIdInt;
                if (isEdit) {
                    storyItemIdInt = Integer.parseInt(strStoryItemId);
                    // Update title and description in photo_stories_photo
                    Object[] updVals = {locationArea, mapDescription, storyItemIdInt};
                    String[] updTypes = {"java.lang.String", "java.lang.String", "java.lang.Integer"};
                    recordService.insertOneRecordWithQuery(
                            "UPDATE photo_stories_photo SET item_title = ?, descr = ? WHERE id = ?",
                            updVals, updTypes);
                } else {
                    String sqlInsertItem = "INSERT INTO photo_stories_photo (user_id, story_id, item_type, item_title, descr) VALUES (?, ?, 'Map', ?, ?)";
                    storyItemIdInt = photoStoryService.insertAndGetGeneratedId(sqlInsertItem,
                            Integer.parseInt(strMemberId), Integer.parseInt(strStoryId), locationArea, mapDescription);
                    if (storyItemIdInt == null) {
                        Notification.show("Failed to create story item.", 3000, Notification.Position.TOP_CENTER);
                        return;
                    }
                }
                var mapEntity = storyMapService.saveMap(storyItemIdInt, Integer.parseInt(strMemberId), Integer.parseInt(strStoryId), locationArea);
                storyMapService.savePoints(mapEntity.getId(), points);
                Notification.show("Map panel saved!", 3000, Notification.Position.TOP_CENTER);
                dlg.close();
            } catch (NumberFormatException ex) {
                Notification.show("Invalid member or story ID.", 3000, Notification.Position.TOP_CENTER);
            }
        });

        Button btnCancel = new Button("Cancel");
        btnCancel.addClickListener(e -> dlg.close());

        HorizontalLayout btnRow = new HorizontalLayout(btnSave, btnCancel);

        layout.add(dlgTitle, txtLocationArea, txtMapDescription, pointsLayout, btnAddPoint, btnRow);
        dlg.add(layout);
        return dlg;
    }

    private HorizontalLayout buildPointRow(String name, String lat, String lon, String desc,
                                            List<HorizontalLayout> pointRows, VerticalLayout pointsLayout) {
        TextField tfName = new TextField("Name");
        tfName.setPlaceholder("e.g. Acropolis");
        tfName.setValue(name != null && !name.equalsIgnoreCase("null") ? name : "");

        TextField tfLat = new TextField("Lat");
        tfLat.setPlaceholder("37.9715");
        tfLat.setValue(lat != null && !lat.equalsIgnoreCase("null") ? lat : "");
        tfLat.setWidth("110px");

        TextField tfLon = new TextField("Lon");
        tfLon.setPlaceholder("23.7269");
        tfLon.setValue(lon != null && !lon.equalsIgnoreCase("null") ? lon : "");
        tfLon.setWidth("110px");

        TextField tfDesc = new TextField("Description");
        tfDesc.setPlaceholder("Optional");
        tfDesc.setValue(desc != null && !desc.equalsIgnoreCase("null") ? desc : "");
        tfDesc.getStyle().setFlexGrow("1");

        Button btnSearch = new Button(VaadinIcon.SEARCH.create());
        btnSearch.getStyle().setAlignSelf("flex-end");
        btnSearch.getElement().setAttribute("title", "Search location");
        btnSearch.addClickListener(e -> openLocationSearch(tfName, tfLat, tfLon));

        Button btnRemove = new Button("-");
        btnRemove.getStyle().setAlignSelf("flex-end");

        HorizontalLayout row = new HorizontalLayout(tfName, btnSearch, tfLat, tfLon, tfDesc, btnRemove);
        row.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        row.setWidthFull();

        btnRemove.addClickListener(e -> {
            pointRows.remove(row);
            pointsLayout.remove(row);
        });

        return row;
    }

    private void openLocationSearch(TextField tfName, TextField tfLat, TextField tfLon) {
        Dialog dlg = new Dialog();
        dlg.setWidth("520px");
        dlg.setMaxHeight("80vh");

        H4 dlgTitle = new H4("Search Location");
        dlgTitle.getStyle().set("margin", "0 0 var(--lumo-space-s) 0");

        TextField tfQuery = new TextField("Location");
        tfQuery.setPlaceholder("e.g. Athens, Greece");
        tfQuery.setWidthFull();

        VerticalLayout resultsLayout = new VerticalLayout();
        resultsLayout.setPadding(false);
        resultsLayout.setSpacing(false);
        resultsLayout.setWidthFull();
        resultsLayout.getStyle().set("overflow-y", "auto").set("max-height", "340px");

        Paragraph statusMsg = new Paragraph();
        statusMsg.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)").set("margin", "0");

        Button btnSearch = new Button("Search", VaadinIcon.SEARCH.create());
        btnSearch.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSearch.addClickListener(e -> {
            String q = tfQuery.getValue().trim();
            if (q.isEmpty()) return;
            resultsLayout.removeAll();
            statusMsg.setText("Searching…");
            List<String[]> hits = searchNominatim(q);
            statusMsg.setText("");
            if (hits.isEmpty()) {
                statusMsg.setText("No results found.");
            } else {
                for (String[] hit : hits) {
                    String displayName = hit[0];
                    String lat = hit[1];
                    String lon = hit[2];
                    Button btnResult = new Button(displayName);
                    btnResult.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                    btnResult.setWidthFull();
                    btnResult.getStyle()
                            .set("text-align", "left")
                            .set("white-space", "normal")
                            .set("height", "auto")
                            .set("min-height", "2.5em")
                            .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)");
                    btnResult.addClickListener(ce -> {
                        tfLat.setValue(lat);
                        tfLon.setValue(lon);
                        if (tfName.getValue().isBlank()) {
                            String shortName = displayName.contains(",")
                                    ? displayName.substring(0, displayName.indexOf(",")).trim()
                                    : displayName;
                            tfName.setValue(shortName);
                        }
                        dlg.close();
                    });
                    resultsLayout.add(btnResult);
                }
            }
        });

        tfQuery.addKeyPressListener(Key.ENTER, e -> btnSearch.click());

        HorizontalLayout searchRow = new HorizontalLayout(tfQuery, btnSearch);
        searchRow.setWidthFull();
        searchRow.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        searchRow.setFlexGrow(1, tfQuery);

        VerticalLayout content = new VerticalLayout(dlgTitle, searchRow, statusMsg, resultsLayout);
        content.setPadding(true);
        content.setSpacing(false);
        content.setWidthFull();
        content.getStyle().set("gap", "var(--lumo-space-s)");
        dlg.add(content);
        dlg.open();
        tfQuery.focus();
    }

    private List<String[]> searchNominatim(String query) {
        List<String[]> results = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=5&addressdetails=0"))
                    .header("User-Agent", "PhotoActApp/1.0 (nickgiant@yahoo.com)")
                    .header("Accept-Language", "en")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arr = mapper.readTree(response.body());
            for (JsonNode node : arr) {
                String displayName = node.path("display_name").asText("");
                String lat = node.path("lat").asText("");
                String lon = node.path("lon").asText("");
                if (!lat.isEmpty() && !lon.isEmpty()) {
                    results.add(new String[]{displayName, lat, lon});
                }
            }
        } catch (Exception ex) {
            logger.error("Nominatim search failed for query '{}': {}", query, ex.getMessage());
        }
        return results;
    }

    private void getUserClientInfo() {

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

    private void deleteStoryInfo(String strAlbumId, String strMemberId) {

        String[] field = {"photo_count"};
        String sqlCountPhotosOfTheAlbum = "SELECT s.id, s.title AS story_title, s.user_id, s.story_visible_to, s.description " +
                " " +
                " FROM photo_stories_photo sp, photo_stories s " +
                " WHERE sp.story_id = s.id AND sp.user_id = s.user_id AND s.user_id = " + strMemberId + " AND s.id = " + strAlbumId +
                " ORDER BY s.title ";
        List<Record> lstCountPhotoInAlbum = getRecordsFromDb(sqlCountPhotosOfTheAlbum, field);

        if (!lstCountPhotoInAlbum.isEmpty()) {

            String messageUp = "This Photo-Story already has items! Please remove them first.";
            Notification notificationUp = Notification.show(messageUp, 5000, Notification.Position.TOP_CENTER);
            notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);

        } else {
            String sqlDeleteAlbum = "DELETE FROM photo_stories WHERE id='" + strAlbumId + "' AND user_id = '" + strMemberId + "'";

            if (recordService.insertOneRecordWithQuery(sqlDeleteAlbum, null, null) == 1) {
                String messageUp = "Photo-Story is deleted!";
                Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                notificationUp.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                String sqlMemberAlbums = sqlMemberOfAlbums + sqlMemberOfAlbumsOrderBy;

                List<Record> lstAlbums = getRecordsFromDb(sqlMemberAlbums, arrColumnsMemberAlbums);
                List<String> lstAlbumTitle = new ArrayList<>();
                List<String> lstAlbumId = new ArrayList<>();
                for (int i = 0; i < lstAlbums.size(); i++) {
                    lstAlbumTitle.add(lstAlbums.get(i).getColumnData("title"));
                    lstAlbumId.add(lstAlbums.get(i).getColumnData("id"));
                }
                listBoxAlbums.setItems(lstAlbumTitle);

                reUpdateMyStoriesCount(Integer.parseInt(strMemberId));
            } else {
                String messageUp = "Photo-Story is not deleted !";
                Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }

    private boolean saveStoryInfo(String strAlbumTitle, String strAlbumDescription, Select<String> cmbAlbumType, List<String> lstAlbumTypes, List<String> lstAlbumTypeIds,
                                  String strAlbumId, String strMemberId) {

        if (strAlbumTitle.isEmpty()) {
            String messageUp = "Title is needed.";
            Notification notificationUp = Notification.show(messageUp, 5000, Notification.Position.TOP_CENTER);
            notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (cmbAlbumType == null || cmbAlbumType.getValue() == null || cmbAlbumType.getValue().isEmpty()) {
            String messageUp = "Please select a category";
            Notification notificationUp = Notification.show(messageUp, 5000, Notification.Position.TOP_CENTER);
            notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        Object[] fieldValueS = new Object[1];
        String[] fieldValueTypeS = new String[1];
        fieldValueS[0] = strAlbumTitle;
        fieldValueTypeS[0] = "java.lang.String";

        String sqlMemberWithAlbums = sqlMemberOfAlbums + " AND id <> '" + strAlbumId + "' AND user_id = '" + strMemberId + "' AND title = ? ";
        if (recordService.findAll(sqlMemberWithAlbums, arrColumnsMemberAlbums, fieldValueS, fieldValueTypeS).size() == 1) {

            String messageUp = "Title already exists! Please type an other one.";
            Notification notificationUp = Notification.show(messageUp, 5000, Notification.Position.TOP_CENTER);
            notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        } else {
            String strAlbumTypeId = "";

            String strSelectedType = cmbAlbumType.getValue();

            for (int i = 0; i < lstAlbumTypeIds.size(); i++) {
                if (lstAlbumTypes.get(i).equalsIgnoreCase(strSelectedType)) {
                    //event.getSource().setTooltipText(lstDestinationsId.get(i));
                    strAlbumTypeId = lstAlbumTypeIds.get(i);
                }
            }

            logger.info(" strAlbumId:" + strAlbumId + "  strMemberId:" + strMemberId);

            if (strAlbumId == null && !strMemberId.isEmpty()) {
                if (!strAlbumTypeId.isEmpty() && !strAlbumTitle.isEmpty()) {
                    StringBuilder strInsert = new StringBuilder("INSERT INTO photo_stories (");
                    StringBuilder placeholders = new StringBuilder("(");
                    Object[] fieldValue = new Object[5];
                    String[] fieldValueType = new String[5];

                    boolean first = true;

                    strInsert.append("type_id");
                    placeholders.append(" 1 ");

                    first = false;


                    if (strAlbumTitle != null) {
                        if (!first) {
                            strInsert.append(", ");
                            placeholders.append(", ");
                        }
                        strInsert.append("title");
                        placeholders.append("?");
                        fieldValue[0] = strAlbumTitle;
                        fieldValueType[0] = "java.lang.String";
                        first = false;


                        String slug = SlugUtil.toSlug(strAlbumTitle);

                        strInsert.append(", ");
                        placeholders.append(", ");
                        strInsert.append("slug");
                        placeholders.append("?");
                        fieldValue[1] = slug;
                        fieldValueType[1] = "java.lang.String";

                    }

                    if (strAlbumDescription != null) {
                        if (!first) {
                            strInsert.append(", ");
                            placeholders.append(", ");
                        }
                        strInsert.append("description");
                        placeholders.append("?");
                        fieldValue[2] = strAlbumDescription;
                        fieldValueType[2] = "java.lang.String";
                        first = false;
                    }

                    if (strAlbumTypeId != null) {
                        if (!first) {
                            strInsert.append(", ");
                            placeholders.append(", ");
                        }
                        strInsert.append("category_id");
                        placeholders.append("?");
                        fieldValue[3] = strAlbumTypeId;
                        fieldValueType[3] = "java.lang.Integer";
                        first = false;
                    }

                    // user_id is typically required
                    if (!first) {
                        strInsert.append(", ");
                        placeholders.append(", ");
                    }
                    strInsert.append("user_id");
                    placeholders.append("?");
                    fieldValue[4] = strMemberId;
                    fieldValueType[4] = "java.lang.Integer";

                    strInsert.append(") VALUES ");
                    placeholders.append(")");
                    strInsert.append(placeholders);

                    if (recordService.insertOneRecordWithQuery(strInsert.toString(), fieldValue, fieldValueType) == 1) {
                        String messageUp = "Photo-Story is created !";
                        Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                        notificationUp.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                        String sqlMemberAlbums = sqlMemberOfAlbums + sqlMemberOfAlbumsOrderBy;

                        List<Record> lstAlbums = getRecordsFromDb(sqlMemberAlbums, arrColumnsMemberAlbums);
                        List<String> lstAlbumTitle = new ArrayList<>();
                        List<String> lstAlbumId = new ArrayList<>();
                        for (int i = 0; i < lstAlbums.size(); i++) {
                            lstAlbumTitle.add(lstAlbums.get(i).getColumnData("title"));
                            lstAlbumId.add(lstAlbums.get(i).getColumnData("id"));
                        }
                        listBoxAlbums.setItems(lstAlbumTitle);

                        reUpdateMyStoriesCount(Integer.parseInt(strMemberId));
                    } else {
                        String messageUp = "Photo-Story is not created !";
                        Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                        notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
            } else if (strAlbumId != null && !strMemberId.isEmpty()) {

                StringBuilder strUpdate = new StringBuilder("UPDATE photo_stories SET ");
                Object[] fieldValue = new Object[5];
                String[] fieldValueType = new String[5];

                boolean first = true;

                // Only add columns that have non-null values
                if (strAlbumTitle != null) {
                    if (!first) strUpdate.append(", ");
                    strUpdate.append("title = ?");
                    fieldValue[0] = strAlbumTitle;
                    fieldValueType[0] = "java.lang.String";
                    first = false;
                }

                if (strAlbumDescription != null) {
                    if (!first) strUpdate.append(", ");
                    strUpdate.append("description = ?");
                    fieldValue[1] = strAlbumDescription;
                    fieldValueType[1] = "java.lang.String";
                    first = false;
                }

                if (strAlbumTypeId != null) {
                    if (!first) strUpdate.append(", ");
                    strUpdate.append("category_id = ?");
                    fieldValue[2] = strAlbumTypeId;
                    fieldValueType[2] = "java.lang.Integer";
                    first = false;
                }

                strUpdate.append(" WHERE id = ? AND user_id = ?");
                fieldValue[3] = strAlbumId;
                fieldValueType[3] = "java.lang.Integer";
                fieldValue[4] = strMemberId;
                fieldValueType[4] = "java.lang.Integer";

                if (recordService.insertOneRecordWithQuery(strUpdate.toString(), fieldValue, fieldValueType) == 1) {
                    String messageUp = "Saved !";
                    Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                    notificationUp.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    String sqlMemberAlbums = sqlMemberOfAlbums + sqlMemberOfAlbumsOrderBy;

                    List<Record> lstAlbums = getRecordsFromDb(sqlMemberAlbums, arrColumnsMemberAlbums);
                    List<String> lstAlbumTitle = new ArrayList<>();
                    List<String> lstAlbumId = new ArrayList<>();
                    for (int i = 0; i < lstAlbums.size(); i++) {
                        lstAlbumTitle.add(lstAlbums.get(i).getColumnData("title"));
                        lstAlbumId.add(lstAlbums.get(i).getColumnData("id"));
                    }
                    listBoxAlbums.setItems(lstAlbumTitle);

                    reUpdateMyStoriesCount(Integer.parseInt(strMemberId));

                } else {

                    String messageUp = "Not Saved !";
                    Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                    notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
            return true;
        }

    }

    private Dialog loadStoryEditDialog(String sqlAlbumCategories, String[] arrAlbumCategoriesColumns, String strTitle,
                                       String sqlAlbum, String[] arrAlbumColumns,
                                       String strAlbumId, String strMemberId) {
        Dialog dlg = new Dialog();
        dlg.setMinWidth("580px");
        dlg.setDraggable(true);
        dlg.setResizable(false);
        dlg.setCloseOnEsc(false);
        dlg.setCloseOnOutsideClick(false);

        String strAlbumTitle = "";
        String strAlbumDescription = "";
        String strAlbumCategoryId = "";


        if (strAlbumId != null) {
            String sqlAlbumInfo = "";

            Object[] fieldValueS = new Object[1];
            String[] fieldValueTypeS = new String[1];
            fieldValueS[0] = listBoxAlbums.getValue();
            fieldValueTypeS[0] = "java.lang.String";

            if (!strAlbumId.isEmpty()) {
                sqlAlbumInfo = sqlAlbum + " AND id = '" + strAlbumId + "' AND s.user_id = '" + strMemberId + "' ";
            } else {
                sqlAlbumInfo = sqlAlbum + " AND title = ? AND s.user_id = '" + strMemberId + "' ";
            }

            List<Record> lstAlbum = getRecordsFromDb(sqlAlbumInfo, arrAlbumColumns, fieldValueS, fieldValueTypeS);
            if (lstAlbum.isEmpty()){
                String messageUp = "There are no Photo-Stories yet!";
                Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.TOP_CENTER);
                strAlbumId = "0";
                return null;
            }else {
                strAlbumId = lstAlbum.get(0).getColumnData("id");
                strAlbumTitle = lstAlbum.get(0).getColumnData("title");
                strAlbumDescription = lstAlbum.get(0).getColumnData("description");
                strAlbumCategoryId = lstAlbum.get(0).getColumnData("category_id");
            }
        }

        // Load existing cover photo
        String strCoverPhotoId = "";
        String strCoverPhotoFile = "";
        if (strAlbumId != null && !strAlbumId.isEmpty() && !strAlbumId.equals("0")) {
            String[] coverCols = {"photo_id1"};
            List<Record> lstCover = recordService.findAll(
                    "SELECT photo_id1 FROM photo_stories WHERE id = " + strAlbumId, coverCols);
            if (!lstCover.isEmpty()) {
                String rawCoverId = lstCover.get(0).getColumnData("photo_id1");
                if (rawCoverId != null && !rawCoverId.isEmpty() && !rawCoverId.equalsIgnoreCase("null")) {
                    strCoverPhotoId = rawCoverId;
                    String[] metaCols = {"name_new"};
                    List<Record> lstMeta = recordService.findAll(
                            "SELECT name_new FROM photo_meta WHERE id = " + strCoverPhotoId, metaCols);
                    if (!lstMeta.isEmpty()) {
                        String rawFile = lstMeta.get(0).getColumnData("name_new");
                        if (rawFile != null && !rawFile.equalsIgnoreCase("null")) strCoverPhotoFile = rawFile;
                    }
                }
            }
        }
        final String[] coverIdHolder = {strCoverPhotoId};

        VerticalLayout layoutAlbumInfo = new VerticalLayout();
        layoutAlbumInfo.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.MEDIUM);

        Div divTitleCaption = new Div("Photo-Story");
        divTitleCaption.setText(strTitle);


        TextField txtAlbumTitle = new TextField("Title");
        txtAlbumTitle.setRequiredIndicatorVisible(true);
        txtAlbumTitle.setMaxLength(70);
        txtAlbumTitle.setWidthFull();
        if (strAlbumTitle != null) {
            txtAlbumTitle.setValue(strAlbumTitle);
        }

        TextArea txtAlbumDescription = new TextArea("Description");
        txtAlbumDescription.setMaxLength(350);
        txtAlbumDescription.setWidthFull();
        txtAlbumDescription.setValue(strAlbumDescription);
        txtAlbumDescription.setMinRows(6);

        List<Record> lstAlbumCategories = getRecordsFromDb(sqlAlbumCategories, arrAlbumCategoriesColumns);
        List<String> lstAlbumCategoryTitle = new ArrayList<String>();
        List<String> lstAlbumCategoryId = new ArrayList<String>();
        for (int i = 0; i < lstAlbumCategories.size(); i++) {
            lstAlbumCategoryTitle.add(lstAlbumCategories.get(i).getColumnData("cat_title"));
            lstAlbumCategoryId.add(lstAlbumCategories.get(i).getColumnData("id"));
        }
        Select<String> selAlbumCategory = new Select<>();
        selAlbumCategory.setRequiredIndicatorVisible(true);
        selAlbumCategory.setWidthFull();
        selAlbumCategory.setLabel("Category");
        selAlbumCategory.setItems(lstAlbumCategoryTitle);
        for (int r = 0; r < lstAlbumCategoryId.size(); r++) {
            if (strAlbumCategoryId.equalsIgnoreCase(lstAlbumCategoryId.get(r))) {
                selAlbumCategory.setValue(lstAlbumCategoryTitle.get(r));
            }
        }

        // Cover photo section
        Div thumbDiv = new Div();
        thumbDiv.getStyle().set("width", "100px").set("height", "80px")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center")
                .set("border-radius", "var(--lumo-border-radius-s)").set("overflow", "hidden");
        if (!strCoverPhotoFile.isEmpty() && !strCoverPhotoFile.equalsIgnoreCase("null")) {
            String strPathPhotos = DIR_PHOTOS_SERVER + dirChar + subPathSmall + dirChar;
            Image imgCover = new Image();
            imgCover.setAlt("Cover");
            imgCover.setSrc(DownloadHandler.forFile(new File(strPathPhotos + strCoverPhotoFile)));
            imgCover.setMaxHeight("80px");
            imgCover.setWidth("auto");
            thumbDiv.add(imgCover);
        } else {
            thumbDiv.add(new Span("No cover"));
        }

        Dialog dlgCoverSelection = new Dialog();
        dlgCoverSelection.setHeightFull();
        dlgCoverSelection.setMinWidth("1000px");
        dlgCoverSelection.setCloseOnEsc(true);
        dlgCoverSelection.setDraggable(true);
        dlgCoverSelection.setCloseOnOutsideClick(true);
        dlgCoverSelection.setResizable(true);
        Button btnCloseCoverDlg = new Button();
        btnCloseCoverDlg.setIcon(VaadinIcon.CLOSE_BIG.create());
        btnCloseCoverDlg.addClickListener(ce -> dlgCoverSelection.close());
        HorizontalLayout coverDlgHeader = new HorizontalLayout(new Span("Select Cover Photo"), btnCloseCoverDlg);
        coverDlgHeader.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.BETWEEN);
        String sqlGalleryForCover = sqlMemberGallery + " AND usr.username = '" + strMember + "' ";
        dlgCoverSelection.add(coverDlgHeader);
        dlgCoverSelection.add(loadPhotos(sqlGalleryForCover, arrColumnMemberGallery, dlgCoverSelection));
        dlgCoverSelection.addOpenedChangeListener(ce -> {
            if (!ce.isOpened() && !strSelectedPhotoId.isEmpty()) {
                coverIdHolder[0] = strSelectedPhotoId;
                thumbDiv.removeAll();
                String[] metaCols2 = {"name_new"};
                List<Record> lstMeta2 = recordService.findAll(
                        "SELECT name_new FROM photo_meta WHERE id = " + strSelectedPhotoId, metaCols2);
                if (!lstMeta2.isEmpty()) {
                    String newFile = lstMeta2.get(0).getColumnData("name_new");
                    if (newFile != null && !newFile.isEmpty() && !newFile.equalsIgnoreCase("null")) {
                        String strPathPhotos2 = DIR_PHOTOS_SERVER + dirChar + subPathSmall + dirChar;
                        Image imgNew = new Image();
                        imgNew.setAlt("Cover");
                        imgNew.setSrc(DownloadHandler.forFile(new File(strPathPhotos2 + newFile)));
                        imgNew.setMaxHeight("80px");
                        imgNew.setWidth("auto");
                        thumbDiv.add(imgNew);
                    }
                }
            }
        });

        Button btnSelectCoverPhoto = new Button("Select Cover Photo");
        btnSelectCoverPhoto.setIcon(VaadinIcon.PICTURE.create());
        btnSelectCoverPhoto.addClickListener(ce -> {
            strSelectedPhotoId = "";  // clear so only a fresh selection in this dialog counts
            dlgCoverSelection.open();
        });

        HorizontalLayout coverSection = new HorizontalLayout(thumbDiv, btnSelectCoverPhoto);
        coverSection.addClassNames(AlignItems.CENTER);
        coverSection.setWidthFull();

        Button btnOk = new Button("Save");
        btnOk.setIcon(FontAwesome.Regular.CHECK_SQUARE.create());
        final String strAlbumIdFinal = strAlbumId;
        btnOk.addClickListener(ok ->
        {
            if (saveStoryInfo(txtAlbumTitle.getValue(), txtAlbumDescription.getValue(), selAlbumCategory, lstAlbumCategoryTitle, lstAlbumCategoryId,
                    strAlbumIdFinal, strMemberId)) {
                if (!coverIdHolder[0].isEmpty() && !coverIdHolder[0].equalsIgnoreCase("null")) {
                    String storyIdToUpdate = strAlbumIdFinal;
                    if (storyIdToUpdate == null) {
                        String[] idCols = {"id"};
                        List<Record> lstLatest = recordService.findAll(
                                "SELECT id FROM photo_stories WHERE user_id = " + strMemberId + " ORDER BY date_inserted DESC LIMIT 1",
                                idCols);
                        if (!lstLatest.isEmpty()) storyIdToUpdate = lstLatest.get(0).getColumnData("id");
                    }
                    if (storyIdToUpdate != null && !storyIdToUpdate.isEmpty()) {
                        Object[] updCoverVals = {coverIdHolder[0], storyIdToUpdate, strMemberId};
                        String[] updCoverTypes = {"java.lang.Integer", "java.lang.Integer", "java.lang.Integer"};
                        recordService.insertOneRecordWithQuery(
                                "UPDATE photo_stories SET photo_id1 = ? WHERE id = ? AND user_id = ?",
                                updCoverVals, updCoverTypes);
                    }
                }
                dlg.close();
            }
        });

        Button btnClose = new Button("Cancel");
        btnClose.setIcon(FontAwesome.Regular.WINDOW_CLOSE.create());
        btnClose.addClickListener(close ->
        {
            dlg.close();
        });

        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.add(btnOk, btnClose);


        layoutAlbumInfo.add(divTitleCaption, txtAlbumTitle, txtAlbumDescription, selAlbumCategory, coverSection, layoutButtons);
        dlg.add(layoutAlbumInfo);

        return dlg;
    }


    private int reUpdateMyStoriesCount(int intUserId) {

        Object[] fieldValueCount = {intUserId};
        String[] fieldTypeCount = {"java.lang.Integer"};

        String strUpdateCount = "UPDATE dbuser_extra AS d " +
                " JOIN ( " +
                "    SELECT user_id, COUNT(*) AS stories_count " +
                "    FROM photo_stories " +
                "    WHERE story_visible_to = 'ALL' " +
                "    GROUP BY user_id " +
                " ) AS p ON d.user_id = p.user_id " +
                " SET d.username = NULL , d.count_stories = p.stories_count " +
                " WHERE d.user_id = ? ";
        return recordService.insertOneRecordWithQuery(strUpdateCount, fieldValueCount, fieldTypeCount);

    }

    private int reUpdateMyStoriesPhotosCount(int intUserId) {

        Object[] fieldValueCount = {intUserId};
        String[] fieldTypeCount = {"java.lang.Integer"};

        String strUpdateCount = "UPDATE dbuser_extra AS d " +
                " JOIN ( " +
                "    SELECT s.user_id, COUNT(*) AS story_photos_count " +
                "    FROM photo_stories s, photo_stories_photo p " +
                "    WHERE story_visible_to = 'ALL' " +
                "    AND s.id = p.story_id AND s.user_id = p.user_id " +
                "    AND p.photo_id is not null " +
                "    GROUP BY s.user_id " +
                " ) AS p ON d.user_id = p.user_id " +
                " SET d.username = NULL , d.count_story_photos = p.story_photos_count " +
                " WHERE d.user_id = ? ";
        return recordService.insertOneRecordWithQuery(strUpdateCount, fieldValueCount, fieldTypeCount);

    }

    private Dialog loadStoryItemEditDialog(String strTitle, boolean isInsert,
                                           String sqlMemberStories, String[] arrColumnsMemberAlbums,
                                           String sqlStoryItems, String[] arrColumnsStoryItems,
                                           Dialog dlgPhotoSelection,
                                           String strMemberId, String strStoryItemId) {
        Dialog dlg = new Dialog();
        dlg.setMinWidth("680px");
        dlg.setDraggable(true);
        dlg.setResizable(false);
        dlg.setCloseOnEsc(false);
        dlg.setCloseOnOutsideClick(false);

        String strInc = "1";
        String strAlbumTitle = "";
        String strAlbumDescription = "";
        String strAlbumCategoryId = "";
        String strItemPhotoId = "";
        String strItemPhotoFile = "";
        String strMetaOrientation = "";

        String strPathPhotos = DIR_PHOTOS_SERVER + dirChar + subPathSmall + dirChar;

        List<Record> lstStories = getRecordsFromDb(sqlMemberStories, arrColumnsMemberAlbums);
        List<String> lstStoriesTitle = new ArrayList<>();
        List<String> lstStoriesId = new ArrayList<>();
        for (int i = 0; i < lstStories.size(); i++) {
            lstStoriesTitle.add(lstStories.get(i).getColumnData("title"));
            lstStoriesId.add(lstStories.get(i).getColumnData("id"));
        }

        for (int i = 0; i < lstStoriesTitle.size(); i++) {
            if (lstStoriesTitle.get(i).equalsIgnoreCase(listBoxAlbums.getValue())) {
                strSelectedStoryId = lstStoriesId.get(i);
            }
        }

        if (!strStoryItemId.isEmpty() && !strStoryItemId.isEmpty()) {
            String sqlStoryItemInfo = "";
//            if (!strAlbumId.isEmpty()) {
            sqlStoryItemInfo = sqlStoryItems + " AND i.story_id = '" + strSelectedStoryId + "' AND i.id = '" + strStoryItemId + "' AND s.user_id = '" + strMemberId + "' ";
//            } else {
//                sqlAlbumInfo = sqlAlbum + " AND title = '" + listBoxAlbums.getValue() + "' AND s.user_id = '" + strMemberId + "' ";
//            }
            List<Record> lstStoryItem = getRecordsFromDb(sqlStoryItemInfo, arrColumnsStoryItems);
            if (!lstStoryItem.isEmpty()) {
                strInc = lstStoryItem.get(0).getColumnData("inc");
                strAlbumTitle = lstStoryItem.get(0).getColumnData("item_title");
                strAlbumDescription = lstStoryItem.get(0).getColumnData("descr");
                strAlbumCategoryId = lstStoryItem.get(0).getColumnData("item_type");
                strItemPhotoId = lstStoryItem.get(0).getColumnData("photo_id");
                strItemPhotoFile = lstStoryItem.get(0).getColumnData("name_new");
                strMetaOrientation = lstStoryItem.get(0).getColumnData("meta_orientation").toString();
            }
        } else {

        }

        List<String> lstAlbumCategoryTitle = new ArrayList<String>();
        String[] arrCategories = {"Header", "Text", "Photo", "Tip", "Summary"};
        lstAlbumCategoryTitle = Arrays.stream(arrCategories).toList();

//        List<String> lstAlbumCategoryTitleGr = new ArrayList<String>();
//        String[] arrCategoriesGr = {"Header", "Text", "Photo", "Tip", "Summary"};
//        lstAlbumCategoryTitleGr = Arrays.stream(arrCategoriesGr).toList();

        VerticalLayout layoutAlbumInfo = new VerticalLayout();
        layoutAlbumInfo.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.MEDIUM);

        Div divTitleCaption = new Div("Photo-Story properties");
        divTitleCaption.setText(strTitle);

        Select<String> selAlbumCategoryGr = new Select<>();
        selAlbumCategoryGr.setRequiredIndicatorVisible(true);
        selAlbumCategoryGr.setWidthFull();
        selAlbumCategoryGr.setLabel("Category");
        selAlbumCategoryGr.setItems(lstAlbumCategoryTitle);


        HorizontalLayout divImage = new HorizontalLayout();

        for (int r = 0; r < lstAlbumCategoryTitle.size(); r++) {
            if (strAlbumCategoryId.equalsIgnoreCase(lstAlbumCategoryTitle.get(r))) {
                selAlbumCategoryGr.setValue(lstAlbumCategoryTitle.get(r));
            }
        }


        if (strItemPhotoFile.isEmpty()) {
            divImage.add(new Div("Empty"));
        } else {
            String imagePath = strPathPhotos + strItemPhotoFile;

            Image image = new Image();
            image.setAlt("Photo");
            divImage.add(image);
            File imgFile = new File(imagePath);
            image.setSrc(DownloadHandler.forFile(imgFile));

            image.setMaxHeight("125px");
            image.setHeight("120px");
            image.setWidth("auto");
            image.setMaxWidth("170px");

            if (strMetaOrientation.equalsIgnoreCase("8")) {
                image.getStyle().set("rotate", "-90deg");
                image.setMaxWidth("125px");
//                                    image.setWidth("120px");
//                                    image.setHeight("auto");
                image.setMaxHeight("170px");
            } else if (strMetaOrientation.equalsIgnoreCase("6")) {
                image.getStyle().set("rotate", "90deg");
                image.setMaxWidth("125px");
//                                    image.setWidth("120px");
//                                    image.setHeight("auto");
                image.setMaxHeight("170px");
            } else {

            }

            image.getStyle().setBorderRadius("6px");
        }
        divImage.setHeight("130px");


//        Button btnAddPhotos = new Button();
//        btnAddPhotos.addClassNames(Padding.LARGE);
//        btnAddPhotos.setIcon(VaadinIcon.PICTURE.create());
//        btnAddPhotos.addClickListener(clickEvent -> {
//            dlgPhotoSelection.open();
//        });
//        divImage.add(btnAddPhotos);

        TextField txtInc = new TextField("inc (top 0, towards bottom incremented)");
        txtInc.setMaxLength(4);
        txtInc.setWidthFull();
        if (strInc != null) {
            txtInc.setValue(strInc);
        }

        TextField txtAlbumTitle = new TextField("Title");
        txtAlbumTitle.setMaxLength(100);
        txtAlbumTitle.setWidthFull();
        if (strAlbumTitle != null) {
            txtAlbumTitle.setValue(strAlbumTitle);
        }

        TextArea txtAlbumDescription = new TextArea("Description");
        txtAlbumDescription.setRequiredIndicatorVisible(true);
        txtAlbumDescription.setMaxLength(400);
        txtAlbumDescription.setWidthFull();
        txtAlbumDescription.setMinRows(8);
        if (strAlbumDescription != null) {
            txtAlbumDescription.setValue(strAlbumDescription);
        }

        selAlbumCategoryGr.addValueChangeListener(sel -> {
            if (sel.getSource().getValue().equalsIgnoreCase("Photo")) {
                divImage.setVisible(true);
                txtAlbumTitle.setVisible(false);
            } else {
                divImage.setVisible(false);
                txtAlbumTitle.setVisible(true);
            }
        });
        if (selAlbumCategoryGr.getValue() == null) {
            selAlbumCategoryGr.setValue("Κείμενο");
        }
        if (selAlbumCategoryGr.getValue().equalsIgnoreCase("Photo")) {
            divImage.setVisible(true);
            txtAlbumTitle.setVisible(false);
        } else {
            divImage.setVisible(false);
            txtAlbumTitle.setVisible(true);
        }

        Button btnOk = new Button("Save");
        btnOk.setIcon(FontAwesome.Regular.CHECK_SQUARE.create());
        // final String strAlbumIdFinal = strStoryId;
        btnOk.addClickListener(ok ->
        {

            String strItemInc = "";
            if (txtInc.getValue() != null && !txtInc.getValue().isEmpty()) {
                strItemInc = txtInc.getValue();
            }

            String strItemTitle = "";
            if (txtAlbumTitle.getValue() != null && !txtAlbumTitle.getValue().isEmpty()) {
                strItemTitle = txtAlbumTitle.getValue();
            }

            String strItemDescription = "";
            if (txtAlbumDescription.getValue() != null && !txtAlbumDescription.getValue().isEmpty()) {
                strItemDescription = txtAlbumDescription.getValue();
            }

            if (isInsert) {
                saveStoryItem(strMemberId, strSelectedStoryId, "", selAlbumCategoryGr.getValue(), strItemInc, strItemTitle, strItemDescription);
            } else {
                saveStoryItem(strMemberId, strSelectedStoryId, strStoryItemId, selAlbumCategoryGr.getValue(), strItemInc, strItemTitle, strItemDescription);
            }

            dlg.close();
        });

        Button btnClose = new Button("Cancel");
        btnClose.setIcon(FontAwesome.Regular.WINDOW_CLOSE.create());
        btnClose.addClickListener(close ->
        {
            dlg.close();
        });

        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.add(btnOk, btnClose);


        layoutAlbumInfo.add(divTitleCaption, selAlbumCategoryGr, txtInc, divImage, txtAlbumTitle, txtAlbumDescription, layoutButtons);
        dlg.add(layoutAlbumInfo);

        return dlg;
    }


    private void initPaging(String sqlRead) {
        totalRows = photoStoryService.count(sqlRead);
        totalPages = (int) Math.ceil((double) totalRows / pageSize);

        currentPage = 0;
        updatePager();
        goToPage(0);
    }

    private void refreshPagingAfterWrite(String sqlRead) {
        totalRows = photoStoryService.count(sqlRead);
        totalPages = (int) Math.ceil((double) totalRows / pageSize);
        updatePager();
    }

    private HorizontalLayout buildPager() {

        firstBtn = new Button("⏮ Πρόσφατες", e -> goToFirst());
        prevBtn = new Button("◀ Προυγούμενες", e -> goToPrev());
        nextBtn = new Button("Επόμενες ▶", e -> goToNext());
        lastBtn = new Button("Παλιότερες ⏭", e -> goToLast());

        pageInfoTop = new Span();


        HorizontalLayout pager = new HorizontalLayout(
                firstBtn, prevBtn, pageInfoTop, nextBtn, lastBtn
        );

        pager.addClassNames(AlignItems.CENTER, JustifyContent.AROUND);
        return pager;
    }

    private void goToFirst() {
        goToPage(0);
    }

    private void goToLast() {
        goToPage(totalPages - 1);
    }

    private void goToNext() {
        if (currentPage < totalPages - 1) {
            goToPage(currentPage + 1);
        }
    }

    private void goToPrev() {
        if (currentPage > 0) {
            goToPage(currentPage - 1);
        }
    }

    private void goToPage(int pageIndex) {

        if (pageIndex < 0 || pageIndex >= totalPages) return;

        this.currentPage = pageIndex;

        int targetRowIndex = pageIndex * pageSize;

        grid.scrollToIndex(targetRowIndex);   // ✅ triggers new DB fetch
        updatePager();
    }

    private void updatePager() {

        String strRecs =
                "Σελίδα: " + (currentPage + 1) + " / " + totalPages +
                        " - Φωτογραφίες: " + totalRows + "";
        pageInfoTop.setText(strRecs);


        firstBtn.setEnabled(currentPage > 0);
        prevBtn.setEnabled(currentPage > 0);

        nextBtn.setEnabled(currentPage < totalPages - 1);
        lastBtn.setEnabled(currentPage < totalPages - 1);
    }

    private List<Record> getRecordsFromDb(String strUpdate, String[] arrColumnNames) {

        logger.info(" photo  getRecordsFromDb:   " + strUpdate);
        return recordService.findAll(strUpdate, arrColumnNames);
    }

    private List<Record> getRecordsFromDb(String strUpdate, String[] arrColumnNames, Object[] sqlParValue, String[] sqlParType) {
        logger.info(" photo  getRecordsFromDb with params:   " + strUpdate);
        return recordService.findAll(strUpdate, arrColumnNames, sqlParValue, sqlParType);
    }


    private int checkMemberExists(String sqlRead, String[] arrColumnNames, boolean isEditable) {

        int intUserId = 0;
        VerticalLayout layoutMember = new VerticalLayout();
        layoutMember.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextColor.TERTIARY,
                Padding.LARGE,
                Gap.SMALL
        );

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);

        if (lstRecords == null) {
            logger.warn("check lstRecords is null");
        } else if (lstRecords.isEmpty()) {
            logger.warn("check lstRecords is empty");
        } else if (lstRecords.size() == 1) {
            Record rec = lstRecords.get(0);
            String strUserId = rec.getColumnData("userId");
            try {
                intUserId = Integer.parseInt(strUserId);
            } catch (NumberFormatException e) {
                logger.error("strUserId: " + strUserId + "    " + e.getMessage());
            }

            String strNameOfUser = rec.getColumnData("username");
            String strCountOfPhotosOfAlbums = rec.getColumnData("photo_count");
            String strMemberSince = rec.getColumnData("member_since");
            String strAvatarPath = rec.getColumnData("avatar_path");


            Image imgAvatar = genericView.getAvatarThumbImage(strAvatarPath, strNameOfUser, "130px", "130px");

            H3 objMember = new H3(strNameOfUser);
            Div divMemberSince = new Div("Member since " + strMemberSince);
            Div divAlbumsAndPhotos = new Div("No photos uploaded yet! Try uploading now !");
            layoutMember.add(imgAvatar, objMember, divMemberSince, divAlbumsAndPhotos);
        } else {
            logger.warn("check lstRecords is more than one record");
        }

        verticalLayout.add(layoutMember);
        return intUserId;
    }

    private VerticalLayout getMembersPanels(String sqlRead, String[] arrColumnNames, boolean isEditable) {

        VerticalLayout membersLayout = new VerticalLayout();
        membersLayout.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.MEDIUM,
                Margin.NONE,
                Gap.LARGE
        );

        int intUserId = 0;


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);

        if (lstRecords == null) {
            logger.warn("check lstRecords is null");
        } else if (lstRecords.isEmpty()) {
            logger.warn("check lstRecords is empty");
        } else if (lstRecords.size() >= 1) {

            for (int r = 0; r < lstRecords.size(); r++) {

                VerticalLayout layoutMember = new VerticalLayout();
                layoutMember.addClassNames(
                        AlignItems.CENTER, JustifyContent.CENTER,
                        TextColor.TERTIARY,
                        Padding.LARGE,
                        Margin.LARGE,
                        Gap.MEDIUM
                );
                layoutMember.getStyle().setBorderRadius("60px");
                layoutMember.getStyle().setMaxWidth("610px");
//                layoutMember.getStyle().set("border", "lightgrey 1px solid");
                layoutMember.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER, Background.CONTRAST_5);

                Record rec = lstRecords.get(r);
                String strUserId = rec.getColumnData("userId");
                try {
                    intUserId = Integer.parseInt(strUserId);
                } catch (NumberFormatException e) {
                    logger.error("strUserId: " + strUserId + "    " + e.getMessage());
                }

                String strName = rec.getColumnData("name");
                String strSurname = rec.getColumnData("surname");
                String strUsername = rec.getColumnData("username");
                String strMemberSince = rec.getColumnData("member_since");
                String strAvatarPath = rec.getColumnData("avatar_path");

                String strResident = rec.getColumnData("resident");
                String strResidentCountry = rec.getColumnData("resident_country");

                String strShortBio = rec.getColumnData("short_bio");
                String strFb = rec.getColumnData("url_fb");
                String strYt = rec.getColumnData("url_yt");
                String strInsta = rec.getColumnData("url_insta");
                String strFlickr = rec.getColumnData("url_flickr");
                String strWebsite = rec.getColumnData("url_website");

                String strCountPhotos = rec.getColumnData("count_photos");
                String strCountStories = rec.getColumnData("count_stories");
                String strCountLearningsRef = rec.getColumnData("count_learnings_ref");

                Anchor linkWebsite = new Anchor();
                linkWebsite.add(FontAwesome.Solid.LINK.create());
//                linkWebsite.setVisible(false);
                if (strWebsite != null && !strWebsite.equalsIgnoreCase("null") && !strWebsite.isEmpty()) {
                    linkWebsite.setVisible(true);
                    linkWebsite.setHref(strWebsite);
                    linkWebsite.setTarget("_blank");
                }

                Anchor linkTutorYt = new Anchor();
                linkTutorYt.add(FontAwesome.Brands.YOUTUBE.create());
                // linkTutorYt.getStyle().setColor(strColorExternalweb);
                // linkTutorYt.setClassName("lazy-result-line-button");
//                linkTutorYt.setVisible(false);
                if (strYt != null && !strYt.equalsIgnoreCase("null") && !strYt.isEmpty()) {

                    linkTutorYt.setHref(strYt);
                    linkTutorYt.setTarget("_blank");
                    linkTutorYt.setVisible(true);
                }

                Anchor linkTutorFacebook = new Anchor();
                linkTutorFacebook.add(FontAwesome.Brands.FACEBOOK_F.create());
                // linkTutorWikipedia.getStyle().setColor(strColorExternalweb);
                //   linkTutorWikipedia.setClassName("lazy-result-line-button");
//            linkTutorFacebook.setVisible(false);

                if (strFb != null && !strFb.equalsIgnoreCase("null") && !strFb.isEmpty()) {
                    //linkTutorYt.setText("YouTube");
                    //strUrlTutorWikipedia = "https://www.youtube.com/"+strUrlTutorYt;
                    linkTutorFacebook.setHref(strFb);
                    linkTutorFacebook.setTarget("_blank");
                    linkTutorFacebook.setVisible(true);
                }

                Anchor linkTutorInsta = new Anchor();
                //  linkTutorInsta.setClassName("lazy-result-line-button");
                linkTutorInsta.add(FontAwesome.Brands.INSTAGRAM.create());
                // linkTutorInsta.getStyle().setColor(strColorExternalweb);
//            linkTutorInsta.setVisible(false);
                if (strInsta != null && !strInsta.equalsIgnoreCase("null") && !strInsta.isEmpty()) {
                    linkTutorInsta.setHref(strInsta);
                    linkTutorInsta.setTarget("_blank");
//                linkTutorInsta.setVisible(true);
                }

                Anchor linkFlickr = new Anchor();
                linkFlickr.add(FontAwesome.Brands.FLICKR.create());
                // linkTutorYt.getStyle().setColor(strColorExternalweb);
                // linkTutorYt.setClassName("lazy-result-line-button");
//                linkFlickr.setVisible(false);
                if (strFlickr != null && !strFlickr.equalsIgnoreCase("null") && !strFlickr.isEmpty()) {

                    linkFlickr.setHref(strFlickr);
                    linkFlickr.setTarget("_blank");
                    linkFlickr.setVisible(true);
                }

                Div divBioTitle = new Div("Short Bio");
                divBioTitle.addClassNames(TextColor.TERTIARY, FontWeight.BOLD);

                HorizontalLayout layoutMemberLinks = new HorizontalLayout();
                layoutMemberLinks.add(linkWebsite, linkTutorFacebook, linkTutorYt, linkTutorInsta, linkTutorYt, linkFlickr);

                Div divBio = new Div();
//            divBio.setVisible(false);
                if (strShortBio != null && !strShortBio.equalsIgnoreCase("null") && !strShortBio.isEmpty()) {
                    divBio.setVisible(true);
                    divBio.setText(strShortBio);
                } else {
//                divBio.setVisible(false);
                }

                Image imgAvatar = genericView.getAvatarThumbImage(strAvatarPath, strUsername, "150px", "150px");
//            Image imgAvatar = getAvatarThumbImage(strAvatarPath, strNameOfUser, "120px", "120px");

                H3 objName = new H3(strName + " " + strSurname);
                objName.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);
                H4 objMember = new H4(strUsername);
                objMember.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);
                Div divMemberSince = new Div("Member since " + strMemberSince);
                divMemberSince.addClassNames(TextColor.SECONDARY, FontWeight.MEDIUM);

                VerticalLayout layoutMemberCard = new VerticalLayout();
                layoutMemberCard.getStyle().setBorderRadius("30px");
                layoutMemberCard.getStyle().setWidth("330px");
                layoutMemberCard.getStyle().set("border", "lightgrey 1px solid");
                layoutMemberCard.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER, Background.CONTRAST_5);
                layoutMemberCard.add(imgAvatar, objName, objMember, layoutMemberLinks, divMemberSince);

                Div divResident = new Div("Lives at " + strResident + ", " + strResidentCountry);


                VerticalLayout layoutContribution = new VerticalLayout();
                Div divContribTitle = new Div("Contributed with");
                Div divCounts = new Div(strCountPhotos + ":Photos,  " + strCountStories + ":Albums,  ");

                Div divCounts2 = new Div(strCountLearningsRef + ":Learnings Referenced");

                layoutContribution.add(divContribTitle, divCounts, divCounts2);
                layoutContribution.getStyle().setBorderRadius("10px");
                layoutContribution.getStyle().setWidth("330px");
                layoutContribution.getStyle().set("border", "lightgrey 1px solid");
                layoutContribution.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER, Background.CONTRAST_5);

                layoutMember.add(layoutMemberCard, layoutContribution, divBioTitle, divBio, divResident);
                //layoutMember.add(imgAvatar, objName, objMember, divMemberSince, divShortBio, divResident, );

                membersLayout.add(layoutMember);
            }

        } else {
            logger.warn("check lstRecords is more than one record");
        }


        return membersLayout;
    }


    private void logVisitorToDb(String logText) {

//        category = category.replaceAll("'", " ");
//        category = category.replaceAll("\"", " ");

        //search = search.replaceAll("'"," ");
        //search = search.replaceAll("\""," ");

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously

            strUrlRequestToBeLogged = currentUrl.toExternalForm();

        });

        sysUserName = System.getProperty("user.name");


        // String ipAddress = VaadinSession.getCurrent().getBrowser().getAddress();
        String browser = VaadinSession.getCurrent().getBrowser().getBrowserApplication();
        int versionOfBrowserMajor = VaadinSession.getCurrent().getBrowser().getBrowserMajorVersion();
        int versionOfBrowserMinor = VaadinSession.getCurrent().getBrowser().getBrowserMinorVersion();
        int intUiId = VaadinSession.getCurrent().getNextUIid();


        int[] availWidth = calcTotalAvailableWidth();


        if (strUrlRequestToBeLogged == null || strUrlRequestToBeLogged.isEmpty() || strUrlRequestToBeLogged.equalsIgnoreCase("null")) {
            strUrlRequestToBeLogged = "NULL";
        } else {
            strUrlRequestToBeLogged = "'" + strUrlRequestToBeLogged + "'";
        }

//        if (strPath == null || strPath.isEmpty()) {
//            strPath = "NULL";
//        } else {
//            strPath = strPath.replace("\\", "-");
//            strPath = strPath.replace("'", "");
//            strPath = "'" + strPath + "'";
//        }


        logger.info("photo visitor:" + publicIp + " . " + hostname + " . " + hostAddress + " . " + canonicalHostname + "  .  " + browser + " " + sessionid);

        String insertSQL = "INSERT INTO dbvisitor_log SET visitorlogId = 0,  timeOfVisit = now(), ipAddress = '" + publicIp + "', browserName = '" + browser + "', "
                + " browserVersionMajor = '" + versionOfBrowserMajor + "', browserVersionMinor = '" + versionOfBrowserMinor + "', urlParameter = NULL , timeZoneId = '" + timeZoneId + "', "
                + " appVersion = '" + APP_NAME + "-" + APP_VERSION + "',  parentSection = 'photo',  sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
                + " hostAddress = '" + hostAddress + "', os = '" + strOS + "', browser = '" + strBrowser + "', section = '" + section + "',"
                + " item = '" + logText + "' , ref = " + strUrlRequestToBeLogged + ", "
                + " locale = '" + locale + "', localeName ='" + localeName + "' ";

        ArrayList<String> lstQueryInsert = new ArrayList<String>();
        lstQueryInsert.add(insertSQL);

        recordService.massRecordInsert(lstQueryInsert, null, null);
    }

    public int[] calcTotalAvailableWidth() {
        final int[] availWidth = {-1, -1, -1};

        UI.getCurrent().getPage().retrieveExtendedClientDetails(details -> {
            // This is your own method that you may do something with the screen width.
            // Note that this method runs asynchronously
            availWidth[0] = details.getWindowInnerWidth();
            availWidth[1] = details.getBodyClientWidth();
            availWidth[2] = details.getScreenWidth();

            logger.info("availWidth:  window inner " + details.getWindowInnerWidth() + " body client  " + details.getBodyClientWidth() + "  screen  " + details.getScreenWidth());

        });
        return availWidth;
    }

    private String getFileSize(File file) {

        return String.format("%.2f", getFileSizeDouble(file));

    }

    private double getFileSizeDouble(File file) {

        double filesizeMB = (double) file.length() / (1024 * 1024);// + " mb";
        return filesizeMB;
    }

    private String getMBFromLong(long size) {

        double filesizeMB = (double) size / (1024 * 1024);// + " mb";
        return String.format("%.2f", filesizeMB);
    }
}