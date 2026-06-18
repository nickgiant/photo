package com.photo.act.photo_act.views;

import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailSendService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.DialogRegistration;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.LoginDialog;
import com.photo.act.photo_act.views.components.RegistrationDialog;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.provisioning.UserDetailsManager;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static com.photo.act.photo_act.views.MainLayout.*;

// https://vaadin.com/docs/latest/building-apps/security/add-login/flow

@Route(value = "login", layout = MainLayout.class) //autoLayout = false)
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {


    private static final Logger logger = LoggerFactory.getLogger(LoginView.class);
    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String section = SECTION_LOGIN;
    private String forMemberName;
    private RecordService recordService;
    private String strHeader;
    private String publicIp;
    private String strPath;
    private String hostname;
    private String hostAddress;
    private String canonicalHostname;
    private UtilsDate utilsDate;
    private String sessionDateTime;

    private String strOS;
    private String strBrowser;
    private GenericView genericView;
//    private final LoginForm login;
    @Autowired
    UserDetailsManager userDetailsManager;
    private EmailSendService emailSendService;

    private String dirChar = FileSystems.getDefault().getSeparator();

    public LoginView(RecordService recordService, EmailSendService emailSendService) {
        this.recordService = recordService;
        this.emailSendService = emailSendService;
        utilsDate = new UtilsDate();
        genericView = new GenericView(recordService);

        constructUI();

        var i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("Login");

        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("Wrong credentials");
        i18nErrorMessage.setMessage("Wrong credentials. Please retype username and password.");
        i18n.setErrorMessage(i18nErrorMessage);

        H1 titlePage = new H1(APP_NAME);
        Span subTitle = new Span("[ Through Photography, We Connect and Act ]");

        Header siteHeader = new Header(titlePage, subTitle);
        siteHeader.addClassNames(LumoUtility.Width.FULL);

        Div divMainImage = new Div();
        Image mainImage = new Image();
        String strMainImagePath = DIR_PHOTOS_SERVER + dirChar + "photographer.png";

        Path path = Paths.get(strMainImagePath);
        File file = path.toFile();

        mainImage.setSrc(DownloadHandler.forFile(file));
        mainImage.setAlt("sketch image of a photographer");
        mainImage.setHeight("18rem");
        mainImage.setWidth("auto");
        mainImage.getStyle().setBorderRadius("20px");
        mainImage.getStyle().setPadding("10px");

        divMainImage.add(mainImage);

        Div div1 = new Div("We are a community site, with members exchanging info and links in order to improve our skills in photography!");
        Div div2 = new Div("Currently, we share info about events and learnings. Of course, we also have space for our photos and albums.");

        Button btnLogin = new Button("Login");
        btnLogin.addClassName("btn-register");
        btnLogin.addClickListener(click ->{
            displayLoginDialog();
        });

        Button btnRegister = new Button("Register");
        btnRegister.addClassName("btn-register");
        btnRegister.addClickListener(click -> {
            displayRegisterDialog();
        });

        HorizontalLayout layoutUserBtns = new HorizontalLayout();
        layoutUserBtns.setAlignItems(FlexComponent.Alignment.CENTER);
        layoutUserBtns.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);
        layoutUserBtns.setWrap(true);
        String usrName = genericView.checkIfAuthUserName();
        if (usrName == null) {
            layoutUserBtns.add(btnLogin,btnRegister);
        } else {
            mainImage.setHeight("16rem");
            mainImage.setWidth("auto");
            layoutUserBtns.add(genericView.getAuthUserPanel(usrName));
        }

        H2 titleLastPhotos = new H2("Content for members. Login or Register");

        this.addClassNames(LumoUtility.Width.FULL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER
        );

        this.add(siteHeader, divMainImage, div1, div2, titleLastPhotos, layoutUserBtns, genericView.loadFooter(isMobile));
    }

    @Override
    public String getPageTitle() {
        return strHeader;
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        getUserClientInfo();

        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
//            login.setError(true);
        }
    }

    private void constructUI() {
        addClassNames("home-view");
        addClassNames(LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                LumoUtility.Margin.NONE,
                LumoUtility.Padding.NONE,
                LumoUtility.Gap.MEDIUM,
                //  Padding.NONE, //.Left.MEDIUM, Padding.Right.MEDIUM,
                //Margin.Vertical.MEDIUM, Padding.Vertical.NONE,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER
        );

        DIR_PHOTOS_SERVER = genericView.getAppProps(PROP_PHOTOS);


        this.setWidthFull();

    }

    private void displayLoginDialog(){

        LoginDialog loginDialog = new LoginDialog();

        loginDialog.getLoginForm().setAction("login");
        loginDialog.open();

    }

    private void displayRegisterDialog() {
        RegistrationDialog dialogRegister = new RegistrationDialog(isMobile, "", sessionCreation, hostname, publicIp, recordService,
                section, "register-from-login-view", emailSendService);
        dialogRegister.open();
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


}
