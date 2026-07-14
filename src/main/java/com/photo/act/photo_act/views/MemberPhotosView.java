package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.dto.LearningDto;
import com.photo.act.photo_act.services.*;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.*;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.HomeView.subPathSmall;
import static com.photo.act.photo_act.views.MainLayout.*;


//@RolesAllowed("Admin")
@PermitAll

@Route(value = "member-photos") //":section?")
//@RouteAlias(value = "members/name/:member?", layout = MainLayout.class)
//@RouteAlias(value = ":section/:member?", layout = MainLayout.class)
//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class MemberPhotosView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

    private static final Logger logger = LoggerFactory.getLogger(MemberPhotosView.class);
    public static String subPathThumbs = "photo-thumbs";
    public static String subPathMedium = "photo-medium";
    public static String subPathUpload = "photo-upload";
    public static String subPathShow = "photo-show";
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
            " DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since , getDateDiffFromNow(usr.date_joined) AS member_for " +
            " , usr.avatar_path, name, surname, short_bio, url_insta, url_fb, url_flickr, url_yt, email, resident, resident_country " +
            " , usrx.count_photos, usrx.count_stories, usrx.count_learnings_ref " +
            //     "--  , pa.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
            " FROM dbuser usr, dbuser_extra usrx " +
            " WHERE usr.userId = usrx.user_id  " +
            " AND usertype <> 'Guest' " +
            " ORDER BY username ";
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
    //            " AND pm.visible_to  = 'ALL' ";
    String sqlMemberPhotosGroupBy =
            " GROUP BY usr.userid " +
                    " ORDER BY usr.userid ASC ";
    String[] arrColumnsMemberAlbums = {"id", "title", "description", "album_visible_to", "category_id", "user_id"
            , "username", "name", "surname", "resident", "date_joined", "member_since", "avatar_path"
    };
    String sqlMemberOfAlbums = "SELECT a.id, a.title, a.description, a.album_visible_to, a.category_id " +
            " , a.user_id " +
            " , usr.username, usr.name, usr.surname, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined " +
            " , DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
            " , usr.avatar_path " +
            " FROM dbuser usr, photo_album a " +
            " WHERE a.user_id = usr.userId " +
            " AND a.album_visible_to = 'ALL' ";
    String sqlMemberOfAlbumsOrderBy = " ORDER BY a.date_inserted DESC, a.title ASC ";
    String[] arrAlbumCategoriesColumns = {"id", "cat_title", "cat_description_min"};
    String sqlAlbumCategories = "SELECT id, cat_title, cat_description_min " +
            " FROM photo_album_categories pc " +
            " WHERE 1=1 " +
            " ORDER BY cat_title ASC";
    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";
    private VerticalLayout verticalLayout;
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String forMemberName;
    private RecordService recordService;
    private ShareService shareService;
    private ShareMetricService shareMetricService;
    private WeatherService weatherService;
    private PhotoRatingService photoRatingService;
    private PhotoViewService photoViewService;
    private String strHeader;

//    private String[] arrColumnMemberGalleryCount = {"countOfMemberPhotos"
//            , "username", "surname", "name", "resident", "date_joined", "avatar_path"};
//
//    private String sqlMemberGalleryCount =
//            " SELECT COUNT(pm.id) AS countOfMemberPhotos, " +
//                    "  usr.userId, usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
//                    " FROM dbuser usr, photo_meta pm " +
//                    " WHERE pm.uploaderId = usr.userId AND pm.visible_to = 'ALL' ";

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
    private String dirChar = FileSystems.getDefault().getSeparator();
    private String publicIp;
    private String strPath;
    private String hostname;
    private String hostAddress;
    private String canonicalHostname;
    private int intUserId;
    private String[] arrColumnMemberGallery = {"id", "name_new", "title", "subtitle", "photo_type", "uploader", "creator", "visible_to", "meta_date", "photo_date", "photo_time_shot"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso", "meta_aperture", "meta_shutter_speed", "meta_orientation"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon", "date_inserted"
            , "genre_id", "destination_id", "subject_id", "user_rights_id"
            , "city_name"
            , "subject_name", "subject_description", "subject_type"
            , "username", "surname", "name", "resident", "date_joined", "avatar_path"
    };
    private String sqlMemberGallery =
            " SELECT pm.id, pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.creator, pm.visible_to,  DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, " +
                    "DATE_FORMAT(pm.meta_date, '%M %Y') AS photo_date, DATE_FORMAT(pm.meta_date, '%d/%m/%Y - %H:%i:%S') AS photo_time_shot,  pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model,  pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed, meta_orientation  , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
                    " , DATE_FORMAT(pm.date_inserted, '%d/%m/%Y - %H:%i:%S') AS date_inserted " +
                    " , pm.genre_id, pm.destination_id, pm.subject_id, user_rights_id " +
                    " , usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
                    " FROM dbuser usr, photo_meta pm " +
                    " WHERE pm.uploaderId = usr.userId AND pm.visible_to = 'ALL' ";
    private String sqlMemberGalleryOrderBy = " ORDER BY pm.date_inserted DESC  ";
    private String strUrlRequestToBeLogged;
    private String section = SECTION_MEMBERS;
    private String strMember;
    private String[] arrClubsColumnNames = {"org_name", "org_type", "org_type_parent", "city", "used_for", "country", "url", "url_local_events", "url_fb", "url_yt", "url_insta",
            "url_flickr", "url_wikipedia"};
    private String sqlShowClubsSelect = "SELECT id, org_name, org_type, org_type_parent , city , used_for , country , url , city, address, pc, country, map_x, map_y, url, " +
            " url_local_events, url_fb, url_yt, url_insta, url_flickr, url_wikipedia, " +
            " date_inserted, dateUpdated " +
            " FROM organizations o ";
    private String sqlShowClubsWhere = " WHERE o.org_type LIKE 'Club' ";
    private String sqlShowClubsOrder = " ORDER BY o.city ASC, o.org_name ASC";
    private String[] arrColumnNamesGallery = {"name_new", "title", "subtitle", "photo_type", "uploader", "city_name", "meta_date"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon"
            , "date_inserted"};
    private String sqlReadGallery = "SELECT pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, d.city_name, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, " +
            " pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model, " +
            " pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, " +
            "  pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon" +
            " , DATE_FORMAT(pm.date_inserted, '%d/%m/%Y - %H:%i:%S') AS date_inserted " +
            //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
            " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id ";
    private UtilsDate utilsDate;
    private String sessionDateTime;

    private String strOS;
    private String strBrowser;
    private GenericView genericView;
    private EmailSendService emailSendService;
    private LearningService learningService;
    private TutorService tutorService;

    private ListBox<String> listBoxAlbums;

    public MemberPhotosView(RecordService recordService, EmailSendService emailSendService, ShareService shareService,
                            ShareMetricService shareMetricService, WeatherService weatherService, PhotoRatingService photoRatingService,
                            PhotoViewService photoViewService, LearningService learningService, TutorService tutorService) {
        this.recordService = recordService;
        this.emailSendService = emailSendService;
        this.shareService = shareService;
        this.shareMetricService = shareMetricService;
        this.weatherService = weatherService;
        this.photoRatingService = photoRatingService;
        this.photoViewService = photoViewService;
        this.learningService = learningService;
        this.tutorService = tutorService;

        utilsDate = new UtilsDate();
        genericView = new GenericView(recordService);

        constructUI();

    }


    @Override
    public String getPageTitle() {
        return strHeader;
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


        verticalLayout.add(loadHeader("My Photos", "Manage my photos", ""));


        String sqlMembers = sqlMemberCountPhotos + " " + sqlMemberPhotosGroupBy;
        //       verticalLayout.add(getMembersPanels(sqlMembers, arrColumnsMemberPhotos, false));

        String usrName = genericView.checkIfAuthUserName();
        strMember = usrName;
        String strMemberId = genericView.checkIfAuthMemberId();
        String sqlMemberMe = sqlMember + " AND usr.username = '" + strMember + "' ";
        String sqlGallery = sqlMemberGallery + "  AND usr.username = '" + strMember + "' ";

        sqlMemberOfAlbums = sqlMemberOfAlbums + "  AND usr.username = '" + strMember + "' ";

//        sqlMemberGallery = "( " + sqlMemberGallery1 + "  AND usr.username = '" + strMember + "' " + sqlMemberGallery1OrderBy +
//                ") UNION (" + sqlMemberGallery2 + "  AND usr.username = '" + strMember + "' " + sqlMemberGallery2OrderBy + " ) ";

        Div layoutMemberNAlbums = new Div();
        layoutMemberNAlbums.addClassNames(
                Display.FLEX, FlexDirection.COLUMN,
                FlexDirection.Breakpoint.Medium.ROW, Gap.MEDIUM,

                AlignItems.CENTER, JustifyContent.EVENLY,
                Margin.MEDIUM, Padding.LARGE,
                Width.FULL);

        int intMemberId = Integer.parseInt(strMemberId);

        if(intMemberId==44){
            layoutMemberNAlbums.add(loadLearningsPanel(intMemberId));
            layoutMemberNAlbums.addClassNames(Height.FULL);
        }else{
            layoutMemberNAlbums.add(loadMemberInfo(sqlMemberMe, arrColumnsMember, false));
//        layoutMemberNAlbums.add(loadLearningsPanel(sqlMemberOfAlbums, arrColumnsMemberAlbums, strMemberId));
        }

        verticalLayout.add(layoutMemberNAlbums);

        verticalLayout.add(loadMemberPhotos(sqlGallery, arrColumnMemberGallery, intUserId, strMember));


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
//        addClassNames("upload-view");
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

        this.setWidthFull();

    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSection) {

        this.strHeader = strHeader;

        VerticalLayout headerContainer = new VerticalLayout();
        if (isMobile) {
            headerContainer.addClassNames(
                    AlignItems.START, JustifyContent.BETWEEN,
                    Overflow.HIDDEN,// Width.FULL,
                    Margin.NONE,
                    Padding.SMALL,
                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        } else {
            headerContainer.addClassNames(
                    AlignItems.START, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, //Width.FULL,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }
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


        Div divLine = new Div();
        divLine.addClassNames(Border.BOTTOM, Width.FULL);

        headerContainer.add(header, subheader, divLine, headerSection);

        return headerContainer;
    }

    private VerticalLayout loadMemberPhotos(String sqlMemberPhotos, String[] arrColMemberPhotos, int intUserId, String strMember) {

        VerticalLayout layoutMemberPhotos = new VerticalLayout();
        layoutMemberPhotos.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);

//        Select<String> cmbCount = new Select<>();
//        cmbCount.setLabel("Last Uploaded");
//        cmbCount.setItems("0", "4", "10", "20", "30");
//        cmbCount.setValue("10");

        int intRecordsPerPage = 10;

        String sqlMemberGalleryCount = sqlMemberCountPhotos + "  AND usr.username = '" + strMember + "' " + sqlMemberPhotosGroupBy;
        List<Record> lstPhotoCount = getRecordsFromDb(sqlMemberGalleryCount, arrColumnsMemberCountPhotos); //getRecordsFromDb(sqlRead, arrColumnsLearning);
        String strMemberPhotosCount = "0";
        if (!lstPhotoCount.isEmpty()) {
            strMemberPhotosCount = lstPhotoCount.get(0).getColumnData("photo_count");
        }
        int intMemberPhotosCount = Integer.parseInt(strMemberPhotosCount);

        int intPagesTotal = (int) Math.ceil((double) intMemberPhotosCount / intRecordsPerPage);

        Div divGallery = new Div();
        divGallery.addClassName("member-gallery");

        Div divPage = new Div();
        Div divSeparate = new Div("/");
        Div divTotalPages = new Div(intPagesTotal + "");

        Button btnFirst = new Button("Last uploaded");
        Button btnPrevious = new Button("Previous");
        Button btnNext = new Button("Next");
        Button btnLast = new Button("First Uploaded");

        if (intPagesTotal == 1) {
            btnNext.setEnabled(false);
        }

        btnFirst.addClickListener(event -> {
            int intPageToGo = 1;
            int intOffset = 0;

            String sqlPhotosFromMembers = sqlMemberPhotos + sqlMemberGalleryOrderBy + " LIMIT " + intRecordsPerPage + " OFFSET  " + intOffset + " ";
            List<Record> lstPhotoRecords = getRecordsFromDb(sqlPhotosFromMembers, arrColMemberPhotos); //getRecordsFromDb(sqlRead, arrColumnsLearning);
            divGallery.removeAll();

            for (int r = 0; r < lstPhotoRecords.size(); r++) {
                Record rec = lstPhotoRecords.get(r);
                String strId = rec.getColumnData("id");

                String strPath = DIR_PHOTOS_SERVER + dirChar + subPathSmall;
                divGallery.add(getImagePanelFromDb(rec, strPath, intUserId, strMember));
            }

            btnPrevious.setEnabled(false);
            if (divTotalPages.getText().equalsIgnoreCase("1") || divTotalPages.getText().equalsIgnoreCase("0")) {
                btnNext.setEnabled(false);
            } else {
                btnNext.setEnabled(true);
            }
            divPage.setText(intPageToGo + "");
        });


        btnPrevious.addClickListener(event -> {
            String strPage = divPage.getText();
            int intPage = Integer.parseInt(strPage);
            int intPageToGo = intPage - 1;
            int intOffset = (intPageToGo - 1) * intRecordsPerPage;
            String sqlPhotosFromMembers = sqlMemberPhotos + sqlMemberGalleryOrderBy + " LIMIT " + intRecordsPerPage + " OFFSET  " + intOffset + " ";
            List<Record> lstPhotoRecords = getRecordsFromDb(sqlPhotosFromMembers, arrColMemberPhotos); //getRecordsFromDb(sqlRead, arrColumnsLearning);
            divGallery.removeAll();

            for (int r = 0; r < lstPhotoRecords.size(); r++) {
                Record rec = lstPhotoRecords.get(r);
                String strId = rec.getColumnData("id");

                String strPath = DIR_PHOTOS_SERVER + dirChar + subPathSmall;
                divGallery.add(getImagePanelFromDb(rec, strPath, intUserId, strMember));
            }


            if (intPageToGo == 1) {
                event.getSource().setEnabled(false);
            } else {
                event.getSource().setEnabled(true);
            }

            if (intPageToGo > intPagesTotal) {
                btnNext.setEnabled(false);
            } else {
                btnNext.setEnabled(true);
            }

            divPage.setText(intPageToGo + "");
        });

        btnNext.addClickListener(event -> {
            String strPage = divPage.getText();
            int intPage = Integer.parseInt(strPage);

            int intOffset = intPage * intRecordsPerPage;
            String sqlPhotosFromMembers = sqlMemberPhotos + sqlMemberGalleryOrderBy + " LIMIT " + intRecordsPerPage + " OFFSET  " + intOffset + " ";
            List<Record> lstPhotoRecords = getRecordsFromDb(sqlPhotosFromMembers, arrColMemberPhotos); //getRecordsFromDb(sqlRead, arrColumnsLearning);
            divGallery.removeAll();

            for (int r = 0; r < lstPhotoRecords.size(); r++) {
                Record rec = lstPhotoRecords.get(r);
                String strId = rec.getColumnData("id");

                String strPath = DIR_PHOTOS_SERVER + dirChar + subPathSmall;
                divGallery.add(getImagePanelFromDb(rec, strPath, intUserId, strMember));
            }

            int intPageToGo = intPage + 1;
            if (intPageToGo < 1) {
                btnPrevious.setEnabled(false);
            } else {
                btnPrevious.setEnabled(true);
            }

            if (intPageToGo > intPagesTotal) {
                event.getSource().setEnabled(false);
            } else {
                event.getSource().setEnabled(true);
            }
            divPage.setText(intPageToGo + "");
        });


        btnLast.addClickListener(event -> {

            String strPage = (intPagesTotal - 1) + "";
            int intPage = Integer.parseInt(strPage);

            int intOffset = intPage * intRecordsPerPage;
            String sqlPhotosFromMembers = sqlMemberPhotos + sqlMemberGalleryOrderBy + " LIMIT " + intRecordsPerPage + " OFFSET  " + intOffset + " ";
            List<Record> lstPhotoRecords = getRecordsFromDb(sqlPhotosFromMembers, arrColMemberPhotos); //getRecordsFromDb(sqlRead, arrColumnsLearning);
            divGallery.removeAll();

            for (int r = 0; r < lstPhotoRecords.size(); r++) {
                Record rec = lstPhotoRecords.get(r);
                String strId = rec.getColumnData("id");

                String strPath = DIR_PHOTOS_SERVER + dirChar + subPathSmall;
                divGallery.add(getImagePanelFromDb(rec, strPath, intUserId, strMember));
            }
            if (intPage < 1) {
                btnPrevious.setEnabled(false);
            } else {
                btnPrevious.setEnabled(true);
            }

            btnNext.setEnabled(false);
            divPage.setText(intPagesTotal + "");
        });

        String sqlPhotos = sqlMemberPhotos + sqlMemberGalleryOrderBy + " LIMIT " + intRecordsPerPage + " OFFSET  0 ";
        // String sqlPhotos = sqlMemberPhotos + sqlMemberGalleryOrderBy + " LIMIT 10 ";
        divPage.setText("1");
        btnPrevious.setEnabled(false);

        List<Record> lstRecords = getRecordsFromDb(sqlPhotos, arrColMemberPhotos); //getRecordsFromDb(sqlRead, arrColumnsLearning);

        for (int r = 0; r < lstRecords.size(); r++) {
            Record rec = lstRecords.get(r);
            String strId = rec.getColumnData("id");


            String strPath = DIR_PHOTOS_SERVER + dirChar + subPathSmall;
            divGallery.add(getImagePanelFromDb(rec, strPath, intUserId, strMember));
        }

        Div divEdit = new Div("Edit last uploaded photos");
        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);
        layoutButtons.add(btnFirst, btnPrevious, divPage, divSeparate, divTotalPages, btnNext, btnLast);

        layoutMemberPhotos.add(divEdit, layoutButtons, divGallery);
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
            // linkTutorYt.getStyle().setColor(strColorExternalweb);
            // linkTutorYt.setClassName("lazy-result-line-button");

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
//            Image imgAvatar = getAvatarImage(strAvatarPath, strNameOfUser, "120px", "120px");

            H3 objName = new H3(strName + " " + strSurname);
            objName.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);
            H4 objMember = new H4(strMember);
            objMember.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);
//            Div divMemberSince = new Div("Member since " + strMemberSince);
//            divMemberSince.addClassNames(TextColor.SECONDARY, FontWeight.MEDIUM);

            HorizontalLayout layoutAll = new HorizontalLayout();


            VerticalLayout layoutMemberCard = new VerticalLayout();
            layoutMemberCard.getStyle().setBorderRadius("30px");
            layoutMemberCard.getStyle().setMaxWidth("300px");
//            layoutMemberCard.getStyle().set("border", "lightgrey 1px solid");
            layoutMemberCard.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER);
            layoutMemberCard.add(imgAvatar, objName, objMember);

            layoutAll.add(layoutMemberCard, layoutMemberLinks);

//            Div divResident = new Div("Lives at " + strResident);

            layoutMember.add(layoutAll); //, divBioTitle, divBio, divResident);
        } else {
            logger.warn(" lstRecords is more than one record");
        }

        return layoutMember;
    }

    private VerticalLayout loadLearningsPanel(int intMemberId){

        VerticalLayout layoutLearnings = new VerticalLayout();
        layoutLearnings.addClassNames(Width.FULL, Height.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.NONE, Margin.NONE);

        Button btnNew = new Button("Create News");
        Button btnNewTutor = new Button("Create Tutor");

        HorizontalLayout layoutLearningActions = new HorizontalLayout(btnNew, btnNewTutor);
        layoutLearningActions.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);

        String strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;
        MemberLearningsGrid learningsGrid = new MemberLearningsGrid(intMemberId, learningService,tutorService,strPath);
        learningsGrid.setMinHeight("600px");
        learningsGrid.setWidthFull();
        learningsGrid.setHeightFull();
        layoutLearnings.add(layoutLearningActions,learningsGrid);

        btnNew.addClickListener( e-> {
            new LearningDialog(learningService, tutorService, intMemberId, saved -> learningsGrid.refresh(intMemberId)).open();
        });

        btnNewTutor.addClickListener( e-> {
            new TutorDialog(tutorService, saved -> { }).open();
        });
       return  layoutLearnings;
    }


    private GalleryImageViewCard getImagePanelFromDb(Record record, String strPath, int intUserId, String strMember) {


        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");

        String strCity = record.getColumnData("city_name");
        String strSubject = record.getColumnData("subject_name");
        String strUploader = record.getColumnData("uploader");

        int isType = 2;

        logger.info(" Photo:" + strFileName + " Member Gallery -> city and subject:'" + strCity + "'_'" + strSubject + "'");


        String strImagePath = strPath + dirChar + strFileName;
        logger.info(" strImagePath " + strImagePath);

        String sqlMemberPhotosOrderby = " ORDER BY pm.date_inserted DESC ";
        boolean isEditable = true;

        GalleryImageViewCard imageGalleryViewCard = new GalleryImageViewCard(record, strImagePath, isMobile, intUserId, strMember, sessionCreation, hostname, publicIp, isEditable,
                recordService, isType, sqlReadGallery, sqlMemberPhotosOrderby, arrColumnNamesGallery, shareService, shareMetricService, weatherService, photoRatingService, photoViewService);

        imageGalleryViewCard.addClassName("image-to-show");
        imageGalleryViewCard.getStyle().setOpacity("1");
        return imageGalleryViewCard;
    }

    private VerticalLayout loadAlbumsPanel(String sqlMemberOfAlbums, String[] arrColumnsMemberAlbums, String strMemberId) {

        String sqlMemberAlbums = sqlMemberOfAlbums + sqlMemberOfAlbumsOrderBy;

        List<Record> lstAlbums = getRecordsFromDb(sqlMemberAlbums, arrColumnsMemberAlbums);
        List<String> lstAlbumTitle = new ArrayList<>();
        List<String> lstAlbumId = new ArrayList<>();
        for (int i = 0; i < lstAlbums.size(); i++) {
            lstAlbumTitle.add(lstAlbums.get(i).getColumnData("title"));
            lstAlbumId.add(lstAlbums.get(i).getColumnData("id"));
        }

        VerticalLayout layoutAlbumsPanel = new VerticalLayout();
        layoutAlbumsPanel.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Background.CONTRAST_5, BorderRadius.LARGE);
        layoutAlbumsPanel.setMinWidth("380px");
        layoutAlbumsPanel.setMaxWidth("420px");
        layoutAlbumsPanel.setMaxHeight("600px");

        Div divAlbumsCaption = new Div("Albums");
        divAlbumsCaption.addClassNames(TextAlignment.CENTER);

        HorizontalLayout layoutControls = new HorizontalLayout();
        layoutControls.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);


        listBoxAlbums.addClassNames(Background.BASE, BorderRadius.SMALL, Height.FULL, Overflow.SCROLL);
        listBoxAlbums.setWidthFull();
        listBoxAlbums.setHeightFull();
        listBoxAlbums.setMinHeight("250px");
        listBoxAlbums.setItems(lstAlbumTitle);
        if (!lstAlbumTitle.isEmpty()) {
            listBoxAlbums.setValue(lstAlbumTitle.get(0));
        }

        Button btnCreate = new Button("New");
        btnCreate.setIcon(VaadinIcon.PLUS.create());
        btnCreate.addClickListener(event -> {
            Dialog dlg = loadAlbumPropertiesDialog(sqlAlbumCategories, arrAlbumCategoriesColumns, "New Album", sqlMemberOfAlbums, arrColumnsMemberAlbums,
                    null, strMemberId);
            dlg.open();
        });

        Button btnEdit = new Button("Edit");
        btnEdit.setIcon(VaadinIcon.EDIT.create());
        btnEdit.addClickListener(event -> {
            String strAlbumId = "";
            for (int i = 0; i < lstAlbumTitle.size(); i++) {
                if (lstAlbumTitle.get(i).equalsIgnoreCase(listBoxAlbums.getValue())) {
                    strAlbumId = lstAlbumId.get(i);
                }
            }

            Dialog dlg = loadAlbumPropertiesDialog(sqlAlbumCategories, arrAlbumCategoriesColumns, "Edit Album", sqlMemberOfAlbums, arrColumnsMemberAlbums,
                    strAlbumId, strMemberId);
            dlg.open();
        });

        Button btnDelete = new Button("Delete");
        btnDelete.setIcon(VaadinIcon.MINUS.create());
        btnDelete.addClickListener(delete -> {

            String strAlbumId = "";
            for (int i = 0; i < lstAlbumTitle.size(); i++) {
                if (lstAlbumTitle.get(i).equalsIgnoreCase(listBoxAlbums.getValue())) {
                    strAlbumId = lstAlbumId.get(i);
                }
            }

            if (strAlbumId != null) {
                String sqlAlbumInfo = "";

                if (!strAlbumId.isEmpty()) {
                    sqlAlbumInfo = sqlMemberOfAlbums + " AND id = '" + strAlbumId + "' AND a.user_id = '" + strMemberId + "' ";
                } else {
                    sqlAlbumInfo = sqlMemberOfAlbums + " AND title = '" + listBoxAlbums.getValue() + "' AND a.user_id = '" + strMemberId + "' ";
                }

                List<Record> lstAlbum = getRecordsFromDb(sqlAlbumInfo, arrColumnsMemberAlbums);
                strAlbumId = lstAlbum.get(0).getColumnData("id");
            }

            deleteAlbumInfo(strAlbumId, strMemberId);
        });

        layoutControls.add(btnCreate, btnEdit, btnDelete);
        layoutAlbumsPanel.add(divAlbumsCaption, layoutControls, listBoxAlbums);

        return layoutAlbumsPanel;
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

    private void deleteAlbumInfo(String strAlbumId, String strMemberId) {

        String[] field = {"photo_count"};
        String sqlCountPhotosOfTheAlbum = "SELECT a.id, a.title AS album_title, a.user_id, a. album_visible_to, a.description " +
                " " +
                " FROM photo_album_photo pap, photo_album a " +
                " WHERE pap.photo_album_id = a.id AND pap.user_id = a.user_id AND a.user_id = " + strMemberId + " AND a.id = " + strAlbumId +
                " ORDER BY a.title ";
        List<Record> lstCountPhotoInAlbum = getRecordsFromDb(sqlCountPhotosOfTheAlbum, field);

        if (!lstCountPhotoInAlbum.isEmpty()) {

            String messageUp = "Album has attached photos! Please remove all photos from album.";
            Notification notificationUp = Notification.show(messageUp, 5000, Notification.Position.MIDDLE);
            notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);

        } else {
            String sqlDeleteAlbum = "DELETE FROM photo_album WHERE id='" + strAlbumId + "' AND user_id = '" + strMemberId + "'";

            if (recordService.insertOneRecordWithQuery(sqlDeleteAlbum, null, null) == 1) {
                String messageUp = "Album Deleted !";
                Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.MIDDLE);
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

                reUpdateMyAlbumCounts(Integer.parseInt(strMemberId));
            } else {
                String messageUp = "Album Not Deleted !";
                Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.MIDDLE);
                notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }

    private boolean saveAlbumInfo(String strAlbumTitle, String strAlbumDescription, Select<String> cmbAlbumType, List<String> lstAlbumTypes, List<String> lstAlbumTypeIds,
                                  String strAlbumId, String strMemberId) {

        if (strAlbumTitle.isEmpty()) {
            String messageUp = "Album needs to have a title! Please type one.";
            Notification notificationUp = Notification.show(messageUp, 5000, Notification.Position.MIDDLE);
            notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (cmbAlbumType == null || cmbAlbumType.getValue() == null || cmbAlbumType.getValue().isEmpty()) {
            String messageUp = "Album needs to be assigned a category! Please select one.";
            Notification notificationUp = Notification.show(messageUp, 5000, Notification.Position.MIDDLE);
            notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        String sqlMemberWithAlbums = sqlMemberOfAlbums + " AND id <> '" + strAlbumId + "' AND user_id = '" + strMemberId + "' AND title = '" + strAlbumTitle + "' ";
        if (recordService.findAll(sqlMemberWithAlbums, arrColumnsMemberAlbums).size() == 1) {

            String messageUp = "Album title already exists! Please type a different one.";
            Notification notificationUp = Notification.show(messageUp, 5000, Notification.Position.MIDDLE);
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
                    StringBuilder strInsert = new StringBuilder("INSERT INTO photo_album (");
                    StringBuilder placeholders = new StringBuilder("(");
                    Object[] fieldValue = new Object[4];
                    String[] fieldValueType = new String[4];
                    boolean first = true;

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
                    }

                    if (strAlbumDescription != null) {
                        if (!first) {
                            strInsert.append(", ");
                            placeholders.append(", ");
                        }
                        strInsert.append("description");
                        placeholders.append("?");
                        fieldValue[1] = strAlbumDescription;
                        fieldValueType[1] = "java.lang.String";
                        first = false;
                    }

                    if (strAlbumTypeId != null) {
                        if (!first) {
                            strInsert.append(", ");
                            placeholders.append(", ");
                        }
                        strInsert.append("category_id");
                        placeholders.append("?");
                        fieldValue[2] = strAlbumTypeId;
                        fieldValueType[2] = "java.lang.Integer";
                        first = false;
                    }

                    // user_id is typically required
                    if (!first) {
                        strInsert.append(", ");
                        placeholders.append(", ");
                    }
                    strInsert.append("user_id");
                    placeholders.append("?");
                    fieldValue[3] = strMemberId;
                    fieldValueType[3] = "java.lang.Integer";

                    strInsert.append(", album_visible_to ");
                    placeholders.append(", 'ALL' ");

                    strInsert.append(") VALUES ");
                    placeholders.append(")");
                    strInsert.append(placeholders);

                    if (recordService.insertOneRecordWithQuery(strInsert.toString(), fieldValue, fieldValueType) == 1) {
                        String messageUp = "Album Created !";
                        Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.MIDDLE);
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

                        reUpdateMyAlbumCounts(Integer.parseInt(strMemberId));
                    } else {
                        String messageUp = "Album Not Created !";
                        Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.MIDDLE);
                        notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
            } else if (strAlbumId != null && !strMemberId.isEmpty()) {

                StringBuilder strUpdate = new StringBuilder("UPDATE photo_album SET ");
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
                    String messageUp = "Album Info Updated !";
                    Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.MIDDLE);
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

                    reUpdateMyAlbumCounts(Integer.parseInt(strMemberId));

                } else {

                    String messageUp = "Album Info Not Updated !";
                    Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.MIDDLE);
                    notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
            return true;
        }

    }

    private int reUpdateMyAlbumCounts(int intUserId) {

        Object[] fieldValueCount = {intUserId};
        String[] fieldTypeCount = {"java.lang.Integer"};

        String strUpdateCount = "UPDATE dbuser_extra AS d " +
                " JOIN ( " +
                "    SELECT user_id, COUNT(*) AS album_count " +
                "    FROM photo_album " +
                "    WHERE album_visible_to = 'ALL' " +
                "    GROUP BY user_id " +
                " ) AS p ON d.user_id = p.user_id " +
                " SET d.username = NULL , d.count_stories = p.album_count " +
                " WHERE d.user_id = ? ";
        return recordService.insertOneRecordWithQuery(strUpdateCount, fieldValueCount, fieldTypeCount);

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
//            Image imgAvatar = getAvatarImage(strAvatarPath, strNameOfUser, "120px", "120px");

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

    private Dialog loadAlbumPropertiesDialog(String sqlAlbumCategories, String[] arrAlbumCategoriesColumns, String strTitle,
                                             String sqlAlbum, String[] arrAlbumColumns,
                                             String strAlbumId, String strMemberId) {
        Dialog dlg = new Dialog();
        dlg.setMinWidth("340px");
        dlg.setDraggable(true);
        dlg.setResizable(false);
        dlg.setCloseOnEsc(false);
        dlg.setCloseOnOutsideClick(false);

        String strAlbumTitle = "";
        String strAlbumDescription = "";
        String strAlbumCategoryId = "";


        if (strAlbumId != null) {
            String sqlAlbumInfo = "";

            if (!strAlbumId.isEmpty()) {
                sqlAlbumInfo = sqlAlbum + " AND id = '" + strAlbumId + "' AND a.user_id = '" + strMemberId + "' ";
            } else {
                sqlAlbumInfo = sqlAlbum + " AND title = '" + listBoxAlbums.getValue() + "' AND a.user_id = '" + strMemberId + "' ";
            }

            List<Record> lstAlbum = getRecordsFromDb(sqlAlbumInfo, arrAlbumColumns);

            strAlbumId = lstAlbum.get(0).getColumnData("id");
            strAlbumTitle = lstAlbum.get(0).getColumnData("title");
            strAlbumDescription = lstAlbum.get(0).getColumnData("description");
            strAlbumCategoryId = lstAlbum.get(0).getColumnData("category_id");
        }

        List<Record> lstAlbumCategories = getRecordsFromDb(sqlAlbumCategories, arrAlbumCategoriesColumns);
        List<String> lstAlbumCategoryTitle = new ArrayList<String>();
        List<String> lstAlbumCategoryId = new ArrayList<String>();
        for (int i = 0; i < lstAlbumCategories.size(); i++) {
            lstAlbumCategoryTitle.add(lstAlbumCategories.get(i).getColumnData("cat_title"));
            lstAlbumCategoryId.add(lstAlbumCategories.get(i).getColumnData("id"));
        }

        VerticalLayout layoutAlbumInfo = new VerticalLayout();
        layoutAlbumInfo.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.MEDIUM);

        Div divTitleCaption = new Div("Album Properties");
        divTitleCaption.setText(strTitle);

        TextField txtAlbumTitle = new TextField("Title");
        txtAlbumTitle.setRequiredIndicatorVisible(true);
        txtAlbumTitle.setMaxLength(70);
        txtAlbumTitle.setWidthFull();
        if (strAlbumTitle != null) {
            txtAlbumTitle.setValue(strAlbumTitle);
        }
        TextArea txtAlbumDescription = new TextArea("Description");
        txtAlbumDescription.setMaxLength(140);
        txtAlbumDescription.setWidthFull();
        txtAlbumDescription.setValue(strAlbumDescription);
        txtAlbumDescription.setMinRows(4);


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

        Button btnOk = new Button("Ok");
        btnOk.setIcon(FontAwesome.Regular.CHECK_SQUARE.create());
        final String strAlbumIdFinal = strAlbumId;
        btnOk.addClickListener(ok ->
        {
            if (saveAlbumInfo(txtAlbumTitle.getValue(), txtAlbumDescription.getValue(), selAlbumCategory, lstAlbumCategoryTitle, lstAlbumCategoryId,
                    strAlbumIdFinal, strMemberId)) {
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


        layoutAlbumInfo.add(divTitleCaption, txtAlbumTitle, txtAlbumDescription, selAlbumCategory, layoutButtons);
        dlg.add(layoutAlbumInfo);

        return dlg;
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
