package com.photo.act.photo_act.views;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.dto.PhotoAlbumCategoryDto;
import com.photo.act.photo_act.dto.PhotoAlbumDto;
import com.photo.act.photo_act.services.PhotoAlbumService;
import com.photo.act.photo_act.services.PhotoRatingService;
import com.photo.act.photo_act.services.PhotoViewService;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.*;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

import static com.photo.act.photo_act.views.HomeView.subPathSmall;
import static com.photo.act.photo_act.views.MainLayout.*;


@AnonymousAllowed
//@PageTitle("Image Gallery")
@Route(value = "albums") //":category?")
@RouteAlias(value = "albums/category/:category?", layout = MainLayout.class)
@RouteAlias(value = "albums/member/:member?/title/:title?", layout = MainLayout.class)


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
    String[] arrColumnsAlbumTypes = {"cat_title", "cat_type", "cat_title_count", "cat_description_min", "cat_description_big"};

    private String strAlbumTitle;
    private RecordService recordService;
    private ShareService shareService;
    private ShareMetricService shareMetricService;
    private WeatherService weatherService;
    private PhotoRatingService photoRatingService;
    private PhotoViewService photoViewService;
    private PhotoAlbumService photoAlbumService;
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
    String sqlAlbumTypes =
            " SELECT a.id, ac.id, ac.cat_title, ac.cat_type, ac.cat_description_min, ac.cat_description_big, " +
                    " count(ac.cat_title) AS cat_title_count, ac.cat_order " +
                    " FROM photo_album a LEFT JOIN photo_album_categories ac ON a.category_id = ac.id " +
                    " WHERE 1 = 1 " +
                    " GROUP BY ac.cat_title " +
                    " ORDER BY ac.cat_title ASC";
    String[] arrColumnsAlbumsCategories = {"id", "cat_title", "cat_type", "cat_description_min", "cat_type_description_min", "cat_description_big", "date_inserted", "cat_count",
            "photo_1", "meta_orientation1", "meta_i_length1", "meta_i_width1", "meta_i_height1"
            , "photo_2", "photo_3", "photo_4", "datetime_album_created"
            , "username", "surname", "name", "resident", "date_joined", "member_since", "avatar_path"
    };


    String[] arrColumnsMemberAlbums = {"id", "title", "description", "album_visible_to", "category_id"
            , "username", "name", "surname", "resident", "date_joined", "member_since", "avatar_path"
            , "album_count"
            , "count_photos", "count_albums"
    };

    String sqlMemberOfAlbums = "SELECT a.id, a.title, a.description, a.album_visible_to, a.category_id " +
            " , usr.username, usr.name, usr.surname, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined " +
            " , DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
            " , usr.avatar_path " +
            " , count(usr.userId) AS album_count " +
            " , ux.count_photos, ux.count_albums " +
            " FROM dbuser usr,  dbuser_extra ux, photo_album a " +
            " WHERE a.user_id = usr.userId " +
            " AND usr.userId = ux.user_id " +
            " AND a.album_visible_to = 'ALL' ";

    String sqlMemberOfAlbumsGroupBy =
            " GROUP BY usr.userId " +
                    " ORDER BY usr.username ASC, a.date_inserted DESC ";
    String sqlAlbumsCategoriesAll = "SELECT ac.id, ac.cat_title, ac.cat_type, ac.cat_description_min, ac.cat_type_description_min, ac.cat_description_big " +
            " , count(ac.cat_type) AS cat_count " + //SUM(pm.space_size) AS album_photo_size " +
            " , a.photo_id1, p1.name_new AS photo_1 , p1.meta_orientation AS meta_orientation1 , p1.meta_i_length AS meta_i_length1 , p1.meta_i_width AS meta_i_width1 , p1.meta_i_height AS meta_i_height1 " +
            " , a.photo_id2, p2.name_new  AS photo_2 " +
            " , a.photo_id3, p3.name_new AS photo_3 ,  a.photo_id4, p4.name_new  AS photo_4 " +
            " , getDateDiffFromNow(a.date_inserted) AS datetime_album_created " +
            " , usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since, usr.avatar_path " +
            " FROM photo_album_categories ac, dbuser usr, photo_album a LEFT JOIN photo_meta p1 ON a.photo_id1 = p1.id " +
            " LEFT JOIN photo_meta p2 ON a.photo_id2 = p2.id  LEFT JOIN photo_meta p3 ON a.photo_id3 = p3.id " +
            " LEFT JOIN photo_meta p4 ON a.photo_id4 = p4.id " +
            " WHERE a.user_id = usr.userId " +
            " AND ac.id = a.category_id " +
            " AND a.album_visible_to = 'ALL' ";
    String sqlAlbumsCategoriesGroupBy =
            " GROUP BY ac.cat_type " +
                    " ORDER BY ac.cat_order ASC, a.date_inserted DESC ";
    String[] arrColumnsAlbums = {"id", "album_title", "description", "album_visible_to", "user_id", "date_inserted", "album_photo_count", "album_photo_size",
            "name_new", "photo_1", "meta_orientation1", "meta_i_length1", "meta_i_width1", "meta_i_height1"
            , "photo_2", "photo_3", "photo_4", "datetime_album_created"
            , "cat_type"
            , "username", "surname", "name", "resident", "date_joined", "member_since", "avatar_path"
            , "count_photos", "count_albums"
    };
    String sqlAlbumsAll = "SELECT a.id, a.title AS album_title, a.description, a.album_visible_to, a.user_id, a.date_inserted " +
            " , count(pap.photo_album_id) AS album_photo_count, SUM(pm.space_size) AS album_photo_size " +
            " , pm.name_new , a.photo_id1, p1.name_new AS photo_1 , p1.meta_orientation AS meta_orientation1,  p1.meta_i_length AS meta_i_length1 , p1.meta_i_width AS meta_i_width1 , p1.meta_i_height AS meta_i_height1 " +
            " , a.photo_id2, p2.name_new  AS photo_2 " +
            " , a.photo_id3, p3.name_new AS photo_3 ,  a.photo_id4, p4.name_new  AS photo_4 " +
            " , getDateDiffFromNow(a.date_inserted) AS datetime_album_created " +
            " , ac.cat_type " +
            " , usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since, usr.avatar_path " +
            " , ux.count_photos, ux.count_albums " +
            //     "--  , pap.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
            " FROM photo_album_photo pap , photo_meta pm, photo_album_categories ac,  dbuser_extra ux, dbuser usr, photo_album a LEFT JOIN photo_meta p1 ON a.photo_id1 = p1.id " +
            " LEFT JOIN photo_meta p2 ON a.photo_id2 = p2.id  LEFT JOIN photo_meta p3 ON a.photo_id3 = p3.id " +
            "  LEFT JOIN photo_meta p4 ON a.photo_id4 = p4.id " +
            " WHERE a.id = pap.photo_album_id AND pap.photo_id = pm.id AND a.user_id = usr.userId AND a.user_id = pap.user_id " +
            " AND ac.id = a.category_id " +
            " AND usr.userId = ux.user_id " +
            " AND a.album_visible_to = 'ALL' AND pm.visible_to  = 'ALL' ";
    private String strCategory;

    String sqlAlbumsGroupBy =
            " GROUP BY concat( usr.userId, pap.photo_album_id )  ";
    String sqlAlbumsOrderBy = " ORDER BY a.date_inserted DESC, a.title ASC ";

    private String[] arrColumnNamesAlbumPhotos = {"album_title", "album_visible_to", "description"
            , "id", "name_new", "title", "subtitle", "photo_type", "uploader", "creator", "visible_to", "album_destination_name_map", "album_destination_country_map", "city_name", "country"
            , "meta_date", "photo_date", "photo_time_shot", "date_inserted_diff_from_now"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso", "meta_aperture", "meta_shutter_speed", "meta_orientation"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon"
            , "date_inserted"
            , "username", "surname", "name", "resident", "date_joined", "member_since", "avatar_path"
            , "count_photos", "count_albums"
    };

    private String sqlReadAlbumPhotos = "SELECT a.title AS album_title, a.user_id, a. album_visible_to, a.description, " +
            " pm.id, pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.creator, pm.visible_to, " +
            " d.city_name , d.country,  " +
            " if(da.name_for_map IS NULL, da.city_name, da.name_for_map ) AS album_destination_name_map, " +
            " da.country AS album_destination_country_map, " +
            " DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, DATE_FORMAT(pm.meta_date, '%M %Y') AS photo_date, DATE_FORMAT(pm.meta_date, '%d/%m/%Y - %H:%i:%S') AS photo_time_shot, " +
            " getDateDiffFromNow(pm.date_inserted) AS date_inserted_diff_from_now " +
            " , pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model " +
            " , pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed, meta_orientation " +
            " , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
            " , usr.username, usr.surname, usr.name, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since, usr.avatar_path " +
            " , ux.count_photos, ux.count_albums " +
            //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
            " FROM dbuser usr, dbuser_extra ux, photo_album a LEFT JOIN destination da  ON (da.id = a.destination_id) , photo_album_photo pap , photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id " +
            " WHERE  a.user_id = usr.userId AND a.user_id = pap.user_id AND a.id = pap.photo_album_id AND pap.photo_id = pm.id " +
            " AND usr.userId = ux.user_id " +
            " AND pm.visible_to = 'ALL' ";
    // String sqlAlbumsGroupBy =
    //         " GROUP BY concat( usr.userId, pa.photo_album_id )  ";

    private String sqlReadAlbumPhotosOrderBy = " ORDER BY a.date_inserted DESC ";

    private UtilsDate utilsDate;
    private String sessionDateTime;
    private GenericView genericView;

    public AlbumsView(RecordService recordService, ShareService shareService,
                      ShareMetricService shareMetricService, WeatherService weatherService,
                      PhotoRatingService photoRatingService, PhotoViewService photoViewService,
                      PhotoAlbumService photoAlbumService) {
        this.recordService      = recordService;
        this.shareService       = shareService;
        this.shareMetricService = shareMetricService;
        this.weatherService     = weatherService;
        this.photoRatingService = photoRatingService;
        this.photoViewService   = photoViewService;
        this.photoAlbumService  = photoAlbumService;
        utilsDate    = new UtilsDate();
        genericView  = new GenericView(recordService);

        constructUI();
    }

    /** Returns album categories via JPA service (used by UI category strip). */
    public java.util.List<PhotoAlbumCategoryDto> getAlbumCategories() {
        return photoAlbumService.getAllCategories();
    }

    /** Returns paginated public albums via JPA service. */
    public org.springframework.data.domain.Page<PhotoAlbumDto> getPublicAlbums(int page, int size) {
        return photoAlbumService.getPublicAlbums(page, size);
    }

    @Override
    public String getPageTitle() {
        return strHeader;
    }

    @Override
    public void beforeEnter(@OptionalParameter BeforeEnterEvent event) {
        strMember = event.getRouteParameters().get("member").orElse(STR_ALL_MEMBERS);
        strAlbumTitle = event.getRouteParameters().get("title").orElse(STR_ALL_ALBUMS);
        strCategory = event.getRouteParameters().get("category").orElse(STR_ALL_CATEGORIES);

        getUserClientInfo();


        VerticalLayout layoutHeaderParameters;
        verticalLayout.removeAll();

        logger.warn("strCategory: " + strCategory + " strMember: " + strMember + " strAlbumTitle: " + strAlbumTitle);

        if (strAlbumTitle == null || strAlbumTitle.isEmpty() || strAlbumTitle.equalsIgnoreCase(STR_ALL_ALBUMS)) {

            if (!strCategory.isEmpty() && !strCategory.equalsIgnoreCase(STR_ALL_CATEGORIES)) {
                layoutHeaderParameters = loadHeader("Albums", "Collections of photos from members", "Category", strCategory);

                String sqlAlbums = sqlAlbumsAll + " AND ac.cat_type = '" + strCategory + "' " + sqlAlbumsGroupBy + sqlAlbumsOrderBy;
                loadAlbums(sqlAlbums, arrColumnsAlbums, 1);
            } else if (strMember == null || strMember.isEmpty() || strMember.equalsIgnoreCase(STR_ALL_MEMBERS) || strCategory.equalsIgnoreCase(STR_ALL_CATEGORIES)) {
                layoutHeaderParameters = loadHeader("Albums", "Collections of photos from members", "", "");
                String sqlAlbums = sqlAlbumsAll + sqlAlbumsGroupBy + sqlAlbumsOrderBy;
                loadAlbums(sqlAlbums, arrColumnsAlbums, 1);
            } else {
                layoutHeaderParameters = loadHeader("Albums", "Collections of photos from members", "", "");
                String sqlMember = sqlMemberOfAlbums + " AND usr.username = '" + strMember + "' " + sqlMemberOfAlbumsGroupBy;
                loadMemberOfAlbumsFromDb(sqlMember, arrColumnsMemberAlbums, false);

                String sqlAlbums = sqlAlbumsAll + " AND usr.username = '" + strMember + "' " + sqlAlbumsGroupBy + sqlAlbumsOrderBy;
                loadAlbums(sqlAlbums, arrColumnsAlbums, 1);
            }
        } else if (!strAlbumTitle.equalsIgnoreCase(STR_ALL_ALBUMS) && !strMember.equalsIgnoreCase(STR_ALL_MEMBERS)) {

            layoutHeaderParameters = loadHeader("Albums", "Collections of photos from members", "Title", strAlbumTitle);
//            logger.info("if2 strCategory: " + strCategory + " strMember: " + strMember + " strAlbumTitle: " + strAlbumTitle);
            String sqlMember = sqlMemberOfAlbums + " AND usr.username = '" + strMember + "' " + sqlMemberOfAlbumsGroupBy;
            loadMemberOfAlbumsFromDb(sqlMember, arrColumnsMemberAlbums, false);

            String sqlAlbumsPhotoAll = sqlReadAlbumPhotos + " AND a.title = '" + strAlbumTitle + "' AND usr.username = '" + strMember + "' "
                    + sqlReadAlbumPhotosOrderBy;
            logger.info("sqlAlbumsPhotoAll    " + sqlAlbumsPhotoAll);
            loadAlbumImagesFromDb(sqlAlbumsPhotoAll, arrColumnNamesAlbumPhotos, false, VIEW_PHOTO_GRID);

        } else if (!strCategory.isEmpty() && !strCategory.equalsIgnoreCase(STR_ALL_CATEGORIES)) {
//            logger.info("if3 strCategory: " + strCategory + " strMember: " + strMember + " strAlbumTitle: " + strAlbumTitle);
            layoutHeaderParameters = loadHeader("Albums", "Collections of photos from members", "Category", strCategory);

            String sqlAlbums = sqlAlbumsAll + " AND ac.cat_type = '" + strCategory + "' " + sqlAlbumsGroupBy + sqlAlbumsOrderBy;
            loadAlbums(sqlAlbums, arrColumnsAlbums, 1);

        } else {
            layoutHeaderParameters = loadHeader("Albums", "Collections of photos from members", "", "");

            logger.info("else strMember:" + strMember + " strAlbumTitle:" + strAlbumTitle);

            String sqlAlbumsPhotoAll = sqlReadAlbumPhotos; // + " AND pm.visible_to = 'ALL' ";


            sqlAlbumsPhotoAll = sqlAlbumsPhotoAll + " ORDER BY pap.inc, pap.date_inserted ASC ";
            loadAlbumImagesFromDb(sqlAlbumsPhotoAll, arrColumnNamesAlbumPhotos, false, VIEW_PHOTO_GRID);
        }

        this.removeAll();
        this.add(layoutHeaderParameters);

        if (isMobile) {
            VerticalLayout layoutMobileContent = new VerticalLayout();
            layoutMobileContent.addClassNames(Width.FULL,
                    AlignItems.START, JustifyContent.CENTER,
                    Padding.XSMALL, Margin.NONE,
                    Gap.XSMALL
            );

            layoutMobileContent.add(verticalLayout);
            this.add(layoutMobileContent);
        } else {
            HorizontalLayout layoutContent = new HorizontalLayout();
            layoutContent.addClassNames(Width.FULL,
                    AlignItems.START, JustifyContent.CENTER,
                    Padding.XSMALL, Margin.NONE,
                    Gap.XSMALL
            );

            layoutContent.add(verticalLayout);
            this.add(layoutContent);
        }

        if (!strAlbumTitle.equalsIgnoreCase(STR_ALL_ALBUMS)) {
            this.add(loadPageFooter(sqlAlbumsAll + " " + sqlAlbumsGroupBy + " " + sqlAlbumsOrderBy, arrColumnsAlbums));
        }
        this.add(genericView.loadFooter(isMobile));
        logVisitorToDb();
    }

    private VerticalLayout loadPageFooter(String sqlRead, String[] arrColumnNames) {
        VerticalLayout layoutPageFooter = new VerticalLayout();
        layoutPageFooter.addClassNames(Width.FULL, Padding.MEDIUM, Margin.NONE, AlignItems.CENTER, JustifyContent.START);
        H3 moreAlbums = new H3("More Albums");
        layoutPageFooter.add(moreAlbums);
        layoutPageFooter.add(loadMoreAlbumsHorizontally(sqlRead + " LIMIT 8 ", arrColumnNames, 2));

        //  H3 moreAlbumsCategories = new H3("Album Categories");
        // layoutPageFooter.add(moreAlbumsCategories);
        //layoutPageFooter.add(loadFiltersHeader(sqlAlbumsCategoriesAll + sqlAlbumsCategoriesGroupBy, arrColumnsAlbumsCategories));

        return layoutPageFooter;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String o) {
        //       strAlbumTitle = o;//beforeEvent.getRouteParameters().get("member").orElse("pictures");
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
        this.addClassName("image-gallery-view");
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

        lstAlbums = new ArrayList<>();

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
                    Gap.SMALL,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        } else {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
                    Padding.SMALL,
                    Padding.Top.XSMALL,
                    Gap.MEDIUM,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        }

    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSectionCaption, String strSection) {

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


        H2 headerSection = new H2(strSection);
        headerSection.addClassNames(
                FontSize.XLARGE
        );
        if (strSection.isEmpty()) {
            headerSection.setVisible(false);
        }

        Div headerSectionCaption = new Div(strSectionCaption);
        headerSectionCaption.addClassNames(
                AlignItems.CENTER, JustifyContent.START,
                Margin.NONE, Margin.Top.NONE,
                FontSize.SMALL, TextColor.SECONDARY);
        if (strSectionCaption.isEmpty()) {
            headerSectionCaption.setVisible(false);
        }


        Div divLine = new Div();
        divLine.addClassNames(Border.BOTTOM, Width.FULL);

        Div divLine2 = new Div();
        divLine2.addClassNames(Border.BOTTOM, Width.FULL);

        headerContainer.add(header, subheader, divLine);

        headerContainer.add(loadFiltersHeader(sqlAlbumsCategoriesAll + sqlAlbumsCategoriesGroupBy, arrColumnsAlbumsCategories, "Albums"));
        headerContainer.add(headerSection, headerSectionCaption, divLine2);

        return headerContainer;
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

            Image imgAvatar = genericView.getAvatarThumbImage(strAvatarPath, strName + " " + strSurname, "80px", "80px");

            H2 objMemberName = new H2(strName + " " + strSurname);
            objMemberName.addClassNames(TextColor.TERTIARY);
            H2 objMember = new H2(strUsername);
            objMember.addClassNames(TextColor.TERTIARY);
            Div divMemberSince = new Div("Member since " + strMemberSince);
            // Div divAlbumsAndPhotos = new Div("Has " + strCountOfAlbums + " albums");
            layoutMember.add(imgAvatar, objMemberName, objMember, divMemberSince);
        } else {

        }
        verticalLayout.add(layoutMember);
    }

    private void showDialogWithCarousel(String sqlCarousel, String sqlReadAlbumPhotosOrderBy, String[] arrColumnsCarousel, String strAlbum, String strAlbumUsername) {
/*
        String[] arrAlbumNames = new String[]{"user_id", "id", "title", "description", "city_name", "country"};
        String sqlReadAlbums = "SELECT distinct a.title , a.description, a.user_id, d.city_name, d.country " +
                " FROM  destination d RIGHT JOIN photo_album a  ON (d.id = a.destination_id )  LEFT JOIN photo_album_photo pap ON (pap.photo_album_id = a.id AND a.user_id = pap.user_id), dbuser usr " +
                " WHERE usr.userId = a.user_id " +
                "  AND usr.username = '" + strAlbumUsername + "' " +
                " ORDER BY title ASC ";

        Dialog dlgCarousel = new Dialog();
        dlgCarousel.setDraggable(true);
        dlgCarousel.setResizable(false);
        dlgCarousel.setWidthFull();
        dlgCarousel.setHeightFull();
        dlgCarousel.addClassNames(LumoUtility.Overflow.HIDDEN,
                Margin.NONE, Padding.SMALL,
                AlignItems.CENTER, JustifyContent.CENTER,
                BorderRadius.NONE);
        dlgCarousel.setCloseOnOutsideClick(true);
        dlgCarousel.setCloseOnEsc(true);
        dlgCarousel = genericView.showCarouselDialog(true, sqlCarousel, sqlReadAlbumPhotosOrderBy, arrColumnsCarousel, strAlbum, "a.title",
                sqlReadAlbums, arrAlbumNames);
        dlgCarousel.open();*/
    }


    private void loadAlbums(String sqlRead, String[] arrColumnNames, int intType) {
        strPath = DIR_PHOTOS_SERVER + dirChar;

        Div divGallery = new Div();
        divGallery.addClassName("layout-albums");

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {
            Record rec = lstRecords.get(r);
            divGallery.add(getAlbumsFromDb(rec, intType));
        }
        verticalLayout.add(divGallery);
    }

    private AlbumViewCard getAlbumsFromDb(Record record, int intType) {
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

        AlbumViewCard albumViewCard = new AlbumViewCard(record, strImagePath, isMobile, userId, strUsername, sessionCreation, hostname, publicIp, intType,
                recordService, sqlReadAlbumPhotos, sqlReadAlbumPhotosOrderBy, arrColumnNamesAlbumPhotos);
        return albumViewCard;
    }


    private void loadAlbumImagesFromDb(String sqlRead, String[] arrColumnNames, boolean isEditable, String strPhotoView) {
        //      strPath = DIR_PHOTOS_SERVER + dirChar;

//        Div divGallery = new Div();
//        divGallery.addClassName("gallery");
//
//        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
//        for (int r = 0; r < lstRecords.size(); r++) {
//
//            Record rec = lstRecords.get(r);
//            divGallery.add(getAlbumImagesFromDb(rec, isEditable));
//        }

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);




        Div divGallery = new Div();
        divGallery.addClassName("gallery");
        for (int r = 0; r < lstRecords.size(); r++) {
            Record rec = lstRecords.get(r);
            String strId = rec.getColumnData("id");


//            if (strPhotoView.equalsIgnoreCase(VIEW_PHOTO_GRID)) {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathMedium;
            divGallery.add(getAlbumImagesFromDb(rec, isEditable));
//            } else if (strPhotoView.equalsIgnoreCase(VIEW_BIGGER_PHOTOS)) {
//                strPath = DIR_PHOTOS_SERVER + dirChar + subPathLarge;
//                viewBiggerPhotos.add(getAlbumImagesFromDb(rec, isEditable));
//            }
        }


//        if (strPhotoView.equalsIgnoreCase(VIEW_PHOTO_GRID)) {
        verticalLayout.add(divGallery);
//        } else if (strPhotoView.equalsIgnoreCase(VIEW_BIGGER_PHOTOS)) {
//            verticalLayout.add(viewBiggerPhotos);
//        }


    }

    private Scroller loadMoreAlbumsHorizontally(String sqlRead, String[] arrColumnNames, int intType) {

        strPath = DIR_PHOTOS_SERVER + dirChar + subPathThumbs;

        HorizontalLayout layoutGallery = new HorizontalLayout();
        layoutGallery.addClassNames(Overflow.SCROLL,
                Width.FULL, Height.FULL,
                Padding.LARGE, Margin.NONE,
                Gap.LARGE,
                AlignItems.CENTER, JustifyContent.CENTER);

        Scroller scrMoreAlbums = new Scroller(Scroller.ScrollDirection.HORIZONTAL);
        scrMoreAlbums.addClassNames(Width.FULL, Height.FULL, Padding.LARGE, Margin.NONE);

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {
            Record rec = lstRecords.get(r);
            AlbumThumbViewCard albumThumb = new AlbumThumbViewCard(rec, strPath, isMobile, userId, sessionCreation, hostname, publicIp, recordService, intType);
            layoutGallery.add(albumThumb);
        }
        scrMoreAlbums.setContent(layoutGallery);

        return scrMoreAlbums;
    }

    private Div loadFiltersHeader(String sqlRead, String[] arrColumnNames, String strCaptionsCount) {
        Div filtersBar = new Div();

        filtersBar.addClassName("top-tall-layout-filters");

        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);

        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {

            FilterTallCard filterTallCard = new FilterTallCard(lstLearningCategoriesRecs.get(r), strPath, isMobile, userId, sessionCreation, publicIp, strCaptionsCount,
                    this);
            filterTallCard.addClassName("top-tall-filters");
            filtersBar.add(filterTallCard);
        }

        return filtersBar;
    }


    private GalleryImageViewCard getAlbumImagesFromDb(Record record, boolean isEditable) {

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
                recordService, 1, sqlReadAlbumPhotos, sqlReadAlbumPhotosOrderBy, arrColumnNamesAlbumPhotos, shareService, shareMetricService, weatherService, photoRatingService, photoViewService);
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
            strUrlRequestToBeLogged = strUrlRequestToBeLogged.replace("'", "");
            strUrlRequestToBeLogged = "'" + strUrlRequestToBeLogged + "'";
        }

        String strPathB;
        if (strPath == null || strPath.isEmpty()) {
            strPathB = "NULL";
        } else {
            strPathB = "'" + strPath + "'";
        }


        logger.info("photo visitor:" + publicIp + " . " + hostname + " . " + hostAddress + " . " + canonicalHostname + "  .  " + browser + " " + sessionid);

        String insertSQL = "INSERT INTO dbvisitor_log SET visitorlogId = 0,  timeOfVisit = now(), ipAddress = '" + publicIp + "', browserName = '" + browser + "', "
                + " browserVersionMajor = '" + versionOfBrowserMajor + "', browserVersionMinor = '" + versionOfBrowserMinor + "', urlParameter = NULL , timeZoneId = '" + timeZoneId + "', "
                + " appVersion = '" + APP_NAME + "-" + APP_VERSION + "',  parentSection = 'photo',  sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
                + " hostAddress = '" + hostAddress + "', os = '" + strOS + "', browser = '" + strBrowser + "', section = '" + section + "',"
                + " item = " + strPathB + ", ref = " + strUrlRequestToBeLogged + ", "
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
