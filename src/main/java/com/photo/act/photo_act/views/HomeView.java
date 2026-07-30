package com.photo.act.photo_act.views;

import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flowingcode.vaadin.addons.carousel.Carousel;
import com.flowingcode.vaadin.addons.carousel.Slide;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.ResponsiveBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.HorizontalAlign;
import com.github.appreciated.apexcharts.helper.Series;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.dto.LearningDto;
import com.photo.act.photo_act.services.EmailSendService;
import com.photo.act.photo_act.services.LearningService;
import com.photo.act.photo_act.services.PhotoFlickrService;
import com.photo.act.photo_act.services.PhotoRatingService;
import com.photo.act.photo_act.services.PhotoStatisticsService;
import com.photo.act.photo_act.services.PhotoViewService;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.PageSeoUtil;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.*;
import com.vaadin.flow.component.Component;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static com.photo.act.photo_act.views.MainLayout.*;







@AnonymousAllowed
@PageTitle( "PhotoAct.net - Photography Community | Share Photos, Stories & Events")
@Route(value = "") //":category?")
//@RouteAlias(value = "home") // empty on homepage
@RouteAlias(value = "home/:category?", layout = MainLayout.class)
//@Route(value = "learnings") //":category?")
//@RouteAlias(value = "learnings/category/:category?", layout = MainLayout.class)
//@RouteAlias(value = "learnings/tutor/:tutor?", layout = MainLayout.class)
//@RouteAlias(value = "learnings/category/:category/tutor/:tutor?", layout = MainLayout.class)

//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
//@EnableGoogleAnalytics(value="G-NQH7NZ6JJL", devLogging = EnableGoogleAnalytics.LogLevel.NONE, sendMode = EnableGoogleAnalytics.SendMode.ALWAYS)
public class HomeView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasStyle {

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
    private LearningService learningService;

    public HomeView(RecordService recordService, LearningService learningService,
                    EmailSendService emailSendService, WeatherService weatherService,
                    ShareService shareService, ShareMetricService shareMetricService,
                    PhotoRatingService photoRatingService, PhotoViewService photoViewService,
                    PhotoStatisticsService photoStatisticsService) {
        this.recordService = recordService;
        this.learningService = learningService;
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
    public void beforeEnter(@OptionalParameter BeforeEnterEvent event) {
        category = event.getRouteParameters().get("category").orElse(STR_ALL_CATEGORIES);
//        tutor = event.getRouteParameters().get("tutor").orElse(STR_ALL_TUTORS);

        PageSeoUtil.setMetaDescription("Community website of photographers, sharing their photos, stories, learning sources and events.");
        getUserClientInfo();

        userId = 1;
        strUsername = "visitor-user";
        verticalLayout.removeAll();

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

        // Latest uploaded photo per distinct member — one card per uploader, most recent first.
        String sqlGalleryLatestPerMember = sqlReadGallery + " WHERE pm.visible_to = 'ALL' " +
                " AND usr.userId = pm.uploaderId " +
                " AND pm.id = (SELECT pm2.id FROM photo_meta pm2 WHERE pm2.uploaderId = pm.uploaderId AND pm2.visible_to = 'ALL' ORDER BY pm2.date_inserted DESC LIMIT 1) " +
                " ORDER BY pm.date_inserted DESC ";

        String[] arrColsUploadsGrouped = {"Month", "Photos"};

        String sqlUploadsGrouped = "SELECT DATE_FORMAT(pm.date_inserted, '%M %Y') as 'month', DATE_FORMAT(pm.date_inserted, '%V-%Y') as 'week', COUNT(pm.id) AS 'Photos'" +
                " FROM photo_meta pm " +
                " WHERE visible_to LIKE 'all' ";
        String sqlGroupByMonthly = " GROUP BY DATE_FORMAT(pm.date_inserted, '%M %Y') ";
        String sqlGroupByWeekly = " GROUP BY DATE_FORMAT(pm.date_inserted, '%V-%Y') ";
        String sqlUploadsGroupedOrderBy = " ORDER BY DATE_FORMAT(pm.date_inserted, '%Y-%m-%V') DESC LIMIT 10";

        String usrName = genericView.checkIfAuthUserName();

        // ── Hero — option 2a: kicker + headline + CTAs ──────────────
        verticalLayout.add(createHeroSection(usrName));

/*        if (usrName != null) {
            Div authPanel = new Div(genericView.getAuthUserPanel(usrName));
            authPanel.addClassName("hero-auth-panel");
            verticalLayout.add(authPanel);
        }*/

        // ── Hero slider — real community photos ─────────────────────
        HeroSliderComponent heroSlider = new HeroSliderComponent(
                recordService, photoStatisticsService,
                photoViewService, photoRatingService,
                shareService, shareMetricService,
                DIR_PHOTOS_SERVER, isMobile, userId, publicIp);
        verticalLayout.add(heroSlider);
        // ──────────────────────────────────────────────────────────

        // ── Tell it as a Photo Story — composer promo ───────────────
        verticalLayout.add(createStoryComposerSection());

        // ── Showcase / Feedback / Learn feature grid ────────────────
        verticalLayout.add(createFeatureGrid());

        // ── Fresh from the community ─────────────────────────────────
        verticalLayout.add(createCommunityGrid(sqlGalleryLatestPerMember, arrColumnNamesGallery));

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


 /*       VerticalLayout layoutPhotoUploads = new VerticalLayout();
        layoutPhotoUploads.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
        Padding.SMALL, Margin.NONE);
        layoutPhotoUploads.addClassName("page-section");
        H2 titleGraphLastPhotos = new H2("Photo Uploads");
        HorizontalLayout layoutFilterUploadsPeriod = new HorizontalLayout();
        layoutFilterUploadsPeriod.addClassName("tab-select");
        VerticalLayout layoutGraph = new VerticalLayout();
        layoutGraph.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.NONE, Margin.NONE);
        layoutGraph.add(loadGraphUploads(sqlUploadsGrouped + sqlGroupByMonthly + sqlUploadsGroupedOrderBy, arrColsUploadsGrouped, "month"));
        layoutPhotoUploads.add(titleGraphLastPhotos, layoutFilterUploadsPeriod, layoutGraph);
        verticalLayout.add(layoutPhotoUploads);*/

/*        HorizontalLayout layoutTabSelectPeriod = new HorizontalLayout();
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

        layoutFilterUploadsPeriod.add(btnGroupSelectPeriod);*/



        Div layoutLastNewsSection = new Div();

        VerticalLayout layoutLastNews = loadLastNews();
        layoutLastNewsSection.add( layoutLastNews);
        verticalLayout.add(layoutLastNewsSection);

        String finalSqlGalleryAll = sqlGalleryAll;

        HorizontalLayout layoutPhotosButton = new HorizontalLayout();

/*        HorizontalLayout layoutTabViewPhotos = new HorizontalLayout();
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
        layoutTabViewPhotos.add(btnGroupShowPhotos);*/
/*
        HorizontalLayout layoutMorePhotosActions = new HorizontalLayout();
        layoutMorePhotosActions.addClassName("view-more");
        Button btnMorePhotos = new Button("More Photos");
        btnMorePhotos.addClassName("view-more");
        btnMorePhotos.addClickListener(click -> {
            btnMorePhotos.getUI().ifPresent(ui ->
                    ui.navigate(GalleryView.class)
            );
        });
        layoutMorePhotosActions.add(btnMorePhotos);*/





        // ── Most viewed locations ─────────────────────────────────────
        verticalLayout.add(createLocationsSection());

        H2 titleWeather = new H2("Current Weather");

        Div layoutWeather = new Div();
        layoutWeather.addClassName("learnings-horizontal-panel");
        layoutWeather.addClassName("section-header-row");

        layoutWeather.add(titleWeather);

        HorizontalLayout layoutCities = new HorizontalLayout();
        layoutCities.addClassName("container-weather");
        layoutCities.setWrap(true);

//        H4 titleA = new H4("Athens");
        VerticalLayout layoutResultsA = loadWeather("Athens", "Greece");
/*        VerticalLayout layoutAllA = new VerticalLayout();
        layoutAllA.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);
        layoutAllA.add(titleA, layoutResultsA);*/

//        H4 titleB = new H4("Thessaloniki");
        VerticalLayout layoutResultsB = loadWeather("Thessaloniki", "Greece");
        /*VerticalLayout layoutAllB = new VerticalLayout();
        layoutAllB.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);
        layoutAllB.add(titleB, layoutResultsB);*/

        layoutCities.add(layoutResultsA, layoutResultsB);
        verticalLayout.add( layoutWeather,layoutCities);

        Div divBanner = new Div();
        divBanner.addClassNames(Padding.LARGE, Margin.NONE);
        divBanner.add(createCtaBanner(usrName));

        this.removeAll();
        this.add(verticalLayout);
        this.add(divBanner);
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

    // ================================================================
    // Option 2a — "Sidebar layout + photo-story composer" home sections
    // ================================================================

    private Div createHeroSection(String usrName) {
        Div hero = new Div();
        hero.addClassName("hero-2a");

        Span kicker = new Span("Photography Community");
        kicker.addClassName("hero-kicker");

        H1 heroTitle = new H1("Upload and share in our community.");
        heroTitle.addClassName("hero-title");

        Paragraph heroSubtitle = new Paragraph("Post your best shots, tell them as a story, and trade feedback "
                + "with photographers who take the craft seriously.");
        heroSubtitle.addClassName("hero-subtitle");

        Button btnUpload = new Button("Upload Photos");
        btnUpload.addClassName("btn-hero-primary");
        btnUpload.addClickListener(click -> {
            if (usrName == null) {
                displayRegisterDialog();
            } else {
                btnUpload.getUI().ifPresent(ui -> ui.navigate(UploadView.class));
            }
        });

        Button btnExplore = new Button("Explore Community");
        btnExplore.addClassName("btn-hero-outline");
        btnExplore.addClickListener(click ->
                btnExplore.getUI().ifPresent(ui -> ui.navigate(PhotographersView.class)));

        HorizontalLayout heroActions = new HorizontalLayout(btnUpload, btnExplore);
        heroActions.addClassName("hero-actions");
        heroActions.setWrap(true);

        hero.add(kicker, heroTitle, heroSubtitle, heroActions);
        return hero;
    }

    private Div createStoryComposerSection() {
        Div section = new Div();
        section.addClassName("story-section");

        Div headerRow = new Div();
        headerRow.addClassName("section-header-row");
        H2 title = new H2("Tell it as a Photo Story");
        Button seeAll = new Button("See all stories →");
        seeAll.addClassName("btn-hero-outline");
        seeAll.addClickListener(e -> seeAll.getUI().ifPresent(ui -> ui.navigate(StoriesView.class)));
        headerRow.add(title, seeAll);

        Paragraph intro = new Paragraph("Sequence multiple photos with your own narration — a walkthrough of a "
                + "shoot, a trip, or the story behind one shot.");
        intro.addClassName("section-intro");

        Div card = new Div();
        card.addClassName("composer-card");

        String[] arrColumnsLastStory = {"title", "slug", "description", "user_id", "date_inserted",
                "datetime_story_created", "username", "name", "surname", "avatar_path", "story_id"
        };
        String sqlLastStory = "SELECT s.title, s.slug, s.`description`, s.user_id, s.date_inserted " +
                " , getDateDiffFromNow(s.date_inserted) AS datetime_story_created " +
                " , usr.username, usr.name, usr.surname, usr.avatar_path " +
                " , s.id AS story_id " +
                " FROM photo_stories s, dbuser usr " +
                " WHERE s.user_id = usr.userId AND s.story_visible_to = 'ALL' " +
                " ORDER BY s.date_inserted DESC LIMIT 1";

        List<Record> lstLastStory = getRecordsFromDb(sqlLastStory, arrColumnsLastStory);

        if (lstLastStory.isEmpty()) {
            Div empty = new Div("No stories published yet — be the first to tell yours.");
            empty.addClassName("story-preview-empty");
            card.add(empty);
        } else {
            Record story = lstLastStory.get(0);
            String strStoryId = story.getColumnData("story_id");
            String strTitle = story.getColumnData("title");
            String strSlug = story.getColumnData("slug");
            String strDescription = story.getColumnData("description");
            String strMemberUsername = story.getColumnData("username");
            String strName = story.getColumnData("name");
            String strSurname = story.getColumnData("surname");
            String strAvatarPath = story.getColumnData("avatar_path");
            String strDateCreated = story.getColumnData("datetime_story_created");
            String strFullName = (strName + " " + strSurname).trim();
            String strStoryTitle = (strTitle == null || strTitle.isBlank() || strTitle.equalsIgnoreCase("null"))
                    ? "Untitled story" : strTitle;

            Div cardHeader = new Div();
            cardHeader.addClassName("composer-card-header");

            Image avatar = genericView.getAvatarThumbImage(strAvatarPath, strFullName, "28px", "28px");
            avatar.addClassName("composer-avatar");
            Span storyName = new Span(strStoryTitle + " — " + strFullName);
            storyName.addClassName("composer-story-name");
            Div nameRow = new Div(avatar, storyName);
            nameRow.addClassName("composer-name-row");

            Span dateBadge = new Span(strDateCreated);
            dateBadge.addClassName("composer-draft-badge");
            cardHeader.add(nameRow, dateBadge);

            Div photoGrid = new Div();
            photoGrid.addClassName("composer-photo-grid");

            int intStoryId = 0;
            try {
                intStoryId = Integer.parseInt(strStoryId);
            } catch (NumberFormatException ignored) {
            }

            String[] arrColumnsStoryPhotos = {"name_new"};
            String sqlStoryPhotos = "SELECT pm.name_new FROM photo_stories_photo sp, photo_meta pm " +
                    " WHERE sp.story_id = " + intStoryId + " AND sp.photo_id = pm.id AND pm.visible_to = 'ALL' " +
                    " ORDER BY sp.inc ASC LIMIT 4";
            List<Record> lstStoryPhotos = getRecordsFromDb(sqlStoryPhotos, arrColumnsStoryPhotos);
            String strStoryImagePath = DIR_PHOTOS_SERVER + dirChar + subPathMedium;
            for (Record photoRecord : lstStoryPhotos) {
                Image photo = getImageThumbFromDb(photoRecord, strStoryImagePath);
                photo.addClassName("composer-photo");
                photoGrid.add(photo);
            }

            Div quote = new Div();
            quote.addClassName("composer-quote");
            if (strDescription != null && !strDescription.isBlank() && !strDescription.equalsIgnoreCase("null")) {
                quote.setText("\"" + strDescription + "\"");
            } else {
                quote.setVisible(false);
            }

            Div footerRow = new Div();
            footerRow.addClassName("composer-footer-row");
            String strPhotoCountLabel = lstStoryPhotos.size() + (lstStoryPhotos.size() == 1 ? " photo" : " photos");
            Span meta = new Span(strPhotoCountLabel + " · " + strDateCreated);
            meta.addClassName("composer-meta");

            RouteParam routeMember = new RouteParam("member", strMemberUsername);
            RouteParam routeStory = new RouteParam("story", strSlug);
            RouterLink btnView = new RouterLink("View Story", StoriesView.class, new RouteParameters(routeMember, routeStory));
            btnView.addClassName("btn-hero-cta");

            footerRow.add(meta, btnView);

            card.add(cardHeader, photoGrid, quote, footerRow);
        }

        section.add(headerRow, intro, card);
        return section;
    }

    private Div createFeatureGrid() {
        Div grid = new Div();
        grid.addClassName("feature-grid");

        grid.add(
                createFeatureCard("Showcase your work",
                        "Build a portfolio your whole community can discover and follow.",
                        GalleryView.class, "feature-icon-circle"),
                createFeatureCard("Get real feedback",
                        "Trade critiques with photographers who take the craft seriously.",
                        StoriesView.class, "feature-icon-square"),
                createFeatureCard("Learn & attend events",
                        "Workshops, meetups and guides from members further down the road.",
                        FestivalsView.class, "feature-icon-dot")
        );
        return grid;
    }

    private Div createFeatureCard(String title, String description, Class<? extends Component> route, String iconClass) {
        Div card = new Div();
        card.addClassName("feature-card");

        Div iconBox = new Div();
        iconBox.addClassName("feature-icon-box");
        Div iconShape = new Div();
        iconShape.addClassName(iconClass);
        iconBox.add(iconShape);

        H3 titleEl = new H3(title);
        titleEl.addClassName("feature-title");
        Div descEl = new Div(description);
        descEl.addClassName("feature-description");

        RouterLink link = new RouterLink();
        link.setRoute(route);
        link.addClassName("feature-card-link");
        link.add(iconBox, titleEl, descEl);

        card.add(link);
        return card;
    }

    private Div createCommunityGrid(String sqlRead, String[] arrColumnNames) {
        Div section = new Div();
        section.addClassName("community-section");

        Div headerRow = new Div();
        headerRow.addClassName("section-header-row");
        H2 title = new H2("Fresh from the community");
        Button viewAll = new Button("View all >");
        viewAll.addClassName("btn-hero-outline");
        RouteParameters routeParametersMonth = new RouteParameters("month-uploaded", STR_ALL_MONTHS);
        viewAll.addClickListener(e -> viewAll.getUI().ifPresent(ui -> ui.navigate(GalleryView.class,routeParametersMonth)));
        headerRow.add(title, viewAll);

        Div grid = new Div();
        grid.addClassName("community-grid");

        String strPathThumb = DIR_PHOTOS_SERVER + dirChar + subPathSmall;
        List<Record> lstRecords = getRecordsFromDb(sqlRead + " LIMIT 4", arrColumnNames);
        for (Record record : lstRecords) {
            Image image = getImageThumbFromDb(record, strPathThumb);
            image.addClassName("community-photo");

            String strName = nvl(record.getColumnData("name"));
            String strSurname = nvl(record.getColumnData("surname"));
            String strUsername = nvl(record.getColumnData("username"));
            String strAvatarPath = nvl(record.getColumnData("avatar_path"));
            String strUploadedAgo = nvl(record.getColumnData("date_inserted"));
            String strFullName = (strName + " " + strSurname).trim();
            if (strFullName.isEmpty()) {
                strFullName = !strUsername.isEmpty() ? "@" + strUsername : "Member";
            }

            Image avatarImg = genericView.getAvatarThumbImage(strAvatarPath, strFullName, "40px", "40px");
            AvatarItem avatarItem = new AvatarItem(strFullName, "", avatarImg);
            avatarItem.addClassName("community-avatar-item");

            Span timeAgoSpan = new Span(strUploadedAgo);
            timeAgoSpan.addClassName("community-time-ago");

            Div caption = new Div(avatarItem, timeAgoSpan);
            caption.addClassName("community-caption");

            RouterLink card = new RouterLink();
            card.setRoute(GalleryView.class, new RouteParameters(new RouteParam("member", strUsername)));
            card.addClassName("community-card");
            card.add(image, caption);
            grid.add(card);
        }

        section.add(headerRow, grid);
        return section;
    }

    private Div createLocationsSection() {
        Div section = new Div();
        section.addClassName("locations-section");

        Div headerRow = new Div();
        headerRow.addClassName("section-header-row");
        H2 title = new H2("Most Viewed Locations");
        Button viewAll = new Button("View all >");
        viewAll.addClassName("btn-hero-outline");
        RouteParameters routeParametersDestination = new RouteParameters("destination-type", "Cities");
        viewAll.addClickListener(e -> viewAll.getUI().ifPresent(ui ->
                ui.navigate(GalleryView.class, routeParametersDestination)));
        headerRow.add(title, viewAll);

        Div grid = new Div();
        grid.addClassName("locations-grid");

        String[] arrColumnsLocations = {"city_name", "country", "total_views", "total_likes", "total_ratings", "name_new"};
        String sqlMostViewedLocations =
                "SELECT d.city_name, d.country, " +
                " SUM(COALESCE(pv.view_count, 0)) AS total_views, " +
                " SUM(COALESCE(pl.like_count, 0)) AS total_likes, " +
                " SUM(COALESCE(pr.rating_count, 0)) AS total_ratings, " +
                " (SELECT pm2.name_new FROM photo_meta pm2 " +
                "    LEFT JOIN (SELECT photo_id, COUNT(*) AS vc FROM photo_view WHERE view_type IN ('List', 'Full') GROUP BY photo_id) pv2 " +
                "      ON pm2.id = pv2.photo_id " +
                "    WHERE pm2.destination_Id = d.id AND pm2.visible_to = 'ALL' " +
                "    ORDER BY COALESCE(pv2.vc, 0) DESC, pm2.date_inserted DESC LIMIT 1) AS name_new " +
                " FROM destination d " +
                " JOIN photo_meta pm ON pm.destination_Id = d.id AND pm.visible_to = 'ALL' " +
                " LEFT JOIN (SELECT photo_id, COUNT(*) AS view_count FROM photo_view WHERE view_type IN ('List', 'Full') GROUP BY photo_id) pv " +
                "   ON pm.id = pv.photo_id " +
                " LEFT JOIN (SELECT photo_id, COUNT(DISTINCT ip_address) AS like_count FROM photo_view WHERE view_type = 'Like' GROUP BY photo_id) pl " +
                "   ON pm.id = pl.photo_id " +
                " LEFT JOIN (SELECT photo_id, COUNT(*) AS rating_count FROM photo_rating GROUP BY photo_id) pr " +
                "   ON pm.id = pr.photo_id " +
                " GROUP BY d.id, d.city_name, d.country " +
                " ORDER BY total_views DESC " +
                " LIMIT 6";

        List<Record> lstLocations = getRecordsFromDb(sqlMostViewedLocations, arrColumnsLocations);
        String strPathThumb = DIR_PHOTOS_SERVER + dirChar + subPathSmall;
        for (Record record : lstLocations) {
            String strCity = nvl(record.getColumnData("city_name"));
            String strCountry = nvl(record.getColumnData("country"));
            String strViews = nvl(record.getColumnData("total_views"));
            String strLikes = nvl(record.getColumnData("total_likes"));
            String strRatings = nvl(record.getColumnData("total_ratings"));

            Image image = getImageThumbFromDb(record, strPathThumb);
            image.addClassName("location-photo");

            Div nameRow = new Div();
            nameRow.addClassName("location-name-row");
            Span citySpan = new Span(strCity);
            citySpan.addClassName("location-city");
            nameRow.add(citySpan);
            if (!strCountry.isEmpty()) {
                Span countrySpan = new Span(" · " + strCountry);
                countrySpan.addClassName("location-country");
                nameRow.add(countrySpan);
            }

            Div statsRow = new Div();
            statsRow.addClassName("location-stats-row");
            addLocationBadge(statsRow, strViews, "view", "views");
            addLocationBadge(statsRow, strLikes, "like", "likes");
            addLocationBadge(statsRow, strRatings, "rating", "ratings");

            Div caption = new Div(nameRow, statsRow);
            caption.addClassName("location-caption");

            RouterLink link = new RouterLink();
            link.setRoute(GalleryView.class, new RouteParameters(new RouteParam("destination", strCity)));
            link.addClassName("location-card");
            link.add(image, caption);

            grid.add(link);
        }

        if (lstLocations.isEmpty()) {
            Div empty = new Div("No location data yet.");
            empty.addClassName("story-preview-empty");
            grid.add(empty);
        }

        section.add(headerRow, grid);
        return section;
    }

    private void addLocationBadge(Div statsRow, String strCount, String singular, String plural) {
        if (strCount.isEmpty() || strCount.equals("0")) {
            return;
        }
        Span badge = new Span(strCount + " " + (strCount.equals("1") ? singular : plural));
        badge.addClassName("location-stat-badge");
        statsRow.add(badge);
    }

    private Div createCtaBanner(String usrName) {
        Div banner = new Div();
        banner.addClassName("cta-banner");

        Div textBlock = new Div();
        textBlock.addClassName("cta-banner-text");
        Div heading = new Div("Every photo has a story.");
        heading.addClassName("cta-banner-heading");
        Div sub = new Div("Upload and share in our community today.");
        sub.addClassName("cta-banner-sub");
        textBlock.add(heading, sub);

        Button btn = new Button("Upload Photos");
        btn.addClassName("btn-hero-cta");
        btn.addClickListener(click -> {
            if (usrName == null) {
                displayRegisterDialog();
            } else {
                btn.getUI().ifPresent(ui -> ui.navigate(UploadView.class));
            }
        });

        banner.add(textBlock, btn);
        return banner;
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
        /*btnNow.setIcon(VaadinIcon.SUN_RISE.create());*/
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


    private VerticalLayout loadLastNews() {
        VerticalLayout layoutLastNews = new VerticalLayout();
        layoutLastNews.addClassName("learnings-horizontal-panel");
        layoutLastNews.addClassName("section-header-row");


        H2 titleLastNews = new H2("Last Posted News");
        layoutLastNews.add(titleLastNews);
        List<LearningDto> news = learningService.getLatestLearnings(0, 3).getContent();
        for (LearningDto dto : news) {
            layoutLastNews.add(new LearningHorizontalPanel(dto));
        }
        return layoutLastNews;
    }

    private HorizontalLayout[] loadUploadedPhotos(String sqlRead, String[] arrColumnNames, boolean isEditable, boolean isThumbnails) {


        strPath = DIR_PHOTOS_SERVER + dirChar;
        String strPath;
        if (!isThumbnails) {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathSmall;
        } else {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;
        }

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

    private static String nvl(String s) {
        return s == null ? "" : s;
    }


}
