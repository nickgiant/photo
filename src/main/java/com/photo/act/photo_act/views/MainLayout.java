package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.views.components.GoogleAnalytics;
import com.photo.act.photo_act.views.components.LoginDialog;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed

public class MainLayout extends AppLayout{

    public static final String APP_VERSION = "2026.06.1.02";
    public static final String APP_NAME = "PhotoAct.net";
    public static final String baseUrl = "https://photoact.net";
    public static final String baseMoto = "Through Photography, We Connect and Act";

    public static final String HOSTNAME_LAPTOP = "mike-SATELLITE-PRO-C50-H-11G";
    public static final String HOSTNAME_LAPTOP_LENOVO_WIN = "my-pc";
    public static final String HOSTNAME_LAPTOP_LENOVO = "linux-pc-LOQ-15ARP9";

    public static final String HOSTNAME_SERVER_HOSTINGER = "srv882238";
    public static final String PROP_PHOTOS = "dir-photos";

    private static final Logger logger = LoggerFactory.getLogger(MainLayout.class);
    private boolean isMobile;
    private String sysUsername;

    public static final String SECTION_HOME = "home";
    public static final String SECTION_GALLERY = "gallery";
    public static final String SECTION_ALBUMS = "albums";
    public static final String SECTION_STORIES = "stories";
    public static final String SECTION_FESTIVALS = "festivals"; // clubs festivals exhibitions photowalks schools
    public static final String SECTION_WEBSITES = "websites"; // clubs festivals exhibitions photowalks schools
    public static final String SECTION_LEARNINGS = "learnings";
    public static final String SECTION_CLUBS = "clubs";
    public static final String SECTION_LOCATIONS = "locations";
    public static final String SECTION_MY_FAVOURITES = "my-favourites";
    public static final String SECTION_MY_TEAMS = "my-teams";
    public static final String SECTION_MY_PHOTOS = "my-photos";
    public static final String SECTION_MEMBERS = "members";
    public static final String SECTION_FEED = "feed";
    public static final String SECTION_LOGIN = "login";

    public static final String STR_ALL_MEMBERS = "all-members";
    public static final String STR_ALL_ALBUMS = "all-albums";
    public static final String STR_ALL_DESTINATIONS = "all-locations";
    public static final String STR_ALL_DESTINATION_TYPES = "all-location-types";
    public static final String STR_ALL_MONTHS = "all-months";
    public static final String STR_ALL_COUNTRIES = "all-countries";
    public static final String STR_ALL_CATEGORIES = "all-categories";

    public static final String SECTION_LOG = "log";

    public static final String SUB_PATH_AVAILABLE_AVATARS = "avail-avatars";
    public static final String SUB_PATH_AVATARS_THUMBS = "avatars-thumbs";
    public static final String SUB_PATH_AVATARS_SMALL = "avatars-small";

    public static final String VIEW_PHOTO_GRID = "Photo Grid";
    public static final String VIEW_ONE_PHOTO = "Photo";

    public String strNameOfUser = "My Self";

    private boolean drawerMinimized = false;

    VerticalLayout layoutMenu;

    private int userId;
    private String strUsername;

    public MainLayout() {

        GoogleAnalytics analytics = new GoogleAnalytics("G-NQH7NZ6JJL"); // Your measurement ID
        addToDrawer(analytics);

        // Track page views when route changes
        UI.getCurrent().addBeforeEnterListener(event -> {
            analytics.sendPageView(event.getLocation().getPath());
        });



        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String hostname = inetAddress.getHostName();

        isMobile = VaadinSession.getCurrent().getBrowser().isAndroid() || VaadinSession.getCurrent().getBrowser().isIPhone() || VaadinSession.getCurrent().getBrowser().isWindowsPhone();

        layoutMenu = new VerticalLayout();

//        userId = 1;
//        strUsername = "visitor-user";

        logger.info("hostname:" + hostname + " isMobile:" + isMobile);
        this.addClassName("background");

        createDrawer();
        addToNavbar(createMobileHeader());
        getElement().executeJs("this.drawerOpened = window.innerWidth > 768;");

//        addClassName("app-container");
//
//        // --- Sidebar ---
//        sidebar = new Div();
//        sidebar.addClassName("sidebar");
//
//        // Header: logo + lock toggle
//        Div header = createHeader();
//        sidebar.add(header);
//
//        // Navigation sections
//        Div navWrapper = new Div();
//        navWrapper.addClassName("nav-wrapper");
//
//        navWrapper.add(createSection("Dashboard",
//                new NavItem("Overview",       "vaadin:dashboard",    "#"),
//                new NavItem("All Projects",   "vaadin:folder-open",  "#")
//        ));
//
//        navWrapper.add(createSection("Editor",
//                new NavItem("Magic Build",    "vaadin:magic",        "#"),
//                new NavItem("New Projects",   "vaadin:plus-circle",  "#"),
//                new NavItem("Upload New",     "vaadin:upload",       "#")
//        ));
//
//        navWrapper.add(createSection("Settings",
//                new NavItem("Notice Board",   "vaadin:clipboard-text","#"),
//                new NavItem("Award",          "vaadin:trophy",       "#"),
//                new NavItem("Settings",       "vaadin:cog",          "#")
//        ));
//
//        sidebar.add(navWrapper);
//
//        // Profile section at bottom
//        Div profile = createProfile();
//        sidebar.add(profile);
//
//        // --- Main content area ---
//        Div content = new Div();
//        content.addClassName("main-content");
//
//        // Content placeholder
//        Div contentInner = new Div();
//        contentInner.addClassName("content-inner");
//        H2 heading = new H2("Welcome to the Dashboard");
//        heading.getStyle().set("color", "#333").set("margin-bottom", "12px");
//        Paragraph p = new Paragraph("This is your main content area. "
//                + "Hover over the sidebar to expand it, or click the lock icon to keep it open.");
//        p.getStyle().set("color", "#666").set("max-width", "600px").set("line-height", "1.7");
//        contentInner.add(heading, p);
//        content.add(contentInner);
//
//        addToDrawer(sidebar);
//

    }




    private Component createMobileHeader() {
        Header header = new Header();
        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.ROW, Width.FULL,
                AlignItems.CENTER, Gap.SMALL,
                Padding.Horizontal.SMALL, Padding.Vertical.XSMALL);

        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        Div divLogo = new Div();
        divLogo.add(VaadinIcon.CAMERA.create());
        divLogo.addClassName("logo-icon");
        divLogo.addClassNames(FontSize.MEDIUM, FontWeight.BOLD, TextColor.TERTIARY,
                Padding.NONE, Margin.NONE);

        H1 appName = new H1(APP_NAME);
        appName.addClassNames(FontSize.MEDIUM, FontWeight.SEMIBOLD, TextColor.TERTIARY,
                Padding.NONE, Margin.NONE);
//        appName.getStyle().set("font-family", "Times-New-Roman, serif");
//        appName.getStyle().set("font-stretch", "semi-expanded");
        appName.setClassName("brand-text");

        header.add(toggle, divLogo, appName);
        return header;
    }

    private MenuItemInfo[] createMenuItems() {

        StreamResource imageResourceMember = new StreamResource("user-profile-icon.svg",
                () -> getClass()
                        .getResourceAsStream("/icons/user-profile-icon.svg"));
        SvgIcon svgMember = new SvgIcon(imageResourceMember);

        SvgIcon svgGroup = new SvgIcon(DownloadHandler.forClassResource(getClass(), "/icons/group-icon.svg"));

        StreamResource imageResourceStories = new StreamResource("story.svg",
                () -> getClass()
                        .getResourceAsStream("/icons/story.svg"));
        SvgIcon svgStories = new SvgIcon(imageResourceStories);


        if (isMobile) {
            return new MenuItemInfo[]{ //
                    new MenuItemInfo("", FontAwesome.Solid.HOME.create(), HomeView.class),//  LineAwesomeIcon.PENCIL_RULER_SOLID.create(),
                    //  new MenuItemInfo("Stories", svgStories, StoriesView.class), //  LineAwesomeIcon.PENCIL_RULER_SOLID.create(),
                    new MenuItemInfo("", VaadinIcon.PICTURE.create(), GalleryView.class), //
                    new MenuItemInfo("", FontAwesome.Solid.PHOTO_FILM.create(), AlbumsView.class), //
                    new MenuItemInfo("", VaadinIcon.BOOK.create(), LearningsView.class), //  LineAwesomeIcon.PENCIL_RULER_SOLID.create(),
                    new MenuItemInfo("", VaadinIcon.CALENDAR_USER.create(), FestivalsView.class), //  LineAwesomeIcon.PENCIL_RULER_SOLID.create(),
                    new MenuItemInfo("", svgGroup, PhotographersView.class),

                    new MenuItemInfo("", FontAwesome.Solid.UPLOAD.create(), UploadView.class), //
                    new MenuItemInfo("", svgMember, MeView.class), //

                    // new MenuItemInfo("Checkout Form", LineAwesomeIcon.CREDIT_CARD.create(), CheckoutFormView.class), //
            };

        } else {
            return new MenuItemInfo[]{ //

                    new MenuItemInfo(APP_NAME, FontAwesome.Solid.HOME.create(), HomeView.class),//  LineAwesomeIcon.PENCIL_RULER_SOLID.create(),
                    //  new MenuItemInfo("Stories", svgStories, StoriesView.class), //  LineAwesomeIcon.PENCIL_RULER_SOLID.create(),
                    new MenuItemInfo("Albums", FontAwesome.Solid.PHOTO_FILM.create(), AlbumsView.class), //
                    new MenuItemInfo("Photos", VaadinIcon.PICTURE.create(), GalleryView.class), //
                    new MenuItemInfo("Learnings", VaadinIcon.BOOK.create(), LearningsView.class), //  LineAwesomeIcon.PENCIL_RULER_SOLID.create(),
                    new MenuItemInfo("Events", VaadinIcon.CALENDAR_USER.create(), FestivalsView.class), //  LineAwesomeIcon.PENCIL_RULER_SOLID.create(),
                    new MenuItemInfo("Photographers", svgGroup, PhotographersView.class),
                    new MenuItemInfo("My Photos", FontAwesome.Solid.CAMERA_ALT.create(), MemberPhotosView.class), //
                    new MenuItemInfo("Upload", FontAwesome.Solid.UPLOAD.create(), UploadView.class), //
                    new MenuItemInfo("Me", svgMember, MeView.class), //

                    // new MenuItemInfo("Checkout Form", LineAwesomeIcon.CREDIT_CARD.create(), CheckoutFormView.class), //
            };
        }

    }

    private void addHeaderContent() {

        Header header = new Header();
        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.ROW,
                Width.FULL,
                Margin.NONE,
//                Padding.NONE,
                Padding.Horizontal.LARGE, Padding.Vertical.NONE,
//                Padding.Horizontal.LARGE,
                Gap.MEDIUM,
                AlignItems.CENTER, JustifyContent.BETWEEN);
        header.addClassName("header-bar");

        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");


        Div logoLayout = new Div();
//        logoLayout.addClassNames(Display.FLEX, AlignItems.CENTER, JustifyContent.CENTER,
//                Gap.XSMALL,
//                Margin.Vertical.NONE,
//                Padding.Vertical.NONE, Padding.Horizontal.NONE);
        logoLayout.addClassNames(Display.FLEX, AlignItems.CENTER, JustifyContent.CENTER,
                Gap.XSMALL,
                Margin.SMALL,
                Padding.NONE);

        H1 appName = new H1(APP_NAME);
        //appName.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.AUTO, FontSize.LARGE, FontWeight.BOLD, TextColor.TERTIARY);
        appName.addClassNames(FontSize.MEDIUM, FontWeight.SEMIBOLD, TextColor.TERTIARY,
                Padding.NONE, Margin.NONE);
//        appName.getStyle().set("font-family", "Times-New-Roman, serif");
//        appName.getStyle().set("font-stretch", "semi-expanded");
        appName.setClassName("brand-text");
        // appName.getStyle().setColor("#d64f00");//"#f9943b");//""#bd3450");

        Div divLogo = new Div();
        divLogo.add(VaadinIcon.CAMERA.create());
        divLogo.addClassName("logo-icon");
        // divLogo.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.LARGE, FontSize.LARGE, FontWeight.BOLD,TextColor.TERTIARY);
        divLogo.addClassNames(FontSize.MEDIUM, FontWeight.BOLD, TextColor.TERTIARY,
                Padding.NONE, Margin.NONE);
        //divLogo.getStyle().setColor("rgba(231, 24, 24, 0.5)");
        //divLogo.getStyle().setColor("#d64f00");

        logoLayout.add(toggle, divLogo, appName);


        HorizontalLayout layoutControls = new HorizontalLayout();
        if (isMobile) {
            layoutControls.addClassNames(
                    Gap.XSMALL,
                    Margin.NONE, Padding.NONE,
                    AlignItems.CENTER, JustifyContent.AROUND, FontSize.LARGE, FontWeight.BOLD
            );
        } else {
            layoutControls.addClassNames(
                    Gap.MEDIUM,
                    Margin.NONE, Padding.NONE,
                    AlignItems.CENTER, JustifyContent.AROUND, FontSize.LARGE, FontWeight.BOLD
            );
        }
        layoutControls.addClassName("actions");


        Avatar avatar = new Avatar("User Name");
        avatar.addThemeVariants(AvatarVariant.LUMO_SMALL);

//        AvatarGroup avatarGroup = new AvatarGroup();
//        int colorIndex = 0;
//
//        for (int i =0; i<3;i++) {
//            String name = "whoever "+i;//person.getFirstName() + " " + person.getLastName();
//            AvatarGroup.AvatarGroupItem avatarGroupItem = new AvatarGroup.AvatarGroupItem(name);
//            avatarGroupItem.setImage("https://randomuser.me/api/portraits/men/1"+i+".jpg");
//            avatar.setColorIndex(colorIndex++);
//            avatarGroup.add(avatarGroupItem);
//        }

        MenuBar menuBarUser = new MenuBar();
        menuBarUser.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);


        Text selected = new Text("");
        ComponentEventListener<ClickEvent<MenuItem>> listener = e -> selected
                .setText(e.getSource().getText());
        Div message = new Div(new Text("Clicked item: "), selected);

        StreamResource imageResourceUserSettings = new StreamResource("manager-icon.svg",
                () -> getClass()
                        .getResourceAsStream("/icons/manager-icon.svg"));
        SvgIcon svgUserSettings = new SvgIcon(imageResourceUserSettings);
        Button btnSettings = new Button();
        btnSettings.setIcon(svgUserSettings);

        Icon icon = LumoIcon.DROPDOWN.create();

        Popover popoverSettings = new Popover();
        popoverSettings.setModal(true);
        popoverSettings.setWidth("325px");
        popoverSettings.setAriaLabel("Select a date range");
        popoverSettings.setOpenOnFocus(true);
        popoverSettings.setFocusDelay(0);
        popoverSettings.setTarget(btnSettings);

//       ThemeSelect themeSelect = new ThemeSelect();
//       themeSelect.addClassNames("minimal");
//
//       ThemeRadioGroup themeRadioGroup = new ThemeRadioGroup();

        HorizontalLayout layoutSettings = new HorizontalLayout();
        layoutSettings.setSpacing(true);
        // layoutSettings.getThemeList().add("spacing-s");
        layoutSettings.setAlignItems(FlexComponent.Alignment.BASELINE);

        popoverSettings.add(btnSettings, layoutSettings);

        Button btnNotifications = new Button();
        btnNotifications.setIcon(VaadinIcon.BELL.create());

        Button btnMessages = new Button();
        btnMessages.setIcon(VaadinIcon.MAILBOX.create());

        Avatar userAvatarSmallPop = new Avatar(strNameOfUser);
        userAvatarSmallPop.setImage("https://randomuser.me/api/portraits/men/17.jpg");
        userAvatarSmallPop.getElement().setAttribute("tabindex", "-1");
        userAvatarSmallPop.addThemeVariants(AvatarVariant.LUMO_SMALL);
//        Avatar avatarUser = new Avatar(strNameOfUser);
//        avatarUser.getStyle().set("display", "block");
//        avatarUser.getStyle().set("cursor", "pointer");
//        avatarUser.getElement().setAttribute("tabindex", "-1");


        Button buttonUser = new Button(userAvatarSmallPop);
//        buttonUser.addThemeVariants(ButtonVariant.LUMO_ICON,  ButtonVariant.LUMO_TERTIARY_INLINE);
        buttonUser.getStyle().set("margin", "var(--lumo-space-s)");
        buttonUser.getStyle().set("margin-inline-start", "auto");
//        buttonUser.getStyle().set("border-radius", "50%");

        Popover popover = new Popover();
        popover.setModal(true);
        popover.setHoverDelay(50);
        popover.setOverlayRole("menu");
        popover.setAriaLabel("User menu");
        popover.setTarget(userAvatarSmallPop);
        popover.setPosition(PopoverPosition.BOTTOM_END);
        popover.addThemeVariants(PopoverVariant.LUMO_NO_PADDING);

        HorizontalLayout userInfo = new HorizontalLayout();
        userInfo.addClassName("userMenuHeader");
        userInfo.setSpacing(false);


        Avatar userAvatarLarge = new Avatar(strNameOfUser);
        userAvatarLarge.setImage("https://randomuser.me/api/portraits/men/17.jpg");
        userAvatarLarge.getElement().setAttribute("tabindex", "-1");
        userAvatarLarge.addThemeVariants(AvatarVariant.LUMO_XLARGE);

//        AvatarItem avatarItemMe = new AvatarItem("developer","the best developer !",userAvatarPop);

        VerticalLayout nameLayout = new VerticalLayout();
        nameLayout.setSpacing(false);
        nameLayout.setPadding(false);

        Div fullName = new Div(strNameOfUser);
        fullName.getStyle().set("font-weight", "bold");
        Div nickName = new Div("@" + strNameOfUser);
//        nickName.addClassName("userMenuNickname");
        nameLayout.add(fullName, nickName);

        userInfo.add(userAvatarLarge, nameLayout);

        VerticalLayout linksLayout = new VerticalLayout();
        linksLayout.setSpacing(false);
        linksLayout.setPadding(false);
        linksLayout.addClassName("userMenuLinks");

        Anchor profile = new Anchor("#", "User profile");
        profile.getElement().setAttribute("role", "menuitem");

        Anchor preferences = new Anchor("#", "Preferences");
        preferences.getElement().setAttribute("role", "menuitem");

        Anchor signOut = new Anchor("#", "Sign out");
        signOut.getElement().setAttribute("role", "menuitem");

        linksLayout.add(profile, preferences, signOut);
        popover.add(userInfo, linksLayout);

        if (isMobile) {
            layoutControls.add(btnNotifications, btnMessages, btnSettings, popoverSettings, buttonUser, popover);
        } else {
            //layoutControls.add(avatarGroup, btnNotifications, btnMessages, btnSettings, popoverSettings, buttonUser, popover);
            layoutControls.add(btnNotifications, btnMessages, btnSettings, popoverSettings, buttonUser, popover);
        }

        header.add(logoLayout);
//        header.add(logoLayout,layoutControls);

        addToNavbar(true, header);
    }


//    private void addDrawerContent() {
//        Span appName = new Span("simple-drawer");
//        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
//        Header header = new Header(appName);
//
//        Scroller scroller = new Scroller(createNavigation());
//
//        addToDrawer(header, scroller, createFooter());
//    }
//
//    private SideNav createNavigation() {
//        SideNav nav = new SideNav();
//
//        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
//        menuEntries.forEach(entry -> {
//            if (entry.icon() != null) {
//                nav.addItem(new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon())));
//            } else {
//                nav.addItem(new SideNavItem(entry.title(), entry.path()));
//            }
//        });
//
//        return nav;
//    }

    private Footer createFooter() {
        Footer layout = new Footer();

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getElement().executeJs("if (window.innerWidth <= 768) { this.drawerOpened = false; }");
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }


//   private Component createHeaderContent() {
//        Header header = new Header();
//        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.COLUMN,
//                Width.FULL,
//                Margin.SMALL, Padding.NONE,
////                Padding.Horizontal.LARGE,
//                Margin.XSMALL, Gap.MEDIUM,
//                AlignItems.CENTER, JustifyContent.BETWEEN);
//
//
//
//
//        Div layout = new Div();
//       if (isMobile) {
//           layout.addClassNames(Display.FLEX,  Width.FULL,
//                   Padding.Horizontal.LARGE,
//                   Margin.XSMALL, Gap.SMALL,
//                   AlignItems.START, JustifyContent.BETWEEN);
//       }else{
//           layout.addClassNames(Display.FLEX, Width.FULL,
//                   Padding.Horizontal.LARGE,
//                   Margin.SMALL, Margin.Vertical.NONE,
//                   AlignItems.STRETCH,JustifyContent.BETWEEN);
//       }
//
//
//       Div logoLayout = new Div();
//       logoLayout.addClassNames(Display.FLEX, AlignItems.CENTER,
//               Gap.XSMALL,
//               Margin.Vertical.NONE,
//               Padding.Vertical.NONE, Padding.Horizontal.LARGE);
//
//       H1 appName = new H1(APP_NAME);
//       appName.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.AUTO, FontSize.LARGE, FontWeight.BOLD, TextColor.TERTIARY);
//       appName.getStyle().set("font-family", "Times-New-Roman, serif");
//       appName.getStyle().set("font-stretch", "semi-expanded");
//       // appName.getStyle().setColor("#d64f00");//"#f9943b");//""#bd3450");
//
//       Div divLogo = new Div();
//       divLogo.add(VaadinIcon.CAMERA.create());
//       divLogo.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.LARGE, FontSize.LARGE, FontWeight.BOLD,TextColor.TERTIARY);
//       //divLogo.getStyle().setColor("rgba(231, 24, 24, 0.5)");
//       //divLogo.getStyle().setColor("#d64f00");
//
//       logoLayout.add(divLogo,appName);
//
//        Nav nav = new Nav();
//        nav.addClassNames(Display.FLEX, Overflow.AUTO, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL);
//
//        // Wrap the links in a list; improves accessibility
//        UnorderedList list = new UnorderedList();
//        list.addClassNames(Display.FLEX, Gap.SMALL, ListStyleType.NONE, Margin.NONE, Padding.NONE, AlignItems.CENTER, JustifyContent.CENTER);
//        nav.add(list);
//
//        for (MenuItemInfo menuItem : createMenuItems()) {
//            list.add(menuItem);
//        }
//
//        HorizontalLayout layoutControls = new HorizontalLayout();
//        if(isMobile){
//            layoutControls.addClassNames(
//                    Gap.XSMALL,
//                    Margin.NONE, Padding.NONE,
//                    AlignItems.CENTER, JustifyContent.AROUND, FontSize.LARGE, FontWeight.BOLD
//            );
//        } else {
//            layoutControls.addClassNames(
//                    Gap.MEDIUM,
//                    Margin.NONE, Padding.NONE,
//                    AlignItems.CENTER, JustifyContent.AROUND, FontSize.LARGE, FontWeight.BOLD
//            );
//        }
//
//       Avatar avatar = new Avatar("User Name");
//      avatar.addThemeVariants(AvatarVariant.LUMO_SMALL);
//
//       AvatarGroup avatarGroup = new AvatarGroup();
//       Integer colorIndex = (Integer) 0;
//
//       for (int i =0; i<2;i++) {
//           String name = "me "+i;//person.getFirstName() + " " + person.getLastName();
//           AvatarGroup.AvatarGroupItem avatarGroupItem = new AvatarGroup.AvatarGroupItem(name);
//           avatar.setColorIndex(colorIndex++);
//           avatarGroup.add(avatarGroupItem);
//       }
//
//       MenuBar menuBarUser = new MenuBar();
//       menuBarUser.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
//
//
//       Text selected = new Text("");
//       ComponentEventListener<ClickEvent<MenuItem>> listener = e -> selected
//               .setText(e.getSource().getText());
//       Div message = new Div(new Text("Clicked item: "), selected);
//
//       StreamResource imageResourceUserSettings = new StreamResource("manager-icon.svg",
//               () -> getClass()
//                       .getResourceAsStream("/icons/manager-icon.svg"));
//       SvgIcon svgUserSettings = new SvgIcon(imageResourceUserSettings);
//       Button btnSettings = new Button();
//       btnSettings.setIcon(svgUserSettings);
//
//       Icon icon = LumoIcon.DROPDOWN.create();
//
//       Popover popoverSettings = new Popover();
//       popoverSettings.setModal(true);
//       popoverSettings.setWidth("325px");
//       popoverSettings.setAriaLabel("Select a date range");
//       popoverSettings.setOpenOnFocus(true);
//       popoverSettings.setFocusDelay(0);
//       popoverSettings.setTarget(btnSettings);
//

    /// /       ThemeSelect themeSelect = new ThemeSelect();
    /// /       themeSelect.addClassNames("minimal");
    /// /
    /// /       ThemeRadioGroup themeRadioGroup = new ThemeRadioGroup();
//
//       HorizontalLayout layoutSettings = new HorizontalLayout();
//       layoutSettings.setSpacing(true);
//      // layoutSettings.getThemeList().add("spacing-s");
//       layoutSettings.setAlignItems(FlexComponent.Alignment.BASELINE);
//
//       popoverSettings.add(btnSettings, layoutSettings);
//
//       Button btnNotifications = new Button();
//       btnNotifications.setIcon(VaadinIcon.BELL.create());
//
//       Button btnMessages = new Button();
//       btnMessages.setIcon(VaadinIcon.MAILBOX.create());
//
//       Avatar avatarUser = new Avatar(strNameOfUser);
//       avatarUser.getStyle().set("display", "block");
//       avatarUser.getStyle().set("cursor", "pointer");
//       avatarUser.getElement().setAttribute("tabindex", "-1");
//
//       Button buttonUser = new Button(avatarUser);
//       buttonUser.addThemeVariants(ButtonVariant.LUMO_ICON,
//               ButtonVariant.LUMO_TERTIARY_INLINE);
//       buttonUser.getStyle().set("margin", "var(--lumo-space-s)");
//       buttonUser.getStyle().set("margin-inline-start", "auto");
//       buttonUser.getStyle().set("border-radius", "50%");
//
//       Popover popover = new Popover();
//       popover.setModal(true);
//       popover.setHoverDelay(50);
//       popover.setOverlayRole("menu");
//       popover.setAriaLabel("User menu");
//       popover.setTarget(buttonUser);
//       popover.setPosition(PopoverPosition.BOTTOM_END);
//       popover.addThemeVariants(PopoverVariant.LUMO_NO_PADDING);
//
//       HorizontalLayout userInfo = new HorizontalLayout();
//       userInfo.addClassName("userMenuHeader");
//       userInfo.setSpacing(false);
//
//       Avatar userAvatarPop = new Avatar(strNameOfUser);
//       //userAvatarPop.setImage(pictureUrl);
//       userAvatarPop.getElement().setAttribute("tabindex", "-1");
//       userAvatarPop.addThemeVariants(AvatarVariant.LUMO_LARGE);
//
//       VerticalLayout nameLayout = new VerticalLayout();
//       nameLayout.setSpacing(false);
//       nameLayout.setPadding(false);
//
//       Div fullName = new Div(strNameOfUser);
//       fullName.getStyle().set("font-weight", "bold");
//       Div nickName = new Div("@" + strNameOfUser);
//       nickName.addClassName("userMenuNickname");
//       nameLayout.add(fullName, nickName);
//
//       userInfo.add(userAvatarPop, nameLayout);
//
//       VerticalLayout linksLayout = new VerticalLayout();
//       linksLayout.setSpacing(false);
//       linksLayout.setPadding(false);
//       linksLayout.addClassName("userMenuLinks");
//
//       Anchor profile = new Anchor("#", "User profile");
//       profile.getElement().setAttribute("role", "menuitem");
//
//       Anchor preferences = new Anchor("#", "Preferences");
//       preferences.getElement().setAttribute("role", "menuitem");
//
//       Anchor signOut = new Anchor("#", "Sign out");
//       signOut.getElement().setAttribute("role", "menuitem");
//
//       linksLayout.add(profile, preferences, signOut);
//       popover.add(userInfo, linksLayout);
//
//       if (isMobile) {
//           layoutControls.add(btnNotifications, btnMessages, btnSettings, popoverSettings, buttonUser, popover);
//       } else {
//           layoutControls.add(avatarGroup, btnNotifications, btnMessages, btnSettings, popoverSettings, buttonUser, popover);
//       }
//
//        layout.add(logoLayout, layoutControls);
//
//        header.add(layout);
//        return header;
//    }
    private void createDrawer() {

        Div logoLayout = new Div();
        logoLayout.addClassName("sidebar-header");

        H1 appName = new H1(APP_NAME);
        appName.addClassName("brand-text");
//        appName.getStyle().set("font-stretch", "semi-expanded");
        VerticalLayout sidebarLayout = new VerticalLayout();
        sidebarLayout.setSizeFull();
        sidebarLayout.setPadding(false);
        sidebarLayout.setSpacing(false);
        sidebarLayout.addClassName("sidebar");

        Div divLogo = new Div();
        divLogo.add(VaadinIcon.CAMERA.create());
        divLogo.addClassName("logo-icon");

        Div toggleBtn = new Div();
        toggleBtn.addClassName("sidebar-toggle-btn");
        toggleBtn.add(FontAwesome.Solid.BARS.create());
        toggleBtn.addClickListener(e -> {
            drawerMinimized = !drawerMinimized;
            if (drawerMinimized) {
                sidebarLayout.addClassName("collapsed");
                getElement().getClassList().add("sidebar-collapsed");
                getElement().getStyle().set("--vaadin-app-layout-drawer-width", "90px");
            } else {
                sidebarLayout.removeClassName("collapsed");
                getElement().getClassList().remove("sidebar-collapsed");
                getElement().getStyle().set("--vaadin-app-layout-drawer-width", "290px");
            }
        });

        logoLayout.add(divLogo, appName, toggleBtn);

        layoutMenu.add(createSideMenu());
        Scroller scroller = new Scroller(layoutMenu);
        scroller.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Padding.NONE, Margin.NONE);

        // "Members" is pinned below the scrollable "Sections" list, fixed at the bottom of the sidebar.
        VerticalLayout sidebarFooter = createSidebarFooter();

        sidebarLayout.add(logoLayout, scroller, sidebarFooter);
        sidebarLayout.expand(scroller);

        addToDrawer(sidebarLayout);

    }

    /**
     * "Members" navigation — pinned to the bottom of the sidebar, outside the
     * scrollable "Sections" area, so it stays visible regardless of scroll position.
     */
    private VerticalLayout createSidebarFooter() {
        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.setPadding(false);
        leftLayout.setSpacing(false);
        leftLayout.addClassName("nav-wrapper");
        leftLayout.addClassName("sidebar-footer");

        SideNav navUser = new SideNav();
        navUser.setWidthFull();
        navUser.addClassName("label-text");

        Div divMemberStories = new Div(FontAwesome.Solid.PHOTO_FILM.create());
        navUser.addItem(createSideNavItem("Create Photo-Stories", divMemberStories, "Manage my photo-stories",
                MemberStoriesView.class));

        Div divMemberPhotos = new Div(FontAwesome.Solid.CAMERA_ALT.create());
        navUser.addItem(createSideNavItem("My Photos", divMemberPhotos, "Manage my photos",
                MemberPhotosView.class));

        Div divMembers = new Div(FontAwesome.Solid.UPLOAD.create());
        SideNavItem navItem = createSideNavItem("Upload Photos", divMembers, "Upload photos",
                UploadView.class);
        navItem.addClassName("sidebar-cta-btn");
        navUser.addItem(navItem);

        Div divMe = new Div(FontAwesome.Solid.USER.create());
        navUser.addItem(createSideNavItem("Me", divMe, "Manage my account",
                MeView.class));

        navUser.setLabel("Members");

        leftLayout.add(navUser);
        return leftLayout;
    }

    private VerticalLayout createSideMenu() {

        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.setSizeFull();
        leftLayout.setPadding(false);
        leftLayout.setSpacing(false);
        leftLayout.addClassName("nav-wrapper");

//        getElement().getStyle().set("--vaadin-app-layout-drawer-width", "278px");


        StreamResource imageResourceMember = new StreamResource("user-profile-icon.svg",
                () -> getClass()
                        .getResourceAsStream("/icons/user-profile-icon.svg"));
        SvgIcon svgMember = new SvgIcon(imageResourceMember);

        SvgIcon svgGroup = new SvgIcon(DownloadHandler.forClassResource(getClass(), "/icons/group-icon.svg"));

        StreamResource imageResourceStories = new StreamResource("story.svg",
                () -> getClass()
                        .getResourceAsStream("/icons/story.svg"));
        SvgIcon svgStories = new SvgIcon(imageResourceStories);

        String strColorOfMenuIcons = "#8d4c7c"; //"#985163"; // "#823b4d";//"#f9943b";//"#a62c5c";//"#7d1e32";

        SideNav nav = new SideNav();
        nav.setWidthFull();
        nav.addClassName("label-text");

        Div divImageHome = new Div(FontAwesome.Solid.HOME.create());
        nav.addItem(createSideNavItem("Home", divImageHome, "Introduction",
                HomeView.class));


        Div divImageNews = new Div(FontAwesome.Solid.NEWSPAPER.create());
        nav.addItem(createSideNavItem("News", divImageNews, "News and Updates",
                LearningsView.class));

        Div divStories = new Div(FontAwesome.Solid.PHOTO_FILM.create());
        nav.addItem(createSideNavItem("Photo-Stories", divStories, "Collections of photos from members",
                StoriesView.class));

        RouteParameters routeParametersDestination = new RouteParameters("destination-type", "Cities");
        RouteParameters routeParametersMonth = new RouteParameters("month-uploaded", STR_ALL_MONTHS);

        Div divImage = new Div(FontAwesome.Solid.IMAGE.create());
        SideNavItem itemPhotos = createSideNavItem("Photos", divImage, "Photos Uploaded over a Month",
                GalleryView.class, routeParametersMonth);
        nav.addItem(itemPhotos);

        Div divImageLocation = new Div(FontAwesome.Solid.LOCATION_DOT.create());
        SideNavItem itemPhotosLocation = createSideNavItem("In Location", divImageLocation, "Photos and Info about a Location",
                GalleryView.class, routeParametersDestination);
        nav.addItem(itemPhotosLocation);

//        itemPhotos.addItem(itemPhotosLocation);

//        Div divImageMonth = new Div(FontAwesome.Solid.CALENDAR_WEEK.create());
//        SideNavItem itemPhotosMonth = createSideNavItem("by Month", divImageMonth, "Photos Uploaded over a Month",
//                GalleryView.class, routeParametersMonth);
//        itemPhotos.addItem(itemPhotosMonth);



        Div divImageFestivals = new Div(VaadinIcon.CALENDAR_USER.create());
        nav.addItem(createSideNavItem("Events", divImageFestivals, "Photo events around the globe",
                FestivalsView.class));

        Div divImagePhotographers = new Div(svgGroup);
        nav.addItem(createSideNavItem("Photographers", divImagePhotographers, "Photographer Information",
                PhotographersView.class));

        nav.setLabel("Sections");

        leftLayout.add(nav);

        return leftLayout;
    }

    private SideNavItem createSideNavItem(String strTitle, Div menuIcon, String strSubTitle, Class<?> classToDirect) {
        return createSideNavItem(strTitle, menuIcon, strSubTitle, classToDirect, null);

    }

    private SideNavItem createSideNavItem(String strTitle, Div menuIcon, String strSubTitle, Class<?> classToDirect,
                                          RouteParameters routeParameters) {

        SideNavItem navItemHome;
        if (routeParameters == null) {
            navItemHome = new SideNavItem(strTitle, (Class<? extends Component>) classToDirect, menuIcon);
        } else {
            navItemHome = new SideNavItem(strTitle, (Class<? extends Component>) classToDirect, routeParameters, menuIcon);
        }

        navItemHome.setLabel(strTitle);

        if (strSubTitle != null && !strSubTitle.isEmpty()) {
            Popover popover = new Popover();
            popover.setOpenOnClick(false);
            popover.setOpenOnHover(true);
            popover.setHoverDelay(500);
            popover.setHideDelay(100);
            popover.setWidth("300px");
            popover.addThemeVariants(PopoverVariant.ARROW);
            popover.setPosition(PopoverPosition.END);
            popover.setModal(true);
            popover.setAriaLabelledBy("menu-popup");
            VerticalLayout popoverLayout = new VerticalLayout();
            popoverLayout.addClassName("menu-popover-content");
            popover.addClassName("menu-popover-content");
            H4 h4Home = new H4(strTitle);
            Div divText = new Div(strSubTitle);
            popoverLayout.add(h4Home, divText);
            popover.add(popoverLayout);
            popover.setTarget(navItemHome);
        }

        return navItemHome;

    }


//    private MenuItemInfo[] createMenuItems() {
//        return new MenuItemInfo[]{ //
//                new MenuItemInfo("Photography", LineAwesomeIcon.TH_LIST_SOLID.create(), GalleryView.class), //
//
//        };
//    }






    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, Component icon, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames(Display.FLEX, Gap.XSMALL, Height.MEDIUM, AlignItems.CENTER, Padding.Horizontal.SMALL, TextColor.BODY);
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames(FontWeight.MEDIUM, FontSize.MEDIUM, Whitespace.NOWRAP);

            if (icon != null) {
                link.add(icon);
            }
            link.add(text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }

    }




    // ---- Header with logo + lock ----
/*
    private Div createHeader() {
        Div header = new Div();
        header.addClassName("sidebar-header");

        Div logoGroup = new Div();
        logoGroup.addClassName("logo-group");

        // Logo icon (circle placeholder)
        Div logoIcon = new Div();
        logoIcon.addClassName("logo-icon");
        Icon logo = VaadinIcon.CODE.create();
        logo.addClassName("logo-svg");
        logoIcon.add(logo);

        Span brand = new Span("SideMenu");
        brand.addClassName("brand-text");

        logoGroup.add(logoIcon, brand);

        // Lock / unlock button
        Div lockBtn = new Div();
        lockBtn.addClassName("lock-btn");
        Icon lockIcon = VaadinIcon.LOCK.create();
        lockIcon.addClassName("lock-icon");
        lockBtn.add(lockIcon);

        lockBtn.addClickListener(e -> {
            locked = !locked;
            if (locked) {
                sidebar.addClassName("locked");
                lockIcon.getElement().removeAttribute("icon");
                lockBtn.removeAll();
                Icon unlocked = VaadinIcon.UNLOCK.create();
                unlocked.addClassName("lock-icon");
                lockBtn.add(unlocked);
            } else {
                sidebar.removeClassName("locked");
                lockBtn.removeAll();
                Icon lockedIcon = VaadinIcon.LOCK.create();
                lockedIcon.addClassName("lock-icon");
                lockBtn.add(lockedIcon);
            }
        });

        header.add(logoGroup, lockBtn);
        return header;
    }

    // ---- Navigation section with label + items ----
    private Div createSection(String label, NavItem... items) {
        Div section = new Div();
        section.addClassName("nav-section");

        Div sectionLabel = new Div();
        sectionLabel.addClassName("section-label");

        // Short line separator shown when collapsed
        Div separator = new Div();
        separator.addClassName("separator-line");

        Span labelText = new Span(label);
        labelText.addClassName("label-text");

        sectionLabel.add(separator, labelText);
        section.add(sectionLabel);

        for (NavItem item : items) {
            section.add(createNavLink(item));
        }

        return section;
    }

    // ---- Single nav link ----
    private Div createNavLink(NavItem item) {
        Div link = new Div();
        link.addClassName("nav-link");

        // Icon
        Div iconWrap = new Div();
        iconWrap.addClassName("nav-icon-wrap");

        Icon icon = resolveIcon(item.iconName);
        icon.addClassName("nav-icon");
        iconWrap.add(icon);

        // Label
        Span label = new Span(item.label);
        label.addClassName("nav-label");

        // Tooltip (visible when collapsed)
        Span tooltip = new Span(item.label);
        tooltip.addClassName("nav-tooltip");

        link.add(iconWrap, label, tooltip);

        link.addClickListener(e -> {
            // Remove active from all siblings
            link.getParent().ifPresent(parent -> {
                if (parent instanceof Div parentDiv) {
                    parentDiv.getChildren().forEach(child -> {
                        if (child instanceof Div d) {
                            d.removeClassName("active");
                        }
                    });
                }
            });
            link.addClassName("active");
        });

        return link;
    }

    // ---- Profile card at bottom ----
    private Div createProfile() {
        Div profile = new Div();
        profile.addClassName("sidebar-profile");

        Div avatar = new Div();
        avatar.addClassName("avatar");
        Icon userIcon = VaadinIcon.USER.create();
        userIcon.addClassName("avatar-icon");
        avatar.add(userIcon);

        Div info = new Div();
        info.addClassName("profile-info");
        Span name = new Span("David Oliva");
        name.addClassName("profile-name");
        Span email = new Span("david@example.com");
        email.addClassName("profile-email");
        info.add(name, email);

        Div logoutBtn = new Div();
        logoutBtn.addClassName("logout-btn");
        Icon logoutIcon = VaadinIcon.SIGN_OUT.create();
        logoutIcon.addClassName("logout-icon");
        logoutBtn.add(logoutIcon);

        profile.add(avatar, info, logoutBtn);
        return profile;
    }

    // ---- Resolve Vaadin icon from string ----
    private Icon resolveIcon(String iconName) {
        // iconName is like "vaadin:dashboard"
        String name = iconName.replace("vaadin:", "");
        try {
            VaadinIcon vi = VaadinIcon.valueOf(name.toUpperCase().replace("-", "_"));
            return vi.create();
        } catch (IllegalArgumentException e) {
            return VaadinIcon.CIRCLE.create();
        }
    }

    // ---- Simple record for nav items ----
    private record NavItem(String label, String iconName, String href) {}

*/




}