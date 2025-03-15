package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.GenericView;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.MainLayout.*;

//@PageTitle("Image Gallery")
//@RouteAlias("") // empty on homepage
@Route(value = "learnings") //":category?")
@RouteAlias(value = "learnings/category/:category?", layout = MainLayout.class)
@RouteAlias(value = "learnings/tutor/:tutor?", layout = MainLayout.class)
//@RouteAlias(value = "learnings/category/:category/tutor/:tutor?", layout = MainLayout.class)

//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class LearningsView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents,HasDynamicTitle, HasStyle {

    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";

    private static final Logger logger = LoggerFactory.getLogger(LearningsView.class);

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
    private String tutor;

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


    private String[] arrColumnNamesGallery = {"name_new", "title" , "subtitle" , "photo_type" , "uploader", "city_name", "meta_date"
            ,"space_size","space_size_medium", "space_size_thumb","meta_camera_make", "meta_camera_model","meta_lens_make","meta_lens_model"
            ,"meta_focal_length", "meta_focal_length_ff", "meta_iso"
            ,"location_by_user","location_area","location_country_code","location_lat","location_lon"
            ,"date_inserted"};

    private String sqlReadGallery = "SELECT pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, d.city_name, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, " +
            " pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model, "+
            " pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, "+
            "  pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon "+
            //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
            " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id ";


    String[] arrColumnsLearning = {"title", "picture" , "section" , "category" , "format" , "url" , "artists_ref" , "description" , "duration" , "pages" , "published",
            "tutor_name" , "website" , "url_fb" , "url_yt" , "url_insta" , "url_flickr" , "url_wikipedia", "url_ref1", "url_ref2", "url_ref3",
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
            urlHost[5] = currentUrl.getPort()+"";
            urlHost[6] = currentUrl.getAuthority();
            urlHost[7] = currentUrl.getQuery();

            logger.info("  url:" + urlHost[0] + "  url:" + urlHost[1] + "  url:" + urlHost[2] + "  url:" + urlHost[3] + "  url:" + urlHost[4]
                    + "  url:" + urlHost[5] + "  url:" + urlHost[6] + "  url:" + urlHost[7]);
        });


        userId = 1;
        strUsername = "visitor-user";



        verticalLayout.removeAll();
        VerticalLayout layoutHeaderParameters = loadHeader("Learnings", "Filter to learn for:",category);
        verticalLayout.add(layoutHeaderParameters);
        VerticalLayout layoutResults = loadResults(null);
        verticalLayout.add(layoutResults);
        verticalLayout.add(genericView.loadFooter(isMobile));

        logVisitorToDb();
    }

    @Override
    public void setParameter( BeforeEvent beforeEvent, @OptionalParameter String o) {
//        category = o;//beforeEvent.getRouteParameters().get("category").orElse("pictures");
    }

    private void constructUI() {
        addClassNames("learnings-view");
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
        verticalLayout.setId("verticalLayout");
        if(isMobile){
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.NONE,
                    Padding.Top.XSMALL,
//                    Gap.MEDIUM,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        }else {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.LARGE,  //<---
                    Padding.Top.XSMALL,
//                    Gap.LARGE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );

        }

        this.setWidthFull();
        this.add(verticalLayout);
    }

    private VerticalLayout loadResults(String inCategory){

        String strWhereSubClause ="";

        if(!tutor.isEmpty() && !tutor.equalsIgnoreCase(STR_ALL_TUTORS)) {
            strWhereSubClause = strWhereSubClause   + " AND t.tutor_name LIKE '"+tutor+"' ";
        }

        if(category.isEmpty() ||  category.equalsIgnoreCase(STR_ALL_CATEGORIES)) {
        }
        else if (inCategory!=null && !inCategory.isEmpty()){
            strWhereSubClause = strWhereSubClause  + " AND l.category LIKE '"+inCategory+"' ";
        }else{
            strWhereSubClause = strWhereSubClause  + " AND l.category LIKE '"+category+"' ";
        }
        sqlLearningsReadOrderBy =" ORDER BY l.dateInsert DESC";
        String sqlRead = sqlLearningsRead + strWhereSubClause + sqlLearningsReadOrderBy;

//        VerticalLayout  layoutLearnings = loadLearnings(sqlRead, arrColumnsLearning);
//        layoutLearnings.setId("layoutLearnings");
//

        strPath = DIR_PHOTOS_SERVER + dirChar;

        VerticalLayout  layoutLearnings = new VerticalLayout();
        if(isMobile){
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
        }else {
            layoutLearnings.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.LARGE, // <---
//                    Padding.Top.NONE,
//                    Padding.XLARGE,
                    Gap.LARGE,
                    //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                    //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
//            layoutLearnings.getStyle().set("gap","3rem");
        }


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnsLearning);
        logger.info(" record size: "+lstRecords.size());
        for (int r = 0;r< lstRecords.size();r++) {

            Record rec = lstRecords.get(r);
            layoutLearnings.add(getLearningsItem(rec));
        }

        return layoutLearnings;
    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader,String strSection){

        this.strHeader = strHeader;
        HorizontalLayout headerContainerMaster = new HorizontalLayout();
        if(isMobile){
            headerContainerMaster.addClassNames(
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Overflow.HIDDEN,// Width.FULL,
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
                    Overflow.HIDDEN, //Width.FULL,
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
                AlignItems.CENTER, JustifyContent.START,
                Margin.NONE, Padding.NONE,
                Gap.XSMALL);

        H3 header = new H3(strHeader+" ...");
        header.addClassNames(Margin.Bottom.NONE, Margin.Top.NONE, FontSize.LARGE, FontWeight.BOLD, TextColor.SECONDARY);
//        header.addClassName("text-header");
        //header.getStyle().set("font-family", "Times-New-Roman, serif");  //"'Brush Script MT', cursive");

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(Margin.Bottom.NONE, Margin.Top.NONE, FontSize.SMALL, TextColor.SECONDARY);

        headerTextContainer.add(header,subheader);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Most Viewed", "Least Viewed", "Most Favourite", "Least Favourite", "Newest First", "Oldest First", "Most Liked", "Least Liked");
        sortBy.setValue("Most Viewed");

        HorizontalLayout headerContainerSecondary = new HorizontalLayout();
        if(isMobile){
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
        }else {
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


        ArrayList<String> lstCategories = new ArrayList<>();
        lstCategories.add("Street Photography");
        lstCategories.add("Landscape Photography");
        lstCategories.add("Techniques");

        RouteParam routeCategoryAll = new RouteParam("category", STR_ALL_CATEGORIES);
        RouterLink linkPhotoCategoryAll = new RouterLink("All Categories", LearningsView.class,new RouteParameters(routeCategoryAll));
        layoutFilters.add(linkPhotoCategoryAll);

        for(int c=0;c<lstCategories.size();c++){
            String captionCategory = lstCategories.get(c);
            RouteParam routeCategory = new RouteParam("category", captionCategory);
            RouterLink linkPhotoCategory = new RouterLink(captionCategory, LearningsView.class,new RouteParameters(routeCategory));
            layoutFilters.add(linkPhotoCategory);
        }


//        CheckboxGroup<String> checkboxGroupSubject = new CheckboxGroup<>();
//        checkboxGroupSubject.setTooltipText("Category");
//        checkboxGroupSubject.setLabel("Subject");
//        checkboxGroupSubject.setItems("Photography", "Street Photography", "Landscape", "Cityscape");
//        checkboxGroupSubject.select(category);

        CheckboxGroup<String> checkboxGroupFormat = new CheckboxGroup<>();
        checkboxGroupFormat.setTooltipText("Format");
//        checkboxGroupFormat.setLabel("Format");
        checkboxGroupFormat.setItems("Book", "Youtube");
//        Div lblFilterFormat = new Div("Format");
//        layoutFilters.add(checkboxGroupFormat);



        CheckboxGroup<String> checkboxGroupLocation = new CheckboxGroup<>();
        checkboxGroupLocation.setTooltipText("Location");
//         checkboxGroupLocation.setLabel("Location");
        checkboxGroupLocation.setItems("Hungary", "UK", "Greece");//, "Thursday",

//        layoutFilters.add(checkboxGroupLocation);


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




        Select<String> cmbView = new Select<>();
        cmbView.setLabel("View");

        cmbView.setItems("Micro View", "Ordinary - No MetaData", "Ordinary - MetaData Bottom", "Ordinary - MetaData Right",
                "Wide - No MetaData", "Wide - MetaData Bottom","Wide - MetaData Right");
        cmbView.setValue("Ordinary - No MetaData");

        headerContainerMaster.add(headerTextContainer);
        headerContainerSecondary.add(layoutFilters);
        layoutHeaderParameters.add(headerContainerMaster,headerContainerSecondary);

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
////            layoutLearnings.getStyle().set("gap","3rem");
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

    public VerticalLayout getLearningsItem( Record record) {

        String strTitle = record.getColumnData("title");
        String strCategory = record.getColumnData("category");

        String strFormat = record.getColumnData("format");
        String strDuration  = record.getColumnData("duration");
        String strPages  = record.getColumnData("pages");

        String strTutor = record.getColumnData("tutor_name");
        Div divTutor = new Div();
        divTutor.addClassName(TextColor.SECONDARY);
        divTutor.setVisible(false);
        if(!strTutor.equalsIgnoreCase("null") && !strTutor.isEmpty())
        {
            divTutor.setText(strTutor);
            divTutor.setVisible(true);
        }

        String strTutorTeam = record.getColumnData("learnings_team_id");
        Div divTutorTeam = new Div();
        divTutorTeam.addClassName(TextColor.SECONDARY);
        divTutorTeam.setVisible(false);
        if(!strTutorTeam.equalsIgnoreCase("null") && !strTutorTeam.isEmpty())
        {
            divTutorTeam.setText(strTutorTeam);
            divTutorTeam.setVisible(true);
        }

        String strImage = record.getColumnData("picture");

        if(!strImage.equalsIgnoreCase("null") && !strImage.equalsIgnoreCase(""))
        {
            strImage = strPath+"/"+strImage;
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

        H5 titleName = new H5(strTitle);
        titleName.addClassName(TextColor.SECONDARY);

        String strDate = "";
        String dt = record.getColumnData("dateInsert");
        SimpleDateFormat toui = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat fromdb = new SimpleDateFormat("yyyy-MM-dd");

        try {
            strDate = toui.format(fromdb.parse(dt));
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }

        Div dayUpdated = new Div("updated: "+strDate);
        dayUpdated.addClassName(TextColor.SECONDARY);

        HorizontalLayout layoutPostTitle = new HorizontalLayout();
        if(isMobile) {
            layoutPostTitle.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.AROUND,
                    Margin.XSMALL,
                    Padding.XSMALL,
                    Gap.XSMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_10,
                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
        }else{
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
        layoutPostTitle.add(layoutSection, titleName, divTutor, dayUpdated);

        VerticalLayout layoutLearningInfo = new VerticalLayout();
        if(isMobile){
            layoutLearningInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    Background.CONTRAST_5,
                    BorderRadius.NONE);
        }else {
            layoutLearningInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_5,
                    BorderRadius.LARGE);
            layoutLearningInfo.addClassName("bottom-radius-shadow");
        }

        HorizontalLayout layoutImage = new HorizontalLayout();

        layoutImage.addClassNames(Padding.SMALL, Background.CONTRAST_70, Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE,
                BoxShadow.SMALL);

        if(!strImage.equalsIgnoreCase("null") && !strImage.equalsIgnoreCase(""))
        {
            String finalStrImage = strImage;
            final StreamResource imageResource = new StreamResource("image", () -> {
                try
                {
                    return new FileInputStream(new File(finalStrImage));
                }
                catch(final FileNotFoundException e)
                {
                    logger.error("FileNotFoundException learning "+e.getMessage());
                    return null;
                }
            });

            Image img = new Image(imageResource, "image");
            img.setMaxHeight("270px");
            img.addClassNames(BorderRadius.LARGE);

            layoutImage.add(img);

        }

        Div divFormat = new Div();
        if(strFormat.equalsIgnoreCase("YouTube"))
        {
            if(!strDuration.equalsIgnoreCase("null") && !strDuration.equalsIgnoreCase(""))
            {
                divFormat.setText(strFormat+"("+strDuration+")");
            }else {
                divFormat.setText(strFormat);
            }
        }
        else if(strFormat.equalsIgnoreCase("book"))
        {
            layoutImage.setMaxWidth("290px");
            if(!strPages.equalsIgnoreCase("null") && !strPages.equalsIgnoreCase(""))
            {
                divFormat.setText("Book ("+strPages+" pages)");
            }else {
                divFormat.setText("book");
            }
        }
        else if(strFormat.equalsIgnoreCase("Url with Free e-book"))
        {
            layoutImage.setMaxWidth("420px");
            if(!strPages.equalsIgnoreCase("null") && !strPages.equalsIgnoreCase(""))
            {
                divFormat.setText("E-Book ("+strPages+" pages)");
            }else {
                divFormat.setText("E-Book");
            }
        }
        else
        {
            layoutImage.setMaxWidth("290px");
            if(!strPages.equalsIgnoreCase("null") && !strPages.equalsIgnoreCase(""))
            {
                divFormat.setText(strFormat+"("+strPages+" pages)");
            }else {
                divFormat.setText(strFormat);
            }
        }

        Anchor linkTutor = new Anchor();
        linkTutor.add(FontAwesome.Solid.LINK.create());
        // linkTutor.getStyle().setColor(strColorExternalweb);
        //  linkTutor.setClassName("lazy-result-line-button");

        String strUrlTutorExt = record.getColumnData("website");
        if(!strUrlTutorExt.equalsIgnoreCase("null") && !strUrlTutorExt.equalsIgnoreCase("")) {

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
        if(!strUrlTutorYt.equalsIgnoreCase("null") && !strUrlTutorYt.equalsIgnoreCase("")) {

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
        if(!strUrlTutorWikipedia.equalsIgnoreCase("null") && !strUrlTutorWikipedia.equalsIgnoreCase("")) {

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
        if(!strUrlTutorInsta.equalsIgnoreCase("null") && !strUrlTutorInsta.equalsIgnoreCase("")) {

            // linkTutorInsta.setText("Instagram");
//            strUrlTutorInsta = "https://www.instagram.com/"+ strUrlTutorInsta;
            linkTutorInsta.setHref(strUrlTutorInsta);
            linkTutorInsta.setTarget("_blank");
            linkTutorInsta.setVisible(true);
        }

        String strDescription = record.getColumnData("description");

        Paragraph parDescription = new Paragraph(strDescription);
        parDescription.addClassNames(TextColor.TERTIARY,FontSize.MEDIUM,Padding.MEDIUM);
        if(!strDescription.equalsIgnoreCase("null") && !strDescription.equalsIgnoreCase("")) {
            parDescription.setVisible(true);
        }
        else{
            parDescription.setVisible(false);
        }
        Anchor link1InNewTab = new Anchor();

        String strUrl = record.getColumnData("url");
        String strYouTubeVideo = "https://www.youtube.com/watch?v=";
        String strVideoOnly = strUrl.replace(strYouTubeVideo, "");

        String youtubeEmbedded = "<div><iframe class='video-iframe' src='https://www.youtube.com/embed/"+strVideoOnly+"' title='"+strTitle+"'  allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share'  allowFullScreen></iframe></div>";

        Html htmlVideo =new Html(youtubeEmbedded);
        htmlVideo.setHtmlContent(youtubeEmbedded);
//        htmlVideo.addClassNames(Padding.SMALL, Margin.MEDIUM, Background.CONTRAST_60, BorderRadius.LARGE);
        htmlVideo.setClassName("video-container");

//        Div layoutVideo = new Div();
//        layoutVideo.addClassNames( AlignItems.CENTER, JustifyContent.CENTER,
//                Padding.NONE, Margin.NONE,                    Width.FULL,
//                Background.CONTRAST_70, Border.NONE, BorderRadius.LARGE,
//                BoxShadow.MEDIUM);

//        layoutVideo.add(htmlVideo);


        VerticalLayout layoutSourceCard = new VerticalLayout();
        layoutSourceCard.addClassNames(
                Overflow.HIDDEN, //Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.SMALL,
                Padding.MEDIUM,
                Gap.MEDIUM,
                TextColor.SECONDARY
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                Background.TINT_10
//                BorderColor.CONTRAST_10,
//                Border.ALL,  BorderRadius.LARGE
        );


        layoutSourceCard.setMaxWidth("280px");

        RouteParam routeCategory = new RouteParam("category", strCategory);
        RouteParam routeTutor = new RouteParam("tutor", strTutor);

        RouterLink linkPhotoCategory = new RouterLink(strCategory, LearningsView.class,new RouteParameters(routeCategory));
        RouterLink linkLearningTutor = new RouterLink(strTutor, LearningsView.class,new RouteParameters(routeTutor));

        Div divTutorInfo = new Div();
        divTutorInfo.addClassName(TextColor.SECONDARY);
        divTutorInfo.setVisible(false);
        if(!strTutor.equalsIgnoreCase("null") && !strTutor.isEmpty())
        {
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
        layoutExtLinks.add(linkTutor,linkTutorWikipedia,linkTutorInsta,linkTutorYt);

        HorizontalLayout layoutData = new HorizontalLayout();
        layoutData.addClassNames(AlignItems.CENTER,JustifyContent.CENTER,
                Width.FULL);

        layoutData.add(layoutImage,htmlVideo,layoutSourceCard);

        VerticalLayout layoutIDData = new VerticalLayout();
        layoutIDData.addClassNames(AlignItems.CENTER,JustifyContent.CENTER, //Width.FULL,
        Background.TINT_10, BorderRadius.LARGE,
                Margin.NONE, Padding.LARGE,
                Gap.SMALL,
                BoxShadow.XSMALL
                );
        layoutIDData.add(imgPerson, linkLearningTutor, divTutorTeam, layoutExtLinks);
        layoutIDData.addClassName("item-id-info");

        VerticalLayout layoutItemInfo = new VerticalLayout();
        layoutItemInfo.addClassNames(AlignItems.CENTER,JustifyContent.CENTER, //Width.FULL,
                Background.TINT_10, BorderRadius.LARGE,
                Margin.NONE, Padding.LARGE,
                Gap.SMALL,
                BoxShadow.XSMALL
        );
        layoutItemInfo.add( imgInfo, divFormat, linkPhotoCategory);
        layoutItemInfo.addClassName("item-id-info");


        layoutSourceCard.add(layoutIDData,layoutItemInfo);



        if(!strUrl.equalsIgnoreCase("null") && !strUrl.equalsIgnoreCase(""))
        {
            if(strFormat.equalsIgnoreCase("YouTube"))
            {
                link1InNewTab.setVisible(false);
                htmlVideo.setVisible(true);
                layoutImage.setVisible(false);
            }
            else {
                link1InNewTab.setText(strUrl);
                //link1InNewTab.setTarget(festUrl);
                link1InNewTab.setHref(strUrl);
                link1InNewTab.setTarget("_blank");
                //link1InNewTab.getElement().setAttribute("target", "_blank");
                link1InNewTab.setVisible(true);
                htmlVideo.setVisible(false);
                layoutImage.setVisible(true);
            }
        }
        else{
            link1InNewTab.setVisible(false);
            htmlVideo.setVisible(false);
            layoutImage.setVisible(true);
        }

        logger.info("  htmlVideo  "+htmlVideo.isVisible());

        HorizontalLayout layoutPostRelated = new HorizontalLayout();
        layoutPostRelated.addClassNames(Width.FULL,
                AlignItems.CENTER,JustifyContent.CENTER,
        TextColor.SECONDARY,
        Padding.NONE, Margin.NONE, BorderRadius.LARGE);


        if(strFormat.equalsIgnoreCase("Url with Free e-book"))
        {
            Div lblGotoUrl = new Div("Click to go to author's site, to download the e-book.");
            if(strUrl!=null && !strUrl.isEmpty()) {
                Anchor linkSourceToNewTab = new Anchor();
                String strUrlShorter = "";
                if(strUrl.trim().length()>50)
                {
                    strUrlShorter =  strUrl.substring(0,46)+"...";
                }
                linkSourceToNewTab.setText(strUrlShorter);
                linkSourceToNewTab.setHref(strUrl);
                linkSourceToNewTab.setTarget("_blank");
                linkSourceToNewTab.setVisible(true);


                layoutPostRelated.add(lblGotoUrl,linkSourceToNewTab);
            }
        }

        layoutLearningInfo.add(layoutPostTitle,layoutData,parDescription,layoutPostRelated,getSubTabs("learning", strTitle,record));

        return layoutLearningInfo;
    }



    private HorizontalLayout getActions(){

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
        if(isMobile) {
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
        }else{
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


        layoutActions.add(btnLike,btnMoreAction, btnComment,btnMoreInfo,btnShare);

        return layoutActions;
    }

    private VerticalLayout getSubTabs (String strContentType, String strContentTitle, Record record) {

        VerticalLayout layoutTabsInfo = new VerticalLayout();
        if(isMobile) {
            layoutTabsInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.MEDIUM
            );
        }else{
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

        btnGroup.addValueChangeListener(event->{
            if(event.getValue().toString().equalsIgnoreCase("My Notes")){
                divTabContent.setText(" my notes ... of "+strContentTitle+" in "+strContentType);
            }
            else if(event.getValue().toString().equalsIgnoreCase("Reviews")){
                divTabContent.setText(strUsername+" users review 1 ...");
            }
            else{
                divTabContent.setText(strContentTitle+" ....... in "+strContentType);
            }
        });

        layoutTabsInfo.add(btnGroup,divTabContent);


        return layoutTabsInfo;
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

//        category = category.replaceAll("'", " ");
//        category = category.replaceAll("\"", " ");

        //search = search.replaceAll("'"," ");
        //search = search.replaceAll("\""," ");

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously

            strUrlRequestToBeLogged  = currentUrl.toExternalForm();

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


        if(strUrlRequestToBeLogged == null || strUrlRequestToBeLogged.isEmpty() || strUrlRequestToBeLogged.equalsIgnoreCase("null"))
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


        logger.info("photo visitor:" + publicIp + " . " + hostname + " . " + hostAddress + " . " + canonicalHostname + "  .  "+ browser + " " + sessionid);

        String insertSQL = "INSERT INTO dbvisitor_log SET visitorlogId = 0,  timeOfVisit = now(), ipAddress = '" + publicIp + "', browserName = '" + browser + "', "
                + " browserVersionMajor = '" + versionOfBrowserMajor + "', browserVersionMinor = '" + versionOfBrowserMinor + "', urlParameter = NULL , timeZoneId = '" + timeZoneId + "', "
                + " appVersion = '" + APP_NAME + "-" + APP_VERSION + "', sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
                + " hostAddress = '" + hostAddress + "', os = '" + strOS + "', section = '" +section+"',"
                + " item = " +strPath+", ref = "+strUrlRequestToBeLogged+", "
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
