package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.PageSeoUtil;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.utils.UtilsString;
import com.photo.act.photo_act.views.components.GenericView;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
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
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.MainLayout.*;

@AnonymousAllowed

@PageTitle("PhotoAct.net - Photography Community | Events")
@Route(value = "events")
@RouteAlias(value = "events/genre/:genre?", layout = MainLayout.class)
@RouteAlias(value = "events/organizer/:organizer?", layout = MainLayout.class)
@RouteAlias(value = "events/title/:title?", layout = MainLayout.class)

public class FestivalsView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasStyle {

    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";

    private static final Logger logger = LoggerFactory.getLogger(FestivalsView.class);

    private VerticalLayout verticalLayout;
    String sqlLearningsReadOrderBy;
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String section = SECTION_FESTIVALS;
    private String forMemberName;
    private RecordService recordService;
    private String strHeader;

    private String dirChar = FileSystems.getDefault().getSeparator();
    public static String subPathThumbs = "photo-thumbs";
    public static String subPathMedium = "photo-medium";
    public static String subPathUpload = "photo-upload";
    public static String subPathShow = "photo-show";

    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";

    String[] arrFestivalsColumnNames = {"nameShort", "city_name", "country", "periodOfYear", "type", "website", "url_facebook", "url_instagram", "url_youtube", "activities", "image_top", "image_logo", "dateInsert", "title", "subtitle", "formatedDateFrom", "formatedDateTo",
            "edition_description", "formatedDateUpdated", "title_of_place", "address_of_place", "url_planned", "url_fb", "url_insta"};

    private int userId;
    private String publicIp;
    private String strPath;
    private String hostname;
    private String hostAddress;
    private String canonicalHostname;
    private String strOS;
    private String strBrowser;
    String sqlFestivalsRead = "SELECT  f.nameShort, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert " +
            ", e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated " +
            ", e.title_of_place, e.address_of_place, e.url_planned, e.url_fb, e.url_insta " +
            ", f.country " +
            " FROM  festivals f LEFT JOIN festivals_edition e ON f.id = e.festival_id " +
            " WHERE 1 = 1 ";
//            " LEFT JOIN destination d ON  d.id = e.destination_id " +
//            " , destination d " +
//            " WHERE d.id = e.destination_id ";

    private String strColorExternalweb = "#9fafd5";

    private String[] arrClubsColumnNames = {"org_name", "org_type", "org_type_parent", "city", "used_for", "country", "url", "url_local_events", "url_fb", "url_yt", "url_insta",
            "url_flickr", "url_wikipedia"};
    private String sqlShowClubsSelect = "SELECT id, org_name, org_type, org_type_parent , city , used_for , country , url , city, address, pc, country, map_x, map_y, url, " +
            " url_local_events, url_fb, url_yt, url_insta, url_flickr, url_wikipedia, " +
            " date_inserted, dateUpdated " +
            " FROM organizations o ";
    private String sqlShowClubsWhere = " WHERE o.org_type LIKE 'Club' ";
    private String sqlShowClubsOrder = " ORDER BY o.city ASC, o.org_name ASC";


    private String[] arrColumnNamesGallery = {"name_new", "title", "subtitle", "photo_type", "uploader", "meta_date"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon"
            , "date_inserted"};

    private String sqlReadGallery = "SELECT pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, " +
            " pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model, " +
            " pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, " +
            "  pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
            //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
            " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id ";
    private VerticalLayout filtersColumn;
    private GenericView genericView;

    UtilsDate utilsDate;
    String sessionDateTime;
    private String strUrlRequestToBeLogged;

    public FestivalsView(RecordService recordService) {
        this.recordService = recordService;
        utilsDate = new UtilsDate();

        genericView = new GenericView(recordService);

        constructUI();
    }


/*    @Override
    public String getPageTitle() {
        return strHeader;
    }*/

    @Override
    public void beforeEnter(@OptionalParameter BeforeEnterEvent event) {
        PageSeoUtil.setMetaDescription("Get informed about recent future events related to photography.");
        getUserClientInfo();

        verticalLayout.removeAll();

        VerticalLayout layoutHeaderParameters = loadHeader("Events", "Photo events around the globe", "");
        VerticalLayout layoutResults = loadResults(15);
        verticalLayout.add(layoutResults);

        this.removeAll();
        this.add(layoutHeaderParameters);


        if (isMobile) {
            VerticalLayout layoutMobileContent = new VerticalLayout();
            layoutMobileContent.addClassNames(Width.FULL,
                    AlignItems.START, JustifyContent.CENTER,
                    Padding.MEDIUM, Margin.NONE,
                    Gap.XSMALL
            );
            filtersColumn.removeAll();
            filtersColumn.setMaxWidth("290px");
            verticalLayout.setMaxWidth("1040px");

//            filtersColumn.add(loadFiltersColumn());

            layoutMobileContent.add(filtersColumn, verticalLayout);
            this.add(layoutMobileContent);
        } else {
            HorizontalLayout layoutContent = new HorizontalLayout();
            layoutContent.addClassNames(Width.FULL,
                    AlignItems.START, JustifyContent.CENTER,
                    Padding.LARGE, Margin.NONE,
                    Gap.XSMALL
            );
            filtersColumn.removeAll();
            filtersColumn.setMaxWidth("290px");
            verticalLayout.setMaxWidth("980px");

//            filtersColumn.add(loadFiltersColumn());

            layoutContent.add(filtersColumn, verticalLayout);
            this.add(layoutContent);
        }

        this.add(genericView.loadFooter(isMobile));

//
//        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
//            // This is your own method that you may do something with the url.
//            // Note that this method runs asynchronously
//
//            strUrlRequestToBeLogged = currentUrl.toExternalForm();
//
//        });
//
//        NetUtils netUtils = new NetUtils();
//        publicIp = netUtils.getClientPublicIp(hostname);
//
//        verticalLayout.removeAll();
//        verticalLayout.setMaxWidth("1040px");
//
//        verticalLayout.add(loadHeader("Events", "around the World, being prepared for visitors!", ""));
//
//        String[] arrColumnNames = {"nameShort", "location", "country", "periodOfYear", "type", "website", "url_facebook", "url_instagram", "url_youtube", "activities", "image_top", "image_logo", "dateInsert", "title", "subtitle", "formatedDateFrom", "formatedDateTo", "edition_description", "formatedDateUpdated"};
//
//        String sqlRead = "SELECT  f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, " +
//                "e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
//                "FROM  festivals f LEFT JOIN festivals_edition e ON f.id = e.festival_id ORDER BY f.dateInsert DESC";
//        verticalLayout.add(loadResults(sqlRead, arrColumnNames));

//            verticalLayout.add(loadFooter());

        logVisitorToDb();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String o) {
//        section = o;//beforeEvent.getRouteParameters().get("section").orElse("pictures");
    }

    private void constructUI() {
        addClassNames(Overflow.HIDDEN, Width.FULL,
                // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                Margin.NONE, Padding.NONE,
                Gap.MEDIUM,
                //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
                AlignItems.CENTER, JustifyContent.CENTER
        );
        this.addClassName("background");

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
        } else {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.SMALL,  //<---
                    Padding.Top.XSMALL,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        }

//        Html htmlTitle = new Html("<title>'photoact.net Network and Act around Photography'</title>");
//        Html htmlMeta = new Html("<meta name='description' content='Get info about events that take place around globe for friends of photography.'>");
//        verticalLayout.add(htmlTitle, htmlMeta);


        this.add(verticalLayout);
    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSection) {

        this.strHeader = strHeader;

        VerticalLayout headerContainer = new VerticalLayout();
        if (isMobile) {
            headerContainer.addClassNames(
                    AlignItems.START, JustifyContent.BETWEEN,
                    Overflow.HIDDEN,// Width.FULL,
                    Margin.NONE,
                    Padding.SMALL,
                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        } else {
            headerContainer.addClassNames(
                    AlignItems.START, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, //Width.FULL,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }
        headerContainer.addClassName("header-layout");


        H1 header = new H1(strHeader);

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.SMALL, TextColor.SECONDARY);


        H3 headerSection = new H3(strSection);
        headerSection.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Margin.Top.MEDIUM,
                Padding.NONE
        );


        Div divLine = new Div();
        divLine.addClassNames(Border.BOTTOM, Width.FULL);

        headerContainer.add(header, subheader, divLine, headerSection);

        return headerContainer;
    }

    private VerticalLayout loadResults(int intLimit) {

        sqlLearningsReadOrderBy = " ORDER BY f.dateInsert DESC";

        String sqlLimit = "";
        if (intLimit == 0) {

        } else {
            sqlLimit = " LIMIT " + intLimit;
        }

        String sqlRead = sqlFestivalsRead + sqlLearningsReadOrderBy + sqlLimit;

        strPath = DIR_PHOTOS_SERVER + dirChar;

        VerticalLayout layoutFestivals = new VerticalLayout();
        if (isMobile) {
            layoutFestivals.addClassNames(
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
            layoutFestivals.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.SMALL, // <----
//                    Padding.Top.NONE,
//                    Padding.XLARGE,
                    Gap.XLARGE,
                    //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                    //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
//            layoutFestivals.getStyle().set("gap","3rem");
        }

        layoutFestivals.addClassName("festivals-view");
        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrFestivalsColumnNames);

        for (int r = 0; r < lstRecords.size(); r++) {
            Record rec = lstRecords.get(r);
            layoutFestivals.add(getFestival(rec));
        }

        return layoutFestivals;
    }


    public VerticalLayout getFestival(Record record) {


        HorizontalLayout layoutSection = new HorizontalLayout();
        layoutSection.addClassName("country");
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
//        titleName.setClassName("lazy-result-line-title");

        String strCountry = record.getColumnData("country"); // from locations
        String strCityName = record.getColumnData("city_name"); // from locations

        String strTitleOfPlace = record.getColumnData("title_of_place");
        String strAddressOfPlace = record.getColumnData("address_of_place");

        String strDate = "";
        String dt = record.getColumnData("formatedDateUpdated");

        Div dayUpdated = new Div("updated: " + dt);
        dayUpdated.addClassName(TextColor.TERTIARY);

//        HorizontalLayout layoutPostTitle = new HorizontalLayout();
//        if (isMobile) {
//            layoutPostTitle.addClassNames(
//                    Overflow.HIDDEN, Width.FULL,
//                    AlignItems.CENTER, JustifyContent.AROUND,
//                    Margin.NONE, Padding.NONE,
//                    Gap.XSMALL,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_10,
//                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20,
//                    BorderRadius.NONE);
//        } else {
//            layoutPostTitle.addClassNames(
//                    Overflow.HIDDEN, Width.FULL,
//                    AlignItems.CENTER, JustifyContent.AROUND,
//                    Margin.SMALL,
//                    Padding.SMALL,
//                    Gap.MEDIUM,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_10,
//                    Border.BOTTOM, Border.RIGHT, BorderColor.CONTRAST_20, BorderRadius.FULL);
//        }
//
//        layoutPostTitle.add(layoutSection, titleName, dayUpdated);

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

        VerticalLayout layoutFestivalInfo = new VerticalLayout();
        if (isMobile) {
            layoutFestivalInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE, Padding.NONE,
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
            layoutFestivalInfo.addClassName("item-total");
        }

        HorizontalLayout layoutWithMap = new HorizontalLayout();
        layoutWithMap.addClassNames(Width.FULL);

        layoutWithMap.add(getDestinationMap(strAddressOfPlace, strTitleOfPlace, strCityName,strCountry));

        layoutFestivalInfo.add(layoutPostTitle, getFestivalItem(record), getActions());

        return layoutFestivalInfo;
    }

    private VerticalLayout getFestivalItem(Record record) {

        String strPeriodOfYear = record.getColumnData("periodOfYear");
        String strCityName = record.getColumnData("city_name");
        String strTitleOfPlace = record.getColumnData("title_of_place");
        String strAddressOfPlace = record.getColumnData("address_of_place");
        String strCuntry = record.getColumnData("country");
        String strType = record.getColumnData("type");
        String strImageLogo;
        String strImageTop;

        String strUrlEdition = record.getColumnData("url_planned");
        String strUrlEdFacebook = record.getColumnData("url_fb");
        String strUrlEdInsta = record.getColumnData("url_insta");

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

        if (strImageLogo.equalsIgnoreCase("") || strImageTop.equalsIgnoreCase("")) {
            layoutImage.setVisible(false);
        }

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

        Paragraph parDescription = new Paragraph(strType + " takes place each year  in " + strCityName + " usually during " + strPeriodOfYear + ". " + strActivities);
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
//                Background.CONTRAST_5,
                Border.ALL, BorderColor.CONTRAST_5, BorderRadius.LARGE
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
        if (!strUrlEdition.isEmpty() && !strUrlEdition.equalsIgnoreCase("null")) {
            linkWebsite.setHref(strUrlEdition);
            linkWebsite.setTarget("_blank");
            linkWebsite.setVisible(true);
        } else if (!festUrl.equalsIgnoreCase("null") && !festUrl.isEmpty()) {
            linkWebsite.setHref(festUrl);
            linkWebsite.setTarget("_blank");
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
        if (!strUrlEdFacebook.isEmpty() && !strUrlEdFacebook.equalsIgnoreCase("null")) {
            linkFacebookNewTab.setHref(strUrlEdFacebook);
            linkFacebookNewTab.setTarget("_blank");
            linkFacebookNewTab.setVisible(true);
        } else if (!festUrlFace.equalsIgnoreCase("null") && !festUrlFace.equalsIgnoreCase("")) {
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
        if (!strUrlEdInsta.isEmpty() && !strUrlEdInsta.equalsIgnoreCase("null")) {
            linkInstaNewTab.setHref(strUrlEdInsta);
            linkInstaNewTab.setTarget("_blank");
            linkInstaNewTab.setVisible(true);
        } else if (!festUrlInsta.equalsIgnoreCase("null") && !festUrlInsta.equalsIgnoreCase("")) {
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

        VerticalLayout layoutPlannedTitle = new VerticalLayout();
        layoutPlannedTitle.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);


        Div divTitleOfPlace = new Div(strTitleOfPlace);
        divTitleOfPlace.addClassNames(FontWeight.SEMIBOLD,
                TextColor.SECONDARY
        );
        if (strTitleOfPlace.isEmpty() || strTitleOfPlace.equalsIgnoreCase("null")) {
            divTitleOfPlace.setVisible(false);
        }

        Div divAddressOfPlace = new Div(strAddressOfPlace);
        divAddressOfPlace.addClassNames(
                TextColor.SECONDARY
        );
        if (strAddressOfPlace.isEmpty() || strAddressOfPlace.equalsIgnoreCase("null")) {
            divAddressOfPlace.setVisible(false);
        }

        Div h5Title = new Div(strTitle);
        h5Title.addClassNames(FontWeight.SEMIBOLD,
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

        String strTakesPlace = strType + " takes place between";

        Div divTakesPlace = new Div(strTakesPlace);
        divTakesPlace.addClassNames(
                TextColor.TERTIARY
        );

        Div divAnd = new Div("and");
        divAnd.addClassNames(
                TextColor.TERTIARY
        );

        HorizontalLayout datesLayout = new HorizontalLayout();
        datesLayout.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);
        datesLayout.add(h6DateFrom, divAnd, h6DateTo);
        layoutPlannedTitle.add(divTitleOfPlace, divAddressOfPlace, h5Title, divTakesPlace, datesLayout);

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
            Div divNoEvents = new Div("Currently we have no info on future events.");
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

        if (strType.equalsIgnoreCase("Exhibition")) {
            layoutDetails.add(layoutPlanned);
        } else {
            layoutDetails.add(parDescription, layoutPlanned);
        }

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

    private VerticalLayout loadFiltersColumn() {
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

//        StreamResource iconComments = new StreamResource("comments.svg",
//                () -> getClass().getResourceAsStream("/icons/comments.svg"));
//        SvgIcon svgComments = new SvgIcon(iconComments);
/*        Button btnSuggestEvent = new Button("Suggest an Event");
        btnSuggestEvent.addClassName("btn-suggest");
//        btnSuggestEvent.setIcon(svgComments);
        btnSuggestEvent.addClickListener(click -> {

        });*/

//        filtersColumn.add(btnSuggestEvent);

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


//        StreamResource iconAction = new StreamResource("stories.svg",
//                () -> getClass().getResourceAsStream("/icons/stories.svg"));
//        SvgIcon svgAction = new SvgIcon(iconAction);
        Button btnMoreAction = new Button(VaadinIcon.BOOKMARK.create());//svgAction);
        btnMoreAction.setTooltipText("Save to list");


        Button btnComment = new Button(VaadinIcon.COMMENT.create());
        btnComment.setTooltipText("Comment on it");

        Button btnUpload = new Button(VaadinIcon.UPLOAD.create());
        btnUpload.setTooltipText("Upload your related photos");

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
            layoutActions.addClassName("actions");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
            layoutActions.addClassName("actions-mobile");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
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
            layoutActions.addClassName("actions");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
        }
        //layoutActions.setWidthFull();


        layoutActions.add(btnLike, btnComment, btnMoreAction, btnUpload, btnShare);

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
                divTabContent.setText(" users review 1 ...");
            } else {
                divTabContent.setText(strContentTitle + " ....... in " + strContentType);
            }
        });

        layoutTabsInfo.add(btnGroup, divTabContent);


        return layoutTabsInfo;
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

    private IFrame getDestinationMap(String strAddressOfPlace, String strTitleOfPlace, String city, String country) {


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

        //String strMaps =
//"<iframe width='100%' height='400px' src=\""+mapSrc+"\" title=\"Navigation\" style=\"border:none;\"></iframe>";

        IFrame mapsFrame = new IFrame();
        mapsFrame.setSrcdoc(strHtml);
        mapsFrame.setWidthFull();
        mapsFrame.setHeight("550px");
        mapsFrame.getStyle().setBorder("0px");
        mapsFrame.getStyle().setBorderRadius("10px");


        return mapsFrame;
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
