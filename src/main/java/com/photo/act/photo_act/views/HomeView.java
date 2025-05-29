package com.photo.act.photo_act.views;

import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flowingcode.vaadin.addons.carousel.Carousel;
import com.flowingcode.vaadin.addons.carousel.Slide;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.PhotoFlickrService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.AvatarItem;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.HeaderFilterTabs;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.MainLayout.*;

//@PageTitle("Photo Act")
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


    String[] arrColLearningTopics = {"cat_title", "cat_title2", "cat_title_type", "cat_title_type2", "cat_type", "cat_type2",
            "cat_description_min", "cat_description_min2", "cat_type_count", "cat_type_count2"};

    String sqlLearningTopics = "SELECT "
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


    String[] arrColLearningGenres = {"cat_title", "cat_title2", "cat_title_type", "cat_type", "cat_description_min", "cat_description_big", "cat_count"};

    String sqlLearningGenres = "SELECT  " //f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description  " +
            + " lc.cat_title, lc.cat_title_type, lc.cat_type, lc.cat_description_min, count (lc.cat_title) AS cat_count, "
            + " lc2.cat_title AS cat_title2, lc2.cat_title_type AS cat_title_type2, lc2.cat_type AS cat_type2, count (lc2.cat_title) AS cat_count2 "
//            + " l.id, l.title, l.picture, l.section , l.category, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert "
//            + ", l.tutor_id, l.tutor_id_team, t.tutor_name, t.website, t.url_fb, t.url_yt, t.url_insta, t.url_flickr, t.url_wikipedia, t.url_ref1, t.url_ref2, t.url_ref3, t.city_base, t.country_base, t.userIdInsert, t.username, t.date_inserted "
            + " FROM learnings_categories lc2 RIGHT JOIN learnings l ON lc2.id = l.category_id2 LEFT JOIN learnings_categories lc ON lc.id = l.category_id "
            + " WHERE 1 = 1 "
            + " AND ( lc.cat_type LIKE '%genre%') "
            + " GROUP BY lc.cat_title ORDER BY lc.cat_order ASC ";

    String sqlLearningsReadOrderBy;

    private UtilsDate utilsDate;
    private String sessionDateTime;
    private String strUrlRequestToBeLogged;
    private GenericView genericView;
    private String strOS;
    private String strBrowser;

    public HomeView(RecordService recordService) {
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
//        tutor = event.getRouteParameters().get("tutor").orElse(STR_ALL_TUTORS);

        getUserClientInfo();

        userId = 1;
        strUsername = "visitor-user";
        verticalLayout.removeAll();
//        VerticalLayout layoutHeaderParameters = loadHeader("", "", "");
//
//        verticalLayout.add(layoutHeaderParameters);


        String[] arrColumnsLearning = {"title", "picture", "cat_title", "cat_title2", "cat_title_type", "cat_title_type2", "cat_type", "format", "url", "artists_ref", "description", "duration", "pages", "published", "year_published",
                "category_id", "category_id2", "tutor_name", "website", "url_fb", "url_yt", "url_insta", "url_flickr", "url_wikipedia", "url_ref1", "url_ref2", "url_ref3",
                "dateInsert",
                "cat_count"};

        // learnings: l.id, l.title, l.picture, l.section , l.category, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert
// learnings_tutor:  lt.id, lt.tutor_name, lt.learnings_team_id, lt.website, lt.url_fb, lt.url_yt, lt.url_insta, lt.url_flickr, lt.url_wikipedia, lt.url_ref1, lt.url_ref2, lt.url_ref3, lt.url_flckr, lt.city_base, lt.country_base, lt.userIdInsert, lt.username, lt.date_inserted
        String sqlLearningsRead = "SELECT  " //f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description  " +
                + " lc.cat_title, lc.cat_title_type, lc.cat_type, COUNT(lc.cat_title) AS cat_count "
                + " , lc2.cat_title AS cat_title2, lc2.cat_title_type AS cat_title_type2 "
                // + " , l.id, l.title, l.picture, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, DATE_FORMAT(l.published, '%Y') AS year_published,  l.userIdInsert, l.username, l.dateInsert "
                //  + " , l.tutor_id, l.tutor_id_team, l.category_id, l.category_id2, t.tutor_name, t.website, t.url_fb, t.url_yt, t.url_insta, t.url_flickr, t.url_wikipedia, t.url_ref1, t.url_ref2, t.url_ref3, t.city_base, t.country_base, t.userIdInsert, t.username, t.date_inserted "
                + " FROM learnings_categories lc, learnings l LEFT JOIN learnings_categories lc2 ON lc2.id = l.category_id2 " //, tutor t  "
                + " WHERE 1 = 1 "
                + " AND lc.id = l.category_id "  //AND l.tutor_id = t.id "
                + " AND l.dateInsert BETWEEN NOW() - INTERVAL 5 DAY AND NOW() "
                + " GROUP BY lc.cat_title";


        String[] arrColumnNamesGallery = {"name_org", "name_new", "title", "subtitle", "photo_type", "uploader", "uploaderId", "photo_type", "contains",
                "space_size", "space_size_medium", "space_size_thumb", "city_name", "meta_date", "date_inserted",
                "username", "nameOfUser", "avatar_path", "member_since"
        };

        String sqlReadGallery = "SELECT pm.name_org, pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.uploaderId, pm.photo_type, pm.contains, " +
                " pm.space_size, pm.space_size_medium, pm.space_size_thumb,  d.city_name, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, " + //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
                "                 ( case " +
                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '00:06:00' THEN 'almost now'" +
                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '00:18:00' THEN '10 minutes ago'" +
                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '00:48:00' THEN '30 minutes ago'" +
                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '01:37:00' THEN 'an hour ago'" +
                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '02:40:00' THEN 'two hours ago'" +
                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '03:42:00' THEN 'three hours ago'" +
                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '04:28:00' THEN 'four hours ago'" +
                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '05:35:00' THEN 'five hours ago'" +
                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '06:35:00' THEN 'six hours ago'" +
                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '07:35:00' THEN 'seven hours ago'" +
                "                WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '08:35:00' THEN 'eight hours ago'" +
                "                when DATE(DATE(pm.date_inserted) + 1) = DATE(NOW()) then CONCAT('Yesterday at ' , DATE_FORMAT(pm.date_inserted, '%H:%i %p') )" +
                "                when DATE(DATE(pm.date_inserted) + 2) = DATE(NOW())  then CONCAT('Last ' , DATE_FORMAT(pm.date_inserted, '%W at %H:%i %p') )" +
                "                when DATE(DATE(pm.date_inserted) + 6) >= DATE(NOW())  then CONCAT('Last ' , DATE_FORMAT(pm.date_inserted, '%W') )" +
                "                when DATE(DATE(pm.date_inserted) + 6) < DATE(NOW())  then CONCAT('' , DATE_FORMAT(pm.date_inserted, '%D of %M %Y') )" +
                "                ELSE DATE_FORMAT(pm.date_inserted, '%D %M %Y') " +
                "              END ) " +
                " AS date_inserted " +
                " , usr.username, usr.nameOfUser, usr.avatar_path, DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
                " FROM dbuser usr, photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id ";
//                    " WHERE pm.hostname like '"+hostname+"' "+
//                    " ORDER BY pm.title ASC ";
        String sqlGalleryAll = sqlReadGallery + " WHERE pm.hostname like '" + hostname + "' AND pm.visible_to = 'ALL' " +
                " AND usr.userId = pm.uploaderId";
//        if(!strDestination.equalsIgnoreCase(STR_ALL_DESTINATIONS)) {
//            sqlGalleryAll = sqlGalleryAll + " AND d.city_name LIKE '" + strDestination + "' ";
//        }
        sqlGalleryAll = sqlGalleryAll + " ORDER BY pm.date_inserted DESC, pm.title ASC, pm.meta_date DESC, pm.name_new ASC ";

        ArrayList<Image> lstImage = loadImagesFromDbToCarousel(sqlGalleryAll + " LIMIT 10 ", arrColumnNamesGallery, false, false);

        Span subTitle = new Span("[ Network and Act around Photography ]");
        H1 titlePage = new H1("photoact.net");

        Header siteHeader = new Header(titlePage,subTitle);
        siteHeader.addClassNames(Width.FULL);

        verticalLayout.add(siteHeader);

        H3 titleCarousel = new H3("10 Recently Uploaded Photos:");
        verticalLayout.add(titleCarousel, getCarousel(lstImage));

        H3 titleLastLearnings = new H3("In previous 5 days were Posted Learnings:");

        VerticalLayout layoutLastLearnings = loadLastLearnings(sqlLearningsRead, arrColumnsLearning);
        verticalLayout.add(titleLastLearnings, layoutLastLearnings);

        H3 titleLastPhotos = new H3("Last 20 Photos that Members Uploaded:");
        Div layoutLastPhotos = loadUploadedPhotos(sqlGalleryAll + " LIMIT 20 ", arrColumnNamesGallery, false, true);
        verticalLayout.add(titleLastPhotos, layoutLastPhotos);


        Div layoutLearningTopics = loadLearningTopics(sqlLearningTopics, arrColLearningTopics);

        H3 titleLearnTopics = new H3("Learn about the following topics");
        verticalLayout.add(titleLearnTopics, layoutLearningTopics);


        Div layoutLearningGenres = loadLearningsAboutGenres(sqlLearningGenres, arrColLearningGenres);

        H3 titleLearnGenres = new H3("Learn about the following photo genres");
        verticalLayout.add(titleLearnGenres, layoutLearningGenres);


        H3 titleWeather = new H3("Current Weather in:");

        H4 titleA = new H4("Athens");
        VerticalLayout layoutResultsA = loadResults("Athens", "Greece");
        H4 titleB = new H4("Thessaloniki");
        VerticalLayout layoutResultsB = loadResults("Thessaloniki", "Greece");
        verticalLayout.add(titleWeather, titleA, layoutResultsA, titleB, layoutResultsB);


        this.removeAll();
        this.add(verticalLayout);
        this.add(genericView.loadFooter(isMobile));

        logVisitorToDb();
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
        } else if(hostname.equalsIgnoreCase(HOSTNAME_LAPTOP_WIN)){
            DIR_PHOTOS_SERVER =  "C:\\Users\\nickg\\Pictures\\lazy-photos";

        } else if (hostname.equalsIgnoreCase("piot")) {
            DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
        } else {
            DIR_PHOTOS_SERVER = "/home/sammy/lazy-photos";

        }

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

        this.setWidthFull();

    }

    private VerticalLayout loadResults(String city, String country) {

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

        GenericView genericView = new GenericView();

        VerticalLayout layoutWeather = genericView.getWeatherCurrent(city, country);

//        HorizontalLayout  layoutPhotos = getDestinationPhotos(city,4);

        VerticalLayout layoutResults = new VerticalLayout();
        layoutResults.add(layoutWeather);

        return layoutResults;
    }

    private VerticalLayout loadLastLearnings(String sqlRead, String[] arrColumnNames) {


        VerticalLayout layoutLastLearnings = new VerticalLayout();
        layoutLastLearnings.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Horizontal.XLARGE, Margin.Vertical.SMALL,
                Padding.Horizontal.XLARGE, Padding.Vertical.SMALL,
                Gap.LARGE);


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            HorizontalLayout layoutLearningCat = new HorizontalLayout();
            layoutLearningCat.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                    Padding.MEDIUM, Margin.XSMALL,
                    TextColor.TERTIARY,
                    Background.CONTRAST_5
                    );
            layoutLearningCat.addClassName("uploaded-line");
            Record record = lstRecords.get(r);
            String strCategory = record.getColumnData("cat_title");
            String strCount = record.getColumnData("cat_count");

            Div divCategory = new Div(strCount + " about " + strCategory);

            layoutLearningCat.add(divCategory);

            layoutLastLearnings.add(layoutLearningCat);
        }
        return layoutLastLearnings;
    }

    private Div loadUploadedPhotos(String sqlRead, String[] arrColumnNames, boolean isEditable, boolean isThumbnails) {


        strPath = DIR_PHOTOS_SERVER + dirChar;
        String strPath;
        if (!isThumbnails) {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathShow;
        } else {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;
        }


        Div layoutLastPhotos = new Div();
        layoutLastPhotos.addClassName("container-uploaded-lines");
/*                Width.FULL, AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Horizontal.XLARGE, Margin.Vertical.SMALL,
                Padding.Horizontal.XLARGE, Padding.Vertical.SMALL,
                Gap.LARGE);*/

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            HorizontalLayout layoutPhotoUploaded = new HorizontalLayout();
            layoutPhotoUploaded.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN,
                    Padding.XSMALL, Margin.SMALL,
                    TextColor.TERTIARY,
                    Background.CONTRAST_5
            );
            layoutPhotoUploaded.addClassName("uploaded-line");
            Record record = lstRecords.get(r);
            String strFileName = record.getColumnData("name_new");
            String strTitle = record.getColumnData("title");
            String strSubTitle = record.getColumnData("subtitle");
            String strPhotoType = record.getColumnData("photo_type");

            String strCityName = record.getColumnData("city_name");
            String strUploader = record.getColumnData("uploader");
            String strDateUploaded = record.getColumnData("date_inserted");

            String strUsername = record.getColumnData("username");
            String strNameOfUser = record.getColumnData("nameOfUser");
            String strAvatarPath = record.getColumnData("avatar_path");
            String strMemberSince = record.getColumnData("member_since");

            Image image = getImageThumbFromDb(record, strPath);
            image.getStyle().setWidth("auto");
            image.getStyle().setMaxHeight("91px");
            image.addClassNames(BorderRadius.SMALL);

            Icon iconLocation = VaadinIcon.LOCATION_ARROW_CIRCLE_O.create();
            iconLocation.getStyle().set("padding", "var(--lumo-space-xs)");
            if (strCityName == null || strCityName.trim().equalsIgnoreCase("") || strCityName.trim().equalsIgnoreCase("null") || strCityName.isEmpty()) {
                strCityName = "not defined";
            }


            Span badgeLocation = new Span(iconLocation, new Span(strCityName));
            // badgeLocation.getElement().setAttribute("theme", "badge");
            badgeLocation.getElement().getThemeList().add("badge contrast");

            Div divUploadedAt = new Div("uploaded at");
            Div divLocation = new Div("photo shoot in");

            String strAvatarFullPath = DIR_PHOTOS_SERVER + dirChar + SUB_PATH_AVATARS + dirChar + strAvatarPath;
            Image imgAvatarMedium = genericView.getAvatarImage(strAvatarFullPath, strNameOfUser, "50px", "50px");
            AvatarItem avatarLargeItemMe = new AvatarItem(strNameOfUser, "@" + strUsername, imgAvatarMedium);

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
            layoutDateLocationUp.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, Margin.NONE, Padding.XSMALL,Gap.XSMALL);
            layoutDateLocationUp.add(divUploadedAt,badgeDateTime, divLocation, badgeLocation);

            layoutPhotoUploaded.add(image, layoutMemberUp, layoutDateLocationUp);

            layoutLastPhotos.add(layoutPhotoUploaded);
        }
        return layoutLastPhotos;
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
                    Padding.XSMALL,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE);
        } else {
            panelOfTopics.addClassNames(
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
        panelOfTopics.addClassName("learning-photo-genres");


        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);

        ArrayList<String> lstCategories = new ArrayList<>();
        ArrayList<String> lstCategoriesDescriptions = new ArrayList<>();
        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {
            lstCategories.add(lstLearningCategoriesRecs.get(r).getColumnData("cat_type"));
            String strDescr = lstLearningCategoriesRecs.get(r).getColumnData("cat_description_min");
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

            RouteParam routeCategory = new RouteParam("category", captionCategory);
            RouterLink linkPhotoCategory = new RouterLink(LearningsView.class, new RouteParameters(routeCategory));
            linkPhotoCategory.add(categoryTitle, categoryDescription);

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
                    Padding.XSMALL,
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
                    Padding.SMALL,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }
        panelOfGenres.addClassName("learning-photo-genres");

        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);

        ArrayList<String> lstCategories = new ArrayList<>();
        ArrayList<String> lstCategoriesDescriptions = new ArrayList<>();
        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {
            lstCategories.add(lstLearningCategoriesRecs.get(r).getColumnData("cat_title"));
            String strDescr = lstLearningCategoriesRecs.get(r).getColumnData("cat_description_min");
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

            RouteParam routeCategory = new RouteParam("genre", captionCategory);
            RouterLink linkPhotoCategory = new RouterLink(LearningsView.class, new RouteParameters(routeCategory));
            linkPhotoCategory.add(genreTitle, genreDescription);

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
            strPath = strPath.replace("\\","-");
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
