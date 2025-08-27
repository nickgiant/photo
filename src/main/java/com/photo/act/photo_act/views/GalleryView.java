package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.CacheService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.GalleryImageViewCard;
import com.photo.act.photo_act.views.components.GenericView;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
import java.util.Set;

import static com.photo.act.photo_act.views.MainLayout.*;


@AnonymousAllowed

@Route(value = "photos") //":category?")
@RouteAlias(value = "photos/location/:destination?", layout = MainLayout.class)
@RouteAlias(value = "photos/member/:member?/location/:destination?", layout = MainLayout.class)


//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class GalleryView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";

    private static final Logger logger = LoggerFactory.getLogger(GalleryView.class);

    String sqlGalleryReadOrderBy;
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


    private String strUrlRequestToBeLogged;

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


    private String[] arrDestinationNames = {"id", "city_name", "prefecture", "country"};
    private String sqlReadDestination = "SELECT distinct city_name, prefecture, country " +
            " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id " +
            " ORDER BY city_name ASC ";
    @Autowired
    private CacheService cacheService;
    private String[] arrColumnNamesGallery = {"id", "name_new", "title", "subtitle", "photo_type", "uploader", "creator", "visible_to", "city_name", "meta_date", "photo_date", "photo_time"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso", "meta_aperture", "meta_shutter_speed", "meta_orientation"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon"
            , "spot_name", "spot_type"
            , "date_inserted"
            , "username", "surname", "name", "resident", "date_joined", "avatar_path"
    };
    private String sqlReadGallery = "SELECT pm.id, pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.creator, pm.visible_to, d.city_name, d.country, " +
            " DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, getDateDiffFromNow(pm.meta_date) AS photo_date, DATE_FORMAT(pm.meta_date, '%H:%i %p') AS photo_time, " +
            " pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model, " +
            " pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed, meta_orientation " +
            " , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
            " , usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
//            "  ds.spot_name, ds.spot_type " +
            //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
            " FROM dbuser usr, photo_meta pm LEFT JOIN destination d ON pm.destination_id = d.id " +
            " WHERE pm.uploaderId = usr.userId ";

    private UtilsDate utilsDate;
    private String sessionDateTime;
    private GenericView genericView;
    private String strOS;
    private String strBrowser;

    private String sqlWhereAnd;

    private int intPage = 1;
    private int intRecsOnPage = 20;
    private String strDefCountPerPage = "20";


    private Select<String> cmbCount;
    private Select<String> cmbSortBy;
    // private VerticalLayout recsHolder;

    private HorizontalLayout layoutRecControl;

    private String[] arrOrderByItems = {"Newest Upload First", "Oldest Upload First", "Newest Shot First", "Oldest Shot First"};
    private String[] arrOrderByItemsSql = {"ORDER BY pm.date_inserted DESC", "ORDER BY pm.date_inserted ASC", "ORDER BY pm.meta_date DESC", "ORDER BY pm.meta_date ASC"};
    private String sqlOrderBy = " ORDER BY pm.date_inserted DESC";
    private String strDefOrderBy = arrOrderByItems[0];

    private CheckboxGroup<String> checkboxCheckboxGroup;
    private CheckboxGroup<String> checkboxGenres;


    public GalleryView(RecordService recordService) {
        this.recordService = recordService;
        utilsDate = new UtilsDate();
        genericView = new GenericView(recordService, 1);

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


        if (strDestination.isEmpty()) {
            logger.error(" empty strDestination: " + strDestination);
        }

        intPage = 1;
//        intRecsOnPage = Integer.parseInt(cmbCount.getValue());
        VerticalLayout layoutHeaderParameters = null;
        verticalLayout.removeAll();

        if (strMember.equalsIgnoreCase(STR_ALL_MEMBERS) && (strDestination.equalsIgnoreCase(STR_ALL_DESTINATIONS) || (strDestination.isEmpty()))) {
            layoutHeaderParameters = loadHeader("Photos", "", "");

            sqlWhereAnd = " AND pm.visible_to = 'ALL' ";
            // String sqlOrderBy = " ORDER BY pm.date_inserted DESC, pm.title ASC, meta_date DESC ";

//            verticalLayout.add(loadCarouselWithThumbnails(sqlGalleryAll, arrColumnNamesGallery));

            filter(sqlOrderBy);
        } else if (strMember.equalsIgnoreCase(STR_ALL_MEMBERS) && !strDestination.equalsIgnoreCase(STR_ALL_DESTINATIONS) && !strDestination.isEmpty()) {
            layoutHeaderParameters = loadHeader("Photos", "", strDestination);
            layoutHeaderParameters.add(loadWeather(strDestination, ""));

            sqlWhereAnd = " AND pm.visible_to = 'ALL' " + " AND d.city_name LIKE '" + strDestination + "' ";

            //  String sqlOrderBy = " ORDER BY pm.date_inserted DESC, pm.title ASC, meta_date DESC ";
//            verticalLayout.add(loadCarouselWithThumbnails(sqlGalleryAll, arrColumnNamesGallery));

            filter(sqlOrderBy);
        } else if (!strMember.equalsIgnoreCase(STR_ALL_MEMBERS)) {
            layoutHeaderParameters = loadHeader("My Photos", "and how to manage them.", "");
            layoutHeaderParameters.add(loadWeather(strDestination, ""));

            sqlWhereAnd = " AND pm.uploader LIKE '" + strMember + "' ";
            //  String sqlOrderBy = " ORDER BY pm.date_inserted DESC, meta_date DESC";

//            verticalLayout.add(loadCarouselWithThumbnails(sqlGalleryUser, arrColumnNamesGallery));
//            getFilterResults(sqlWhereAnd, sqlOrderBy);
            filter(sqlOrderBy);
        } else {

            layoutHeaderParameters = loadHeader("Photos", "", "");

            sqlWhereAnd = " AND pm.visible_to = 'ALL' ";
            // String sqlOrderBy = " ORDER BY pm.date_inserted DESC, pm.title ASC, meta_date DESC ";

//            verticalLayout.add(loadCarouselWithThumbnails(sqlGalleryAll, arrColumnNamesGallery));

            filter(sqlOrderBy);
        }


        this.removeAll();

        this.add(layoutHeaderParameters);

        verticalLayout.add(layoutRecControl);
        if (isMobile) {
            VerticalLayout layoutMobileContent = new VerticalLayout();
            layoutMobileContent.addClassNames(Width.FULL,
                    AlignItems.START, JustifyContent.CENTER,
                    Padding.MEDIUM, Margin.NONE,
                    Gap.XSMALL
            );

            layoutMobileContent.add(verticalLayout);

            this.add(layoutMobileContent);
        } else {
            HorizontalLayout layoutContent = new HorizontalLayout();
            layoutContent.addClassNames(Width.FULL,
                    AlignItems.START, JustifyContent.CENTER,
                    Padding.LARGE, Margin.NONE,
                    Gap.XSMALL
            );

            layoutContent.add(verticalLayout);
            this.add(layoutContent);
        }

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
        this.addClassName("image-gallery-view");
        this.addClassName("background");

        DIR_PHOTOS_SERVER = genericView.getAppProps(PROP_PHOTOS);


        verticalLayout = new VerticalLayout();
        if (isMobile) {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,// not full width
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.NONE,
                    Padding.Top.XSMALL,
                    Gap.SMALL,
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
                    Padding.SMALL,
                    Padding.Top.XSMALL,
                    Gap.MEDIUM,
                    AlignItems.CENTER, JustifyContent.CENTER
            );

//            Html htmlTitle = new Html("<title>'photoact.net Network and Act around Photography'</title>");
//            Html htmlMeta = new Html("<meta name='description' content='Get the latest uploaded photos from our community of photographers.'>");
//            verticalLayout.add(htmlTitle, htmlMeta);
        }


        layoutRecControl = new HorizontalLayout();
        layoutRecControl.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);


    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSection) {

        this.strHeader = strHeader;

        VerticalLayout headerContainer = new VerticalLayout();
        if (isMobile) {
            headerContainer.addClassNames(
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Overflow.HIDDEN,// Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        } else {
            headerContainer.addClassNames(
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, //Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }
        headerContainer.addClassName("header-layout");


        VerticalLayout headerContainerMaster = new VerticalLayout();
        if (isMobile) {
            headerContainerMaster.addClassNames(
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE
                    //                  Background.CONTRAST_5
            );
        } else {
            headerContainerMaster.addClassNames(
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE
//                    Background.CONTRAST_5
            );
        }

        cmbCount = new Select<>();
        cmbCount.setLabel("Photos per page");
        cmbCount.setItems("20", "30", "40", "60", "80");
        cmbCount.setValue(strDefCountPerPage);

//        cmbCount.addValueChangeListener(event -> {
//            if (!event.getValue().equalsIgnoreCase(event.getOldValue())) {
//
//                recsHolder.removeAll();
//                filter(sqlOrderBy);
//            }
//        });

        cmbSortBy = new Select<>();
        cmbSortBy.setItems(arrOrderByItems);
        cmbSortBy.setValue(strDefOrderBy);

        cmbSortBy.setLabel("Sort Photos");
//        cmbSortBy.addValueChangeListener(event -> {
//            sqlOrderBy = event.getValue();
//            if (!event.getValue().equalsIgnoreCase(event.getOldValue())) {
//                recsHolder.removeAll();
//                filter(sqlOrderBy);
//            }
//        });


//        cmbCount = new Select<>();
//        cmbCount.setLabel("Count of Photos");
//        cmbCount.setItems("20", "40", "60", "80");
//        cmbCount.setValue("20");

//        sortBy = new Select<>();
//        sortBy.setLabel("Sort Photos"); // "Most Viewed", "Least Viewed",
//        sortBy.setItems("Newest First", "Oldest First", "Most Liked", "Least Liked");
//        sortBy.setValue("Newest First");

        H1 header = new H1(strHeader);

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.SMALL, TextColor.SECONDARY);

        HorizontalLayout layoutHeaderHorizontal = new HorizontalLayout();
        if (isMobile) {
            layoutHeaderHorizontal.addClassNames(
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.XSMALL
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,

            );
        } else {
            layoutHeaderHorizontal.addClassNames(
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.SMALL,
                    Gap.XSMALL
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,

            );
        }
        layoutHeaderHorizontal.addClassName("header-layout-panel");

        H3 headerSection = new H3(strSection);
        headerSection.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Margin.Top.MEDIUM,
                Padding.NONE
        );

        VerticalLayout layoutSortNCommands = new VerticalLayout();
        layoutSortNCommands.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.NONE, Padding.SMALL,
                FontSize.SMALL, TextColor.SECONDARY,
                TextAlignment.CENTER
        );
        layoutSortNCommands.addClassName("header-layout-sort");

        VerticalLayout layoutFiltersAll = new VerticalLayout();
        layoutFiltersAll.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.NONE, Padding.SMALL,
                FontSize.SMALL, TextColor.SECONDARY,
//                Background.CONTRAST_5,
                TextAlignment.CENTER
        );

        Div layoutFilters = new Div();

        checkboxCheckboxGroup = loadFiltersHeader(sqlReadDestination, arrDestinationNames, "city_name");
        layoutFilters.add(checkboxCheckboxGroup);
        layoutFilters.addClassNames(Width.FULL, Height.FULL);

        Div layoutFilterGenres = new Div();
        checkboxGenres = new CheckboxGroup<>();
        checkboxGenres.setVisible(false);
        // checkboxGenres = loadFiltersHeader(sqlLearningCategoriesRead, arrColLearningCategories, "cat_title");
//        layoutFilterGenres.add(checkboxGenres);
//        layoutFilterGenres.addClassNames(Width.FULL, Height.FULL);

        Div layoutGenres = new Div("mmm");
        layoutGenres.addClassNames(Width.FULL, Height.FULL);

        Span tab1Icon = new Span();
        tab1Icon.add(FontAwesome.Solid.MAP_LOCATION.create());

        Span tab1 = new Span("Locations");
        tab1.addClassNames(FontWeight.BOLD, Padding.MEDIUM);
        tab1Icon.add(tab1);
//        tab1.getStyle().setColor("#466ca8");
//        Span tab2 = new Span("Photo Genres");
//        tab2.addClassNames(FontWeight.BOLD);
//        Span tab3 = new Span("Time");
//        tab3.addClassNames(FontWeight.BOLD);

        TabSheet tabSheet = new TabSheet();
        tabSheet.add(tab1Icon,
                layoutFilters);
//        tabSheet.add(tab2,
//                layoutFilterGenres);

        tabSheet.addThemeVariants(TabSheetVariant.LUMO_BORDERED);
        tabSheet.addClassNames(Width.FULL, Height.FULL);

        Div divFiltersTitle = new Div("Filter by");
        layoutFiltersAll.add(divFiltersTitle, tabSheet);

        Button btnFilter = new Button("Filter");
        btnFilter.setIcon(VaadinIcon.SEARCH.create());
        btnFilter.addClassName("btn-filter");
        btnFilter.addClickListener(event -> {

            int intSelected = cmbSortBy.getItemPosition(cmbSortBy.getValue());
            sqlOrderBy = arrOrderByItemsSql[intSelected];

            filter(sqlOrderBy);
        });

        layoutSortNCommands.add(cmbCount, cmbSortBy, btnFilter);
        layoutHeaderHorizontal.add(layoutFiltersAll, layoutSortNCommands);

        headerContainerMaster.add(layoutHeaderHorizontal);

        headerContainer.add(header, subheader, headerContainerMaster);

        return headerContainer;
    }


//    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSection) {
//
//        this.strHeader = strHeader;
//
//        HorizontalLayout headerContainerMaster = new HorizontalLayout();
//        headerContainerMaster.setId("header-master");
//        if (isMobile) {
//            headerContainerMaster.addClassNames(
//                    AlignItems.CENTER, JustifyContent.START,
//                    Overflow.HIDDEN, Width.FULL,
//                    Margin.NONE,
//                    Padding.SMALL,
//                    Gap.XSMALL,
//                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
//                    //   Background.CONTRAST_5,
//                    BorderRadius.NONE
//            );
//        } else {
//            headerContainerMaster.addClassNames(
//                    AlignItems.CENTER, JustifyContent.START,
//                    Overflow.HIDDEN, Width.FULL,
//                    Margin.NONE,
//                    Padding.MEDIUM,
//                    Gap.SMALL,
//                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
//                    //   Background.CONTRAST_5,
//                    BorderRadius.LARGE
//            );
//        }
//
//
//        Div headerContainer = new Div();
//        if (isMobile) {
//            headerContainer.addClassNames(
//                    AlignItems.START, JustifyContent.CENTER,
//                    Overflow.HIDDEN, Width.FULL,
//                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.SMALL,
//                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
//                    //   Background.CONTRAST_5,
//                    BorderRadius.NONE
//            );
//        } else {
//            headerContainer.addClassNames(
//                    AlignItems.START, JustifyContent.CENTER,
//                    Overflow.HIDDEN, Width.FULL,
//                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.SMALL,
//                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
//                    //   Background.CONTRAST_5,
//                    BorderRadius.NONE
//            );
//        }
//
//        VerticalLayout layoutFiltersAll = new VerticalLayout();
//        layoutFiltersAll.addClassNames(
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Margin.NONE, Padding.MEDIUM,
//                LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
////                Background.CONTRAST_5,
//                TextAlignment.CENTER
//        );
//
//        Div layoutFilters = new Div();
//        if (isMobile) {
//            layoutFilters.addClassNames(
//                    Overflow.HIDDEN,
//                    AlignItems.CENTER, JustifyContent.CENTER,
//                    Margin.NONE,
//                    Padding.XSMALL,
//                    Gap.SMALL,
//                    Width.FULL,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    //  Background.CONTRAST_5,
//                    BorderRadius.NONE);
//        } else {
//            layoutFilters.addClassNames(
//                    Overflow.HIDDEN,
//                    AlignItems.CENTER, JustifyContent.AROUND,
//                    Margin.NONE,
//                    Padding.LARGE,
//                    Gap.SMALL,
//                    Width.FULL,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    //  Background.CONTRAST_5,
//                    BorderRadius.LARGE);
//        }
//        layoutFilters.addClassName("header-layout-filters");
//
//
//        Div divFiltersTitle = new Div("Filter by Location");
//        layoutFiltersAll.add(divFiltersTitle, layoutFilters);
//
//        List<Record> lstDestinationRecs = getRecordsFromDb(sqlReadDestination, arrDestinationNames);
//
//        ArrayList<String> lstDestinations = new ArrayList<>();
//        for (int r = 0; r < lstDestinationRecs.size(); r++) {
//            String strDestination = lstDestinationRecs.get(r).getColumnData("city_name");
//            if (strDestination == null || strDestination.trim().isEmpty() || strDestination.trim().equalsIgnoreCase("null")) {
//            } else {
//                lstDestinations.add(strDestination);
//            }
//
//        }
//
//        RouteParam routeMember = new RouteParam("member", strMember);
//
//        RouteParam routeDestinationAll = new RouteParam("destination", STR_ALL_DESTINATIONS);
////        RouteParameters routeParamsAll = new RouteParameters(routeDestinationAll, routeMember);
////        RouterLink linkPhotoDestinationAll = new RouterLink("All Locations", GalleryView.class, routeParamsAll);
////        layoutFilters.add(linkPhotoDestinationAll);
//
//        for (int c = 0; c < lstDestinations.size(); c++) {
//            String captionDestination = lstDestinations.get(c);
//            RouteParam routeParamDestination = new RouteParam("destination", captionDestination);
//
//            RouterLink linkPhotoCategory = new RouterLink(captionDestination, GalleryView.class, new RouteParameters(routeParamDestination, routeMember));
//            layoutFilters.add(linkPhotoCategory);
//        }
//
////        CheckboxGroup<String> checkboxGroupSubject = new CheckboxGroup<>();
////        checkboxGroupSubject.setTooltipText("Subject");
////        checkboxGroupSubject.setItems("Photography", "Street Photography", "Landscape", "Cityscape");
//        //   "Friday", "Saturday", "Sunday");
//        // checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
////        Div lblFilterSubject = new Div("Subject");
//
////        layoutFilters.add(checkboxGroupSubject);
//
////        CheckboxGroup<String> checkboxGroupFormat = new CheckboxGroup<>();
////        checkboxGroupFormat.setTooltipText("Format");
//////        checkboxGroupFormat.setLabel("Format");
////        checkboxGroupFormat.setItems("Book", "Youtube");
//////        Div lblFilterFormat = new Div("Format");
////        layoutFilters.add(checkboxGroupFormat);
//
////        VerticalLayout layoutHeaderParameters = new VerticalLayout();
////        if (isMobile) {
////            layoutHeaderParameters.addClassNames(
////                    AlignItems.CENTER, JustifyContent.EVENLY,
////                    Overflow.HIDDEN, Width.FULL,
////                    Margin.SMALL,
////                    Padding.NONE,
////                    Gap.XSMALL,
////                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
////                    //   Background.CONTRAST_5,
////                    BorderRadius.NONE
////            );
////        } else {
////            layoutHeaderParameters.addClassNames(
////                    AlignItems.CENTER, JustifyContent.EVENLY,
////                    Overflow.HIDDEN, Width.FULL,
////                    Margin.SMALL,
////                    Padding.NONE,
////                    Gap.XSMALL,
////                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
//////                       Background.CONTRAST_5,
////                    BorderRadius.LARGE
////            );
////        }
//
//        Select<String> cmbView = new Select<>();
//        cmbView.setLabel("View");
//        cmbView.setItems("Micro View", "Ordinary - No MetaData", "Ordinary - MetaData Bottom", "Ordinary - MetaData Right",
//                "Wide - No MetaData", "Wide - MetaData Bottom", "Wide - MetaData Right");
//        cmbView.setValue("Ordinary - No MetaData");
//
//
//        Tab tabFilterLocation = new Tab(VaadinIcon.LOCATION_ARROW_CIRCLE_O.create(), new Span("Location"));
//        Tab tabFilterKeyword = new Tab(VaadinIcon.KEYBOARD_O.create(), new Span("Keyword"));
//        Tab tabFilterUser = new Tab(VaadinIcon.USER.create(), new Span("User"));
//
//        // Set the icon on top
//        for (Tab tab : new Tab[]{tabFilterLocation, tabFilterKeyword, tabFilterUser}) {
//            tab.addThemeVariants(TabVariant.LUMO_ICON_ON_TOP);
////            tab.addClassNames(
//////                    Width.FULL,
////                    AlignItems.CENTER, JustifyContent.END,
////                    IconSize.LARGE, //FontSize.MEDIUM,
////                    TextColor.SECONDARY,
//////                    BorderColor.CONTRAST_20,
////                    Padding.MEDIUM, Margin.NONE,
////                    Gap.MEDIUM
////            );
////            FontSize.MEDIUM, TextColor.SECONDARY, IconSize.SMALL, //BorderRadius.LARGE,
////                    Width.FULL, Padding.XSMALL, Margin.NONE,
////                    BorderColor.CONTRAST_20, Border.ALL);
//        }
//
////        Tabs tabsFilterBased = new Tabs(tabFilterLocation, tabFilterKeyword, tabFilterUser);
//////        tabsViewInfo.addThemeVariants(  TabsVariant.LUMO_SMALL,
//////                TabsVariant.LUMO_EQUAL_WIDTH_TABS);
////        tabsFilterBased.addClassNames(
////                AlignItems.CENTER, JustifyContent.END,
////                Padding.LARGE, Margin.NONE,
//////                BorderRadius.LARGE,
////                Border.ALL,
////                BorderColor.CONTRAST_5
//////                Gap.XSMALL
////        );
////        tabsFilterBased.addClassName("header-view-type");
//
////        Tabs tabsViewInfo = new Tabs(tabFilterLocation, tabFilterKeyword, tabFilterUser);
//////        tabsViewInfo.addThemeVariants(  TabsVariant.LUMO_SMALL,
//////                TabsVariant.LUMO_EQUAL_WIDTH_TABS);
////        tabsViewInfo.addClassNames(
////
////                AlignItems.CENTER, JustifyContent.END,
////                Padding.LARGE, Margin.NONE,
//////                BorderRadius.LARGE,
////                Border.ALL,
////                BorderColor.CONTRAST_5
//////                Gap.XSMALL
////        );
////        tabsViewInfo.addClassName("header-view-type");
//
////        headerContainerMaster.add(headerTextContainer); //,tabsViewInfo);
////        layoutHeaderParameters.add(headerContainerMaster);
//
////        H3 divSection = new H3(strSection);
////        divSection.addClassNames(
////                AlignItems.CENTER, JustifyContent.CENTER,
////                Margin.Bottom.MEDIUM, Margin.Top.MEDIUM);
//
//        VerticalLayout layoutViewNOrder = new VerticalLayout();
//        layoutViewNOrder.addClassNames(
//                AlignItems.START, JustifyContent.AROUND,
//                Margin.NONE, Padding.MEDIUM,
//                Gap.MEDIUM
//        );
//        layoutViewNOrder.add(cmbCount, cmbSortBy);
//        layoutViewNOrder.setMaxWidth("200px");
//
////        headerContainerMaster.add(headerTextContainer);
//        headerContainer.add(layoutFiltersAll);
//        headerContainerMaster.add(headerContainer, layoutViewNOrder);
//        headerContainerMaster.setMaxWidth("1160px");
////        layoutHeaderParameters.add( headerContainerSecondary, divSection);
//
//        HeaderFilterTabs headerFilterTabs = new HeaderFilterTabs(recordService, isMobile);
//        VerticalLayout layoutHeaderParameters = headerFilterTabs.getHeader(strHeader, strSubHeader, strSection, headerContainerMaster);
//

    /// /        headerContainerMaster.add(headerTextContainer, cmbView);
    /// /        headerContainerSecondary.add(layoutFilters, sortBy);
//        // layoutHeaderParameters.add(headerContainerMaster);
//
//        return layoutHeaderParameters;
//    }
    private HorizontalLayout getFooterControls(String sqlWhereAnd, String sqlOrderBy) {

        // recsHolder.removeAll();
        layoutRecControl.removeAll();

        Div divInfo = new Div("Page ");
        Button btnPrevious = new Button("Previous");
        btnPrevious.setIcon(FontAwesome.Solid.ARROW_LEFT.create());
        Button btnNext = new Button("Next");
        btnNext.setIcon(FontAwesome.Solid.ARROW_RIGHT.create());
        btnNext.setIconAfterText(true);

        btnPrevious.addClickListener(event -> {
            int intResultsCount = 0;
            if (intPage > 1) {
                intPage--;
                divInfo.setText("Page " + intPage);

                intResultsCount = filter(null);

            }

            if (intPage <= 1) {
                event.getSource().setVisible(false);
            } else {
                event.getSource().setVisible(true);
            }

            if (intResultsCount > 0) {
                btnNext.setVisible(true);
            } else {
                btnNext.setVisible(false);
            }
        });

        btnNext.addClickListener(event -> {
            int intResultsCount = 0;
            if (intPage > 0) {
                intPage++;
                divInfo.setText("Page " + intPage);

                intResultsCount = filter(null);

            }

            if (intPage > 1) {
                btnPrevious.setVisible(true);
            } else {
                btnPrevious.setVisible(false);
            }

            if (intResultsCount > 0) {
                event.getSource().setVisible(true);
            } else {
                event.getSource().setVisible(false);
            }
        });

        divInfo.setText("Page " + intPage);
//        recsHolder.add(filterPage(sqlWhereAnd, null));
        if (intPage > 1) {
            btnPrevious.setVisible(true);
        } else {
            btnPrevious.setVisible(false);
        }

        layoutRecControl.add(btnPrevious, divInfo, btnNext);

        return layoutRecControl;
    }

    //   private Div filterPage(String sqlWhereAnd, String sqlOrderBy) {
//        intRecsOnPage = Integer.parseInt(cmbCount.getValue());
//
//        if (sqlWhereAnd == null) {
//            sqlWhereAnd = "";
//        }
//
//        if (sqlOrderBy == null) {
//            int intSelected = cmbSortBy.getItemPosition(cmbSortBy.getValue());
//            sqlOrderBy = arrOrderByItemsSql[intSelected];
//        }
//
//        String sqlReadPage;
//        if (intRecsOnPage == intPage * intRecsOnPage) {
//            sqlReadPage = sqlReadGallery + " " + sqlWhereAnd + " " + sqlOrderBy + " LIMIT " + intRecsOnPage + " ";
//        } else {
//            sqlReadPage = sqlReadGallery + " " + sqlWhereAnd + " " + sqlOrderBy + " LIMIT " + intRecsOnPage + " OFFSET " + (intPage * intRecsOnPage) + " ";
//        }
//        return loadImagesFromDb(sqlReadPage, arrColumnNamesGallery);
    //   }

    private CheckboxGroup<String> loadFiltersHeader(String sqlRead, String[] arrColumnNames, String columnName) {

        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);
        CheckboxGroup<String> chkGroup = new CheckboxGroup<>();
        chkGroup.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER);
        ArrayList<String> lstCategories = new ArrayList<>();
        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {

            String captionCategory = lstLearningCategoriesRecs.get(r).getColumnData(columnName);
            if (captionCategory != null && captionCategory.isEmpty()) {
            } else {
                lstCategories.add(captionCategory);
            }
        }
        chkGroup.setItems(lstCategories);

        return chkGroup;
    }

    private int filter(String sqlOrderBy) {
        int intResultsCount = 0;
        verticalLayout.removeAll();


        String strWhereSubClause = "";

        Set<String> setSelectedGenres = checkboxGenres.getSelectedItems();
        List<String> lstSelectedGenres = setSelectedGenres.stream().toList();

        Set<String> setSelected = checkboxCheckboxGroup.getSelectedItems();
        List<String> lstSelected = setSelected.stream().toList();

        if ((lstSelected == null || lstSelected.size() == 0) && (lstSelectedGenres == null || lstSelectedGenres.size() == 0)) {

        } else {
            strWhereSubClause = " AND ( ";
            for (int s = 0; s < lstSelected.size(); s++) {

                String strCategory = lstSelected.get(s); //  OR lc2.cat_type LIKE '" + strCategory + "')
                strWhereSubClause = strWhereSubClause + "  d.city_name LIKE '" + strCategory + "'  ";
                if (s < lstSelected.size() - 1) {
                    strWhereSubClause = strWhereSubClause + " OR ";
                }
            }

            if (lstSelectedGenres.size() > 0) {
                if (lstSelected.size() > 0) {
                    strWhereSubClause = strWhereSubClause + " OR ";
                }

                for (int s = 0; s < lstSelectedGenres.size(); s++) {
                    String strCategory = lstSelectedGenres.get(s); //  lc.cat_type LIKE '" + strCategory + "' OR
                    strWhereSubClause = strWhereSubClause + "   d.city_name LIKE '" + strCategory + "' ";
                    if (s < lstSelectedGenres.size() - 1) {
                        strWhereSubClause = strWhereSubClause + " OR ";
                    }
                }
            }
            strWhereSubClause = strWhereSubClause + " ) ";
        }

        intRecsOnPage = Integer.parseInt(cmbCount.getValue());

        if (sqlWhereAnd == null) {
            sqlWhereAnd = "";
        }

        if (sqlOrderBy == null) {
            int intSelected = cmbSortBy.getItemPosition(cmbSortBy.getValue());
            sqlOrderBy = arrOrderByItemsSql[intSelected];
        }

        String sqlReadPage;
        if (intRecsOnPage == intPage * intRecsOnPage) {
            sqlReadPage = sqlReadGallery + " " + strWhereSubClause + " " + sqlOrderBy + " LIMIT " + intRecsOnPage + " ";
        } else {
            sqlReadPage = sqlReadGallery + " " + strWhereSubClause + " " + sqlOrderBy + " LIMIT " + intRecsOnPage + " OFFSET " + (intPage * intRecsOnPage) + " ";
        }

        List<Record> lstRecords = cacheService.getAllPhotos(sqlReadPage, arrColumnNamesGallery, "id"); //getRecordsFromDb(sqlRead, arrColumnsLearning);
        intResultsCount = lstRecords.size();
        logger.info(" record size: " + lstRecords.size());

        boolean isEditable = false;
        Div divGallery = new Div();
        divGallery.addClassName("gallery");
        for (int r = 0; r < lstRecords.size(); r++) {
            Record rec = lstRecords.get(r);
            String strId = rec.getColumnData("id");

            Record record = cacheService.getPhotoById(strId);

            divGallery.add(getImageGalleryThumbsFromDb(record, isEditable));
        }


        verticalLayout.add(divGallery);

        //  verticalLayout.add(getFooterControls("", ""));
        return intResultsCount;
    }


//    private Div getImagesItemsFromDb(String sqlRead, String[] arrColumnNames) {
//
//
//        boolean isEditable = false;
//
//        Div divGallery = new Div();
//        divGallery.addClassName("gallery");
//
//        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
//        for (int r = 0; r < lstRecords.size(); r++) {
//
//            Record rec = lstRecords.get(r);
//            divGallery.add(getImageGalleryThumbsFromDb(rec, isEditable));
//        }
//        return divGallery;
//    }

    private GalleryImageViewCard getImageGalleryThumbsFromDb(Record record, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar + subPathMedium;

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

        GalleryImageViewCard imageGalleryViewCard = new GalleryImageViewCard(record, strImagePath, isMobile, userId, strUsername, sessionCreation, hostname, publicIp, isEditable,
                recordService, sqlReadGallery, sqlOrderBy, arrColumnNamesGallery);
        return imageGalleryViewCard;
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
