package com.photo.act.photo_act.views;

import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flowingcode.vaadin.addons.carousel.Carousel;
import com.flowingcode.vaadin.addons.carousel.Slide;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.PhotoFlickrService;
import com.photo.act.photo_act.services.WeatherImageService;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.HeaderFilterTabs;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
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
import com.vaadin.flow.dom.Style;
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
    private String section = SECTION_LEARNINGS;
    //    private String forMemberName;
    private RecordService recordService;
    private String strHeader;

    private String category;

    private String dirChar = FileSystems.getDefault().getSeparator();

    public static String STR_ALL_TUTORS = "all-tutors";
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

    String[] arrColumnsLearning = {"title", "picture", "section", "category", "format", "url", "artists_ref", "description", "duration", "pages", "published",
            "tutor_name", "website", "url_fb", "url_yt", "url_insta", "url_flickr", "url_wikipedia", "url_ref1", "url_ref2", "url_ref3",
            "dateInsert"};

    // learnings: l.id, l.title, l.picture, l.section , l.category, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert
// learnings_tutor:  lt.id, lt.tutor_name, lt.learnings_team_id, lt.website, lt.url_fb, lt.url_yt, lt.url_insta, lt.url_flickr, lt.url_wikipedia, lt.url_ref1, lt.url_ref2, lt.url_ref3, lt.url_flckr, lt.city_base, lt.country_base, lt.userIdInsert, lt.username, lt.date_inserted
    String sqlLearningsRead = "SELECT  " //f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description  " +
            + " l.id, l.title, l.picture, l.section , l.category, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert, "
            + "  t.tutor_name, t.learnings_team_id, t.website, t.url_fb, t.url_yt, t.url_insta, t.url_flickr, t.url_wikipedia, t.url_ref1, t.url_ref2, t.url_ref3, t.city_base, t.country_base, t.userIdInsert, t.username, t.date_inserted "
            + " FROM  learnings l LEFT JOIN tutor t ON t.id = l.tutor_id "
            + " WHERE 1=1 ";

    String sqlLearningsReadOrderBy;

    private UtilsDate utilsDate;
    private String sessionDateTime;
    private String strUrlRequestToBeLogged;
    private GenericView genericView;

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


        sessionid = VaadinSession.getCurrent().getSession().getId();
        sessionCreation = VaadinSession.getCurrent().getSession().getCreationTime();
        isMobile = VaadinSession.getCurrent().getBrowser().isAndroid() || VaadinSession.getCurrent().getBrowser().isIPhone();


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


        userId = 1;
        strUsername = "visitor-user";
        verticalLayout.removeAll();
        VerticalLayout layoutHeaderParameters = loadHeader("", "", "");

        verticalLayout.add(layoutHeaderParameters);


        String[] arrColumnNamesGallery = {"name_org", "name_new", "title", "subtitle", "photo_type", "uploader", "uploaderId", "photo_type", "contains",
                "space_size", "space_size_medium", "space_size_thumb", "city_name", "meta_date", "date_inserted"};

        String sqlReadGallery = "SELECT pm.name_org, pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.uploaderId, pm.photo_type, pm.contains, " +
                " pm.space_size, pm.space_size_medium, pm.space_size_thumb,  d.city_name, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, " + //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
                " case \n" +
                "\t\tWHEN TIMEDIFF(NOW(), pm.date_inserted) <= '00:04:00' THEN 'almost now'\n" +
                "\t\tWHEN TIMEDIFF(NOW(), pm.date_inserted) <= '00:09:00' THEN 'less than 10 minutes ago'\n" +
                "\t\tWHEN TIMEDIFF(NOW(), pm.date_inserted) <= '00:29:00' THEN 'less than 30 minutes ago'\n" +
                "      WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '00:44:00' THEN 'less than 45 minutes ago'\n" +
                "      WHEN TIMEDIFF(NOW(), pm.date_inserted) <= '00:59:00' THEN 'almost an hour ago'\n" +
                "\t\twhen DATE(NOW()) = DATE(pm.date_inserted)  then CONCAT('today at ' , DATE_FORMAT(pm.date_inserted, '%H:%i %p') )\n" +
                "\t\twhen DATE(NOW()+1) < DATE(pm.date_inserted) then CONCAT('yesterday ' , DATE_FORMAT(pm.date_inserted, '%W %D of %M at about %H %p') )\n" +
                " \t\twhen DATE(NOW()) > DATE(pm.date_inserted)  then CONCAT(' on ' , DATE_FORMAT(pm.date_inserted, '%W %D of %M %Y') )\n" +
                "\t\tELSE DATE_FORMAT(pm.date_inserted, '%W %D %M %Y %H:%i %p')\n" +
                " END" +
                " AS date_inserted " +
                " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id ";
//                    " WHERE pm.hostname like '"+hostname+"' "+
//                    " ORDER BY pm.title ASC ";
        String sqlGalleryAll = sqlReadGallery + " WHERE pm.hostname like '" + hostname + "' AND pm.visible_to = 'ALL' ";
//        if(!strDestination.equalsIgnoreCase(STR_ALL_DESTINATIONS)) {
//            sqlGalleryAll = sqlGalleryAll + " AND d.city_name LIKE '" + strDestination + "' ";
//        }
        sqlGalleryAll = sqlGalleryAll + " ORDER BY pm.date_inserted DESC, pm.title ASC, pm.meta_date DESC, pm.name_new ASC ";

        ArrayList<Image> lstImage = loadImagesFromDbToCarousel(sqlGalleryAll + " LIMIT 10 ", arrColumnNamesGallery, false, false);


        H1 titlePage = new H1("10 Sample Photos");
        verticalLayout.add(titlePage, getCarousel(lstImage));

        H1 titleLastPhotos = new H1("Last 6 Photos uploaded");
        VerticalLayout layoutLastPhotos = loadUploadedPhotos(sqlGalleryAll + " LIMIT 6 ", arrColumnNamesGallery, false, true);
        verticalLayout.add(titleLastPhotos, layoutLastPhotos);

        H1 titleWeather = new H1("Current Weather");

        H3 titleA = new H3("Athens");
        VerticalLayout layoutResultsA = loadResults("Athens", "Greece");
        H3 titleB = new H3("Thessaloniki");
        VerticalLayout layoutResultsB = loadResults("Thessaloniki", "Greece");
        verticalLayout.add(titleWeather, titleA, layoutResultsA, titleB, layoutResultsB);


        this.removeAll();
        this.add(verticalLayout);
        this.add(genericView.loadFooter(isMobile));

        logVisitorToDb();
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


        VerticalLayout layoutWeather = getWeatherCurrent(city, country);

//        HorizontalLayout  layoutPhotos = getDestinationPhotos(city,4);

        VerticalLayout layoutResults = new VerticalLayout();
        layoutResults.add(layoutWeather);

        return layoutResults;
    }


    private VerticalLayout loadUploadedPhotos(String sqlRead, String[] arrColumnNames, boolean isEditable, boolean isThumbnails) {


        strPath = DIR_PHOTOS_SERVER + dirChar;
        String strPath;
        if (!isThumbnails) {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathShow;
        } else {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;
        }


        VerticalLayout layoutLastPhotos = new VerticalLayout();
        layoutLastPhotos.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Horizontal.XLARGE, Margin.Vertical.SMALL,
                Padding.Horizontal.XLARGE, Padding.Vertical.SMALL,
                Gap.LARGE);

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            HorizontalLayout layoutPhotoUploaded = new HorizontalLayout();
            layoutPhotoUploaded.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.BETWEEN,
                    Padding.MEDIUM, Margin.XSMALL,
                    TextColor.TERTIARY,
                    Background.CONTRAST_5,
                    BorderRadius.MEDIUM);
            layoutPhotoUploaded.addClassName("uploaded-lines");
            Record record = lstRecords.get(r);
            String strFileName = record.getColumnData("name_new");
            String strTitle = record.getColumnData("title");
            String strSubTitle = record.getColumnData("subtitle");
            String strPhotoType = record.getColumnData("photo_type");

            String strCityName = record.getColumnData("city_name");
            String strUploader = record.getColumnData("uploader");
            String strDateUploaded = record.getColumnData("date_inserted");

            Image image = getImageThumbFromDb(record, strPath);
            image.getStyle().setMaxWidth("auto");
            image.getStyle().setMaxHeight("80px");
            image.addClassNames(BorderRadius.SMALL);

            Icon iconLocation = VaadinIcon.LOCATION_ARROW_CIRCLE_O.create();
            iconLocation.getStyle().set("padding", "var(--lumo-space-xs)");
            if (strCityName == null || strCityName.trim().equalsIgnoreCase("") || strCityName.trim().equalsIgnoreCase("null") || strCityName.isEmpty()) {
                strCityName = "not defined";
            }


            Span badgeLocation = new Span(iconLocation, new Span(strCityName));
            // badgeLocation.getElement().setAttribute("theme", "badge");
            badgeLocation.getElement().getThemeList().add("badge contrast");

            Div divUserAvatar = new Div("uploaded by");
            Div divLocation = new Div("photoshoot in");

            Avatar userAvatar = new Avatar(strUploader);
            userAvatar.setImage("https://randomuser.me/api/portraits/men/17.jpg");
            userAvatar.getElement().setAttribute("tabindex", "-1");
            userAvatar.addThemeVariants(AvatarVariant.LUMO_SMALL);

            Span divUser = new Span(strUploader);
            Span divUserObject = new Span(userAvatar, divUser);
            divUserObject.addClassNames(AlignContent.CENTER, JustifyContent.CENTER,
                    Padding.SMALL,
                    BorderRadius.SMALL, Background.CONTRAST_5);

            Icon iconDateTime = VaadinIcon.CALENDAR_CLOCK.create();
            iconDateTime.getStyle().set("padding", "var(--lumo-space-xs)");
            Span badgeDateTime = new Span(iconDateTime, new Span(strDateUploaded));
            if (strDateUploaded.trim().isEmpty() || strDateUploaded.equalsIgnoreCase("null")) {
                badgeDateTime.setText("");
                badgeDateTime.setVisible(false);
            }
            badgeDateTime.getElement().getThemeList().add("badge contrast");

            layoutPhotoUploaded.add(image, divUserAvatar, divUserObject, badgeDateTime, divLocation, badgeLocation);

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

        HorizontalLayout headerContainerSecondary = new HorizontalLayout();
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

    public VerticalLayout getWeatherCurrent(String destination, String country) {
        HorizontalLayout layoutWeather = new HorizontalLayout();
        layoutWeather.getStyle().setColor("#8b94a0");
        layoutWeather.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER
        );
//        layoutWeather.addClassName("lazy-card-overview-min-space");
        //layoutWeather.addClassName("lazy-card-overview-border-solid");

        WeatherService weatherService = new WeatherService("metric");

        String[] locations = weatherService.lookUpLocation(destination, "", country);

        for (int i = 0; i < locations.length; i++) {
            logger.info("locations  " + locations[i]);
        }
        String[] currentWeatherData = weatherService.getCurrentWeatherDataMetric(locations);
        //String[][] dailyForecast =weatherService.getDailyForecastMetric(locations);

//        layoutWeather.getStyle().setAlignItems(Style.AlignItems.CENTER);
//        layoutWeather.getStyle().setJustifyContent(Style.JustifyContent.SPACE_AROUND);

        VerticalLayout layoutLeft = new VerticalLayout();
        layoutLeft.setMargin(false);
        layoutLeft.setSpacing(false);
        layoutLeft.setPadding(false);

        WeatherImageService weatherImage = new WeatherImageService();

        Image imageWeather = new Image();
        imageWeather.getStyle().setOpacity("62%");

        StreamResource iconWeather = new StreamResource(currentWeatherData[5],
                () -> getClass().getResourceAsStream(weatherImage.weatherImage(currentWeatherData)));

        imageWeather.setSrc(iconWeather);
        imageWeather.setMaxWidth("80px");
        imageWeather.setAlt(currentWeatherData[5]);

        VerticalLayout layoutRight = new VerticalLayout();
        layoutRight.setMinWidth("180px");
        layoutRight.setMargin(false);
        layoutRight.setSpacing(false);
        layoutRight.setPadding(false);

        layoutLeft.add(imageWeather);
        layoutLeft.setSizeFull();
        Div hTemp = new Div(currentWeatherData[0]);
        hTemp.addClassName("lazy-card-overview-font-big");
        hTemp.getStyle().setFontSize("26px");
        hTemp.getStyle().setFontWeight(Style.FontWeight.BOLDER);
        layoutLeft.add(hTemp);

        Div hCondition = new Div(currentWeatherData[5]);
        hCondition.addClassName("lazy-card-overview-font-big");
        hCondition.getStyle().setFontSize("16px");
        hTemp.getStyle().setFontWeight(Style.FontWeight.BOLD);
        layoutLeft.add(hCondition);


        Div divTime = new Div(currentWeatherData[14]);
        divTime.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divSunRise = new Div(currentWeatherData[12]);
        divSunRise.getStyle().setFontWeight(Style.FontWeight.BOLD);

        Div divSunset = new Div(currentWeatherData[13]);
        divSunset.getStyle().setFontWeight(Style.FontWeight.BOLD);

        String strIconSize = "35px";

        Image imageSunrise = new Image();
        StreamResource iconSunrise = new StreamResource("Sunrise",
                () -> getClass().getResourceAsStream("/icons/sunrise.png"));
        imageSunrise.setSrc(iconSunrise);
        imageSunrise.setAlt("Sunrise");
//        imageSunrise.setClassName("lazy-card-travel-weather-icons");
        imageSunrise.getStyle().setWidth(strIconSize);
        imageSunrise.getStyle().setHeight(strIconSize);

        Image imageSet = new Image();
        StreamResource iconSunset = new StreamResource("Sunset",
                () -> getClass().getResourceAsStream("/icons/sunset.png"));
        imageSet.setSrc(iconSunset);
        imageSet.setAlt("Sunset");
//        imageSet.setClassName("lazy-card-travel-weather-icons");
        imageSet.getStyle().setWidth(strIconSize);
        imageSet.getStyle().setHeight(strIconSize);


        Div divToday = new Div("Today");
        divToday.getStyle().setFontSize("11px");
        layoutRight.add(new HorizontalLayout(divToday));
        layoutRight.add(new HorizontalLayout(new Div("Sunrise: "), divSunRise));
        layoutRight.add(new HorizontalLayout(new Div("Sunset: "), divSunset));
        // layoutRight.add(new HorizontalLayout(new Div("Sunset: "), divSunset));


        Div divL = new Div(currentWeatherData[2]);
        divL.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divH = new Div(currentWeatherData[3]);
        divH.getStyle().setFontWeight(Style.FontWeight.BOLDER);


        Div divFeelsLike = new Div(currentWeatherData[1]);
        divFeelsLike.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divHumidity = new Div(currentWeatherData[4]);
        divHumidity.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divWindSpeed = new Div(currentWeatherData[7]);
        divWindSpeed.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divClouds = new Div(currentWeatherData[15]);
        divClouds.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divRain = new Div();
        String rain = currentWeatherData[16];

        Div divVisibility = new Div(currentWeatherData[17]);
        divVisibility.getStyle().setFontWeight(Style.FontWeight.BOLDER);


        layoutRight.add(new HorizontalLayout(new Div("L: "), divL, new Div("H: "), divH));
        Div divNow = new Div("Now");
        divNow.getStyle().setFontSize("11px");
        layoutRight.add(new HorizontalLayout(divNow));

        layoutRight.add(new HorizontalLayout(new Div("Feels like: "), divFeelsLike));
        layoutRight.add(new HorizontalLayout(new Div("Clouds: "), divClouds));

        if (!rain.equalsIgnoreCase("")) {
            divRain.setText(currentWeatherData[16]);
            divRain.getStyle().setFontWeight(Style.FontWeight.BOLDER);
            layoutRight.add(new HorizontalLayout(new Div("Rain: "), divRain));
        }

        layoutRight.add(new HorizontalLayout(new Div("Humidity: "), divHumidity));
        layoutRight.add(new HorizontalLayout(new Div("Wind speed: "), divWindSpeed));

        layoutWeather.add(layoutLeft, layoutRight);

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);

        Anchor apiLink = new Anchor();
        apiLink.getStyle().setColor("#8b94a0");
        apiLink.setClassName("lazy-api-link");
        apiLink.setHref(weatherService.getUrlReference());
        apiLink.setTarget("_blank");
        apiLink.setText("Weather data by: " + weatherService.getTitleReference());

        layout.add(layoutWeather, apiLink);

        return layout;

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


        String strOS = "";

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
        } else {
            strOS = "Unknown";
        }


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
                + " hostAddress = '" + hostAddress + "', os = '" + strOS + "', section = '" + section + "',"
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
