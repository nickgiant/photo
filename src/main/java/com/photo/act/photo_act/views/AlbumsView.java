package com.photo.act.photo_act.views;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.AlbumViewCard;
import com.photo.act.photo_act.views.components.GalleryImageViewCard;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.HeaderFilterTabs;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Html;
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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.MainLayout.*;
import static com.photo.act.photo_act.views.MeView.subPathSmall;

@AnonymousAllowed
//@PageTitle("Image Gallery")
@Route(value = "albums") //":category?")
//@RouteAlias(value = "albums/title/:title?", layout = MainLayout.class)
//@RouteAlias(value = "albums/member/:member?", layout = MainLayout.class)
@RouteAlias(value = "albums/member/:member?/:title?", layout = MainLayout.class)

//@RouteAlias(value = "gallery/location/:destination?", layout = MainLayout.class)

//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class AlbumsView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";

    private static final Logger logger = LoggerFactory.getLogger(AlbumsView.class);

    private VerticalLayout verticalLayout;
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String section = SECTION_ALBUMS;
    private String strMember;
    private String strAlbumTitle;
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
    private String strOS;
    private String strBrowser;

    private int userId;
    private String strUsername;

    private ArrayList<String> lstAlbums;

    private String strColorExternalweb = "#9fafd5";


    String[] arrColumnsMemberAlbums = {"album_count"
            , "username", "name", "surname", "resident", "date_joined", "member_since", "avatar_path"
    };

    String sqlMemberOfAlbums = "SELECT usr.username, usr.name, usr.surname, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined " +
            " , DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
            " , usr.avatar_path " +
            " , count(usr.userId) AS album_count " +
            " FROM dbuser usr LEFT JOIN photo_album a ON a.user_id = usr.userId " +
            " WHERE  1 = 1 " +
            " AND a.album_visible_to = 'ALL' ";

    String sqlMemberOfAlbumsGroupBy =
            " GROUP BY usr.userId " +
                    " ORDER BY usr.username ASC, a.date_inserted DESC ";

    String[] arrColumnsAlbums = {"title", "description", "album_visible_to", "user_id", "date_inserted", "album_photo_count", "album_photo_size",
            "name_new", "photo_1", "photo_2", "datetime_album_created"
            , "username", "surname", "name", "resident", "date_joined", "avatar_path"
    };

    String sqlAlbumsAll = "SELECT a.title, a.`description`, a.album_visible_to, a.user_id, a.date_inserted " +
            " , count(pa.photo_album_id) AS album_photo_count, SUM(pm.space_size) AS album_photo_size " +
            " , pm.name_new , a.photo_id1, p1.name_new AS photo_1 ,  a.photo_id2, p2.name_new  AS photo_2 " +
            " , getDateDiffFromNow(a.date_inserted) AS datetime_album_created " +
            " , usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
            //     "--  , pa.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
            " FROM photo_album_photo pa , photo_meta pm, dbuser usr, photo_album a LEFT JOIN photo_meta p1 ON a.photo_id1 = p1.id " +
            " LEFT JOIN photo_meta p2 ON a.photo_id2 = p2.id " +
            " WHERE a.id = pa.photo_album_id AND pa.photo_id = pm.id AND a.user_id = usr.userId " +
            " AND a.album_visible_to = 'ALL' AND pm.visible_to  = 'ALL' ";

    String sqlAlbumsGroupBy =
            " GROUP BY pa.photo_album_id " +
                    " ORDER BY a.date_inserted DESC, a.title ASC ";

    private String[] arrColumnNamesAlbumPhotos = {"album_title", "album_visible_to", "description"
            , "name_new", "title", "subtitle", "photo_type", "uploader", "creator", "visible_to", "city_name", "meta_date", "photo_date", "photo_time"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso", "meta_aperture", "meta_shutter_speed"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon"
            , "date_inserted"
            , "username", "surname", "name", "resident", "date_joined", "avatar_path"
    };

    private String sqlReadAlbumPhotos = "SELECT a.title AS album_title, a.user_id, a. album_visible_to, a.description, " +
            " pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.creator, pm.visible_to, d.city_name, d.country, " +
            " DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, getDateDiffFromNow(pm.meta_date) AS photo_date, DATE_FORMAT(pm.meta_date, '%H:%i %p') AS photo_time, " +
            " pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model, " +
            " pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed " +
            " , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
            " , usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
            //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
            " FROM dbuser usr, photo_album a , photo_album_photo pa , photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id " +
            " WHERE  pm.uploaderId = usr.userId AND a.id = pa.photo_album_id AND pa.photo_id = pm.id ";

    private UtilsDate utilsDate;
    private String sessionDateTime;
    private GenericView genericView;

    public AlbumsView(RecordService recordService) {
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
        strAlbumTitle = event.getRouteParameters().get("title").orElse(STR_ALL_ALBUMS);

        getUserClientInfo();


        if (strMember.equalsIgnoreCase("visitor-user")) {
            userId = 1;
            strUsername = "visitor-user";
        }

        verticalLayout.removeAll();
        if (strAlbumTitle == null || strAlbumTitle.isEmpty()) {
            verticalLayout.add(loadHeader("Albums", "", strAlbumTitle));
            logger.error(" empty strAlbumTitle:" + strAlbumTitle);
        } else if (strAlbumTitle.equalsIgnoreCase(STR_ALL_ALBUMS)) {
            verticalLayout.add(loadHeader("Albums", "", ""));
            if (strMember == null || strMember.isEmpty() || strMember.equalsIgnoreCase(STR_ALL_MEMBERS)) {
                String sqlAlbums = sqlAlbumsAll + sqlAlbumsGroupBy;
                loadAlbumsFromDb(sqlAlbums, arrColumnsAlbums, false);
            } else {
                String sqlMember = sqlMemberOfAlbums + " AND usr.username = '" + strMember + "' " + sqlMemberOfAlbumsGroupBy;
                loadMemberOfAlbumsFromDb(sqlMember, arrColumnsMemberAlbums, false);

                String sqlAlbums = sqlAlbumsAll + " AND usr.username = '" + strMember + "' " + sqlAlbumsGroupBy;
                loadAlbumsFromDb(sqlAlbums, arrColumnsAlbums, false);
            }

        } else if (!strAlbumTitle.equalsIgnoreCase(STR_ALL_ALBUMS)) {

            verticalLayout.add(loadHeader("Albums", "", ""));
            if (strMember == null || strMember.isEmpty() || strMember.equalsIgnoreCase(STR_ALL_MEMBERS)) {
                H3 titleAlbum = new H3(strAlbumTitle);
                titleAlbum.addClassNames(Width.FULL, TextAlignment.CENTER);
                verticalLayout.add(titleAlbum);

                String sqlAlbumsPhotoAll = sqlReadAlbumPhotos + " AND pm.visible_to = 'ALL' ";
                sqlAlbumsPhotoAll = sqlAlbumsPhotoAll + " AND a.title LIKE '" + strAlbumTitle + "' ";
                sqlAlbumsPhotoAll = sqlAlbumsPhotoAll + " ORDER BY pa.inc, pa.date_inserted ASC ";
                verticalLayout.add(loadAlbumImagesFromDb(sqlAlbumsPhotoAll, arrColumnNamesAlbumPhotos, false));
            } else {
                String sqlMember = sqlMemberOfAlbums + " AND usr.username = '" + strMember + "' " + sqlMemberOfAlbumsGroupBy;
                loadMemberOfAlbumsFromDb(sqlMember, arrColumnsMemberAlbums, false);

                H3 titleAlbum = new H3(strAlbumTitle);
                titleAlbum.addClassNames(Width.FULL, TextAlignment.CENTER);
                verticalLayout.add(titleAlbum);

                String sqlAlbumsPhotoAll = sqlReadAlbumPhotos + " AND pm.visible_to = 'ALL' ";
                sqlAlbumsPhotoAll = sqlAlbumsPhotoAll + " AND a.title LIKE '" + strAlbumTitle + "' ";
                sqlAlbumsPhotoAll = sqlAlbumsPhotoAll + " ORDER BY pa.inc, pa.date_inserted ASC ";
                verticalLayout.add(loadAlbumImagesFromDb(sqlAlbumsPhotoAll, arrColumnNamesAlbumPhotos, false));
            }

        } else {
            verticalLayout.add(loadHeader("Albums", "else", ""));

            String sqlAlbumsPhotoAll = sqlReadAlbumPhotos + " AND pm.visible_to = 'ALL' ";
            //sqlAlbumsAll = sqlAlbumsAll + " AND a.title LIKE '" + strAlbumTitle + "' ";

            sqlAlbumsPhotoAll = sqlAlbumsPhotoAll + " ORDER BY pa.inc, pa.date_inserted ASC ";
            verticalLayout.add(loadAlbumImagesFromDb(sqlAlbumsPhotoAll, arrColumnNamesAlbumPhotos, false));
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
        addClassNames(Overflow.HIDDEN, Width.FULL,
                // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                Margin.NONE,
                Padding.NONE,
                Gap.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER
                //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
        );
        addClassName("image-gallery-view");

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


        lstAlbums = new ArrayList<>();

        verticalLayout = new VerticalLayout();
        if (isMobile) {
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
                    AlignItems.STRETCH, JustifyContent.CENTER
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
                    Gap.LARGE,
                    //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                    //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
                    AlignItems.STRETCH, JustifyContent.CENTER
            );
//            verticalLayout.getStyle().set("gap", "3rem");
        }

        Html htmlTitle = new Html("<title>'photoact.net Network and Act around Photography'</title>");
        Html htmlMeta = new Html("<meta name='description' content='Get the latest uploaded photos, organized to albums, from our community of photographers.'>");
        verticalLayout.add(htmlTitle, htmlMeta);

        this.setWidthFull();

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

        Div headerContainerSecondary = new Div();
        if (isMobile) {
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
        } else {
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
        if (isMobile) {
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
        } else {
            layoutFilters.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }
        layoutFilters.addClassName("header-layout-filters");

//        RouteParam routeMember = new RouteParam("member", strMember);
//
//        RouteParam routeAlbumAll = new RouteParam("title", STR_ALL_ALBUMS);
//        RouteParameters routeParamsAll = new RouteParameters(routeAlbumAll, routeMember);
//        RouterLink linkPhotoAlbumAll = new RouterLink("All Albums", AlbumsView.class, routeParamsAll);
//        layoutFilters.add(linkPhotoAlbumAll);


        List<Record> recAlbums = getRecordsFromDb(sqlAlbumsAll, arrColumnsAlbums);

        lstAlbums.clear();
        for (int r = 0; r < recAlbums.size(); r++) {
            lstAlbums.add(recAlbums.get(r).getColumnData("title"));
        }
//
//        for (int c = 0; c < lstAlbums.size(); c++) {
//            String captionAlbum = lstAlbums.get(c);
//            RouteParam routeParamAlbum = new RouteParam("title", captionAlbum);
//
//            RouterLink linkPhotoAlbum = new RouterLink(captionAlbum, AlbumsView.class, new RouteParameters(routeParamAlbum, routeMember));
//            layoutFilters.add(linkPhotoAlbum);
//        }

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
        headerContainerSecondary.add(layoutFilters);
//        layoutHeaderParameters.add( headerContainerSecondary, divSection);

        HeaderFilterTabs headerFilterTabs = new HeaderFilterTabs(recordService, isMobile);
        VerticalLayout layoutHeaderParameters = headerFilterTabs.getHeader(strHeader, strSubHeader, strSection, headerContainerSecondary);

//        headerContainerMaster.add(headerTextContainer, cmbView);
//        headerContainerSecondary.add(layoutFilters, sortBy);
//        layoutHeaderParameters.add(headerContainerMaster,headerContainerSecondary);

        return layoutHeaderParameters;
    }

    private void loadMemberOfAlbumsFromDb(String sqlRead, String[] arrColumnNames, boolean isEditable) {


        VerticalLayout layoutMember = new VerticalLayout();
        layoutMember.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextColor.TERTIARY,
                Padding.LARGE,
                Gap.SMALL
        );

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);

        if (lstRecords == null || lstRecords.size() == 0) {

        } else if (lstRecords.size() == 1) {
            Record rec = lstRecords.get(0);
            String strUsername = rec.getColumnData("username");
            String strName = rec.getColumnData("name");
            String strSurname = rec.getColumnData("surname");
            String strCountOfAlbums = rec.getColumnData("album_count");
//            String strCountOfPhotosOfAlbums = rec.getColumnData("album_photo_count");
            String strMemberSince = rec.getColumnData("member_since");
            String strAvatarPath = rec.getColumnData("avatar_path");


            Image imgAvatar = genericView.getAvatarImage(strAvatarPath, strName + " " + strSurname, "120px", "120px");

            H2 objMemberName = new H2(strName + " " + strSurname);
            H3 objMember = new H3(strUsername);
            Div divMemberSince = new Div("Member since " + strMemberSince);
            Div divAlbumsAndPhotos = new Div("Has " + strCountOfAlbums + " albums");
            layoutMember.add(imgAvatar, objMemberName, objMember, divMemberSince, divAlbumsAndPhotos);
        } else {

        }

        verticalLayout.add(layoutMember);
    }

    private void loadAlbumsFromDb(String sqlRead, String[] arrColumnNames, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar;

        Div divGallery = new Div();

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {
            Record rec = lstRecords.get(r);
            divGallery.add(getAlbumsFromDb(rec, isEditable));
        }
        verticalLayout.add(divGallery);
    }

    private AlbumViewCard getAlbumsFromDb(Record record, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar + subPathSmall;

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");

        String strCityName = record.getColumnData("city_name");
        String strUploader = record.getColumnData("uploader");

        String strAlbumTitle = record.getColumnData("album_title");

//        RouteParam routeUploaderAll = new RouteParam("member", STR_ALL_MEMBERS);

//        RouteParam routeAlbum = new RouteParam("title", strAlbumTitle);
//        RouteParam routeUploader = new RouteParam("member", strUploader);
//        //RouterLink linkUploader = new RouterLink(strUploader, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));
//        RouterLink linkAlbum = new RouterLink(strAlbumTitle, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));

        String strImagePath = strPath + dirChar; // + strFileName;
        logger.info(" strImagePath " + strImagePath);

        AlbumViewCard albumViewCard = new AlbumViewCard(record, strImagePath, isMobile, userId, strUsername, sessionCreation, hostname, publicIp, isEditable,
                recordService);
        return albumViewCard;

    }

    private Div loadAlbumImagesFromDb(String sqlRead, String[] arrColumnNames, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar;

        Div divGallery = new Div();
        divGallery.addClassName("gallery");

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            Record rec = lstRecords.get(r);
            divGallery.add(getAlbumImagesFromDb(rec, isEditable));
        }

        return divGallery;
    }

    private GalleryImageViewCard getAlbumImagesFromDb(Record record, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar + subPathSmall;

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");

        String strCityName = record.getColumnData("city_name");
        String strUploader = record.getColumnData("uploader");

        String strAlbumTitle = record.getColumnData("album_title");

//        RouteParam routeUploaderAll = new RouteParam("member", STR_ALL_MEMBERS);

//        RouteParam routeAlbum = new RouteParam("title", strAlbumTitle);
//        RouteParam routeUploader = new RouteParam("member", strUploader);
//        RouterLink linkUploader = new RouterLink(strUploader, AlbumsView.class, new RouteParameters(routeUploader, routeAlbum));
//        RouterLink linkAlbum = new RouterLink(strAlbumTitle, AlbumsView.class, new RouteParameters(routeUploader, routeAlbum));

        String strImagePath = strPath + dirChar + strFileName;
        logger.info(" strImagePath " + strImagePath);

        GalleryImageViewCard imageGalleryViewCard = new GalleryImageViewCard(record, strImagePath, isMobile, userId, strUsername, sessionCreation, hostname, publicIp, isEditable,
                recordService, sqlReadAlbumPhotos, arrColumnNamesAlbumPhotos);
        return imageGalleryViewCard;

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


    private void logErrorInDb(Exception e, String function, String hostname, int userId, String strUsername, String publicIp, long sessionCreation, String info) {
        recordService.logErrorInDb(e, hostname, function, userId, strUsername, publicIp, Long.toString(sessionCreation), info);
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
