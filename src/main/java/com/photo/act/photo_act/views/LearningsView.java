package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.ResponsiveBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.HorizontalAlign;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.dto.LearningCategoryDto;
import com.photo.act.photo_act.dto.LearningDto;
import com.photo.act.photo_act.model.ShareType;
import com.photo.act.photo_act.model.ShareableResource;
import com.photo.act.photo_act.services.LearningService;
import com.photo.act.photo_act.services.LearningViewService;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.AvatarItem;
import com.photo.act.photo_act.views.components.FilterDestinationTypeCard;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.LikeButton;
import com.photo.act.photo_act.views.components.ShareBottomBar;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.MainLayout.*;

@AnonymousAllowed

@Route(value = "learnings") //":category?")
@RouteAlias(value = "learnings/category/:category?", layout = MainLayout.class)
@RouteAlias(value = "learnings/genre/:genre?", layout = MainLayout.class)
@RouteAlias(value = "learnings/tutor/:tutor?", layout = MainLayout.class)
@RouteAlias(value = "learnings/title/:title?", layout = MainLayout.class)
//@RouteAlias(value = "learnings/tutors/:tutor?", layout = MainLayout.class) // when tutors team
//@RouteAlias(value = "learnings/category/:category/tutor/:tutor?", layout = MainLayout.class)

//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class LearningsView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

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
    private String genre;
    private String tutor;
    private String title;
    String[] arrColLearningCategories = {"id", "cat_title", "cat_title_type", "cat_type", "cat_location_count", "cat_title_count", "cat_description_min", "cat_description_big"};

    public static String STR_ALL_TUTORS = "all-tutors";
    public static String STR_ALL_CATEGORIES = "all-categories";
    public static String STR_ALL_GENRES = "all-genres";
    public static String STR_ALL_TITLES = "all";

    public static String STR_ORDER_BY_NEWEST = "newest";
    public static String STR_ORDER_BY_OLDER = "older";

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
    private String dirChar = FileSystems.getDefault().getSeparator();

    private UtilsDate utilsDate;
    private String sessionDateTime;
    private String strUrlRequestToBeLogged;
    private GenericView genericView;

    private String strOS;
    private String strBrowser;
    private int intDefRecsOnPage = 20;

/*    private CheckboxGroup<String> checkboxCheckboxGroup;
    private CheckboxGroup<String> checkboxGenres;
    private CheckboxGroup<String> checkboxFormat;*/
    private Select<String> cmbCount;
    private Select<String> cmbSortBy;

    private String[] arrOrderByItems = {"Newest First", "Oldest First", "Most Liked", "Least Liked"};
    private String sqlOrderBy = " ORDER BY pm.date_inserted DESC";
    private String strDefOrderBy = arrOrderByItems[0];
    private VerticalLayout filtersContainer;
    private final LearningService learningService;
    private final LearningViewService learningViewService;
    private final ShareService shareService;
    private final ShareMetricService shareMetricService;
    private LocalDateTime sessionDateTimeLDT;

    public LearningsView(RecordService recordService, LearningService learningService,
                         LearningViewService learningViewService,
                         ShareService shareService, ShareMetricService shareMetricService) {
        this.recordService = recordService;
        this.learningService = learningService;
        this.learningViewService = learningViewService;
        this.shareService = shareService;
        this.shareMetricService = shareMetricService;
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
        category = event.getRouteParameters().get("category").orElse(STR_ALL_CATEGORIES);
        tutor = event.getRouteParameters().get("tutor").orElse(STR_ALL_TUTORS);
        title = event.getRouteParameters().get("title").orElse(STR_ALL_TITLES);
        genre = event.getRouteParameters().get("genre").orElse(STR_ALL_GENRES);

        getUserClientInfo();

        userId = 1;
        strUsername = "visitor-user";

        VerticalLayout layoutHeaderParameters;
        verticalLayout.removeAll();

        if (!category.equalsIgnoreCase(STR_ALL_CATEGORIES)) {
            layoutHeaderParameters = loadHeader("Learnings", "Lessons to improve our photography skills", "Learning Category", category);
            VerticalLayout layoutResults = loadResults(0);
            verticalLayout.add(layoutResults);
        } else if (!genre.equalsIgnoreCase(STR_ALL_GENRES)) {
            layoutHeaderParameters = loadHeader("Learnings", "Lessons to improve our photography skills", "Photo Genre", genre);
            VerticalLayout layoutResults = loadResults(0);
            verticalLayout.add(layoutResults);
        } else if (!title.equalsIgnoreCase(STR_ALL_TITLES)) {
            layoutHeaderParameters = loadHeader("Learnings", "Lessons to improve our photography skills", "","");
            VerticalLayout layoutResults = loadResults(0);
            verticalLayout.add(layoutResults);
        } else if (category.equalsIgnoreCase(STR_ALL_CATEGORIES) || genre.equalsIgnoreCase(STR_ALL_GENRES)) {
            layoutHeaderParameters = loadHeader("Learnings", "Lessons to improve our photography skills", "","");
            VerticalLayout layoutResults = loadResults(25);
            verticalLayout.add(layoutResults);
        } else {
            layoutHeaderParameters = loadHeader("Learnings", "Lessons to improve our photography skills", "","");
            logger.warn(category + "  " + tutor + "  " + genre);
        }


        this.removeAll();
        this.add(layoutHeaderParameters);

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
//        category = o;//beforeEvent.getRouteParameters().get("category").orElse("pictures");
    }

    private void constructUI() {
        this.addClassNames(Overflow.HIDDEN, Width.FULL,
                // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                Margin.NONE,
                Padding.NONE,
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

        filtersContainer = new VerticalLayout();
        filtersContainer.addClassNames(Width.FULL,
                Margin.NONE, Padding.NONE,
        Gap.XSMALL);

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


//        Html htmlTitle = new Html("<title>'photoact.net Network and Act around Photography'</title>");
//        Html htmlMeta = new Html("<meta name='description' content='Get reviews of the latest uploaded video learnings and books.'>");
//        verticalLayout.add(htmlTitle, htmlMeta);


    }

    private VerticalLayout loadResults(int intLimit) {
        List<LearningDto> learnings;
        int pageSize = intLimit > 0 ? intLimit : 25;

        if (!title.isEmpty() && !title.equalsIgnoreCase(STR_ALL_TITLES)) {
            learnings = learningService.searchLearnings(title, 0, pageSize).getContent();
        } else if (!tutor.isEmpty() && !tutor.equalsIgnoreCase(STR_ALL_TUTORS)) {
            learnings = learningService.getLearningsByTutorName(tutor);
        } else if (!category.isEmpty() && !category.equalsIgnoreCase(STR_ALL_CATEGORIES)) {
            learnings = learningService.getCategoryByTitle(category)
                    .map(cat -> learningService.getLearningsByCategory(cat.getId()))
                    .orElse(List.of());
        } else if (!genre.isEmpty() && !genre.equalsIgnoreCase(STR_ALL_GENRES)) {
            learnings = learningService.getCategoryByTitle(genre)
                    .map(cat -> learningService.getLearningsByGenre(cat.getId()))
                    .orElse(List.of());
        } else {
            learnings = learningService.getLatestLearnings(0, pageSize).getContent();
        }

        VerticalLayout layoutLearnings = new VerticalLayout();
        if (isMobile) {
            layoutLearnings.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE, Padding.NONE,
                    Gap.MEDIUM,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        } else {
            layoutLearnings.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.SMALL,
                    Gap.LARGE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        }
        layoutLearnings.addClassName("learnings-view");

        for (LearningDto dto : learnings) {
            layoutLearnings.add(getLearningItem(dto));
        }
        return layoutLearnings;
    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSectionCaption, String strSection) {

        this.strHeader = strHeader;

        VerticalLayout headerContainer = new VerticalLayout();
        if (isMobile) {
            headerContainer.addClassNames(
                    AlignItems.START, JustifyContent.BETWEEN,
                    Overflow.HIDDEN,// Width.FULL,
                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        } else {
            headerContainer.addClassNames(
                    AlignItems.START, JustifyContent.BETWEEN,
                    Overflow.HIDDEN, //Width.FULL,
                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }
        headerContainer.addClassName("header-layout");

        H1 header = new H1(strHeader);

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.START,
                LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.NONE, LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);



        filtersContainer.removeAll();

        filtersContainer.add(loadFiltersHeader("category", "Learnings"));

/*        layoutSortNCommands.add(cmbCount, cmbSortBy);
        layoutHeaderHorizontal.add(layoutFiltersAll, layoutSortNCommands);

        if (title.equalsIgnoreCase(STR_ALL_TITLES)) {
            headerContainerMaster.add(layoutHeaderHorizontal);
        }*/

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

        Div divLineB = new Div();
        divLineB.addClassNames(Border.BOTTOM, Width.FULL);

        headerContainer.add(header, subheader, divLine);
        headerContainer.add(filtersContainer);
        headerContainer.add(headerSection, headerSectionCaption, divLineB);

        return headerContainer;
    }

    private Div loadFiltersHeader(String nameUrlVariable, String strCaptionsCount) {
        Div filtersPanel = new Div();
        filtersPanel.addClassName("top-tall-filters-layout");

        List<LearningCategoryDto> categories = learningService.getAllCategories().stream()
                .filter(c -> !"not show".equalsIgnoreCase(c.getCatType()))
                .toList();

        for (LearningCategoryDto cat : categories) {
            FilterDestinationTypeCard card = new FilterDestinationTypeCard(
                    cat, nameUrlVariable, strPath, isMobile, userId, sessionCreation, publicIp,
                    strCaptionsCount, this);
            card.addClassName("top-tall-filters");
            filtersPanel.add(card);
        }
        return filtersPanel;
    }


    public VerticalLayout getLearningItem(LearningDto dto) {

        String strTitle = nvl(dto.getTitle());
        String strCategory = nvl(dto.getCategoryTitle());
        String strCatGenre = nvl(dto.getCatGenreTitle());
        String strFormat = nvl(dto.getFormat());
        String strDuration = nvl(dto.getDuration());
        String strPages = nvl(dto.getPages());
        String strTutor = nvl(dto.getTutorName());
        String strYearPublished = dto.getPublished() != null ? String.valueOf(dto.getPublished().getYear()) : "";
        String strUserIdPost = dto.getUserIdPost() != null ? dto.getUserIdPost().toString() : "";
        String strUsername = "";
        String strNameOfUser = "";
        String strMemberSince = "";
        String strAvatarPath = null;
        String strImage = nvl(dto.getPicture());
        String dateCreated = formatDateAgo(dto.getDateInsert());

        Div divTutor = new Div();
        divTutor.addClassNames(TextColor.SECONDARY, TextAlignment.CENTER);
        divTutor.setVisible(false);
        if (!strTutor.equalsIgnoreCase("null") && !strTutor.isEmpty()) {
            divTutor.setText(strTutor);
            divTutor.setVisible(true);
        }


//        String strTutorTeam = record.getColumnData("learnings_team_id");
//        Div divTutorTeam = new Div();
//        divTutorTeam.addClassName(TextColor.SECONDARY);
//        divTutorTeam.setVisible(false);
//        if (!strTeamName.equalsIgnoreCase("null") && !strTeamName.isEmpty()) {
//            divTutorTeam.setText(strTeamName);
//            divTutorTeam.setVisible(true);
//        }

        DIR_PHOTOS_SERVER = genericView.getAppProps(PROP_PHOTOS);
        strPath = DIR_PHOTOS_SERVER + dirChar;


        if (!strImage.equalsIgnoreCase("null") && !strImage.equalsIgnoreCase("")) {
            strImage = strPath + strImage;
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

        RouteParam routeTutor = new RouteParam("tutor", strTutor);
        RouterLink linkLearningTutor = new RouterLink(strTutor, LearningsView.class, new RouteParameters(routeTutor));

//        RouteParam routeTitle = new RouteParam("title", strTitle);
//        RouterLink linkLearningTitle = new RouterLink(strTitle, LearningsView.class, new RouteParameters(routeTitle));

        H4 titleName = new H4(strTitle);
        titleName.addClassName(TextColor.SECONDARY);

//        SimpleDateFormat toui = new SimpleDateFormat("dd/MM/yyyy");
//        SimpleDateFormat fromdb = new SimpleDateFormat("yyyy-MM-dd");

//        try {
//            strDate = toui.format(fromdb.parse(dt));
//        } catch (ParseException e) {
//            logger.error(e.getMessage());
//        }

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
        layoutPostTitle.addClassName("item-title-bar");

        VerticalLayout layoutLearningInfo = new VerticalLayout();
        if (isMobile) {
            layoutLearningInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.XSMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.NONE
            );
        } else {
            layoutLearningInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.MEDIUM,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_5,
                    BorderRadius.NONE
            );
            layoutLearningInfo.addClassName("item-panel");
        }

        HorizontalLayout layoutImageSmall = new HorizontalLayout();
        layoutImageSmall.addClassNames(Padding.SMALL, Background.CONTRAST_70, Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE,
                BoxShadow.SMALL);

        HorizontalLayout layoutImageNormal = new HorizontalLayout();
        layoutImageNormal.addClassNames(Padding.SMALL, Background.CONTRAST_70, Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE,
                BoxShadow.SMALL);

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

            Image imgSmall = new Image(imageResource, "image");
            imgSmall.setMaxHeight("240px");
            imgSmall.addClassNames(BorderRadius.LARGE);
            layoutImageSmall.add(imgSmall);

            Image imgNormal = new Image(imageResource, "image");
            imgNormal.setMaxHeight("440px");
            imgNormal.addClassNames(BorderRadius.LARGE);
            layoutImageNormal.add(imgNormal);
        }

        Anchor linkTutor = new Anchor();
        linkTutor.add(FontAwesome.Solid.LINK.create());
        linkTutor.setVisible(false);
        // linkTutor.getStyle().setColor(strColorExternalweb);
        //  linkTutor.setClassName("lazy-result-line-button");

        String strUrlTutorExt = nvl(dto.getTutorWebsite());
        if (!strUrlTutorExt.equalsIgnoreCase("null") && !strUrlTutorExt.equalsIgnoreCase("")) {

            // linkTutor.setText("Website");
            linkTutor.setVisible(true);
            linkTutor.setHref(strUrlTutorExt);
            linkTutor.setTarget("_blank");
        }

        Anchor linkTutorYt = new Anchor();
        linkTutorYt.add(FontAwesome.Brands.YOUTUBE.create());
        // linkTutorYt.getStyle().setColor(strColorExternalweb);
        // linkTutorYt.setClassName("lazy-result-line-button");
        linkTutorYt.setVisible(false);
        String strUrlTutorYt = nvl(dto.getTutorUrlYt());
        if (!strUrlTutorYt.equalsIgnoreCase("null") && !strUrlTutorYt.equalsIgnoreCase("")) {

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
        String strUrlTutorWikipedia = nvl(dto.getTutorUrlWikipedia());
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
        String strUrlTutorInsta = nvl(dto.getTutorUrlInsta());
        if (!strUrlTutorInsta.equalsIgnoreCase("null") && !strUrlTutorInsta.equalsIgnoreCase("")) {

            // linkTutorInsta.setText("Instagram");
//            strUrlTutorInsta = "https://www.instagram.com/"+ strUrlTutorInsta;
            linkTutorInsta.setHref(strUrlTutorInsta);
            linkTutorInsta.setTarget("_blank");
            linkTutorInsta.setVisible(true);
        }

        Anchor link1InNewTab = new Anchor();

        String strUrl = nvl(dto.getUrl());
        String strYouTubeVideo = "https://www.youtube.com/watch?v=";
        String strVideoOnly = strUrl.replace(strYouTubeVideo, "");

        String youtubeEmbedded = "<div><iframe class='video-iframe' src='https://www.youtube.com/embed/" + strVideoOnly + "' title='" + strTitle + "'  allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share'  allowFullScreen></iframe></div>";

        Html htmlVideoSmall = new Html(youtubeEmbedded);
        htmlVideoSmall.setHtmlContent(youtubeEmbedded);
//        htmlVideo.addClassNames(Padding.SMALL, Margin.MEDIUM, Background.CONTRAST_60, BorderRadius.LARGE);
        htmlVideoSmall.setClassName("video-container-small");

        VerticalLayout layoutSourceCardSmall = new VerticalLayout();
        layoutSourceCardSmall.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.AROUND,
                Margin.NONE,
                Padding.MEDIUM,
                Gap.MEDIUM,
                TextColor.SECONDARY

        );


        HorizontalLayout layoutCategorySmallAll = new HorizontalLayout();
        layoutCategorySmallAll.addClassNames(
                //  Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        HorizontalLayout layoutCategorySmall = new HorizontalLayout();
        layoutCategorySmall.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        H5 spCategorySmall = new H5(strCategory);
        spCategorySmall.addClassNames(AlignItems.CENTER, TextAlignment.CENTER, JustifyContent.CENTER);
        layoutCategorySmall.add(FontAwesome.Solid.TAG.create(), spCategorySmall);

        if (strCategory == null || strCategory.isEmpty() || strCategory.equalsIgnoreCase("null")) {
            layoutCategorySmallAll.setVisible(false);
        }
        layoutCategorySmallAll.add(layoutCategorySmall);

        HorizontalLayout layoutCategory2SmallAll = new HorizontalLayout();
        layoutCategory2SmallAll.addClassNames(
                //  Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        HorizontalLayout layoutCategory2Small = new HorizontalLayout();
        layoutCategory2Small.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        H5 spCategory2Small = new H5(strCatGenre);
        spCategory2Small.addClassNames(AlignItems.CENTER, TextAlignment.CENTER, JustifyContent.CENTER);
        layoutCategory2Small.add(FontAwesome.Solid.TAG.create(), spCategory2Small);

        layoutCategory2SmallAll.add(layoutCategory2Small);
        if (strCatGenre == null || strCatGenre.isEmpty() || strCatGenre.equalsIgnoreCase("null")) {
            layoutCategory2SmallAll.setVisible(false);
        }

//        Image imgAvatarSmall = genericView.getAvatarImage(strAvatarPath, strNameOfUser, "40px", "40px");
//        AvatarItem avatarItemSmall = new AvatarItem(strNameOfUser, "", imgAvatarSmall);
//        avatarItemSmall.addClassNames(Width.FULL, AlignItems.STRETCH, JustifyContent.BETWEEN);
//        Span spAvatarItemSmall = new Span(avatarItemSmall);

        HorizontalLayout layoutTutorSmallAll = new HorizontalLayout();
        StreamResource iconTutorSmall = new StreamResource("man-user-circle-black-icon.svg",
                () -> getClass().getResourceAsStream("/icons/man-user-circle-black-icon.svg"));
        SvgIcon svgTutorSmall = new SvgIcon(iconTutorSmall);
        Div imgPersonSmall = new Div(svgTutorSmall);

        Div divTutorInfoSmall = new Div();
        divTutorInfoSmall.setText(strTutor);
        divTutorInfoSmall.addClassName(TextColor.SECONDARY);
        if (strTutor.equalsIgnoreCase("null") || strTutor.isEmpty()) {

            divTutorInfoSmall.setVisible(false);
        }
        layoutTutorSmallAll.add(svgTutorSmall, divTutorInfoSmall);

        Div divYearPublished = new Div();
        divYearPublished.addClassName(TextColor.SECONDARY);
        divYearPublished.setVisible(false);
        if (!strYearPublished.equalsIgnoreCase("null") && !strYearPublished.isEmpty()) {
            divYearPublished.setText("Year Published: " + strYearPublished);
            divYearPublished.setVisible(true);
        }

        StreamResource iconInfo = new StreamResource("info-circle-line-icon.svg",
                () -> getClass().getResourceAsStream("/icons/info-circle-line-icon.svg"));
        SvgIcon svgInfo = new SvgIcon(iconInfo);

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
        layoutExtLinks.add(linkTutor, linkTutorWikipedia, linkTutorInsta, linkTutorYt);

        Div imgInfo = new Div(svgInfo);
        Div divFormat = new Div();
        if (strFormat.equalsIgnoreCase("YouTube")) {
            if (!strDuration.equalsIgnoreCase("null") && !strDuration.equalsIgnoreCase("")) {
                divFormat.setText(strFormat + "(" + strDuration + ")");
            } else {
                divFormat.setText(strFormat);
            }
        } else if (strFormat.equalsIgnoreCase("book")) {
            layoutImageSmall.setMaxWidth("430px");
            if (!strPages.equalsIgnoreCase("null") && !strPages.equalsIgnoreCase("")) {
                divFormat.setText("Book (" + strPages + " pages)");
            } else {
                divFormat.setText("book");
            }
        } else if (strFormat.equalsIgnoreCase("Url with Free e-book")) {
            layoutImageSmall.setMaxWidth("430px");
            if (!strPages.equalsIgnoreCase("null") && !strPages.equalsIgnoreCase("")) {
                divFormat.setText("E-Book (" + strPages + " pages)");
            } else {
                divFormat.setText("E-Book");
            }
        } else {
            layoutImageSmall.setMaxWidth("430px");
            if (!strPages.equalsIgnoreCase("null") && !strPages.equalsIgnoreCase("")) {
                divFormat.setText(strFormat + "(" + strPages + " pages)");
            } else {
                divFormat.setText(strFormat);
            }
        }

        VerticalLayout layoutIDDataSmall = new VerticalLayout();
        layoutIDDataSmall.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, //Width.FULL,
//                Background.TINT_10,
                BorderRadius.LARGE,
                Margin.NONE, Padding.MEDIUM,
                Gap.SMALL
//                BoxShadow.XSMALL
        );
        layoutIDDataSmall.addClassName("item-id-info");
        layoutIDDataSmall.add(imgPersonSmall, divTutor, layoutExtLinks, divFormat, divYearPublished);

        Div dayUpdatedLabelSmall = new Div("Info Posted: ");
        dayUpdatedLabelSmall.addClassName(TextColor.SECONDARY);

        HorizontalLayout layoutDateSmallAll = new HorizontalLayout();
        layoutDateSmallAll.addClassNames(
                //  Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        HorizontalLayout layoutDateSmall = new HorizontalLayout();
        layoutDateSmall.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        H4 divDateCreatedSmall = new H4(dateCreated);
        divDateCreatedSmall.addClassNames(AlignItems.CENTER, TextAlignment.CENTER, JustifyContent.CENTER);
        layoutDateSmall.add(FontAwesome.Solid.CALENDAR_DAY.create(), divDateCreatedSmall);

        layoutDateSmallAll.add(layoutDateSmall);

        VerticalLayout layoutItemInfoSmall = new VerticalLayout();
        layoutItemInfoSmall.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, //Width.FULL,
//                Background.TINT_10,
                BorderRadius.LARGE,
                Margin.NONE, Padding.MEDIUM,
                Gap.SMALL
//                BoxShadow.XSMALL
        );
        layoutItemInfoSmall.addClassName("item-id-info");
        layoutItemInfoSmall.add(imgInfo, dayUpdatedLabelSmall, layoutDateSmallAll, layoutCategorySmallAll, layoutCategory2SmallAll, layoutTutorSmallAll);
        layoutSourceCardSmall.setMaxWidth("310px");
        layoutSourceCardSmall.add(layoutIDDataSmall, layoutItemInfoSmall);

        Div layoutDataSmall = new Div();
        layoutDataSmall.addClassNames(
                Display.FLEX, FlexDirection.COLUMN,
                FlexDirection.Breakpoint.Medium.ROW, Gap.MEDIUM,

                AlignItems.CENTER, JustifyContent.AROUND,
                Margin.MEDIUM, Padding.LARGE,
                Width.FULL);
        layoutDataSmall.add(layoutImageSmall, htmlVideoSmall, layoutSourceCardSmall);


        StreamResource iconTutor = new StreamResource("man-user-circle-black-icon.svg",
                () -> getClass().getResourceAsStream("/icons/man-user-circle-black-icon.svg"));
        SvgIcon svgTutor = new SvgIcon(iconTutor);

        Div imgPerson = new Div(svgTutor);

        HorizontalLayout layoutSourceCardNormal = new HorizontalLayout();
        layoutSourceCardNormal.addClassNames(
                Overflow.HIDDEN, //Width.FULL,
                AlignItems.START, JustifyContent.CENTER,
                Margin.LARGE,
                Padding.NONE,
                Gap.SMALL,
                TextColor.SECONDARY
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                Background.TINT_10
//                BorderColor.CONTRAST_10,
//                Border.ALL,  BorderRadius.LARGE
        );

        VerticalLayout layoutIDDataNormal = new VerticalLayout();
        layoutIDDataNormal.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, //Width.FULL,
//                Background.TINT_10,
                BorderRadius.LARGE,
                Margin.NONE, Padding.MEDIUM,
                Gap.SMALL
//                BoxShadow.XSMALL
        );
        layoutIDDataNormal.add(imgPerson, divTutor, layoutExtLinks, divFormat, divYearPublished);

        HorizontalLayout layoutCategoryNormal = new HorizontalLayout();
        H5 spCategoryNormal = new H5(strCategory);
        spCategoryNormal.addClassNames(AlignItems.CENTER, TextAlignment.CENTER, JustifyContent.CENTER);
        layoutCategoryNormal.add(FontAwesome.Solid.TAG.create(), spCategoryNormal);
        if (strCategory == null || strCategory.isEmpty() || strCategory.equalsIgnoreCase("null")) {
            layoutCategoryNormal.setVisible(false);
        }

        HorizontalLayout layoutCategory2Normal = new HorizontalLayout();
        H5 spCategory2Normal = new H5(strCatGenre);
        spCategory2Normal.addClassNames(AlignItems.CENTER, TextAlignment.CENTER, JustifyContent.CENTER);
        layoutCategory2Normal.add(FontAwesome.Solid.TAG.create(), spCategory2Normal);
        if (strCatGenre == null || strCatGenre.isEmpty() || strCatGenre.equalsIgnoreCase("null")) {
            layoutCategory2Normal.setVisible(false);
        }

        Div dayUpdatedLabelNormal = new Div("Info Created: ");
        dayUpdatedLabelNormal.addClassName(TextColor.SECONDARY);

        Div dayUpdatedNormal = new Div(dateCreated);
        dayUpdatedNormal.getElement().getThemeList().add("badge contrast");

        Details detUserPostedNormal = getMemberDetail(strUserIdPost,
                strAvatarPath, strUsername, strNameOfUser, strMemberSince);
        detUserPostedNormal.getStyle().setBackgroundColor("#f3f3f3");

        VerticalLayout layoutItemInfoNormal = new VerticalLayout();
        layoutItemInfoNormal.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, //Width.FULL,
//                Background.TINT_10,
                BorderRadius.LARGE,
                Margin.NONE, Padding.MEDIUM,
                Gap.SMALL
//                BoxShadow.XSMALL
        );
        layoutItemInfoNormal.add(imgInfo, dayUpdatedLabelNormal, dayUpdatedNormal, layoutCategoryNormal, layoutCategory2Normal);

        layoutIDDataNormal.setMinWidth("280px");
        layoutItemInfoNormal.setMinWidth("280px");
        layoutSourceCardNormal.addClassName("item-id-info");
        layoutSourceCardNormal.add(layoutIDDataNormal, layoutItemInfoNormal);

        Html htmlVideoNormal = new Html(youtubeEmbedded);
        htmlVideoNormal.setHtmlContent(youtubeEmbedded);
        htmlVideoNormal.setClassName("video-container-normal");

        HorizontalLayout layoutDataNormal = new HorizontalLayout();
        layoutDataNormal.addClassNames(AlignItems.CENTER, JustifyContent.EVENLY,
                Width.FULL);

        layoutDataNormal.add(layoutImageNormal, htmlVideoNormal);

        if (!strUrl.equalsIgnoreCase("null") && !strUrl.equalsIgnoreCase("")) {
            if (strFormat.equalsIgnoreCase("YouTube")) {
                link1InNewTab.setVisible(false);
                htmlVideoSmall.setVisible(true);
                layoutImageSmall.setVisible(false);

                htmlVideoNormal.setVisible(true);
                layoutImageNormal.setVisible(false);
            } else {
                link1InNewTab.setText(strUrl);
                //link1InNewTab.setTarget(festUrl);
                link1InNewTab.setHref(strUrl);
                link1InNewTab.setTarget("_blank");
                //link1InNewTab.getElement().setAttribute("target", "_blank");
                link1InNewTab.setVisible(true);
                htmlVideoSmall.setVisible(false);
                layoutImageSmall.setVisible(true);

                htmlVideoNormal.setVisible(false);
                layoutImageNormal.setVisible(true);
            }
        } else {
            link1InNewTab.setVisible(false);
            htmlVideoSmall.setVisible(false);
            layoutImageSmall.setVisible(true);

            htmlVideoNormal.setVisible(false);
            layoutImageNormal.setVisible(true);
        }

        logger.info("  htmlVideoSmall  " + htmlVideoSmall.isVisible());

        HorizontalLayout layoutPostRelated = new HorizontalLayout();
        layoutPostRelated.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextColor.SECONDARY,
                Padding.NONE, Margin.NONE, BorderRadius.LARGE);


        if (strFormat.equalsIgnoreCase("Url with Free e-book")) {
            Div lblGotoUrl = new Div("Click to go to author's site, to download the e-book.");
            if (strUrl != null && !strUrl.isEmpty()) {
                Anchor linkSourceToNewTab = new Anchor();
                String strUrlShorter = "";
                if (strUrl.trim().length() > 50) {
                    strUrlShorter = strUrl.substring(0, 46) + "...";
                }
                linkSourceToNewTab.setText(strUrlShorter);
                linkSourceToNewTab.setHref(strUrl);
                linkSourceToNewTab.setTarget("_blank");
                linkSourceToNewTab.setVisible(true);


                layoutPostRelated.add(lblGotoUrl, linkSourceToNewTab);
            }
        }

        String strDescription = nvl(dto.getDescription());

//        parDescription.addClassNames(TextColor.TERTIARY, FontSize.MEDIUM, Padding.MEDIUM);
        if (!strDescription.equalsIgnoreCase("null") && !strDescription.isEmpty()) {
//            parDescription.setText(strDescription);
        } else {
            strDescription = " Overview of " + strTitle;
        }


        VerticalLayout layoutSourceReviewSmall = getFormattedText(strDescription, true);
        layoutSourceReviewSmall.addClassNames(TextColor.SECONDARY, FontSize.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER
        );
        layoutSourceReviewSmall.addClassName("item-description");

        // ── Resolve learning id and view counts ──────────────────────────────
        int learningId = dto.getId() != null ? dto.getId().intValue() : 0;
        Integer viewUserId = userId > 0 ? userId : null;

        String learningPublicUrl = baseUrl + "/learnings/title/" + strTitle;
        ShareableResource learningResource = new ShareableResource(
                ShareType.LEARNING,
                String.valueOf(learningId),
                strTitle,
                strDescription,
                "",
                learningPublicUrl
        );

        RouteParam routeTitle = new RouteParam("title", strTitle);

        if (title.equalsIgnoreCase(STR_ALL_TITLES) || title.isEmpty()) {
            // ── List mode: record List view and build list ButtonBar ──────────
            if (learningViewService != null && learningId > 0) {
                learningViewService.recordView(learningId, dto.getSlug(), viewUserId, publicIp,
                        LearningViewService.TYPE_LIST, sessionid, sessionDateTimeLDT);
            }

            long listViewCount = learningViewService != null && learningId > 0
                    ? learningViewService.getViewCountByType(learningId, LearningViewService.TYPE_LIST) : 0;
            long fullViewCount = learningViewService != null && learningId > 0
                    ? learningViewService.getViewCountByType(learningId, LearningViewService.TYPE_FULL) : 0;
            long likeCount = learningViewService != null && learningId > 0
                    ? learningViewService.getLikeCount(learningId) : 0;

            HorizontalLayout layoutAggregateInfo = getViewAggregateInfo( fullViewCount);

            LikeButton likeButton = new LikeButton(likeCount);
            final int finalLearningId = learningId;
            final String finalSlug = dto.getSlug();

            ShareBottomBar listBar = new ShareBottomBar(learningResource, shareService, shareMetricService);
            listBar.addClassName("btn-bar-wrapper");

            listBar.addComponent(layoutAggregateInfo);

            listBar.addButton("Like it!", likeButton, () -> {
                if (learningViewService != null && finalLearningId > 0) {
                    learningViewService.recordLike(finalLearningId, finalSlug, viewUserId, publicIp,
                            sessionid, sessionDateTimeLDT);
                    likeButton.setCount(learningViewService.getLikeCount(finalLearningId));
                }
            }, "btn-bar-share");

            listBar.addButton("View", FontAwesome.Solid.ARROW_RIGHT.create(),
                    () -> getUI().ifPresent(ui ->
                            ui.navigate(LearningsView.class, new RouteParameters(routeTitle))),
                    "btn-bar-view");

            listBar.addShareItemMenu();

            layoutLearningInfo.add(layoutPostTitle, layoutDataSmall, layoutSourceReviewSmall,
                    buildActionBar(listBar));

        } else {
            // ── Detail mode: record Full view and build detail ButtonBar ──────
            if (learningViewService != null && learningId > 0) {
                learningViewService.recordView(learningId, dto.getSlug(), viewUserId, publicIp,
                        LearningViewService.TYPE_FULL, sessionid, sessionDateTimeLDT);
            }

            long likeCount = learningViewService != null && learningId > 0
                    ? learningViewService.getLikeCount(learningId) : 0;

            LikeButton likeButton = new LikeButton(likeCount);
            final int finalLearningId = learningId;
            final String finalSlug = dto.getSlug();

            VerticalLayout layoutSubTabs = getSubTabs("Learning", strTitle, dto);

            VerticalLayout layoutReviewNormal = getFormattedText(strDescription, false);
            layoutReviewNormal.addClassNames(FontSize.MEDIUM,
                    Margin.SMALL,
                    Padding.MEDIUM,
                    Gap.SMALL,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
            layoutReviewNormal.addClassName("item-description");

            Div divRelated = new Div(new Text(""));

            Details detUserPosted = getMemberDetail(strUserIdPost,
                    strAvatarPath, strUsername, strNameOfUser, strMemberSince);
            detUserPosted.getStyle().setBackgroundColor("#f3f3f3");

            Span spUserPoster = new Span(detUserPosted);

            ShareBottomBar detailBar = new ShareBottomBar(learningResource, shareService, shareMetricService);
            detailBar.addClassName("btn-bar-wrapper");

            detailBar.addButton("Like it!", likeButton, () -> {
                if (learningViewService != null && finalLearningId > 0) {
                    learningViewService.recordLike(finalLearningId, finalSlug, viewUserId, publicIp,
                            sessionid, sessionDateTimeLDT);
                    likeButton.setCount(learningViewService.getLikeCount(finalLearningId));
                }
            }, "btn-bar-share");

/*            detailBar.addButton("Comment", VaadinIcon.COMMENT.create(), () -> {
            }, "btn-bar-comment");

            detailBar.addButton("Save to list", VaadinIcon.BOOKMARK.create(), () -> {
            }, "btn-bar-bookmark");

            detailBar.addButton("Upload related photos", VaadinIcon.UPLOAD.create(), () -> {
            }, "btn-bar-upload");*/

            detailBar.addShareItemMenu();

            layoutLearningInfo.add(layoutPostTitle, layoutDataNormal, layoutSourceCardNormal, layoutReviewNormal,
                    divRelated, spUserPoster, buildActionBar(detailBar));
        }

        return layoutLearningInfo;
    }

    private HorizontalLayout buildActionBar(ShareBottomBar bar) {
        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER,
                Padding.XSMALL, Margin.NONE);
        wrapper.addClassName("learning-bottom-bar");
        wrapper.add(bar);
        return wrapper;
    }

    private HorizontalLayout getViewAggregateInfo( long fullViews) {
        HorizontalLayout layoutPhotosInfo = new HorizontalLayout();
        layoutPhotosInfo.addClassNames(
                Overflow.HIDDEN,
                AlignItems.CENTER, JustifyContent.EVENLY,
                Margin.NONE,
                Padding.SMALL,
                Gap.SMALL,
                TextColor.TERTIARY
        );

/*        HorizontalLayout layoutListViews = new HorizontalLayout();
        layoutListViews.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.XSMALL, Gap.XSMALL, BorderRadius.NONE);
        layoutListViews.add(FontAwesome.Regular.EYE.create(),
                new Span(fullViews > 0 ? String.valueOf(fullViews) : ""));*/

        HorizontalLayout layoutFullViews = new HorizontalLayout();
        layoutFullViews.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.XSMALL, Gap.XSMALL, BorderRadius.NONE);
        layoutFullViews.add(VaadinIcon.EYE.create(),
                new Span(fullViews > 0 ? String.valueOf(fullViews) : ""));

/*        HorizontalLayout layoutLikes = new HorizontalLayout();
        layoutLikes.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.XSMALL, Gap.XSMALL, BorderRadius.NONE);
        layoutLikes.add(FontAwesome.Solid.HEART.create(),
                new Span(likes > 0 ? String.valueOf(likes) : ""));*/

        layoutPhotosInfo.add( layoutFullViews);
        return layoutPhotosInfo;
    }

    private VerticalLayout loadFiltersColumn(String sqlRead, String[] arrColumnNames) {
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

        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);

        ArrayList<String> lstCategories = new ArrayList<>();
        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {
            lstCategories.add(lstLearningCategoriesRecs.get(r).getColumnData("cat_type"));
        }

        for (int c = 0; c < lstCategories.size(); c++) {
            String captionCategory = lstCategories.get(c);
            RouteParam routeCategory = new RouteParam("category", captionCategory);
            RouterLink linkPhotoCategory = new RouterLink(captionCategory, LearningsView.class, new RouteParameters(routeCategory));
            layoutFiltersType.add(linkPhotoCategory);
        }


//        StreamResource iconComments = new StreamResource("comments.svg",
//                () -> getClass().getResourceAsStream("/icons/comments.svg"));
//        SvgIcon svgComments = new SvgIcon(iconComments);
        Button btnSuggestLearning = new Button("Suggest a Learning");
        btnSuggestLearning.addClassName("btn-suggest");
//        btnSuggestLearning.setIcon(svgComments);
        btnSuggestLearning.addClickListener(click -> {

        });


        Div divFiltersTitle = new Div("Filter by Category");
        filtersColumn.add(btnSuggestLearning, divFiltersTitle, layoutFiltersType);

        return filtersColumn;
    }

    private void filter(String sqlOrderBy) {
        verticalLayout.removeAll();
        title = STR_ALL_TITLES;


        String strWhereSubClause = "";

        List<String> lstSelectedGenres = List.of(); //checkboxGenres.getSelectedItems();

        List<String> lstSelected = List.of(); //checkboxCheckboxGroup.getSelectedItems();

        List<String> lstSelectedFormat = List.of(); //checkboxFormat.getSelectedItems();

        if (!lstSelected.isEmpty() || !lstSelectedGenres.isEmpty()) {

            strWhereSubClause = " AND ( ";
            for (int s = 0; s < lstSelected.size(); s++) {

                String strCategory = lstSelected.get(s); //  OR lc2.cat_type LIKE '" + strCategory + "')
                strWhereSubClause = strWhereSubClause + "  lc.cat_type LIKE '" + strCategory + "'  ";
                if (s < lstSelected.size() - 1) {
                    strWhereSubClause = strWhereSubClause + " OR ";
                }
            }
            if (lstSelectedGenres.size() > 0) {

                if (lstSelected.size() > 0) {
                    strWhereSubClause = strWhereSubClause + " OR ";
                }

/*                for (int s = 0; s < lstSelectedGenres.size(); s++) {
                    String strCategory = lstSelectedGenres.get(s); //  lc.cat_type LIKE '" + strCategory + "' OR
                    strWhereSubClause = strWhereSubClause + "   lc2.cat_title LIKE '" + strCategory + "' ";
                    if (s < lstSelectedGenres.size() - 1) {
                        strWhereSubClause = strWhereSubClause + " OR ";
                    }
                }*/
            }
            strWhereSubClause = strWhereSubClause + " ) ";
        }

        if (!lstSelectedFormat.isEmpty()) {
            strWhereSubClause = strWhereSubClause + " AND ( ";
            for (int s = 0; s < lstSelectedFormat.size(); s++) {

                String strFormat = lstSelectedFormat.get(s); //  OR lc2.cat_type LIKE '" + strCategory + "')
                strWhereSubClause = strWhereSubClause + "  l.format LIKE '" + strFormat + "'  ";
                if (s < lstSelectedFormat.size() - 1) {
                    strWhereSubClause = strWhereSubClause + " OR ";
                }
            }
            strWhereSubClause = strWhereSubClause + " ) ";
        }


        List<LearningDto> learnings = learningService.getLatestLearnings(0, Integer.parseInt(cmbCount.getValue())).getContent();

        VerticalLayout layoutLearnings = new VerticalLayout();
        if (isMobile) {
            layoutLearnings.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE, Padding.NONE,
                    Gap.MEDIUM,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        } else {
            layoutLearnings.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.SMALL,
                    Gap.LARGE,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
        }
        layoutLearnings.addClassName("learnings-view");

        logger.info(" record size: " + learnings.size());
        for (LearningDto dto : learnings) {
            layoutLearnings.add(getLearningItem(dto));
        }

        VerticalLayout layoutResults = layoutLearnings;
        verticalLayout.add(layoutResults);
    }

/*    private CheckboxGroup<String> loadFiltersHeader(String sqlRead, String[] arrColumnNames, String columnName) {


        List<Record> lstLearningCategoriesRecs = getRecordsFromDb(sqlRead, arrColumnNames);
        CheckboxGroup<String> chkGroup = new CheckboxGroup<>();
        chkGroup.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER);
        ArrayList<String> lstCategories = new ArrayList<>();
        for (int r = 0; r < lstLearningCategoriesRecs.size(); r++) {

            String captionCategory = lstLearningCategoriesRecs.get(r).getColumnData(columnName);
            lstCategories.add(captionCategory);

        }
        chkGroup.setItems(lstCategories);

        return chkGroup;
    }*/

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

    private VerticalLayout getReviewResults() {

        VerticalLayout layoutReview = new VerticalLayout();
        layoutReview.addClassNames(Width.FULL,
                TextColor.TERTIARY,
                FontSize.MEDIUM,
                Padding.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER);

//        H6 headerPoll = new H6("Poll");
//        headerPoll.addClassNames(Width.FULL,
//                TextColor.TERTIARY,
//                FontSize.MEDIUM,
//                Padding.MEDIUM,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                TextAlignment.CENTER);

        Div layoutPollQnA = new Div();
        layoutPollQnA.setClassName("lazy-poll-container");
        layoutPollQnA.addClassNames(Width.FULL,
                TextColor.TERTIARY,
                FontSize.MEDIUM,
                Padding.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER);

//        Div layoutPoll = new Div();
//        layoutPoll.setWidthFull();
        Div divQuestion = new Div("How much does this item satisfy your learning requirements?");
        divQuestion.addClassNames(Width.FULL,
                TextColor.TERTIARY,
                FontSize.MEDIUM,
                Padding.MEDIUM,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER);

        layoutReview.add(divQuestion, layoutPollQnA);
//        Paragraph par = new Paragraph("(1 very bad, 2 bad ,3 average, 4 good, 5 very good)");
//        par.setWidthFull();
//        par.getStyle().setTextAlign(Style.TextAlign.CENTER);
//        par.getStyle().setColor("#5d6f87");

//        layoutPoll.getStyle().setColor("#5d6f87");


//        layoutPoll.add(divQuestion, layoutPollQnA);

        String vote1 = "5.Very Good";
        String vote2 = "4.Good";
        String vote3 = "3.Average";
        String vote4 = "2.Bad";
        String vote5 = "1.Very Bad";

        ApexChartsBuilder charts1 = new ApexChartsBuilder();
        charts1.withChart(ChartBuilder.get()
                        .withType(Type.PIE).withHeight("230px")
                        .build())
                .withLabels(vote1, vote2, vote3, vote4, vote5)
                .withLegend(LegendBuilder.get()
                        .withPosition(com.github.appreciated.apexcharts.config.legend.Position.LEFT)
                        .withHorizontalAlign(HorizontalAlign.LEFT)
                        .build())
                .withSeries(44.0, 55.0, 13.0, 43.0, 22.0)
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .build())
                //.withTitle(title1)
                .build();
        Div divTitle1 = new Div("Interesting Subject & well structured");
        divTitle1.getStyle().setColor("#5d6f87");
        divTitle1.setWidthFull();
        Div layoutGraph1 = new Div();
        layoutGraph1.setClassName("lazy-poll-graph");
        layoutGraph1.setMinHeight("190px");
        layoutGraph1.add(divTitle1, charts1.build());


        //TitleSubtitle title2 =new TitleSubtitle();
        //title2.setText("Actors");
        //title2.setAlign(Align.CENTER);
        ApexChartsBuilder charts2 = new ApexChartsBuilder();
        charts2.withChart(ChartBuilder.get()
                        .withType(Type.PIE).withHeight("230px")
                        .build())
                .withLabels(vote1, vote2, vote3, vote4, vote5)
                .withLegend(LegendBuilder.get()
                        .withPosition(com.github.appreciated.apexcharts.config.legend.Position.LEFT)
                        .withHorizontalAlign(HorizontalAlign.LEFT)
                        .build())
                .withSeries(4.0, 25.0, 95.0, 128.0, 42.0)
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .build())
                //.withTitle(title2)
                .build();
        Div divTitle2 = new Div("Thorough explained in time duration");
        divTitle2.getStyle().setColor("#5d6f87");
        divTitle2.setWidthFull();
        Div layoutGraph2 = new Div();
        layoutGraph2.setClassName("lazy-poll-graph");
        layoutGraph2.setMinHeight("190px");
        layoutGraph2.add(divTitle2, charts2.build());

//        TitleSubtitle title3 =new TitleSubtitle();
//        title3.setText("Photography");
//        title3.setAlign(Align.CENTER);
        ApexChartsBuilder charts3 = new ApexChartsBuilder();
        charts3.withChart(ChartBuilder.get()
                        .withType(Type.PIE).withHeight("230px")
                        .build())
                .withLabels(vote1, vote2, vote3, vote4, vote5)
                .withLegend(LegendBuilder.get()
                        .withPosition(com.github.appreciated.apexcharts.config.legend.Position.LEFT)
                        .withHorizontalAlign(HorizontalAlign.LEFT)
                        .build())
                .withSeries(44.0, 55.0, 13.0, 43.0, 22.0)
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .build())
                //.withTitle(title3)
                .build();
        Div divTitle3 = new Div("Inspiring & motivates me to ...");
        divTitle3.getStyle().setColor("#5d6f87");
        divTitle3.setWidthFull();
        Div layoutGraph3 = new Div();
        layoutGraph3.setClassName("lazy-poll-graph");
        layoutGraph3.setMinHeight("190px");
        layoutGraph3.add(divTitle3, charts3.build());

        ApexChartsBuilder charts4 = new ApexChartsBuilder();
        charts4.withChart(ChartBuilder.get()
                        .withType(Type.PIE).withHeight("230px")
                        .build())
                .withLabels(vote1, vote2, vote3, vote4, vote5)
                .withLegend(LegendBuilder.get()
                        .withPosition(com.github.appreciated.apexcharts.config.legend.Position.LEFT)
                        .withHorizontalAlign(HorizontalAlign.LEFT)
                        .build())
                .withSeries(4.0, 25.0, 95.0, 128.0, 42.0)
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .build())
                //.withTitle(title2)
                .build();
        Div divTitle4 = new Div("Photography");
        divTitle4.getStyle().setColor("#5d6f87");
        divTitle4.setWidthFull();
        Div layoutGraph4 = new Div();
        layoutGraph4.setClassName("lazy-poll-graph");
        layoutGraph4.setMinHeight("190px");
        layoutGraph4.add(divTitle4, charts4.build());

        ApexChartsBuilder charts5 = new ApexChartsBuilder();
        charts5.withChart(ChartBuilder.get()
                        .withType(Type.PIE).withHeight("230px")
                        .build())
                .withLabels(vote1, vote2, vote3, vote4, vote5)
                .withLegend(LegendBuilder.get()
                        .withPosition(com.github.appreciated.apexcharts.config.legend.Position.LEFT)
                        .withHorizontalAlign(HorizontalAlign.LEFT)
                        .build())
                .withSeries(4.0, 25.0, 95.0, 128.0, 42.0)
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .build())
                //.withTitle(title2)
                .build();
        Div divTitle5 = new Div("Sound");
        divTitle5.getStyle().setColor("#5d6f87");
        divTitle5.setWidthFull();
        Div layoutGraph5 = new Div();
        layoutGraph5.setClassName("lazy-poll-graph");
        layoutGraph5.setMinHeight("190px");
        layoutGraph5.add(divTitle5, charts5.build());

        layoutPollQnA.add(layoutGraph1, layoutGraph2, layoutGraph3); //, layoutGraph4, layoutGraph5);

        return layoutReview;
    }

    private VerticalLayout getFormattedText(String strDescription, boolean isShort) {

        VerticalLayout layoutFormattedText = new VerticalLayout();
        String strDescriptionNew = "";
        if (isShort && strDescription.length() >= 211) {
            strDescriptionNew = strDescription.substring(0, 198) + " ......";
        } else {
            strDescriptionNew = strDescription;
        }

        Paragraph formattedParagraph = new Paragraph(strDescriptionNew);
        formattedParagraph.getElement().getStyle().set("white-space", "pre-wrap");

        layoutFormattedText.add(formattedParagraph);
//        ,
//                new Paragraph(
//                        "For full formatting, you can use HTML either as a string or by assembling individual elements. "
//                                + "When using an HTML string, you should be careful to not include any user-provided values that might lead to cross-site scripting vulnerabilities."),
//                elements, html);

        return layoutFormattedText;
    }


    private VerticalLayout getSubTabs(String strContentType, String strContentTitle, LearningDto dto) {

        VerticalLayout layoutTabsInfo = new VerticalLayout();
        if (isMobile) {
            layoutTabsInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.XSMALL,
                    Gap.SMALL
            );
        } else {
            layoutTabsInfo.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.Horizontal.SMALL, Padding.Vertical.MEDIUM,
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

        String strDescription = nvl(dto.getDescription());

//        Paragraph parDescription = new Paragraph();
//        parDescription.addClassNames(TextColor.TERTIARY, FontSize.MEDIUM, Padding.MEDIUM);
        if (!strDescription.equalsIgnoreCase("null") && !strDescription.isEmpty()) {
//            parDescription.setText(strDescription);
        } else {
            strDescription = " Overview of " + strContentTitle;
        }


        VerticalLayout layoutSourceReview = getFormattedText(strDescription, false);
        layoutSourceReview.addClassNames(TextColor.SECONDARY, FontSize.MEDIUM,
                Margin.NONE,
                Padding.SMALL,
                Gap.SMALL,
                AlignItems.CENTER, JustifyContent.CENTER
        );

//        ArrayList<String> lstLocationTabs = new ArrayList<String>();
//        lstLocationTabs.add("Reviews");
//        lstLocationTabs.add("Notes");
//        lstLocationTabs.add("Related Info");

        VerticalLayout layoutReviews = new VerticalLayout();
        layoutReviews.addClassNames(TextColor.SECONDARY, FontSize.MEDIUM,
                Margin.NONE,
                Padding.NONE,
                Gap.SMALL,
                AlignItems.CENTER, JustifyContent.CENTER
        );
        layoutReviews.add(getFormattedText(strDescription, false), getReviewResults());


        TabSheet tabSheetRelated = new TabSheet();
        tabSheetRelated.addThemeVariants(TabSheetVariant.LUMO_TABS_CENTERED);
        tabSheetRelated.setClassName("lazy-tab-panel");
        tabSheetRelated.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                Margin.NONE,
                Padding.SMALL,
                Gap.SMALL,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextColor.TERTIARY
        );

        StreamResource iconReview = new StreamResource("review.svg",
                () -> getClass().getResourceAsStream("/icons/review.svg"));
        SvgIcon svgReview = new SvgIcon(iconReview);


        Div tabOverview = new Div();
        tabOverview.addClassName("tab-item");
        tabOverview.setId("overview");
        tabOverview.add(FontAwesome.Solid.EYE.create(), new Div("Overview"));
        tabOverview.setClassName("lazy-tab");//.getStyle().set("color","#6a8ab0");
        tabSheetRelated.add(tabOverview, layoutSourceReview);
        Div tab2 = new Div();
        tab2.addClassName("tab-item");
        tab2.setId("reviews");
        tab2.add(svgReview, new Div("Reviews"));
        tab2.setClassName("lazy-tab");//.getStyle().set("color","#6a8ab0");
        tabSheetRelated.add(tab2, layoutReviews);
        Div tab3 = new Div();
        tab3.addClassName("tab-item");
        tab3.setId("related-info");
        tab3.add(FontAwesome.Solid.LINK_SLASH.create(), new Div("Related Info"));
        tab3.setClassName("lazy-tab");//.getStyle().set("color","#6a8ab0");
        tabSheetRelated.add(tab3, new Div(new Text(strContentTitle)));

        layoutTabsInfo.add(tabSheetRelated);

        tabSheetRelated.addSelectedChangeListener(selected -> {
            selected.getPreviousTab();
            Tab selectedTab = selected.getSelectedTab();
            logger.info("Selected tab: {}", selectedTab.getId().get());

        });

//
//
//        btnGroup.addValueChangeListener(event -> {
//            if (event.getValue().toString().equalsIgnoreCase("My Notes")) {
//                divTabContent.setText(" my notes ... of " + strContentTitle + " in " + strContentType);
//                layoutReviews.setVisible(false);
//            } else if (event.getValue().toString().equalsIgnoreCase("Reviews")) {
////                divTabContent.setText(strUsername + " users review 1 ...");
//                layoutReviews.setVisible(true);
//            } else {
//                divTabContent.setText(strContentTitle + " ....... in " + strContentType);
//                layoutReviews.setVisible(false);
//            }
//        });
//
//        layoutTabsInfo.add(btnGroup, divTabContent);


        return layoutTabsInfo;
    }

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {
        logger.info(" learnings  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

    public Details getMemberDetail(String strUserIdPost, String strAvatarPath, String strUserName, String strNameOfUser, String strUserJoined) {

        Image imgAvatarSmall = genericView.getAvatarThumbImage(strAvatarPath, strNameOfUser, "40px", "40px");
        AvatarItem avatarItemMe = new AvatarItem(strNameOfUser, "", imgAvatarSmall);
        avatarItemMe.addClassNames(Width.FULL, AlignItems.STRETCH, JustifyContent.BETWEEN);

        Image imgAvatarMedium = genericView.getAvatarThumbImage(strAvatarPath, strNameOfUser, "70px", "70px");
        AvatarItem avatarLargeItemMe = new AvatarItem(strNameOfUser, "@" + strUserName, imgAvatarMedium);


        Details detailsMember = new Details();
        detailsMember.addClassNames(Width.FULL, BorderRadius.SMALL);
//        detailsMember.addThemeVariants(DetailsVariant.FILLED);
        detailsMember.addClassName("member-small");
        detailsMember.setSummary(avatarItemMe);


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

        HorizontalLayout layoutMemberLocationsCount = new HorizontalLayout();
        layoutMemberLocationsCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divMemberLocations = new Div("1");
        layoutMemberLocationsCount.add(FontAwesome.Regular.COMPASS.create(), divMemberLocations);
//
//        HorizontalLayout layoutDateJoined = new HorizontalLayout();
//        layoutDateJoined.addClassNames(
////                Overflow.HIDDEN, Width.FULL,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Margin.NONE,
//                Padding.XSMALL,
//                Gap.XSMALL,
//                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                //   Background.CONTRAST_5,
//                BorderRadius.NONE
//        );
//        Div divDateJoined = new Div(strUserJoined);
//        layoutDateJoined.add(VaadinIcon.CALENDAR_CLOCK.create(), divDateJoined); // FontAwesome.Regular.CALENDAR.create()

        layoutMemberInfo.add(layoutMemberPhotoCount, layoutMemberViewCount, layoutMemberLocationsCount);


        detailsMember.add(avatarLargeItemMe, layoutMemberInfo);

        return detailsMember;
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
        sessionDateTimeLDT = utilsDate.calcDateTimeFromLongInLDT(sessionCreation, "UTC");
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

    public void configurePage(AppShellSettings settings) {

        settings.addFavIcon("icon", "camera.png", "512x512");
        settings.setPageTitle("photoact.net - Learnings");
        settings.addMetaTag("description", "Community website of photographers, sharing our photos, albums, learning sources and events.");

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

        String strPathToSave;
        if (strPath == null || strPath.isEmpty()) {
            strPathToSave = "NULL";
        } else {
            strPathToSave = "'" + strPath + "'";
        }


        logger.info("photo visitor:" + publicIp + " . " + hostname + " . " + hostAddress + " . " + canonicalHostname + "  .  " + browser + " " + sessionid);

        String insertSQL = "INSERT INTO dbvisitor_log SET visitorlogId = 0,  timeOfVisit = now(), ipAddress = '" + publicIp + "', browserName = '" + browser + "', "
                + " browserVersionMajor = '" + versionOfBrowserMajor + "', browserVersionMinor = '" + versionOfBrowserMinor + "', urlParameter = NULL , timeZoneId = '" + timeZoneId + "', "
                + " appVersion = '" + APP_NAME + "-" + APP_VERSION + "',  parentSection = 'photo',  sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
                + " hostAddress = '" + hostAddress + "', os = '" + strOS + "', browser = '" + strBrowser + "', section = '" + section + "',"
                + " item = " + strPathToSave + ", ref = " + strUrlRequestToBeLogged + ", "
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

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String formatDateAgo(LocalDateTime dt) {
        if (dt == null) return "";
        long days = ChronoUnit.DAYS.between(dt, LocalDateTime.now());
        if (days == 0) return "Today";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        long weeks = days / 7;
        if (weeks < 5) return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        long months = ChronoUnit.MONTHS.between(dt, LocalDateTime.now());
        if (months < 12) return months + (months == 1 ? " month ago" : " months ago");
        long years = ChronoUnit.YEARS.between(dt, LocalDateTime.now());
        return years + (years == 1 ? " year ago" : " years ago");
    }


}
