package com.photo.act.photo_act.views;

import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.PhotoFlickrService;
import com.photo.act.photo_act.services.WeatherImageService;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.GalleryImageViewCard;
import com.photo.act.photo_act.views.components.UploadImageCard;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
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
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.MainLayout.*;

//@PageTitle("Image Gallery")
//@RouteAlias("") // empty on homepage
//@Route(value = ":section?")
@Route(value = "commons")
//@RouteAlias(value = ":section/:member?", layout = MainLayout.class)
@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class CommonsView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";

    private static final Logger logger = LoggerFactory.getLogger(CommonsView.class);

    private VerticalLayout verticalLayout;
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String section;
    private String forMemberName;
    private RecordService recordService;
    private String strHeader;

    private String dirChar = FileSystems.getDefault().getSeparator();
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


    UtilsDate utilsDate;
    String sessionDateTime;


    public CommonsView(RecordService recordService) {
        this.recordService = recordService;

        utilsDate = new UtilsDate();


        constructUI();

    }


    @Override
    public String getPageTitle() {
        return strHeader;
    }

    @Override
    public void beforeEnter(@OptionalParameter BeforeEnterEvent event) {
        section = event.getRouteParameters().get("section").orElse(SECTION_HOME);
        forMemberName = event.getRouteParameters().get("forMemberName").orElse("all-members");


        sessionid = VaadinSession.getCurrent().getSession().getId();
        sessionCreation = VaadinSession.getCurrent().getSession().getCreationTime();
        isMobile = VaadinSession.getCurrent().getBrowser().isAndroid() || VaadinSession.getCurrent().getBrowser().isIPhone();

        userId = 1;
        strUsername = "visitor-user";

        verticalLayout.removeAll();
        if (section.equalsIgnoreCase(SECTION_HOME)) {
            verticalLayout.add(loadHeader("Welcome!", "", SECTION_HOME));
        } else if (section.equalsIgnoreCase(SECTION_GALLERY)) {
            verticalLayout.add(loadHeader("Gallery of Images", "To please your eyes", SECTION_GALLERY));

            //strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;

//            String[] arrColumnNamesGallery = {"name_new", "title" , "subtitle" , "photo_type" , "uploader", "city_name", "meta_date" };
//
//            String sqlReadGallery = "SELECT pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, d.city_name, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date " + //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
//                    " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id" +
//                    " WHERE pm.hostname like '"+hostname+"' "+
//                    " ORDER BY pm.title ASC ";
            String sqlGalleryAll = sqlReadGallery +
                    " WHERE pm.hostname like '" + hostname + "' " +
                    " ORDER BY pm.title ASC ";

            loadImagesFromDb(sqlGalleryAll, arrColumnNamesGallery, false);


            verticalLayout.add(loadFooter());
        } else if (section.equalsIgnoreCase(SECTION_FESTIVALS)) {
            verticalLayout.add(loadHeader("Festivals and Exhibitions", "Around the World, being prepared for visitors", SECTION_FESTIVALS));

            String[] arrColumnNames = {"nameShort", "location", "country", "periodOfYear", "type", "website", "url_facebook", "url_instagram", "url_youtube", "activities", "image_top", "image_logo", "dateInsert", "title", "subtitle", "formatedDateFrom", "formatedDateTo", "edition_description", "formatedDateUpdated"};

            String sqlRead = "SELECT  f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, " +
                    "e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
                    "FROM  festivals f LEFT JOIN festivals_edition e ON f.id = e.festival_id ORDER BY f.dateInsert DESC";
            loadFestivals(sqlRead, arrColumnNames);


            verticalLayout.add(loadFooter());
        } else if (section.equalsIgnoreCase(SECTION_LEARNINGS)) {
            verticalLayout.add(loadHeader("Learnings", "In order to improve ourselves", SECTION_LEARNINGS));
            String[] arrColumnNames = {"title", "picture", "section", "subject", "format", "url", "artists_ref", "description", "duration", "pages", "published",
                    "tutor_name", "website", "url_fb", "url_yt", "url_insta", "url_flickr", "url_wikipedia", "url_ref1", "url_ref2", "url_ref3"};

// learnings: l.id, l.title, l.picture, l.section , l.subject, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert
// learnings_tutor:  lt.id, lt.tutor_name, lt.learnings_team_id, lt.website, lt.url_fb, lt.url_yt, lt.url_insta, lt.url_flickr, lt.url_wikipedia, lt.url_ref1, lt.url_ref2, lt.url_ref3, lt.url_flckr, lt.city_base, lt.country_base, lt.userIdInsert, lt.username, lt.date_inserted
            String sqlRead = "SELECT  " //f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description  " +
                    + " l.id, l.title, l.picture, l.section , l.subject, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert, "
                    + "  t.tutor_name, t.learnings_team_id, t.website, t.url_fb, t.url_yt, t.url_insta, t.url_flickr, t.url_wikipedia, t.url_ref1, t.url_ref2, t.url_ref3, t.city_base, t.country_base, t.userIdInsert, t.username, t.date_inserted "
                    + " FROM  learnings l LEFT JOIN tutor t ON t.id = l.tutor_id ORDER BY l.dateInsert DESC";
            loadLearnings(sqlRead, arrColumnNames);

            verticalLayout.add(loadFooter());
        } else if (section.equalsIgnoreCase(SECTION_CLUBS)) {
            verticalLayout.add(loadHeader("Photography Clubs", "Clubs and their events around earth.", SECTION_CLUBS));

            loadClubs(sqlShowClubsSelect + sqlShowClubsWhere + sqlShowClubsOrder, arrClubsColumnNames);


            verticalLayout.add(loadFooter());

        } else if (section.equalsIgnoreCase(SECTION_LOCATIONS)) {
            verticalLayout.add(loadHeader("Locations", "Browse before visiting.", SECTION_LOCATIONS));
            String[] arrLocationColumnNames = {"city_name", "city_name_local", "perfecture", "country", "nameShort", "location", "country"}; //, "periodOfYear" , "type" , "website" , "url_facebook" , "url_instagram" , "url_youtube" , "activities" , "image_top",  "image_logo" , "dateInsert" , "title" , "subtitle" , "formatedDateFrom" , "formatedDateTo" , "edition_description","formatedDateUpdated"};

            String sqlShowLocations = "SELECT d.id, d.city_name, d.city_name_local, d.perfecture, d.country, d.longitude, d.latitude, d.url_googlemap, d.url_openstreetmap, d.url_1, d.url_2, d.url_3, d.url_weather1, d.url_transportation1, d.url_transportation2, d.dateInsert " +
                    // " f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, " +
                    // " e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
                    " FROM destination d " + //LEFT JOIN festivals f ON d.id = f.destination_id LEFT JOIN festivals_edition e ON f.id = e.festival_id ORDER BY d.city_name DESC ";
                    " ORDER BY d.city_name DESC ";
            loadLocations(sqlShowLocations, arrLocationColumnNames);

            verticalLayout.add(loadFooter());
        } else if (section.equalsIgnoreCase(SECTION_WEBSITES)) {
            verticalLayout.add(loadHeader("Interesting Sites", "List of Interesting Websites.", SECTION_WEBSITES));
            String[] arrColumnNames = {"org_name", "org_type", "org_type_parent", "city", "used_for", "country", "url"};
            String sqlShowSites = "SELECT id, org_name, org_type, org_type_parent , city , used_for , country , url , city, address, pc, country, map_x, map_y, url, date_inserted, dateUpdated\n" +
                    "FROM organizations o " +
                    "WHERE o.org_type_parent LIKE 'Website' " +
                    "ORDER BY o.city ASC, o.org_name ASC";
            loadWebSites(sqlShowSites, arrColumnNames);


            verticalLayout.add(loadFooter());

        } else if (section.equalsIgnoreCase(SECTION_MY_FAVOURITES)) {
            verticalLayout.add(loadHeader("My Favourites", "Lists of my Favourites.", SECTION_MY_FAVOURITES));

            verticalLayout.add(loadFooter());
        } else if (section.equalsIgnoreCase(SECTION_MY_TEAMS)) {
            verticalLayout.add(loadHeader("My Teams", "The Teams I Participate.", SECTION_MY_TEAMS));

            verticalLayout.add(loadFooter());
        } else if (section.equalsIgnoreCase(SECTION_MY_PHOTOS)) {
            verticalLayout.add(loadHeader("My Photos", "Manage my photos and Albums.", SECTION_MY_PHOTOS));


            String sqlGalleryUser = sqlReadGallery +
                    " WHERE pm.hostname like '" + hostname + "' AND pm.uploader LIKE '" + strUsername + "' " +
                    " ORDER BY pm.date_inserted ASC ";

            loadImagesFromDb(sqlGalleryUser, arrColumnNamesGallery, true);

            verticalLayout.add(loadFooter());
        } else if (section.equalsIgnoreCase(SECTION_LOG)) {
            verticalLayout.add(loadHeader("logs", "View", SECTION_LOG));

            String arrColumns[] = {"visitorLogId", "timeOfVisit", "browserName"};

            String sqlRead = "SELECT  visitorLogId, timeOfVisit, browserName " +
                    " from journey.dbvisitor_log " +
                    " Order By visitorLogId desc";
            recordService.findAll(sqlRead, arrColumns);

            verticalLayout.add(loadFooter());
        } else if (section.equalsIgnoreCase(SECTION_UPLOAD)) {

            verticalLayout.add(loadHeader("Upload Photos", "Upload my photos.", SECTION_MY_PHOTOS));


            UploadImageCard uploadImageCard = new UploadImageCard(userId, strUsername, sessionCreation, publicIp, hostname);

            uploadImageCard.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    Margin.SMALL,
                    Padding.NONE,
                    Gap.MEDIUM,
                    Background.CONTRAST_5, BorderRadius.LARGE,
                    AlignItems.STRETCH, //JustifyContent.BETWEEN,
                    JustifyContent.EVENLY
            );

//            verticalLayout.add(uploadImageCard.getLocationSelectionLayout());
            verticalLayout.add(uploadImageCard.getUploadImageCard(recordService));

            verticalLayout.add(loadFooter());
        } else {
            verticalLayout.add(loadHeader("- -", "", ""));
        }
        logVisitorToDb();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String o) {
        section = o;//beforeEvent.getRouteParameters().get("section").orElse("pictures");
    }

    private void constructUI() {
//        addClassName("image-gallery-view");
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

        final String[] urlHost = {new String(), new String(), new String(), new String(), new String()};

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously
            urlHost[0] = currentUrl.getHost();
            urlHost[1] = currentUrl.getProtocol();
            urlHost[2] = currentUrl.getRef();
            urlHost[3] = currentUrl.getUserInfo();
            urlHost[4] = currentUrl.toExternalForm();
        });

        logger.info("  url:" + urlHost[0] + "  url:" + urlHost[1] + "  url:" + urlHost[2] + "  url:" + urlHost[3] + "  url:" + urlHost[4]);


        publicIp = getClientPublicIp();

        verticalLayout = new VerticalLayout();
        verticalLayout.setId("verticalLayout");
        if (isMobile) {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN,
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
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.XLARGE,
                    Padding.Top.XSMALL,
//                    Gap.LARGE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
            verticalLayout.getStyle().set("gap", "3rem");
        }
        verticalLayout.setMaxWidth("1220px");


//        if (isMobile) {
////            imageContainer.setWidthFull();
//            this.setWidthFull();
//        } else {
//            //           imageContainer.setWidthFull();
//            this.setWidthFull();
//        }


        this.add(verticalLayout);
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
                    Margin.SMALL,
                    Padding.SMALL,
                    Gap.MEDIUM,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }

        VerticalLayout headerTextContainer = new VerticalLayout();
        headerTextContainer.addClassNames(Margin.XSMALL, Gap.XSMALL);

        H3 header = new H3(strHeader);
        header.addClassNames(Margin.Bottom.NONE, Margin.Top.SMALL, FontSize.XXLARGE, TextColor.SECONDARY);
        header.getStyle().set("font-family", "Times-New-Roman, serif");

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(Margin.Bottom.NONE, Margin.Top.NONE, FontSize.MEDIUM, TextColor.SECONDARY);

        headerTextContainer.add(header, subheader);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Most Viewed", "Least Viewed", "Most Favourite", "Least Favourite", "Newest First", "Oldest First", "Most Liked", "Least Liked");
        sortBy.setValue("Most Viewed");

        HorizontalLayout headerContainerSecondary = new HorizontalLayout();
        if (isMobile) {
            headerContainerSecondary.addClassNames(
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
            headerContainerSecondary.addClassNames(
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.SMALL,
                    Padding.SMALL,
                    Gap.MEDIUM,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }


        VerticalLayout layoutFilters = new VerticalLayout();
        if (isMobile) {
            layoutFilters.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE);
        } else {
            layoutFilters.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }

        CheckboxGroup<String> checkboxGroupSubject = new CheckboxGroup<>();
        checkboxGroupSubject.setTooltipText("Subject");
//        checkboxGroupSubject.setLabel("Subject");
        checkboxGroupSubject.setItems("Photography", "Street Photography", "Landscape", "Cityscape");
        //   "Friday", "Saturday", "Sunday");
        // checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
//        Div lblFilterSubject = new Div("Subject");
        if (!strSection.equalsIgnoreCase(SECTION_UPLOAD)) {
            layoutFilters.add(checkboxGroupSubject);
        }

        CheckboxGroup<String> checkboxGroupFormat = new CheckboxGroup<>();
        checkboxGroupFormat.setTooltipText("Format");
//        checkboxGroupFormat.setLabel("Format");
        checkboxGroupFormat.setItems("Book", "Youtube");
//        Div lblFilterFormat = new Div("Format");
        if (strSection.equalsIgnoreCase(SECTION_LEARNINGS)) {
            layoutFilters.add(checkboxGroupFormat);
        }


        CheckboxGroup<String> checkboxGroupLocation = new CheckboxGroup<>();
        checkboxGroupLocation.setTooltipText("Location");
//         checkboxGroupLocation.setLabel("Location");
        checkboxGroupLocation.setItems("Hungary", "UK", "Greece");//, "Thursday",
        //        "Friday", "Saturday", "Sunday");
        // checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
//        Div lblFilterLocation = new Div("Location");
        if (!strSection.equalsIgnoreCase(SECTION_LEARNINGS) && !strSection.equalsIgnoreCase(SECTION_WEBSITES)
                && !strSection.equalsIgnoreCase(SECTION_HOME) && !strSection.equalsIgnoreCase(SECTION_UPLOAD)) {
            layoutFilters.add(checkboxGroupLocation);
        }

        VerticalLayout layoutHeaderParameters = new VerticalLayout();
        if (isMobile) {
            layoutHeaderParameters.addClassNames(
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.XSMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        } else {
            layoutHeaderParameters.addClassNames(
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.SMALL,
                    Padding.SMALL,
                    Gap.XSMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }


        Select<String> cmbView = new Select<>();
        cmbView.setLabel("View");
        if (strSection.equalsIgnoreCase(SECTION_GALLERY) || strSection.equalsIgnoreCase(SECTION_MY_PHOTOS)) {
            cmbView.setItems("Micro View", "Ordinary - No MetaData", "Ordinary - MetaData Bottom", "Ordinary - MetaData Right",
                    "Wide - No MetaData", "Wide - MetaData Bottom", "Wide - MetaData Right");
            cmbView.setValue("Ordinary - No MetaData");
        } else {
            cmbView.setItems("Micro View", "Ordinary View", "Wide View");
            cmbView.setValue("Ordinary View");
        }


        if (strSection.equalsIgnoreCase(SECTION_UPLOAD)) {
            headerContainerMaster.add(headerTextContainer);
            headerContainerSecondary.add(layoutFilters);
        } else {
            headerContainerMaster.add(headerTextContainer, sortBy);
            headerContainerSecondary.add(layoutFilters, cmbView);
        }

        layoutHeaderParameters.add(headerContainerMaster, headerContainerSecondary);

        return layoutHeaderParameters;
    }

    private void loadImagesFromDb(String sqlRead, String[] arrColumnNames, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar;

        Div divGallery = new Div();
        divGallery.addClassName("gallery");

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            Record rec = lstRecords.get(r);
            divGallery.add(loadImageGalleryThumbsFromDb(rec, isEditable));
        }
        verticalLayout.add(divGallery);

    }

    private GalleryImageViewCard loadImageGalleryThumbsFromDb(Record record, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;


        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");

        String strCityName = record.getColumnData("city_name");
        String strUploader = record.getColumnData("uploader");

        RouteParam routeUploader = new RouteParam("member", strUploader);
        RouterLink linkUploader = new RouterLink(strUploader, GalleryView.class, new RouteParameters(routeUploader));

        RouteParam routeDestination = new RouteParam("destination", strCityName);
        RouterLink linkDestination = new RouterLink(strCityName, GalleryView.class, new RouteParameters(routeDestination));

//        ArrayList<RouterLink> lstRouterLinks =new ArrayList<>();
//        lstRouterLinks.add(linkDestination);


        String strImagePath = strPath + dirChar + strFileName;
        logger.info(" strImagePath " + strImagePath);

        GalleryImageViewCard imageGalleryViewCard = new GalleryImageViewCard(record, strImagePath, isMobile, userId, strUsername, sessionCreation, hostname, publicIp, isEditable,
                recordService);
        imageGalleryViewCard.addClassNames(Background.CONTRAST_5, BorderColor.CONTRAST_10, TextColor.TERTIARY);
        imageGalleryViewCard.addClassName("image-card");
        imageGalleryViewCard.addClassName("bottom-radius-shadow");


        return imageGalleryViewCard;
    }

//    private OrderedList loadImageThumbsFromDb(Record record, boolean isEditable) {
//        strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;
//
//        OrderedList imageContainer = new OrderedList();
//        if(isMobile){
//            imageContainer.addClassNames(
////                    Overflow.HIDDEN,
//                    Width.FULL,
//                    Margin.SMALL,
//                    Margin.Left.NONE, Margin.Right.NONE, Margin.Horizontal.NONE,
//                    Padding.SMALL,
//                    Gap.MEDIUM,
//                    BorderRadius.NONE,
//                    AlignItems.START, JustifyContent.BETWEEN,
//
//                    ListStyleType.NONE
//            );
//        }else {
//            imageContainer.addClassNames(
////                    Overflow.HIDDEN,
//                    Width.FULL,
//                    Margin.SMALL,
//                    Margin.Left.NONE, Margin.Right.NONE, Margin.Horizontal.NONE,
//                    Padding.SMALL,
//                    Gap.MEDIUM,
//                    BorderRadius.NONE,
//                    AlignItems.START, JustifyContent.BETWEEN,

    /// /                    Display.GRID,
//                    Display.FLEX, FlexWrap.WRAP,
//                    ListStyleType.NONE
//            );
//        }
//
//        String strFileName = record.getColumnData("name_new");
//        String strTitle = record.getColumnData("title");
//        String strSubTitle = record.getColumnData("subtitle");
//        String strPhotoType = record.getColumnData("photo_type");
//
//                            String strImagePath = strPath + dirChar + strFileName;
//                            logger.info(" strImagePath "+strImagePath);
//
//                            GalleryImageViewCard imageGalleryViewCard = new GalleryImageViewCard(record,strImagePath,isMobile,userId, strUsername, sessionCreation,hostname,publicIp,isEditable , recordService);
//
//                            imageContainer.add(imageGalleryViewCard);
//
//        return imageContainer;
//    }
    private void loadLocations(String sqlRead, String[] arrColumnNames) {


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        List<String> lstStrLocation = new ArrayList<String>();

        for (int r = 0; r < lstRecords.size(); r++) {

            Record rec = lstRecords.get(r);

            String strCityName = rec.getColumnData("city_name");
            String strCountry = rec.getColumnData("country");
            String destination = strCityName + "." + strCountry;

            verticalLayout.add(getLocation(rec));

//            if(lstStrLocation.isEmpty()){
//                lstStrLocation.add(destination);
//                verticalLayout.add(getLocation(rec));
//            }else {
//                for (int i = 0; i < lstStrLocation.size(); i++) {
//                    if (lstStrLocation.get(i).toString().equalsIgnoreCase(destination)) {
//
//                    } else {
//                        lstStrLocation.add(destination);
//                        verticalLayout.add(getLocation(rec));
//                    }
//                }
//            }
        }


    }

    private void loadClubs(String sqlRead, String[] arrColumnNames) {


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            Record rec = lstRecords.get(r);
            verticalLayout.add(getClubItem(rec));
        }

    }

    private void loadFestivals(String sqlRead, String[] arrColumnNames) {
        strPath = DIR_PHOTOS_SERVER + dirChar;

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);

        for (int r = 0; r < lstRecords.size(); r++) {
            Record rec = lstRecords.get(r);

            verticalLayout.add(getFestival(rec));
        }


    }

    private void loadLearnings(String sqlRead, String[] arrColumnNames) {
        strPath = DIR_PHOTOS_SERVER + dirChar;


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            Record rec = lstRecords.get(r);
            verticalLayout.add(getLearningsItem(rec));
        }


    }

    private void loadWebSites(String sqlRead, String[] arrColumnNames) {


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            Record rec = lstRecords.get(r);
            verticalLayout.add(getWebsiteItem(rec));
        }


    }

    public VerticalLayout loadFooter() {

        H2 divTitle = new H2(APP_NAME);
//        divTitle.addClassName(TextColor.TERTIARY);

        Div divPhotoact = new Div("Act around Photography");

//        HorizontalLayout layoutLine = new HorizontalLayout();
//        if(isMobile) {
//            layoutLine.addClassNames(
//                    Overflow.HIDDEN, Width.FULL,
//                    AlignItems.CENTER, JustifyContent.AROUND,
//                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.XSMALL,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_10,
//                    Border.BOTTOM, Border.RIGHT,
////                    BorderColor.CONTRAST_20,
//                    BorderRadius.NONE);
//        }else{
//            layoutLine.addClassNames(
//                    Overflow.HIDDEN, Width.FULL,
//                    AlignItems.CENTER, JustifyContent.AROUND,
//                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.MEDIUM,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_10,
//                    Border.BOTTOM, Border.RIGHT,
////                    BorderColor.CONTRAST_20,
//                    BorderRadius.NONE);
//        }
//
//        layoutLine.add(divTitle);


        VerticalLayout layoutFooter = new VerticalLayout();
//        layoutFooter.addClassName("bottom-radius-shadow");

        if (isMobile) {
            layoutFooter.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_5,
                    BorderRadius.NONE);
        } else {
            layoutFooter.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_5,
                    BorderRadius.NONE);
        }
//        layoutFooter.addClassName("footer");

        layoutFooter.add(divTitle, divPhotoact);
        return layoutFooter;
    }

    public VerticalLayout getFestival(Record record) {


        HorizontalLayout layoutSection = new HorizontalLayout();
        layoutSection.addClassName("category");
//        layoutSection.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);
        String strType = record.getColumnData("type");


        Div divImage = new Div();
//        divImage.addClassName("category");
//        divImage.getStyle().setColor(strColorOfIcons);
        Div linkCategoryRelated = new Div(strType);//,"",);
//        linkCategoryRelated.addClassName("category");
        divImage.add(LineAwesomeIcon.OBJECT_GROUP.create());
        layoutSection.add(divImage, linkCategoryRelated);

        String strName = record.getColumnData("nameShort");
        H5 titleName = new H5(strName);
        titleName.addClassName(TextColor.SECONDARY);
        titleName.setClassName("lazy-result-line-title");

        String strDate = "";
        String dt = record.getColumnData("formatedDateUpdated");

        Div dayUpdated = new Div("updated: " + dt);
        dayUpdated.addClassName(TextColor.TERTIARY);

        HorizontalLayout layoutPostTitle = new HorizontalLayout();
        if (isMobile) {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.XSMALL,
                    Padding.XSMALL,
                    Gap.XSMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_10,
                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
        } else {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.SMALL,
                    Padding.SMALL,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_10,
                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
        }

        layoutPostTitle.add(layoutSection, titleName, dayUpdated);

        VerticalLayout layoutFestivalInfo = new VerticalLayout();
        layoutFestivalInfo.addClassName("bottom-radius-shadow");
        if (isMobile) {
            layoutFestivalInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_5, BorderRadius.NONE);
        } else {
            layoutFestivalInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_5, BorderRadius.LARGE);
        }

        layoutFestivalInfo.add(layoutPostTitle, getFestivalItem(record), getSubTabs("festival", strName, record), getFestivalActions());

        return layoutFestivalInfo;
    }

    private VerticalLayout getFestivalItem(Record record) {

        String strPeriodOfYear = record.getColumnData("periodOfYear");

        String location = record.getColumnData("location");
        String strCuntry = record.getColumnData("country");
        String strImageLogo;
        String strImageTop;


        String strImgLogoPath = record.getColumnData("image_logo");
        String strImgTopPath = record.getColumnData("image_top");

        if (!strImgLogoPath.equalsIgnoreCase("null") && !strImgLogoPath.equalsIgnoreCase("")) {
            strImageLogo = strPath + "/" + strImgLogoPath;
        } else {
            strImageLogo = "";
        }

        if (!strImgTopPath.equalsIgnoreCase("null") && !strImgTopPath.equalsIgnoreCase("")) {
            strImageTop = strPath + "/" + strImgTopPath;
        } else {
            strImageTop = "";
        }

        HorizontalLayout layoutImage = new HorizontalLayout();
        layoutImage.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.START,
                Margin.SMALL,
                Padding.SMALL,
                Gap.SMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                Background.CONTRAST_5, BorderRadius.LARGE
        );

        Scroller scrFestImages = new Scroller(layoutImage);
        scrFestImages.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE
        );

        scrFestImages.setScrollDirection(Scroller.ScrollDirection.HORIZONTAL);

        if (!strImageLogo.equalsIgnoreCase("null") && !strImageLogo.equalsIgnoreCase("")) {

            final StreamResource imageResourceLogo = new StreamResource("image-logo", () -> {
                try {
                    return new FileInputStream(new File(strImageLogo));
                } catch (final FileNotFoundException e) {
                    logger.error("FileNotFoundException club-festival logo " + e.getMessage());
                    return null;
                }
            });

            Image imgLogo = new Image(imageResourceLogo, "image-logo");
            imgLogo.setHeight("200px");
            imgLogo.getStyle().set("border-radius", "50%");
            imgLogo.getStyle().set("box-shadow", "0 10px 50px rgba(207, 208, 208, 0.65)");

            layoutImage.add(imgLogo);
        }

        if (!strImageTop.equalsIgnoreCase("null") && !strImageTop.equalsIgnoreCase("")) {

            final StreamResource imageResourceTop = new StreamResource("image-top", () -> {
                try {
                    return new FileInputStream(new File(strImageTop));
                } catch (final FileNotFoundException e) {
                    logger.error("FileNotFoundException club-festival top " + e.getMessage());
                    return null;
                }
            });

            Image imgTop = new Image(imageResourceTop, "image-top");
            imgTop.setHeight("200px");
            imgTop.getStyle().set("border-radius", "9px");

            layoutImage.add(imgTop);
        }


        String strActivities = record.getColumnData("activities");
        if (strActivities != null && !strActivities.trim().equalsIgnoreCase("") && !strActivities.trim().equalsIgnoreCase("null")) {

        } else {
            strActivities = "";
        }

        Paragraph parDescription = new Paragraph("It takes place each year  in " + location + " (" + strCuntry + ") usually during " + strPeriodOfYear + ". " + strActivities);
        parDescription.addClassNames(TextColor.SECONDARY);

        VerticalLayout layoutSourceCard = new VerticalLayout();
        layoutSourceCard.addClassNames(
                Overflow.HIDDEN, //Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.SMALL,
                Padding.SMALL,
                Gap.MEDIUM,
                TextColor.SECONDARY,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                Background.CONTRAST_5,
                Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE
        );
        layoutSourceCard.setMaxWidth("250px");

        HorizontalLayout layoutExtLinks = new HorizontalLayout();
        layoutExtLinks.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.SMALL,
                Padding.NONE,
                Gap.MEDIUM
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //    Background.CONTRAST_5,
                //BorderRadius.LARGE
        );
        layoutExtLinks.addClassNames("external-links");

        Anchor linkWebsite = new Anchor();
        linkWebsite.add(FontAwesome.Solid.LINK.create());
        //linkWebsite.getStyle().setColor(strColorExternalweb);
//        linkWebsite.setClassName("external-links");
        String festUrl = record.getColumnData("website");
        //"fest url: "+ festUrl);
        if (!festUrl.equalsIgnoreCase("null") && !festUrl.equalsIgnoreCase("")) {
//            linkWebsite.setText("Website");
            //link1InNewTab.setTarget(festUrl);
            linkWebsite.setHref(festUrl);
            linkWebsite.setTarget("_blank");
            //link1InNewTab.getElement().setAttribute("target", "_blank");
            linkWebsite.setVisible(true);
        } else {
            linkWebsite.setVisible(false);
        }

        Anchor linkTutorWikipedia = new Anchor();
        linkTutorWikipedia.add(FontAwesome.Brands.WIKIPEDIA_W.create());
        //  linkTutorWikipedia.getStyle().setColor(strColorExternalweb);
//        linkTutorWikipedia.addClassName("external-links");
        linkTutorWikipedia.setVisible(false);
        String strUrlTutorWikipedia = record.getColumnData("url_wikipedia");
        if (!strUrlTutorWikipedia.equalsIgnoreCase("null") && !strUrlTutorWikipedia.equalsIgnoreCase("")) {

            //linkTutorYt.setText("YouTube");
            //strUrlTutorWikipedia = "https://www.youtube.com/"+strUrlTutorYt;
            linkTutorWikipedia.setHref(strUrlTutorWikipedia);
            linkTutorWikipedia.setTarget("_blank");
            linkTutorWikipedia.setVisible(true);
        }

        Anchor linkFacebookNewTab = new Anchor();
        // linkFacebookNewTab.getStyle().setColor(strColorExternalweb);
        // linkFacebookNewTab.addClassName("external-links");
        linkFacebookNewTab.add(FontAwesome.Brands.FACEBOOK.create());
        String festUrlFace = record.getColumnData("url_facebook");
        //"fest url: "+ festUrl);
        if (!festUrlFace.equalsIgnoreCase("null") && !festUrlFace.equalsIgnoreCase("")) {
            //linkFacebookNewTab.setText("Facebook");
            linkFacebookNewTab.setHref(festUrlFace);
            linkFacebookNewTab.setTarget("_blank");
            linkFacebookNewTab.setVisible(true);
        } else {
            linkFacebookNewTab.setVisible(false);
        }

        Anchor linkInstaNewTab = new Anchor();
        linkInstaNewTab.add(FontAwesome.Brands.INSTAGRAM.create());
        //linkInstaNewTab.getStyle().setColor(strColorExternalweb);
        // linkInstaNewTab.setClassName("external-links");
        String festUrlInsta = record.getColumnData("url_instagram");
        //"fest url: "+ festUrl);
        if (!festUrlInsta.equalsIgnoreCase("null") && !festUrlInsta.equalsIgnoreCase("")) {
            // linkInstaNewTab.setText("Instagram");
            linkInstaNewTab.setHref(festUrlInsta);
            linkInstaNewTab.setTarget("_blank");
            linkInstaNewTab.setVisible(true);
        } else {
            linkInstaNewTab.setVisible(false);
        }

        Anchor linkYTNewTab = new Anchor();
        linkYTNewTab.add(FontAwesome.Brands.YOUTUBE.create());
        //linkYTNewTab.getStyle().setColor(strColorExternalweb);
//        linkYTNewTab.setClassName("external-links");
        String festUrlYT = record.getColumnData("url_youtube");
        //"fest url: "+ festUrl);
        if (!festUrlYT.equalsIgnoreCase("null") && !festUrlYT.equalsIgnoreCase("")) {
            // linkYTNewTab.setText("YouTube");
            linkYTNewTab.setHref(festUrlYT);
            linkYTNewTab.setTarget("_blank");
            linkYTNewTab.setVisible(true);
        } else {
            linkYTNewTab.setVisible(false);
        }

        layoutExtLinks.add(linkWebsite, linkFacebookNewTab, linkInstaNewTab, linkYTNewTab);

        StreamResource iconInfo = new StreamResource("info-circle-line-icon.svg",
                () -> getClass().getResourceAsStream("/icons/info-circle-line-icon.svg"));
        SvgIcon svgInfo = new SvgIcon(iconInfo);

        Div imgInfo = new Div(svgInfo);

        layoutSourceCard.add(imgInfo, layoutExtLinks);

        Paragraph parTab1 = new Paragraph("activities planned");

        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strDateFrom = record.getColumnData("formatedDateFrom");
        String strDateTo = record.getColumnData("formatedDateTo");
        String strEdition_description = record.getColumnData("edition_description");

        HorizontalLayout layoutPlannedTitle = new HorizontalLayout();
        layoutPlannedTitle.setWidthFull();
        layoutPlannedTitle.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
        layoutPlannedTitle.getStyle().setAlignItems(Style.AlignItems.CENTER);

        Div h5Title = new Div(strTitle);
        h5Title.addClassNames(//FontWeight.SEMIBOLD,
                TextColor.SECONDARY
        );

        Div h6DateFrom = new Div(strDateFrom);
        h6DateFrom.addClassNames(FontWeight.SEMIBOLD,
                TextColor.SECONDARY
        );
        Div h6DateTo = new Div(strDateTo);
        h6DateTo.addClassNames(FontWeight.SEMIBOLD,
                TextColor.SECONDARY
        );

        String strTakesPlace = "takes place between";

        Div divTakesPlace = new Div(strTakesPlace);
        divTakesPlace.addClassNames(
                TextColor.TERTIARY
        );

        Div divAnd = new Div("and");
        divAnd.addClassNames(
                TextColor.TERTIARY
        );

        layoutPlannedTitle.add(h5Title, divTakesPlace, h6DateFrom, divAnd, h6DateTo);

        Div divSubTitle = new Div(strSubTitle);
        divSubTitle.setWidthFull();
        divSubTitle.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
        divSubTitle.getStyle().setAlignItems(Style.AlignItems.CENTER);

        Paragraph parEdDescription = new Paragraph(strEdition_description);
        parEdDescription.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
        parEdDescription.getStyle().setAlignItems(Style.AlignItems.CENTER);
        parEdDescription.setWidthFull();

        VerticalLayout layoutPlanned = new VerticalLayout();
        layoutPlanned.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.SMALL,
                Padding.NONE,
                Gap.SMALL,
                BorderRadius.LARGE
        );

        if (!strDateFrom.trim().isEmpty() && !strDateFrom.trim().equalsIgnoreCase("null")) {
            layoutPlanned.add(layoutPlannedTitle);//, divSubTitle, parEdDescription);
        } else {
            Div divNoEvents = new Div("Currently we have no info on future events. If you have any specific indication inform us here.");
            divNoEvents.addClassNames(TextColor.TERTIARY);
            layoutPlanned.add(divNoEvents);
        }

        HorizontalLayout layoutInfoAbout = new HorizontalLayout();
        layoutInfoAbout.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.SMALL
        );

        VerticalLayout layoutDetails = new VerticalLayout();
        layoutDetails.addClassNames(
                Overflow.HIDDEN,// Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.SMALL,
                Padding.NONE,
                Gap.SMALL
        );

        layoutDetails.add(parDescription, layoutPlanned);
        layoutInfoAbout.add(layoutDetails, layoutSourceCard);

        VerticalLayout layoutFestivalInfo = new VerticalLayout();
        if (isMobile) {
            layoutFestivalInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    BorderRadius.NONE);
        } else {
            layoutFestivalInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    BorderRadius.LARGE);
        }
        layoutFestivalInfo.add(scrFestImages, layoutInfoAbout);


        return layoutFestivalInfo;
    }

    private HorizontalLayout getFestivalActions() {

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
        layoutActions.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.MEDIUM,
                Padding.NONE,
                Gap.LARGE,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.LARGE);
        layoutActions.addClassNames("actions");

        layoutActions.add(btnLike, btnMoreAction, btnComment, btnMoreInfo, btnShare);

        return layoutActions;
    }

    public VerticalLayout getLearningsItem(Record record) {

        String strTitle = record.getColumnData("title");
        String strSubject = record.getColumnData("subject");

        String strFormat = record.getColumnData("format");
        String strDuration = record.getColumnData("duration");
        String strPages = record.getColumnData("pages");

        String strTutor = record.getColumnData("tutor_name");
        Div divTutor = new Div();
        divTutor.addClassName(TextColor.SECONDARY);
        divTutor.setVisible(false);
        if (!strTutor.equalsIgnoreCase("null") && !strTutor.isEmpty()) {
            divTutor.setText(strTutor);
            divTutor.setVisible(true);
        }

        String strTutorTeam = record.getColumnData("learnings_team_id");
        Div divTutorTeam = new Div();
        divTutorTeam.addClassName(TextColor.SECONDARY);
        divTutorTeam.setVisible(false);
        if (!strTutorTeam.equalsIgnoreCase("null") && !strTutorTeam.isEmpty()) {
            divTutorTeam.setText(strTutorTeam);
            divTutorTeam.setVisible(true);
        }

        String strImage = record.getColumnData("picture");

        if (!strImage.equalsIgnoreCase("null") && !strImage.equalsIgnoreCase("")) {
            strImage = strPath + "/" + strImage;
        } else {
            strImage = "";
        }

//        HorizontalLayout layoutSection = new HorizontalLayout();
//
//        StreamResource iconCinema = new StreamResource("film-camera-icon.svg",
//                () -> getClass().getResourceAsStream("/icons/film-camera-icon.svg"));
//        SvgIcon svgCinema = new SvgIcon(iconCinema);
//        Div divImage = new Div();
//        divImage.getStyle().setColor("#c20853");
//        Div divSection = new Div(section);
//        divSection.getStyle().setColor("#c20853");


        HorizontalLayout layoutSection = new HorizontalLayout();
        layoutSection.addClassName("category"); //.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);


        Div divImage = new Div();
//        divImage.addClassName("category");//.getStyle().setColor(strColorOfIcons);
        Div linkCategoryRelated = new Div("Learnings");//,"",);
//        linkCategoryRelated.addClassName("category");//.getStyle().setColor(strColorOfIcons);
        divImage.add(LineAwesomeIcon.BOOK_SOLID.create());
        layoutSection.add(divImage, linkCategoryRelated);


//        RouteParam routeSection = new RouteParam("section", section);
//        RouteParam routeItem = new RouteParam("subsection", strSubject);
//
//        RouterLink linkPhotoSubSection = new RouterLink(strSubject, PhotoView.class,new RouteParameters(routeSection,routeItem));
//        linkPhotoSubSection.setClassName("lazy-result-line-button");


//        String strDate = "";
//        String dt = record.getColumnData("dateInsert");
//        SimpleDateFormat toui = new SimpleDateFormat("dd/MM/yyyy");
//        SimpleDateFormat fromdb = new SimpleDateFormat("yyyy-MM-dd");
//
//        try {
//
//            strDate = toui.format(fromdb.parse(dt));
//        } catch (ParseException e) {
//            logger.error(e.getMessage());
//        }

        H5 titleName = new H5(strTitle);
        titleName.addClassName(TextColor.SECONDARY);
        titleName.addClassName("lazy-result-line-title");

//        H6 dayUpdated = new H6("updated: "+strDate);
//        dayUpdated.getStyle().setColor("#8b94a0");

        HorizontalLayout layoutPostTitle = new HorizontalLayout();
        if (isMobile) {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.XSMALL,
                    Padding.XSMALL,
                    Gap.XSMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_10,
                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
        } else {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.SMALL,
                    Padding.SMALL,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_10,
                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
        }

//        layoutPostTitle.setWidthFull();
//        layoutPostTitle.setClassName("lazy-result-line-title-align");
//        //layoutPostTitle.getStyle().setAlignItems(Style.AlignItems.CENTER);
//        //layoutPostTitle.getStyle().setJustifyContent(Style.JustifyContent.SPACE_BETWEEN);
//        layoutPostTitle.setPadding(true);
//        layoutPostTitle.setSpacing(true);
//        layoutPostTitle.setMargin(true);
        //layoutPostTitle.addClassName("lazy-result-line-title-align");
        layoutPostTitle.add(layoutSection, titleName, divTutor);


        VerticalLayout layoutLearningInfo = new VerticalLayout();
        layoutLearningInfo.addClassName("bottom-radius-shadow");

        if (isMobile) {
            layoutLearningInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_5, BorderRadius.NONE);
        } else {
            layoutLearningInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_5, BorderRadius.LARGE);
        }

        HorizontalLayout layoutImage = new HorizontalLayout();
        layoutImage.addClassNames(Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE);

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

            Image img = new Image(imageResource, "image");
            img.setMaxHeight("240px");
            img.getStyle().set("border-radius", "9px");

            layoutImage.add(img);

        }

        Div divFormat = new Div();
        if (strFormat.equalsIgnoreCase("YouTube")) {
            if (!strDuration.equalsIgnoreCase("null") && !strDuration.equalsIgnoreCase("")) {
                divFormat.setText(strFormat + "(" + strDuration + ")");
            } else {
                divFormat.setText(strFormat);
            }
        } else if (strFormat.equalsIgnoreCase("book")) {
            layoutImage.setMaxWidth("250px");
            if (!strPages.equalsIgnoreCase("null") && !strPages.equalsIgnoreCase("")) {
                divFormat.setText(strFormat + "(" + strPages + " pages)");
            } else {
                divFormat.setText(strFormat);
            }
        }

        Anchor linkTutor = new Anchor();
        linkTutor.add(FontAwesome.Solid.LINK.create());
        // linkTutor.getStyle().setColor(strColorExternalweb);
        //  linkTutor.setClassName("lazy-result-line-button");

        String strUrlTutorExt = record.getColumnData("website");
        if (!strUrlTutorExt.equalsIgnoreCase("null") && !strUrlTutorExt.equalsIgnoreCase("")) {

            // linkTutor.setText("Website");
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
            strUrlTutorYt = "https://www.youtube.com/" + strUrlTutorYt;
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

        String strDescription = record.getColumnData("description");

        Paragraph parDescription = new Paragraph(strDescription);
        if (!strDescription.equalsIgnoreCase("null") && !strDescription.equalsIgnoreCase("")) {
            parDescription.setVisible(true);
        } else {
            parDescription.setVisible(false);
        }
        Anchor link1InNewTab = new Anchor();

        String strUrl = record.getColumnData("url");
        String strYouTubeVideo = "https://www.youtube.com/watch?v=";
        String strVideoOnly = strUrl.replace(strYouTubeVideo, "");

        String youtubeEmbedded = "<p><iframe width='660' height='390' src='https://www.youtube.com/embed/" + strVideoOnly + "' title='" + strTitle + "' frameBorder='0'   allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share'  allowFullScreen></iframe></p>";

        Html video = new Html(youtubeEmbedded);
        video.setHtmlContent(youtubeEmbedded);


        Div divVideo = new Div();
        video.addClassNames(Padding.SMALL, Background.CONTRAST, Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE);
        divVideo.setClassName("lazy-video-background");
        divVideo.add(video);

        VerticalLayout layoutSourceCard = new VerticalLayout();
        layoutSourceCard.addClassNames(
                Overflow.HIDDEN,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.SMALL,
                Padding.SMALL,
                Gap.MEDIUM,
                TextColor.SECONDARY,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                Background.CONTRAST_5,
                Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE
        );

        layoutSourceCard.setMaxWidth("250px");

        Div divTutorInfo = new Div();
        divTutorInfo.addClassName(TextColor.SECONDARY);
        divTutorInfo.setVisible(false);
        if (!strTutor.equalsIgnoreCase("null") && !strTutor.isEmpty()) {
            divTutorInfo.setText(strTutor);
            divTutorInfo.setVisible(true);

        }


        StreamResource iconInfo = new StreamResource("info-circle-line-icon.svg",
                () -> getClass().getResourceAsStream("/icons/info-circle-line-icon.svg"));
        SvgIcon svgInfo = new SvgIcon(iconInfo);

        StreamResource iconTutor = new StreamResource("man-user-circle-black-icon.svg",
                () -> getClass().getResourceAsStream("/icons/man-user-circle-black-icon.svg"));
        SvgIcon svgTutor = new SvgIcon(iconTutor);


        Div imgInfo = new Div(svgInfo);
        Div imgPerson = new Div(svgTutor);
        Div divLearningCat = new Div(strSubject);


        RouteParam routeSectionTutor = new RouteParam("section", section);
        RouteParam routeItemTutor = new RouteParam("subsection", strTutor);

//        RouterLink linkPhotoSubSectionTutor = new RouterLink(strTutor, PhotoView.class,new RouteParameters(routeSectionTutor,routeItemTutor));
//        linkPhotoSubSectionTutor.setClassName("lazy-result-line-button");

//        VerticalLayout layoutExtLinks = new VerticalLayout();
//        layoutExtLinks.setWidthFull();
        HorizontalLayout layoutExtLinks = new HorizontalLayout();
        layoutExtLinks.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                Margin.SMALL,
                Padding.NONE,
                Gap.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER

        );
        layoutExtLinks.addClassNames("external-links");

        layoutExtLinks.add(linkTutor, linkTutorWikipedia, linkTutorInsta, linkTutorYt);
        layoutSourceCard.add(imgInfo, divFormat, divLearningCat, imgPerson, divTutorInfo, divTutorTeam, layoutExtLinks);

        HorizontalLayout layoutIdInfo = new HorizontalLayout();
        layoutIdInfo.getStyle().setAlignItems(Style.AlignItems.CENTER);
        layoutIdInfo.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
        layoutIdInfo.setWidthFull();
        layoutIdInfo.add(layoutImage, divVideo, layoutSourceCard);


        if (!strUrl.equalsIgnoreCase("null") && !strUrl.equalsIgnoreCase("")) {
            if (strFormat.equalsIgnoreCase("YouTube")) {
                link1InNewTab.setVisible(false);
                divVideo.setVisible(true);
                layoutImage.setVisible(false);
            } else {
                link1InNewTab.setText(strUrl);
                //link1InNewTab.setTarget(festUrl);
                link1InNewTab.setHref(strUrl);
                link1InNewTab.setTarget("_blank");
                //link1InNewTab.getElement().setAttribute("target", "_blank");
                link1InNewTab.setVisible(true);
                divVideo.setVisible(false);
                layoutImage.setVisible(true);
            }
        } else {
            link1InNewTab.setVisible(false);
            divVideo.setVisible(false);
            layoutImage.setVisible(true);
        }


//        H6 headerPoll = new H6("Evaluation by Members");
//        headerPoll.setWidthFull();
//        headerPoll.getStyle().setTextAlign(Style.TextAlign.CENTER);
//
//        Div layoutPollQnA = new Div();
//        layoutPollQnA.setClassName("lazy-poll-container");
//        layoutPollQnA.setWidthFull();
//
//        Div layoutPoll = new Div();
//        layoutPoll.setWidthFull();
//
//
//        Paragraph par = new Paragraph("(1 very bad, 2 bad ,3 average, 4 good, 5 very good)");
//        par.setWidthFull();
//        par.getStyle().setTextAlign(Style.TextAlign.CENTER);
//        par.getStyle().setColor("#5d6f87");
//
//        layoutPoll.getStyle().setColor("#5d6f87");
//        layoutPoll.add(headerPoll,par,layoutPollQnA);
//
//        String vote1 = "1.Very Good";
//        String vote2 = "2.Good";
//        String vote3 = "3.Average";
//        String vote4 = "4.Bad";
//        String vote5 = "5.Very Bad";
//
//        final String GRAPH_MAX_WIDTH = "310px";
//        final String TITLE_MIN_HEIGHT = "70px";

//        ApexChartsBuilder charts1 = new ApexChartsBuilder();
//        charts1.withChart(ChartBuilder.get()
//                        .withType(Type.PIE).withHeight("230px")
//                        .build())
//                .withLabels(vote1,vote2, vote3, vote4, vote5)
//                .withLegend(LegendBuilder.get()
//                        .withPosition(Position.LEFT)
//                        .withHorizontalAlign(HorizontalAlign.LEFT)
//                        .build())
//                .withSeries(44.0, 55.0, 13.0, 43.0, 22.0)
//                .withResponsive(ResponsiveBuilder.get()
//                        .withBreakpoint(480.0)
//                        .build())
//                //.withTitle(title1)
//                .build();
//
//        Div divTitle1 = new Div("Does the author appear to have knowledge on the field, and presents it in an understood way?");
//        divTitle1.getStyle().setColor("#5d6f87");
//        divTitle1.setWidthFull();
//        divTitle1.setMinHeight(TITLE_MIN_HEIGHT);
//        Div layoutGraph1= new Div();
//        layoutGraph1.setClassName("lazy-poll-graph");
//        layoutGraph1.setMaxWidth(GRAPH_MAX_WIDTH);
//        layoutGraph1.add(divTitle1,charts1.build());
//
//
//        ApexChartsBuilder charts4 = new ApexChartsBuilder();
//        charts4.withChart(ChartBuilder.get()
//                        .withType(Type.PIE).withHeight("230px")
//                        .build())
//                .withLabels(vote1,vote2, vote3, vote4, vote5)
//                .withLegend(LegendBuilder.get()
//                        .withPosition(Position.LEFT)
//                        .withHorizontalAlign(HorizontalAlign.LEFT)
//                        .build())
//                .withSeries(4.0, 25.0, 95.0, 128.0, 42.0)
//                .withResponsive(ResponsiveBuilder.get()
//                        .withBreakpoint(480.0)
//                        .build())
//                //.withTitle(title2)
//                .build();
//        Div divTitle4 = new Div("Have you spotted enough elements that can be used in your activities (work, hobbies)?");
//        divTitle4.getStyle().setColor("#5d6f87");
//        divTitle4.setMinHeight(TITLE_MIN_HEIGHT);
//        divTitle4.setWidthFull();
//        Div layoutGraph4= new Div();
//        layoutGraph4.setClassName("lazy-poll-graph");
//        layoutGraph4.setMaxWidth(GRAPH_MAX_WIDTH);
//        layoutGraph4.add(divTitle4,charts4.build());
//
//        ApexChartsBuilder charts5 = new ApexChartsBuilder();
//        charts5.withChart(ChartBuilder.get()
//                        .withType(Type.PIE).withHeight("230px")
//                        .build())
//                .withLabels(vote1,vote2, vote3, vote4, vote5)
//                .withLegend(LegendBuilder.get()
//                        .withPosition(Position.LEFT)
//                        .withHorizontalAlign(HorizontalAlign.LEFT)
//                        .build())
//                .withSeries(4.0, 25.0, 95.0, 128.0, 42.0)
//                .withResponsive(ResponsiveBuilder.get()
//                        .withBreakpoint(480.0)
//                        .build())
//                //.withTitle(title2)
//                .build();
//        Div divTitle5 = new Div("Would you spend again what it requires in order to acquire the knowledge it states its providing?");
//        divTitle5.getStyle().setColor("#5d6f87");
//        divTitle5.setMinHeight(TITLE_MIN_HEIGHT);
//        divTitle5.setWidthFull();
//        Div layoutGraph5= new Div();
//        layoutGraph5.setClassName("lazy-poll-graph");
//        layoutGraph5.setMaxWidth(GRAPH_MAX_WIDTH);
//        layoutGraph5.add(divTitle5,charts5.build());
//
//        layoutPollQnA.add(layoutGraph1,layoutGraph4,layoutGraph5);


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
        layoutActions.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.MEDIUM,
                Padding.NONE,
                Gap.LARGE,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.LARGE);
        layoutActions.addClassNames("actions");


        layoutActions.add(btnLike, btnMoreAction, btnComment, btnMoreInfo, btnShare);

        HorizontalLayout layoutPostRelated = new HorizontalLayout();
        layoutPostRelated.setWidthFull();
        layoutPostRelated.setPadding(false);
        layoutPostRelated.setSpacing(false);

        layoutLearningInfo.add(layoutPostTitle, layoutIdInfo, parDescription, getSubTabs("learning", strTitle, record), layoutActions, link1InNewTab);

        return layoutLearningInfo;
    }

    public VerticalLayout getClubItem(Record record) {

        // String[] arrColumnsLearning = {"org_name","org_type","city", "country"};

        String strName = record.getColumnData("org_name");
        String strType = record.getColumnData("org_type");

        String strTypeParent = record.getColumnData("org_type_parent");
        String strCanBeUsedFor = record.getColumnData("used_for");


        String strCountry = record.getColumnData("country");
        String strCity = record.getColumnData("city");

        String strUrl = record.getColumnData("url");

        String strImage = record.getColumnData("picture");

//        "url_local_events", "url_fb", "url_yt", "url_insta",
//                "url_flickr", "url_wikipedia"

        if (!strImage.equalsIgnoreCase("null") && !strImage.equalsIgnoreCase("")) {
            strImage = strPath + "/" + strImage;
        } else {
            strImage = "";
        }


        HorizontalLayout layoutSection = new HorizontalLayout();
        layoutSection.addClassName("category");//addClassNames(AlignItems.CENTER, JustifyContent.CENTER);


        Div divImage = new Div();
//        divImage.addClassName("category"); //.getStyle().setColor(strColorOfIcons);
        Div linkCategoryRelated = new Div("Photo Clubs");//,"",);
//        linkCategoryRelated.addClassName("category"); //.getStyle().setColor(strColorOfIcons);
        divImage.add(LineAwesomeIcon.IMAGE.create());
        layoutSection.add(divImage, linkCategoryRelated);


//        RouteParam routeSection = new RouteParam("section", section);
//        RouteParam routeItem = new RouteParam("subsection", strSubject);
//
//        RouterLink linkPhotoSubSection = new RouterLink(strSubject, PhotoView.class,new RouteParameters(routeSection,routeItem));
//        linkPhotoSubSection.setClassName("lazy-result-line-button");


//        String strDate = "";
//        String dt = record.getColumnData("dateInsert");
//        SimpleDateFormat toui = new SimpleDateFormat("dd/MM/yyyy");
//        SimpleDateFormat fromdb = new SimpleDateFormat("yyyy-MM-dd");
//
//        try {
//
//            strDate = toui.format(fromdb.parse(dt));
//        } catch (ParseException e) {
//            logger.error(e.getMessage());
//        }

        H5 titleName = new H5(strName);
        titleName.addClassName(TextColor.SECONDARY);
        titleName.addClassName("lazy-result-line-title");

        Div divLocation = new Div(strCity + " / " + strCountry);
        divLocation.addClassNames(TextColor.SECONDARY);
//        H6 dayUpdated = new H6("updated: "+strDate);
//        dayUpdated.getStyle().setColor("#8b94a0");

        HorizontalLayout layoutPostTitle = new HorizontalLayout();
        if (isMobile) {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.XSMALL,
                    Padding.XSMALL,
                    Gap.XSMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_10,
                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
        } else {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.SMALL,
                    Padding.SMALL,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_10,
                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
        }

//        layoutPostTitle.setWidthFull();
//        layoutPostTitle.setClassName("lazy-result-line-title-align");
//        //layoutPostTitle.getStyle().setAlignItems(Style.AlignItems.CENTER);
//        //layoutPostTitle.getStyle().setJustifyContent(Style.JustifyContent.SPACE_BETWEEN);
//        layoutPostTitle.setPadding(true);
//        layoutPostTitle.setSpacing(true);
//        layoutPostTitle.setMargin(true);
        //layoutPostTitle.addClassName("lazy-result-line-title-align");
        layoutPostTitle.add(layoutSection, titleName, divLocation);

        VerticalLayout layoutClubInfo = new VerticalLayout();
        layoutClubInfo.addClassName("bottom-radius-shadow");

        if (isMobile) {
            layoutClubInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_5, BorderRadius.NONE);
        } else {
            layoutClubInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_5, BorderRadius.LARGE);
        }

        HorizontalLayout layoutImage = new HorizontalLayout();
        layoutImage.addClassNames(Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE);

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

            Image img = new Image(imageResource, "image");
            img.setMaxHeight("240px");
            img.getStyle().set("border-radius", "9px");

            layoutImage.add(img);
        }

//        Anchor linkTutor = new Anchor();
//        linkTutor.add(FontAwesome.Solid.LINK.create());
//        linkTutor.setClassName("lazy-result-line-button");
//
//        String strUrlTutorExt = record.getColumnData("website");
//        if(!strUrlTutorExt.equalsIgnoreCase("null") && !strUrlTutorExt.equalsIgnoreCase("")) {
//
//            // linkTutor.setText("Website");
//            linkTutor.setHref(strUrlTutorExt);
//            linkTutor.setTarget("_blank");
//        }

//        Anchor linkTutorYt = new Anchor();
//        linkTutorYt.add(FontAwesome.Brands.YOUTUBE.create());
//        linkTutorYt.setClassName("lazy-result-line-button");
//        linkTutorYt.setVisible(false);
//        String strUrlTutorYt = record.getColumnData("url_yt");
//        if(!strUrlTutorYt.equalsIgnoreCase("null") && !strUrlTutorYt.equalsIgnoreCase("")) {
//
//            //linkTutorYt.setText("YouTube");
//            strUrlTutorYt = "https://www.youtube.com/"+strUrlTutorYt;
//            linkTutorYt.setHref(strUrlTutorYt);
//            linkTutorYt.setTarget("_blank");
//            linkTutorYt.setVisible(true);
//        }
//
//        Anchor linkTutorWikipedia = new Anchor();
//        linkTutorWikipedia.add(FontAwesome.Brands.WIKIPEDIA_W.create());
//        linkTutorWikipedia.setClassName("lazy-result-line-button");
//        linkTutorWikipedia.setVisible(false);
//        String strUrlTutorWikipedia = record.getColumnData("url_wikipedia");
//        if(!strUrlTutorWikipedia.equalsIgnoreCase("null") && !strUrlTutorWikipedia.equalsIgnoreCase("")) {
//
//            //linkTutorYt.setText("YouTube");
//            //strUrlTutorWikipedia = "https://www.youtube.com/"+strUrlTutorYt;
//            linkTutorWikipedia.setHref(strUrlTutorWikipedia);
//            linkTutorWikipedia.setTarget("_blank");
//            linkTutorWikipedia.setVisible(true);
//        }
//
//        Anchor linkTutorInsta = new Anchor();
//        linkTutorInsta.setClassName("lazy-result-line-button");
//        linkTutorInsta.add(FontAwesome.Brands.INSTAGRAM.create());
//        linkTutorInsta.setVisible(false);
//        String strUrlTutorInsta = record.getColumnData("url_insta");
//        if(!strUrlTutorInsta.equalsIgnoreCase("null") && !strUrlTutorInsta.equalsIgnoreCase("")) {
//
//            // linkTutorInsta.setText("Instagram");
////            strUrlTutorInsta = "https://www.instagram.com/"+ strUrlTutorInsta;
//            linkTutorInsta.setHref(strUrlTutorInsta);
//            linkTutorInsta.setTarget("_blank");
//            linkTutorInsta.setVisible(true);
//        }

        String strDescription = record.getColumnData("description");

        Paragraph parDescription = new Paragraph(strDescription);
        if (!strDescription.equalsIgnoreCase("null") && !strDescription.equalsIgnoreCase("")) {
            parDescription.setVisible(true);
        } else {
            parDescription.setVisible(false);
        }

        Anchor urlLink = new Anchor();
        urlLink.getStyle().setColor("#8b94a0");
        urlLink.setClassName("lazy-api-link");
        urlLink.setHref(strUrl);
        urlLink.setTarget("_blank");
        urlLink.setText(strUrl.toLowerCase().replace("https://", "").replace("http://", ""));


//
//        VerticalLayout layoutSourceCard = new VerticalLayout();
//        layoutSourceCard.addClassNames(
//                Overflow.HIDDEN,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Margin.SMALL,
//                Padding.SMALL,
//                Gap.MEDIUM,
//                TextColor.SECONDARY,
//                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                Background.CONTRAST_5,
//                Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE
//        );
//
//        layoutSourceCard.setMaxWidth("250px");
//
//        Div divTutorInfo = new Div();
//        divTutorInfo.addClassName(TextColor.SECONDARY);
//        divTutorInfo.setVisible(false);
//        if(!strTutor.equalsIgnoreCase("null") && !strTutor.isEmpty())
//        {
//            divTutorInfo.setText(strTutor);
//            divTutorInfo.setVisible(true);
//
//        }
//
//        StreamResource iconInfo = new StreamResource("info-circle-line-icon.svg",
//                () -> getClass().getResourceAsStream("/icons/info-circle-line-icon.svg"));
//        SvgIcon svgInfo = new SvgIcon(iconInfo);
//
//        StreamResource iconTutor = new StreamResource("man-user-circle-black-icon.svg",
//                () -> getClass().getResourceAsStream("/icons/man-user-circle-black-icon.svg"));
//        SvgIcon svgTutor = new SvgIcon(iconTutor);
//
//        Div imgInfo = new Div(svgInfo);
//        Div imgPerson = new Div(svgTutor);
//
//        RouteParam routeSectionTutor = new RouteParam("section", section);
//        RouteParam routeItemTutor = new RouteParam("subsection", strTutor);

//        RouterLink linkPhotoSubSectionTutor = new RouterLink(strTutor, PhotoView.class,new RouteParameters(routeSectionTutor,routeItemTutor));
//        linkPhotoSubSectionTutor.setClassName("lazy-result-line-button");

//        VerticalLayout layoutExtLinks = new VerticalLayout();
//        layoutExtLinks.setWidthFull();
        HorizontalLayout layoutExtLinks = new HorizontalLayout();
        layoutExtLinks.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                Margin.SMALL,
                Padding.NONE,
                Gap.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER

        );

//        layoutExtLinks.add(linkTutor,linkTutorWikipedia,linkTutorInsta,linkTutorYt);
//        layoutSourceCard.add(imgInfo, divLearningCat, imgPerson, divTutorInfo,divTutorTeam,layoutExtLinks);

//        if(!strUrl.equalsIgnoreCase("null") && !strUrl.equalsIgnoreCase(""))
//        {
//            if(strFormat.equalsIgnoreCase("YouTube"))
//            {
//                link1InNewTab.setVisible(false);
//                divVideo.setVisible(true);
//                layoutImage.setVisible(false);
//            }
//            else {
//                link1InNewTab.setText(strUrl);
//                //link1InNewTab.setTarget(festUrl);
//                link1InNewTab.setHref(strUrl);
//                link1InNewTab.setTarget("_blank");
//                //link1InNewTab.getElement().setAttribute("target", "_blank");
//                link1InNewTab.setVisible(true);
//                divVideo.setVisible(false);
//                layoutImage.setVisible(true);
//            }
//        }
//        else{
//            link1InNewTab.setVisible(false);
//            divVideo.setVisible(false);
//            layoutImage.setVisible(true);
//        }


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
        layoutActions.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.MEDIUM,
                Padding.NONE,
                Gap.LARGE,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.LARGE);
        layoutActions.addClassNames("actions");

        layoutActions.add(btnLike, btnMoreAction, btnComment, btnMoreInfo, btnShare);

        HorizontalLayout layoutPostRelated = new HorizontalLayout();
        layoutPostRelated.setWidthFull();
        layoutPostRelated.setPadding(false);
        layoutPostRelated.setSpacing(false);

        layoutClubInfo.add(layoutPostTitle, parDescription, urlLink, getSubTabs("photoclub", strName, record), layoutActions);

        return layoutClubInfo;
    }

    public VerticalLayout getLocation(Record record) {
        String strCityName = record.getColumnData("city_name");
        String strPerfecture = record.getColumnData("perfecture");

        String strCountry = record.getColumnData("country");


        HorizontalLayout layoutSection = new HorizontalLayout();
        layoutSection.addClassName("category");//addClassNames(AlignItems.CENTER, JustifyContent.CENTER);

        Div divImage = new Div();
//        divImage.addClassName("category"); //.getStyle().setColor(strColorOfIcons);
        Div linkCategoryRelated = new Div("Locations");//,"",);
//        linkCategoryRelated.addClassName("category"); //.getStyle().setColor(strColorOfIcons);
        divImage.add(LineAwesomeIcon.GLOBE_SOLID.create());
        layoutSection.add(divImage, linkCategoryRelated);

        H5 titleName = new H5(strCityName);
        titleName.addClassName(TextColor.SECONDARY);
        titleName.addClassName("lazy-result-line-title");

        Div divLocation = new Div(strCountry);
        divLocation.addClassNames(TextColor.SECONDARY);

        HorizontalLayout layoutPostTitle = new HorizontalLayout();
        if (isMobile) {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.XSMALL,
                    Padding.XSMALL,
                    Gap.XSMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_10,
                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
        } else {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.SMALL,
                    Padding.SMALL,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_10,
                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
        }

        layoutPostTitle.add(layoutSection, titleName, divLocation);
        VerticalLayout layoutLocationInfo = new VerticalLayout();
        layoutLocationInfo.addClassName("bottom-radius-shadow");


        if (isMobile) {
            layoutLocationInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM,
                    Background.CONTRAST_5, BorderRadius.NONE);
        } else {
            layoutLocationInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    Background.CONTRAST_5, BorderRadius.LARGE);
        }

        layoutLocationInfo.add(layoutPostTitle, getLocationItem(record), getLocationSubTabs("location", strCityName));
        layoutLocationInfo.add(getLocationActions());

        return layoutLocationInfo;
    }

    private VerticalLayout getLocationItem(Record record) {

        // String[] arrColumnsLearning = {"city_name", "city_name_local", "perfecture", "country"};

        //"country", "nameShort", "location" , "country" , "periodOfYear" , "type" , "website" , "url_facebook" , "url_instagram" , "url_youtube" , "activities" , "image_top",  "image_logo" , "dateInsert" , "title" , "subtitle" , "formatedDateFrom" , "formatedDateTo" , "edition_description","formatedDateUpdated"

        String strCityName = record.getColumnData("city_name");
        String strPerfecture = record.getColumnData("perfecture");

        String strCountry = record.getColumnData("country");

        VerticalLayout layoutLocationInfo = new VerticalLayout();
        if (isMobile) {
            layoutLocationInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM
            );
        } else {
            layoutLocationInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM
            );
        }

        HorizontalLayout layoutExtLinks = new HorizontalLayout();
        layoutExtLinks.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                Margin.SMALL,
                Padding.NONE,
                Gap.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER
        );

        HorizontalLayout layoutPostRelated = new HorizontalLayout();
        layoutPostRelated.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.SMALL, Padding.NONE);
        layoutPostRelated.add(getWeatherCurrent(strCityName, strCountry));
        layoutPostRelated.add(getLocationMap(strCityName, strCountry));

        layoutLocationInfo.add(layoutPostRelated);

        return layoutLocationInfo;
    }

    private VerticalLayout getLocationMap(String city, String country) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);

        String strHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<title>Add a marker using a place name</title>\n" +
                "<meta name=\"viewport\" content=\"initial-scale=1,maximum-scale=1,user-scalable=no\">\n" +
                "<link href=\"https://api.mapbox.com/mapbox-gl-js/v3.7.0/mapbox-gl.css\" rel=\"stylesheet\">\n" +
                "<script src=\"https://api.mapbox.com/mapbox-gl-js/v3.7.0/mapbox-gl.js\"></script>\n" +
                "<style>\n" +
                "body { margin: 0; padding: 0; }\n" +
                "#map { position: absolute; top: 0; bottom: 0; width: 100%; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div id=\"map\"></div>\n" +
                "\n" +
                "<script src=\"https://unpkg.com/@mapbox/mapbox-sdk/umd/mapbox-sdk.min.js\"></script>\n" +
                "\n" +
                "<script>\n" +
                "\tmapboxgl.accessToken = 'pk.eyJ1Ijoibmlja2dpY2siLCJhIjoiY20xcm9nMTZ5MGJsNDJzczM1aWk0Mm1zdCJ9.qSV85DCU8ewpGjTA3uajpg';\n" +
                "    const mapboxClient = mapboxSdk({ accessToken: mapboxgl.accessToken });\n" +
                "    mapboxClient.geocoding\n" +
                "        .forwardGeocode({\n" +
                "            query: '" + city + ", " + country + "',\n" +
                "            autocomplete: false,\n" +
                "            limit: 1\n" +
                "        })\n" +
                "        .send()\n" +
                "        .then((response) => {\n" +
                "            if (\n" +
                "                !response ||\n" +
                "                !response.body ||\n" +
                "                !response.body.features ||\n" +
                "                !response.body.features.length\n" +
                "            ) {\n" +
                "                console.error('Invalid response:');\n" +
                "                console.error(response);\n" +
                "                return;\n" +
                "            }\n" +
                "            const feature = response.body.features[0];\n" +
                "\n" +
                "            const map = new mapboxgl.Map({\n" +
                "                container: 'map',\n" +
                "                // Choose from Mapbox's core styles, or make your own style with Mapbox Studio\n" +
                "                style: 'mapbox://styles/mapbox/streets-v12',\n" +
                "                center: feature.center,\n" +
                "                zoom: 12\n" +
                "            });\n" +
                "\n" +
                "    // Add the control to the map.\n" +
                "    map.addControl(\n" +
                "        new MapboxGeocoder({\n" +
                "            accessToken: mapboxgl.accessToken,\n" +
                "            language: 'en-GB',\n" +
                "            mapboxgl: mapboxgl\n" +
                "        })\n" +
                "    );\n" +
                "\n" +
                "            // Create a marker and add it to the map.\n" +
                "            new mapboxgl.Marker().setLngLat(feature.center).addTo(map);\n" +
                "        });\n" +
                "\n" +
                "\n" +
                "    map.addControl(new mapboxgl.FullscreenControl());\n" +
                "\n" +
                "</script>\n" +
                "\n" +
                "</body>\n" +
                "</html>";

        //String mapSrc = "https://api.mapbox.com/search/geocode/v6/forward?q=budapest&proximity=ip&access_token=pk.eyJ1Ijoibmlja2dpY2siLCJhIjoiY20xcm9nMTZ5MGJsNDJzczM1aWk0Mm1zdCJ9.qSV85DCU8ewpGjTA3uajpg";

        IFrame mapsFrame = new IFrame();
        mapsFrame.setSrcdoc(strHtml);
        mapsFrame.setWidthFull();
        mapsFrame.setHeight("400px");
        mapsFrame.getStyle().setBorder("0px");
        mapsFrame.getStyle().setBorderRadius("8px");

        layout.add(mapsFrame);

        return layout;
    }

    private VerticalLayout getLocationSubFestival(Record record) {

        String strName = record.getColumnData("nameShort");
        Div divTitle = new Div(strName);
        divTitle.addClassNames(
                TextColor.SECONDARY
        );

        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strDateFrom = record.getColumnData("formatedDateFrom");
        String strDateTo = record.getColumnData("formatedDateTo");
        String strEdition_description = record.getColumnData("edition_description");

        HorizontalLayout layoutPlannedTitle = new HorizontalLayout();
        if (isMobile) {
            layoutPlannedTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.XSMALL
            );
        } else {
            layoutPlannedTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.XSMALL
            );
        }

        Div h5Title = new Div(strTitle);
        h5Title.addClassNames(//FontWeight.SEMIBOLD,
                TextColor.SECONDARY
        );

        Div h6DateFrom = new Div(strDateFrom);
        h6DateFrom.addClassNames(
                TextColor.SECONDARY
        );
        Div h6DateTo = new Div(strDateTo);
        h6DateTo.addClassNames(
                TextColor.SECONDARY
        );

        String strTakesPlace = "takes place between";

        Div divTakesPlace = new Div(strTakesPlace);
        divTakesPlace.addClassNames(
                TextColor.TERTIARY
        );

        Div divAnd = new Div("and");
        divAnd.addClassNames(
                TextColor.TERTIARY
        );

        layoutPlannedTitle.add(h5Title, divTakesPlace, h6DateFrom, divAnd, h6DateTo);

        Div divSubTitle = new Div(strSubTitle);
        divSubTitle.setWidthFull();
        divSubTitle.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
        divSubTitle.getStyle().setAlignItems(Style.AlignItems.CENTER);

        Paragraph parEdDescription = new Paragraph(strEdition_description);
        parEdDescription.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
        parEdDescription.getStyle().setAlignItems(Style.AlignItems.CENTER);
        parEdDescription.setWidthFull();

        VerticalLayout layoutPlanned = new VerticalLayout();
        if (isMobile) {
            layoutPlanned.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.XSMALL
            );
        } else {
            layoutPlanned.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL
            );
        }

        if (!strName.trim().isEmpty() && !strName.trim().equalsIgnoreCase("null")) {
            layoutPlanned.add(divTitle);
        }

        if (!strDateFrom.trim().isEmpty() && !strDateFrom.trim().equalsIgnoreCase("null")) {
            layoutPlanned.add(layoutPlannedTitle);//, divSubTitle, parEdDescription);
        } else {
//            Div divNoEvents = new Div("Currently we have no info on future events. If you have any specific indication inform us here.");
//            divNoEvents.addClassNames(TextColor.TERTIARY);
//            layoutPlanned.add(divNoEvents);
        }

        return layoutPlanned;
    }

    private VerticalLayout getLocationSubTabs(String strContentType, String strContentTitle) {

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
        lstLocationTabs.add("Spots");
        lstLocationTabs.add("Reviews");
        lstLocationTabs.add("Additional Info");


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
                TextAlignment.CENTER
        );

        divTabContent.setHeight("50px");

        btnGroup.addValueChangeListener(event -> {
            if (event.getValue().toString().equalsIgnoreCase("Spots")) {
                divTabContent.removeAll();
                divTabContent.add(getLocationSubSpots(strContentTitle));
                divTabContent.setHeight("120px");
            } else if (event.getValue().toString().equalsIgnoreCase("Additional Info")) {
                divTabContent.setText(" info ... of " + strContentTitle + " in " + strContentType);
            } else if (event.getValue().toString().equalsIgnoreCase("Reviews")) {
                divTabContent.setText(strUsername + " users review 1 ...");
            } else {
                divTabContent.setText(strContentTitle + " ....... in " + strContentType);
            }
        });

        layoutTabsInfo.add(btnGroup, divTabContent);


        return layoutTabsInfo;
    }

    private VerticalLayout getLocationSubSpots(String strLocation) {//,String sqlRead, String[] arrColumnsLearning){

        String[] arrColumnNames = {"city_name", "perfecture", "country", "name", "entity_type", "photo_festival_ed_Id"};

        String readSqlDestinationSpots = "SELECT d.city_name, d.perfecture, d.country, ds.name, ds.entity_type, ds.photo_festival_ed_Id FROM destination d LEFT JOIN destination_spots ds ON d.id = ds.id_destination " +
                " WHERE d.city_name LIKE '" + strLocation + "' " +
                " ORDER BY d.city_name, ds.entity_type, ds.name";

        VerticalLayout layoutSpotsInfo = new VerticalLayout();
        if (isMobile) {
            layoutSpotsInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM
            );
        } else {
            layoutSpotsInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM
            );
        }

        List<Record> lstRecords = getRecordsFromDb(readSqlDestinationSpots, arrColumnNames);
        if (lstRecords.size() > 0) {
            for (int r = 0; r < lstRecords.size(); r++) {

                Record rec = lstRecords.get(r);
                layoutSpotsInfo.add(getSpotLayout(rec));
            }
        }


        return layoutSpotsInfo;
    }

    private HorizontalLayout getSpotLayout(Record record) {

        HorizontalLayout layoutSpot = new HorizontalLayout();
        layoutSpot.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);
        // layoutSpot.setClassName("lazy-card-overview-gradient");
//        layoutSpot.addClassName("lazy-card-overview-align-left");
//        layoutSpot.addClassName("lazy-card-overview-border-solid");

        String city = record.getColumnData("city_name");
        String country = record.getColumnData("country");
        String entity_type = record.getColumnData("entity_type");
        String name = record.getColumnData("name");

        Div divSpot = new Div(name); // +" ("+entity_type+")");
        divSpot.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);
        divSpot.getStyle().setColor("#8e7138");
        divSpot.addClassName("lazy-card-overview-font-important");


        //divSpot.addComponentAtIndex(0, LineAwesomeIcon.MAP_PIN_SOLID.create());

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
        layoutActions.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.MEDIUM,
                Padding.NONE,
                Gap.LARGE,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.LARGE);
        layoutActions.addClassNames("actions");
//        layoutSpot.add(LineAwesomeIcon.MAP_PIN_SOLID.create(),divSpot);//,btnLike,btnShare);
        layoutSpot.add(divSpot);//,btnLike,btnShare);

        return layoutSpot;

    }

    private HorizontalLayout getLocationActions() {


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
        layoutActions.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.MEDIUM,
                Padding.NONE,
                Gap.LARGE,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.LARGE);
        layoutActions.addClassNames("actions");

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


    public VerticalLayout getWebsiteItem(Record record) {

        // String[] arrColumnsLearning = {"org_name","org_type","city", "country"};

        String strName = record.getColumnData("org_name");
        String strType = record.getColumnData("org_type");

        String strTypeParent = record.getColumnData("org_type_parent");
        String strCanBeUsedFor = record.getColumnData("used_for");


        String strCountry = record.getColumnData("country");
        String strCity = record.getColumnData("city");

        String strUrl = record.getColumnData("url");

        String strImage = record.getColumnData("picture");

        if (!strImage.equalsIgnoreCase("null") && !strImage.equalsIgnoreCase("")) {
            strImage = strPath + "/" + strImage;
        } else {
            strImage = "";
        }


        HorizontalLayout layoutSection = new HorizontalLayout();
        layoutSection.addClassName("category"); //addClassNames(AlignItems.CENTER, JustifyContent.CENTER);


        Div divImage = new Div();
//        divImage.getStyle().setColor(strColorOfIcons);
        Div linkCategoryRelated = new Div("Website");//,"",);
//        linkCategoryRelated.getStyle().setColor(strColorOfIcons);
        divImage.add(LineAwesomeIcon.LINK_SOLID.create());
        layoutSection.add(divImage, linkCategoryRelated);


//        RouteParam routeSection = new RouteParam("section", section);
//        RouteParam routeItem = new RouteParam("subsection", strSubject);
//
//        RouterLink linkPhotoSubSection = new RouterLink(strSubject, PhotoView.class,new RouteParameters(routeSection,routeItem));
//        linkPhotoSubSection.setClassName("lazy-result-line-button");


//        String strDate = "";
//        String dt = record.getColumnData("dateInsert");
//        SimpleDateFormat toui = new SimpleDateFormat("dd/MM/yyyy");
//        SimpleDateFormat fromdb = new SimpleDateFormat("yyyy-MM-dd");
//
//        try {
//
//            strDate = toui.format(fromdb.parse(dt));
//        } catch (ParseException e) {
//            logger.error(e.getMessage());
//        }

        H5 titleName = new H5(strName);
        titleName.addClassName(TextColor.SECONDARY);
        titleName.addClassName("lazy-result-line-title");

//        Div divLocation = new Div(strCity+" / "+strCountry);
//        divLocation.addClassNames(TextColor.SECONDARY);
//        H6 dayUpdated = new H6("updated: "+strDate);
//        dayUpdated.getStyle().setColor("#8b94a0");

        HorizontalLayout layoutPostTitle = new HorizontalLayout();
        if (isMobile) {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.XSMALL,
                    Padding.XSMALL,
                    Gap.XSMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_10,
                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
        } else {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.SMALL,
                    Padding.SMALL,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_10,
                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
        }

//        layoutPostTitle.setWidthFull();
//        layoutPostTitle.setClassName("lazy-result-line-title-align");
//        //layoutPostTitle.getStyle().setAlignItems(Style.AlignItems.CENTER);
//        //layoutPostTitle.getStyle().setJustifyContent(Style.JustifyContent.SPACE_BETWEEN);
//        layoutPostTitle.setPadding(true);
//        layoutPostTitle.setSpacing(true);
//        layoutPostTitle.setMargin(true);
        //layoutPostTitle.addClassName("lazy-result-line-title-align");
        layoutPostTitle.add(layoutSection, titleName);

        VerticalLayout layoutMainInfo = new VerticalLayout();
        layoutMainInfo.addClassName("bottom-radius-shadow");

        if (isMobile) {
            layoutMainInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_5, BorderRadius.NONE);
        } else {
            layoutMainInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_5, BorderRadius.LARGE);
        }

        HorizontalLayout layoutImage = new HorizontalLayout();
        layoutImage.addClassNames(Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE);

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

            Image img = new Image(imageResource, "image");
            img.setMaxHeight("240px");
            img.getStyle().set("border-radius", "9px");

            layoutImage.add(img);
        }

//        Anchor linkTutor = new Anchor();
//        linkTutor.add(FontAwesome.Solid.LINK.create());
//        linkTutor.setClassName("lazy-result-line-button");
//
//        String strUrlTutorExt = record.getColumnData("website");
//        if(!strUrlTutorExt.equalsIgnoreCase("null") && !strUrlTutorExt.equalsIgnoreCase("")) {
//
//            // linkTutor.setText("Website");
//            linkTutor.setHref(strUrlTutorExt);
//            linkTutor.setTarget("_blank");
//        }

//        Anchor linkTutorYt = new Anchor();
//        linkTutorYt.add(FontAwesome.Brands.YOUTUBE.create());
//        linkTutorYt.setClassName("lazy-result-line-button");
//        linkTutorYt.setVisible(false);
//        String strUrlTutorYt = record.getColumnData("url_yt");
//        if(!strUrlTutorYt.equalsIgnoreCase("null") && !strUrlTutorYt.equalsIgnoreCase("")) {
//
//            //linkTutorYt.setText("YouTube");
//            strUrlTutorYt = "https://www.youtube.com/"+strUrlTutorYt;
//            linkTutorYt.setHref(strUrlTutorYt);
//            linkTutorYt.setTarget("_blank");
//            linkTutorYt.setVisible(true);
//        }
//
//        Anchor linkTutorWikipedia = new Anchor();
//        linkTutorWikipedia.add(FontAwesome.Brands.WIKIPEDIA_W.create());
//        linkTutorWikipedia.setClassName("lazy-result-line-button");
//        linkTutorWikipedia.setVisible(false);
//        String strUrlTutorWikipedia = record.getColumnData("url_wikipedia");
//        if(!strUrlTutorWikipedia.equalsIgnoreCase("null") && !strUrlTutorWikipedia.equalsIgnoreCase("")) {
//
//            //linkTutorYt.setText("YouTube");
//            //strUrlTutorWikipedia = "https://www.youtube.com/"+strUrlTutorYt;
//            linkTutorWikipedia.setHref(strUrlTutorWikipedia);
//            linkTutorWikipedia.setTarget("_blank");
//            linkTutorWikipedia.setVisible(true);
//        }
//
//        Anchor linkTutorInsta = new Anchor();
//        linkTutorInsta.setClassName("lazy-result-line-button");
//        linkTutorInsta.add(FontAwesome.Brands.INSTAGRAM.create());
//        linkTutorInsta.setVisible(false);
//        String strUrlTutorInsta = record.getColumnData("url_insta");
//        if(!strUrlTutorInsta.equalsIgnoreCase("null") && !strUrlTutorInsta.equalsIgnoreCase("")) {
//
//            // linkTutorInsta.setText("Instagram");
////            strUrlTutorInsta = "https://www.instagram.com/"+ strUrlTutorInsta;
//            linkTutorInsta.setHref(strUrlTutorInsta);
//            linkTutorInsta.setTarget("_blank");
//            linkTutorInsta.setVisible(true);
//        }

        Div divUsedFor = new Div(strCanBeUsedFor);
        divUsedFor.addClassName(TextColor.TERTIARY);


        String strDescription = record.getColumnData("description");

        Paragraph parDescription = new Paragraph(strDescription);
        if (!strDescription.equalsIgnoreCase("null") && !strDescription.equalsIgnoreCase("")) {
            parDescription.setVisible(true);
        } else {
            parDescription.setVisible(false);
        }

//        Anchor link1InNewTab = new Anchor();


//
//        VerticalLayout layoutSourceCard = new VerticalLayout();
//        layoutSourceCard.addClassNames(
//                Overflow.HIDDEN,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Margin.SMALL,
//                Padding.SMALL,
//                Gap.MEDIUM,
//                TextColor.SECONDARY,
//                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                Background.CONTRAST_5,
//                Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE
//        );
//
//        layoutSourceCard.setMaxWidth("250px");
//
//        Div divTutorInfo = new Div();
//        divTutorInfo.addClassName(TextColor.SECONDARY);
//        divTutorInfo.setVisible(false);
//        if(!strTutor.equalsIgnoreCase("null") && !strTutor.isEmpty())
//        {
//            divTutorInfo.setText(strTutor);
//            divTutorInfo.setVisible(true);
//
//        }
//
//        StreamResource iconInfo = new StreamResource("info-circle-line-icon.svg",
//                () -> getClass().getResourceAsStream("/icons/info-circle-line-icon.svg"));
//        SvgIcon svgInfo = new SvgIcon(iconInfo);
//
//        StreamResource iconTutor = new StreamResource("man-user-circle-black-icon.svg",
//                () -> getClass().getResourceAsStream("/icons/man-user-circle-black-icon.svg"));
//        SvgIcon svgTutor = new SvgIcon(iconTutor);
//
//        Div imgInfo = new Div(svgInfo);
//        Div imgPerson = new Div(svgTutor);
//
//        RouteParam routeSectionTutor = new RouteParam("section", section);
//        RouteParam routeItemTutor = new RouteParam("subsection", strTutor);

//        RouterLink linkPhotoSubSectionTutor = new RouterLink(strTutor, PhotoView.class,new RouteParameters(routeSectionTutor,routeItemTutor));
//        linkPhotoSubSectionTutor.setClassName("lazy-result-line-button");

//        VerticalLayout layoutExtLinks = new VerticalLayout();
//        layoutExtLinks.setWidthFull();
        HorizontalLayout layoutExtLinks = new HorizontalLayout();
        layoutExtLinks.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                Margin.SMALL,
                Padding.NONE,
                Gap.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER

        );

        Anchor urlLink = new Anchor();
        urlLink.getStyle().setColor("#8b94a0");
        urlLink.setClassName("lazy-api-link");
        urlLink.setHref(strUrl);
        urlLink.setTarget("_blank");
        urlLink.setText(strUrl.toLowerCase().replace("https://", "").replace("http://", ""));

//        layoutExtLinks.add(linkTutor,linkTutorWikipedia,linkTutorInsta,linkTutorYt);
//        layoutSourceCard.add(imgInfo, divLearningCat, imgPerson, divTutorInfo,divTutorTeam,layoutExtLinks);

//        if(!strUrl.equalsIgnoreCase("null") && !strUrl.equalsIgnoreCase(""))
//        {
//            if(strFormat.equalsIgnoreCase("YouTube"))
//            {
//                link1InNewTab.setVisible(false);
//                divVideo.setVisible(true);
//                layoutImage.setVisible(false);
//            }
//            else {
//                link1InNewTab.setText(strUrl);
//                //link1InNewTab.setTarget(festUrl);
//                link1InNewTab.setHref(strUrl);
//                link1InNewTab.setTarget("_blank");
//                //link1InNewTab.getElement().setAttribute("target", "_blank");
//                link1InNewTab.setVisible(true);
//                divVideo.setVisible(false);
//                layoutImage.setVisible(true);
//            }
//        }
//        else{
//            link1InNewTab.setVisible(false);
//            divVideo.setVisible(false);
//            layoutImage.setVisible(true);
//        }
//


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
        layoutActions.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.MEDIUM,
                Padding.NONE,
                Gap.MEDIUM,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.LARGE);
        layoutActions.addClassNames("actions");

        layoutActions.add(btnLike, btnMoreAction, btnComment, btnMoreInfo, btnShare);


        HorizontalLayout layoutPostRelated = new HorizontalLayout();
        layoutPostRelated.setWidthFull();
        layoutPostRelated.setPadding(false);
        layoutPostRelated.setSpacing(false);

        layoutMainInfo.add(layoutPostTitle, divUsedFor, parDescription, urlLink, getSubTabs("websites", strName, record), layoutActions);

        return layoutMainInfo;
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

            String thumbUrl = photo.getThumbnailUrl();//.getSmallUrl(); //.getThumbnailUrl();
            String title = photo.getTitle();

            User user = photo.getOwner();
            user.getId();
            user.getRealName();
            user.getProfileurl();
            user.getPhotosCount();
            user.getPhotosurl();

            Image image = new Image(thumbUrl, destination);
            image.setHeight("88px");
            image.setWidth("auto");

            VerticalLayout photoLayout = new VerticalLayout();
            photoLayout.setPadding(true);
            photoLayout.setMargin(true);
            photoLayout.setSpacing(true);

            HorizontalLayout layoutUser = new HorizontalLayout();
            layoutUser.setSpacing(false);
            layoutUser.setMargin(false);
            layoutUser.setPadding(false);
            layoutUser.setAlignItems(FlexComponent.Alignment.CENTER);
            layoutUser.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

            String userId = user.getId();
            String userName = user.getRealName(); //photoFlickr.getUserName(userId); //user.getUsername();

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

            layoutUser.add(linkUserInNewTab);
            photoLayout.add(layoutUser, image);
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

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {

        logger.info(" photo  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames, Object[] sqlParValue, String[] sqlParType) {
        logger.info(" photo  getRecordsFromDb with params:   " + sql);
        return recordService.findAll(sql, arrColumnNames, sqlParValue, sqlParType);
    }

    private void logVisitorToDb() {

//        section = section.replaceAll("'", " ");
//        section = section.replaceAll("\"", " ");

        //search = search.replaceAll("'"," ");
        //search = search.replaceAll("\""," ");

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


        logger.info("photo visitor:" + publicIp + " . " + hostname + " . " + hostAddress + " . " + canonicalHostname + " .  " + browser + " " + sessionid);

        String insertSQL = "INSERT INTO dbvisitor_log SET visitorlogId = 0,  timeOfVisit = now(), ipAddress = '" + publicIp + "', browserName = '" + browser + "', "
                + " browserVersionMajor = '" + versionOfBrowserMajor + "', browserVersionMinor = '" + versionOfBrowserMinor + "', urlParameter = NULL , timeZoneId = '" + timeZoneId + "', "
                + "appVersion = '" + APP_NAME + "-" + APP_VERSION + "', sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
                + "hostAddress = '" + hostAddress + "', os = '" + strOS + "', section = '" + section + "'," +
                " item = '" + strPath + "'," +
                " locale = '" + locale + "', localeName ='" + localeName + "' ";

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


    private String getClientPublicIp() {
        String urlString = "http://checkip.amazonaws.com/";
        String publicIp = "";
        try {
            URL url = new URL(urlString);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            publicIp = br.readLine();
        } catch (IOException MalformedURLException) {
            logger.error("error getClientPublicIp from " + urlString);
        }
        return publicIp;
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
