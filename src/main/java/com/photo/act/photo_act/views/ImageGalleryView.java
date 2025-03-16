package com.photo.act.photo_act.views;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.ImageGalleryViewCard;
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
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.MainLayout.*;

//@PageTitle("Image Gallery")
//@RouteAlias("") // empty on homepage
@Route(value = "gallery") //":category?")
@RouteAlias(value = "gallery/location/:destination?", layout = MainLayout.class)
@RouteAlias(value = "gallery/member/:member?", layout = MainLayout.class)
@RouteAlias(value = "gallery/location/:destination?/member/:member?", layout = MainLayout.class)



//@RouteAlias(value = "gallery/location/:destination?", layout = MainLayout.class)

//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class ImageGalleryView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents,HasDynamicTitle, HasStyle {

    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";

    private static final Logger logger = LoggerFactory.getLogger(ImageGalleryView.class);

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
    private String strDestination;
    private RecordService recordService;
    private String strHeader;



    private String strUrlRequestToBeLogged ;

    private String dirChar = FileSystems.getDefault().getSeparator();
    public static String subPathThumbs = "photo-thumbs";
    public static String subPathMedium = "photo-medium";
    public static String subPathUpload = "photo-upload";
    public static String subPathShow = "photo-show";

    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";


    private String publicIp;
    private String strPath;
    private String hostname;
    private String hostAddress ;
    private String canonicalHostname ;

    private int userId;
    private String strUsername;

    private String strColorExternalweb = "#9fafd5";

    private String[] arrClubsColumnNames = {"org_name","org_type","org_type_parent","city", "used_for", "country","url", "url_local_events", "url_fb", "url_yt", "url_insta",
            "url_flickr", "url_wikipedia" };
    private String sqlShowClubsSelect = "SELECT id, org_name, org_type, org_type_parent , city , used_for , country , url , city, address, pc, country, map_x, map_y, url, " +
            " url_local_events, url_fb, url_yt, url_insta, url_flickr, url_wikipedia, " +
            " date_inserted, dateUpdated " +
            " FROM organizations o " ;
    private String sqlShowClubsWhere = " WHERE o.org_type LIKE 'Club' " ;
    private String sqlShowClubsOrder = " ORDER BY o.city ASC, o.org_name ASC";


    private String[] arrColumnNamesGallery = {"name_new", "title" , "subtitle" , "photo_type" , "uploader", "creator", "visible_to", "city_name", "meta_date"
            ,"space_size","space_size_medium", "space_size_thumb","meta_camera_make", "meta_camera_model","meta_lens_make","meta_lens_model"
            ,"meta_focal_length", "meta_focal_length_ff", "meta_iso"
            ,"location_by_user","location_area","location_country_code","location_lat","location_lon"
            ,"date_inserted"};

    private String sqlReadGallery = "SELECT pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.creator, pm.visible_to, d.city_name, d.country, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, " +
            " pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model, "+
            " pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, "+
            "  pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon "+
            //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
            " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id ";





    private UtilsDate utilsDate;
    private String sessionDateTime;
    private GenericView genericView;


    public ImageGalleryView(RecordService recordService) {
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
        strMember = event.getRouteParameters().get("member").orElse(STR_ALL_MEMBERS);
        strDestination = event.getRouteParameters().get("destination").orElse(STR_ALL_DESTINATIONS);



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

        final String[] urlHost = {"", "", "", "", "", "", "", ""};

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously
            urlHost[0] = currentUrl.getHost();
            urlHost[1] = currentUrl.getProtocol();
            urlHost[2] = currentUrl.getRef();
            urlHost[3] = currentUrl.getUserInfo();
            urlHost[4] = currentUrl.toExternalForm();
            urlHost[5] = currentUrl.getPort()+"";
            urlHost[6] = currentUrl.getAuthority();
            urlHost[7] = currentUrl.getQuery();

            logger.info("  url:" + urlHost[0] + "  url:" + urlHost[1] + "  url:" + urlHost[2] + "  url:" + urlHost[3] + "  url:" + urlHost[4]
                    + "  url:" + urlHost[5] + "  url:" + urlHost[6] + "  url:" + urlHost[7]);
        });

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously

            strUrlRequestToBeLogged  = currentUrl.toExternalForm();

        });

        NetUtils netUtils = new NetUtils();
        publicIp = netUtils.getClientPublicIp(hostname);

        if(strMember.equalsIgnoreCase("visitor-user")){
            userId = 1;
            strUsername = "visitor-user";
        }

        verticalLayout.removeAll();


    if(strMember.equalsIgnoreCase(STR_ALL_MEMBERS) )  {


        verticalLayout.add(loadHeader("Gallery of Images", "To please your eyes", ""));

        //strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;

//            String[] arrColumnNamesGallery = {"name_new", "title" , "subtitle" , "photo_type" , "uploader", "city_name", "meta_date" };
//
//            String sqlReadGallery = "SELECT pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, d.city_name, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date " + //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
//                    " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id" +
//                    " WHERE pm.hostname like '"+hostname+"' "+
//                    " ORDER BY pm.title ASC ";
        String sqlGalleryAll = sqlReadGallery + " WHERE pm.hostname like '" + hostname + "' AND pm.visible_to = 'ALL' ";
        if(strDestination.isEmpty()) {
          logger.error(" empty strDestination: " + strDestination);
        }
        else if(strDestination.equalsIgnoreCase(STR_ALL_DESTINATIONS)) {
//            sqlGalleryAll = sqlGalleryAll + " AND d.city_name LIKE '" + strDestination + "' ";

        }else if (!strDestination.equalsIgnoreCase(STR_ALL_DESTINATIONS)) {
            sqlGalleryAll = sqlGalleryAll + " AND d.city_name LIKE '" + strDestination + "' ";
        }

        sqlGalleryAll = sqlGalleryAll +  " ORDER BY pm.date_inserted DESC, pm.title ASC, meta_date DESC ";

        loadImagesFromDb(sqlGalleryAll, arrColumnNamesGallery, false);
    }
    else {

        verticalLayout.add(loadHeader("My Photos", "and how to manage them and my Albums.", ""));


        String sqlGalleryUser = sqlReadGallery +
                " WHERE pm.hostname like '" + hostname + "' AND pm.visible_to = 'ALL' AND pm.uploader LIKE '" + strMember + "' " +
                " ORDER BY pm.date_inserted DESC, meta_date DESC";

        loadImagesFromDb(sqlGalleryUser, arrColumnNamesGallery, true);
    }


        this.removeAll();
        this.add(verticalLayout);
        this.add(genericView.loadFooter(isMobile));

        logVisitorToDb();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String o) {
        strMember = o;//beforeEvent.getRouteParameters().get("member").orElse("pictures");
    }

    private void constructUI() {
        addClassNames("image-gallery-view");
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


        if(hostname.equalsIgnoreCase(HOSTNAME_LAPTOP)){
            DIR_PHOTOS_SERVER = "/home/mike/Pictures/lazy-photos";
        }
        else if(hostname.equalsIgnoreCase("piot")) {
            DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
        }
        else{
            DIR_PHOTOS_SERVER = "/home/sammy/lazy-photos";

        }



        verticalLayout = new VerticalLayout();
        if(isMobile){
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
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
        }else {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.XLARGE,
                    Padding.Top.XSMALL,
                    Gap.LARGE,
                    //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                    //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
            verticalLayout.getStyle().set("gap","3rem");
        }

        this.setWidthFull();

    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader,String strSection){

        this.strHeader = strHeader;

        HorizontalLayout headerContainerMaster = new HorizontalLayout();
        if(isMobile){
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
        }else {
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

        H3 header = new H3(strHeader+" ...");
        header.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.LARGE, FontWeight.BOLD, TextColor.SECONDARY);
//        header.getStyle().set("font-family", "Times-New-Roman, serif");

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.SMALL, TextColor.SECONDARY);

        headerTextContainer.add(header,subheader);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Most Viewed", "Least Viewed", "Most Favourite", "Least Favourite", "Newest First", "Oldest First", "Most Liked", "Least Liked");
        sortBy.setValue("Most Viewed");

        HorizontalLayout headerContainerSecondary = new HorizontalLayout();
        if(isMobile){
            headerContainerSecondary.addClassNames(
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        }else {
            headerContainerSecondary.addClassNames(
                    AlignItems.CENTER, JustifyContent.EVENLY,
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
        if(isMobile){
            layoutFilters.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.SMALL,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE);
        }else {
            layoutFilters.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }
        layoutFilters.addClassName("header-layout");

        ArrayList<String> lstDestinations = new ArrayList<>();
        lstDestinations.add("Athens");
        lstDestinations.add("Thessaloniki");
        lstDestinations.add("Alonissos");
        lstDestinations.add("London");

        RouteParam routeMember = new RouteParam("member", strMember);

        RouteParam routeDestinationAll = new RouteParam("destination", STR_ALL_DESTINATIONS);
        RouteParameters routeParamsAll = new RouteParameters(routeDestinationAll,routeMember);
        RouterLink linkPhotoDestinationAll = new RouterLink("All Locations", ImageGalleryView.class,routeParamsAll);
        layoutFilters.add(linkPhotoDestinationAll);

        for(int c = 0; c< lstDestinations.size(); c++){
            String captionDestination = lstDestinations.get(c);
            RouteParam routeParamDestination = new RouteParam("destination", captionDestination);

            RouterLink linkPhotoCategory = new RouterLink(captionDestination, ImageGalleryView.class,new RouteParameters(routeParamDestination,routeMember));
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

        VerticalLayout layoutHeaderParameters = new VerticalLayout();
        if(isMobile){
            layoutHeaderParameters.addClassNames(
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.SMALL,
                    Padding.NONE,
                    Gap.XSMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        }else {
            layoutHeaderParameters.addClassNames(
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.SMALL,
                    Padding.NONE,
                    Gap.XSMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
//                       Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }

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
        for (Tab tab : new Tab[] {tabFilterLocation, tabFilterKeyword, tabFilterUser}) {
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

//        headerContainerMaster.add(headerTextContainer, cmbView);
        headerContainerSecondary.add(layoutFilters);  //, sortBy);
        layoutHeaderParameters.add(headerContainerMaster,headerContainerSecondary);

        return layoutHeaderParameters;
    }

    private void loadImagesFromDb(String sqlRead, String[] arrColumnNames,boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar;

        Div divGallery = new Div();
        divGallery.addClassName("gallery");

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            Record rec = lstRecords.get(r);
            divGallery.add(getImageGalleryThumbsFromDb(rec,isEditable));
        }
        verticalLayout.add(divGallery);
    }

    private ImageGalleryViewCard getImageGalleryThumbsFromDb(Record record, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");

        String strCityName = record.getColumnData("city_name");
        String strUploader = record.getColumnData("uploader");

        RouteParam routeUploader = new RouteParam("member", strUploader);
        RouterLink linkUploader = new RouterLink(strUploader, ImageGalleryView.class,new RouteParameters(routeUploader));

        RouteParam routeDestination = new RouteParam("destination", strCityName);
        RouterLink linkDestination = new RouterLink(strCityName, ImageGalleryView.class,new RouteParameters(routeDestination));

//        ArrayList<RouterLink> lstRouterLinks =new ArrayList<>();
//        lstRouterLinks.add(linkDestination);

        String strImagePath = strPath + dirChar + strFileName;
        logger.info(" strImagePath "+strImagePath);

        ImageGalleryViewCard imageGalleryViewCard = new ImageGalleryViewCard(record,strImagePath,isMobile,userId, strUsername, sessionCreation,hostname,publicIp, isEditable,
                linkUploader, linkDestination, recordService);
        return imageGalleryViewCard;
    }

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {
        logger.info(" photo  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql,arrColumnNames);
    }

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames, Object[] sqlParValue, String[] sqlParType) {
        logger.info(" photo  getRecordsFromDb with params:   " + sql);
        return recordService.findAll(sql,arrColumnNames, sqlParValue, sqlParType);
    }

    private void logVisitorToDb() {

//        member = member.replaceAll("'", " ");
//        member = member.replaceAll("\"", " ");

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

        if(!strMember.equalsIgnoreCase("visitor-user")){
            strUsername = "view-all";
        }

        if(strUrlRequestToBeLogged == null || strUrlRequestToBeLogged.isEmpty())
        {
            strUrlRequestToBeLogged = "NULL";
        }else{
            strUrlRequestToBeLogged = "'"+strUrlRequestToBeLogged+"'";
        }

        if(strPath == null || strPath.isEmpty()){
            strPath = "NULL";
        }else{
            strPath = "'"+strPath+"'";
        }

        logger.info("photo visitor:" + publicIp + " . " + hostname + " . " + hostAddress + " . " + canonicalHostname + " .  "+ browser + " " + sessionid);

        String insertSQL = "INSERT INTO dbvisitor_log SET visitorlogId = 0,  timeOfVisit = now(), ipAddress = '" + publicIp + "', browserName = '" + browser + "', "
                + " browserVersionMajor = '" + versionOfBrowserMajor + "', browserVersionMinor = '" + versionOfBrowserMinor + "', urlParameter = NULL , timeZoneId = '" + timeZoneId + "', "
                + "appVersion = '" + APP_NAME + "-" + APP_VERSION + "', sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
                + "hostAddress = '" + hostAddress + "', os = '" + strOS + "', section = '" +section+"',"
                + " item = " +strPath+", ref = "+strUrlRequestToBeLogged+", "
                + " username = '" + strUsername +"', "
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

    private String getFileSize(File file){

        return String.format("%.2f", getFileSizeDouble(file));
    }

    private double getFileSizeDouble(File file) {

        double filesizeMB = (double) file.length() / (1024 * 1024);// + " mb";
        return filesizeMB;
    }

    private String getMBFromLong(long size) {

        double filesizeMB = (double)  size / (1024 * 1024);// + " mb";
        return String.format("%.2f", filesizeMB);
    }
}
