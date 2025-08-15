package com.photo.act.photo_act.views;


import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.model.Person;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.MainLayout.*;

@PermitAll
//@PageTitle("Feed")
@Route("feed")
//@Menu(order = 2, icon = "line-awesome/svg/list-solid.svg")  //, icon = LineAwesomeIconUrl.LIST_SOLID)
public class FeedView extends Div implements AfterNavigationObserver {

    private static final Logger logger = LoggerFactory.getLogger(FeedView.class);

    private Grid<Person> grid = new Grid<>();
    private RecordService recordService;
    private boolean isMobile = false;      //TODO
    private String section = "feed";
    private String strUrlRequestToBeLogged;
    private String strOS;
    private String strBrowser;
    private String sysUserName;
    private String sessionid;
    private long sessionCreation;
    private String timeZoneId;
    private Object sessionDateTime;
    private String locale;
    private String localeName;
    private String publicIp;
    private String hostname;
    private String hostAddress;
    private String canonicalHostname;


    private String dirChar = FileSystems.getDefault().getSeparator();

    public static String STR_ALL_TUTORS = "all-tutors";
    public static String STR_ALL_CATEGORIES = "all-categories";
    public static String STR_ALL_TITLES = "all";

    public static String STR_ORDER_BY_NEWEST = "newest";
    public static String STR_ORDER_BY_OLDER = "older";

    public static String subPathThumbs = "photo-thumbs";
    public static String subPathMedium = "photo-medium";
    public static String subPathUpload = "photo-upload";
    public static String subPathShow = "photo-show";

    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";

    private String strPath;

    private UtilsDate utilsDate;

    public FeedView(RecordService recordService) {
        addClassName("feed-view");

        this.recordService = recordService;
        utilsDate = new UtilsDate();


        constructUI();

        setSizeFull();
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addComponentColumn(person -> createCard(person));
        add(grid);
    }

    private void constructUI() {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        hostname = inetAddress.getHostName();
        hostAddress = inetAddress.getHostAddress();
        canonicalHostname = inetAddress.getCanonicalHostName();

        if (hostname.equalsIgnoreCase(HOSTNAME_LAPTOP)) {
            DIR_PHOTOS_SERVER = "/home/mike/Pictures/lazy-photos";
        } else if (hostname.equalsIgnoreCase(HOSTNAME_LAPTOP_LENOVO)) {
            DIR_PHOTOS_SERVER = "/home/linux-pc/Pictures/lazy-photos";
        } else if (hostname.equalsIgnoreCase(HOSTNAME_LAPTOP_LENOVO_WIN)) {
            DIR_PHOTOS_SERVER = "C:\\Users\\nickg\\Pictures\\lazy-photos";
        } else if (hostname.equalsIgnoreCase("piot")) {
            DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
        } else if (hostname.equalsIgnoreCase(HOSTNAME_SERVER_HOSTINGER)) {
            DIR_PHOTOS_SERVER = "/home/mikel/lazy-photos";
        } else {
            DIR_PHOTOS_SERVER = "/home/sammy/lazy-photos";

        }
    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader, String strSection) {

        HorizontalLayout headerContainerMaster = new HorizontalLayout();
        if (isMobile) {
            headerContainerMaster.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.MEDIUM,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE
            );
        } else {
            headerContainerMaster.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.MEDIUM,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    LumoUtility.BorderRadius.LARGE
            );
        }

        VerticalLayout headerTextContainer = new VerticalLayout();
        headerTextContainer.addClassNames(
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.Gap.XSMALL);

        H3 header = new H3(strHeader + " ...");
        header.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.NONE, LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD, LumoUtility.TextColor.SECONDARY);
//        header.getStyle().set("font-family", "Times-New-Roman, serif");

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.NONE, LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        headerTextContainer.add(header, subheader);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Most Viewed", "Least Viewed", "Most Favourite", "Least Favourite", "Newest First", "Oldest First", "Most Liked", "Least Liked");
        sortBy.setValue("Most Viewed");

        HorizontalLayout headerContainerSecondary = new HorizontalLayout();
        if (isMobile) {
            headerContainerSecondary.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE
            );
        } else {
            headerContainerSecondary.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    LumoUtility.BorderRadius.LARGE
            );
        }


        VerticalLayout layoutFilters = new VerticalLayout();
        if (isMobile) {
            layoutFilters.addClassNames(
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.SMALL,
                    LumoUtility.Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE);
        } else {
            layoutFilters.addClassNames(
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.SMALL,
                    LumoUtility.Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    LumoUtility.BorderRadius.LARGE);
        }

        CheckboxGroup<String> checkboxGroupSubject = new CheckboxGroup<>();
        checkboxGroupSubject.setTooltipText("Subject");
//        checkboxGroupSubject.setLabel("Subject");
        checkboxGroupSubject.setItems("Photography", "Street Photography", "Landscape", "Cityscape");
        //   "Friday", "Saturday", "Sunday");
        // checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
//        Div lblFilterSubject = new Div("Subject");

        layoutFilters.add(checkboxGroupSubject);


        CheckboxGroup<String> checkboxGroupFormat = new CheckboxGroup<>();
        checkboxGroupFormat.setTooltipText("Format");
//        checkboxGroupFormat.setLabel("Format");
        checkboxGroupFormat.setItems("Book", "Youtube");
//        Div lblFilterFormat = new Div("Format");
        layoutFilters.add(checkboxGroupFormat);


        CheckboxGroup<String> checkboxGroupLocation = new CheckboxGroup<>();
        checkboxGroupLocation.setTooltipText("Location");
//         checkboxGroupLocation.setLabel("Location");
        checkboxGroupLocation.setItems("Hungary", "UK", "Greece");//, "Thursday",

        layoutFilters.add(checkboxGroupLocation);


        VerticalLayout layoutHeaderParameters = new VerticalLayout();
        if (isMobile) {
            layoutHeaderParameters.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.SMALL,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.XSMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE
            );
        } else {
            layoutHeaderParameters.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.SMALL,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.XSMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
//                       Background.CONTRAST_5,
                    LumoUtility.BorderRadius.LARGE
            );
        }


        Select<String> cmbView = new Select<>();
        cmbView.setLabel("View");

        cmbView.setItems("Micro View", "Ordinary - No MetaData", "Ordinary - MetaData Bottom", "Ordinary - MetaData Right",
                "Wide - No MetaData", "Wide - MetaData Bottom", "Wide - MetaData Right");
        cmbView.setValue("Ordinary - No MetaData");

        headerContainerMaster.add(headerTextContainer, cmbView);
        headerContainerSecondary.add(layoutFilters, sortBy);


        layoutHeaderParameters.add(headerContainerMaster, headerContainerSecondary);

        return layoutHeaderParameters;
    }

    private HorizontalLayout createCard(Person person) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.getThemeList().add("spacing-s");

        Image image = new Image();
        image.setSrc(person.getImage());
        VerticalLayout description = new VerticalLayout();
        description.addClassName("description");
        description.setSpacing(false);
        description.setPadding(false);

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("header");
        header.setSpacing(false);
        header.getThemeList().add("spacing-s");

        Span name = new Span(person.getName());
        name.addClassName("name");
        Span date = new Span(person.getDate());
        date.addClassName("date");
        header.add(name, date);

        Span post = new Span(person.getPost());
        post.addClassName("post");

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("actions");
        actions.setSpacing(false);
        actions.getThemeList().add("spacing-s");

        Icon likeIcon = VaadinIcon.HEART.create();
        likeIcon.addClassName("icon");
        Span likes = new Span(person.getLikes());
        likes.addClassName("likes");
        Icon commentIcon = VaadinIcon.COMMENT.create();
        commentIcon.addClassName("icon");
        Span comments = new Span(person.getComments());
        comments.addClassName("comments");
        Icon shareIcon = VaadinIcon.CONNECT.create();
        shareIcon.addClassName("icon");
        Span shares = new Span(person.getShares());
        shares.addClassName("shares");

        actions.add(likeIcon, likes, commentIcon, comments, shareIcon, shares);

        description.add(header, post, actions);
        card.add(image, description);
        return card;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {

        // Set some data when this view is displayed.
        List<Person> persons = Arrays.asList( //
                createPerson("https://randomuser.me/api/portraits/men/42.jpg", "John Smith", "May 8",
                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/42.jpg", "Abagail Libbie", "May 3",
                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/24.jpg", "Alberto Raya", "May 3",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/24.jpg", "Emmy Elsner", "Apr 22",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/76.jpg", "Alf Huncoot", "Apr 21",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/76.jpg", "Lidmila Vilensky", "Apr 17",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/94.jpg", "Jarrett Cawsey", "Apr 17",
                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/94.jpg", "Tania Perfilyeva", "Mar 8",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/16.jpg", "Ivan Polo", "Mar 5",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/16.jpg", "Emelda Scandroot", "Mar 5",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/67.jpg", "Marcos Sá", "Mar 4",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/67.jpg", "Jacqueline Asong", "Mar 2",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20")

        );

        grid.setItems(persons);
    }

    private static Person createPerson(String image, String name, String date, String post, String likes,
                                       String comments, String shares) {
        Person p = new Person();
        p.setImage(image);
        p.setName(name);
        p.setDate(date);
        p.setPost(post);
        p.setLikes(likes);
        p.setComments(comments);
        p.setShares(shares);

        return p;
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


}
