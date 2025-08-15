package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailSendService;
import com.photo.act.photo_act.services.ImageService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.DialogRegistration;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.HeaderFilterTabs;
import com.photo.act.photo_act.views.components.UploadImageCard;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
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

import static com.photo.act.photo_act.views.MainLayout.*;


//@RolesAllowed("Admin")
@PermitAll

@Route(value = "members") //":section?")
//@RouteAlias(value = "members/name/:member?", layout = MainLayout.class)
//@RouteAlias(value = ":section/:member?", layout = MainLayout.class)
//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class MembersView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";

    private static final Logger logger = LoggerFactory.getLogger(MembersView.class);

    private VerticalLayout verticalLayout;
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    String[] arrColumnsMemberExists = {"userId", "username", "username", "resident", "date_joined", "member_since", "avatar_path"};
    private String forMemberName;
    private RecordService recordService;
    private String strHeader;

    private String dirChar = FileSystems.getDefault().getSeparator();
    public static String subPathThumbs = "photo-thumbs";
    public static String subPathMedium = "photo-medium";
    public static String subPathUpload = "photo-upload";
    public static String subPathShow = "photo-show";

    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
    String sqlDoesMemberExist = "SELECT usr.userId, usr.username, usr.username, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined " +
            " , DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since, usr.avatar_path " +
            " FROM dbuser usr " +
            " WHERE 1 = 1 ";

    private String publicIp;
    private String strPath;
    private String hostname;
    private String hostAddress;
    private String canonicalHostname;

    private int userId;
    String[] arrColumnsMembers = {"userId", "username", "username", "resident", "date_joined", "member_since", "member_for",
            "avatar_path", "name", "surname", "short_bio", "url_insta", "url_fb", "url_flickr", "url_yt", "email", "resident", "resident_country",
            "count_photos", "count_albums", "count_learnings"};

    private String strUrlRequestToBeLogged;
    String sqlMembers = "SELECT " +
            "   usr.userId, usr.username, usr.username, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined,  " +
            " DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since , getDateDiffFromNow(usr.date_joined) AS member_for " +
            " , usr.avatar_path, name, surname, short_bio, url_insta, url_fb, url_flickr, url_yt, email, resident, resident_country " +
            " , count_photos, count_albums, count_learnings " +
            //     "--  , pa.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
            " FROM dbuser usr " +
            " WHERE 1 = 1  " +
            " AND usertype <> 'Guest' " +
            " ORDER BY username ";
    String[] arrColumnsMemberPhotos = {"photo_count", "photo_size",
            "userId", "username", "name", "surname", "resident", "resident_country", "date_joined", "member_since", "avatar_path",
            "short_bio", "url_fb", "url_yt", "url_insta", "url_flickr", "url_website",
            "count_photos", "count_albums", "count_learnings"
    };
    String sqlMemberPhotos = "SELECT count(pm.id) AS photo_count, SUM(pm.space_size) AS photo_size " +
            " ,  usr.userId, usr.username, usr.resident, usr.resident_country, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined,  DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
            " , usr.avatar_path, usr.name, usr.surname " +
            " , usr.short_bio, usr.url_fb, usr.url_yt, usr.url_insta, usr.url_flickr, usr.url_website " +
            " , count_photos, count_albums, count_learnings " +
            //     "--  , pa.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
            " FROM dbuser usr LEFT JOIN photo_meta pm ON pm.uploaderId = usr.userId " +
            " WHERE 1 = 1  " +
            " AND role <> 'Guest' ";
    //            " AND pm.visible_to  = 'ALL' ";
    String sqlMemberPhotosGroupBy =
            " GROUP BY usr.userid " +
                    " ORDER BY usr.userid ASC ";
    private String section = SECTION_MEMBERS;
    private String strMember;


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
    private UtilsDate utilsDate;
    private String sessionDateTime;

    private String strOS;
    private String strBrowser;
    private GenericView genericView;
    private EmailSendService emailSendService;

    public MembersView(RecordService recordService, EmailSendService emailSendService) {
        this.recordService = recordService;
        this.emailSendService = emailSendService;

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
        strMember = event.getRouteParameters().get("member").orElse("all-members");

        getUserClientInfo();

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously
            strUrlRequestToBeLogged = currentUrl.toExternalForm();
        });


        verticalLayout.removeAll();


        if (strMember == null || strMember.isEmpty() || strMember.equalsIgnoreCase(STR_ALL_MEMBERS)) {


            H1 titlePage = new H1(APP_NAME);
            Span subTitle = new Span("[ Network and Act around Photography ]");

            Header siteHeader = new Header(titlePage, subTitle);
            siteHeader.addClassNames(Width.FULL);

            verticalLayout.add(siteHeader);

            Div divMainImage = new Div();
            Image mainImage = new Image();
            String strMainImagePath = DIR_PHOTOS_SERVER + dirChar + "photographerS.jpg";

            final StreamResource imageMainResource = new StreamResource("streamResource", () -> {
                try {

                    Path path = Paths.get(strMainImagePath);
                    File file = path.toFile();
                    return new FileInputStream(file);
                } catch (final FileNotFoundException e) {
//                logErrorInDb(e, "GalleryImageViewCard StreamResource FileNotFoundException", hostname, userId, strUserName, publicIp, sessionCreation, file.getAbsolutePath());
                    // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                    logger.error(e.getMessage());
                }
                return null;
            });


            mainImage.setSrc(imageMainResource);
            mainImage.setAlt("sketch image of a photographer");
            mainImage.setHeight("14rem");
            mainImage.setWidth("auto");
            mainImage.getStyle().setBorderRadius("40px");
            mainImage.getStyle().setPadding("10px");

            divMainImage.add(mainImage);


            Div div1 = new Div("We are a community site, with members exchanging info and links in order to improve our skills in photography!");
            Div div2 = new Div("Currently, we share info about events and learnings. Of course, we also have space for our photos and albums.");


            verticalLayout.add(divMainImage);

            verticalLayout.add(loadHeader("Members", "", ""));


            String sqlMembers = sqlMemberPhotos + " " + sqlMemberPhotosGroupBy;
            verticalLayout.add(getMembersPanels(sqlMembers, arrColumnsMemberPhotos, false));

        } else {
//            verticalLayout.add(loadHeader("Member", "", ""));
//
//            String sqlMember = sqlMemberPhotos + " AND usr.username = '" + strMember + "' " + sqlMemberPhotosGroupBy;
//            int intUserId = loadMember(sqlMember, arrColumnsMemberPhotos, false);
//            if (intUserId > 0) {
//                loadUploadView(intUserId, strMember);
//            } else if (intUserId == 0) {
//                String sqlMemberExists = sqlDoesMemberExist + " AND usr.username = '" + strMember + "' ";
//                intUserId = checkMemberExists(sqlMemberExists, arrColumnsMemberExists, false);
//                if (intUserId > 0) {
//                    loadUploadView(intUserId, strMember);
//                }
//            }
        }

        this.removeAll();
        this.add(verticalLayout);
        this.add(genericView.loadFooter(isMobile));

        logVisitorToDb("");

    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String o) {
//        section = o;//beforeEvent.getRouteParameters().get("section").orElse("pictures");
    }

    private void constructUI() {
        addClassNames("upload-view");
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

        DIR_PHOTOS_SERVER = genericView.getAppProps(PROP_PHOTOS);

        verticalLayout = new VerticalLayout();
        verticalLayout.setId("verticalLayout");
        if (isMobile) {
            verticalLayout.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE, Padding.NONE,
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
                    Padding.XLARGE,
                    Padding.Top.XSMALL,
                    Gap.MEDIUM,
                    AlignItems.CENTER, JustifyContent.CENTER
            );
//            verticalLayout.getStyle().set("gap", "3rem");
        }
        this.setWidthFull();

    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSection) {

        this.strHeader = strHeader;

        HorizontalLayout headerContainerMaster = new HorizontalLayout();
        if (isMobile) {
            headerContainerMaster.addClassNames(
                    AlignItems.CENTER, JustifyContent.CENTER,
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
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Overflow.HIDDEN, Width.FULL,
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
                Margin.NONE, Padding.NONE,
                Gap.XSMALL);

        H2 header = new H2(strHeader);
        header.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.LARGE, FontWeight.BOLD, TextColor.SECONDARY);
//        header.getStyle().set("font-family", "Times-New-Roman, serif");

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.NONE, Margin.Top.NONE, FontSize.SMALL, TextColor.SECONDARY);

        H3 divSection = new H3(strSection);
        divSection.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Margin.Top.MEDIUM);

        headerTextContainer.add(header, subheader, divSection);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Most Viewed", "Least Viewed", "Most Favourite", "Least Favourite", "Newest First", "Oldest First", "Most Liked", "Least Liked");
        sortBy.setValue("Most Viewed");

        Div headerContainerSecondary = new Div();
        if (isMobile) {
            headerContainerSecondary.addClassNames(
                    AlignItems.CENTER, JustifyContent.CENTER,
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
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Overflow.HIDDEN, Width.FULL,
                    Margin.NONE,
                    Padding.NONE,
                    Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    BorderRadius.LARGE
            );
        }

        VerticalLayout layoutFilters = new VerticalLayout();
        if (isMobile) {
            layoutFilters.addClassNames(
                    Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE,
                    Padding.NONE,
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
                    Padding.NONE,
                    Gap.SMALL,
                    Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    BorderRadius.LARGE);
        }

        CheckboxGroup<String> checkboxGroupSubject = new CheckboxGroup<>();
        checkboxGroupSubject.setTooltipText("Subject");
//        checkboxGroupSubject.setLabel("Subject");
        checkboxGroupSubject.setItems("Photography", "Street Photography", "Landscape", "Cityscape");
        //   "Friday", "Saturday", "Sunday");
        // checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
//        Div lblFilterSubject = new Div("Subject");

        layoutFilters.add(checkboxGroupSubject);

//        CheckboxGroup<String> checkboxGroupFormat = new CheckboxGroup<>();
//        checkboxGroupFormat.setTooltipText("Format");
////        checkboxGroupFormat.setLabel("Format");
//        checkboxGroupFormat.setItems("Book", "Youtube");
////        Div lblFilterFormat = new Div("Format");
//        layoutFilters.add(checkboxGroupFormat);

        CheckboxGroup<String> checkboxGroupLocation = new CheckboxGroup<>();
        checkboxGroupLocation.setTooltipText("Location");
//         checkboxGroupLocation.setLabel("Location");
        checkboxGroupLocation.setItems("Hungary", "UK", "Greece");//, "Thursday",

        layoutFilters.add(checkboxGroupLocation);

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

        Select<String> cmbView = new Select<>();
        cmbView.setLabel("View");

        cmbView.setItems("Micro View", "Ordinary - No MetaData", "Ordinary - MetaData Bottom", "Ordinary - MetaData Right",
                "Wide - No MetaData", "Wide - MetaData Bottom", "Wide - MetaData Right");
        cmbView.setValue("Ordinary - No MetaData");

        //        headerContainerMaster.add(headerTextContainer);
//        layoutHeaderParameters.add(headerContainerMaster);


//        headerContainerMaster.add(headerTextContainer);
//        headerContainerSecondary.add(layoutFilters);
//        layoutHeaderParameters.add( headerContainerSecondary, divSection);

        HeaderFilterTabs headerFilterTabs = new HeaderFilterTabs(recordService, isMobile);
        VerticalLayout layoutHeaderParameters = headerFilterTabs.getHeader(strHeader, strSubHeader, strSection, headerContainerSecondary);

//        headerContainerMaster.add(headerTextContainer, cmbView);
//        headerContainerSecondary.add(layoutFilters, sortBy);
//        layoutHeaderParameters.add(headerContainerMaster,headerContainerSecondary);

        return layoutHeaderParameters;
    }

    private void loadUploadView(int intUserId, String strMember) {
        UploadImageCard uploadImageCard = new UploadImageCard(recordService, emailSendService , intUserId, strMember, sessionCreation, publicIp, hostname);
        uploadImageCard.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                Margin.SMALL,
                Padding.NONE,
                Gap.MEDIUM,
                Background.CONTRAST_5, BorderRadius.LARGE,
                AlignItems.STRETCH, //JustifyContent.BETWEEN,
                JustifyContent.EVENLY
        );
        uploadImageCard.setHeight("100px");

//            verticalLayout.add(uploadImageCard.getLocationSelectionLayout());
        Button btnRefreshPhotoMeta = new Button("Refresh Photo Meta");
        btnRefreshPhotoMeta.addClickListener(e -> {
            ImageService imageService = new ImageService();
            if (imageService.updatePhotoMeta(recordService, intUserId)) {
                Notification notification = Notification.show(
                        "Updated !",
                        5000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification notification = Notification.show(
                        "Error !",
                        5000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

            }
        });

        verticalLayout.add(uploadImageCard.getUploadImageCard());
    }

    private void displayRegisterDialog() {
        DialogRegistration dialogRegister = new DialogRegistration(isMobile, "", sessionCreation, hostname, publicIp, recordService, section, "register-from-members-view");
        dialogRegister.open();
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


    private int checkMemberExists(String sqlRead, String[] arrColumnNames, boolean isEditable) {

        int intUserId = 0;
        VerticalLayout layoutMember = new VerticalLayout();
        layoutMember.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextColor.TERTIARY,
                Padding.LARGE,
                Gap.SMALL
        );

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);

        if (lstRecords == null) {
            logger.warn("check lstRecords is null");
        } else if (lstRecords.isEmpty()) {
            logger.warn("check lstRecords is empty");
        } else if (lstRecords.size() == 1) {
            Record rec = lstRecords.get(0);
            String strUserId = rec.getColumnData("userId");
            try {
                intUserId = Integer.parseInt(strUserId);
            } catch (NumberFormatException e) {
                logger.error("strUserId: " + strUserId + "    " + e.getMessage());
            }

            String strNameOfUser = rec.getColumnData("username");
            String strCountOfPhotosOfAlbums = rec.getColumnData("photo_count");
            String strMemberSince = rec.getColumnData("member_since");
            String strAvatarPath = rec.getColumnData("avatar_path");


            Image imgAvatar = genericView.getAvatarImage(strAvatarPath, strNameOfUser, "130px", "130px");

            H3 objMember = new H3(strNameOfUser);
            Div divMemberSince = new Div("Member since " + strMemberSince);
            Div divAlbumsAndPhotos = new Div("No photos uploaded yet! Try uploading now !");
            layoutMember.add(imgAvatar, objMember, divMemberSince, divAlbumsAndPhotos);
        } else {
            logger.warn("check lstRecords is more than one record");
        }

        verticalLayout.add(layoutMember);
        return intUserId;
    }

    private VerticalLayout getMembersPanels(String sqlRead, String[] arrColumnNames, boolean isEditable) {

        VerticalLayout membersLayout = new VerticalLayout();
        membersLayout.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.MEDIUM,
                Margin.NONE,
                Gap.LARGE
        );

        int intUserId = 0;


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);

        if (lstRecords == null) {
            logger.warn("check lstRecords is null");
        } else if (lstRecords.isEmpty()) {
            logger.warn("check lstRecords is empty");
        } else if (lstRecords.size() >= 1) {

            for (int r = 0; r < lstRecords.size(); r++) {

                VerticalLayout layoutMember = new VerticalLayout();
                layoutMember.addClassNames(
                        AlignItems.CENTER, JustifyContent.CENTER,
                        TextColor.TERTIARY,
                        Padding.LARGE,
                        Margin.LARGE,
                        Gap.MEDIUM
                );
                layoutMember.getStyle().setBorderRadius("60px");
                layoutMember.getStyle().setMaxWidth("610px");
//                layoutMember.getStyle().set("border", "lightgrey 1px solid");
                layoutMember.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER, Background.CONTRAST_5);

                Record rec = lstRecords.get(r);
                String strUserId = rec.getColumnData("userId");
                try {
                    intUserId = Integer.parseInt(strUserId);
                } catch (NumberFormatException e) {
                    logger.error("strUserId: " + strUserId + "    " + e.getMessage());
                }

                String strName = rec.getColumnData("name");
                String strSurname = rec.getColumnData("surname");
                String strUsername = rec.getColumnData("username");
                String strMemberSince = rec.getColumnData("member_since");
                String strAvatarPath = rec.getColumnData("avatar_path");

                String strResident = rec.getColumnData("resident");
                String strResidentCountry = rec.getColumnData("resident_country");

                String strShortBio = rec.getColumnData("short_bio");
                String strFb = rec.getColumnData("url_fb");
                String strYt = rec.getColumnData("url_yt");
                String strInsta = rec.getColumnData("url_insta");
                String strFlickr = rec.getColumnData("url_flickr");
                String strWebsite = rec.getColumnData("url_website");

                String strCountPhotos = rec.getColumnData("count_photos");
                String strCountAlbums = rec.getColumnData("count_albums");
                String strCountLearnings = rec.getColumnData("count_learnings");

                Anchor linkWebsite = new Anchor();
                linkWebsite.add(FontAwesome.Solid.LINK.create());
//                linkWebsite.setVisible(false);
                if (strWebsite != null && !strWebsite.equalsIgnoreCase("null") && !strWebsite.isEmpty()) {
                    linkWebsite.setVisible(true);
                    linkWebsite.setHref(strWebsite);
                    linkWebsite.setTarget("_blank");
                }

                Anchor linkTutorYt = new Anchor();
                linkTutorYt.add(FontAwesome.Brands.YOUTUBE.create());
                // linkTutorYt.getStyle().setColor(strColorExternalweb);
                // linkTutorYt.setClassName("lazy-result-line-button");
//                linkTutorYt.setVisible(false);
                if (strYt != null && !strYt.equalsIgnoreCase("null") && !strYt.isEmpty()) {

                    linkTutorYt.setHref(strYt);
                    linkTutorYt.setTarget("_blank");
                    linkTutorYt.setVisible(true);
                }

                Anchor linkTutorFacebook = new Anchor();
                linkTutorFacebook.add(FontAwesome.Brands.FACEBOOK_F.create());
                // linkTutorWikipedia.getStyle().setColor(strColorExternalweb);
                //   linkTutorWikipedia.setClassName("lazy-result-line-button");
//            linkTutorFacebook.setVisible(false);

                if (strFb != null && !strFb.equalsIgnoreCase("null") && !strFb.isEmpty()) {
                    //linkTutorYt.setText("YouTube");
                    //strUrlTutorWikipedia = "https://www.youtube.com/"+strUrlTutorYt;
                    linkTutorFacebook.setHref(strFb);
                    linkTutorFacebook.setTarget("_blank");
                    linkTutorFacebook.setVisible(true);
                }

                Anchor linkTutorInsta = new Anchor();
                //  linkTutorInsta.setClassName("lazy-result-line-button");
                linkTutorInsta.add(FontAwesome.Brands.INSTAGRAM.create());
                // linkTutorInsta.getStyle().setColor(strColorExternalweb);
//            linkTutorInsta.setVisible(false);
                if (strInsta != null && !strInsta.equalsIgnoreCase("null") && !strInsta.isEmpty()) {
                    linkTutorInsta.setHref(strInsta);
                    linkTutorInsta.setTarget("_blank");
//                linkTutorInsta.setVisible(true);
                }

                Anchor linkFlickr = new Anchor();
                linkFlickr.add(FontAwesome.Brands.FLICKR.create());
                // linkTutorYt.getStyle().setColor(strColorExternalweb);
                // linkTutorYt.setClassName("lazy-result-line-button");
//                linkFlickr.setVisible(false);
                if (strFlickr != null && !strFlickr.equalsIgnoreCase("null") && !strFlickr.isEmpty()) {

                    linkFlickr.setHref(strFlickr);
                    linkFlickr.setTarget("_blank");
                    linkFlickr.setVisible(true);
                }

                Div divBioTitle = new Div("Short Bio");
                divBioTitle.addClassNames(TextColor.TERTIARY, FontWeight.BOLD);

                HorizontalLayout layoutMemberLinks = new HorizontalLayout();
                layoutMemberLinks.add(linkWebsite, linkTutorFacebook, linkTutorYt, linkTutorInsta, linkTutorYt, linkFlickr);

                Div divBio = new Div();
//            divBio.setVisible(false);
                if (strShortBio != null && !strShortBio.equalsIgnoreCase("null") && !strShortBio.isEmpty()) {
                    divBio.setVisible(true);
                    divBio.setText(strShortBio);
                } else {
//                divBio.setVisible(false);
                }

                Image imgAvatar = genericView.getAvatarImage(strAvatarPath, strUsername, "150px", "150px");
//            Image imgAvatar = getAvatarImage(strAvatarPath, strNameOfUser, "120px", "120px");

                H3 objName = new H3(strName + " " + strSurname);
                objName.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);
                H4 objMember = new H4(strUsername);
                objMember.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);
                Div divMemberSince = new Div("Member since " + strMemberSince);
                divMemberSince.addClassNames(TextColor.SECONDARY, FontWeight.MEDIUM);

                VerticalLayout layoutMemberCard = new VerticalLayout();
                layoutMemberCard.getStyle().setBorderRadius("30px");
                layoutMemberCard.getStyle().setWidth("330px");
                layoutMemberCard.getStyle().set("border", "lightgrey 1px solid");
                layoutMemberCard.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER, Background.CONTRAST_5);
                layoutMemberCard.add(imgAvatar, objName, objMember, layoutMemberLinks, divMemberSince);

                Div divResident = new Div("Lives at " + strResident + ", " + strResidentCountry);


                VerticalLayout layoutContribution = new VerticalLayout();
                Div divContribTitle = new Div("Contributed with");
                Div divCounts = new Div(strCountPhotos + ":Photos,  " + strCountAlbums + ":Albums,  " + strCountLearnings + ":Learnings");

                layoutContribution.add(divContribTitle, divCounts);
                layoutContribution.getStyle().setBorderRadius("10px");
                layoutContribution.getStyle().setWidth("330px");
                layoutContribution.getStyle().set("border", "lightgrey 1px solid");
                layoutContribution.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER, Background.CONTRAST_5);

                layoutMember.add(layoutMemberCard, layoutContribution, divBioTitle, divBio, divResident);
                //layoutMember.add(imgAvatar, objName, objMember, divMemberSince, divShortBio, divResident, );

                membersLayout.add(layoutMember);
            }

        } else {
            logger.warn("check lstRecords is more than one record");
        }


        return membersLayout;
    }


    private void logVisitorToDb(String logText) {

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

//        if (strPath == null || strPath.isEmpty()) {
//            strPath = "NULL";
//        } else {
//            strPath = strPath.replace("\\", "-");
//            strPath = strPath.replace("'", "");
//            strPath = "'" + strPath + "'";
//        }


        logger.info("photo visitor:" + publicIp + " . " + hostname + " . " + hostAddress + " . " + canonicalHostname + "  .  " + browser + " " + sessionid);

        String insertSQL = "INSERT INTO dbvisitor_log SET visitorlogId = 0,  timeOfVisit = now(), ipAddress = '" + publicIp + "', browserName = '" + browser + "', "
                + " browserVersionMajor = '" + versionOfBrowserMajor + "', browserVersionMinor = '" + versionOfBrowserMinor + "', urlParameter = NULL , timeZoneId = '" + timeZoneId + "', "
                + " appVersion = '" + APP_NAME + "-" + APP_VERSION + "',  parentSection = 'photo',  sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
                + " hostAddress = '" + hostAddress + "', os = '" + strOS + "', browser = '" + strBrowser + "', section = '" + section + "',"
                + " item = '" + logText + "' , ref = " + strUrlRequestToBeLogged + ", "
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
