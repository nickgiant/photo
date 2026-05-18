package com.photo.act.photo_act.views;

import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flowingcode.vaadin.addons.carousel.Carousel;
import com.flowingcode.vaadin.addons.carousel.Slide;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.ResponsiveBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.HorizontalAlign;
import com.github.appreciated.apexcharts.helper.Series;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailSendService;
import com.photo.act.photo_act.services.PhotoFlickrService;
import com.photo.act.photo_act.services.PhotoRatingService;
import com.photo.act.photo_act.services.PhotoStatisticsService;
import com.photo.act.photo_act.services.PhotoViewService;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.*;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.taefi.component.ToggleButtonGroup;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static com.photo.act.photo_act.views.MainLayout.*;







@AnonymousAllowed

@Route(value = "") //":category?")
//@RouteAlias(value = "home") // empty on homepage
@RouteAlias(value = "home/:category?", layout = MainLayout.class)
//@Route(value = "learnings") //":category?")
//@RouteAlias(value = "learnings/category/:category?", layout = MainLayout.class)
//@RouteAlias(value = "learnings/tutor/:tutor?", layout = MainLayout.class)
//@RouteAlias(value = "learnings/category/:category/tutor/:tutor?", layout = MainLayout.class)

//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
//@EnableGoogleAnalytics(value="G-NQH7NZ6JJL", devLogging = EnableGoogleAnalytics.LogLevel.NONE, sendMode = EnableGoogleAnalytics.SendMode.ALWAYS)
public class HomeView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";

    private static final Logger logger = LoggerFactory.getLogger(HomeView.class);
    private VerticalLayout verticalLayout;
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String section = SECTION_HOME;
    //    private String forMemberName;
    private RecordService recordService;
    private String strHeader;

    private String category;

    private String dirChar = FileSystems.getDefault().getSeparator();


    public static String STR_ALL_CATEGORIES = "all-categories";

    public static final String subPathThumbs = "photo-thumbs";
    public static final String subPathSmall = "photo-small";
    public static final String subPathMedium = "photo-medium";
    public static final String subPathLarge = "photo-large";
    public static final String subPathUpload = "photo-upload";
    public static final String subPathShow = "photo-show";

    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";


    private String publicIp;
    private String strPath;
    private String hostname;
    private String hostAddress;
    private String canonicalHostname;

    private int userId;
    private String strUsername;

    private String strColorExternalweb = "#9fafd5";


    String[] arrColLearningTopics = {"cat_title", "cat_title_type", "cat_type", "cat_location_count", "cat_title_count", "cat_description_min", "cat_description_big"};

    String sqlLearningTopics =
            " SELECT l.id, lc.id, lc.cat_title, lc.cat_title_type, lc.cat_type, cat_description_min, cat_description_big, " +
                    " count(l.has_location) AS cat_location_count , count(lc.cat_title) AS cat_title_count, lc.cat_order " +
                    " FROM learnings l LEFT JOIN learnings_categories lc ON l.category_id = lc.id " +
                    " WHERE 1 = 1 " +
                    " AND lc.cat_type not LIKE '%genre%' " +
                    " AND lc.cat_type not LIKE 'not show' " +
                    " GROUP BY lc.cat_type " +
                    " ORDER BY lc.cat_order ASC " +
                    " LIMIT 6 ";

    String[] arrColLearningGenres = {"cat_title", "cat_title_type", "cat_type", "cat_location_count", "cat_title_count", "cat_description_min", "cat_description_big"};

    String sqlLearningGenres = //f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description  " +
            " SELECT lc.id, lc.cat_title, lc.cat_title_type, lc.cat_type, l.cat_genre_id, count(l.has_location) AS cat_location_count, count(lc.cat_title) AS cat_title_count, cat_description_min, cat_description_big " +

                    " FROM learnings l LEFT JOIN learnings_categories lc ON l.cat_genre_id = lc.id " +
                    " WHERE 1 = 1 " +
                    " AND lc.cat_type LIKE '%genre%' " +
                    " GROUP BY lc.cat_title " +
                    " ORDER BY lc.cat_order ASC " +
                    " LIMIT 6 ";


//    String[] arrColLearningTopics = {"cat_title", "cat_title2", "cat_title_type", "cat_title_type2", "cat_type",
//            "cat_description_min", "cat_type_count"};
//
//    String sqlLearningTopics = "SELECT "
//            + " lc.cat_title, lc.cat_title_type, lc.cat_type, lc.cat_description_min "
//            + " , count (l.category_id) AS cat_type_count "
////            + " , lc2.cat_title AS cat_title2, lc2.cat_title_type AS cat_title_type2, lc2.cat_type AS cat_type2, count (lc2.cat_type) AS cat_type_count2 "
////            + " l.id, l.title, l.picture, l.section , l.category, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert "
////            + ", l.tutor_id, l.tutor_id_team, t.tutor_name, t.website, t.url_fb, t.url_yt, t.url_insta, t.url_flickr, t.url_wikipedia, t.url_ref1, t.url_ref2, t.url_ref3, t.city_base, t.country_base, t.userIdInsert, t.username, t.date_inserted "
//            + " FROM learnings l, learnings_categories lc "
////            + " FROM learnings_categories lc "
////            + " WHERE 1 = 1 "
//            + " WHERE lc.id = l.category_id "
//            + " AND lc.cat_type NOT LIKE '%genre%' "
//            + " GROUP BY lc.cat_type "
//            + " ORDER BY lc.cat_order ASC ";


//    String[] arrColLearningGenres = {"cat_title", "cat_title2", "cat_title_type", "cat_type", "cat_description_min", "cat_description_big", "cat_genre_count"};
//
//    String sqlLearningGenres = "SELECT  " //f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description  " +
//            + " lc.cat_title, lc.cat_title_type, lc.cat_type, lc.cat_description_min, count (lc.cat_title) AS cat_genre_count, "
//            + " lc2.cat_title AS cat_title2, lc2.cat_title_type AS cat_title_type2, lc2.cat_type AS cat_type2, count (lc2.cat_title) AS cat_genre_count2 "
    /// /            + " l.id, l.title, l.picture, l.section , l.category, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert "
    /// /            + ", l.tutor_id, l.tutor_id_team, t.tutor_name, t.website, t.url_fb, t.url_yt, t.url_insta, t.url_flickr, t.url_wikipedia, t.url_ref1, t.url_ref2, t.url_ref3, t.city_base, t.country_base, t.userIdInsert, t.username, t.date_inserted "
//            + " FROM learnings_categories lc2 RIGHT JOIN learnings l ON lc2.id = l.category_id2 LEFT JOIN learnings_categories lc ON lc.id = l.category_id "
//            + " WHERE 1 = 1 "
//            + " AND ( lc.cat_type LIKE '%genre%') "
//            + " GROUP BY lc.cat_title ORDER BY lc.cat_order ASC ";

    String sqlLearningsReadOrderBy;

    private UtilsDate utilsDate;
    private String sessionDateTime;
    private String strUrlRequestToBeLogged;
    private GenericView genericView;
    private String strOS;
    private String strBrowser;

    private Div layoutLastPhotos;
    private EmailSendService emailSendService;
    private WeatherService weatherService;
    private ShareService shareService;
    private ShareMetricService shareMetricService;
    private PhotoRatingService photoRatingService;
    private PhotoViewService photoViewService;
    private PhotoStatisticsService photoStatisticsService;

    public HomeView(RecordService recordService, EmailSendService emailSendService, WeatherService weatherService,
                    ShareService shareService, ShareMetricService shareMetricService,
                    PhotoRatingService photoRatingService, PhotoViewService photoViewService,
                    PhotoStatisticsService photoStatisticsService) {
        this.recordService = recordService;
        this.emailSendService = emailSendService;
        this.weatherService = weatherService;
        this.shareService = shareService;
        this.shareMetricService = shareMetricService;
        this.photoRatingService = photoRatingService;
        this.photoViewService = photoViewService;
        this.photoStatisticsService = photoStatisticsService;
        utilsDate = new UtilsDate();
        genericView = new GenericView(recordService);

        constructUI();

    }



    @Override
    public String getPageTitle() {
        strHeader = "photoact.net";
        return strHeader;
    }

    @Override
    public void beforeEnter(@OptionalParameter BeforeEnterEvent event) {
        category = event.getRouteParameters().get("category").orElse(STR_ALL_CATEGORIES);
//        tutor = event.getRouteParameters().get("tutor").orElse(STR_ALL_TUTORS);

        getUserClientInfo();

        userId = 1;
        strUsername = "visitor-user";
        verticalLayout.removeAll();
//        VerticalLayout layoutHeaderParameters = loadHeader("", "", "");
//
//        verticalLayout.add(layoutHeaderParameters);


        String[] arrColumnsLearning = {"title", "subtitle", "picture", "category_id", "cat_genre_id", "format", "url", "tutor_id", "artists_ref", "description", "duration", "pages", "published", "year_published",
                "userId_post", "date_inserted",
                "cat_title", "cat_type", "genre_title",
                "tutor_name",
                "username", "name", "surname"
        };

        // learnings: l.id, l.title, l.picture, l.section , l.category, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert
// learnings_tutor:  lt.id, lt.tutor_name, lt.learnings_team_id, lt.website, lt.url_fb, lt.url_yt, lt.url_insta, lt.url_flickr, lt.url_wikipedia, lt.url_ref1, lt.url_ref2, lt.url_ref3, lt.url_flckr, lt.city_base, lt.country_base, lt.userIdInsert, lt.username, lt.date_inserted
        String sqlLearningsRead = "SELECT "
                + " l.id, l.title,  l.subtitle, l.picture, l.category_id, l.cat_genre_id, l.format, l.url, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, DATE_FORMAT(l.published, '%Y') AS year_published,  l.userId_post, l.dateInsert, getDateDiffFromNow(l.dateInsert) AS date_inserted "
                + " , lc.cat_title,  lc.cat_type, t.tutor_name "
                + " , lcg.cat_title AS genre_title"
                + " , usr.username, usr.name, usr.surname "
                //  + " , l.tutor_id, l.tutor_id_team, l.category_id, l.category_id2, t.tutor_name, t.website, t.url_fb, t.url_yt, t.url_insta, t.url_flickr, t.url_wikipedia, t.url_ref1, t.url_ref2, t.url_ref3, t.city_base, t.country_base, t.userIdInsert, t.username, t.date_inserted "
                + " FROM learnings_categories lc, learnings l LEFT JOIN learnings_categories lcg ON l.cat_genre_id = lcg.id,  tutor t, dbuser usr " //, tutor t  "
                + " WHERE 1 = 1 "
                + " AND lc.id = l.category_id AND l.tutor_id = t.id AND l.userId_post = usr.userId"
                + " ORDER BY l.dateInsert DESC "
                + " LIMIT 3";


        String[] arrColumnNamesGallery = {"name_org", "name_new", "title", "subtitle", "photo_type", "uploader", "uploaderId", "photo_type", "contains",
                "space_size", "space_size_medium", "space_size_thumb", "city_name", "meta_date", "date_inserted",
                "username", "name", "surname", "avatar_path", "member_since"
        };

        String sqlReadGallery = "SELECT pm.name_org, pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.uploaderId, pm.photo_type, pm.contains_tags, " +
                " pm.space_size, pm.space_size_medium, pm.space_size_thumb,  d.city_name, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date " +
                " , getDateDiffFromNow(pm.date_inserted) AS date_inserted " +
                " , usr.username, usr.name, usr.surname, usr.avatar_path, DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
                " FROM dbuser usr, photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id ";
//                    " WHERE pm.hostname like '"+hostname+"' "+
//                    " ORDER BY pm.title ASC ";
        String sqlGalleryAll = sqlReadGallery + " WHERE pm.visible_to = 'ALL' " +
                " AND usr.userId = pm.uploaderId";
        sqlGalleryAll = sqlGalleryAll + " ORDER BY pm.date_inserted DESC, pm.title ASC, pm.meta_date DESC, pm.name_new ASC ";

        String[] arrColsUploadsGrouped = {"Month", "Photos"};

        String sqlUploadsGrouped = "SELECT DATE_FORMAT(pm.date_inserted, '%M %Y') as 'month', DATE_FORMAT(pm.date_inserted, '%V-%Y') as 'week', COUNT(pm.id) AS 'Photos'" +
                " FROM photo_meta pm " +
                " WHERE visible_to LIKE 'all' ";
        String sqlGroupByMonthly = " GROUP BY DATE_FORMAT(pm.date_inserted, '%M %Y') ";
        String sqlGroupByWeekly = " GROUP BY DATE_FORMAT(pm.date_inserted, '%V-%Y') ";
        String sqlUploadsGroupedOrderBy = " ORDER BY DATE_FORMAT(pm.date_inserted, '%Y-%m-%V') DESC LIMIT 10";

        H1 titlePage = new H1(APP_NAME);
        Span subTitle = new Span("[ Through Photography, We Connect and Act ]");

        Header siteHeader = new Header(titlePage, subTitle);
        siteHeader.addClassNames(Width.FULL);

        verticalLayout.add(siteHeader);

        Div divMainImage = new Div();
        divMainImage.setWidthFull();
        divMainImage.setHeight("auto");
        divMainImage.setMaxWidth("47rem");
        divMainImage.setMaxHeight("24rem");
        Image mainImage = new Image();
        String strMainImagePath = DIR_PHOTOS_SERVER + dirChar + "photographerM.jpg";


        Path path = Paths.get(strMainImagePath);
        File file = path.toFile();

        mainImage.setSrc(DownloadHandler.forFile(file));
        mainImage.setAlt("sketch image of a photographer");
        mainImage.setSizeFull();
//        mainImage.setHeight("24rem");
//        mainImage.setWidth("auto");
        mainImage.getStyle().setBorderRadius("20px");
        mainImage.getStyle().setPadding("10px");

        divMainImage.add(mainImage);


        Div div1 = new Div("We are a community site, with members exchanging info and links in order to improve our skills in photography!");
        Div div2 = new Div("Currently, we share info about events and learnings. Of course, we also have space for our photos and albums.");


        Button btnLogin = new Button("Login");
        btnLogin.addClassName("btn-register");
        btnLogin.addClickListener(click ->{
            displayLoginDialog();
        });


        Button btnRegister = new Button("Register");
        btnRegister.addClassName("btn-register");
//        btnSuggestEvent.setIcon(svgComments);
        btnRegister.addClickListener(click -> {
            displayRegisterDialog();
        });


        HorizontalLayout layoutUserBtns = new HorizontalLayout();
        layoutUserBtns.setAlignItems(FlexComponent.Alignment.CENTER);
        layoutUserBtns.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);
        layoutUserBtns.setWrap(true);
        String usrName = genericView.checkIfAuthUserName();
        if (usrName == null) {
            layoutUserBtns.add(btnLogin,btnRegister);
        } else {

            mainImage.setHeight("16rem");
            mainImage.setWidth("auto");
            layoutUserBtns.add(genericView.getAuthUserPanel(usrName));
        }

        verticalLayout.add(divMainImage, div1, div2, layoutUserBtns);

        Div divLearningTopics = loadLearningTopics(sqlLearningTopics, arrColLearningTopics);
        VerticalLayout layoutLearningTopics = new VerticalLayout();
        H2 titleLearnTopics = new H2("Learning Categories");

        HorizontalLayout layoutLearningsActions = new HorizontalLayout();
        layoutLearningsActions.addClassName("view-more");
        Button btnMoreLearnings = new Button("View All Learnings");
        btnMoreLearnings.addClassName("view-more");
        btnMoreLearnings.addClickListener(click -> {
            btnMoreLearnings.getUI().ifPresent(ui ->
                    ui.navigate(LearningsView.class)
            );
        });
        layoutLearningsActions.add(btnMoreLearnings);
        layoutLearningTopics.add(titleLearnTopics, divLearningTopics, layoutLearningsActions);

        layoutLearningTopics.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER, Padding.MEDIUM);
        layoutLearningTopics.addClassName("page-section");
        verticalLayout.add(layoutLearningTopics);


        //Div layoutLearningGenres = loadLearningsAboutGenres(sqlLearningGenres, arrColLearningGenres);

/*        H2 titleLearnGenres = new H2("Learning Photo Genres");

        HorizontalLayout layoutLearningsActionsGenres = new HorizontalLayout();
        layoutLearningsActionsGenres.addClassName("view-more");
        Button btnMoreLearningGenres = new Button("View All Learnings");
        btnMoreLearningGenres.addClickListener(click -> {
            btnMoreLearningGenres.getUI().ifPresent(ui ->
                    ui.navigate(LearningsView.class)
            );
        });
        layoutLearningsActionsGenres.add(btnMoreLearningGenres);
        verticalLayout.add(titleLearnGenres, layoutLearningGenres, layoutLearningsActionsGenres);*/


//        H3 titleCarousel = new H3("10 Recently Uploaded Photos:");
//        verticalLayout.add(titleCarousel, getCarousel(lstImage));



        H2 titleGraphLastPhotos = new H2("Photo Uploads");
        HorizontalLayout layoutFilterUploadsPeriod = new HorizontalLayout();
        layoutFilterUploadsPeriod.addClassName("tab-select");
        VerticalLayout layoutGraph = new VerticalLayout();
        layoutGraph.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.NONE, Margin.NONE);
        layoutGraph.add(loadGraphUploads(sqlUploadsGrouped + sqlGroupByMonthly + sqlUploadsGroupedOrderBy, arrColsUploadsGrouped, "month"));
        verticalLayout.add(titleGraphLastPhotos, layoutFilterUploadsPeriod, layoutGraph);

        HorizontalLayout layoutTabSelectPeriod = new HorizontalLayout();
        layoutTabSelectPeriod.addClassName("tab-select");
        RadioButtonGroup<String> btnGroupSelectPeriod = new RadioButtonGroup<>();
        btnGroupSelectPeriod.setItems("Last 10 Months", "Last 10 Weeks");
        btnGroupSelectPeriod.addValueChangeListener(select -> {
            layoutGraph.removeAll();
            if (select.getValue().indexOf("Month") != -1) {
                layoutGraph.add(loadGraphUploads(sqlUploadsGrouped + sqlGroupByMonthly + sqlUploadsGroupedOrderBy, arrColsUploadsGrouped, "month"));
            } else if (select.getValue().indexOf("Week") != -1) {
                layoutGraph.add(loadGraphUploads(sqlUploadsGrouped + sqlGroupByWeekly + sqlUploadsGroupedOrderBy, arrColsUploadsGrouped, "week"));
            }
        });
        btnGroupSelectPeriod.setValue("Last 10 Months");

        layoutFilterUploadsPeriod.add(btnGroupSelectPeriod);



        VerticalLayout layoutLastLearnings = new VerticalLayout();
        layoutLastLearnings.addClassName("page-section");
        layoutLastLearnings.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER, Padding.MEDIUM);
        H2 titleLastLearnings = new H2("Last Posted Learnings");
        Div divLastLearnings = loadLastLearnings(sqlLearningsRead, arrColumnsLearning);
        layoutLastLearnings.add(titleLastLearnings, divLastLearnings);
        verticalLayout.add(layoutLastLearnings);


        H2 titleLastPhotos = new H2("Last Photos Uploaded");
        layoutLastPhotos.addClassNames(Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.SMALL);
        layoutLastPhotos.addClassName("container-uploaded-lines");

        VerticalLayout layoutLastPhotoUploads = new VerticalLayout();
        layoutLastPhotoUploads.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER, Padding.MEDIUM);


        String finalSqlGalleryAll = sqlGalleryAll;

        HorizontalLayout layoutPhotosButton = new HorizontalLayout();

        HorizontalLayout layoutTabViewPhotos = new HorizontalLayout();
        layoutTabViewPhotos.addClassName("tab-select");
        RadioButtonGroup<String> btnGroupShowPhotos = new RadioButtonGroup<>();
        btnGroupShowPhotos.setItems("Last 5 Photos", "Last 10 Photos", "Last 15 Photos");
        btnGroupShowPhotos.addValueChangeListener(e -> {
            String strSelection = e.getSource().getValue();
            if (strSelection.indexOf(" 5 ") != -1) {
                layoutLastPhotos.removeAll();
                layoutLastPhotos.add(loadUploadedPhotos(finalSqlGalleryAll + " LIMIT 5", arrColumnNamesGallery, false, false));
            } else if (strSelection.indexOf(" 10 ") != -1) {
                layoutLastPhotos.removeAll();
                layoutLastPhotos.add(loadUploadedPhotos(finalSqlGalleryAll + " LIMIT 10", arrColumnNamesGallery, false, false));
            } else if (strSelection.indexOf(" 15 ") != -1) {
                layoutLastPhotos.removeAll();
                layoutLastPhotos.add(loadUploadedPhotos(finalSqlGalleryAll + " LIMIT 15", arrColumnNamesGallery, false, false));
            }
        });
        btnGroupShowPhotos.setValue("Last 5 Photos");
        layoutTabViewPhotos.add(btnGroupShowPhotos);

        HorizontalLayout layoutMorePhotosActions = new HorizontalLayout();
        layoutMorePhotosActions.addClassName("view-more");
        Button btnMorePhotos = new Button("More Photos");
        btnMorePhotos.addClassName("view-more");
        btnMorePhotos.addClickListener(click -> {
            btnMorePhotos.getUI().ifPresent(ui ->
                    ui.navigate(GalleryView.class)
            );
        });
        layoutMorePhotosActions.add(btnMorePhotos);
        layoutLastPhotoUploads.add(titleLastPhotos, layoutPhotosButton, layoutTabViewPhotos, layoutLastPhotos, layoutMorePhotosActions);
        verticalLayout.add(layoutLastPhotoUploads);

        VerticalLayout layoutStatistics = loadStatisticsSection();
        verticalLayout.add(layoutStatistics);

        H2 titleWeather = new H2("Current Weather at:");

        Div layoutWeather = new Div();
        layoutWeather.addClassNames(Width.FULL);
        layoutWeather.addClassName("container-weather");

        H4 titleA = new H4("Athens");
        VerticalLayout layoutResultsA = loadWeather("Athens", "Greece");
        VerticalLayout layoutAllA = new VerticalLayout();
        layoutAllA.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);
        layoutAllA.add(titleA, layoutResultsA);

        H4 titleB = new H4("Thessaloniki");
        VerticalLayout layoutResultsB = loadWeather("Thessaloniki", "Greece");
        VerticalLayout layoutAllB = new VerticalLayout();
        layoutAllB.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);
        layoutAllB.add(titleB, layoutResultsB);

        layoutWeather.add(layoutAllA, layoutAllB);
        verticalLayout.add(titleWeather, layoutWeather);

        this.removeAll();
        this.add(verticalLayout);
        this.add(genericView.loadFooter(isMobile));

        logVisitorToDb();
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

        sessionDateTime = utilsDate.calcDateTimeFromLong(Long.valueOf(sessionCreation), "UTC");
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

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String o) {
//        category = o;//beforeEvent.getRouteParameters().get("category").orElse("pictures");
    }

    private void constructUI() {
        addClassName("home-view");
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

        layoutLastPhotos = new Div();

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

        verticalLayout = new VerticalLayout();
        verticalLayout.setId("verticalLayout-home");
        if (isMobile) {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE, Padding.NONE,
                    Padding.Top.XSMALL,
//                    Gap.MEDIUM,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        } else {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.XLARGE,
                    Padding.Top.XSMALL,
//                    Gap.LARGE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        }

        //HTMLElement htmlHead = new HTMLElement();

//        HtmlContainer htmlTitle = new HtmlContainer("<title>'photoact.net Network and Act around Photography'</title>");
//        HtmlContainer htmlMeta = new HtmlContainer("<meta name='description' content='Get the latest updates from our community of photographers.'>");
//        verticalLayout.add(htmlTitle, htmlMeta);

        this.setWidthFull();

    }

    private VerticalLayout loadWeather(String city, String country) {

//        String strWhereSubClause ="";
//
//        if(category.isEmpty() ||  category.equalsIgnoreCase(STR_ALL_CATEGORIES)) {
//        }
//        else if (inCategory!=null && !inCategory.isEmpty()){
//            strWhereSubClause = strWhereSubClause  + " AND l.category LIKE '"+inCategory+"' ";
//        }else{
//            strWhereSubClause = strWhereSubClause  + " AND l.category LIKE '"+category+"' ";
//        }
//        sqlLearningsReadOrderBy =" ORDER BY l.dateInsert DESC";
//        String sqlRead = sqlLearningsRead + strWhereSubClause + sqlLearningsReadOrderBy;



//        HorizontalLayout  layoutPhotos = getDestinationPhotos(city,4);

        VerticalLayout layoutResults = new VerticalLayout();
        layoutResults.addClassName("weather-layout");
        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Gap.XSMALL, Padding.SMALL, Margin.NONE);

        LocalDate currentDate = LocalDate.now();
        logger.info("Current date: " + currentDate + " DAY " + currentDate.getDayOfWeek());



        Button btnNow = new Button("Weather @ "+city);
        btnNow.setIcon(VaadinIcon.REFRESH.create());
        btnNow.addClickListener(event -> {

           Dialog dialog = getWeatherCurrent(city, country);
           dialog.open();

        });


//        // add 300 Months to LocalDate
//        LocalDate dateTomorrow = currentDate.plusDays(1);
//        LocalDate datePlusTwo = currentDate.plusDays(2);
//        LocalDate datePlusThree = currentDate.plusDays(3);
//        LocalDate datePlusFour = currentDate.plusDays(4);


/*
        Button btnToday = new Button(currentDate.getDayOfWeek().name());
        btnToday.addClickListener(event -> {
            layoutWeather.removeAll();
            VerticalLayout layout = genericView.getWeatherApiForecast(city, country, 15);
            layoutWeather.add(layout);
        });

        Button btnTomorrow = new Button(dateTomorrow.getDayOfWeek().name());
        btnTomorrow.addClickListener(event -> {
            layoutWeather.removeAll();
            VerticalLayout layout = genericView.getWeatherApiForecast(city, country, 15);
            layoutWeather.add(layout);
        });

        Button btnDayPlusTwo = new Button(datePlusTwo.getDayOfWeek().name());
        btnDayPlusTwo.addClickListener(event -> {
            layoutWeather.removeAll();
            VerticalLayout layout = genericView.getWeatherApiForecast(city, country, 15);
            layoutWeather.add(layout);
        });

        Button btnDayPlusThree = new Button(datePlusThree.getDayOfWeek().name());
        btnDayPlusTwo.addClickListener(event -> {
            layoutWeather.removeAll();
            VerticalLayout layout = genericView.getWeatherApiForecast(city, country, 15);
            layoutWeather.add(layout);
        });

        Button btnDayPlusFour = new Button(datePlusFour.getDayOfWeek().name());
*/


        Button btnSaturday = new Button();
        Button btnSunday = new Button();
        layoutButtons.add(btnNow); //, btnToday, btnTomorrow, btnDayPlusTwo, btnDayPlusThree, btnDayPlusFour);

        layoutResults.add(layoutButtons);

        return layoutResults;
    }

    public Dialog getWeatherCurrent(String destination, String country) {


        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);

        HorizontalLayout layoutWeather = new HorizontalLayout();
        layoutWeather.getStyle().setColor("#8b94a0");
        layoutWeather.addClassNames(
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER
        );


        LocalWeatherForecast weatherForecast = new LocalWeatherForecast(weatherService, destination, country);
        weatherForecast.setMaxWidth("900px");

        layoutWeather.add(weatherForecast);



        if (destination != null && !destination.isEmpty()) {



            VerticalLayout layout = new VerticalLayout();
            layout.setMargin(false);
            layout.setSpacing(false);
            layout.setPadding(false);
            layout.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);


            Anchor apiLink = new Anchor();
            apiLink.getStyle().setColor("#8b94a0");
            apiLink.setClassName("lazy-api-link");
//            apiLink.setHref(weatherService.getUrlReference());
//            apiLink.setTarget("_blank");
//            apiLink.setText("Weather data by: " + weatherService.getTitleReference());

            layout.add(layoutWeather, apiLink);
            dialog.add(layout);
            return dialog;
        } else {
            VerticalLayout layout = new VerticalLayout();
            layout.setMargin(false);
            layout.setSpacing(false);
            layout.setPadding(false);
            return null;
        }
    }


    private Div loadLastLearnings(String sqlRead, String[] arrColumnNames) {

        Div layoutLastLearnings = new Div();
        layoutLastLearnings.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Margin.XSMALL, //Margin.Vertical.SMALL,
                Padding.LARGE, //Padding.Vertical.SMALL,
                Gap.XLARGE);

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            VerticalLayout layoutLearning = new VerticalLayout();
            layoutLearning.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                    Padding.LARGE, Margin.LARGE,
                    TextColor.TERTIARY
            );
            layoutLearning.addClassName("last-learning-item");

            Record record = lstRecords.get(r);
            String strTitle = record.getColumnData("title");
            String strSubtitle = record.getColumnData("subtitle");
            String strCategory = record.getColumnData("cat_title");
            String strCatGenre = record.getColumnData("genre_title");
            String strPicture = record.getColumnData("picture");
            String strUrl = record.getColumnData("url");
//            String strDescription = record.getColumnData("description");
            String strDuration = record.getColumnData("duration");
            String strPages = record.getColumnData("pages");
            String strDateInserted = record.getColumnData("date_inserted");
            String strTutorName = record.getColumnData("tutor_name");

            H4 h4Title = new H4(strTitle);
            h4Title.addClassNames(FontWeight.BOLD, FontSize.LARGE);


            Div divCategory = new Div();
            if (!strCategory.isEmpty()) {
                divCategory.setText("Category: " + strCategory);
            } else {
                divCategory.setText("Genre: " + strCatGenre);
            }
            Div divDuration = new Div("Duration: " + strDuration);
            Div divDateInserted = new Div("Inserted: " + strDateInserted);

            H4 divTutor = new H4(strTutorName);
            divTutor.addClassNames(FontWeight.BOLD, FontSize.LARGE);

            HorizontalLayout layoutHor1 = new HorizontalLayout();
            layoutHor1.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.BETWEEN);
            layoutHor1.add(divDuration, divDateInserted);

            HorizontalLayout layoutHor2 = new HorizontalLayout();
            layoutHor2.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.BETWEEN);
            layoutHor2.add(divTutor, divCategory);

            layoutLearning.add(h4Title, layoutHor1, layoutHor2);

            layoutLastLearnings.add(layoutLearning);
        }
        return layoutLastLearnings;
    }

    private HorizontalLayout[] loadUploadedPhotos(String sqlRead, String[] arrColumnNames, boolean isEditable, boolean isThumbnails) {


        strPath = DIR_PHOTOS_SERVER + dirChar;
        String strPath;
        if (!isThumbnails) {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathSmall;
        } else {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;
        }

//
//        Div layoutLastPhotos = new Div();
//        layoutLastPhotos.addClassNames(Overflow.HIDDEN,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Margin.NONE, Padding.MEDIUM);
/*                Width.FULL, AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Horizontal.XLARGE, Margin.Vertical.SMALL,
                Padding.Horizontal.XLARGE, Padding.Vertical.SMALL,
                Gap.LARGE);*/


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        HorizontalLayout[] layoutPhotoUploaded = new HorizontalLayout[lstRecords.size()];
        for (int r = 0; r < lstRecords.size(); r++) {

            HorizontalLayout layoutPhotoUploadedPanel = new HorizontalLayout();
            layoutPhotoUploadedPanel.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN,
//                   Margin.NONE, Padding.NONE,
                    TextColor.TERTIARY,
                    Background.CONTRAST_5
            );
            layoutPhotoUploadedPanel.addClassName("uploaded-line");

            Record record = lstRecords.get(r);
            String strFileName = record.getColumnData("name_new");
            String strTitle = record.getColumnData("title");
            String strSubTitle = record.getColumnData("subtitle");
            String strPhotoType = record.getColumnData("photo_type");

            String strCityName = record.getColumnData("city_name");
            String strUploader = record.getColumnData("uploader");
            String strDateUploaded = record.getColumnData("date_inserted");

            String strUsername = record.getColumnData("username");
            String strName = record.getColumnData("name");
            String strSurname = record.getColumnData("surname");
            String strAvatarPath = record.getColumnData("avatar_path");
            String strMemberSince = record.getColumnData("member_since");

            Image image = getImageThumbFromDb(record, strPath);
            image.getStyle().setWidth("auto");
            image.getStyle().setMaxHeight("120px");
            image.addClassNames(BorderRadius.SMALL);

            Icon iconLocation = VaadinIcon.LOCATION_ARROW_CIRCLE_O.create();
            iconLocation.getStyle().set("padding", "var(--lumo-space-xs)");
            if (strCityName == null || strCityName.trim().equalsIgnoreCase("") || strCityName.trim().equalsIgnoreCase("null") || strCityName.isEmpty()) {
                strCityName = "not defined";
            }

            Span badgeLocation = new Span(iconLocation, new Span(strCityName));
            // badgeLocation.getElement().setAttribute("theme", "badge");
            badgeLocation.getElement().getThemeList().add("badge contrast");

            Div divUploadedAt = new Div("uploaded");
            divUploadedAt.addClassNames(FontSize.XSMALL);
            Div divLocation = new Div("photo shoot at");
            divLocation.addClassNames(FontSize.XSMALL);

            Image imgAvatarMedium = genericView.getAvatarThumbImage(strAvatarPath, strName + " " + strSurname, "70px", "70px");
            AvatarItem avatarLargeItemMe = new AvatarItem(strName + " " + strSurname, "@" + strUsername, imgAvatarMedium);

//            Avatar userAvatar = new Avatar(strUploader);
//            userAvatar.setImage("https://randomuser.me/api/portraits/men/17.jpg");
//            userAvatar.getElement().setAttribute("tabindex", "-1");
//            userAvatar.addThemeVariants(AvatarVariant.LUMO_SMALL);

//            Span divUser = new Span(strUploader);
//            Span divUserObject = new Span(userAvatar, divUser);
//            divUserObject.addClassNames(AlignContent.CENTER, JustifyContent.CENTER,
//                    Padding.SMALL,
//                    BorderRadius.SMALL, Background.CONTRAST_5);

            Icon iconDateTime = VaadinIcon.CALENDAR_CLOCK.create();
            iconDateTime.getStyle().set("padding", "var(--lumo-space-xs)");
            Span badgeDateTime = new Span(iconDateTime, new Span(strDateUploaded));
            if (strDateUploaded.trim().isEmpty() || strDateUploaded.equalsIgnoreCase("null")) {
                badgeDateTime.setText("");
                badgeDateTime.setVisible(false);
            }
            badgeDateTime.getElement().getThemeList().add("badge contrast");

            VerticalLayout layoutMemberUp = new VerticalLayout();
            layoutMemberUp.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, Margin.NONE, Padding.SMALL);
            layoutMemberUp.add(avatarLargeItemMe);

            VerticalLayout layoutDateLocationUp = new VerticalLayout();
            layoutDateLocationUp.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, Margin.NONE, Padding.XSMALL, Gap.XSMALL);
            layoutDateLocationUp.add(divUploadedAt, badgeDateTime, divLocation, badgeLocation);

            layoutPhotoUploadedPanel.add(image, layoutMemberUp, layoutDateLocationUp);

            layoutPhotoUploadedPanel.getStyle().setOpacity("1");
            layoutPhotoUploaded[r] = layoutPhotoUploadedPanel;
        }
        return layoutPhotoUploaded;
    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSection) {

        this.strHeader = strHeader;
        HorizontalLayout headerContainerMaster = new HorizontalLayout();
        if (isMobile) {
            headerContainerMaster.addClassNames(
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        } else {
            headerContainerMaster.addClassNames(
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }

        VerticalLayout headerTextContainer = new VerticalLayout();
        headerTextContainer.addClassNames(
                Margin.NONE, Padding.NONE,
                Gap.XSMALL);

        H3 header = new H3(strHeader);
        header.addClassNames(Margin.Bottom.NONE, Margin.Top.NONE, FontSize.LARGE, FontWeight.BOLD, TextColor.SECONDARY);
        //header.getStyle().set("font-family", "Times-New-Roman, serif");  //"'Brush Script MT', cursive");

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(Margin.Bottom.NONE, Margin.Top.NONE, FontSize.SMALL, TextColor.SECONDARY);

        Div divSection = new Div(strSection);
        divSection.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.MEDIUM, FontWeight.BOLD, TextColor.PRIMARY);

        headerTextContainer.add(header, subheader, divSection);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Most Viewed", "Least Viewed", "Most Favourite", "Least Favourite", "Newest First", "Oldest First", "Most Liked", "Least Liked");
        sortBy.setValue("Most Viewed");

        Div headerContainerSecondary = new Div();
        if (isMobile) {
            headerContainerSecondary.addClassNames(
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        } else {
            headerContainerSecondary.addClassNames(
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }

        Div layoutFilters = new Div();
        if (isMobile) {
            layoutFilters.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE);
        } else {
            layoutFilters.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }
        layoutFilters.addClassName("header-layout-filters");


        ArrayList<String> lstCategories = new ArrayList<>();
        lstCategories.add("Street Photography");
        lstCategories.add("Landscape Photography");
        lstCategories.add("Techniques");

        for (int c = 0; c < lstCategories.size(); c++) {
            String captionCategory = lstCategories.get(c);
            RouteParam routeCategory = new RouteParam("category", captionCategory);
            RouterLink linkPhotoCategory = new RouterLink(captionCategory, HomeView.class, new RouteParameters(routeCategory));
            layoutFilters.add(linkPhotoCategory);
        }


        CheckboxGroup<String> checkboxGroupFormat = new CheckboxGroup<>();
        checkboxGroupFormat.setTooltipText("Format");
//        checkboxGroupFormat.setLabel("Format");
        checkboxGroupFormat.setItems("Book", "Youtube");


        CheckboxGroup<String> checkboxGroupLocation = new CheckboxGroup<>();
        checkboxGroupLocation.setTooltipText("Location");
//         checkboxGroupLocation.setLabel("Location");
        checkboxGroupLocation.setItems("Hungary", "UK", "Greece");//, "Thursday",

//        VerticalLayout layoutHeaderParameters = new VerticalLayout();
//        if (isMobile) {
//            layoutHeaderParameters.addClassNames(
//                    AlignItems.CENTER, JustifyContent.EVENLY,
//                    Overflow.HIDDEN, Width.FULL,
//                    Margin.SMALL,
//                    Padding.NONE,
//                    Gap.XSMALL,
//                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
//                    //   Background.CONTRAST_5,
//                    BorderRadius.NONE
//            );
//        } else {
//            layoutHeaderParameters.addClassNames(
//                    AlignItems.CENTER, JustifyContent.EVENLY,
//                    Overflow.HIDDEN, Width.FULL,
//                    Margin.SMALL,
//                    Padding.NONE,
//                    Gap.XSMALL,
//                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
////                       Background.CONTRAST_5,
//                    BorderRadius.LARGE
//            );
//        }

        Select<String> cmbView = new Select<>();
        cmbView.setLabel("View");

        cmbView.setItems("Micro View", "Ordinary - No MetaData", "Ordinary - MetaData Bottom", "Ordinary - MetaData Right",
                "Wide - No MetaData", "Wide - MetaData Bottom", "Wide - MetaData Right");
        cmbView.setValue("Ordinary - No MetaData");

//        headerContainerMaster.add(headerTextContainer);
//        layoutHeaderParameters.add(headerContainerMaster);


//        headerContainerMaster.add(headerTextContainer);
//        headerContainerSecondary.add(layoutFilters);
//        layoutHeaderParameters.add( headerContainerSecondary, divSection);

        HeaderFilterTabs headerFilterTabs = new HeaderFilterTabs(recordService, isMobile);
        VerticalLayout layoutHeaderParameters = headerFilterTabs.getHeader(strHeader, strSubHeader, strSection);

//        headerContainerMaster.add(headerTextContainer, cmbView);
//        headerContainerSecondary.add(layoutFilters, sortBy);
//        layoutHeaderParameters.add(headerContainerMaster,headerContainerSecondary);

        return layoutHeaderParameters;
    }

    private VerticalLayout getCarousel(ArrayList<Image> lstImage)//, ArrayList<String> strTitle, ArrayList<String> strDescription){
    {
        VerticalLayout layoutCarousel = new VerticalLayout();
        layoutCarousel.addClassNames(Height.AUTO,
                BorderRadius.LARGE,
                AlignItems.CENTER, JustifyContent.CENTER,
                Background.CONTRAST_20);
        layoutCarousel.addClassName("carousel");

        Slide[] slides = new Slide[lstImage.size()];
        for (int i = 0; i < lstImage.size(); i++) {

            Image image = lstImage.get(i);
            image.addClassNames(Width.FULL, Height.AUTO, BorderRadius.LARGE);

            slides[i] = new Slide(image);
            slides[i].addClassNames(Width.FULL, Height.AUTO, BorderRadius.LARGE);
        }
        Carousel carousel = new Carousel();
        carousel.setSlides(slides);
        carousel.setWidthFull();
        carousel.setAutoProgress(true);
        carousel.setSlideDuration(3);
        carousel.addClassNames( //Background.CONTRAST_5,
                Width.FULL, Height.AUTO,
                BorderRadius.LARGE,
                AlignItems.CENTER, JustifyContent.CENTER);

        Div titleImg = new Div((lstImage.size()) + " photos");
        layoutCarousel.add(carousel, titleImg);

        return layoutCarousel;
    }

    private VerticalLayout loadGraphUploads(String sqlRead, String[] arrColumnNames, String strColumn) {

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        Series<Object> data = new Series<>();
        data.setName("Photos");
        Object[] intPhotos = new Object[lstRecords.size()];
        String[] strMonths = new String[lstRecords.size()];
        for (int r = 0; r < lstRecords.size(); r++) {

            intPhotos[r] = Integer.parseInt(lstRecords.get(r).getColumnData("photos"));
            strMonths[r] = lstRecords.get(r).getColumnData(strColumn);
        }

        data.setData(intPhotos);

        VerticalLayout layoutUploads = new VerticalLayout();
        if (isMobile) {
            layoutUploads.addClassNames(
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.XSMALL, Padding.MEDIUM,
                    Gap.XSMALL
            );
            layoutUploads.setWidth("97%");
        } else {
            layoutUploads.addClassNames(
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.SMALL, Padding.LARGE,
                    Gap.SMALL
            );
            layoutUploads.setWidth("90%");
            layoutUploads.setMaxWidth("1300px");
        }
        layoutUploads.addClassName("chart-panel");


        ApexChartsBuilder charts1 = new ApexChartsBuilder();
        charts1.withChart(ChartBuilder.get()
                        .withType(Type.AREA).withHeight("400px")
                        .build())
                .withLabels(strMonths)
                .withColors()
                .withLegend(LegendBuilder.get()
                        .withPosition(com.github.appreciated.apexcharts.config.legend.Position.LEFT)
                        .withHorizontalAlign(HorizontalAlign.LEFT)
                        .build())
                .withSeries(data)
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .build())
                //.withTitle(title1)
                .build();
//        Div divTitle1 = new Div("Interesting Subject & well structured");
//        divTitle1.getStyle().setColor("#5d6f87");
//        divTitle1.setWidthFull();
//        Div layoutGraph1 = new Div();
//        layoutGraph1.setClassName("lazy-poll-graph");
//        layoutGraph1.setMinHeight("190px");sqlUploadsGrouped
//        layoutGraph1.add(divTitle1, charts1.build());

        layoutUploads.add(charts1.build());

        return layoutUploads;
    }


    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames, Object[] sqlParValue, String[] sqlParType) {
        logger.info(" photo  getRecordsFromDb with params:   " + sql);
        return recordService.findAll(sql, arrColumnNames, sqlParValue, sqlParType);
    }

    private ArrayList<Image> loadImagesFromDbToCarousel(String sqlRead, String[] arrColumnNames, boolean isEditable, boolean isThumbnails) {
        strPath = DIR_PHOTOS_SERVER + dirChar;
        String strPath;
        if (!isThumbnails) {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathMedium;
        } else {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;
        }

//        Div divGallery = new Div();
//        divGallery.addClassName("gallery");


//        image1.addClassNames(Width.FULL, BorderRadius.LARGE);
//        image1.getStyle().setHeight("auto");

        ArrayList<Image> lstImage = new ArrayList<>();

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {


            Record rec = lstRecords.get(r);
            lstImage.add(getImageThumbFromDb(rec, strPath));
        }
        return lstImage;
    }

    private Image getImageThumbFromDb(Record record, String strPathIn) {
        strPath = strPathIn;

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");

        String strCityName = record.getColumnData("city_name");
        String strUploader = record.getColumnData("uploader");

        if (strTitle == null || strTitle.isEmpty()) {
            strTitle = "image";
        }

        String strImagePath = strPath + dirChar + strFileName;
        logger.info(" strImagePath " + strImagePath);
//        Image image1 = new Image("https://images.unsplash.com/photo-1536048810607-3dc7f86981cb?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80", "img2");
        //GalleryImageViewCard imageGalleryViewCard = new GalleryImageViewCard(record,strImagePath,isMobile,userId, strUsername, sessionCreation,hostname,publicIp, isEditable, linkUploader, lstRouterLinks, recordService);
        Image image = new Image();

        final StreamResource imageResource = new StreamResource("streamResource", () -> {
            try {

                Path path = Paths.get(strImagePath);
                File file = path.toFile();
                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
//                logErrorInDb(e, "GalleryImageViewCard StreamResource FileNotFoundException", hostname, userId, strUserName, publicIp, sessionCreation, file.getAbsolutePath());
                // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                logger.error(e.getMessage());
            }
            return null;
        });

        image.setSrc(imageResource);
        return image;
    }


    private HorizontalLayout getDestinationPhotos(String destination, int count) {
        HorizontalLayout layoutPhotos = new HorizontalLayout();
        layoutPhotos.setPadding(false);
        layoutPhotos.setMargin(false);
        layoutPhotos.setSpacing(false);
        //layoutPhotos.setWidthFull();
        PhotoFlickrService photoFlickr = new PhotoFlickrService();
        ArrayList<Photo> listPhotos = photoFlickr.findPhotos(destination, count);
        for (int p = 0; p < listPhotos.size(); p++) {

            Photo photo = listPhotos.get(p);
            Image image = new Image();
            image.addClassNames(BorderRadius.LARGE);

//            if(imgSize==1) {
//                String thumbUrl = photo.getThumbnailUrl();//.getSmallUrl(); //.getThumbnailUrl();
//                image.setSrc(thumbUrl);
//                image.setMaxHeight("90px");
//                image.setWidth("auto");
//            }else{
            String thumbUrl = photo.getMedium640Url();//.getThumbnailUrl();//.getSmallUrl(); //.getThumbnailUrl();
            image.setSrc(thumbUrl);
            image.setMaxHeight("340px");
            image.setWidth("auto");
//            }


            String title = photo.getTitle();
            User user = photo.getOwner();

            String strRealName = user.getRealName();
            user.getProfileurl();
            user.getPhotosCount();
            String strPhotosUrl = user.getPhotosurl();

            String strTitle = photo.getTitle();


            VerticalLayout photoLayout = new VerticalLayout();
            photoLayout.addClassNames(Width.FULL,
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Margin.NONE, Padding.SMALL,
                    BorderRadius.LARGE,
                    Background.CONTRAST_5
            );


            HorizontalLayout layoutUser = new HorizontalLayout();
            layoutUser.setSpacing(false);
            layoutUser.setMargin(false);
            layoutUser.setPadding(false);
            layoutUser.setAlignItems(FlexComponent.Alignment.CENTER);
            layoutUser.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

            String userId = user.getId();
            String userName = user.getRealName(); //photoFlickr.getUserName(userId); //user.getUsername();

            strTitle = (strTitle != null ? photo.getTitle() : "");
            strRealName = strRealName != null ? strRealName : userId;

            logger.info("  " + userName + "  " + userId + "  ");

            String userUrl = "https://www.flickr.com/photos/" + userId;
            Anchor linkUserInNewTab = new Anchor(userUrl, "");
            linkUserInNewTab.getElement().setAttribute("target", "_blank");
            linkUserInNewTab.addComponentAtIndex(0, VaadinIcon.USER.create());
            linkUserInNewTab.setClassName("lazy-result-line-button");

            //Div divUser = new Div();
            // divUser.add(VaadinIcon.USER_CARD.create());
            //divUser.setText("flickr user: ");
            //divUser.setClassName("lazy-result-line-button");

            layoutUser.add(linkUserInNewTab, new Div(strRealName));
            photoLayout.add(layoutUser, image, new Div(strTitle));
            layoutPhotos.add(photoLayout);


//
//                photoUrls.add(photoList.get(i).getThumbnailUrl());//.getSmall320Url());
//             //   layoutPhotos.add(photoList.get(i).getThumbnailUrl());


//            Image image = new Image(listPhotosLayout.get(p),destination);
//            image.setHeight("180px");
//            image.setWidth("auto");
            //           layoutPhotos.add(image);
        }


        return layoutPhotos;

    }


    //
//        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
//        for (int r = 0;r< lstRecords.size();r++) {
//
//            Record rec = lstRecords.get(r);
//            layoutLearnings.add(getLearningsItem(rec));
//        }
//        return layoutLearnings;
//    }
    private Div loadLearningTopics(String sqlRead, String[] arrColumnNames) {

        Div panelOfTopics = new Div();
        if (isMobile) {
            panelOfTopics.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.SMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE);
        } else {
            panelOfTopics.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.XLARGE,
                    Gap.SMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }
        panelOfTopics.addClassName("learning-photo-genres");


        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);

        ArrayList<String> lstCategories = new ArrayList<>();
        ArrayList<String> lstCategoriesDescriptions = new ArrayList<>();
        ArrayList<String> lstCategoriesCount = new ArrayList<>();
        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {
            lstCategories.add(lstLearningCategoriesRecs.get(r).getColumnData("cat_type"));
            String strDescr = lstLearningCategoriesRecs.get(r).getColumnData("cat_description_min");
            String strCount = lstLearningCategoriesRecs.get(r).getColumnData("cat_title_count");
            lstCategoriesCount.add(strCount);
            lstCategoriesDescriptions.add(strDescr);
        }

        for (int c = 0; c < lstCategories.size(); c++) {
            String captionCategory = lstCategories.get(c);
            String captionCategoryDescription = lstCategoriesDescriptions.get(c);
            H3 categoryTitle = new H3(captionCategory);
            Div categoryDescription = new Div(captionCategoryDescription);

            if (captionCategoryDescription.isEmpty() || captionCategoryDescription.equalsIgnoreCase("null")) {
                categoryDescription.setVisible(false);
            }
            String strCount = lstCategoriesCount.get(c);
            int intCount = Integer.parseInt(strCount);
            if (intCount == 1) {
                strCount = strCount + " Learning";
            } else {
                strCount = strCount + " Learnings";
            }
            H6 divCount = new H6(strCount);

            RouteParam routeCategory = new RouteParam("category", captionCategory);
            RouterLink linkPhotoCategory = new RouterLink(LearningsView.class, new RouteParameters(routeCategory));
            linkPhotoCategory.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN);
            linkPhotoCategory.add(categoryTitle, categoryDescription, divCount);

            panelOfTopics.add(linkPhotoCategory);
        }
        return panelOfTopics;
    }


    private Div loadLearningsAboutGenres(String sqlRead, String[] arrColumnNames) {

        Div panelOfGenres = new Div();
        if (isMobile) {
            panelOfGenres.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE);
        } else {
            panelOfGenres.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.XLARGE,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }
        panelOfGenres.addClassName("learning-photo-genres");

        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);

        ArrayList<String> lstCategories = new ArrayList<>();
        ArrayList<String> lstCategoriesCount = new ArrayList<>();
        ArrayList<String> lstCategoriesDescriptions = new ArrayList<>();
        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {
            lstCategories.add(lstLearningCategoriesRecs.get(r).getColumnData("cat_title"));
            String strDescr = lstLearningCategoriesRecs.get(r).getColumnData("cat_description_min");
            String strCount = lstLearningCategoriesRecs.get(r).getColumnData("cat_title_count");
            lstCategoriesCount.add(strCount);
            if (strDescr != null && !strDescr.isEmpty() && !strDescr.equalsIgnoreCase("null")) {
                lstCategoriesDescriptions.add(strDescr);
            } else {
                lstCategoriesDescriptions.add("");
            }

        }

        for (int c = 0; c < lstCategories.size(); c++) {
            String captionCategory = lstCategories.get(c);
            String captionCategoryDescription = lstCategoriesDescriptions.get(c);
            H3 genreTitle = new H3(captionCategory);
            Div genreDescription = new Div(captionCategoryDescription);
            String strCount = lstCategoriesCount.get(c);

            int intCount = Integer.parseInt(strCount);
            if (intCount == 1) {
                strCount = strCount + " Learning";
            } else {
                strCount = strCount + " Learnings";
            }
            H6 divCount = new H6(strCount);

            RouteParam routeCategory = new RouteParam("genre", captionCategory);
            RouterLink linkPhotoCategory = new RouterLink(LearningsView.class, new RouteParameters(routeCategory));
            linkPhotoCategory.add(genreTitle, genreDescription, divCount);

            panelOfGenres.add(linkPhotoCategory);
        }
        return panelOfGenres;
    }


    private HorizontalLayout getActions() {

        StreamResource iconLike = new StreamResource("star-empty-icon.svg",
                () -> getClass().getResourceAsStream("/icons/star-empty-icon.svg"));
        SvgIcon svgLike = new SvgIcon(iconLike);
        Button btnLike = new Button(svgLike);

        Div divInfo = new Div("1");
        divInfo.addClassName(TextColor.DISABLED);

        btnLike.setSuffixComponent(divInfo);
        btnLike.setTooltipText("Like It");


//        StreamResource iconAction = new StreamResource("stories.svg",
//                () -> getClass().getResourceAsStream("/icons/stories.svg"));
//        SvgIcon svgAction = new SvgIcon(iconAction);
        Button btnMoreAction = new Button(VaadinIcon.BOOKMARK.create());//svgAction);
        btnMoreAction.setTooltipText("Save to list");


        Button btnComment = new Button(VaadinIcon.COMMENT.create());
        btnComment.setTooltipText("Comment on it");

        Button btnMoreInfo = new Button(VaadinIcon.INFO_CIRCLE_O.create());
        btnMoreInfo.setTooltipText("More info");

        StreamResource iconShare = new StreamResource("share-line-icon.svg",
                () -> getClass().getResourceAsStream("/icons/share-line-icon.svg"));
        SvgIcon svgShare = new SvgIcon(iconShare);
        Button btnShare = new Button(svgShare);
        btnShare.setTooltipText("Share it");

        HorizontalLayout layoutActions = new HorizontalLayout();
        if (isMobile) {
            layoutActions.addClassNames(
                    Overflow.HIDDEN, //Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.SMALL,
                    Padding.NONE
//                    Gap.XSMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //   Background.CONTRAST_5,
//                    BorderRadius.LARGE
            );
            layoutActions.addClassName("actions-toolbar");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
        } else {
            layoutActions.addClassNames(
                    Overflow.HIDDEN, //Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.SMALL,
                    Padding.NONE
//                    Gap.LARGE,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //   Background.CONTRAST_5,
//                    BorderRadius.LARGE
            );
            layoutActions.addClassName("actions-toolbar");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
        }
        //layoutActions.setWidthFull();


        layoutActions.add(btnLike, btnMoreAction, btnComment, btnMoreInfo, btnShare);

        return layoutActions;
    }

    private VerticalLayout getSubTabs(String strContentType, String strContentTitle, Record record) {

        VerticalLayout layoutTabsInfo = new VerticalLayout();
        if (isMobile) {
            layoutTabsInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM
            );
        } else {
            layoutTabsInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM
            );
        }

        HorizontalLayout layoutExtLinks = new HorizontalLayout();
        layoutExtLinks.addClassNames(
                Overflow.HIDDEN, Width.AUTO,
                Margin.SMALL,
                Padding.NONE,
                Gap.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER
        );

        ArrayList<String> lstLocationTabs = new ArrayList<String>();
        lstLocationTabs.add("Reviews");
        lstLocationTabs.add("Notes");
        lstLocationTabs.add("Additional Sources");


        ToggleButtonGroup btnGroup = new ToggleButtonGroup();
        btnGroup.addClassNames(Width.SMALL,
                Overflow.HIDDEN, Width.AUTO,
                Margin.SMALL,
                Padding.NONE,
                Gap.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER
        );
        btnGroup.setItems(lstLocationTabs);
        btnGroup.setToggleable(true);


        Div divTabContent = new Div();
        divTabContent.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER,
                Height.LARGE
        );

        btnGroup.addValueChangeListener(event -> {
            if (event.getValue().toString().equalsIgnoreCase("My Notes")) {
                divTabContent.setText(" my notes ... of " + strContentTitle + " in " + strContentType);
            } else if (event.getValue().toString().equalsIgnoreCase("Reviews")) {
                divTabContent.setText(strUsername + " users review 1 ...");
            } else {
                divTabContent.setText(strContentTitle + " ....... in " + strContentType);
            }
        });

        layoutTabsInfo.add(btnGroup, divTabContent);

        return layoutTabsInfo;
    }

    private void displayLoginDialog(){

        LoginDialog loginDialog = new LoginDialog();

        loginDialog.getLoginForm().setAction("login");
        loginDialog.open();

    }


    private void displayRegisterDialog() {
//        DialogRegistration dialogRegister = new DialogRegistration(isMobile, "", sessionCreation, hostname, publicIp, recordService,
//                section, "register-from-home-view", emailSendService);
        RegistrationDialog dialogRegister = new RegistrationDialog(isMobile, "", sessionCreation, hostname, publicIp, recordService,
                section, "register-from-home-view", emailSendService);
        dialogRegister.open();
    }

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {

        logger.info(" photo  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

//    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames, Object[] sqlParValue, String[] sqlParType) {
//        logger.info(" photo  getRecordsFromDb with params:   " + sql);
//        return recordService.findAll(sql,arrColumnNames, sqlParValue, sqlParType);
//    }

    private VerticalLayout loadStatisticsSection() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("page-section");
        layout.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER, Padding.MEDIUM);

        H2 title = new H2("Photo Statistics");

        Div statsContainer = new Div();
        statsContainer.addClassName("stats-gallery");
        statsContainer.addClassNames(Width.FULL);

        final String[] activeFilter = {"Most Viewed"};
        final int[] activeCount = {10};

        // SVG path data for filter icons (Material Design, viewBox 0 0 24 24)
        String eyePath    = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5C21.27 7.61 17 4.5 12 4.5zm0 12.5c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
        String thumbPath  = "M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z";
        String uploadPath = "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z";

        Div filterTiles = buildRadioTiles("stat-filter",
                new String[]{"Most Viewed", "Most Liked", "Most Recent"},
                new String[]{eyePath, thumbPath, uploadPath},
                val -> {
                    activeFilter[0] = val;
                    statsContainer.removeAll();
                    loadStatsPhotos(statsContainer, activeFilter[0], activeCount[0]);
                });

        Div countTiles = buildRadioTiles("stat-count",
                new String[]{"10", "20", "30"},
                null,
                val -> {
                    activeCount[0] = Integer.parseInt(val);
                    statsContainer.removeAll();
                    loadStatsPhotos(statsContainer, activeFilter[0], activeCount[0]);
                });
        countTiles.addClassName("radio-inputs--compact");

        loadStatsPhotos(statsContainer, "Most Viewed", 10);

        layout.add(title, filterTiles, countTiles, statsContainer);
        return layout;
    }

    private Div buildRadioTiles(String groupName, String[] labels, String[] svgPaths,
                                Consumer<String> onChange) {
        Div container = new Div();
        container.addClassName("radio-inputs");

        for (int i = 0; i < labels.length; i++) {
            Element labelEl = new Element("label");

            Element inputEl = new Element("input");
            inputEl.setAttribute("class", "radio-input");
            inputEl.setAttribute("type", "radio");
            inputEl.setAttribute("name", groupName);
            inputEl.setAttribute("value", labels[i]);
            if (i == 0) inputEl.setAttribute("checked", "");

            Element tileEl = new Element("div");
            tileEl.setAttribute("class", "radio-tile");

            if (svgPaths != null && svgPaths[i] != null) {
                Element iconDiv = new Element("div");
                iconDiv.setAttribute("class", "radio-icon");
                iconDiv.appendChild(createSvgIcon(svgPaths[i]));
                tileEl.appendChild(iconDiv);
            }

            Element labelSpan = new Element("span");
            labelSpan.setAttribute("class", "radio-label");
            labelSpan.setText(labels[i]);
            tileEl.appendChild(labelSpan);

            String val = labels[i];
            inputEl.addEventListener("change", e -> onChange.accept(val));

            labelEl.appendChild(inputEl, tileEl);
            container.getElement().appendChild(labelEl);
        }

        return container;
    }

    private Element createSvgIcon(String pathData) {
        Element svg = new Element("svg");
        svg.setAttribute("viewBox", "0 0 24 24");
        svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        Element path = new Element("path");
        path.setAttribute("d", pathData);
        svg.appendChild(path);
        return svg;
    }

    private void loadStatsPhotos(Div container, String filter, int count) {
        String sql;
        switch (filter) {
            case "Most Liked":
                sql = photoStatisticsService.getMostLikedSql(count);
                break;
            case "Most Recent":
                sql = photoStatisticsService.getMostRecentSql(count);
                break;
            default:
                sql = photoStatisticsService.getMostViewedSql(count);
        }

        String strPath = DIR_PHOTOS_SERVER + dirChar + subPathSmall;
        List<Record> lstRecords = getRecordsFromDb(sql, PhotoStatisticsService.STATS_COLUMNS);

        String sqlCarouselForStats = photoStatisticsService.getMostRecentSql(20);
        String sqlCarouselOrderBy = " ORDER BY pm.date_inserted DESC";

        for (Record record : lstRecords) {
            String strFileName = record.getColumnData("name_new");
            String strImagePath = strPath + dirChar + strFileName;

            GalleryImageViewCard card = new GalleryImageViewCard(
                    record, strImagePath, isMobile, userId, strUsername,
                    sessionCreation, hostname, publicIp,
                    false,
                    recordService,
                    2,
                    sqlCarouselForStats,
                    sqlCarouselOrderBy,
                    PhotoStatisticsService.STATS_COLUMNS,
                    shareService, shareMetricService, weatherService,
                    photoRatingService, photoViewService,
                    GalleryImageViewCard.CardSize.COMPACT
            );
            container.add(card);
        }
    }

    private void logVisitorToDb() {

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
            strUrlRequestToBeLogged = strUrlRequestToBeLogged.replace("'", "");
            strUrlRequestToBeLogged = "'" + strUrlRequestToBeLogged + "'";
        }

        if (strPath == null || strPath.isEmpty()) {
            strPath = "NULL";
        } else {
            strPath = strPath.replace("\\", "-");
            strPath = "'" + strPath + "'";
        }


        logger.info("photo visitor:" + publicIp + " . " + hostname + " . " + hostAddress + " . " + canonicalHostname + "  .  " + browser + " " + sessionid);

        String insertSQL = "INSERT INTO dbvisitor_log SET visitorlogId = 0,  timeOfVisit = now(), ipAddress = '" + publicIp + "', browserName = '" + browser + "', "
                + " browserVersionMajor = '" + versionOfBrowserMajor + "', browserVersionMinor = '" + versionOfBrowserMinor + "', urlParameter = NULL , timeZoneId = '" + timeZoneId + "', "
                + " appVersion = '" + APP_NAME + "-" + APP_VERSION + "', parentSection = 'photo', sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
                + " hostAddress = '" + hostAddress + "', os = '" + strOS + "', browser = '" + strBrowser + "', section = '" + section + "',"
                + " item = " + strPath + ", ref = " + strUrlRequestToBeLogged + ", "
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
