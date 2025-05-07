package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.ResponsiveBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.HorizontalAlign;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.AvatarItem;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.HeaderFilterTabs;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.MainLayout.*;

//@PageTitle("Image Gallery")
//@RouteAlias("") // empty on homepage
@Route(value = "learnings") //":category?")
@RouteAlias(value = "learnings/category/:category?", layout = MainLayout.class)
@RouteAlias(value = "learnings/genre/:genre?", layout = MainLayout.class)
@RouteAlias(value = "learnings/tutor/:tutor?", layout = MainLayout.class)
@RouteAlias(value = "learnings/title/:title?", layout = MainLayout.class)
//@RouteAlias(value = "learnings/tutors/:tutor?", layout = MainLayout.class) // when tutors team
//@RouteAlias(value = "learnings/category/:category/tutor/:tutor?", layout = MainLayout.class)

//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class LearningsView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";

    private static final Logger logger = LoggerFactory.getLogger(LearningsView.class);

    private VerticalLayout verticalLayout;
    private VerticalLayout filtersColumn;
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String section = SECTION_LEARNINGS;
    //    private String forMemberName;
    private RecordService recordService;
    private String strHeader;

    private String category;
    private String genre;
    private String tutor;
    private String title;

    //private String strOrderBy;


    private String dirChar = "/"; //FileSystems.getDefault().getSeparator();

    public static String STR_ALL_TUTORS = "all-tutors";
    public static String STR_ALL_CATEGORIES = "all-categories";
    public static String STR_ALL_GENRES = "all-genres";
    public static String STR_ALL_TITLES = "all";

    public static String STR_ORDER_BY_NEWEST = "newest";
    public static String STR_ORDER_BY_OLDER = "older";

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
            "  pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
            //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
            " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id ";

    String[] arrColumnsLearningTypes = {"cat_title", "cat_title2", "cat_title_type", "cat_title_type2", "cat_type", "cat_type2", "cat_type_count", "cat_type_count2", "cat_description_min"};

    String sqlLearningTypes = "SELECT "
            + " lc.cat_title, lc.cat_title_type, lc.cat_type, lc.cat_description_min "
//            + " , lc2.cat_title AS cat_title2, lc2.cat_title_type AS cat_title_type2, lc2.cat_type AS cat_type2, count (lc2.cat_type) AS cat_type_count2 "
//            + " l.id, l.title, l.picture, l.section , l.category, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert "
//            + ", l.tutor_id, l.tutor_id_team, t.tutor_name, t.website, t.url_fb, t.url_yt, t.url_insta, t.url_flickr, t.url_wikipedia, t.url_ref1, t.url_ref2, t.url_ref3, t.city_base, t.country_base, t.userIdInsert, t.username, t.date_inserted "
//            + " FROM learnings_categories lc2 RIGHT JOIN learnings l ON lc2.id = l.category_id2, learnings_categories lc " // "LEFT JOIN learnings_categories lc ON lc.id = l.category_id "
//            + " FROM learnings l, learnings_categories lc "
            + " FROM learnings_categories lc "
            + " WHERE 1 = 1 "
//            + " WHERE 1 = 1 AND lc.id = l.category_id "
            + " AND lc.cat_type NOT LIKE '%genre%' "
            + " GROUP BY lc.cat_type "
            + " ORDER BY lc.cat_order ASC ";


    String[] arrColLearningCategories = {"cat_title", "cat_title2", "cat_title_type", "cat_type", "cat_description_min", "cat_description_big", "cat_count"};

    String sqlLearningCategoriesRead = "SELECT  " //f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description  " +
            + " lc.cat_title, lc.cat_title_type, lc.cat_type, lc.cat_description_min, count (lc.cat_title) AS cat_count, "
            + " lc2.cat_title AS cat_title2, lc2.cat_title_type AS cat_title_type2, lc2.cat_type AS cat_type2, count (lc2.cat_title) AS cat_count2 "
//            + " l.id, l.title, l.picture, l.section , l.category, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert "
//            + ", l.tutor_id, l.tutor_id_team, t.tutor_name, t.website, t.url_fb, t.url_yt, t.url_insta, t.url_flickr, t.url_wikipedia, t.url_ref1, t.url_ref2, t.url_ref3, t.city_base, t.country_base, t.userIdInsert, t.username, t.date_inserted "
            + " FROM learnings_categories lc2 RIGHT JOIN learnings l ON lc2.id = l.category_id2 LEFT JOIN learnings_categories lc ON lc.id = l.category_id "
            + " WHERE 1 = 1 "
            + " AND ( lc.cat_type LIKE '%genre%') "
            + " GROUP BY lc.cat_title ORDER BY lc.cat_order ASC ";


    String[] arrColumnsLearning = {"title", "picture", "cat_title", "cat_title2", "cat_title_type", "cat_title_type2", "cat_type", "format", "url", "artists_ref", "description", "duration", "pages", "published", "year_published",
            "category_id", "category_id2", "tutor_name", "website", "url_fb", "url_yt", "url_insta", "url_flickr", "url_wikipedia", "url_ref1", "url_ref2", "url_ref3",
            "dateInsert", "date_created",
            "username", "nameOfUser", "avatar_path", "member_since"};

    // learnings: l.id, l.title, l.picture, l.section , l.category, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert
// learnings_tutor:  lt.id, lt.tutor_name, lt.learnings_team_id, lt.website, lt.url_fb, lt.url_yt, lt.url_insta, lt.url_flickr, lt.url_wikipedia, lt.url_ref1, lt.url_ref2, lt.url_ref3, lt.url_flckr, lt.city_base, lt.country_base, lt.userIdInsert, lt.username, lt.date_inserted
    String sqlLearningsRead = "SELECT  " //f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description  " +
            + " lc.cat_title, lc.cat_title_type, lc.cat_type "
            + " , lc2.cat_title AS cat_title2, lc2.cat_title_type AS cat_title_type2 "
            + " , l.id, l.title, l.picture, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published "
            + " , DATE_FORMAT(l.published, '%Y') AS year_published "
            + " , l.dateInsert, " +
            "                 ( case " +
            "                WHEN TIMEDIFF(NOW(), l.dateInsert) <= '00:06:00' THEN 'almost now'" +
            "                WHEN TIMEDIFF(NOW(), l.dateInsert) <= '00:18:00' THEN '10 minutes ago'" +
            "                WHEN TIMEDIFF(NOW(), l.dateInsert) <= '00:48:00' THEN '30 minutes ago'" +
            "                WHEN TIMEDIFF(NOW(), l.dateInsert) <= '01:37:00' THEN 'an hour ago'" +
            "                WHEN TIMEDIFF(NOW(), l.dateInsert) <= '02:40:00' THEN 'two hours ago'" +
            "                WHEN TIMEDIFF(NOW(), l.dateInsert) <= '03:42:00' THEN 'three hours ago'" +
            "                WHEN TIMEDIFF(NOW(), l.dateInsert) <= '04:28:00' THEN 'four hours ago'" +
            "                WHEN TIMEDIFF(NOW(), l.dateInsert) <= '05:35:00' THEN 'five hours ago'" +
            "                WHEN TIMEDIFF(NOW(), l.dateInsert) <= '06:35:00' THEN 'six hours ago'" +
            "                WHEN TIMEDIFF(NOW(), l.dateInsert) <= '07:35:00' THEN 'seven hours ago'" +
            "                WHEN TIMEDIFF(NOW(), l.dateInsert) <= '08:35:00' THEN 'eight hours ago'" +
            "                when DATE(l.dateInsert) = DATE(NOW())  then CONCAT('today at ' , DATE_FORMAT(l.dateInsert, '%H:%i %p') )" +
            "                when DATE(DATE(l.dateInsert) + 1) = DATE(NOW()) then CONCAT('Yesterday on ' , DATE_FORMAT(l.dateInsert, '%H:%i %p') )" +
            "                when DATE(DATE(l.dateInsert) + 2) = DATE(NOW())  then CONCAT('Last ' , DATE_FORMAT(l.dateInsert, '%W on %H:%i %p') )" +
            "                when DATE(DATE(l.dateInsert) + 6) >= DATE(NOW())  then CONCAT('Last ' , DATE_FORMAT(l.dateInsert, '%W') )" +
            "                when DATE(DATE(l.dateInsert) + 6) < DATE(NOW())  then CONCAT('' , DATE_FORMAT(l.dateInsert, '%D of %M %Y') )" +
            "                ELSE DATE_FORMAT(l.dateInsert, '%D %M %Y') " +
            "              END ) " +
            " AS date_created "
            + " , l.tutor_id, l.tutor_id_team, l.category_id, l.category_id2, t.tutor_name, t.website, t.url_fb, t.url_yt, t.url_insta, t.url_flickr, t.url_wikipedia, t.url_ref1 " +
            " , t.url_ref2, t.url_ref3, t.city_base, t.country_base, t.userIdInsert, t.username, t.date_inserted" +
            " , l.userId_post " +
            " , usr.username, usr.nameOfUser, usr.avatar_path, DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since "
            + " FROM learnings_categories lc, learnings l LEFT JOIN learnings_categories lc2 ON lc2.id = l.category_id2, tutor t, dbuser usr  "
            + " WHERE 1 = 1 "
            + " AND l.userId_post = usr.userId"
            + " AND lc.id = l.category_id AND l.tutor_id = t.id ";

    String sqlLearningsReadOrderBy;

    private UtilsDate utilsDate;
    private String sessionDateTime;
    private String strUrlRequestToBeLogged;
    private GenericView genericView;

    private String strOS;
    private String strBrowser;


    public LearningsView(RecordService recordService) {
        this.recordService = recordService;

        utilsDate = new UtilsDate();
        genericView = new GenericView();


        constructUI();

    }


    @Override
    public String getPageTitle() {
        return strHeader;
    }

    @Override
    public void beforeEnter(@OptionalParameter BeforeEnterEvent event) {
        category = event.getRouteParameters().get("category").orElse(STR_ALL_CATEGORIES);
        tutor = event.getRouteParameters().get("tutor").orElse(STR_ALL_TUTORS);
        title = event.getRouteParameters().get("title").orElse(STR_ALL_TITLES);
        genre = event.getRouteParameters().get("genre").orElse(STR_ALL_GENRES);

//        strOrderBy = event.getRouteParameters().get("order").orElse(STR_ORDER_BY_NEWEST);

        getUserClientInfo();

        userId = 1;
        strUsername = "visitor-user";

        VerticalLayout layoutHeaderParameters;
        verticalLayout.removeAll();

        if (!category.equalsIgnoreCase(STR_ALL_CATEGORIES)) {
            layoutHeaderParameters = loadHeader("Learnings", "", category);
            VerticalLayout layoutResults = loadResults(0);
            verticalLayout.add(layoutResults);
        } else if (!genre.equalsIgnoreCase(STR_ALL_GENRES)) {
            layoutHeaderParameters = loadHeader("Learnings", "", genre);
            VerticalLayout layoutResults = loadResults(0);
            verticalLayout.add(layoutResults);
        }else if (!title.equalsIgnoreCase(STR_ALL_TITLES)){
            layoutHeaderParameters = loadHeader("Learnings", "", "");
            VerticalLayout layoutResults = loadResults(0);
            verticalLayout.add(layoutResults);
        } else if (category.equalsIgnoreCase(STR_ALL_CATEGORIES) || genre.equalsIgnoreCase(STR_ALL_GENRES)) {
            layoutHeaderParameters = loadHeader("Learnings", "", "");
            VerticalLayout layoutResults = loadResults(25);
            verticalLayout.add(layoutResults);
        } else {
            layoutHeaderParameters = loadHeader("Learnings", "", "");
            logger.warn(category + "  " + tutor + "  " + genre);
        }

        HorizontalLayout layoutContent = new HorizontalLayout();
        layoutContent.addClassNames(Width.FULL,
                AlignItems.START, JustifyContent.CENTER,
                Padding.LARGE, Margin.NONE,
                Gap.XSMALL
        );
        filtersColumn.removeAll();
        filtersColumn.setMaxWidth("290px");
        verticalLayout.setMaxWidth("1040px");
        filtersColumn.add(loadFiltersColumn(sqlLearningTypes, arrColumnsLearningTypes));
        filtersColumn.add(loadFiltersGenresColumn(sqlLearningCategoriesRead, arrColLearningCategories));

        layoutContent.add(filtersColumn, verticalLayout);
//        layoutContent.setMaxWidth("1200px");

        this.removeAll();
        this.add(layoutHeaderParameters);
        this.add(layoutContent);
        this.add(genericView.loadFooter(isMobile));

        logVisitorToDb();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String o) {
//        category = o;//beforeEvent.getRouteParameters().get("category").orElse("pictures");
    }

    private void constructUI() {
        this.addClassNames(Overflow.HIDDEN, Width.FULL,
                // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                Margin.NONE,
                Padding.NONE,
                Gap.MEDIUM,
                //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
                AlignItems.CENTER, JustifyContent.CENTER
        );

        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        hostname = inetAddress.getHostName();
        hostAddress = inetAddress.getHostAddress();
        canonicalHostname = inetAddress.getCanonicalHostName();

        if (hostname.equalsIgnoreCase(HOSTNAME_LAPTOP)) {
            DIR_PHOTOS_SERVER = "/home/mike/Pictures/lazy-photos";
        } else if (hostname.equalsIgnoreCase("piot")) {
            DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
        } else {
            DIR_PHOTOS_SERVER = "/home/sammy/lazy-photos";

        }

        filtersColumn = new VerticalLayout();
        if (isMobile) {
            filtersColumn.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.NONE,
                    Padding.Top.XSMALL,
                    Gap.XSMALL,
                    AlignItems.START, JustifyContent.CENTER,
                    LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
//                Background.CONTRAST_5,
                    TextAlignment.CENTER
            );
        } else {
            filtersColumn.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.NONE,  //<---
                    Padding.Top.MEDIUM,
                    Gap.SMALL,
                    AlignItems.START, JustifyContent.CENTER,
                    LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
//                Background.CONTRAST_5,
                    TextAlignment.CENTER
            );
        }


        verticalLayout = new VerticalLayout();
        verticalLayout.setId("verticalLayout");
        if (isMobile) {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, //Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.NONE,
                    Padding.Top.XSMALL,
//                    Gap.MEDIUM,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        } else {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, //Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.SMALL,  //<---
                    Padding.Top.XSMALL,
//                    Gap.LARGE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        }
        verticalLayout.addClassName("learnings-view");
        this.setWidthFull();
    }

    private VerticalLayout loadResults(int intLimit) {

        String strWhereSubClause = "";


        if (!title.isEmpty() && !title.equalsIgnoreCase(STR_ALL_TITLES)) {
            strWhereSubClause = strWhereSubClause + " AND l.title LIKE '" + title + "' ";
        } else if (!tutor.isEmpty() && !tutor.equalsIgnoreCase(STR_ALL_TUTORS)) {
            strWhereSubClause = strWhereSubClause + " AND t.tutor_name LIKE '" + tutor + "' ";
        } else if (!category.isEmpty() && !category.equalsIgnoreCase(STR_ALL_CATEGORIES)) {
                strWhereSubClause = strWhereSubClause + " AND ( lc.cat_type LIKE '" + category + "' OR lc2.cat_type LIKE '" + category + "') ";
        } else if (!genre.isEmpty() && !genre.equalsIgnoreCase(STR_ALL_GENRES)) {
                strWhereSubClause = strWhereSubClause + " AND ( lc.cat_title LIKE '" + genre + "' OR lc2.cat_title LIKE '" + genre + "') ";
        }

        sqlLearningsReadOrderBy = " ORDER BY l.dateInsert DESC";

        String sqlLimit ="";
        if(intLimit==0){

        } else {
            sqlLimit = " LIMIT " + intLimit;
        }

        String sqlRead = sqlLearningsRead + strWhereSubClause + sqlLearningsReadOrderBy+sqlLimit;

        strPath = DIR_PHOTOS_SERVER + dirChar;

        VerticalLayout layoutLearnings = new VerticalLayout();
        if (isMobile) {
            layoutLearnings.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE, Padding.NONE,
                    Gap.MEDIUM,
                    //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                    //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        } else {
            layoutLearnings.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.SMALL, // <---
//                    Padding.Top.NONE,
//                    Padding.XLARGE,
                    Gap.XLARGE,
                    //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                    //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
//            layoutLearnings.getStyle().set("gap","3rem");
        }


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnsLearning);
        logger.info(" record size: " + lstRecords.size());

        for (int r = 0; r < lstRecords.size(); r++) {

            Record rec = lstRecords.get(r);
            String strTutorIdTeam = rec.getColumnData("tutor_id_team");
            int intTutorIdTeam = 1;
            if (strTutorIdTeam != null && !strTutorIdTeam.isEmpty()) {
                intTutorIdTeam = Integer.parseInt(strTutorIdTeam);
            }

            String strTutor = rec.getColumnData("tutor_name");

            String strTeamName = rec.getColumnData("team_name");

            // teamId == 1 when is individual, when not 1 is a team
            if (intTutorIdTeam == 1) {
                layoutLearnings.add(getLearningsItem(rec, false));
            } else {
                layoutLearnings.add(getLearningsItem(rec, true));
            }
        }


        return layoutLearnings;
    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSection) {

        this.strHeader = strHeader;
//        HorizontalLayout headerContainerMaster = new HorizontalLayout();
//        if (isMobile) {
//            headerContainerMaster.addClassNames(
//                    AlignItems.CENTER, JustifyContent.BETWEEN,
//                    Overflow.HIDDEN,// Width.FULL,
//                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.MEDIUM,
//                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
//                    //   Background.CONTRAST_5,
//                    BorderRadius.NONE
//            );
//        } else {
//            headerContainerMaster.addClassNames(
//                    AlignItems.CENTER, JustifyContent.BETWEEN,
//                    Overflow.HIDDEN, //Width.FULL,
//                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.MEDIUM,
//                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
//                    //   Background.CONTRAST_5,
//                    BorderRadius.LARGE
//            );
//        }

//        VerticalLayout headerTextContainer = new VerticalLayout();
//        headerTextContainer.addClassNames(
//                AlignItems.CENTER, JustifyContent.START,
//                Margin.NONE, Padding.NONE,
//                Gap.XSMALL);
//
//        H2 header = new H2(strHeader);
//        header.addClassNames(
//                AlignItems.CENTER, JustifyContent.START,
//                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.LARGE, FontWeight.BOLD, TextColor.SECONDARY);
////        header.getStyle().set("font-family", "Times-New-Roman, serif");
//
//        Div subheader = new Div(strSubHeader);
//        subheader.addClassNames(
//                AlignItems.CENTER, JustifyContent.START,
//                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.SMALL, TextColor.SECONDARY);
//
//
//        headerTextContainer.add(header, subheader);

//        Select<String> sortBy = new Select<>();
//        sortBy.setLabel("Sort by");
//        sortBy.setItems("Most Viewed", "Least Viewed", "Most Favourite", "Least Favourite", "Newest First", "Oldest First", "Most Liked", "Least Liked");
//        sortBy.setValue("Most Viewed");


        Div headerContainer = new Div();
        if (isMobile) {
            headerContainer.addClassNames(
                    AlignItems.START, JustifyContent.CENTER,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        } else {
            headerContainer.addClassNames(
                    AlignItems.START, JustifyContent.CENTER,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        }
//        headerContainer.addClassName("header-layout");

        VerticalLayout layoutSortByAll = new VerticalLayout();
        layoutSortByAll.addClassNames(
                LumoUtility.AlignItems.CENTER, JustifyContent.START,
                Margin.NONE, Padding.SMALL,
                LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
                Background.CONTRAST_5,
                TextAlignment.CENTER
        );
        layoutSortByAll.getStyle().setMaxWidth("125px");


        VerticalLayout layoutSortBy = new VerticalLayout();
        layoutSortBy.addClassNames(
                LumoUtility.AlignItems.CENTER, JustifyContent.START,
                Margin.NONE, Padding.SMALL,
                LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
//                Background.CONTRAST_5,
                TextAlignment.CENTER
        );
        layoutSortBy.addClassName("header-layout-sort");

//        RouteParam routeOrderNewest = new RouteParam("order", STR_ORDER_BY_NEWEST);
//        RouterLink linkOrderNewest = new RouterLink("Newest First", LearningsView.class, new RouteParameters(routeOrderNewest));
//
//        RouteParam routeOrderOlder = new RouteParam("order", STR_ORDER_BY_OLDER);
//        RouterLink linkOrderOlder = new RouterLink("Older First", LearningsView.class, new RouteParameters(routeOrderOlder));
////        layoutSortBy.add(linkOrderNewest, linkOrderOlder);

        List lstSortBy = new ArrayList();
        lstSortBy.add("Newest First");
        lstSortBy.add("Older First");

        RadioButtonGroup radioSortBy = new RadioButtonGroup();
        radioSortBy.setItems(lstSortBy);
        radioSortBy.setValue(lstSortBy.get(0));

        Div divSortTitle = new Div("Sort");
        layoutSortByAll.add(divSortTitle, radioSortBy);

        VerticalLayout layoutFiltersAll = new VerticalLayout();
        layoutFiltersAll.addClassNames(
                LumoUtility.AlignItems.CENTER, JustifyContent.START,
                Margin.NONE, Padding.SMALL,
                LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
//                Background.CONTRAST_5,
                TextAlignment.CENTER
        );

        Div layoutFilters = new Div();
        if (isMobile) {
            layoutFilters.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.XSMALL,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE);
        } else {
            layoutFilters.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.SMALL,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }
        layoutFilters.addClassName("header-layout-filters");

//        String sqlLearningCategories;
//        if (type.isEmpty() || type.equalsIgnoreCase(STR_ALL_TYPES)) {
//            sqlLearningCategories = sqlLearningCategoriesRead
//                    + sqlLearningCategoriesReadGroup;
//        } else {
//            sqlLearningCategories = sqlLearningCategoriesRead +
//                    " AND (lc.cat_type LIKE '" + type + "') "
//                    + sqlLearningCategoriesReadGroup;
//        }
//
//        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlLearningCategories, arrColLearningCategories);
//
//        ArrayList<String> lstCategories = new ArrayList<>();
//        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {
//            lstCategories.add(lstLearningCategoriesRecs.get(r).getColumnData("cat_title"));
//        }
//
//        for (int c = 0; c < lstCategories.size(); c++) {
//            String captionCategory = lstCategories.get(c);
//            RouteParam routeCategory = new RouteParam("category", captionCategory);
//            RouterLink linkPhotoCategory = new RouterLink(captionCategory, LearningsView.class, new RouteParameters(routeCategory));
//            layoutFilters.add(linkPhotoCategory);
//        }
//
//        Div divFiltersTitle = new Div("Filter by Subject");
//        layoutFiltersAll.add(divFiltersTitle, layoutFilters);


        CheckboxGroup<String> checkboxGroupLocation = new CheckboxGroup<>();
        checkboxGroupLocation.setTooltipText("Location");
        checkboxGroupLocation.setItems("Hungary", "UK", "Greece");//, "Thursday",


        Select<String> cmbView = new Select<>();
        cmbView.setLabel("View");

        cmbView.setItems("Micro View", "Ordinary - No MetaData", "Ordinary - MetaData Bottom", "Ordinary - MetaData Right",
                "Wide - No MetaData", "Wide - MetaData Bottom", "Wide - MetaData Right");
        cmbView.setValue("Ordinary - No MetaData");

        headerContainer.add(layoutFiltersAll);

        HeaderFilterTabs headerFilterTabs = new HeaderFilterTabs(recordService, isMobile);
        VerticalLayout layoutHeaderParameters = headerFilterTabs.getHeader(strHeader, strSubHeader, strSection, headerContainer);

//        headerContainerMaster.add(headerTextContainer, cmbView);
//        headerContainerSecondary.add(layoutFilters, sortBy);
//        layoutHeaderParameters.add(headerContainerMaster,headerContainerSecondary);

        return layoutHeaderParameters;
    }

//    private VerticalLayout loadLearnings(String sqlRead, String[] arrColumnNames) {
//        strPath = DIR_PHOTOS_SERVER + dirChar;
//
//        VerticalLayout  layoutLearnings = new VerticalLayout();
//        if(isMobile){
//            layoutLearnings.addClassNames(
//                    Overflow.HIDDEN, Width.FULL,
//                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
//                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
//                    // Margin.Horizontal.SMALL,
//                    Margin.NONE, Padding.NONE,
//                    Gap.MEDIUM,
//                    //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
//                    //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
//                    AlignItems.CENTER, JustifyContent.CENTER
//            );
//        }else {
//            layoutLearnings.addClassNames(
//                    Overflow.HIDDEN, Width.FULL,
//                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
//                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
//                    // Margin.Horizontal.SMALL,
//                    Margin.NONE,
//                    Padding.NONE,
////                    Padding.Top.NONE,
////                    Padding.XLARGE,
//                    Gap.LARGE,
//                    //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
//                    //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
//                    AlignItems.CENTER, JustifyContent.CENTER
//            );

    /// /            layoutLearnings.getStyle().set("gap","3rem");
//        }
//
//
//        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
//        for (int r = 0;r< lstRecords.size();r++) {
//
//            Record rec = lstRecords.get(r);
//            layoutLearnings.add(getLearningsItem(rec));
//        }
//        return layoutLearnings;
//    }
    public VerticalLayout getLearningsItem(Record record, boolean isTeam) {

        String strTitle = record.getColumnData("title");
        String strCategory = record.getColumnData("cat_title");
        String strCategory2 = record.getColumnData("cat_title2");

        String strFormat = record.getColumnData("format");
        String strDuration = record.getColumnData("duration");
        String strPages = record.getColumnData("pages");

        String strTutor = record.getColumnData("tutor_name");

        String strTeamName = record.getColumnData("team_name");

        String strYearPublished = record.getColumnData("year_published");

        String strUserIdPost = record.getColumnData("userId_post");
        String strUserIdSuggest = record.getColumnData("userId_suggest");

        String strUsername = record.getColumnData("username");
        String strNameOfUser = record.getColumnData("nameOfUser");
        String strMemberSince = record.getColumnData("member_since");
        String strAvatarPath = record.getColumnData("avatar_path");


        if (isTeam) {
            strTutor = strTeamName;
        }

        Div divTutor = new Div();
        divTutor.addClassNames(TextColor.SECONDARY, TextAlignment.CENTER);
        divTutor.setVisible(false);
        if (!strTutor.equalsIgnoreCase("null") && !strTutor.isEmpty()) {
            divTutor.setText(strTutor);
            divTutor.setVisible(true);
        }


//        String strTutorTeam = record.getColumnData("learnings_team_id");
//        Div divTutorTeam = new Div();
//        divTutorTeam.addClassName(TextColor.SECONDARY);
//        divTutorTeam.setVisible(false);
//        if (!strTeamName.equalsIgnoreCase("null") && !strTeamName.isEmpty()) {
//            divTutorTeam.setText(strTeamName);
//            divTutorTeam.setVisible(true);
//        }

        String strImage = record.getColumnData("picture");

        if (!strImage.equalsIgnoreCase("null") && !strImage.equalsIgnoreCase("")) {
            strImage = strPath + "/" + strImage;
        } else {
            strImage = "";
        }


        HorizontalLayout layoutSection = new HorizontalLayout();

        Div divImage = new Div();
//        divImage.addClassName("section");//.getStyle().setColor(strColorOfIcons);
        Div titleRelated = new Div("Learnings");//,"",);
        titleRelated.addClassNames(Margin.Bottom.NONE, Margin.Top.NONE, TextColor.SECONDARY); //,  FontWeight.BOLD);
        // titleRelated.addClassName("text-header");
//        linkCategoryRelated.addClassName("section");//.getStyle().setColor(strColorOfIcons);
        divImage.add(LineAwesomeIcon.BOOK_SOLID.create());
        layoutSection.add(divImage, titleRelated);

        RouteParam routeTutor = new RouteParam("tutor", strTutor);
        RouterLink linkLearningTutor = new RouterLink(strTutor, LearningsView.class, new RouteParameters(routeTutor));


//        RouteParam routeTitle = new RouteParam("title", strTitle);
//        RouterLink linkLearningTitle = new RouterLink(strTitle, LearningsView.class, new RouteParameters(routeTitle));


        H3 titleName = new H3(strTitle);
        titleName.addClassName(TextColor.SECONDARY);


        String dateCreated = record.getColumnData("date_created");
//        SimpleDateFormat toui = new SimpleDateFormat("dd/MM/yyyy");
//        SimpleDateFormat fromdb = new SimpleDateFormat("yyyy-MM-dd");

//        try {
//            strDate = toui.format(fromdb.parse(dt));
//        } catch (ParseException e) {
//            logger.error(e.getMessage());
//        }


        HorizontalLayout layoutPostTitle = new HorizontalLayout();
        if (isMobile) {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.NONE,
                    Padding.XSMALL,
                    Gap.XSMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //Background.CONTRAST_10,
                    Border.BOTTOM, //Border.RIGHT, //BorderColor.CONTRAST_20,
//                    BorderColor.CONTRAST_20,
                    BorderRadius.NONE);
        } else {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.NONE,
                    Padding.Horizontal.SMALL, Padding.Vertical.MEDIUM,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    // Background.CONTRAST_10,
                    Border.BOTTOM, //Border.RIGHT,// BorderColor.CONTRAST_20,
//                    BorderColor.CONTRAST_20,
                    BorderRadius.LARGE);
        }
        layoutPostTitle.add(titleName);
        layoutPostTitle.addClassName("post-title-bar");

        VerticalLayout layoutLearningInfo = new VerticalLayout();
        if (isMobile) {
            layoutLearningInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        } else {
            layoutLearningInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_5,
                    BorderRadius.NONE
            );
            layoutLearningInfo.addClassName("bottom-radius-shadow");
        }


        HorizontalLayout layoutImageSmall = new HorizontalLayout();
        layoutImageSmall.addClassNames(Padding.SMALL, Background.CONTRAST_70, Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE,
                BoxShadow.SMALL);

        HorizontalLayout layoutImageNormal = new HorizontalLayout();
        layoutImageNormal.addClassNames(Padding.SMALL, Background.CONTRAST_70, Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE,
                BoxShadow.SMALL);

        if (!strImage.equalsIgnoreCase("null") && !strImage.equalsIgnoreCase("")) {
            String finalStrImage = strImage;
            final StreamResource imageResource = new StreamResource("image", () -> {
                try {
                    return new FileInputStream(new File(finalStrImage));
                } catch (final FileNotFoundException e) {
                    logger.error("FileNotFoundException learning " + e.getMessage());
                    return null;
                }
            });

            Image imgSmall = new Image(imageResource, "image");
            imgSmall.setMaxHeight("240px");
            imgSmall.addClassNames(BorderRadius.LARGE);
            layoutImageSmall.add(imgSmall);

            Image imgNormal = new Image(imageResource, "image");
            imgNormal.setMaxHeight("440px");
            imgNormal.addClassNames(BorderRadius.LARGE);
            layoutImageNormal.add(imgNormal);

        }


        Anchor linkTutor = new Anchor();
        linkTutor.add(FontAwesome.Solid.LINK.create());
        linkTutor.setVisible(false);
        // linkTutor.getStyle().setColor(strColorExternalweb);
        //  linkTutor.setClassName("lazy-result-line-button");

        String strUrlTutorExt = record.getColumnData("website");
        if (!strUrlTutorExt.equalsIgnoreCase("null") && !strUrlTutorExt.equalsIgnoreCase("")) {

            // linkTutor.setText("Website");
            linkTutor.setVisible(true);
            linkTutor.setHref(strUrlTutorExt);
            linkTutor.setTarget("_blank");
        }

        Anchor linkTutorYt = new Anchor();
        linkTutorYt.add(FontAwesome.Brands.YOUTUBE.create());
        // linkTutorYt.getStyle().setColor(strColorExternalweb);
        // linkTutorYt.setClassName("lazy-result-line-button");
        linkTutorYt.setVisible(false);
        String strUrlTutorYt = record.getColumnData("url_yt");
        if (!strUrlTutorYt.equalsIgnoreCase("null") && !strUrlTutorYt.equalsIgnoreCase("")) {

            //linkTutorYt.setText("YouTube");
            // strUrlTutorYt = "https://www.youtube.com/"+strUrlTutorYt;
            linkTutorYt.setHref(strUrlTutorYt);
            linkTutorYt.setTarget("_blank");
            linkTutorYt.setVisible(true);
        }

        Anchor linkTutorWikipedia = new Anchor();
        linkTutorWikipedia.add(FontAwesome.Brands.WIKIPEDIA_W.create());
        // linkTutorWikipedia.getStyle().setColor(strColorExternalweb);
        //   linkTutorWikipedia.setClassName("lazy-result-line-button");
        linkTutorWikipedia.setVisible(false);
        String strUrlTutorWikipedia = record.getColumnData("url_wikipedia");
        if (!strUrlTutorWikipedia.equalsIgnoreCase("null") && !strUrlTutorWikipedia.equalsIgnoreCase("")) {

            //linkTutorYt.setText("YouTube");
            //strUrlTutorWikipedia = "https://www.youtube.com/"+strUrlTutorYt;
            linkTutorWikipedia.setHref(strUrlTutorWikipedia);
            linkTutorWikipedia.setTarget("_blank");
            linkTutorWikipedia.setVisible(true);
        }

        Anchor linkTutorInsta = new Anchor();
        //  linkTutorInsta.setClassName("lazy-result-line-button");
        linkTutorInsta.add(FontAwesome.Brands.INSTAGRAM.create());
        // linkTutorInsta.getStyle().setColor(strColorExternalweb);
        linkTutorInsta.setVisible(false);
        String strUrlTutorInsta = record.getColumnData("url_insta");
        if (!strUrlTutorInsta.equalsIgnoreCase("null") && !strUrlTutorInsta.equalsIgnoreCase("")) {

            // linkTutorInsta.setText("Instagram");
//            strUrlTutorInsta = "https://www.instagram.com/"+ strUrlTutorInsta;
            linkTutorInsta.setHref(strUrlTutorInsta);
            linkTutorInsta.setTarget("_blank");
            linkTutorInsta.setVisible(true);
        }


        Anchor link1InNewTab = new Anchor();

        String strUrl = record.getColumnData("url");
        String strYouTubeVideo = "https://www.youtube.com/watch?v=";
        String strVideoOnly = strUrl.replace(strYouTubeVideo, "");

        String youtubeEmbedded = "<div><iframe class='video-iframe' src='https://www.youtube.com/embed/" + strVideoOnly + "' title='" + strTitle + "'  allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share'  allowFullScreen></iframe></div>";

        Html htmlVideoSmall = new Html(youtubeEmbedded);
        htmlVideoSmall.setHtmlContent(youtubeEmbedded);
//        htmlVideo.addClassNames(Padding.SMALL, Margin.MEDIUM, Background.CONTRAST_60, BorderRadius.LARGE);
        htmlVideoSmall.setClassName("video-container-small");

//        Div layoutVideo = new Div();
//        layoutVideo.addClassNames( AlignItems.CENTER, JustifyContent.CENTER,
//                Padding.NONE, Margin.NONE,                    Width.FULL,
//                Background.CONTRAST_70, Border.NONE, BorderRadius.LARGE,
//                BoxShadow.MEDIUM);

//        layoutVideo.add(htmlVideo);


        VerticalLayout layoutSourceCardSmall = new VerticalLayout();
        layoutSourceCardSmall.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.AROUND,
                Margin.NONE,
                Padding.MEDIUM,
                Gap.MEDIUM,
                TextColor.SECONDARY
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                Background.TINT_10
//                BorderColor.CONTRAST_10,
//                Border.ALL,  BorderRadius.LARGE
        );
//        layoutSourceCardSmall.setMaxWidth("280px");

//        RouteParam routeCategory = new RouteParam("category", strCategory);
//        RouterLink linkPhotoCategory = new RouterLink(strCategory, LearningsView.class, new RouteParameters(routeCategory));
//
//        RouteParam routeCategory2 = new RouteParam("category", strCategory2);
//        RouterLink linkPhotoCategory2 = new RouterLink(strCategory2, LearningsView.class, new RouteParameters(routeCategory2));
//
//        if (strCategory2 == null || strCategory2.equalsIgnoreCase("") || strCategory2.equalsIgnoreCase("null") || strCategory2.isEmpty()) {
//            linkPhotoCategory2.setVisible(false);
//        } else {
//            linkPhotoCategory2.setVisible(true);
//        }

        Span spCategorySmall = new Span(strCategory);
        spCategorySmall.getElement().getThemeList().add("badge contrast");
        Span spCategory2Small = new Span(strCategory2);
        spCategory2Small.getElement().getThemeList().add("badge contrast");
        if (strCategory2.isEmpty() || strCategory2.equalsIgnoreCase("null") || strCategory2.equalsIgnoreCase("")) {
            spCategory2Small.setVisible(false);
        }

        String strAvatarFullPath = DIR_PHOTOS_SERVER + dirChar + SUB_PATH_AVATARS + dirChar + strAvatarPath;
        Image imgAvatarSmall = genericView.getAvatarImage(strAvatarFullPath, strNameOfUser, "40px", "40px");
        AvatarItem avatarItemSmall = new AvatarItem(strNameOfUser, "", imgAvatarSmall);
        avatarItemSmall.addClassNames(Width.FULL, AlignItems.STRETCH, JustifyContent.BETWEEN);
        Span spAvatarItemSmall = new Span(avatarItemSmall);

        Div divTutorInfoSmall = new Div();
        divTutorInfoSmall.addClassName(TextColor.SECONDARY);
        divTutorInfoSmall.setVisible(false);
        if (!strTutor.equalsIgnoreCase("null") && !strTutor.isEmpty()) {
            divTutorInfoSmall.setText(strTutor);
            divTutorInfoSmall.setVisible(true);
        }

        Div divYearPublished = new Div();
        divYearPublished.addClassName(TextColor.SECONDARY);
        divYearPublished.setVisible(false);
        if (!strYearPublished.equalsIgnoreCase("null") && !strYearPublished.isEmpty()) {
            divYearPublished.setText("Year Published: " + strYearPublished);
            divYearPublished.setVisible(true);
        }

        StreamResource iconInfo = new StreamResource("info-circle-line-icon.svg",
                () -> getClass().getResourceAsStream("/icons/info-circle-line-icon.svg"));
        SvgIcon svgInfo = new SvgIcon(iconInfo);

        StreamResource iconTutor = new StreamResource("man-user-circle-black-icon.svg",
                () -> getClass().getResourceAsStream("/icons/man-user-circle-black-icon.svg"));
        SvgIcon svgTutor = new SvgIcon(iconTutor);


        Div imgPerson = new Div(svgTutor);

        HorizontalLayout layoutExtLinks = new HorizontalLayout();
        layoutExtLinks.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.NONE,
                Gap.MEDIUM
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //    Background.CONTRAST_5,
                //BorderRadius.LARGE
        );
        layoutExtLinks.addClassNames("external-links");
        layoutExtLinks.add(linkTutor, linkTutorWikipedia, linkTutorInsta, linkTutorYt);


        Div imgInfo = new Div(svgInfo);
        Div divFormat = new Div();
        if (strFormat.equalsIgnoreCase("YouTube")) {
            if (!strDuration.equalsIgnoreCase("null") && !strDuration.equalsIgnoreCase("")) {
                divFormat.setText(strFormat + "(" + strDuration + ")");
            } else {
                divFormat.setText(strFormat);
            }
        } else if (strFormat.equalsIgnoreCase("book")) {
            layoutImageSmall.setMaxWidth("430px");
            if (!strPages.equalsIgnoreCase("null") && !strPages.equalsIgnoreCase("")) {
                divFormat.setText("Book (" + strPages + " pages)");
            } else {
                divFormat.setText("book");
            }
        } else if (strFormat.equalsIgnoreCase("Url with Free e-book")) {
            layoutImageSmall.setMaxWidth("430px");
            if (!strPages.equalsIgnoreCase("null") && !strPages.equalsIgnoreCase("")) {
                divFormat.setText("E-Book (" + strPages + " pages)");
            } else {
                divFormat.setText("E-Book");
            }
        } else {
            layoutImageSmall.setMaxWidth("430px");
            if (!strPages.equalsIgnoreCase("null") && !strPages.equalsIgnoreCase("")) {
                divFormat.setText(strFormat + "(" + strPages + " pages)");
            } else {
                divFormat.setText(strFormat);
            }
        }


        VerticalLayout layoutIDDataSmall = new VerticalLayout();
        layoutIDDataSmall.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, //Width.FULL,
//                Background.TINT_10,
                BorderRadius.LARGE,
                Margin.NONE, Padding.MEDIUM,
                Gap.SMALL
//                BoxShadow.XSMALL
        );
        layoutIDDataSmall.addClassName("item-id-info");
        layoutIDDataSmall.add(imgPerson, divTutor, layoutExtLinks, divFormat, divYearPublished);



        Div dayUpdatedLabelSmall = new Div("Info Created: ");
        dayUpdatedLabelSmall.addClassName(TextColor.SECONDARY);

        Div dayUpdatedSmall = new Div(dateCreated);
        dayUpdatedSmall.getElement().getThemeList().add("badge contrast");


        VerticalLayout layoutItemInfoSmall = new VerticalLayout();
        layoutItemInfoSmall.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, //Width.FULL,
//                Background.TINT_10,
                BorderRadius.LARGE,
                Margin.NONE, Padding.MEDIUM,
                Gap.SMALL
//                BoxShadow.XSMALL
        );
        layoutItemInfoSmall.addClassName("item-id-info");
        layoutItemInfoSmall.add(imgInfo, dayUpdatedLabelSmall, dayUpdatedSmall, spCategorySmall, spCategory2Small,spAvatarItemSmall);
        layoutSourceCardSmall.setMaxWidth("240px");
        layoutSourceCardSmall.add(layoutIDDataSmall, layoutItemInfoSmall);


        HorizontalLayout layoutDataSmall = new HorizontalLayout();
        layoutDataSmall.addClassNames(AlignItems.CENTER, JustifyContent.AROUND,
                Margin.MEDIUM, Padding.LARGE,
                Width.FULL);
        layoutDataSmall.add(layoutImageSmall, htmlVideoSmall, layoutSourceCardSmall);

        HorizontalLayout layoutSourceCardNormal= new HorizontalLayout();
        layoutSourceCardNormal.addClassNames(
                Overflow.HIDDEN, //Width.FULL,
                AlignItems.START, JustifyContent.CENTER,
                Margin.LARGE,
                Padding.NONE,
                Gap.SMALL,
                TextColor.SECONDARY
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                Background.TINT_10
//                BorderColor.CONTRAST_10,
//                Border.ALL,  BorderRadius.LARGE
        );

        VerticalLayout layoutIDDataNormal = new VerticalLayout();
        layoutIDDataNormal.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, //Width.FULL,
//                Background.TINT_10,
                BorderRadius.LARGE,
                Margin.NONE, Padding.MEDIUM,
                Gap.SMALL
//                BoxShadow.XSMALL
        );
        layoutIDDataNormal.add(imgPerson, divTutor, layoutExtLinks, divFormat, divYearPublished);


        Span spCategoryNormal = new Span(strCategory);
        spCategoryNormal.getElement().getThemeList().add("badge contrast");
        Span spCategory2Normal = new Span(strCategory2);
        spCategory2Normal.getElement().getThemeList().add("badge contrast");
        if (strCategory2.isEmpty() || strCategory2.equalsIgnoreCase("null") || strCategory2.equalsIgnoreCase("")) {
            spCategory2Normal.setVisible(false);
        }

        Div dayUpdatedLabelNormal = new Div("Info Created: ");
        dayUpdatedLabelNormal.addClassName(TextColor.SECONDARY);

        Div dayUpdatedNormal = new Div(dateCreated);
        dayUpdatedNormal.getElement().getThemeList().add("badge contrast");

        Details detUserPostedNormal = getMemberDetail(strUserIdPost,
                strAvatarPath, strUsername, strNameOfUser, strMemberSince);
        detUserPostedNormal.getStyle().setBackgroundColor("#f3f3f3");

        VerticalLayout layoutItemInfoNormal = new VerticalLayout();
        layoutItemInfoNormal.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, //Width.FULL,
//                Background.TINT_10,
                BorderRadius.LARGE,
                Margin.NONE, Padding.MEDIUM,
                Gap.SMALL
//                BoxShadow.XSMALL
        );
        layoutItemInfoNormal.add(imgInfo, dayUpdatedLabelNormal, dayUpdatedNormal, spCategoryNormal, spCategory2Normal);

        layoutIDDataNormal.setMinWidth("240px");
        layoutItemInfoNormal.setMinWidth("240px");
        layoutSourceCardNormal.addClassName("item-id-info");
        layoutSourceCardNormal.add(layoutIDDataNormal, layoutItemInfoNormal);

        Html htmlVideoNormal = new Html(youtubeEmbedded);
        htmlVideoNormal.setHtmlContent(youtubeEmbedded);
        htmlVideoNormal.setClassName("video-container-normal");

        HorizontalLayout layoutDataNormal = new HorizontalLayout();
        layoutDataNormal.addClassNames(AlignItems.CENTER, JustifyContent.EVENLY,
                Width.FULL);

        layoutDataNormal.add(layoutImageNormal, htmlVideoNormal);

        if (!strUrl.equalsIgnoreCase("null") && !strUrl.equalsIgnoreCase("")) {
            if (strFormat.equalsIgnoreCase("YouTube")) {
                link1InNewTab.setVisible(false);
                htmlVideoSmall.setVisible(true);
                layoutImageSmall.setVisible(false);

                htmlVideoNormal.setVisible(true);
                layoutImageNormal.setVisible(false);
            } else {
                link1InNewTab.setText(strUrl);
                //link1InNewTab.setTarget(festUrl);
                link1InNewTab.setHref(strUrl);
                link1InNewTab.setTarget("_blank");
                //link1InNewTab.getElement().setAttribute("target", "_blank");
                link1InNewTab.setVisible(true);
                htmlVideoSmall.setVisible(false);
                layoutImageSmall.setVisible(true);

                htmlVideoNormal.setVisible(false);
                layoutImageNormal.setVisible(true);
            }
        } else {
            link1InNewTab.setVisible(false);
            htmlVideoSmall.setVisible(false);
            layoutImageSmall.setVisible(true);

            htmlVideoNormal.setVisible(false);
            layoutImageNormal.setVisible(true);
        }

        logger.info("  htmlVideoSmall  " + htmlVideoSmall.isVisible());

        HorizontalLayout layoutPostRelated = new HorizontalLayout();
        layoutPostRelated.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextColor.SECONDARY,
                Padding.NONE, Margin.NONE, BorderRadius.LARGE);


        if (strFormat.equalsIgnoreCase("Url with Free e-book")) {
            Div lblGotoUrl = new Div("Click to go to author's site, to download the e-book.");
            if (strUrl != null && !strUrl.isEmpty()) {
                Anchor linkSourceToNewTab = new Anchor();
                String strUrlShorter = "";
                if (strUrl.trim().length() > 50) {
                    strUrlShorter = strUrl.substring(0, 46) + "...";
                }
                linkSourceToNewTab.setText(strUrlShorter);
                linkSourceToNewTab.setHref(strUrl);
                linkSourceToNewTab.setTarget("_blank");
                linkSourceToNewTab.setVisible(true);


                layoutPostRelated.add(lblGotoUrl, linkSourceToNewTab);
            }
        }

        String strDescription = record.getColumnData("description");

//        parDescription.addClassNames(TextColor.TERTIARY, FontSize.MEDIUM, Padding.MEDIUM);
        if (!strDescription.equalsIgnoreCase("null") && !strDescription.isEmpty()) {
//            parDescription.setText(strDescription);
        } else {
            strDescription = " Overview of " + strTitle;
        }


        VerticalLayout layoutSourceReviewSmall = getFormattedText(strDescription,true);
        layoutSourceReviewSmall.addClassNames(TextColor.SECONDARY, FontSize.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER
        );
        layoutSourceReviewSmall.addClassName("item-description");


        HorizontalLayout layoutAggregateInfo = getViewAggregateInfo();

        RouteParam routeTitle = new RouteParam("title", strTitle);

        Button btnMore = new Button("More");
        btnMore.setIcon(VaadinIcon.ARROW_RIGHT.create());
        btnMore.addClassName("btn-more");
        btnMore.addClickListener(click->{
            btnMore.getUI().ifPresent(ui ->
                    ui.navigate(LearningsView.class, new RouteParameters(routeTitle)));
        });





//        layoutPostRelated, layoutSubTabs, layoutAggregateInfo);
        if(title.equalsIgnoreCase(STR_ALL_TITLES) || title.isEmpty()) {
            layoutAggregateInfo.add(btnMore);
            layoutLearningInfo.add(layoutPostTitle, layoutDataSmall,layoutSourceReviewSmall,layoutAggregateInfo);
        }else{
            VerticalLayout layoutSubTabs = getSubTabs("Learning", strTitle, record);

            VerticalLayout layoutReviewNormal = getFormattedText(strDescription,false);
            layoutReviewNormal.addClassNames( FontSize.MEDIUM,
                    Margin.SMALL,
                    Padding.MEDIUM,
                    Gap.SMALL,
                    //TextColor.SECONDARY,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
            layoutReviewNormal.addClassName("item-description");

            layoutAggregateInfo.addClassName("aggregate-detail");

           Div divRelated = new Div(new Text(""));

            Details detUserPosted = getMemberDetail(strUserIdPost,
                    strAvatarPath, strUsername, strNameOfUser, strMemberSince);
            detUserPosted.getStyle().setBackgroundColor("#f3f3f3");

            Span spUserPoster = new Span(detUserPosted);

        RouteParam routeCategory;

        if(!genre.isEmpty() && !genre.equalsIgnoreCase(STR_ALL_GENRES)) {
            routeCategory = new RouteParam("category", strCategory);
        }else{
            routeCategory = new RouteParam("genre", strCategory);
        }
        RouterLink linkPhotoCategory = new RouterLink(strCategory, LearningsView.class, new RouteParameters(routeCategory));

        RouteParam routeCategory2 = new RouteParam("category", strCategory2);
        RouterLink linkPhotoCategory2 = new RouterLink(strCategory2, LearningsView.class, new RouteParameters(routeCategory2));

        if (strCategory2 == null || strCategory2.equalsIgnoreCase("") || strCategory2.equalsIgnoreCase("null") || strCategory2.isEmpty()) {
            linkPhotoCategory2.setVisible(false);
        } else {
            linkPhotoCategory2.setVisible(true);
        }

            linkPhotoCategory.addClassName("btn-more");
            linkPhotoCategory2.addClassName("btn-more");

        HorizontalLayout layoutBottomLinks = new HorizontalLayout();
        layoutBottomLinks.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.EVENLY,
                Margin.NONE, Padding.LARGE,
                Gap.XLARGE);

            layoutBottomLinks.add(linkPhotoCategory,linkPhotoCategory2);

            layoutLearningInfo.add(layoutPostTitle, layoutDataNormal,layoutSourceCardNormal,layoutAggregateInfo,layoutReviewNormal,
                    //getReviewResults(),
                    divRelated,spUserPoster,layoutBottomLinks);
        }

        return layoutLearningInfo;
    }

    private HorizontalLayout getViewAggregateInfo() {

        HorizontalLayout layoutPhotosInfo = new HorizontalLayout();
        layoutPhotosInfo.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.EVENLY,
                Margin.NONE,
                Padding.MEDIUM,
                Gap.SMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                TextColor.TERTIARY
        );


        HorizontalLayout layoutViewCount = new HorizontalLayout();
        layoutViewCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divViews = new Div("1");
        layoutViewCount.add(FontAwesome.Regular.EYE.create(), divViews);

        StreamResource iconComments = new StreamResource("comments.svg",
                () -> getClass().getResourceAsStream("/icons/comments.svg"));
        SvgIcon svgComments = new SvgIcon(iconComments);

        HorizontalLayout layoutComment = new HorizontalLayout();
        layoutComment.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divCommentCount = new Div("1");
        layoutComment.add(svgComments, divCommentCount);

        HorizontalLayout layoutLocationsCount = new HorizontalLayout();
        layoutLocationsCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divLocations = new Div("1");
        layoutLocationsCount.add(FontAwesome.Regular.COMPASS.create(), divLocations);

        HorizontalLayout layoutSavedInListCount = new HorizontalLayout();
        layoutSavedInListCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divDate = new Div("1");
        layoutSavedInListCount.add(VaadinIcon.BOOKMARK.create(), divDate); // FontAwesome.Regular.CALENDAR.create()

        layoutPhotosInfo.add(layoutViewCount, layoutComment, layoutLocationsCount, layoutSavedInListCount);

        return layoutPhotosInfo;
    }

    private VerticalLayout loadFiltersColumn(String sqlRead, String[] arrColumnNames) {
        VerticalLayout filtersColumn = new VerticalLayout();
        if (isMobile) {
            filtersColumn.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE, Padding.NONE,
                    Gap.SMALL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
//                Background.CONTRAST_5,
                    TextAlignment.CENTER
            );
        } else {
            filtersColumn.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE, Padding.XSMALL,
                    Gap.MEDIUM,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
//                Background.CONTRAST_5,
                    TextAlignment.CENTER
            );
        }
//        filtersColumn.setWidth(strWidgth);


        VerticalLayout layoutFiltersType = new VerticalLayout();
        if (isMobile) {
            layoutFiltersType.addClassNames(
                    Overflow.HIDDEN,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE, Padding.NONE,
                    Gap.SMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE);
        } else {
            layoutFiltersType.addClassNames(
                    Overflow.HIDDEN,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE, Padding.NONE,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }
        layoutFiltersType.addClassName("side-layout-filters");

        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);

        ArrayList<String> lstCategories = new ArrayList<>();
        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {
            lstCategories.add(lstLearningCategoriesRecs.get(r).getColumnData("cat_type"));
        }

//        RouteParam routeCategoryAll = new RouteParam("category", STR_ALL_CATEGORIES);
//        RouterLink linkPhotoCategoryAll = new RouterLink("All Categories", LearningsView.class, new RouteParameters(routeCategoryAll));
//        layoutFilters.add(linkPhotoCategoryAll);

        for (int c = 0; c < lstCategories.size(); c++) {
            String captionCategory = lstCategories.get(c);
            RouteParam routeCategory = new RouteParam("category", captionCategory);
            RouterLink linkPhotoCategory = new RouterLink(captionCategory, LearningsView.class, new RouteParameters(routeCategory));
            layoutFiltersType.add(linkPhotoCategory);
        }


        Div divFiltersTitle = new Div("Filter by Category");
        filtersColumn.add(divFiltersTitle, layoutFiltersType);


        return filtersColumn;
    }

    private VerticalLayout loadFiltersGenresColumn(String sqlRead, String[] arrColumnNames) {
        VerticalLayout filtersColumn = new VerticalLayout();
        if (isMobile) {
            filtersColumn.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Margin.NONE, Padding.NONE,
                    Gap.SMALL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
//                Background.CONTRAST_5,
                    TextAlignment.CENTER
            );
        } else {
            filtersColumn.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE, Padding.XSMALL,
                    Gap.MEDIUM,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
//                Background.CONTRAST_5,
                    TextAlignment.CENTER
            );
        }
//        filtersColumn.setWidth(strWidgth);


        VerticalLayout layoutFiltersType = new VerticalLayout();
        if (isMobile) {
            layoutFiltersType.addClassNames(
                    Overflow.HIDDEN,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE, Padding.NONE,
                    Gap.SMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE);
        } else {
            layoutFiltersType.addClassNames(
                    Overflow.HIDDEN,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE, Padding.NONE,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }
        layoutFiltersType.addClassName("side-layout-filters");

        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);

        ArrayList<String> lstCategories = new ArrayList<>();
        ArrayList<String> lstCategoriesDescriptions = new ArrayList<>();
        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {
            lstCategories.add(lstLearningCategoriesRecs.get(r).getColumnData("cat_title"));
        }

//        RouteParam routeCategoryAll = new RouteParam("category", STR_ALL_CATEGORIES);
//        RouterLink linkPhotoCategoryAll = new RouterLink("All Categories", LearningsView.class, new RouteParameters(routeCategoryAll));
//        layoutFilters.add(linkPhotoCategoryAll);

        for (int c = 0; c < lstCategories.size(); c++) {
            String captionCategory = lstCategories.get(c);

            RouteParam routeCategory = new RouteParam("genre", captionCategory);
            RouterLink linkPhotoCategory = new RouterLink(captionCategory, LearningsView.class, new RouteParameters(routeCategory));

            layoutFiltersType.add(linkPhotoCategory);
        }


        Div divFiltersTitle = new Div("Filter by Genre");
        filtersColumn.add(divFiltersTitle, layoutFiltersType);


        return filtersColumn;
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


        StreamResource iconAction = new StreamResource("testimonial-icon.svg",
                () -> getClass().getResourceAsStream("/icons/testimonial-icon.svg"));
        SvgIcon svgAction = new SvgIcon(iconAction);
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
            layoutActions.addClassName("actions-toolbar-mobile");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
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

    private VerticalLayout getReviewResults() {

        VerticalLayout layoutReview = new VerticalLayout();
        layoutReview.addClassNames(Width.FULL,
                TextColor.TERTIARY,
                FontSize.MEDIUM,
                Padding.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER);

//        H6 headerPoll = new H6("Poll");
//        headerPoll.addClassNames(Width.FULL,
//                TextColor.TERTIARY,
//                FontSize.MEDIUM,
//                Padding.MEDIUM,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                TextAlignment.CENTER);

        Div layoutPollQnA = new Div();
        layoutPollQnA.setClassName("lazy-poll-container");
        layoutPollQnA.addClassNames(Width.FULL,
                TextColor.TERTIARY,
                FontSize.MEDIUM,
                Padding.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER);

//        Div layoutPoll = new Div();
//        layoutPoll.setWidthFull();
        Div divQuestion = new Div("How much does this item satisfy your learning requirements?");
        divQuestion.addClassNames(Width.FULL,
                TextColor.TERTIARY,
                FontSize.MEDIUM,
                Padding.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER);

        layoutReview.add(divQuestion, layoutPollQnA);
//        Paragraph par = new Paragraph("(1 very bad, 2 bad ,3 average, 4 good, 5 very good)");
//        par.setWidthFull();
//        par.getStyle().setTextAlign(Style.TextAlign.CENTER);
//        par.getStyle().setColor("#5d6f87");

//        layoutPoll.getStyle().setColor("#5d6f87");


//        layoutPoll.add(divQuestion, layoutPollQnA);

        String vote1 = "5.Very Good";
        String vote2 = "4.Good";
        String vote3 = "3.Average";
        String vote4 = "2.Bad";
        String vote5 = "1.Very Bad";

        ApexChartsBuilder charts1 = new ApexChartsBuilder();
        charts1.withChart(ChartBuilder.get()
                        .withType(Type.PIE).withHeight("230px")
                        .build())
                .withLabels(vote1, vote2, vote3, vote4, vote5)
                .withLegend(LegendBuilder.get()
                        .withPosition(com.github.appreciated.apexcharts.config.legend.Position.LEFT)
                        .withHorizontalAlign(HorizontalAlign.LEFT)
                        .build())
                .withSeries(44.0, 55.0, 13.0, 43.0, 22.0)
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .build())
                //.withTitle(title1)
                .build();
        Div divTitle1 = new Div("Interesting Subject & well structured");
        divTitle1.getStyle().setColor("#5d6f87");
        divTitle1.setWidthFull();
        Div layoutGraph1 = new Div();
        layoutGraph1.setClassName("lazy-poll-graph");
        layoutGraph1.setMinHeight("190px");
        layoutGraph1.add(divTitle1, charts1.build());


        //TitleSubtitle title2 =new TitleSubtitle();
        //title2.setText("Actors");
        //title2.setAlign(Align.CENTER);
        ApexChartsBuilder charts2 = new ApexChartsBuilder();
        charts2.withChart(ChartBuilder.get()
                        .withType(Type.PIE).withHeight("230px")
                        .build())
                .withLabels(vote1, vote2, vote3, vote4, vote5)
                .withLegend(LegendBuilder.get()
                        .withPosition(com.github.appreciated.apexcharts.config.legend.Position.LEFT)
                        .withHorizontalAlign(HorizontalAlign.LEFT)
                        .build())
                .withSeries(4.0, 25.0, 95.0, 128.0, 42.0)
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .build())
                //.withTitle(title2)
                .build();
        Div divTitle2 = new Div("Thorough explained in time duration");
        divTitle2.getStyle().setColor("#5d6f87");
        divTitle2.setWidthFull();
        Div layoutGraph2 = new Div();
        layoutGraph2.setClassName("lazy-poll-graph");
        layoutGraph2.setMinHeight("190px");
        layoutGraph2.add(divTitle2, charts2.build());

//        TitleSubtitle title3 =new TitleSubtitle();
//        title3.setText("Photography");
//        title3.setAlign(Align.CENTER);
        ApexChartsBuilder charts3 = new ApexChartsBuilder();
        charts3.withChart(ChartBuilder.get()
                        .withType(Type.PIE).withHeight("230px")
                        .build())
                .withLabels(vote1, vote2, vote3, vote4, vote5)
                .withLegend(LegendBuilder.get()
                        .withPosition(com.github.appreciated.apexcharts.config.legend.Position.LEFT)
                        .withHorizontalAlign(HorizontalAlign.LEFT)
                        .build())
                .withSeries(44.0, 55.0, 13.0, 43.0, 22.0)
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .build())
                //.withTitle(title3)
                .build();
        Div divTitle3 = new Div("Inspiring & motivates me to ...");
        divTitle3.getStyle().setColor("#5d6f87");
        divTitle3.setWidthFull();
        Div layoutGraph3 = new Div();
        layoutGraph3.setClassName("lazy-poll-graph");
        layoutGraph3.setMinHeight("190px");
        layoutGraph3.add(divTitle3, charts3.build());

        ApexChartsBuilder charts4 = new ApexChartsBuilder();
        charts4.withChart(ChartBuilder.get()
                        .withType(Type.PIE).withHeight("230px")
                        .build())
                .withLabels(vote1, vote2, vote3, vote4, vote5)
                .withLegend(LegendBuilder.get()
                        .withPosition(com.github.appreciated.apexcharts.config.legend.Position.LEFT)
                        .withHorizontalAlign(HorizontalAlign.LEFT)
                        .build())
                .withSeries(4.0, 25.0, 95.0, 128.0, 42.0)
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .build())
                //.withTitle(title2)
                .build();
        Div divTitle4 = new Div("Photography");
        divTitle4.getStyle().setColor("#5d6f87");
        divTitle4.setWidthFull();
        Div layoutGraph4 = new Div();
        layoutGraph4.setClassName("lazy-poll-graph");
        layoutGraph4.setMinHeight("190px");
        layoutGraph4.add(divTitle4, charts4.build());

        ApexChartsBuilder charts5 = new ApexChartsBuilder();
        charts5.withChart(ChartBuilder.get()
                        .withType(Type.PIE).withHeight("230px")
                        .build())
                .withLabels(vote1, vote2, vote3, vote4, vote5)
                .withLegend(LegendBuilder.get()
                        .withPosition(com.github.appreciated.apexcharts.config.legend.Position.LEFT)
                        .withHorizontalAlign(HorizontalAlign.LEFT)
                        .build())
                .withSeries(4.0, 25.0, 95.0, 128.0, 42.0)
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .build())
                //.withTitle(title2)
                .build();
        Div divTitle5 = new Div("Sound");
        divTitle5.getStyle().setColor("#5d6f87");
        divTitle5.setWidthFull();
        Div layoutGraph5 = new Div();
        layoutGraph5.setClassName("lazy-poll-graph");
        layoutGraph5.setMinHeight("190px");
        layoutGraph5.add(divTitle5, charts5.build());

        layoutPollQnA.add(layoutGraph1, layoutGraph2, layoutGraph3); //, layoutGraph4, layoutGraph5);

        return layoutReview;
    }

    private VerticalLayout getFormattedText(String strDescription, boolean isShort) {

        VerticalLayout layoutFormattedText = new VerticalLayout();
        String strDescriptionNew="";
        if(isShort && strDescription.length() >= 311){
            strDescriptionNew = strDescription.substring(0,302) + " ...";
        }else{
            strDescriptionNew =strDescription;
        }

        Paragraph formattedParagraph = new Paragraph(strDescriptionNew);
        formattedParagraph.getElement().getStyle().set("white-space", "pre-wrap");

//        Html html = new Html(Jsoup.clean("<p>Formatted <b>text</b><br>as <i>HTML</i></p>", Safelist.basic()));

//        Paragraph elements = new Paragraph();
//        elements.getElement().appendChild(Element.createText("Formatted "), new Element("b").setText("text"),
//                new Element("br"), Element.createText("as "), new Element("i").setText("elements"));

        layoutFormattedText.add(formattedParagraph);
//        ,
//                new Paragraph(
//                        "For full formatting, you can use HTML either as a string or by assembling individual elements. "
//                                + "When using an HTML string, you should be careful to not include any user-provided values that might lead to cross-site scripting vulnerabilities."),
//                elements, html);

        return layoutFormattedText;
    }


    private VerticalLayout getSubTabs(String strContentType, String strContentTitle, Record record) {

        VerticalLayout layoutTabsInfo = new VerticalLayout();
        if (isMobile) {
            layoutTabsInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.XSMALL,
                    Gap.SMALL
            );
        } else {
            layoutTabsInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.Horizontal.SMALL, Padding.Vertical.MEDIUM,
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

        String strDescription = record.getColumnData("description");

//        Paragraph parDescription = new Paragraph();
//        parDescription.addClassNames(TextColor.TERTIARY, FontSize.MEDIUM, Padding.MEDIUM);
        if (!strDescription.equalsIgnoreCase("null") && !strDescription.isEmpty()) {
//            parDescription.setText(strDescription);
        } else {
            strDescription = " Overview of " + strContentTitle;
        }


        VerticalLayout layoutSourceReview = getFormattedText(strDescription,false);
        layoutSourceReview.addClassNames(TextColor.SECONDARY, FontSize.MEDIUM,
                Margin.NONE,
                Padding.SMALL,
                Gap.SMALL,
                AlignItems.CENTER, JustifyContent.CENTER
        );

//        ArrayList<String> lstLocationTabs = new ArrayList<String>();
//        lstLocationTabs.add("Reviews");
//        lstLocationTabs.add("Notes");
//        lstLocationTabs.add("Related Info");

        VerticalLayout layoutReviews = new VerticalLayout();
        layoutReviews.addClassNames(TextColor.SECONDARY, FontSize.MEDIUM,
                Margin.NONE,
                Padding.NONE,
                Gap.SMALL,
                AlignItems.CENTER, JustifyContent.CENTER
        );
        layoutReviews.add(getFormattedText(strDescription,false), getReviewResults());


        TabSheet tabSheetRelated = new TabSheet();
        tabSheetRelated.addThemeVariants(TabSheetVariant.LUMO_TABS_CENTERED);
        tabSheetRelated.setClassName("lazy-tab-panel");
        tabSheetRelated.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                Margin.NONE,
                Padding.SMALL,
                Gap.SMALL,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextColor.TERTIARY
        );

        StreamResource iconReview = new StreamResource("review.svg",
                () -> getClass().getResourceAsStream("/icons/review.svg"));
        SvgIcon svgReview = new SvgIcon(iconReview);


        Div tabOverview = new Div();
        tabOverview.addClassName("tab-item");
        tabOverview.setId("overview");
        tabOverview.add(FontAwesome.Solid.EYE.create(), new Div("Overview"));
        tabOverview.setClassName("lazy-tab");//.getStyle().set("color","#6a8ab0");
        tabSheetRelated.add(tabOverview, layoutSourceReview);
        Div tab2 = new Div();
        tab2.addClassName("tab-item");
        tab2.setId("reviews");
        tab2.add(svgReview, new Div("Reviews"));
        tab2.setClassName("lazy-tab");//.getStyle().set("color","#6a8ab0");
        tabSheetRelated.add(tab2, layoutReviews);
        Div tab3 = new Div();
        tab3.addClassName("tab-item");
        tab3.setId("related-info");
        tab3.add(FontAwesome.Solid.LINK_SLASH.create(), new Div("Related Info"));
        tab3.setClassName("lazy-tab");//.getStyle().set("color","#6a8ab0");
        tabSheetRelated.add(tab3, new Div(new Text(strContentTitle)));

        layoutTabsInfo.add(tabSheetRelated);

        tabSheetRelated.addSelectedChangeListener(selected -> {
            selected.getPreviousTab();
            Tab selectedTab = selected.getSelectedTab();
            logger.info("Selected tab: {}", selectedTab.getId().get());

        });

//
//
//        btnGroup.addValueChangeListener(event -> {
//            if (event.getValue().toString().equalsIgnoreCase("My Notes")) {
//                divTabContent.setText(" my notes ... of " + strContentTitle + " in " + strContentType);
//                layoutReviews.setVisible(false);
//            } else if (event.getValue().toString().equalsIgnoreCase("Reviews")) {
////                divTabContent.setText(strUsername + " users review 1 ...");
//                layoutReviews.setVisible(true);
//            } else {
//                divTabContent.setText(strContentTitle + " ....... in " + strContentType);
//                layoutReviews.setVisible(false);
//            }
//        });
//
//        layoutTabsInfo.add(btnGroup, divTabContent);


        return layoutTabsInfo;
    }

    public Details getMemberDetail(String strUserIdPost, String strAvatarPath, String strUserName, String strNameOfUser, String strUserJoined) {
        String strAvatarFullPath = DIR_PHOTOS_SERVER + dirChar + SUB_PATH_AVATARS + dirChar + strAvatarPath;
        Image imgAvatarSmall = genericView.getAvatarImage(strAvatarFullPath, strNameOfUser, "40px", "40px");
        AvatarItem avatarItemMe = new AvatarItem(strNameOfUser, "", imgAvatarSmall);
        avatarItemMe.addClassNames(Width.FULL, AlignItems.STRETCH, JustifyContent.BETWEEN);

        Image imgAvatarMedium = genericView.getAvatarImage(strAvatarFullPath, strNameOfUser, "70px", "70px");
        AvatarItem avatarLargeItemMe = new AvatarItem(strNameOfUser, "@" + strUserName, imgAvatarMedium);


        Details detailsMember = new Details();
        detailsMember.addClassNames(Width.FULL, BorderRadius.SMALL);
//        detailsMember.addThemeVariants(DetailsVariant.FILLED);
        detailsMember.addClassName("member-small");
        detailsMember.setSummary(avatarItemMe);


        HorizontalLayout layoutMemberInfo = new HorizontalLayout();
        layoutMemberInfo.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.AROUND,
                Margin.NONE,
                Padding.XSMALL,
                Gap.SMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );

        HorizontalLayout layoutMemberPhotoCount = new HorizontalLayout();
        layoutMemberPhotoCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divMemberPhotoCount = new Div("111");
        layoutMemberPhotoCount.add(FontAwesome.Regular.IMAGES.create(), divMemberPhotoCount);

        HorizontalLayout layoutMemberViewCount = new HorizontalLayout();
        layoutMemberViewCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divMemberViews = new Div("1");
        layoutMemberViewCount.add(FontAwesome.Regular.EYE.create(), divMemberViews);

        HorizontalLayout layoutMemberLocationsCount = new HorizontalLayout();
        layoutMemberLocationsCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divMemberLocations = new Div("1");
        layoutMemberLocationsCount.add(FontAwesome.Regular.COMPASS.create(), divMemberLocations);
//
//        HorizontalLayout layoutDateJoined = new HorizontalLayout();
//        layoutDateJoined.addClassNames(
////                Overflow.HIDDEN, Width.FULL,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Margin.NONE,
//                Padding.XSMALL,
//                Gap.XSMALL,
//                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                //   Background.CONTRAST_5,
//                BorderRadius.NONE
//        );
//        Div divDateJoined = new Div(strUserJoined);
//        layoutDateJoined.add(VaadinIcon.CALENDAR_CLOCK.create(), divDateJoined); // FontAwesome.Regular.CALENDAR.create()

        layoutMemberInfo.add(layoutMemberPhotoCount, layoutMemberViewCount, layoutMemberLocationsCount);


        detailsMember.add(avatarLargeItemMe, layoutMemberInfo);

        return detailsMember;
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


    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {

        logger.info(" photo  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames, Object[] sqlParValue, String[] sqlParType) {
        logger.info(" photo  getRecordsFromDb with params:   " + sql);
        return recordService.findAll(sql, arrColumnNames, sqlParValue, sqlParType);
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
            strUrlRequestToBeLogged = "'" + strUrlRequestToBeLogged + "'";
        }

        if (strPath == null || strPath.isEmpty()) {
            strPath = "NULL";
        } else {
            strPath = "'" + strPath + "'";
        }


        logger.info("photo visitor:" + publicIp + " . " + hostname + " . " + hostAddress + " . " + canonicalHostname + "  .  " + browser + " " + sessionid);

        String insertSQL = "INSERT INTO dbvisitor_log SET visitorlogId = 0,  timeOfVisit = now(), ipAddress = '" + publicIp + "', browserName = '" + browser + "', "
                + " browserVersionMajor = '" + versionOfBrowserMajor + "', browserVersionMinor = '" + versionOfBrowserMinor + "', urlParameter = NULL , timeZoneId = '" + timeZoneId + "', "
                + " appVersion = '" + APP_NAME + "-" + APP_VERSION + "', sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
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
