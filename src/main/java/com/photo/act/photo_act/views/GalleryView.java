package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.CacheService;
import com.photo.act.photo_act.services.PhotoRatingService;
import com.photo.act.photo_act.services.PhotoViewService;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.*;
import com.photo.act.photo_act.views.components.Layout;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;
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

import static com.photo.act.photo_act.utils.UtilsString.sanitizeLocation;
import static com.photo.act.photo_act.views.HomeView.subPathMedium;
import static com.photo.act.photo_act.views.MainLayout.*;

@AnonymousAllowed

@Route(value = "photos") //":category?")
@RouteAlias(value = "photos/location/:destination?", layout = MainLayout.class)
@RouteAlias(value = "photos/location-type/:destination-type?", layout = MainLayout.class)
@RouteAlias(value = "photos/month-uploaded/:month-uploaded?", layout = MainLayout.class)
@RouteAlias(value = "photos/member/:member?/location/:destination?", layout = MainLayout.class)



//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class GalleryView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";

    private static final Logger logger = LoggerFactory.getLogger(GalleryView.class);

    String sqlGalleryReadOrderBy;
    private VerticalLayout verticalLayout;

    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String section = SECTION_GALLERY;
    private String strMember;
    private String strDestination;
    private String strDestinationType;
    private String strUploadedMonth;
    private String strPhotoId;
    private RecordService recordService;
    private ShareService shareService;
    private ShareMetricService shareMetricService;
    private WeatherService weatherService;
    private PhotoRatingService photoRatingService;
    private PhotoViewService photoViewService;
    private String strHeader;

    private final List<PhotoItem> photos = new ArrayList<>();
    private int currentIndex = 0;

    // Lightbox widgets (reused across opens)
    private Dialog lightbox;
    private Image lightboxImage;
    private Span lightboxCaption;
    private Span lightboxCounter;

    private String strUrlRequestToBeLogged;



    private int userId;
    private String strUsername;

    private String strColorExternalweb = "#9fafd5";

    private String[] arrPhotoGenreNames = {"id", "title", "description"};
    private String sqlReadPhotoGenre = "SELECT " +
            "  `id`, `title`, `description` " +
            " FROM photo_genre " +
            " ORDER BY title ";

    private String[] arrUploadedPeriodCatNames = {"photo_up_month_id", "photo_up_date", "photo_up_count"};
    private String sqlUploadedPeriodCat =
            " SELECT  DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, DATE_FORMAT(pm.date_inserted, '%M') AS photo_up_month " +
                    " , DATE_FORMAT(pm.date_inserted, '%Y%m') AS photo_up_month_id, DATE_FORMAT(pm.date_inserted, '%M %Y') AS photo_up_date, count(pm.id) AS photo_up_count " +
                    " , getDateDiffFromNow(pm.date_inserted) AS date_inserted_diff_from_now " +
                    " , pm.meta_i_height, pm.meta_i_length, pm.meta_i_width " +
                    " , usr.username, usr.surname, usr.name, usr.resident, usr.resident_country, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since, usr.avatar_path " +
                    " , usr.short_bio " +
                    " , ux.count_photos, ux.count_stories " +
                    " FROM dbuser usr, dbuser_extra ux, photo_meta pm" +
                    " WHERE pm.uploaderId = usr.userId AND pm.visible_to = 'ALL' " +
                    " AND usr.userId = ux.user_id ";
    private String sqlUploadedPeriodCatGroupby =
            " GROUP BY photo_up_month_id " +
                    " ORDER BY photo_up_month_id DESC " +
                    " LIMIT 12 ";

    private String[] arrDestinationCatNames = {"id", "dest_cat_title", "dest_cat_count"};
    private String sqlReadDestinationCat = " SELECT  dc.id, dc.dest_cat_title, COUNT(d.category_id) AS dest_cat_count " +
            " , dc.dest_cat_title, dc.dest_cat_type, dc.dest_cat_descr_min " +
            " FROM destination d, destination_categories dc " +
            " WHERE dc.id = d.category_id ";
    private String sqlReadDestinationCatGroupby =
            " GROUP BY d.category_id " +
                    " ORDER BY dc.dest_cat_order ASC ";

    private String[] arrDestinationNames = {"id", "city_name", "prefecture", "country", "name_new", "nearby_city", "destination_type_name", "name_for_map", "name_for_weather", "photo_count"
            , "dest_cat_title"};
    private String sqlReadDestination = "SELECT d.city_name, d.prefecture, d.country, pm.name_new, d.nearby_city, d.destination_type_name, d.name_for_map, d.name_for_weather, COUNT(pm.id) AS photo_count" +
            " , dc.dest_cat_title " +
            " FROM photo_meta pm, destination d, destination_categories dc " +
            " WHERE pm.destination_Id = d.id AND dc.id = d.category_id ";
    private String sqlReadDestinationGroupby =
            " GROUP BY d.id " +
                    " ORDER BY city_name ASC, pm.date_inserted DESC ";

    private String[] arrDestinationAssignedNames = {"id", "city_name", "prefecture", "country"};
    private String sqlReadDestinationAssigned = "SELECT distinct city_name, d.id, prefecture, country " +
            " FROM photo_meta pm LEFT JOIN destination d ON pm.destination_id = d.id " +
            " ORDER BY country ASC, city_name ASC ";

    private String[] arrSubjectNames = {"id", "subject_name", "subject_description", "subject_type"};
    private String sqlReadSubject = "SELECT distinct subject_name, subject_description, subject_type " +
            " FROM  photo_meta pm LEFT JOIN subject s ON pm.subject_id = s.id " +
            " ORDER BY subject_name ASC ";

    @Autowired
    private CacheService cacheService;

    private String[] arrColumnNamesGallery = {"id", "name_new", "title", "subtitle", "notes", "photo_type", "uploader", "creator", "visible_to", "meta_date", "photo_date", "photo_time", "photo_time_shot"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso", "meta_aperture", "meta_shutter_speed", "meta_orientation", "meta_i_height", "meta_i_length", "meta_i_width"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon"
            , "city_name"
            , "subject_name", "subject_description", "subject_type"
            , "date_inserted_diff_from_now"
            , "username", "surname", "name", "resident", "resident_country", "date_joined", "member_since", "avatar_path", "short_bio"
            , "count_photos", "count_stories"
    };


    private int intPage = 1;
    private int intRecsOnPage = 20;
    private String strDefCountPerPage = "20";


    private String sqlReadGalleryDestinations =
            " SELECT pm.id, pm.name_new, pm.title, pm.subtitle, pm.notes, pm.photo_type, pm.uploader, pm.creator, pm.visible_to,  DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, DATE_FORMAT(pm.meta_date, '%M %Y') AS photo_date, DATE_FORMAT(pm.meta_date, '%H:%i') AS photo_time " +
                    " , DATE_FORMAT(pm.meta_date, '%d/%m/%Y - %H:%i:%S') AS photo_time_shot,  pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model,  pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed, meta_orientation ,  pm.meta_i_height, pm.meta_i_length, pm.meta_i_width , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
                    " , getDateDiffFromNow(pm.date_inserted) AS date_inserted_diff_from_now " +
                    " , d.city_name, d.prefecture, d.country " +
                    " , usr.username, usr.surname, usr.name, usr.resident, usr.resident_country, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since, usr.avatar_path " +
                    " , usr.short_bio " +
                    " , ux.count_photos, ux.count_stories " +
                    " FROM dbuser usr, dbuser_extra ux, photo_meta pm" +
                    " LEFT JOIN destination d ON pm.destination_id = d.id " +
                    " WHERE pm.uploaderId = usr.userId AND pm.visible_to = 'ALL' " +
                    " AND usr.userId = ux.user_id ";
    private String sqlReadGallery1OrderBy = " ORDER BY pm.date_inserted DESC  ";

    private String sqlReadGallerySubjects =
            " SELECT pm.id, pm.name_new, pm.title, pm.subtitle, pm.notes, pm.photo_type, pm.uploader, pm.creator, pm.visible_to,  DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, DATE_FORMAT(pm.meta_date, '%M %Y') AS photo_date, DATE_FORMAT(pm.meta_date, '%H:%i') AS photo_time " +
                    " , DATE_FORMAT(pm.meta_date, '%d/%m/%Y - %H:%i:%S') AS photo_time_shot,  pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model,  pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed, meta_orientation,  pm.meta_i_height, pm.meta_i_length, pm.meta_i_width , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
                    " , getDateDiffFromNow(pm.date_inserted) AS date_inserted_diff_from_now " +
                    " , usr.username, usr.surname, usr.name, usr.resident, usr.resident_country, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined,  DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since, usr.avatar_path " +
                    " , usr.short_bio  " +
                    " , ux.count_photos, ux.count_stories " +
                    " , s.subject_name " +
                    " FROM  dbuser usr, dbuser_extra ux, photo_meta pm " +
                    " RIGHT JOIN  subject s ON s.id = pm.subject_id " +
                    " WHERE pm.uploaderId = usr.userId AND pm.visible_to = 'ALL' " +
                    " AND usr.userId = ux.user_id ";
    private String sqlReadGallery2OrderBy = " ORDER BY pm.date_inserted DESC ";

    // private String sqlReadGallery = "( " + sqlReadGalleryDestinations + " " + sqlReadGallery1OrderBy + " LIMIT " + (intRecsOnPage) + " ";
//            ") UNION (" + sqlReadGallery2 + " " + sqlReadGallery2OrderBy + " LIMIT " + (intRecsOnPage / 2) + ") ";

    private String sessionid;
    private long sessionCreation;
    private String publicIp;
    private String strPath;
    private String hostname;
    private String hostAddress;
    private String canonicalHostname;
    private UtilsDate utilsDate;
    private String sessionDateTime;
    private GenericView genericView;
    private VerticalLayout filtersContainer;
    private String strOS;
    private String strBrowser;

    private String dirChar = FileSystems.getDefault().getSeparator();

    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";


//    private Select<String> cmbCount;
//    private Select<String> cmbSortBy;
    // private VerticalLayout recsHolder;

    private HorizontalLayout layoutRecControl;

    private String[] arrOrderByItems = {"Newest Upload First", "Oldest Upload First", "Newest Shot First", "Oldest Shot First"};
    private String[] arrOrderByItemsSql = {"ORDER BY pm.date_inserted DESC", "ORDER BY pm.date_inserted ASC", "ORDER BY pm.meta_date DESC", "ORDER BY pm.meta_date ASC"};
    private String sqlOrderBy = " ORDER BY pm.date_inserted DESC";
    private String strDefOrderBy = arrOrderByItems[0];

    private Section sidebar;

    public record PhotoItem(String url, String thumbnailUrl, String title, String description) {
        public PhotoItem(String url, String title, String description) {
            this(url, url, title, description);
        }
    }

    public GalleryView(RecordService recordService, ShareService shareService, ShareMetricService shareMetricService, WeatherService weatherService, PhotoRatingService photoRatingService, PhotoViewService photoViewService) {
        this.recordService = recordService;
        this.shareService = shareService;
        this.shareMetricService = shareMetricService;
        this.weatherService = weatherService;
        this.photoRatingService = photoRatingService;
        this.photoViewService = photoViewService;
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
        strDestination = event.getRouteParameters().get("destination").orElse(STR_ALL_DESTINATIONS);
        strDestinationType = event.getRouteParameters().get("destination-type").orElse(STR_ALL_DESTINATION_TYPES);
        strUploadedMonth = event.getRouteParameters().get("month-uploaded").orElse(STR_ALL_MONTHS);
        strPhotoId = event.getRouteParameters().get("photo-id").orElse("");

        getUserClientInfo();

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously
            strUrlRequestToBeLogged = currentUrl.toExternalForm();
        });

        NetUtils netUtils = new NetUtils();
        publicIp = netUtils.getClientPublicIp(hostname);

        if (strDestination.isEmpty()) {
            logger.error(" empty strDestination: " + strDestination);
        }
        intPage = 1;
        VerticalLayout layoutHeaderParameters = null;
        verticalLayout.removeAll();

        Div divGallery = new Div();
        divGallery.addClassName("gallery");

        logger.info("---  " + strMember + " " + strDestination + " " + strDestinationType + "  " + strUploadedMonth);
        if (!strPhotoId.isEmpty()) {
            filter(divGallery, "", VIEW_ONE_PHOTO);
            layoutHeaderParameters = loadHeader("Photos", "Uploaded by our members", "", "");
            filtersContainer.removeAll();
            //   layoutHeaderParameters.add(loadFiltersHeader(sqlReadDestinationCat + sqlReadDestinationCatGroupby, arrDestinationCatNames, "Locations"));
            // String sqlOrderBy = " ORDER BY pm.date_inserted DESC, pm.title ASC, meta_date DESC ";
            filter(divGallery, "", VIEW_PHOTO_GRID);
        } else if (!strUploadedMonth.isEmpty() && (strDestination.isEmpty() || strDestination.equalsIgnoreCase(STR_ALL_DESTINATIONS) && (strDestinationType.isEmpty() || strDestinationType.equalsIgnoreCase(STR_ALL_DESTINATION_TYPES)))) {
            layoutHeaderParameters = loadHeader("Photos", "Uploaded by our members", "Month Uploaded", strUploadedMonth);

            filtersContainer.removeAll();
            filtersContainer.add(loadFiltersHeader(sqlUploadedPeriodCat + sqlUploadedPeriodCatGroupby, arrUploadedPeriodCatNames, "month-uploaded", "Photos"));
            if(!strUploadedMonth.equalsIgnoreCase(STR_ALL_MONTHS)) {
                String sqlWhereSubClause = " AND  DATE_FORMAT(pm.date_inserted, '%M %Y') LIKE '" + strUploadedMonth + "'  ";
                filter(divGallery, sqlWhereSubClause, VIEW_PHOTO_GRID);
            }else{
                filter(divGallery,"", VIEW_PHOTO_GRID);
            }
        } else if (strMember.equalsIgnoreCase(STR_ALL_MEMBERS) && strDestination.equalsIgnoreCase(STR_ALL_DESTINATIONS) && strDestinationType.equalsIgnoreCase(STR_ALL_DESTINATION_TYPES)) {
            layoutHeaderParameters = loadHeader("Photos", "Uploaded by our members", "", "");
            //   layoutHeaderParameters.add(loadFiltersHeader(sqlReadDestinationCat + sqlReadDestinationCatGroupby, arrDestinationCatNames, "Locations"));
            // String sqlOrderBy = " ORDER BY pm.date_inserted DESC, pm.title ASC, meta_date DESC ";
            filtersContainer.removeAll();

            filter(divGallery, "", VIEW_PHOTO_GRID);
        } else if (strMember.equalsIgnoreCase(STR_ALL_MEMBERS) && strDestination.equalsIgnoreCase(STR_ALL_DESTINATIONS) && !strDestinationType.equalsIgnoreCase(STR_ALL_DESTINATION_TYPES)) {
            layoutHeaderParameters = loadHeader("Photos", "Uploaded by our members", "Location type", strDestinationType);
            //   layoutHeaderParameters.add(loadFiltersHeader(sqlReadDestinationCat + sqlReadDestinationCatGroupby, arrDestinationCatNames, "Locations"));
            // String sqlOrderBy = " ORDER BY pm.date_inserted DESC, pm.title ASC, meta_date DESC ";
            filtersContainer.removeAll();
            filtersContainer.add(loadFiltersHeader(sqlReadDestinationCat + sqlReadDestinationCatGroupby, arrDestinationCatNames, "destination-type", "Locations"));

            String sqlWhereSubClause = sqlReadDestination + " AND dc.dest_cat_title LIKE '" + strDestinationType + "'  " + sqlReadDestinationGroupby;
            layoutHeaderParameters.add(loadDestinationCards(sqlWhereSubClause, arrDestinationNames, "city_name"));

        } else if (strMember.equalsIgnoreCase(STR_ALL_MEMBERS) && !strDestination.equalsIgnoreCase(STR_ALL_DESTINATIONS) && !strDestination.isEmpty()) {
            layoutHeaderParameters = loadHeader("Photos", "Uploaded by our members", "Location", strDestination);

            filtersContainer.removeAll();
            filtersContainer.add(loadFiltersHeader(sqlReadDestinationCat + sqlReadDestinationCatGroupby, arrDestinationCatNames, "destination-type", "Locations"));
            String sqlWhere = " AND city_name LIKE '"+sanitizeLocation(strDestination)+"' ";

            String strForMap = "";
            String strForWeather = "";
            String strCountry = "";
            List<Record> lstLocationRecs = getRecordsFromDb(sqlReadDestination+sqlWhere, arrDestinationNames);
            if(lstLocationRecs!= null && !lstLocationRecs.isEmpty())
            {
                strForMap = lstLocationRecs.get(0).getColumnData("name_for_map");
                strForWeather = lstLocationRecs.get(0).getColumnData("name_for_weather");
                strCountry = lstLocationRecs.get(0).getColumnData("country");
            }

            HorizontalLayout layoutWeatherMap = new HorizontalLayout();
            layoutWeatherMap.setAlignItems(FlexComponent.Alignment.CENTER);
            layoutWeatherMap.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);
            layoutWeatherMap.setWrap(true);
            layoutWeatherMap.add(
                    loadWeatherSmall(strDestination, strForWeather, strCountry),
                    loadMapSmall(strDestination, strForMap, strCountry));
            layoutHeaderParameters.add(layoutWeatherMap);

            String sqlWhereSubClause = " AND d.city_name LIKE '" + strDestination + "'  ";
            filter(divGallery, sqlWhereSubClause, VIEW_PHOTO_GRID);
        } else if (!strMember.equalsIgnoreCase(STR_ALL_MEMBERS)) {
            layoutHeaderParameters = loadHeader("Photos", "Uploaded by our members", "", "");

            String sqlWhere = " AND city_name LIKE "+strDestination+" ";
            String strForMap = "";
            String strForWeather = "";
            String strCountry = "";
            List<Record> lstLocationRecs = getRecordsFromDb(sqlReadDestination+sqlWhere, arrDestinationNames);
            if(lstLocationRecs!= null && !lstLocationRecs.isEmpty())
            {
                strForMap = lstLocationRecs.get(0).getColumnData("name_for_map");
                strForWeather = lstLocationRecs.get(0).getColumnData("name_for_weather");
                strCountry = lstLocationRecs.get(0).getColumnData("country");
            }
            HorizontalLayout layoutWeatherMap = new HorizontalLayout();
            layoutWeatherMap.setAlignItems(FlexComponent.Alignment.CENTER);
            layoutWeatherMap.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);
            layoutWeatherMap.setWrap(true);
            layoutWeatherMap.add(
                    loadWeatherSmall(strDestination, strForWeather, strCountry),
                    loadMapSmall(strDestination, strForMap, strCountry));
            layoutHeaderParameters.add(layoutWeatherMap);
            filtersContainer.removeAll();
            filter(divGallery, "", VIEW_PHOTO_GRID);
        } else {
            layoutHeaderParameters = loadHeader("Photos", "Uploaded by our members", "", "");
            filtersContainer.removeAll();

            Div layoutFiltersSubject = new Div();
            layoutFiltersSubject.add(loadDestinationCards(sqlReadSubject, arrSubjectNames, "subject_name"));
            layoutFiltersSubject.addClassNames(Width.FULL, Height.FULL);
            verticalLayout.add(layoutFiltersSubject);

            filter(divGallery, "", VIEW_PHOTO_GRID);
        }

        this.removeAll();
        this.add(layoutHeaderParameters);

        if (isMobile) {
            VerticalLayout layoutMobileContent = new VerticalLayout();
            layoutMobileContent.addClassNames(Width.FULL,
                    AlignItems.START, JustifyContent.BETWEEN,
                    Padding.MEDIUM, Margin.NONE,
                    Gap.XSMALL
            );
            layoutMobileContent.add(verticalLayout);
            this.add(layoutMobileContent);
        } else {
            VerticalLayout layoutContent = new VerticalLayout();
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

        filtersContainer = new VerticalLayout();
        filtersContainer.addClassNames(Width.FULL,
                Margin.NONE, Padding.NONE,
                Gap.XSMALL);

        verticalLayout = new VerticalLayout();
        if (isMobile){
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,// not full width
                    Margin.NONE,
                    Padding.NONE,
                    Padding.Top.XSMALL,
                    Gap.SMALL,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        }else{
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

        }


        layoutRecControl = new HorizontalLayout();
        layoutRecControl.addClassName("actions");
        layoutRecControl.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);

    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSectionCaption, String strSection) {

        this.strHeader = strHeader;

        VerticalLayout headerContainer = new VerticalLayout();
        if (isMobile) {
            headerContainer.addClassNames(
                    AlignItems.START, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.SMALL,
                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        } else {
            headerContainer.addClassNames(
                    AlignItems.STRETCH, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, Width.FULL,
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
                Margin.NONE, Margin.Top.NONE,
                FontSize.SMALL, TextColor.SECONDARY);


        H2 headerSection = new H2(strSection);
        headerSection.addClassNames(
                FontSize.XLARGE
        );
        headerSection.getStyle().set("text-transform","capitalize");
        if (strSection.isEmpty() ||  strSection.contains("all")) {
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

        VerticalLayout layoutHeader = new VerticalLayout();
        layoutHeader.addClassNames(Width.FULL,
                AlignItems.START, JustifyContent.EVENLY,
                Padding.NONE, Margin.NONE, Gap.XSMALL);
        layoutHeader.add(header, subheader);

//
//        Button btnLastUploaded = new Button("Last Uploaded");
//        btnLastUploaded.setIcon(FontAwesome.Solid.CALENDAR_DAY.create());

//        HorizontalLayout layoutTabViewPhotos = new HorizontalLayout();
//
//        layoutTabViewPhotos.addClassName("tab-select");
//        RadioButtonGroup<String> btnGroupShowPhotos = new RadioButtonGroup<>();
////        btnGroupShowPhotos.setItems(btnLastUploaded);
//        btnGroupShowPhotos.setItems("Month Uploaded", "Location Type");
//        layoutTabViewPhotos.add(btnGroupShowPhotos);
//        btnGroupShowPhotos.addValueChangeListener(e -> {
//            if (e.getValue() == null) {
//
//            } else if (e.getValue().contains("Uploaded")) {
//                filtersContainer.removeAll();
//                filtersContainer.add(loadFiltersHeader(sqlUploadedPeriodCat + sqlUploadedPeriodCatGroupby, arrUploadedPeriodCatNames, "month-uploaded", "Photos"));
////                e.getSource().getUI().ifPresent(ui ->
////                        ui.navigate(GalleryView.class)
////                );
//
//            } else if (e.getValue().contains("Location")) {
//                filtersContainer.removeAll();
//
//                filtersContainer.add(loadFiltersHeader(sqlReadDestinationCat + sqlReadDestinationCatGroupby, arrDestinationCatNames, "destination-type", "Locations"));
//
//            } else if (e.getValue().contains("Object")) {
//                filtersContainer.removeAll();
//                filtersContainer.add(loadFiltersHeader(sqlReadDestinationCat + sqlReadDestinationCatGroupby, arrDestinationCatNames, "destination-type", "Objects"));
//            } else if (e.getValue().contains("Date")) {
//                filtersContainer.removeAll();
//                filtersContainer.add(loadFiltersHeader(sqlReadDestinationCat + sqlReadDestinationCatGroupby, arrDestinationCatNames, "destination-type", "Objects"));
//            }
//        });
//        btnGroupShowPhotos.setValue("By Genre");


//        HorizontalLayout headerNTabsLayout = new HorizontalLayout();
//        headerNTabsLayout.addClassNames(Width.FULL,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Padding.NONE, Margin.NONE, Gap.XSMALL);
//        headerNTabsLayout.add(layoutHeader, layoutTabViewPhotos);

        headerContainer.add(layoutHeader);


        headerContainer.add(filtersContainer);
        headerContainer.add(headerSection, headerSectionCaption, divLine);
       // headerContainer.add(createToolbar());

        return headerContainer;
    }

    private HorizontalLayout getFooterControls(Div divGallery, String sqlWhereAnd, String sqlOrderBy) {

        // recsHolder.removeAll();
        layoutRecControl.removeAll();

        Button btnLoadMore = new Button("Load More");
        btnLoadMore.setIcon(FontAwesome.Solid.ARROW_DOWN.create());
        btnLoadMore.setIconAfterText(true);

        btnLoadMore.addClickListener(event -> {
            int intResultsCount = 0;
            if (intPage > 0) {
                intPage++;
                //               divInfo.setText("Page " + intPage);

                intResultsCount = filter(divGallery, sqlWhereAnd, VIEW_PHOTO_GRID);

            }


            if (intResultsCount > 0) {
                event.getSource().setVisible(true);
            } else {
                event.getSource().setVisible(false);
            }
        });


        layoutRecControl.add(btnLoadMore);

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

    private VerticalLayout loadDestinationCards(String sqlRead, String[] arrColumnNames, String columnName) {

        VerticalLayout headerContainer = new VerticalLayout();
        if (isMobile) {
            headerContainer.addClassNames(
                    AlignItems.CENTER, JustifyContent.BETWEEN,
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
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, //Width.FULL,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.MEDIUM,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }


        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);

//        DIR_PHOTOS_SERVER = genericView.getAppProps(PROP_PHOTOS);
        headerContainer.addClassName("layout-destination-filters");
        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {

            //String captionCategory = lstLearningCategoriesRecs.get(r).getColumnData(columnName);
            FilterDestinationCard filterDestinationCard = new FilterDestinationCard(lstLearningCategoriesRecs.get(r), isMobile, userId, sessionCreation, publicIp, DIR_PHOTOS_SERVER, "Photos",
                    this);
            headerContainer.add(filterDestinationCard);
        }

        return headerContainer;
    }

    private Div loadFiltersHeader(String sqlRead, String[] arrColumnNames, String nameUrlVariable, String strCaptionsCount) {
        Div filtersPanel = new Div();
        filtersPanel.addClassName("top-tall-layout-filters");

        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);

        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {
            FilterDestinationTypeCard filterDestinationTypeCard = new FilterDestinationTypeCard(lstLearningCategoriesRecs.get(r), arrColumnNames, nameUrlVariable, strPath, isMobile, userId, sessionCreation, publicIp,
                    strCaptionsCount, this);
            filterDestinationTypeCard.addClassName("top-tall-filters");
            filtersPanel.add(filterDestinationTypeCard);
        }

        return filtersPanel;
    }

    public VerticalLayout getWeatherCurrent(String destination, String country) {
        HorizontalLayout layoutWeather = new HorizontalLayout();
        layoutWeather.getStyle().setColor("#8b94a0");
        layoutWeather.addClassNames(
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER
        );


        LocalWeatherForecast weatherForecast = new LocalWeatherForecast(weatherService, destination, country);
        weatherForecast.setMaxWidth("800px");

        layoutWeather.add(weatherForecast);



        if (destination != null && !destination.isEmpty()) {

/*
//        WeatherService weatherService = new WeatherService("metric");

        String[] locations = weatherService.lookUpLocation(destination, "", country);
        if (locations != null) {
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

 */

            VerticalLayout layout = new VerticalLayout();
            layout.setMargin(false);
            layout.setSpacing(false);
            layout.setPadding(false);
            layout.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);


            Anchor apiLink = new Anchor();
            apiLink.getStyle().setColor("#8b94a0");
            apiLink.setClassName("lazy-api-link");
//            apiLink.setHref(weatherService.getUrlReference());
//            apiLink.setTarget("_blank");
//            apiLink.setText("Weather data by: " + weatherService.getTitleReference());

            layout.add(layoutWeather, apiLink);

            return layout;
        } else {
            VerticalLayout layout = new VerticalLayout();
            layout.setMargin(false);
            layout.setSpacing(false);
            layout.setPadding(false);
            return layout;
        }
    }

    private IFrame getDestinationMap(String city, String country)
    {


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
                "            query: '"+city+", "+country+"',\n" +
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
                "            language: 'en-GB',\n"+
                "            mapboxgl: mapboxgl\n" +
                "        })\n" +
                "    );\n"+
                "\n" +
                "            // Create a marker and add it to the map.\n" +
                "            new mapboxgl.Marker().setLngLat(feature.center).addTo(map);\n" +
                "        });\n" +
                "\n" +
                "\n" +
                "    map.addControl(new mapboxgl.FullscreenControl());\n"+
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
        mapsFrame.setHeight("400px");
        mapsFrame.getStyle().setBorder("0px");
        mapsFrame.getStyle().setBorderRadius("6px");



        return mapsFrame;
    }

    private int filter(Div divGallery, String sqlWhereSubClause, String strPhotoView) {
        int intResultsCount = 0;

        if (strPhotoView.equalsIgnoreCase(VIEW_ONE_PHOTO)) {
            showDialogWithCarousel("", sqlWhereSubClause, strPhotoId, false);
            return 1;
        } else {

            String sqlWhereMember = "";
            if (!strMember.isEmpty() && !strMember.equalsIgnoreCase(STR_ALL_MEMBERS)) {
                sqlWhereMember = " AND usr.username LIKE '" + strMember + "' ";
            }

            String sqlOrderNLimit;
            if (intRecsOnPage == intPage * intRecsOnPage) {
                sqlOrderNLimit = sqlReadGallery1OrderBy + " LIMIT " + (intRecsOnPage);
            } else {
                sqlOrderNLimit = sqlReadGallery1OrderBy + " LIMIT " + (intRecsOnPage) + " OFFSET " + ((intPage -1) * intRecsOnPage);
            }

            String sqlReadPage;
            sqlReadPage = sqlReadGalleryDestinations + " " + sqlWhereSubClause + sqlWhereMember + " " + sqlOrderNLimit; //+ sqlReadGallery1OrderBy + " LIMIT " + (intRecsOnPage) + " ";

            List<Record> lstRecords = cacheService.getAllPhotos(sqlReadPage, arrColumnNamesGallery, "id"); //getRecordsFromDb(sqlRead, arrColumnsLearning);
            intResultsCount = lstRecords.size();
            logger.info(" record size: " + lstRecords.size());

            boolean isEditable = false;



/*            verticalLayout.add(createGalleryGrid(lstRecords));
            buildLightbox();*/

            verticalLayout.remove(layoutRecControl);
            if (strPhotoView.equalsIgnoreCase(VIEW_PHOTO_GRID)) {


                if (divGallery.getComponentCount() > 0) {
                    for (int r = 0; r < lstRecords.size(); r++) {
                        Record rec = lstRecords.get(r);
                        String strId = rec.getColumnData("id");

                        Record record = cacheService.getPhotoById(strId);

                        String strPath = DIR_PHOTOS_SERVER + dirChar + subPathMedium;
                        divGallery.add(getImageFromDb(record, strPath, isEditable));
                    }

                } else {
                    for (int r = 0; r < lstRecords.size(); r++) {
                        Record rec = lstRecords.get(r);
                        String strId = rec.getColumnData("id");

                        Record record = cacheService.getPhotoById(strId);

                        String strPath = DIR_PHOTOS_SERVER + dirChar + subPathMedium;
                        divGallery.add(getImageFromDb(record, strPath, isEditable));
                    }
                    verticalLayout.add(divGallery);
                }
                layoutRecControl = getFooterControls(divGallery, sqlWhereSubClause, sqlOrderNLimit);
                verticalLayout.add(layoutRecControl);
            }

            return intResultsCount;
        }
    }




    private Text getStars(int stars) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < stars) {
                builder.append("★");
            } else {
                builder.append("☆");
            }
        }
        return new Text(builder.toString());
    }

    private void toggleSidebar() {
        if (this.sidebar.isEnabled()) {
            closeSidebar();
        } else {
            openSidebar();
        }
    }

    private void openSidebar() {
        this.sidebar.setEnabled(true);
        this.sidebar.addClassNames(Border.RIGHT);
        // Desktop
        this.sidebar.getStyle().remove("margin-inline-start");
        // Mobile
        this.sidebar.addClassNames(Position.Start.NONE);
        this.sidebar.removeClassName(Position.Minus.Start.FULL);
    }

    private void closeSidebar() {
        this.sidebar.setEnabled(false);
        this.sidebar.removeClassName(Border.RIGHT);
        // Desktop
        this.sidebar.getStyle().set("margin-inline-start", "-20rem");
        // Mobile
        this.sidebar.addClassNames(Position.Minus.Start.FULL);
        this.sidebar.removeClassName(Position.Start.NONE);
    }

    private Component renderIconWithAriaLabel(String item) {
        Component icon = item.equals("Grid") ?
                VaadinIcon.GRID.create() :
                VaadinIcon.LIST.create();
        icon.getElement().setAttribute("aria-label", item);
        return icon;
    }

    private Button createIconButton(VaadinIcon symbol, String label) {
        Button button = new Button(symbol.create());
        button.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        button.setAriaLabel(label);
        button.setTooltipText(label);
        return button;
    }

    private void setRadioButtonGroupTheme(RadioButtonGroup<String> group, String... themeNames) {
        group.addThemeNames(themeNames);
        group.getChildren().forEach(component -> {
            for (String themeName : themeNames) {
                component.getElement().getThemeList().add(themeName);
            }
        });
    }

    private GalleryImageViewCard getImageFromDb(Record record, String strPath, boolean isEditable) {


        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");

        String strCity = record.getColumnData("city_name");
        String strSubject = record.getColumnData("subject_name");
        String strUploader = record.getColumnData("uploader");

        int isType = 2;

        logger.info(" Photo:" + strFileName + "  Gallery -> city and subject:'" + strCity + "'_'" + strSubject + "'");
        String sqlReadGallery = "";
        if (!strCity.isEmpty()) {
            isType = 2;
            sqlReadGallery = sqlReadGalleryDestinations;
        } else if (!strSubject.isEmpty()) {
            isType = 3;
            sqlReadGallery = sqlReadGallerySubjects;
        } else {

            sqlReadGallery = sqlReadGalleryDestinations;
        }
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
                recordService, isType, sqlReadGallery, sqlOrderBy, arrColumnNamesGallery, shareService, shareMetricService, weatherService, photoRatingService, photoViewService);
        return imageGalleryViewCard;
    }

    /**
     * Creates a fully responsive gallery Div containing photo cards.
     * Uses CSS Grid with auto-fill and minmax for responsive layout.
     * Each photo card is created using getImageFromDb.
     *
     * @param lstRecords List of Record objects from database
     * @param isEditable Whether the photos should be editable
     * @return Div containing responsive gallery of photo cards
     */
    public Div getGallery(List<Record> lstRecords, boolean isEditable) {
        Div divGallery = new Div();
        divGallery.addClassName("gallery");
        divGallery.addClassNames(Width.FULL, Padding.SMALL, Margin.NONE);

        if (lstRecords == null || lstRecords.isEmpty()) {
            logger.info("getGallery: No records to display");
            return divGallery;
        }

        String strPath = DIR_PHOTOS_SERVER + dirChar + subPathMedium;

        for (Record record : lstRecords) {
            String strId = record.getColumnData("id");
            if (strId != null && !strId.isEmpty()) {
                Record fullRecord = cacheService.getPhotoById(strId);
                if (fullRecord != null) {
                    GalleryImageViewCard photoCard = getImageFromDb(fullRecord, strPath, isEditable);
                    divGallery.add(photoCard);
                }
            }
        }

        logger.info("getGallery: Created gallery with " + lstRecords.size() + " photo cards");
        return divGallery;
    }

    /**
     * Creates a fully responsive gallery Div from SQL query.
     * Uses CSS Grid with auto-fill and minmax for responsive layout.
     *
     * @param sqlQuery SQL query to fetch photos
     * @param isEditable Whether the photos should be editable
     * @return Div containing responsive gallery of photo cards
     */
    public Div getGallery(String sqlQuery, boolean isEditable) {
        List<Record> lstRecords = cacheService.getAllPhotos(sqlQuery, arrColumnNamesGallery, "id");
        return getGallery(lstRecords, isEditable);
    }

    /**
     * Creates a fully responsive gallery Div with default non-editable mode.
     *
     * @param lstRecords List of Record objects from database
     * @return Div containing responsive gallery of photo cards
     */
    public Div getGallery(List<Record> lstRecords) {
        return getGallery(lstRecords, false);
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


        Path path = Paths.get(strImagePath);
        File file = path.toFile();
        Image image = new Image();

        image.setSrc(file.getAbsolutePath());
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
        UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
            if (extendedClientDetails == null) {
                logger.info("Image gallery - error timeZoneId: Cannot retrieve client details:" + extendedClientDetails);
                return;
            }
            timeZoneId = extendedClientDetails.getTimeZoneId();
        });
        sessionDateTime = utilsDate.calcDateTimeFromLong(sessionCreation, "UTC");



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

    private void showDialogWithCarousel(String strSelection, String sqlWhereSubClause, String strPhotoId, boolean isOnlyRating) {

        String[] arrNames = null;
        String sqlRead = "";

        String strFilterColumn = "";

        int isType = 2;
        arrNames = arrDestinationAssignedNames;
        sqlRead = sqlReadDestinationAssigned;
        strFilterColumn = "city_name";




        Dialog dlgCarousel = new Dialog();
        dlgCarousel.setDraggable(true);
        dlgCarousel.setResizable(true);
        dlgCarousel.setWidth("91%");
        dlgCarousel.setHeight("97%");
        dlgCarousel.addClassNames(Overflow.HIDDEN,
                Margin.NONE, Padding.SMALL,
                AlignItems.CENTER, JustifyContent.CENTER,
                BorderRadius.NONE);
        dlgCarousel.setCloseOnOutsideClick(true);
        dlgCarousel.setCloseOnEsc(true);
        dlgCarousel = genericView.showCarouselDialog(isType, sqlReadGalleryDestinations + sqlWhereSubClause, sqlReadGallery1OrderBy, arrColumnNamesGallery, strSelection, strFilterColumn,
                sqlRead, arrNames, strPhotoId, null, isOnlyRating, null);
        dlgCarousel.setWidth("1590px");

        dlgCarousel.open();
    }

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {
        logger.info(" photo  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames, Object[] sqlParValue, String[] sqlParType) {
        logger.info(" photo  getRecordsFromDb with params:   " + sql);
        return recordService.findAll(sql, arrColumnNames, sqlParValue, sqlParType);
    }


    private VerticalLayout loadMapSmall(String cityLabel, String strForMap, String country) {

        VerticalLayout layoutMapResult = new VerticalLayout();
        layoutMapResult.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.SMALL, Margin.NONE,
                BorderRadius.LARGE)
        ;

//        IFrame frameMapResult = getDestinationMap(strForMap, country);
//        frameMapResult.setMaxWidth("970px");
//
//        layoutMapResult.add(frameMapResult);

        return layoutMapResult;
    }


    private VerticalLayout loadWeatherSmall(String cityLabel, String strForWeather, String country) {

        VerticalLayout layoutWeatherResult = new VerticalLayout();
        layoutWeatherResult.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.SMALL, Margin.NONE,
                BorderRadius.LARGE)
        ;

        VerticalLayout weatherCard = getWeatherCurrent(strForWeather, country);
        weatherCard.setMaxWidth("990px");

        if (weatherCard.getChildren().count() > 0) {

            layoutWeatherResult.add(weatherCard);
        }

        return layoutWeatherResult;
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

        if (strPath == null || strPath.isEmpty()) {
            strPath = "NULL";
        } else {
            strPath = strPath.replace("\\", "-");
            strPath = strPath.replace("'", "");
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