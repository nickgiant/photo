package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.model.ShareType;
import com.photo.act.photo_act.model.ShareableResource;
import com.photo.act.photo_act.services.PhotoStoryViewService;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.AvatarItem;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.LikeButton;
import com.photo.act.photo_act.views.components.ShareBottomBar;
import com.photo.act.photo_act.views.components.StoryItemViewCard;
import com.photo.act.photo_act.views.components.StoryViewCard;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.method.P;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.helger.commons.locale.LocaleHelper.STR_ALL;
import static com.photo.act.photo_act.views.LearningsView.STR_ALL_CATEGORIES;
import static com.photo.act.photo_act.views.LearningsView.STR_ALL_TITLES;
import static com.photo.act.photo_act.views.MainLayout.*;

@AnonymousAllowed

@PageTitle("Stories · PhotoAct.net")
@Route(value = "stories") //":category?")
@RouteAlias(value = "stories/category/:category?", layout = MainLayout.class)
@RouteAlias(value = "stories/member/:member?/story/:story?", layout = MainLayout.class)


//@RouteAlias(value = "gallery/location/:destination?", layout = MainLayout.class)

//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class StoriesView extends Main implements BeforeEnterObserver, HasComponents, HasStyle {

    private static final Logger logger = LoggerFactory.getLogger(StoriesView.class);
    public static String subPathThumbs = "photo-thumbs";
    public static String subPathMedium = "photo-medium";
    public static String subPathUpload = "photo-upload";
    public static String subPathShow = "photo-show";
    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
    String[] arrColumnsMemberStories = {"stories_count"
            , "username", "surname", "name", "resident", "date_joined", "member_since", "avatar_path", "short_bio"
    };

    String sqlMemberOfStories = "SELECT usr.username, usr.name, usr.surname,  usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined " +
            " , DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
            " , usr.avatar_path, usr.short_bio " +
            " , count(usr.userId) AS stories_count " +
            " FROM dbuser usr LEFT JOIN photo_stories s ON s.user_id = usr.userId " +
            " WHERE  1 = 1 " +
            " AND s.story_visible_to = 'ALL' ";
    String sqlMemberOfStoriesGroupBy =
            " GROUP BY usr.userId " +
                    " ORDER BY usr.username ASC ";
    String[] arrColumnsStories = {"title", "slug", "description", "story_visible_to", "user_id", "date_inserted", "story_photo_count", "story_photo_size",
            "name_new", "photo_1", "photo_2", "datetime_story_created"
            , "cat_title", "cat_title"
            , "username", "surname", "name", "resident", "date_joined", "avatar_path"
            , "story_id"
    };
    String sqlStoriesAll = "SELECT s.title, s.slug,  s.`description`, s.story_visible_to, s.user_id, s.date_inserted " +
            " , count(sp.story_id) AS story_photo_count, SUM(pm.space_size) AS story_photo_size " +
            " , pm.name_new , s.photo_id1,  pm.name_new "+ //, p1.name_new AS photo_1 ,p2.name_new  AS photo_2 " +
            " , getDateDiffFromNow(s.date_inserted) AS datetime_story_created " +
            " , sc.cat_title, sc.cat_title " +
            " , usr.username, usr.name, usr.surname, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
            " , s.id AS story_id " +
            " FROM photo_stories_photo sp , photo_meta pm, photo_stories_categories sc, dbuser usr, photo_stories s " +//LEFT JOIN photo_meta p1 ON s.photo_id1 = p1.id " +
          //  " LEFT JOIN photo_meta p2 ON s.photo_id2 = p2.id " +
            " WHERE s.id = sp.story_id AND s.user_id = usr.userId AND sp.user_id = usr.userId AND s.photo_id1 = pm.id "+
            " AND s.story_visible_to = 'ALL' AND pm.visible_to  = 'ALL' " +
            " AND sc.id = s.category_id ";
    String sqlStoriesGroupBy =
            " GROUP BY sp.story_id " +
                    " ORDER BY s.date_inserted DESC ";
    String[] arrColStoriesCategories = {"cat_title", "cat_title", "cat_type", "cat_description_min", "cat_description_min", "cat_description_big", "cat_description_big", "cat_count"};
    String sqlStoriesCategoriesRead = "SELECT  " //f.nameShort, f.location, f.country, f.periodOfYear, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description  " +
            + " sc.cat_title, sc.cat_title, sc.cat_type, sc.cat_description_min, sc.cat_description_min, count (sc.id) AS cat_count, "
            + " s.title, s.title, s.description, s.description, s.category_id "
            // + " lc.cat_title, lc.cat_title, lc.cat_type, lc.cat_description_min. lc.cat_description_min, count (lc.cat_title) AS cat_count "
//            + " l.id, l.title, l.picture, l.section , l.category, l.format, l.url, l.parent_id, l.child_index, l.tutor_id, l.artists_ref, l.description, l.duration, l.pages, l.published, l.userIdInsert, l.username, l.dateInsert "
//            + ", l.tutor_id, l.tutor_id_team, t.tutor_name, t.website, t.url_fb, t.url_yt, t.url_insta, t.url_flickr, t.url_wikipedia, t.url_ref1, t.url_ref2, t.url_ref3, t.city_base, t.country_base, t.userIdInsert, t.username, t.date_inserted "
            + " FROM photo_stories s LEFT JOIN photo_stories_categories sc ON sc.id = s.category_id "
            + " WHERE 1 = 1 "
            + " GROUP BY sc.cat_title ORDER BY sc.cat_order ASC ";
    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";
    private VerticalLayout verticalLayout;
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String section = SECTION_STORIES;
    private String strMember;
    private String strTitle;
    private String strSlug;
    private String strCategory;
    private RecordService recordService;
    private PhotoStoryViewService photoStoryViewService;
    private WeatherService weatherService;
    @Autowired private ShareService shareService;
    @Autowired private ShareMetricService shareMetricService;
    @Value("${app.base-url}") private String baseUrl;
    private String strHeader;
    private String strUrlRequestToBeLogged;
    private String dirChar = FileSystems.getDefault().getSeparator();
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
    private String[] arrColumnNamesStoryPhotos = {"story_title", "slug", "story_visible_to", "description"
            , "datetime_story_created"
            , "item_title", "descr", "item_type"
            , "name_new", "title", "subtitle", "photo_type", "uploader", "creator", "visible_to", "city_name", "meta_date", "photo_date", "photo_time_shot"
            , "space_size", "space_size_medium", "space_size_thumb", "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model"
            , "meta_focal_length", "meta_focal_length_ff", "meta_iso", "meta_aperture", "meta_shutter_speed","meta_i_width", "meta_i_height","meta_orientation"
            , "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon"
            , "inc"
            , "date_inserted"
            , "username", "name", "surname", "resident", "date_joined", "avatar_path"
            , "story_id", "story_item_id"
    };
    private String sqlReadStoryPhotos = "SELECT s.id AS story_id, sp.id AS story_item_id, s.title AS story_title, s.slug, s.user_id, s.story_visible_to, s.description, " +
            " getDateDiffFromNow(s.date_inserted) AS datetime_story_created, " +
            " sp.item_title, sp.descr, sp.item_type, " +
            " pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.creator, pm.visible_to,  " +
            " DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date, DATE_FORMAT(pm.meta_date, '%M %Y') AS photo_date, DATE_FORMAT(pm.meta_date, '%d/%m/%Y - %H:%i:%S') AS photo_time_shot " +
            " , pm.space_size, pm.space_size_medium, pm.space_size_thumb, pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model " +
            " , pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, meta_aperture,  meta_shutter_speed, pm.meta_i_width, pm.meta_i_height, pm.meta_orientation " +
            " , pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon " +
            " , sp.inc "+
            " , usr.username, usr.name, usr.surname, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined, usr.avatar_path " +
            //, f.type, f.website, f.url_facebook, f.url_instagram, f.url_youtube, f.activities, f.image_top, f.image_logo, f.dateInsert, e.title, e.subtitle, DATE_FORMAT(e.dateFrom , '%W, %D %M %Y') AS formatedDateFrom , DATE_FORMAT(e.dateTo , '%W, %D %M %Y') AS formatedDateTo ,e.edition_description, DATE_FORMAT(f.dateInsert , '%D %M %Y') AS formatedDateUpdated  " +
            " FROM dbuser usr, photo_stories s , photo_stories_photo sp LEFT JOIN photo_meta pm ON sp.photo_id = pm.id " +
            " WHERE s.user_Id = usr.userId AND s.id = sp.story_id ";
    private UtilsDate utilsDate;
    private String sessionDateTime;
    private GenericView genericView;

    public StoriesView(RecordService recordService, PhotoStoryViewService photoStoryViewService, WeatherService weatherService) {
        this.recordService = recordService;
        this.photoStoryViewService = photoStoryViewService;
        this.weatherService = weatherService;
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
        strMember = event.getRouteParameters().get("member").orElse(STR_ALL_MEMBERS);
        strSlug = event.getRouteParameters().get("story").orElse(STR_ALL_TITLES);
        strCategory = event.getRouteParameters().get("category").orElse(STR_ALL_CATEGORIES);


        logger.info("strMember:"+strMember+" story:"+strSlug+" strCategory:"+strCategory);

        getUserClientInfo();

        if (strMember.equalsIgnoreCase("visitor-user")) {
            userId = 1;
            strUsername = "visitor-user";
        }

        verticalLayout.removeAll();

        verticalLayout.removeAll();

         if (strSlug == null || strSlug.isEmpty() || strSlug.equalsIgnoreCase(STR_ALL_TITLES)) {
            verticalLayout.add(loadHeader("Photo-Stories", "Collections of photos", ""));
            if (strMember == null || strMember.isEmpty() || strMember.equalsIgnoreCase(STR_ALL_MEMBERS)) {
                String sqlStories = sqlStoriesAll + sqlStoriesGroupBy;
                loadStoriesFromDb(sqlStories, arrColumnsStories, false);
            }else{
                String sqlMember = sqlMemberOfStories + " AND usr.username = '" + strMember + "' " + sqlMemberOfStoriesGroupBy;
                loadMemberOfStoriesFromDb(sqlMember, arrColumnsMemberStories, false);
                String sqlAlbums = sqlStoriesAll + " AND usr.username = '" + strMember + "' " + sqlStoriesGroupBy;
                loadStoriesFromDb(sqlAlbums, arrColumnsStories, false);
            }
        } else if (!strSlug.equalsIgnoreCase(STR_ALL_TITLES) && (strMember != null || !strMember.isEmpty() || !strMember.equalsIgnoreCase(STR_ALL_MEMBERS))) {
             logger.info("A strMember:"+strMember+" story:"+strSlug+" strCategory:"+strCategory);
            verticalLayout.add(loadHeader("Photo-Stories", "Collections of photos", ""));
/*            if (strMember == null || strMember.isEmpty() || strMember.equalsIgnoreCase(STR_ALL_MEMBERS)) {
                H3 titleAlbum = new H3(strTitle);
                titleAlbum.addClassNames(Width.FULL, TextAlignment.CENTER);
                verticalLayout.add(titleAlbum);
                String sqlAlbumsPhotoAll = sqlReadStoryPhotos + " ";
                sqlAlbumsPhotoAll = sqlAlbumsPhotoAll + " AND s.slug LIKE '" + strSlug + "' ";
                sqlAlbumsPhotoAll = sqlAlbumsPhotoAll + " ORDER BY sp.inc ASC, sp.date_inserted ASC ";
                loadStoryItemsFromDb(sqlAlbumsPhotoAll, arrColumnNamesStoryPhotos, false);
            } else {*/


                String sqlStoriesAll = sqlReadStoryPhotos + " ";
                sqlStoriesAll = sqlStoriesAll + " AND s.slug LIKE '" + strSlug + "' ";
                sqlStoriesAll = sqlStoriesAll + " ORDER BY sp.inc ASC, sp.date_inserted ASC ";
                loadStoryItemsFromDb(sqlStoriesAll, arrColumnNamesStoryPhotos, false);
            //}
        } else if (!strCategory.equalsIgnoreCase(STR_ALL_CATEGORIES)) {
            verticalLayout.add(loadHeader("Photo-Stories", strCategory, ""));
            H3 titleCategory = new H3(strCategory);
            titleCategory.addClassNames(Width.FULL, TextAlignment.CENTER);
            verticalLayout.add(titleCategory);

            String sqlAlbumsPhotoAll = sqlStoriesAll + " ";
            sqlAlbumsPhotoAll = sqlAlbumsPhotoAll + " AND sc.title LIKE '" + strCategory + "' ";
            sqlAlbumsPhotoAll = sqlAlbumsPhotoAll + sqlStoriesGroupBy;
            //sqlAlbumsPhotoAll = sqlAlbumsPhotoAll + " ORDER BY  s.date_inserted DESC ";
            loadStoriesFromDb(sqlAlbumsPhotoAll, arrColumnsStories, false);
        } else {
            verticalLayout.add(loadHeader("Photo-Stories", "else", ""));
            String sqlStoriesAll = sqlReadStoryPhotos + " ";
            sqlStoriesAll = sqlStoriesAll + " ORDER BY sp.inc ASC, sp.date_inserted ASC ";
            loadStoryItemsFromDb(sqlStoriesAll, arrColumnNamesStoryPhotos, false);
        }


        this.removeAll();
        this.add(verticalLayout);
        this.add(genericView.loadFooter(isMobile));

        logVisitorToDb();
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
        addClassName("stories-view");

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
                    Margin.NONE,
                    Padding.NONE,
                    Padding.Top.XSMALL,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        } else {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.SMALL,
                    Padding.Top.XSMALL,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        }


        this.setWidthFull();

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

    private void loadMemberOfStoriesFromDb(String sqlRead, String[] arrColumnNames, boolean isEditable) {


        VerticalLayout layoutMember = new VerticalLayout();
        layoutMember.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER
        );


        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER
        );
        horizontalLayout.addClassName("member-profile");


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);

        if (lstRecords == null || lstRecords.size() == 0) {

        } else if (lstRecords.size() == 1) {
            Record rec = lstRecords.get(0);
            String strNameOfUser = rec.getColumnData("username");
            String strName = rec.getColumnData("name");
            String strSurname = rec.getColumnData("surname");
            String strCountOfAlbums = rec.getColumnData("stories_count");
//            String strCountOfPhotosOfAlbums = rec.getColumnData("album_photo_count");
            String strMemberSince = rec.getColumnData("member_since");
            String strAvatarPath = rec.getColumnData("avatar_path");
            String strShortBio = rec.getColumnData("short_bio");

            Div divBio = new Div(strShortBio);
            divBio.addClassNames(Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Padding.LARGE
            );


            Image imgAvatar = genericView.getAvatarThumbImage(strAvatarPath, strNameOfUser, "120px", "120px");

            H3 objMember = new H3(strName+" "+strSurname);
            Div divUserName = new Div(strNameOfUser);
            Div divMemberSince = new Div("Member since " + strMemberSince);
            Div divAlbumsAndPhotos = new Div("Has " + strCountOfAlbums + " stories");
            layoutMember.add(imgAvatar, objMember,divUserName,  divMemberSince, divAlbumsAndPhotos);
            horizontalLayout.add(layoutMember,divBio);
        } else {

        }

        verticalLayout.add(horizontalLayout);
    }

    private void loadStoriesFromDb(String sqlRead, String[] arrColumnNames, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar;

        Div divGallery = new Div();
        divGallery.addClassName("gallery");

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {
            Record rec = lstRecords.get(r);
            divGallery.add(getStoriesFromDb(rec, isEditable));
        }
        verticalLayout.add(divGallery);
    }

    private StoryViewCard getStoriesFromDb(Record record, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar + subPathMedium;

        String strImagePath = strPath;
        logger.info(" strImagePath " + strImagePath);

        StoryViewCard storyViewCard = new StoryViewCard(record, strImagePath, isMobile, userId, strUsername, sessionCreation, hostname, publicIp, isEditable,
                recordService, photoStoryViewService, publicIp,
                VaadinSession.getCurrent().getSession().getId(),
                new UtilsDate().calcDateTimeFromLongInLDT(sessionCreation, "UTC"),
                shareService, shareMetricService, baseUrl);
        return storyViewCard;
    }

    private void loadStoryItemsFromDb(String sqlRead, String[] arrColumnNames, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar;

        Div divGallery = new Div();
        divGallery.addClassName("stories-view");

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);

        // Record a Full view for this story using data from the first item row
        if (!lstRecords.isEmpty() && photoStoryViewService != null) {
            try {
                String rawId = lstRecords.get(0).getColumnData("story_id");
                String slug  = lstRecords.get(0).getColumnData("slug");
                int storyId  = Integer.parseInt(rawId);
                Integer viewUserId = userId > 0 ? userId : null;
                LocalDateTime sdt = new UtilsDate().calcDateTimeFromLongInLDT(sessionCreation, "UTC");
                photoStoryViewService.recordView(storyId, slug, viewUserId, publicIp,
                        PhotoStoryViewService.TYPE_FULL,
                        VaadinSession.getCurrent().getSession().getId(), sdt);
            } catch (NumberFormatException ignored) {}
        }

        // Gather story metadata from the first row for the bottom action bar
        long likeCount = 0;
        long viewCount = 0;
        int detailStoryId = 0;
        String detailSlug     = "";
        String detailUsername = "";
        String detailName = "";
        String detailSurname = "";
        String detailTitle    = "";
        String detailDesc     = "";
        String strDateCreated = "";
        String strStoryNameOfUser ="";
        String strAvatarPath ="";
        if (!lstRecords.isEmpty()) {
            try {
                detailStoryId  = Integer.parseInt(lstRecords.get(0).getColumnData("story_id"));
                detailSlug     = lstRecords.get(0).getColumnData("slug");
                detailUsername = lstRecords.get(0).getColumnData("username");
                detailName = lstRecords.get(0).getColumnData("name");
                detailSurname = lstRecords.get(0).getColumnData("surname");
                strStoryNameOfUser = lstRecords.get(0).getColumnData("name") +" "+ lstRecords.get(0).getColumnData("surname");
                strAvatarPath = lstRecords.get(0).getColumnData("avatar_path");
                detailTitle    = lstRecords.get(0).getColumnData("story_title");
                detailDesc     = lstRecords.get(0).getColumnData("description");
                strDateCreated = lstRecords.get(0).getColumnData("datetime_story_created");
                if (photoStoryViewService != null) {
                    likeCount = photoStoryViewService.getLikeCount(detailStoryId);
                    viewCount = photoStoryViewService.getViewCount(detailStoryId);
                }



                H3 titleAlbum = new H3(detailTitle);
                titleAlbum.addClassNames(Width.FULL, TextAlignment.CENTER);

                HorizontalLayout layoutStoryTitle = new HorizontalLayout();
                layoutStoryTitle.addClassNames(Width.FULL,
                        Padding.NONE, Margin.NONE,
                        AlignItems.CENTER, JustifyContent.BETWEEN);

                HorizontalLayout layoutDate = new HorizontalLayout();
                layoutDate.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                        AlignItems.CENTER, JustifyContent.CENTER,
                        Margin.NONE,
                        Padding.NONE,
                        Gap.XSMALL,
                        //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                        //   Background.CONTRAST_5,
                        BorderRadius.NONE
                );
                Div divDateCreated = new Div(strDateCreated);
                divDateCreated.addClassNames(AlignItems.CENTER, TextAlignment.CENTER, JustifyContent.CENTER);
                layoutDate.add(FontAwesome.Solid.CALENDAR_DAY.create(), divDateCreated);

                Image imgAvatarSmall = genericView.getAvatarThumbImage(strAvatarPath, strStoryNameOfUser, "40px", "40px");
                Image imgAvatarMedium = genericView.getAvatarThumbImage(strAvatarPath, strStoryNameOfUser, "70px", "70px");

                AvatarItem avatarItemMe = new AvatarItem(strStoryNameOfUser, "", imgAvatarSmall);
                layoutStoryTitle.add(avatarItemMe, layoutDate);

                VerticalLayout layoutTitle = new VerticalLayout();
                layoutTitle.addClassNames(Width.FULL,
                        AlignItems.CENTER, JustifyContent.BETWEEN);
                layoutTitle.addClassName("story-item-title");
                layoutTitle.add(titleAlbum,layoutStoryTitle);

                divGallery.add(layoutTitle);

            } catch (NumberFormatException ignored) {}
        }

        for (int r = 0; r < lstRecords.size(); r++) {
            Record rec = lstRecords.get(r);
            divGallery.add(getStoryItemsFromDb(rec, isEditable));
        }

        verticalLayout.add(divGallery);

        final int    finalStoryId  = detailStoryId;
        final String finalSlug     = detailSlug;
        final String finalUsername = detailUsername;
        final String finalTitle    = detailTitle;
        final String finalDesc     = detailDesc;

        verticalLayout.add(getActions(likeCount, viewCount, finalStoryId, finalSlug,
                finalUsername, finalTitle, finalDesc));

        String sqlMember = sqlMemberOfStories + " AND usr.username = '" + strMember + "' " + sqlMemberOfStoriesGroupBy;
        loadMemberOfStoriesFromDb(sqlMember, arrColumnsMemberStories, false);
    }

    private StoryItemViewCard getStoryItemsFromDb(Record record, boolean isEditable) {
        strPath = DIR_PHOTOS_SERVER + dirChar + subPathMedium;

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");

        String strCityName = record.getColumnData("city_name");
        String strUploader = record.getColumnData("uploader");

        String strStoryTitle = record.getColumnData("story_title");

        String strImagePath = strPath + dirChar + strFileName;

        StoryItemViewCard storyItemViewCard = new StoryItemViewCard(record, strImagePath, isMobile, userId, strUsername, sessionCreation, hostname, publicIp, isEditable,
                recordService, weatherService);
        return storyItemViewCard;
    }

    private Details getStoryMemberInfo(Record record) {


        String strPhotoUserName = record.getColumnData("username");
        String strPhotoNameOfUser = record.getColumnData("username");
        String strPhotoUserResident = record.getColumnData("resident");
        String strAvatarPath = record.getColumnData("avatar_path");
        String strPhotoUserJoined = record.getColumnData("date_joined");


        Image imgAvatarSmall = genericView.getAvatarThumbImage(strAvatarPath, strPhotoUserName, "40px", "40px");
        Image imgAvatarMedium = genericView.getAvatarThumbImage(strAvatarPath, strPhotoUserName, "70px", "70px");


        HorizontalLayout layoutDetailsAvatarNActions = new HorizontalLayout();


        AvatarItem avatarItemMe = new AvatarItem(strPhotoNameOfUser, "", imgAvatarSmall);
        Details detailsMember = new Details();
        detailsMember.addClassNames(Width.FULL);
        detailsMember.addClassName("member-small");
        layoutDetailsAvatarNActions.add(avatarItemMe);
        detailsMember.setSummary(layoutDetailsAvatarNActions);
        AvatarItem avatarLargeItemMe = new AvatarItem(strPhotoNameOfUser, "@" + strPhotoUserName, imgAvatarMedium);

        HorizontalLayout layoutMemberInfo = new HorizontalLayout();
        layoutMemberInfo.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.AROUND,
                Margin.NONE,
                Padding.XSMALL,
                Gap.SMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );

        HorizontalLayout layoutMemberPhotoCount = new HorizontalLayout();
        layoutMemberPhotoCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divMemberPhotoCount = new Div("111");
        layoutMemberPhotoCount.add(FontAwesome.Regular.IMAGES.create(), divMemberPhotoCount);

        HorizontalLayout layoutMemberViewCount = new HorizontalLayout();
        layoutMemberViewCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divMemberViews = new Div("1");
        layoutMemberViewCount.add(FontAwesome.Regular.EYE.create(), divMemberViews);

/*        HorizontalLayout layoutLocationsCount = new HorizontalLayout();
        layoutLocationsCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divLocations = new Div(strPhotoUserResident);
        layoutLocationsCount.add(FontAwesome.Regular.COMPASS.create(), divLocations);*/

        HorizontalLayout layoutDateJoined = new HorizontalLayout();
        layoutDateJoined.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divDateJoined = new Div(strPhotoUserJoined);
        layoutDateJoined.add(VaadinIcon.CALENDAR_CLOCK.create(), divDateJoined); // FontAwesome.Regular.CALENDAR.create()
        layoutMemberInfo.add(layoutMemberPhotoCount, layoutMemberViewCount, layoutDateJoined);
        detailsMember.add(avatarLargeItemMe, layoutMemberInfo);

        return detailsMember;
    }

    private VerticalLayout loadFiltersCategories(String sqlRead, String[] arrColumnNames) {
        VerticalLayout filtersColumn = new VerticalLayout();
        if (isMobile) {
            filtersColumn.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE,
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

        VerticalLayout layoutFiltersType = new VerticalLayout();
        if (isMobile) {
            layoutFiltersType.addClassNames(
                    Overflow.HIDDEN,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE, Padding.NONE,
                    Gap.SMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE);
        } else {
            layoutFiltersType.addClassNames(
                    Overflow.HIDDEN,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE, Padding.NONE,
                    Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }
        layoutFiltersType.addClassName("side-layout-filters");

        List<Record> lstStoriesCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);

        ArrayList<String> lstCategories = new ArrayList<>();
        ArrayList<String> lstCategoriesDescriptions = new ArrayList<>();
        for (int r = 0; r < lstStoriesCategoriesRecs.size(); r++) {
            lstCategories.add(lstStoriesCategoriesRecs.get(r).getColumnData("cat_title"));
        }

//        RouteParam routeCategoryAll = new RouteParam("category", STR_ALL_CATEGORIES);
//        RouterLink linkPhotoCategoryAll = new RouterLink("All Categories", LearningsView.class, new RouteParameters(routeCategoryAll));
//        layoutFilters.add(linkPhotoCategoryAll);

        for (int c = 0; c < lstCategories.size(); c++) {
            String captionCategory = lstCategories.get(c);

            RouteParam routeCategory = new RouteParam("category", captionCategory);
            RouterLink linkStoriesCategory = new RouterLink(captionCategory, StoriesView.class, new RouteParameters(routeCategory));

            layoutFiltersType.add(linkStoriesCategory);
        }

        Div divFiltersTitle = new Div("Filter by Category");
        filtersColumn.add(divFiltersTitle, layoutFiltersType);

        return filtersColumn;
    }


    /**
     * Bottom bar for the full story view (shown after all story items):
     * [left: viewCount + likeButton] | [right: infoBar]
     */
    private HorizontalLayout getActions(long likeCount, long viewCount, int storyId, String slug,
                                        String username, String title, String description) {

        Span divViewCount = new Span(viewCount > 0 ? String.valueOf(viewCount) : "");

        HorizontalLayout viewsLayout = new HorizontalLayout();
        viewsLayout.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.NONE, Gap.XSMALL);
        Span viewsLabel = new Span(FontAwesome.Regular.EYE.create());
        viewsLayout.addClassNames(FontSize.XXSMALL);
        viewsLayout.add(viewsLabel,divViewCount);

        LikeButton btnLike = new LikeButton(likeCount);
//        btnLike.setTooltipText("Like It");
/*        btnLike.addLikeClickListener(e -> {
            if (photoStoryViewService != null && storyId > 0) {
                Integer likeUserId = userId > 0 ? userId : null;
                LocalDateTime sdt = new UtilsDate().calcDateTimeFromLongInLDT(sessionCreation, "UTC");
                photoStoryViewService.recordLike(storyId, slug, likeUserId, publicIp,
                        VaadinSession.getCurrent().getSession().getId(), sdt);
                btnLike.setCount(photoStoryViewService.getLikeCount(storyId));
            }
        });*/

/*        VerticalLayout likeLayout = new VerticalLayout();
        likeLayout.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.NONE, Gap.XSMALL);
        Span likeLabel = new Span("Like");
        likeLabel.addClassNames(FontSize.XXSMALL);
        likeLayout.add(btnLike, likeLabel);*/

        // ── Compose the single action bar ────────────────────────────────────
        String storyPublicUrl = baseUrl + "/stories/member/" + username + "/story/" + slug;
        ShareableResource storyResource = new ShareableResource(
                ShareType.PHOTO_STORY,
                String.valueOf(storyId),
                (title == null || title.isBlank()) ? "Photo Story" : title,
                (description == null || description.isBlank()) ? "" : description,
                "",
                storyPublicUrl
        );
        ShareBottomBar shareBar = new ShareBottomBar(storyResource, shareService, shareMetricService);

        shareBar.addComponent(viewsLayout);
        shareBar.addButton("Like", btnLike,
                () -> {
                    if (photoStoryViewService != null && storyId > 0) {
                        Integer likeUserId = userId > 0 ? userId : null;
                        LocalDateTime sdt = new UtilsDate().calcDateTimeFromLongInLDT(sessionCreation, "UTC");
                        photoStoryViewService.recordLike(storyId, slug, likeUserId, publicIp,
                                VaadinSession.getCurrent().getSession().getId(), sdt);
                        btnLike.setCount(photoStoryViewService.getLikeCount(storyId));
                    }
                }
                ,"btn-bar-share");
        shareBar.addShareItemMenu();

        HorizontalLayout layoutActions = new HorizontalLayout();
        layoutActions.addClassNames(
                Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.SMALL, Margin.NONE);

        layoutActions.add(shareBar);

        return layoutActions;
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
            strPath = strPath.replaceAll("'","");
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
