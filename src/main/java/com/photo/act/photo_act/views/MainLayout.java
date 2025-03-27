package com.photo.act.photo_act.views;


import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    public static final String APP_VERSION = "2025.03.0.2";
    public static final String APP_NAME = "PhotoAct";

    public static final String HOSTNAME_LAPTOP = "mike-SATELLITE-PRO-C50-H-11G";

    private static final Logger logger = LoggerFactory.getLogger(MainLayout.class);
    private boolean isMobile;
    private String sysUsername;

    public static final String SECTION_HOME = "home";
    public static final String SECTION_GALLERY = "gallery";
    public static final String SECTION_ALBUMS = "albums";
    public static final String SECTION_FESTIVALS = "festivals"; // clubs festivals exhibitions photowalks schools
    public static final String SECTION_WEBSITES = "websites"; // clubs festivals exhibitions photowalks schools
    public static final String SECTION_LEARNINGS = "learnings";
    public static final String SECTION_CLUBS = "clubs";
    public static final String SECTION_LOCATIONS = "locations";
    public static final String SECTION_MY_FAVOURITES = "my-favourites";
    public static final String SECTION_MY_TEAMS = "my-teams";
    public static final String SECTION_MY_PHOTOS = "my-photos";
    public static final String SECTION_UPLOAD = "upload";
    public static final String SECTION_FEED = "feed";

    public static final String STR_ALL_MEMBERS = "all-members";
    public static final String STR_ALL_ALBUMS = "all-albums";
    public static final String STR_ALL_DESTINATIONS = "all-locations";

    public static final String SECTION_LOG = "log";

    public static final String strNameOfUser = "My Self";

    private int userId;
    private String strUsername;

//    public MainLayout() {
//        InetAddress inetAddress = null;
//        try {
//            inetAddress = InetAddress.getLocalHost();
//        } catch (UnknownHostException e) {
//            throw new RuntimeException(e);
//        }
//        String hostname = inetAddress.getHostName();
//
//        isMobile = VaadinSession.getCurrent().getBrowser().isAndroid() || VaadinSession.getCurrent().getBrowser().isIPhone();
//
//
//        userId = 1;
//        strUsername = "visitor-user";
//
//
//        logger.info("hostname:" + hostname + " isMobile:" + isMobile);
//
//        createDrawer();
//        this.addToNavbar(createHeaderContent());
//        this.setDrawerOpened(true);
//
//        this.setPrimarySection(Section.DRAWER);

    /// /        this.addDrawerContent();
    /// /        addHeaderContent();
//    }
    public MainLayout() {

        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String hostname = inetAddress.getHostName();

        isMobile = VaadinSession.getCurrent().getBrowser().isAndroid() || VaadinSession.getCurrent().getBrowser().isIPhone();


        userId = 1;
        strUsername = "visitor-user";


        logger.info("hostname:" + hostname + " isMobile:" + isMobile);

        addToNavbar(createHeaderContent());
    }

    private Component createHeaderContent() {
        Header header = new Header();
//        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.COLUMN, Width.FULL);
        if (isMobile) {
            header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.ROW, Width.FULL,
                    Padding.Horizontal.SMALL, Padding.Vertical.XSMALL,
                    Margin.SMALL,
                    Gap.SMALL
            );
        } else {
            header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.ROW, Width.FULL,
                    Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL,
                    Margin.SMALL,
                    Gap.MEDIUM
            );
        }

//        Div layout = new Div();
//        layout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.Horizontal.LARGE);
//
//        H1 appName = new H1("My App-header-menu");
//        appName.addClassNames(Margin.Vertical.MEDIUM, Margin.End.AUTO, FontSize.LARGE);
//        layout.add(appName);

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
        appName.getStyle().set("font-family", "Times-New-Roman, serif");
        appName.getStyle().set("font-stretch", "semi-expanded");
        // appName.getStyle().setColor("#d64f00");//"#f9943b");//""#bd3450");

        Div divLogo = new Div();
        divLogo.add(VaadinIcon.CAMERA.create());
        divLogo.addClassName("logo-icon");
        // divLogo.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.LARGE, FontSize.LARGE, FontWeight.BOLD,TextColor.TERTIARY);
        divLogo.addClassNames(FontSize.MEDIUM, FontWeight.BOLD, TextColor.TERTIARY,
                Padding.NONE, Margin.NONE);
        //divLogo.getStyle().setColor("#cd5c5c");

        logoLayout.add(divLogo, appName);

        Nav nav = new Nav();
        if (isMobile) {
            nav.addClassNames(Display.FLEX, Overflow.AUTO, Padding.Horizontal.XSMALL, Padding.Vertical.SMALL,
                    Gap.SMALL
            );
        } else {
            nav.addClassNames(Display.FLEX, Overflow.AUTO, Margin.NONE, Padding.NONE, //Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL,
                    Gap.MEDIUM
            );
        }

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        if (isMobile) {
            list.addClassNames(Display.FLEX, Gap.XSMALL, ListStyleType.NONE, Margin.NONE, Padding.NONE,
                    FontSize.XXSMALL, TextColor.TERTIARY
            );
        } else {
            list.addClassNames(Display.FLEX, Gap.MEDIUM, ListStyleType.NONE, Margin.NONE, Padding.NONE,
                    FontSize.MEDIUM, TextColor.TERTIARY
            );
        }
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            list.add(menuItem);
        }

        TextField txtObject1 = new TextField();
        TextField txtValue1 = new TextField();

        TextField txtObject2 = new TextField();
        TextField txtValue2 = new TextField();

        Button btnStyle = new Button("Style");
        btnStyle.addClickListener(e -> {
            UI.getCurrent().getElement().getStyle().set(txtObject1.getValue(), txtValue1.getValue());
            UI.getCurrent().getElement().getStyle().set(txtObject2.getValue(), txtValue2.getValue());
        });

        header.add(logoLayout, nav); //, txtObject1, txtValue1, txtObject2, txtValue2, btnStyle);
        return header;
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("Home", LineAwesomeIcon.HOME_SOLID.create(), HomeView.class), //  LineAwesomeIcon.PENCIL_RULER_SOLID.create(),
                new MenuItemInfo("Learnings", LineAwesomeIcon.BOOK_SOLID.create(), LearningsView.class), //  LineAwesomeIcon.PENCIL_RULER_SOLID.create(),
                // new MenuItemInfo("Festivals", LineAwesomeIcon.OBJECT_GROUP.create(), FestivalsView.class), //  LineAwesomeIcon.PENCIL_RULER_SOLID.create(),
                new MenuItemInfo("Albums", LineAwesomeIcon.PHOTO_VIDEO_SOLID.create(), ImageAlbumsView.class), //
                new MenuItemInfo("Photos", VaadinIcon.PICTURE.create(), ImageGalleryView.class), //
                new MenuItemInfo("Upload", VaadinIcon.UPLOAD.create(), UploadView.class), //

                // new MenuItemInfo("Checkout Form", LineAwesomeIcon.CREDIT_CARD.create(), CheckoutFormView.class), //
        };
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
        appName.getStyle().set("font-family", "Times-New-Roman, serif");
        appName.getStyle().set("font-stretch", "semi-expanded");
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
//        viewTitle.removeAll();
//        viewTitle.setText(getCurrentPageTitle());
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
////       ThemeSelect themeSelect = new ThemeSelect();
////       themeSelect.addClassNames("minimal");
////
////       ThemeRadioGroup themeRadioGroup = new ThemeRadioGroup();
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

//    private void createDrawer(){
//
//
//
//
//
//
//        Scroller scroller = new Scroller(createSideMenu());
//
//        addToDrawer( scroller, createFooter());
//
//    }

//    private VerticalLayout createSideMenu(){
//
//        VerticalLayout leftLayout = new VerticalLayout();
//        leftLayout.addClassNames(AlignItems.CENTER, JustifyContent.START,
////                Margin.Top.MEDIUM,
////                Margin.Left.SMALL, Margin.Right.SMALL,
//                Width.FULL,
//                Gap.XSMALL,
//                Margin.NONE,
//                Padding.SMALL);
//        leftLayout.addClassName("left-menu");
//
//        String strColorOfMenuIcons = "#8d4c7c"; //"#985163"; // "#823b4d";//"#f9943b";//"#a62c5c";//"#7d1e32";
//
//        SideNav navHome = new SideNav();
//       // navHome.addClassName("sideMenuLinks");
//        navHome.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
//                Margin.XSMALL,
//                Padding.XSMALL,
//                Gap.MEDIUM
//                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
////                Background.CONTRAST_5
//        );
//
//
//
//
//        SideNav nav = new SideNav();
//     //  nav.addClassName("sideMenuLinks");
//        nav.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
//                Margin.NONE,
//                Padding.MEDIUM,
//                Gap.LARGE
//        );
//
//        Div divImageHome = new Div();
//        divImageHome.add(LineAwesomeIcon.HOME_SOLID.create());
//        //        new RouteParameters("member", SECTION_GALLERY),
//        SideNavItem navItemHome = new SideNavItem("Photo Act", HomeView.class, divImageHome);
////        navItemPhotoGallery.addClassName("left-menu");
//        navItemHome.addClassNames(
//                Overflow.HIDDEN, //Width.FULL,
//                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
////                Padding.MEDIUM
////                Padding.Horizontal.MEDIUM,
//                // FontWeight.SEMIBOLD,TextColor.SECONDARY,
//                Padding.Vertical.SMALL
//        );
//        nav.addItem(navItemHome);
//
//
//        Div divImageGallery = new Div();
////        divImageGallery.getStyle().setColor(strColorOfMenuIcons);
//        divImageGallery.add(LineAwesomeIcon.IMAGES_SOLID.create());
////        new RouteParameters("member", SECTION_GALLERY),
//        SideNavItem navItemPhotoGallery = new SideNavItem("Image Gallery", ImageGalleryView.class,new RouteParameters("member", STR_ALL_MEMBERS), divImageGallery);
////        navItemPhotoGallery.addClassName("left-menu");
//        navItemPhotoGallery.addClassNames(
//                Overflow.HIDDEN, //Width.FULL,
//                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
////                Padding.MEDIUM
////                Padding.Horizontal.MEDIUM,
//                // FontWeight.SEMIBOLD,TextColor.SECONDARY,
//                Padding.Vertical.SMALL
//        );
//        nav.addItem(navItemPhotoGallery);
//
//
//        Div divImageFestivals = new Div();
////        divImageFestivals.getStyle().setColor(strColorOfMenuIcons);
//        divImageFestivals.add(LineAwesomeIcon.OBJECT_GROUP.create());
////        new RouteParameters("section", SECTION_FESTIVALS)
//        SideNavItem navItemPhotoFestivals = new SideNavItem("Festivals", FestivalsView.class,divImageFestivals);
////        navItemPhotoFestivals.addClassName("left-menu");
//        navItemPhotoFestivals.addClassNames(
//                Overflow.HIDDEN, //Width.FULL,
//                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
////                Padding.MEDIUM
////                Padding.Horizontal.MEDIUM,
//                //FontWeight.SEMIBOLD,TextColor.SECONDARY,
//                Padding.Vertical.SMALL
//        );
//        nav.addItem(navItemPhotoFestivals);
//
//        Div divImageLearnings = new Div();
////        divImageLearnings.getStyle().setColor(strColorOfMenuIcons);
//        divImageLearnings.add(LineAwesomeIcon.BOOK_SOLID.create());
////        ,new RouteParameters("section", SECTION_LEARNINGS)
//        SideNavItem navItemPhotoLearnings = new SideNavItem("Learnings", LearningsView.class, divImageLearnings);
////        navItemPhotoLearnings.addClassNames(
////                Overflow.HIDDEN, Width.FULL,
////                Margin.NONE,
////                Padding.NONE);
////        navItemPhotoLearnings.addClassName("left-menu");
//        // navItemPhotoGallery.setClassName("lazy-left-menu");
//        navItemPhotoLearnings.addClassNames(
//                Overflow.HIDDEN, //Width.FULL,
//                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
////                Padding.MEDIUM
////                Padding.Horizontal.MEDIUM,
//              //  FontWeight.SEMIBOLD,TextColor.SECONDARY,
//                Padding.Vertical.SMALL
//        );
//        nav.addItem(navItemPhotoLearnings);
//
//        Div divClubs = new Div();
////        divClubs.getStyle().setColor(strColorOfMenuIcons);
//        divClubs.add(LineAwesomeIcon.IMAGE.create());
////        ,new RouteParameters("section", SECTION_CLUBS)
//        SideNavItem navItemClubs = new SideNavItem("Photo Clubs", ClubsView.class, divClubs);
////        navItemClubs.addClassNames(
////                Overflow.HIDDEN, Width.FULL,
////                Margin.NONE,
////                Padding.NONE);
////        navItemClubs.addClassName("left-menu");
//        navItemClubs.addClassNames(
//                Overflow.HIDDEN, //Width.FULL,
//                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
////                Padding.MEDIUM
////                Padding.Horizontal.MEDIUM,
//              //  FontWeight.SEMIBOLD,TextColor.SECONDARY,
//                Padding.Vertical.SMALL
//        );
//        nav.addItem(navItemClubs);
//
////        Div divLocations = new Div();
////        divLocations.getStyle().setColor(strColorOfMenuIcons);
////        divLocations.add(LineAwesomeIcon.GLOBE_SOLID.create());
////        SideNavItem navItemLocations = new SideNavItem("Locations", ImageGalleryView.class,new RouteParameters("section", SECTION_LOCATIONS), divLocations);
////        navItemLocations.addClassName("left-menu");
////        navItemLocations.addClassNames(FontWeight.SEMIBOLD,
////                Overflow.HIDDEN, //Width.FULL,
////                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//////                Padding.MEDIUM
//////                Padding.Horizontal.MEDIUM,
////                Padding.Vertical.SMALL
////        );
////        nav.addItem(navItemLocations);
//
////        Div divImageLinks = new Div();
////        divImageLinks.getStyle().setColor(strColorOfMenuIcons);
////        divImageLinks.add(LineAwesomeIcon.LINK_SOLID.create());
////        SideNavItem navItemPhotoLinks = new SideNavItem("Websites", ImageGalleryView.class,new RouteParameters("section", SECTION_WEBSITES), divImageLinks);
////        navItemPhotoLinks.addClassName("left-menu");
////        navItemPhotoLinks.addClassNames(FontWeight.SEMIBOLD,
////                Overflow.HIDDEN, //Width.FULL,
////                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//////                Padding.MEDIUM
//////                Padding.Horizontal.MEDIUM,
////                Padding.Vertical.SMALL
////        );
////        nav.addItem(navItemPhotoLinks);
//
//
//
//
//        SideNav navUser = new SideNav();
////        navUser.addClassName("sideMenuLinks");
//        navUser.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
//                Margin.XSMALL,
//                Padding.MEDIUM,
//                Gap.MEDIUM); //,
//                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                //Background.CONTRAST_5);
//
////        Div divUserLinks = new Div();
////        divUserLinks.getStyle().setColor(strColorOfMenuIcons);
////        divUserLinks.add(LineAwesomeIcon.BOOKMARK_SOLID.create());
////        SideNavItem navItemUserLinks = new SideNavItem("My Favourites", ImageGalleryView.class,new RouteParameters("section", SECTION_MY_FAVOURITES), divUserLinks);
////        navItemUserLinks.addClassName("left-menu");
////        navItemUserLinks.addClassNames(FontWeight.SEMIBOLD,
////                Overflow.HIDDEN, //Width.FULL,
////                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//////                Padding.MEDIUM
//////                Padding.Horizontal.MEDIUM,
////                Padding.Vertical.SMALL
////        );
////        navUser.addItem(navItemUserLinks);
//
////        Div divUserTeams = new Div();
////        divUserTeams.getStyle().setColor(strColorOfMenuIcons);
////        divUserTeams.add(VaadinIcon.GROUP.create());
////        SideNavItem navItemUserTeams = new SideNavItem("My Teams", ImageGalleryView.class,new RouteParameters("section", SECTION_MY_TEAMS), divUserTeams);
////        navItemUserTeams.addClassName("left-menu");
////        navItemUserTeams.addClassNames(FontWeight.SEMIBOLD,
////                Overflow.HIDDEN, //Width.FULL,
////                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//////                Padding.MEDIUM
//////                Padding.Horizontal.MEDIUM,
////                Padding.Vertical.SMALL
////        );
////        navUser.addItem(navItemUserTeams);
//
////        Div divUserPhotos = new Div();
//////        divUserPhotos.getStyle().setColor(strColorOfMenuIcons);
////        divUserPhotos.add(VaadinIcon.PICTURE.create());
////        SideNavItem navItemUserPhotos = new SideNavItem("My Photos", ImageGalleryView.class,new RouteParameters("member", strUsername), divUserPhotos);
//////        navItemUserPhotos.addClassName("left-menu");
////        navItemUserPhotos.addClassNames(
////                Overflow.HIDDEN, //Width.FULL,
////                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//////                Padding.MEDIUM
//////                Padding.Horizontal.MEDIUM,
////                FontWeight.SEMIBOLD,TextColor.SECONDARY,
////                Padding.Vertical.SMALL
////        );
////        navUser.addItem(navItemUserPhotos);
//
//
//        Div divUserUpload = new Div();
////        divUserUpload.getStyle().setColor(strColorOfMenuIcons);
//        divUserUpload.add(VaadinIcon.UPLOAD.create());
////        new RouteParameters("member", strUsername),
//        SideNavItem navItemUserUpload = new SideNavItem("Upload", UploadView.class,divUserUpload);
////        navItemUserUpload.addClassName("left-menu");
//        navItemUserUpload.addClassNames(
//                Overflow.HIDDEN, //Width.FULL,
//                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
////                Padding.MEDIUM
////                Padding.Horizontal.MEDIUM,
////                FontWeight.SEMIBOLD,TextColor.SECONDARY,
//                Padding.Vertical.SMALL
//        );
//        navUser.addItem(navItemUserUpload);
//
////        Div divUserFeed = new Div();
//////        divUserFeed.getStyle().setColor(strColorOfMenuIcons);
////        divUserFeed.add(VaadinIcon.LIST.create());
////        SideNavItem navItemUserFeed = new SideNavItem("Feed", FeedView.class, divUserFeed);
//////        navItemUserFeed.addClassName("left-menu");
////        navItemUserFeed.addClassNames(
////                Overflow.HIDDEN, //Width.FULL,
////                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//////                Padding.MEDIUM
//////                Padding.Horizontal.MEDIUM,
////                FontWeight.SEMIBOLD,TextColor.SECONDARY,
////                Padding.Vertical.SMALL
////        );
////
////        navUser.addItem(navItemUserFeed);
//
//
//
//        leftLayout.add(navHome, nav, navUser);
//
//        return leftLayout;
//    }


//    private MenuItemInfo[] createMenuItems() {
//        return new MenuItemInfo[]{ //
//                new MenuItemInfo("Photography", LineAwesomeIcon.TH_LIST_SOLID.create(), ImageGalleryView.class), //
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


}
