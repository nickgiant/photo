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
import com.photo.act.photo_act.services.PhotoFlickrService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.AvatarItem;
import com.photo.act.photo_act.views.components.DialogRegistration;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.HeaderFilterTabs;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.taefi.component.ToggleButtonGroup;
import org.w3c.dom.*;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLHeadElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.MainLayout.*;
import static com.photo.act.photo_act.views.MeView.subPathSmall;

@AnonymousAllowed

@Route(value = "") //":category?")
//@RouteAlias(value = "home") // empty on homepage
@RouteAlias(value = "home/:category?", layout = MainLayout.class)
//@Route(value = "learnings") //":category?")
//@RouteAlias(value = "learnings/category/:category?", layout = MainLayout.class)
//@RouteAlias(value = "learnings/tutor/:tutor?", layout = MainLayout.class)
//@RouteAlias(value = "learnings/category/:category/tutor/:tutor?", layout = MainLayout.class)

//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
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

    public static String subPathThumbs = "photo-thumbs";
    public static String subPathMedium = "photo-medium";
    public static String subPathUpload = "photo-upload";
    public static String subPathShow = "photo-show";

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
                    " ORDER BY lc.cat_order ASC "+
                    " LIMIT 6 ";


    String[] arrColLearningGenres = {"cat_title", "cat_title_type", "cat_type", "cat_location_count", "cat_title_count", "cat_description_min", "cat_description_big"};

    String sqlLearningGenres = //f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description  " +
            " SELECT lc.id, lc.cat_title, lc.cat_title_type, lc.cat_type, l.cat_genre_id, count(l.has_location) AS cat_location_count, count(lc.cat_title) AS cat_title_count, cat_description_min, cat_description_big " +

                    " FROM learnings l LEFT JOIN learnings_categories lc ON l.cat_genre_id = lc.id " +
                    " WHERE 1 = 1 " +
                    " AND lc.cat_type LIKE '%genre%' " +
                    " GROUP BY lc.cat_title " +
                    " ORDER BY lc.cat_order ASC "+
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

    public HomeView(RecordService recordService) {
        this.recordService = recordService;

        utilsDate = new UtilsDate();
        genericView = new GenericView(recordService, 1);

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
                +" l.id, l.title,  l.subtitle, l.picture, l.category_id, l.cat_genre_id, l.format, l.url, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, DATE_FORMAT(l.published, '%Y') AS year_published,  l.userId_post, l.dateInsert, getDateDiffFromNow(l.dateInsert) AS date_inserted "
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
                //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
//                "                 ( case " +
//                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '00:06:00' THEN 'almost now'" +
//                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '00:18:00' THEN '10 minutes ago'" +
//                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '00:48:00' THEN '30 minutes ago'" +
//                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '01:37:00' THEN 'an hour ago'" +
//                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '02:40:00' THEN 'two hours ago'" +
//                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '03:42:00' THEN 'three hours ago'" +
//                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '04:28:00' THEN 'four hours ago'" +
//                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '05:35:00' THEN 'five hours ago'" +
//                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '06:35:00' THEN 'six hours ago'" +
//                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '07:35:00' THEN 'seven hours ago'" +
//                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '08:35:00' THEN 'eight hours ago'" +
//                "                when DATE(DATE(pm.date_inserted) + 1) = DATE(NOW()) then CONCAT('Yesterday at ' , DATE_FORMAT(pm.date_inserted, '%H:%i %p') )" +
//                "                when DATE(DATE(pm.date_inserted) + 2) = DATE(NOW())  then CONCAT('Last ' , DATE_FORMAT(pm.date_inserted, '%W at %H:%i %p') )" +
//                "                when DATE(DATE(pm.date_inserted) + 6) >= DATE(NOW())  then CONCAT('Last ' , DATE_FORMAT(pm.date_inserted, '%W') )" +
//                "                when DATE(DATE(pm.date_inserted) + 6) < DATE(NOW())  then CONCAT('' , DATE_FORMAT(pm.date_inserted, '%D %M %Y') )" +
//                "                ELSE DATE_FORMAT(pm.date_inserted, '%D %M %Y') " +
//                "              END ) " +
                " , getDateDiffFromNow(pm.date_inserted) AS date_inserted " +
                " , usr.username, usr.name, usr.surname, usr.avatar_path, DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
                " FROM dbuser usr, photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id ";
//                    " WHERE pm.hostname like '"+hostname+"' "+
//                    " ORDER BY pm.title ASC ";
        String sqlGalleryAll = sqlReadGallery + " WHERE pm.visible_to = 'ALL' " +
                " AND usr.userId = pm.uploaderId";
//        if(!strDestination.equalsIgnoreCase(STR_ALL_DESTINATIONS)) {
//            sqlGalleryAll = sqlGalleryAll + " AND d.city_name LIKE '" + strDestination + "' ";
//        }
        sqlGalleryAll = sqlGalleryAll + " ORDER BY pm.date_inserted DESC, pm.title ASC, pm.meta_date DESC, pm.name_new ASC ";

//        ArrayList<Image> lstImage = loadImagesFromDbToCarousel(sqlGalleryAll + " LIMIT 10 ", arrColumnNamesGallery, false, false);

        String[] arrColsUploadsGrouped = {"Month", "Photos"};

        String sqlUploadsGrouped = "SELECT DATE_FORMAT(pm.date_inserted, '%M %Y') as 'Month', COUNT(DATE_FORMAT(pm.date_inserted, '%M %Y')) AS 'Photos'" +
                " FROM photo_meta pm " +
                " WHERE visible_to LIKE 'all' " +
                " GROUP BY DATE_FORMAT(pm.date_inserted, '%M %Y') " +
                " ORDER BY DATE_FORMAT(pm.date_inserted, '%Y %m') ";

        H1 titlePage = new H1(APP_NAME);
        Span subTitle = new Span("[ Network and Act around Photography ]");

        Header siteHeader = new Header(titlePage, subTitle);
        siteHeader.addClassNames(Width.FULL);

        verticalLayout.add(siteHeader);

        Div divMainImage = new Div();
        Image mainImage = new Image();
        String strMainImagePath = DIR_PHOTOS_SERVER + dirChar + "photographerM.jpg";

        final StreamResource imageMainResource = new StreamResource("streamResource", () -> {
            try {

                Path path = Paths.get(strMainImagePath);
                File file = path.toFile();
                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
//                logErrorInDb(e, "GalleryImageViewCard StreamResource FileNotFoundException", hostname, userId, strUserName, publicIp, sessionCreation, file.getAbsolutePath());
                // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                logger.error(e.getMessage());
            }
            return null;
        });


        mainImage.setSrc(imageMainResource);
        mainImage.setAlt("sketch image of a photographer");
        mainImage.setHeight("24rem");
        mainImage.setWidth("auto");
        mainImage.getStyle().setBorderRadius("40px");
        mainImage.getStyle().setPadding("10px");

        divMainImage.add(mainImage);


        Div div1 = new Div("We are a community site, with members exchanging info and links in order to improve our skills in photography!");
        Div div2 = new Div("Currently, we share info about events and learnings. Of course, we also have space for our photos and albums.");

        StreamResource imageResourceMember = new StreamResource("user-profile-icon.svg",
                () -> getClass()
                        .getResourceAsStream("/icons/user-profile-icon.svg"));
        SvgIcon svgMember = new SvgIcon(imageResourceMember);
        Button btnRegister = new Button("Register");
        btnRegister.setIcon(svgMember);
        btnRegister.addClassName("btn-register");
//        btnSuggestEvent.setIcon(svgComments);
        btnRegister.addClickListener(click -> {
            displayRegisterDialog();
        });


        HorizontalLayout layoutUserBtns = new HorizontalLayout();
        String usrName = genericView.checkIfAuthUserName();
        if (usrName == null) {
            layoutUserBtns.add(btnRegister);
        } else {

            mainImage.setHeight("16rem");
            mainImage.setWidth("auto");
            layoutUserBtns.add(genericView.getAuthUserPanel(usrName));
        }

        verticalLayout.add(divMainImage, div1, div2, layoutUserBtns);

        Div divLearningTopics = loadLearningTopics(sqlLearningTopics, arrColLearningTopics);
        VerticalLayout layoutLearningTopics = new VerticalLayout();
        H2 titleLearnTopics = new H2("Learning Categories");
        Button btnMoreLearnings = new Button("View All Learnings");
        btnMoreLearnings.addClickListener(click->{
            btnMoreLearnings.getUI().ifPresent(ui ->
                    ui.navigate(LearningsView.class)
            );
        });
        layoutLearningTopics.add(titleLearnTopics, divLearningTopics,btnMoreLearnings);
        layoutLearningTopics.addClassNames(Width.FULL,AlignItems.CENTER,JustifyContent.CENTER,Padding.MEDIUM);
        layoutLearningTopics.addClassName("page-section");
        verticalLayout.add(layoutLearningTopics);


        Div layoutLearningGenres = loadLearningsAboutGenres(sqlLearningGenres, arrColLearningGenres);

        H2 titleLearnGenres = new H2("Learning Photo Genres");
        Button btnMoreLearningGenres = new Button("View All Learnings");
        btnMoreLearningGenres.addClickListener(click->{
            btnMoreLearningGenres.getUI().ifPresent(ui ->
                    ui.navigate(LearningsView.class)
            );
        });
        verticalLayout.add(titleLearnGenres, layoutLearningGenres,btnMoreLearningGenres);


//        H3 titleCarousel = new H3("10 Recently Uploaded Photos:");
//        verticalLayout.add(titleCarousel, getCarousel(lstImage));

        VerticalLayout layoutLastLearnings = new VerticalLayout();
        layoutLastLearnings.addClassNames(Width.FULL,AlignItems.CENTER,JustifyContent.CENTER,Padding.MEDIUM);
        layoutLastLearnings.addClassName("page-section");

        H2 titleLastLearnings = new H2("Last Posted Learnings");
        Div divLastLearnings = loadLastLearnings(sqlLearningsRead, arrColumnsLearning);
        layoutLastLearnings.add(titleLastLearnings, divLastLearnings);
        verticalLayout.add(layoutLastLearnings);

        H2 titleGraphLastPhotos = new H2("Photo Uploads");
        verticalLayout.add(titleGraphLastPhotos, loadGraphUploads(sqlUploadsGrouped, arrColsUploadsGrouped));

        VerticalLayout layoutLastPhotoUploads = new VerticalLayout();
        layoutLastPhotoUploads.addClassNames(Width.FULL,AlignItems.CENTER,JustifyContent.CENTER,Padding.MEDIUM);
        layoutLastPhotoUploads.addClassName("page-section");
        H2 titleLastPhotos = new H2("Last Photos Uploaded");
        layoutLastPhotos.addClassNames(Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.SMALL);
        layoutLastPhotos.addClassName("container-uploaded-lines");

        HorizontalLayout layoutPhotosButton = new HorizontalLayout();
        layoutPhotosButton.addClassNames(Margin.NONE, Padding.SMALL, AlignItems.CENTER,JustifyContent.EVENLY);
        Button btnPhotosA = new Button("5");
        String finalSqlGalleryAll = sqlGalleryAll;
        btnPhotosA.addClickListener(e->{
            layoutLastPhotos.removeAll();
            layoutLastPhotos.add(loadUploadedPhotos(finalSqlGalleryAll + " LIMIT 5 ", arrColumnNamesGallery, false, false));
        });
        Button btnPhotosB = new Button("10");
        btnPhotosB.addClickListener(e->{
            layoutLastPhotos.removeAll();
            layoutLastPhotos.add(loadUploadedPhotos(finalSqlGalleryAll + " LIMIT 10 ", arrColumnNamesGallery, false, false));

        });
        Button btnPhotosC = new Button("20");
        btnPhotosC.addClickListener(e->{
            layoutLastPhotos.removeAll();
            layoutLastPhotos.add(loadUploadedPhotos(finalSqlGalleryAll + " LIMIT 20 ", arrColumnNamesGallery, false, false));
        });

        layoutPhotosButton.add(btnPhotosA,btnPhotosB,btnPhotosC);
        layoutLastPhotos.add(loadUploadedPhotos(sqlGalleryAll + " LIMIT 5 ", arrColumnNamesGallery, false, false));

        Button btnMorePhotos = new Button("More Photos");
        btnMorePhotos.addClickListener(click->{
            btnMorePhotos.getUI().ifPresent(ui ->
                    ui.navigate(GalleryView.class)
            );
        });
        layoutLastPhotoUploads.add(titleLastPhotos,layoutPhotosButton,layoutLastPhotos,btnMorePhotos);
        verticalLayout.add(layoutLastPhotoUploads);

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
        addClassNames("home-view");
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


        VerticalLayout layoutWeather = genericView.getWeatherCurrent(city, country);

//        HorizontalLayout  layoutPhotos = getDestinationPhotos(city,4);

        VerticalLayout layoutResults = new VerticalLayout();
        layoutResults.add(layoutWeather);

        return layoutResults;
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
            if(!strCategory.isEmpty()){
                divCategory.setText("Category: "+strCategory);
            }else{
                divCategory.setText("Genre: "+strCatGenre);
            }
            Div divDuration = new Div("Duration: "+strDuration);
            Div divDateInserted = new Div("Inserted: "+strDateInserted);

            H4 divTutor = new H4(strTutorName);
            divTutor.addClassNames(FontWeight.BOLD, FontSize.LARGE);

            HorizontalLayout layoutHor1 = new HorizontalLayout();
            layoutHor1.addClassNames(Width.FULL, AlignItems.CENTER,JustifyContent.BETWEEN);
            layoutHor1.add( divDuration, divDateInserted);

            HorizontalLayout layoutHor2 = new HorizontalLayout();
            layoutHor2.addClassNames(Width.FULL, AlignItems.CENTER,JustifyContent.BETWEEN);
            layoutHor2.add( divTutor, divCategory);

            layoutLearning.add(h4Title,layoutHor1, layoutHor2);

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


            Image imgAvatarMedium = genericView.getAvatarImage(strAvatarPath, strName + " " + strSurname, "80px", "80px");
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
            layoutPhotoUploaded[r]= layoutPhotoUploadedPanel;
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
        VerticalLayout layoutHeaderParameters = headerFilterTabs.getHeader(strHeader, strSubHeader, strSection, headerContainerSecondary);

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

    private VerticalLayout loadGraphUploads(String sqlRead, String[] arrColumnNames) {

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        Series<Object> data = new Series<>();
        data.setName("Photos");
        Object[] intPhotos = new Object[lstRecords.size()];
        String[] strMonths = new String[lstRecords.size()];
        for (int r = 0; r < lstRecords.size(); r++) {

            intPhotos[r] = Integer.parseInt(lstRecords.get(r).getColumnData("photos"));
            strMonths[r] = lstRecords.get(r).getColumnData("month");
        }

        data.setData(intPhotos);

        VerticalLayout layoutUploads = new VerticalLayout();
        layoutUploads.addClassName("chart-panel");
        layoutUploads.addClassNames(AlignItems.CENTER);
        if (isMobile) {
            layoutUploads.setWidth("97%");
        } else {
            layoutUploads.setWidth("82%");
            layoutUploads.setMaxWidth("1100px");
        }

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
//        layoutGraph1.setMinHeight("190px");
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

//        RouteParam routeUploader = new RouteParam("member", strUploader);
//        RouterLink linkUploader = new RouterLink(strUploader, GalleryView.class,new RouteParameters(routeUploader));
//
//        RouteParam routeDestination = new RouteParam("destination", strCityName);
//        RouterLink linkDestination = new RouterLink(strCityName, GalleryView.class,new RouteParameters(routeDestination));
//
//        ArrayList<RouterLink> lstRouterLinks =new ArrayList<>();
//        lstRouterLinks.add(linkDestination);

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


    private void displayRegisterDialog() {
        DialogRegistration dialogRegister = new DialogRegistration(isMobile, "", sessionCreation, hostname, publicIp, recordService,
                section, "register-from-home-view");
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
