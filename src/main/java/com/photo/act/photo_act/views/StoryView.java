package com.photo.act.photo_act.views;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.HeaderFilterTabs;
import com.photo.act.photo_act.views.components.StoryItemViewCard;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabVariant;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static com.photo.act.photo_act.views.LearningsView.STR_ALL_TITLES;
import static com.photo.act.photo_act.views.MainLayout.*;

@PermitAll

//@RouteAlias("") // empty on homepage
@Route(value = "story") //":category?")
@RouteAlias(value = "story/member/:member?/title/:title?", layout = MainLayout.class)


//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class StoryView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

    private static final Logger logger = LoggerFactory.getLogger(StoryView.class);
    public static String subPathThumbs = "photo-thumbs";
    public static String subPathMedium = "photo-medium";
    public static String subPathUpload = "photo-upload";
    public static String subPathShow = "photo-show";
    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";
    private VerticalLayout verticalLayout;
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String section = SECTION_GALLERY;
    private String strMember;
    private String strTitle;
    private RecordService recordService;
    private String strHeader;
    private String strUrlRequestToBeLogged;
    private String dirChar = FileSystems.getDefault().getSeparator();
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


    private String[] arrDestinationNames = {"id", "city_name", "prefecture", "country"};
    private String sqlReadDestination = "SELECT distinct city_name, prefecture, country " +
            " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id " +
            " ORDER BY city_name ASC ";

    private String[] arrColumnNamesGallery = {"name_new", "title", "subtitle", "photo_type", "uploader", "creator", "visible_to", "city_name", "meta_date", "photo_date", "photo_time_shot"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso", "meta_aperture", "meta_shutter_speed"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon"
            , "spot_name", "spot_type"
            , "date_inserted"
            , "username", "username", "resident", "date_joined", "avatar_path"
    };

    private String sqlReadGallery = "SELECT pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.creator, pm.visible_to, d.city_name, d.country, " +
            " DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, DATE_FORMAT(pm.meta_date, '%M %Y') AS photo_date, DATE_FORMAT(pm.meta_date, '%d/%m/%Y - %H:%i:%S') AS photo_time_shot, " +
            " pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model, " +
            " pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed " +
            " , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
            " , usr.username, usr.username, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
//            "  ds.spot_name, ds.spot_type " +
            //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
            " FROM dbuser usr, photo_meta pm LEFT JOIN destination d ON pm.destination_id = d.id " +
            " WHERE pm.uploaderId = usr.userId ";


    private UtilsDate utilsDate;
    private String sessionDateTime;
    private GenericView genericView;
    private String strOS;
    private String strBrowser;


    public StoryView(RecordService recordService) {
        this.recordService = recordService;
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
        strMember = event.getRouteParameters().get("member").orElse(STR_ALL_MEMBERS);
        strTitle = event.getRouteParameters().get("title").orElse(STR_ALL_TITLES);

        getUserClientInfo();

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously
            strUrlRequestToBeLogged = currentUrl.toExternalForm();

        });

        NetUtils netUtils = new NetUtils();
        publicIp = netUtils.getClientPublicIp(hostname);

        if (strMember.equalsIgnoreCase("visitor-user")) {
            userId = 1;
            strUsername = "visitor-user";
        }

        verticalLayout.removeAll();

        if (strTitle.isEmpty()) {
            logger.error(" empty strTitle: " + strTitle);
        }

        if (strMember.equalsIgnoreCase(STR_ALL_MEMBERS) && (strTitle.equalsIgnoreCase(STR_ALL_TITLES))) {
            verticalLayout.add(loadHeader("Story", "", ""));

            String sqlGalleryAll = sqlReadGallery + " AND pm.visible_to = 'ALL' ";
            sqlGalleryAll = sqlGalleryAll + " ORDER BY pm.date_inserted DESC, pm.title ASC, meta_date DESC ";

//            verticalLayout.add(loadCarouselWithThumbnails(sqlGalleryAll, arrColumnNamesGallery));

            verticalLayout.add(loadStoryItemsFromDb(sqlGalleryAll, arrColumnNamesGallery, false));
        } else if (strMember.equalsIgnoreCase(STR_ALL_MEMBERS) && !strTitle.equalsIgnoreCase(STR_ALL_TITLES)) {
            verticalLayout.add(loadHeader("Story", "", strTitle));
            verticalLayout.add(loadWeather(strTitle, ""));


            String sqlGalleryAll = sqlReadGallery + " AND pm.visible_to = 'ALL' ";

            sqlGalleryAll = sqlGalleryAll + " AND d.city_name LIKE '" + strTitle + "' ";

            sqlGalleryAll = sqlGalleryAll + " ORDER BY pm.date_inserted DESC, pm.title ASC, meta_date DESC ";

//            verticalLayout.add(loadCarouselWithThumbnails(sqlGalleryAll, arrColumnNamesGallery));

            verticalLayout.add(loadStoryItemsFromDb(sqlGalleryAll, arrColumnNamesGallery, false));
        } else if (!strMember.equalsIgnoreCase(STR_ALL_MEMBERS)) {
            verticalLayout.add(loadHeader("My Story", "and how to manage them.", ""));
            verticalLayout.add(loadWeather(strTitle, ""));
            String sqlGalleryUser = sqlReadGallery +
                    " AND pm.visible_to = 'ALL' AND pm.uploader LIKE '" + strMember + "' " +
                    " ORDER BY pm.date_inserted DESC, meta_date DESC";

//            verticalLayout.add(loadCarouselWithThumbnails(sqlGalleryUser, arrColumnNamesGallery));
            verticalLayout.add(loadStoryItemsFromDb(sqlGalleryUser, arrColumnNamesGallery, true));
        }

        this.removeAll();
        this.add(verticalLayout);
        this.add(genericView.loadFooter(isMobile));

        logVisitorToDb();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String o) {
//        strMember = o;//beforeEvent.getRouteParameters().get("member").orElse("pictures");
    }

    private void constructUI() {

        addClassNames(Overflow.HIDDEN, Width.FULL,
                // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.MEDIUM
        );
        //       addClassName("image-gallery-view");

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
        if (isMobile) {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,// not full width
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Padding.Top.XSMALL,
                    Gap.MEDIUM,
                    //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                    //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        } else {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,// not full width
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.XLARGE,
                    Padding.Top.XSMALL,
                    Gap.LARGE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
//
//            Html htmlTitle = new Html("<title>'photoact.net Network and Act around Photography'</title>");
//            Html htmlMeta = new Html("<meta name='description' content='Get the latest uploaded photos from our community of photographers.'>");
//            verticalLayout.add(htmlTitle, htmlMeta);
        }
    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSection) {

        this.strHeader = strHeader;

        HorizontalLayout headerContainerMaster = new HorizontalLayout();
        if (isMobile) {
            headerContainerMaster.addClassNames(
                    AlignItems.CENTER, JustifyContent.BETWEEN,
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
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.XLARGE,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }

        VerticalLayout headerTextContainer = new VerticalLayout();
        headerTextContainer.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.NONE, Padding.NONE,
                Gap.XSMALL);

        H2 header = new H2(strHeader);
        header.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.LARGE, FontWeight.BOLD, TextColor.SECONDARY);
//        header.getStyle().set("font-family", "Times-New-Roman, serif");

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.SMALL, TextColor.SECONDARY);


        headerTextContainer.add(header, subheader);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Most Viewed", "Least Viewed", "Most Favourite", "Least Favourite", "Newest First", "Oldest First", "Most Liked", "Least Liked");
        sortBy.setValue("Most Viewed");

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

        VerticalLayout layoutFiltersAll = new VerticalLayout();
        layoutFiltersAll.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.NONE, Padding.SMALL,
                FontSize.SMALL, TextColor.SECONDARY,
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


        Div divFiltersTitle = new Div("Filter by Location");
        layoutFiltersAll.add(divFiltersTitle, layoutFilters);

        List<Record> lstDestinationRecs = getRecordsFromDb(sqlReadDestination, arrDestinationNames);

        ArrayList<String> lstDestinations = new ArrayList<>();
        for (int r = 0; r < lstDestinationRecs.size(); r++) {
            String strDestination = lstDestinationRecs.get(r).getColumnData("city_name");
            if (strDestination == null || strDestination.trim().isEmpty() || strDestination.trim().equalsIgnoreCase("null")) {
            } else {
                lstDestinations.add(strDestination);
            }

        }

        RouteParam routeMember = new RouteParam("member", strMember);

        RouteParam routeDestinationAll = new RouteParam("destination", STR_ALL_DESTINATIONS);
//        RouteParameters routeParamsAll = new RouteParameters(routeDestinationAll, routeMember);
//        RouterLink linkPhotoDestinationAll = new RouterLink("All Locations", GalleryView.class, routeParamsAll);
//        layoutFilters.add(linkPhotoDestinationAll);

        for (int c = 0; c < lstDestinations.size(); c++) {
            String captionDestination = lstDestinations.get(c);
            RouteParam routeParamDestination = new RouteParam("destination", captionDestination);

            RouterLink linkPhotoCategory = new RouterLink(captionDestination, StoryView.class, new RouteParameters(routeParamDestination, routeMember));
            layoutFilters.add(linkPhotoCategory);
        }

        CheckboxGroup<String> checkboxGroupSubject = new CheckboxGroup<>();
        checkboxGroupSubject.setTooltipText("Subject");
//        checkboxGroupSubject.setLabel("Subject");
        checkboxGroupSubject.setItems("Photography", "Street Photography", "Landscape", "Cityscape");
        //   "Friday", "Saturday", "Sunday");
        // checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
//        Div lblFilterSubject = new Div("Subject");

//        layoutFilters.add(checkboxGroupSubject);

//        CheckboxGroup<String> checkboxGroupFormat = new CheckboxGroup<>();
//        checkboxGroupFormat.setTooltipText("Format");
////        checkboxGroupFormat.setLabel("Format");
//        checkboxGroupFormat.setItems("Book", "Youtube");
////        Div lblFilterFormat = new Div("Format");
//        layoutFilters.add(checkboxGroupFormat);

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

//        Select<String> cmbView = new Select<>();
//        cmbView.setLabel("View");
//
//        cmbView.setItems("Micro View", "Ordinary - No MetaData", "Ordinary - MetaData Bottom", "Ordinary - MetaData Right",
//                "Wide - No MetaData", "Wide - MetaData Bottom","Wide - MetaData Right");
//        cmbView.setValue("Ordinary - No MetaData");

        Tab tabFilterLocation = new Tab(VaadinIcon.LOCATION_ARROW_CIRCLE_O.create(), new Span("Location"));
        Tab tabFilterKeyword = new Tab(VaadinIcon.KEYBOARD_O.create(), new Span("Keyword"));
        Tab tabFilterUser = new Tab(VaadinIcon.USER.create(), new Span("User"));

        // Set the icon on top
        for (Tab tab : new Tab[]{tabFilterLocation, tabFilterKeyword, tabFilterUser}) {
            tab.addThemeVariants(TabVariant.LUMO_ICON_ON_TOP);
//            tab.addClassNames(
////                    Width.FULL,
//                    AlignItems.CENTER, JustifyContent.END,
//                    IconSize.LARGE, //FontSize.MEDIUM,
//                    TextColor.SECONDARY,
////                    BorderColor.CONTRAST_20,
//                    Padding.MEDIUM, Margin.NONE,
//                    Gap.MEDIUM
//            );
//            FontSize.MEDIUM, TextColor.SECONDARY, IconSize.SMALL, //BorderRadius.LARGE,
//                    Width.FULL, Padding.XSMALL, Margin.NONE,
//                    BorderColor.CONTRAST_20, Border.ALL);
        }

        Tabs tabsFilterBased = new Tabs(tabFilterLocation, tabFilterKeyword, tabFilterUser);
//        tabsViewInfo.addThemeVariants(  TabsVariant.LUMO_SMALL,
//                TabsVariant.LUMO_EQUAL_WIDTH_TABS);
        tabsFilterBased.addClassNames(
                Width.FULL,
                AlignItems.CENTER, JustifyContent.END,
                Padding.LARGE, Margin.NONE,
//                BorderRadius.LARGE,
                Border.ALL,
                BorderColor.CONTRAST_5
//                Gap.XSMALL
        );
        tabsFilterBased.addClassName("header-view-type");

        Tabs tabsViewInfo = new Tabs(tabFilterLocation, tabFilterKeyword, tabFilterUser);
//        tabsViewInfo.addThemeVariants(  TabsVariant.LUMO_SMALL,
//                TabsVariant.LUMO_EQUAL_WIDTH_TABS);
        tabsViewInfo.addClassNames(
                Width.FULL,
                AlignItems.CENTER, JustifyContent.END,
                Padding.LARGE, Margin.NONE,
//                BorderRadius.LARGE,
                Border.ALL,
                BorderColor.CONTRAST_5
//                Gap.XSMALL
        );
        tabsViewInfo.addClassName("header-view-type");

        headerContainerMaster.add(headerTextContainer); //,tabsViewInfo);
//        layoutHeaderParameters.add(headerContainerMaster);

        H3 divSection = new H3(strSection);
        divSection.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Margin.Top.MEDIUM);


//        headerContainerMaster.add(headerTextContainer);
        headerContainer.add(layoutFiltersAll);
//        layoutHeaderParameters.add( headerContainerSecondary, divSection);

        HeaderFilterTabs headerFilterTabs = new HeaderFilterTabs(recordService, isMobile);
        VerticalLayout layoutHeaderParameters = headerFilterTabs.getHeader(strHeader, strSubHeader, strSection);

//        headerContainerMaster.add(headerTextContainer, cmbView);
//        headerContainerSecondary.add(layoutFilters, sortBy);
//        layoutHeaderParameters.add(headerContainerMaster,headerContainerSecondary);

        return layoutHeaderParameters;
    }

    private Div loadStoryItemsFromDb(String sqlRead, String[] arrColumnNames, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar;

        Div divGallery = new Div();
        divGallery.addClassName("gallery");

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            Record rec = lstRecords.get(r);
            divGallery.add(getStoryItemFromDb(rec, isEditable));
        }
        return divGallery;
    }

    private StoryItemViewCard getStoryItemFromDb(Record record, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");

        String strCityName = record.getColumnData("city_name");
        String strUploader = record.getColumnData("uploader");
//
//        RouteParam routeUploaderAll = new RouteParam("member", STR_ALL_MEMBERS);
//
//        RouteParam routeUploader = new RouteParam("member", strUploader);
//        RouteParam routeDestination = new RouteParam("destination", strCityName);
//
//        RouterLink linkDestination = new RouterLink(strCityName, GalleryView.class, new RouteParameters(routeDestination, routeUploader));
//        RouterLink linkUploader = new RouterLink(strUploader, GalleryView.class, new RouteParameters(routeDestination, routeUploader));
//
//        RouterLink linkUploaderAll = new RouterLink(STR_ALL_MEMBERS, GalleryView.class, new RouteParameters(routeDestination, routeUploaderAll));
//        ArrayList<RouterLink> lstRouterLinks =new ArrayList<>();
//        lstRouterLinks.add(linkDestination);

        String strImagePath = strPath + dirChar + strFileName;
        logger.info(" strImagePath " + strImagePath);

        StoryItemViewCard storyItemViewCard = new StoryItemViewCard(record, strImagePath, isMobile, userId, strUsername, sessionCreation, hostname, publicIp, isEditable,
                recordService); //, sqlReadGallery, arrColumnNamesGallery);
        return storyItemViewCard;
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
                + " appVersion = '" + APP_NAME + "-" + APP_VERSION + "',  parentSection = 'photo',  sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
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
