package com.photo.act.photo_act.views;


import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarGroup;
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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.vaadin.addons.themeselect.ThemeRadioGroup;
import org.vaadin.addons.themeselect.ThemeSelect;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    public static final String APP_VERSION = "2024.11.0.2";
    public static final String APP_NAME = "PhotoAct.net";

    public static final String HOSTNAME_LAPTOP = "mike-SATELLITE-PRO-C50-H-11G";

    private static final Logger logger = LoggerFactory.getLogger(MainLayout.class);
    private boolean isMobile;
    private String sysUsername;

    public static final String SECTION_HOME = "home";
    public static final String SECTION_GALLERY = "gallery";
    public static final String SECTION_FESTIVALS  = "festivals"; // clubs festivals exhibitions photowalks schools
    public static final String SECTION_WEBSITES  = "websites"; // clubs festivals exhibitions photowalks schools
    public static final String SECTION_CHALLENGES = "challenges";
    public static final String SECTION_LEARNINGS = "learnings";
    public static final String SECTION_CLUBS = "clubs";
    public static final String SECTION_LOCATIONS = "locations";
    public static final String SECTION_MY_FAVOURITES = "my-favourites";
    public static final String SECTION_MY_TEAMS = "my-teams";
    public static final String SECTION_MY_PHOTOS = "my-photos";

    public static final String SECTION_JOURNEYS = "journeys";

    public static final String SECTION_LOG = "log";

    public static final String strNameOfUser = "My Self";

    public MainLayout() {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String hostname = inetAddress.getHostName();

        isMobile = VaadinSession.getCurrent().getBrowser().isAndroid() || VaadinSession.getCurrent().getBrowser().isIPhone();

        logger.info("hostname:" + hostname + " isMobile:" + isMobile);
        
        this.addToDrawer(createSideMenu());
        this.addToNavbar(createHeaderContent());
        this.setDrawerOpened(true);
    }

   private Component createHeaderContent() {
        Header header = new Header();
        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.COLUMN,
                Width.FULL,
                Margin.SMALL, Padding.NONE,
//                Padding.Horizontal.LARGE,
                Margin.XSMALL, Gap.MEDIUM,
                AlignItems.CENTER, JustifyContent.BETWEEN);


       DrawerToggle toggle = new DrawerToggle();
      // toggle.getStyle().setColor("#ffb703");
       toggle.setAriaLabel("Menu toggle");

        Div layout = new Div();
       if (isMobile) {
           layout.addClassNames(Display.FLEX,  Width.FULL,
                   Padding.Horizontal.LARGE,
                   Margin.XSMALL, Gap.SMALL,
                   AlignItems.START, JustifyContent.BETWEEN);
       }else{
           layout.addClassNames(Display.FLEX, Width.FULL,
                   Padding.Horizontal.LARGE,
                   Margin.SMALL, Margin.Vertical.NONE,
                   AlignItems.STRETCH,JustifyContent.BETWEEN);
       }

       Div logoLayout = new Div();
       logoLayout.addClassNames(Display.FLEX, AlignItems.CENTER,
               Gap.XSMALL,
               Margin.Vertical.NONE,
               Padding.Vertical.NONE, Padding.Horizontal.LARGE);



       H1 appName = new H1(APP_NAME);
       appName.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.AUTO, FontSize.LARGE, FontWeight.BOLD);
       appName.getStyle().set("font-family", "Times-New-Roman, serif");
       appName.getStyle().set("font-stretch", "semi-expanded");
       appName.getStyle().setColor("#d64f00");//"#f9943b");//""#bd3450");

       Div divLogo = new Div();
       divLogo.add(VaadinIcon.CAMERA.create());
       divLogo.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.LARGE, FontSize.LARGE, FontWeight.BOLD);
       //divLogo.getStyle().setColor("rgba(231, 24, 24, 0.5)");
       divLogo.getStyle().setColor("#d64f00");

       logoLayout.add(toggle,divLogo,appName);

        Nav nav = new Nav();
        nav.addClassNames(Display.FLEX, Overflow.AUTO, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL);

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames(Display.FLEX, Gap.SMALL, ListStyleType.NONE, Margin.NONE, Padding.NONE, AlignItems.CENTER, JustifyContent.CENTER);
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            list.add(menuItem);
        }

        HorizontalLayout layoutControls = new HorizontalLayout();
        if(isMobile){
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

       Avatar avatar = new Avatar("User Name");
      avatar.addThemeVariants(AvatarVariant.LUMO_SMALL);

       AvatarGroup avatarGroup = new AvatarGroup();
       int colorIndex = 0;

       for (int i =0; i<2;i++) {
           String name = "me "+i;//person.getFirstName() + " " + person.getLastName();
           AvatarGroup.AvatarGroupItem avatarGroupItem = new AvatarGroup.AvatarGroupItem(name);
           avatar.setColorIndex(colorIndex++);
           avatarGroup.add(avatarGroupItem);
       }

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

       ThemeSelect themeSelect = new ThemeSelect();
       themeSelect.addClassNames("minimal");

       ThemeRadioGroup themeRadioGroup = new ThemeRadioGroup();

       HorizontalLayout layoutSettings = new HorizontalLayout(themeRadioGroup);
       layoutSettings.setSpacing(true);
      // layoutSettings.getThemeList().add("spacing-s");
       layoutSettings.setAlignItems(FlexComponent.Alignment.BASELINE);

       popoverSettings.add(btnSettings, layoutSettings);

       Button btnNotifications = new Button();
       btnNotifications.setIcon(VaadinIcon.BELL.create());

       Button btnMessages = new Button();
       btnMessages.setIcon(VaadinIcon.MAILBOX.create());

       Avatar avatarUser = new Avatar(strNameOfUser);
       avatarUser.getStyle().set("display", "block");
       avatarUser.getStyle().set("cursor", "pointer");
       avatarUser.getElement().setAttribute("tabindex", "-1");

       Button buttonUser = new Button(avatarUser);
       buttonUser.addThemeVariants(ButtonVariant.LUMO_ICON,
               ButtonVariant.LUMO_TERTIARY_INLINE);
       buttonUser.getStyle().set("margin", "var(--lumo-space-s)");
       buttonUser.getStyle().set("margin-inline-start", "auto");
       buttonUser.getStyle().set("border-radius", "50%");

       Popover popover = new Popover();
       popover.setModal(true);
       popover.setHoverDelay(50);
       popover.setOverlayRole("menu");
       popover.setAriaLabel("User menu");
       popover.setTarget(buttonUser);
       popover.setPosition(PopoverPosition.BOTTOM_END);
       popover.addThemeVariants(PopoverVariant.LUMO_NO_PADDING);

       HorizontalLayout userInfo = new HorizontalLayout();
       userInfo.addClassName("userMenuHeader");
       userInfo.setSpacing(false);

       Avatar userAvatarPop = new Avatar(strNameOfUser);
       //userAvatarPop.setImage(pictureUrl);
       userAvatarPop.getElement().setAttribute("tabindex", "-1");
       userAvatarPop.addThemeVariants(AvatarVariant.LUMO_LARGE);

       VerticalLayout nameLayout = new VerticalLayout();
       nameLayout.setSpacing(false);
       nameLayout.setPadding(false);

       Div fullName = new Div(strNameOfUser);
       fullName.getStyle().set("font-weight", "bold");
       Div nickName = new Div("@" + strNameOfUser);
       nickName.addClassName("userMenuNickname");
       nameLayout.add(fullName, nickName);

       userInfo.add(userAvatarPop, nameLayout);

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
           layoutControls.add(avatarGroup, btnNotifications, btnMessages, btnSettings, popoverSettings, buttonUser, popover);
       }

        layout.add(logoLayout, layoutControls);

        header.add(layout);
        return header;
    }

    private Component createSideMenu(){

        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.addClassNames(AlignItems.CENTER, JustifyContent.START,
//                Margin.Top.MEDIUM,
//                Margin.Left.SMALL, Margin.Right.SMALL,
                Width.FULL,
                Gap.XSMALL,
                Margin.NONE,
                Padding.SMALL);


        String strColorOfMenuIcons = "#8d4c7c"; //"#985163"; // "#823b4d";//"#f9943b";//"#a62c5c";//"#7d1e32";

        SideNav navHome = new SideNav();
       // navHome.addClassName("sideMenuLinks");
        navHome.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                Margin.XSMALL,
                Padding.XSMALL,
                Gap.MEDIUM
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                Background.CONTRAST_5
        );


        Div divImageHome = new Div();
        divImageHome.getStyle().setColor(strColorOfMenuIcons);
        divImageHome.add(LineAwesomeIcon.HOME_SOLID.create());

        SideNavItem navItemHome = new SideNavItem("Home", ImageGalleryView.class,divImageHome);
        navItemHome.addClassName("left-menu");
        navItemHome.addClassNames(FontWeight.SEMIBOLD,
                Overflow.HIDDEN, //Width.FULL,
                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//                Padding.MEDIUM
//                Padding.Horizontal.MEDIUM,
                Padding.Vertical.SMALL
        );
        navHome.addItem(navItemHome);

        SideNav nav = new SideNav();
     //  nav.addClassName("sideMenuLinks");
        nav.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                Margin.NONE,
                Padding.MEDIUM,
                Gap.LARGE
        );



        Div divImageFestivals = new Div();
        divImageFestivals.getStyle().setColor(strColorOfMenuIcons);
        divImageFestivals.add(LineAwesomeIcon.OBJECT_GROUP.create());
        SideNavItem navItemPhotoFestivals = new SideNavItem("Festivals", ImageGalleryView.class,new RouteParameters("section", SECTION_FESTIVALS),divImageFestivals);
        navItemPhotoFestivals.addClassName("left-menu");
        navItemPhotoFestivals.addClassNames(FontWeight.SEMIBOLD,
                Overflow.HIDDEN, //Width.FULL,
                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//                Padding.MEDIUM
//                Padding.Horizontal.MEDIUM,
                Padding.Vertical.SMALL
        );
        nav.addItem(navItemPhotoFestivals);

        Div divImageLearnings = new Div();
        divImageLearnings.getStyle().setColor(strColorOfMenuIcons);
        divImageLearnings.add(LineAwesomeIcon.BOOK_SOLID.create());
        SideNavItem navItemPhotoLearnings = new SideNavItem("Learnings", ImageGalleryView.class,new RouteParameters("section", SECTION_LEARNINGS), divImageLearnings);
//        navItemPhotoLearnings.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
//                Margin.NONE,
//                Padding.NONE);
        navItemPhotoLearnings.addClassName("left-menu");
        // navItemPhotoGallery.setClassName("lazy-left-menu");
        navItemPhotoLearnings.addClassNames(FontWeight.SEMIBOLD,
                Overflow.HIDDEN, //Width.FULL,
                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//                Padding.MEDIUM
//                Padding.Horizontal.MEDIUM,
                Padding.Vertical.SMALL
        );
        nav.addItem(navItemPhotoLearnings);

        Div divClubs = new Div();
        divClubs.getStyle().setColor(strColorOfMenuIcons);
        divClubs.add(LineAwesomeIcon.IMAGE.create());
        SideNavItem navItemClubs = new SideNavItem("Photo Clubs", ImageGalleryView.class,new RouteParameters("section", SECTION_CLUBS), divClubs);
//        navItemClubs.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
//                Margin.NONE,
//                Padding.NONE);
        navItemClubs.addClassName("left-menu");
        navItemClubs.addClassNames(FontWeight.SEMIBOLD,
                Overflow.HIDDEN, //Width.FULL,
                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//                Padding.MEDIUM
//                Padding.Horizontal.MEDIUM,
                Padding.Vertical.SMALL
        );
        nav.addItem(navItemClubs);

        Div divLocations = new Div();
        divLocations.getStyle().setColor(strColorOfMenuIcons);
        divLocations.add(LineAwesomeIcon.GLOBE_SOLID.create());
        SideNavItem navItemLocations = new SideNavItem("Locations", ImageGalleryView.class,new RouteParameters("section", SECTION_LOCATIONS), divLocations);
        navItemLocations.addClassName("left-menu");
        navItemLocations.addClassNames(FontWeight.SEMIBOLD,
                Overflow.HIDDEN, //Width.FULL,
                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//                Padding.MEDIUM
//                Padding.Horizontal.MEDIUM,
                Padding.Vertical.SMALL
        );
        nav.addItem(navItemLocations);

        Div divImageLinks = new Div();
        divImageLinks.getStyle().setColor(strColorOfMenuIcons);
        divImageLinks.add(LineAwesomeIcon.LINK_SOLID.create());
        SideNavItem navItemPhotoLinks = new SideNavItem("Websites", ImageGalleryView.class,new RouteParameters("section", SECTION_WEBSITES), divImageLinks);
        navItemPhotoLinks.addClassName("left-menu");
        navItemPhotoLinks.addClassNames(FontWeight.SEMIBOLD,
                Overflow.HIDDEN, //Width.FULL,
                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//                Padding.MEDIUM
//                Padding.Horizontal.MEDIUM,
                Padding.Vertical.SMALL
        );
        //        navItemPhotoLinks.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
//                Margin.NONE,
//                Padding.NONE);
//         navItemPhotoGallery.setClassName("left-menu");
        //navItemPhotoFestivals.getStyle().setFontWeight(Style.FontWeight.BOLD);
        nav.addItem(navItemPhotoLinks);

        Div divImageGallery = new Div();
        divImageGallery.getStyle().setColor(strColorOfMenuIcons);
        divImageGallery.add(LineAwesomeIcon.IMAGES_SOLID.create());
        SideNavItem navItemPhotoGallery = new SideNavItem("Image Gallery", ImageGalleryView.class,new RouteParameters("section", SECTION_GALLERY), divImageGallery );
        navItemPhotoGallery.addClassName("left-menu");
        navItemPhotoGallery.addClassNames(FontWeight.SEMIBOLD,
                Overflow.HIDDEN, //Width.FULL,
                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//                Padding.MEDIUM
//                Padding.Horizontal.MEDIUM,
                Padding.Vertical.SMALL
        );
        nav.addItem(navItemPhotoGallery);


        SideNav navUser = new SideNav();
        navUser.addClassName("sideMenuLinks");
        navUser.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                Margin.XSMALL,
                Padding.MEDIUM,
                Gap.MEDIUM); //,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //Background.CONTRAST_5);

        Div divUserLinks = new Div();
        divUserLinks.getStyle().setColor(strColorOfMenuIcons);
        divUserLinks.add(LineAwesomeIcon.BOOKMARK_SOLID.create());
        SideNavItem navItemUserLinks = new SideNavItem("My Favourites", ImageGalleryView.class,new RouteParameters("section", SECTION_MY_FAVOURITES), divUserLinks);
        navItemUserLinks.addClassName("left-menu");
        navItemUserLinks.addClassNames(FontWeight.SEMIBOLD,
                Overflow.HIDDEN, //Width.FULL,
                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//                Padding.MEDIUM
//                Padding.Horizontal.MEDIUM,
                Padding.Vertical.SMALL
        );
        navUser.addItem(navItemUserLinks);

        Div divUserTeams = new Div();
        divUserTeams.getStyle().setColor(strColorOfMenuIcons);
        divUserTeams.add(VaadinIcon.GROUP.create());
        SideNavItem navItemUserTeams = new SideNavItem("My Teams", ImageGalleryView.class,new RouteParameters("section", SECTION_MY_TEAMS), divUserTeams);
        navItemUserTeams.addClassName("left-menu");
        navItemUserTeams.addClassNames(FontWeight.SEMIBOLD,
                Overflow.HIDDEN, //Width.FULL,
                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//                Padding.MEDIUM
//                Padding.Horizontal.MEDIUM,
                Padding.Vertical.SMALL
        );
        navUser.addItem(navItemUserTeams);

        Div divUserPhotos = new Div();
        divUserPhotos.getStyle().setColor(strColorOfMenuIcons);
        divUserPhotos.add(VaadinIcon.UPLOAD.create());
        SideNavItem navItemUserPhotos = new SideNavItem("My Photos", ImageGalleryView.class,new RouteParameters("section", SECTION_MY_PHOTOS), divUserPhotos);
        navItemUserPhotos.addClassName("left-menu");
        navItemUserPhotos.addClassNames(FontWeight.SEMIBOLD,
                Overflow.HIDDEN, //Width.FULL,
                Margin.Horizontal.SMALL, Margin.Vertical.NONE,
//                Padding.MEDIUM
//                Padding.Horizontal.MEDIUM,
                Padding.Vertical.SMALL
        );
        navUser.addItem(navItemUserPhotos);



        leftLayout.add(navHome, nav,navUser);
        return leftLayout;
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("Photography", LineAwesomeIcon.TH_LIST_SOLID.create(), ImageGalleryView.class), //

        };
    }

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
