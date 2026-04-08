package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.CacheService;
import com.photo.act.photo_act.services.EmailSendService;
import com.photo.act.photo_act.services.ImageService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.utils.UtilsString;
import com.photo.act.photo_act.views.components.AvatarItem;
import com.photo.act.photo_act.views.components.DialogMessage;
import com.photo.act.photo_act.views.components.GenericView;
import com.photo.act.photo_act.views.components.UploadImageCard;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.HomeView.*;
import static com.photo.act.photo_act.views.MainLayout.*;


//@RolesAllowed("Admin")
@AnonymousAllowed

@Route(value = "photographers") //":section?")
@RouteAlias(value = "photographer/:member", layout = MainLayout.class)
//@RouteAlias(value = ":section/:member?", layout = MainLayout.class)
//@Menu(order = 0, icon = "line-awesome/svg/th-list-solid.svg")
public class PhotographersView extends Main implements HasUrlParameter<String>, BeforeEnterObserver, HasComponents, HasDynamicTitle, HasStyle {

    private static final Logger logger = LoggerFactory.getLogger(PhotographersView.class);

    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
    String[] arrColumnsMember = {"userId", "username", "resident", "resident_country", "date_joined", "member_since", "member_for", "avatar_path",
            "name", "surname", "user_rights_id", "short_bio", "url_insta", "url_fb", "url_flickr", "url_yt", "url_website", "email", "resident", "resident_country",
            "count_photos", "count_albums"};
    String sqlMember = "SELECT " +
            "  usr.userId, usr.username, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined,  DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
            " , usr.avatar_path, usr.name, usr.surname, usr.email, usr.user_rights_id, usr.resident, usr.resident_country " +
            " , usr.short_bio, usr.url_fb, usr.url_yt, usr.url_insta, usr.url_flickr, usr.url_website " +
            " , ux.count_photos, ux.count_albums " +
            " FROM dbuser usr, dbuser_extra ux " +
            " WHERE usr.userId = ux.user_id ";
    String sqlMembers = "SELECT " +
            " usr.userId, usr.username, usr.username, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined,  " +
            " DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since , getDateDiffFromNow(usr.date_joined) AS member_for " +
            " , usr.avatar_path, name, surname, short_bio, url_insta, url_fb, url_flickr, url_yt, url_website, email, resident, resident_country " +
            " , esrx.count_photos, esrx.count_albums, esrx.count_learnings_ref " +
            //     "--  , pa.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
            " FROM dbuser usr, dbuser_extra esrx, dbuser_rights usrr " +
            " WHERE usr.userId = esrx.user_id " +
            " AND usrr.id = usr.user_rights_id " +
            " AND usrr.role <> 'Guest' ";

    String sqlMembersOrderBy = " ORDER BY usr.surname , usr.name ";

    private String[] arrDestinationNames = {"id", "city_name", "prefecture", "prefecture_capital", "country"};
    private String sqlReadDestination = "SELECT city_name, prefecture, prefecture_capital, country " +
            " FROM destination d " +
            " WHERE ( is_city_or_village = 1 OR is_city_or_village = 2 ) ";
    private String sqlReadDestinationOrder = " ORDER BY country ASC , city_name ASC ";
    @Autowired
    private CacheService cacheService;

    String[] arrColumnsMembers = {"userId", "username", "username", "resident", "date_joined", "member_since", "member_for",
            "avatar_path", "name", "surname", "short_bio", "url_insta", "url_fb", "url_flickr", "url_yt", "url_website", "email", "resident", "resident_country",
            "count_photos", "count_albums", "count_learnings"};
    private String sqlReadDestinationCountries = "SELECT distinct country, city_name, prefecture, prefecture_capital " +
            " FROM destination d " +
            " GROUP BY country " +
            " ORDER BY country ASC ";






    private String strColorOfIcons = "#a62f03"; //"#f9943b";//"#a62c5c";//"#7d1e32";
    private VerticalLayout verticalLayout;
    private String sessionid;
    private long sessionCreation;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String section = SECTION_MEMBERS;
    private String forMemberName;
    private RecordService recordService;
    private String strHeader;
    private String dirChar = FileSystems.getDefault().getSeparator();
    private String strMember;
    private String publicIp;
    private String strPath;
    private String hostname;
    private String hostAddress;
    private String canonicalHostname;
    private int userId;
    private String strUrlRequestToBeLogged;
    private String[] arrClubsColumnNames = {"org_name", "org_type", "org_type_parent", "city", "used_for", "country", "url", "url_local_events", "url_fb", "url_yt", "url_insta",
            "url_flickr", "url_wikipedia"};
    private String sqlShowClubsSelect = "SELECT id, org_name, org_type, org_type_parent , city , used_for , country , url , city, address, pc, country, map_x, map_y, url, " +
            " url_local_events, url_fb, url_yt, url_insta, url_flickr, url_wikipedia, " +
            " date_inserted, dateUpdated " +
            " FROM organizations o ";
    private String sqlShowClubsWhere = " WHERE o.org_type LIKE 'Club' ";
    private String sqlShowClubsOrder = " ORDER BY o.city ASC, o.org_name ASC";





    String[] arrColumnNamesGallery = {"name_org", "name_new", "title", "subtitle", "photo_type", "uploader", "uploaderId", "photo_type", "contains",
            "space_size", "space_size_medium", "space_size_thumb", "city_name", "meta_date", "date_inserted",
            "username", "name", "surname", "avatar_path", "member_since"
    };

    String sqlReadGallery = "SELECT pm.name_org, pm.name_new, pm.title, pm.subtitle, pm.photo_type, pm.uploader, pm.uploaderId, pm.photo_type, pm.contains_tags, " +
            " pm.space_size, pm.space_size_medium, pm.space_size_thumb,  d.city_name, DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date " +
            " , getDateDiffFromNow(pm.date_inserted) AS date_inserted " +
            " , usr.username, usr.name, usr.surname, usr.avatar_path, DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
            " FROM dbuser usr, photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id " +
            " WHERE pm.visible_to = 'ALL' " +
            " AND usr.userId = pm.uploaderId";
    String sqlGalleryOrderBy = " ORDER BY pm.date_inserted DESC, pm.title ASC, pm.meta_date DESC, pm.name_new ASC ";


    private UtilsDate utilsDate;
    private String sessionDateTime;
    private String strMailboxSend = "info@photoact.net";
    private int intUserId;
    private String strOS;
    private String strBrowser;
    private GenericView genericView;
    private UtilsString utilsString;
    private EmailSendService emailSendService;


    public PhotographersView(RecordService recordService, EmailSendService emailSendService) {
        this.recordService = recordService;
        this.emailSendService = emailSendService;

        utilsDate = new UtilsDate();
        utilsString = new UtilsString();
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
        getUserClientInfo();

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously
            strUrlRequestToBeLogged = currentUrl.toExternalForm();
        });

        verticalLayout.removeAll();

        verticalLayout.add(loadHeader("Photographers", "Photographer Information", ""));

        StreamResource imageResourceMember = new StreamResource("user-profile-icon.svg",
                () -> getClass()
                        .getResourceAsStream("/icons/user-profile-icon.svg"));
        SvgIcon svgMember = new SvgIcon(imageResourceMember);

//            Div divHaveToBeAMember = new Div("You have to be a member in order to upload photos!");

        HorizontalLayout layoutUser = new HorizontalLayout();

        if (!strMember.isEmpty() && !strMember.equalsIgnoreCase(STR_ALL_MEMBERS)) {
            if (genericView.checkIfMemberValueExists("username", strMember)) {
                String sqlMemberMe = sqlMembers + " AND usr.username = '" + strMember + "' ";

//          sqlMemberGallery = "( " + sqlMemberGallery1 + "  AND usr.username = '" + strMember + "' " + sqlMemberGallery1OrderBy +
//          ") UNION (" + sqlMemberGallery2 + "  AND usr.username = '" + strMember + "' " + sqlMemberGallery2OrderBy + " ) ";

                verticalLayout.add(loadMemberInfo(sqlMemberMe, arrColumnsMembers, false));
            } else {
                verticalLayout.add(new Div(" '" + strMember + "' is not a valid member!"));
            }
        } else {
            verticalLayout.add(loadMembersProfiles(sqlMembers + sqlMembersOrderBy, arrColumnsMembers, false));
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
        this.addClassNames("me-view");
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

    private VerticalLayout loadMemberInfoTabs(String sqlMember, String[] arrColumnsMember) {

        String strMemberId = genericView.checkIfAuthMemberId();
        String sqlMemberMe = sqlMember + " AND usr.username = '" + strMember + "' ";

        VerticalLayout layoutTabsAll = new VerticalLayout();
        layoutTabsAll.addClassNames(
                Height.FULL,
                Padding.MEDIUM, Margin.NONE,
                AlignItems.CENTER, JustifyContent.CENTER,
                Gap.LARGE
        );

        VerticalLayout memberInfo = new VerticalLayout();
        memberInfo.addClassNames("member-profile");
        memberInfo.add(loadMemberInfo(sqlMemberMe, arrColumnsMember, false));

        layoutTabsAll.add(memberInfo);

        TabSheet tabSheet = new TabSheet();
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_TABS_CENTERED);
        tabSheet.addClassNames(Width.FULL, Height.FULL, Padding.MEDIUM, Margin.NONE);

        String strCalledFrom = "loadMemberInfoTabs";

        List<Record> lstMember = recordService.findAll(sqlMemberMe, arrColumnsMember);

        Record record = lstMember.get(0);

        String strWidthOfFields = "330px";

        FormLayout formLayout = new FormLayout();
        formLayout.addClassNames(Height.FULL, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.NONE);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep(strWidthOfFields, 1));
//        formLayout.setExpandFields(true);
//        formLayout.setLabelsAside(true);

        String txtUserRights = record.getColumnData("user_rights_id");


        TextField txtUserName = new TextField();
        txtUserName.setValue(record.getColumnData("username"));
        txtUserName.setEnabled(false);
        txtUserName.setRequiredIndicatorVisible(true);
        txtUserName.setRequired(true);
        txtUserName.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtUserName, "Username");

        TextField txtName = new TextField();
        txtName.setValue(record.getColumnData("name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setRequired(true);
        txtName.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtName, "Name");

        TextField txtSurname = new TextField();
        txtSurname.setValue(record.getColumnData("surname"));
        txtSurname.setRequiredIndicatorVisible(true);
        txtSurname.setRequired(true);
        txtSurname.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtSurname, "Surname");

        TextField txtEmail = new TextField();
        txtEmail.setValue(record.getColumnData("email"));
        txtEmail.setRequiredIndicatorVisible(true);
        txtEmail.setRequired(true);
        txtEmail.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtEmail, "e-mail");

        TextArea txtShortBio = new TextArea(); //"Short Bio", "Write 2 or 3 phrases about yourself");
        txtShortBio.setMinRows(5);
        txtShortBio.setMaxLength(180);
        txtShortBio.setValue(record.getColumnData("short_bio"));
        txtShortBio.setRequiredIndicatorVisible(false);
        txtShortBio.setRequired(false);
        txtShortBio.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtShortBio, "Short Bio");

//        Select<String> txtResidentCountry = new Select<>();
//        List<Record> lstDestinationCountries = recordService.findAll(sqlReadDestinationCountries, arrDestinationNames);
//        List<String> lstCountryNames = new ArrayList<>();
//        for (int d = 0; d < lstDestinationCountries.size(); d++) {
//            lstCountryNames.add(lstDestinationCountries.get(d).getColumnData("country"));
//        }
//        txtResidentCountry.setItems(lstCountryNames);
//
//        txtResidentCountry.setValue(record.getColumnData("resident_country"));
//        txtResidentCountry.setRequiredIndicatorVisible(false);
////        txtResidentCountry.setRequired(false);
//        txtResidentCountry.setWidth(strWidthOfFields);
//        formLayout.addFormItem(txtResidentCountry, "Country");

        String sqlDestination = sqlReadDestination + " " + sqlReadDestinationOrder;

        Select<String> txtResident = new Select<>();
        List<Record> lstDestinations = recordService.findAll(sqlDestination, arrDestinationNames);
        List<String> lstCityNames = new ArrayList<>();
        for (int d = 0; d < lstDestinations.size(); d++) {
            lstCityNames.add(lstDestinations.get(d).getColumnData("city_name") + " (" + lstDestinations.get(d).getColumnData("country") + ")");
        }
        txtResident.setItems(lstCityNames);

        txtResident.setValue(record.getColumnData("resident"));

        txtResident.setRequiredIndicatorVisible(false);
//        txtResident.setRequired(false);
        txtResident.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtResident, "Resident");

        TextField txtResidentCountry = new TextField();
        txtResidentCountry.setVisible(false);
        txtResidentCountry.setValue(record.getColumnData("resident_country"));
        txtResidentCountry.setRequiredIndicatorVisible(false);
//        txtResidentCountry.setRequired(false);
        txtResidentCountry.setWidth(strWidthOfFields);
//        formLayout.addFormItem(txtResidentCountry, "Country");


        FormLayout formLayoutLinks = new FormLayout();
        formLayoutLinks.addClassNames(Height.FULL, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.NONE);
        formLayoutLinks.setResponsiveSteps(new FormLayout.ResponsiveStep(strWidthOfFields, 1));
//        formLayout.setExpandFields(true);
//        formLayout.setLabelsAside(true);


        TextField txtFacebook = new TextField();
        txtFacebook.setValue(record.getColumnData("url_fb"));
        txtFacebook.setEnabled(true);
        txtFacebook.setRequiredIndicatorVisible(true);
        txtFacebook.setRequired(false);
        txtFacebook.setWidth(strWidthOfFields);
        formLayoutLinks.addFormItem(txtFacebook, "Facebook");

        TextField txtInstagram = new TextField();
        txtInstagram.setValue(record.getColumnData("url_insta"));
        txtInstagram.setRequiredIndicatorVisible(true);
        txtInstagram.setRequired(false);
        txtInstagram.setWidth(strWidthOfFields);
        formLayoutLinks.addFormItem(txtInstagram, "Instagram");

        TextField txtYT = new TextField();
        txtYT.setValue(record.getColumnData("url_yt"));
        txtYT.setRequiredIndicatorVisible(true);
        txtYT.setRequired(false);
        txtYT.setWidth(strWidthOfFields);
        formLayoutLinks.addFormItem(txtYT, "YouTube");

        TextField txtFlickr = new TextField();
        txtFlickr.setValue(record.getColumnData("url_flickr"));
        txtFlickr.setRequiredIndicatorVisible(true);
        txtFlickr.setRequired(false);
        txtFlickr.setWidth(strWidthOfFields);
        formLayoutLinks.addFormItem(txtFlickr, "Flickr");

        TextField txtWebsite = new TextField();
        txtWebsite.setValue(record.getColumnData("url_website"));
        txtWebsite.setRequiredIndicatorVisible(true);
        txtWebsite.setRequired(false);
        txtWebsite.setWidth(strWidthOfFields);
        formLayoutLinks.addFormItem(txtWebsite, "Personal Website");

        VerticalLayout layoutCodes = new VerticalLayout();
        layoutCodes.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);

        TextField txtNewCode = new TextField();
        txtNewCode.setLabel("Code");

        Button btnApply = new Button("Apply");
        layoutCodes.add(txtNewCode, btnApply);

        VerticalLayout layoutButtons = new VerticalLayout();
        layoutButtons.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.LARGE);

        Span tab1Icon = new Span();
        tab1Icon.add(FontAwesome.Solid.USER.create());
        Span tab1 = new Span("Profile");
        tab1.addClassNames(FontWeight.BOLD, Padding.MEDIUM);
        tab1Icon.add(tab1);
//        tab1.getStyle().setColor("#466ca8");

        Span tab2Icon = new Span();
        tab2Icon.add(FontAwesome.Solid.EDIT.create());
        Span tab2 = new Span("Edit Profile");
        tab2.addClassNames(FontWeight.BOLD, Padding.MEDIUM);
        tab2Icon.add(tab2);

        Span tab3Icon = new Span();
        tab3Icon.add(FontAwesome.Solid.PHOTO_FILM.create());
        Span tab3 = new Span("Profile Photos");
        tab3.addClassNames(FontWeight.BOLD, Padding.MEDIUM);
        tab3Icon.add(tab3);

        Span tab4Icon = new Span();
        tab4Icon.add(FontAwesome.Solid.LINK.create());
        Span tab4 = new Span("External Links");
        tab4.addClassNames(FontWeight.BOLD, Padding.MEDIUM);
        tab4Icon.add(tab4);

        Span tab5Icon = new Span();
        tab5Icon.add(FontAwesome.Solid.CODE.create());
        Span tab5 = new Span("Tools");
        tab5.addClassNames(FontWeight.BOLD, Padding.MEDIUM);
        tab5Icon.add(tab5);

        //----------------------

        //tabSheet.add(tab1Icon, loadMemberInfo(sqlMemberMe, arrColumnsMember, false));
        tabSheet.add(tab2Icon, formLayout);
        tabSheet.add(tab4Icon, formLayoutLinks);
        tabSheet.add(tab5Icon, layoutButtons);


        //--------------------------

        Button btnOk = new Button("Update");
        btnOk.setIcon(FontAwesome.Solid.SAVE.create());


        Button btnRefreshPhotosMeta = new Button("Refresh My Photos Meta");
        btnRefreshPhotosMeta.addClickListener(btn -> {
            reUpdateMyPhotoMetadata(Integer.parseInt(strMemberId));
        });

        Button btnRefreshPhotosCounts = new Button("Refresh My Photos Sums");
        btnRefreshPhotosCounts.addClickListener(btn -> {
            reUpdateMyPhotoCounts(Integer.parseInt(strMemberId));
        });

        layoutButtons.add(btnRefreshPhotosMeta, btnRefreshPhotosCounts);

        Button btnEvictCache = new Button("Evict All Photos Cache");
        btnEvictCache.addClickListener(event -> {
            cacheService.evictAllPhotos();
        });


        Button btnCalcAMembersSums = new Button("ReCalculate a Member's Photos Sums");
        btnCalcAMembersSums.addClickListener(event -> {

            DialogMessage dlg = new DialogMessage("ReCalculate a Member's Photos Sums");
            TextField textField = new TextField();
            textField.setLabel("Type Member's id (integer)");
            textField.setWidthFull();
            Button btnApplyNClose = new Button("Apply");
            btnApplyNClose.addClickListener(click -> {
                if (textField.isEmpty()) {
                    String errorMessage = "Empty member id!";
                    Notification notification = Notification.show(
                            errorMessage,
                            3000,
                            Notification.Position.TOP_CENTER
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                } else {
                    reUpdateMyPhotoCounts(Integer.parseInt(textField.getValue()));
                }
            });
            dlg.add(textField, btnApplyNClose);
            dlg.open();

        });


        Button btnRefreshAMembersPhotosMeta = new Button("Refresh a Member's Photos Meta");
        btnRefreshAMembersPhotosMeta.addClickListener(btn -> {
            DialogMessage dlg = new DialogMessage("Refresh a Member's Photos Meta");
            TextField textField = new TextField();
            textField.setLabel("Type Member's id (integer)");
            textField.setWidthFull();
            Button btnApplyNClose = new Button("Apply");
            btnApplyNClose.addClickListener(click -> {
                if (textField.isEmpty()) {
                    String errorMessage = "Empty member id!";
                    Notification notification = Notification.show(
                            errorMessage,
                            3000,
                            Notification.Position.TOP_CENTER
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                } else {
                    reUpdateMyPhotoMetadata(Integer.parseInt(textField.getValue()));
                }
            });
            dlg.add(textField, btnApplyNClose);
            dlg.open();
        });

        VerticalLayout layoutTabs = new VerticalLayout();
        layoutTabs.addClassNames(Width.FULL, Height.FULL,
                AlignItems.CENTER, JustifyContent.START);
        layoutTabs.addClassNames("member-edit-profile");
        layoutTabs.add(tabSheet, btnOk);


        layoutTabsAll.add(layoutTabs);
        if (txtUserRights.equalsIgnoreCase("3")) {
            layoutButtons.add(btnRefreshAMembersPhotosMeta, btnCalcAMembersSums, btnEvictCache);
        }


        btnOk.addClickListener(click -> {

            if (txtName.getValue().isEmpty()) {
                String strMessage = "Name should not be empty!";
                txtName.setErrorMessage(strMessage);
                Notification.show(strMessage);
            }

            if (txtSurname.getValue().isEmpty()) {
                String strMessage = "Surname should not be empty!";
                txtSurname.setErrorMessage(strMessage);
                Notification.show(strMessage);
            }

            if (txtUserName.getValue().isEmpty()) {
                String strMessage = "Username should not be empty!";
                txtUserName.setErrorMessage(strMessage);
                Notification.show(strMessage);
            }

            if (txtEmail.getValue().isEmpty()) {
                String strMessage = "Email should not be empty!";
                txtEmail.setErrorMessage(strMessage);
                Notification.show(strMessage);
            }

//            if (txtPassword.getValue().isEmpty()) {
//                String strMessage = "Password should not be empty!";
//                txtPassword.setErrorMessage(strMessage);
//                Notification.show(strMessage);
//            }

//            if (txtConfirmPassword.getValue().isEmpty()) {
//                String strMessage = "Confirm Password should not be empty!";
//                txtConfirmPassword.setErrorMessage(strMessage);
//                Notification.show(strMessage);
//            }

            String strEmail = txtEmail.getValue();
            String strUsername = txtUserName.getValue();


            boolean isEmailSystaxValid = utilsString.isEmailSysntaxValid(strEmail);
            if (!isEmailSystaxValid) {
                String strMessage = "Email is not valid!";
                txtEmail.setErrorMessage(strMessage);
                Notification.show(strMessage);
            }

//            if (!txtPassword.getValue().equalsIgnoreCase(txtConfirmPassword.getValue())) {
//                Notification.show("Password is not the same in both fields. Please retype.");
//            }

//            boolean doesEmailExist = genericView.checkIfMemerValueExists("email", strEmail);
//            if (doesEmailExist) {
//                Notification.show("Email " + strEmail + " already exists! Please type a different one.");
//                Notification notification = new Notification("Email " + strEmail + " already exists! Please type a different one.");
//                notification.setPosition(Notification.Position.MIDDLE);
//                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//            }

//            boolean doesUsernameExist = genericView.checkIfMemerValueExists("username", strUsername);
//            if (doesUsernameExist) {
//                Notification.show("Username " + strUsername + " already exists! Please type a different one.");
//                Notification notification = new Notification("Username " + strUsername + " already exists! Please type a different one.");
//                notification.setPosition(Notification.Position.MIDDLE);
//                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//            }

            if (txtUserName.getValue().isEmpty() || txtName.getValue().isEmpty() || txtSurname.getValue().isEmpty() || txtEmail.getValue().isEmpty())
//                    || !isEmailSystaxValid || txtPassword.getValue().isEmpty() || txtConfirmPassword.getValue().isEmpty()
//                    || !txtPassword.getValue().equalsIgnoreCase(txtConfirmPassword.getValue())
//                    || doesEmailExist || doesUsernameExist)
            {

            } else {
//                String txt = passwordEncoder().encode(txtPassword.getValue());   //utilsString.encrypt(txtPassword.getValue());
                updateMember(txtUserName.getValue(),
                        txtEmail.getValue(), txtName.getValue(), txtSurname.getValue(), txtResidentCountry.getValue(), txtResident.getValue(), txtShortBio.getValue(),
                        txtFacebook.getValue(), txtInstagram.getValue(), txtYT.getValue(), txtFlickr.getValue(), txtWebsite.getValue(),
                        section, strCalledFrom);

                memberInfo.removeAll();
                memberInfo.add(loadMemberInfo(sqlMemberMe, arrColumnsMember, false));
            }

        });


        return layoutTabsAll;
    }


    private int updateMember(String strUsername, String strEmail, String strName, String strSurname, String strResidentCountry, String strResident, String strShortBio,
                             String strFacebook, String strInstagram, String strYT, String strFlickr, String strWebsite,
                             String section, String strCalledFrom) {

        int retInt = 0;

        genericView.logVisitorToDb(section, strCalledFrom);

        String sqlInsert = "UPDATE dbuser SET email = ?, name = ? , surname = ?, resident_country = ?, resident = ?, short_bio = ?  " +
                " , url_fb = ?, url_insta = ?, url_YT = ?, url_flickr = ?, url_website = ? " +
                " WHERE username = ? ";


//        String strCodeForReferring = utilsString.generateRandomString(6);

        Object[] objInsert = {strEmail, strName, strSurname, strResidentCountry, strResident, strShortBio,
                strFacebook, strInstagram, strYT, strFlickr, strWebsite,
                strUsername};
        String[] arrTypeInsert = {"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String",
                "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String",
                "java.lang.String"};

        retInt = recordService.insertOneRecordWithQuery(sqlInsert, objInsert, arrTypeInsert);

        Notification notification = Notification.show("Information Updated !", 3000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        //       emailSendService.sendSimpleMail(strMailboxSend, "nickgiant@yahoo.com", "Member updated data", "From IP: " + publicIp + " username: " + strUsername +
        //               " email: " + strEmail + " Name: " + strName + " Surname: " + strSurname);


        return retInt;
    }

    private void loadUploadView(int intUserId, String strMember) {
        UploadImageCard uploadImageCard = new UploadImageCard(recordService, emailSendService, intUserId, strMember, sessionCreation, publicIp, hostname);
        uploadImageCard.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                Margin.SMALL,
                Padding.NONE,
                Gap.MEDIUM,
                Background.CONTRAST_5, BorderRadius.LARGE,
                AlignItems.STRETCH, //JustifyContent.BETWEEN,
                JustifyContent.EVENLY
        );
        uploadImageCard.setMinHeight("280px");

//            verticalLayout.add(uploadImageCard.getLocationSelectionLayout());
//        Button btnRefreshPhotoMeta = new Button("Refresh Photo Meta");
//        btnRefreshPhotoMeta.addClickListener(e -> {
//            reUpdateMyPhotoMetadata(intUserId);
//        });

//        HorizontalLayout layoutTextFilters = new HorizontalLayout();
//        TextField txtFrom = new TextField();
//        TextField txtTo = new TextField();
//        layoutTextFilters.add(txtFrom, txtTo);

//
//        Button btnRecompressPhotos = new Button("Recompress Large");
//        btnRecompressPhotos.addClickListener(e -> {
//            reCompressPhotos(4, txtFrom.getValue(), txtTo.getValue());
//        });
//
//        Button btnRecompressMediumPhotos = new Button("Recompress Medium");
//        btnRecompressMediumPhotos.addClickListener(e -> {
//            reCompressPhotos(3, txtFrom.getValue(), txtTo.getValue());
//        });
//
//        Button btnRecompressSmallPhotos = new Button("Recompress Small");
//        btnRecompressSmallPhotos.addClickListener(e -> {
//            reCompressPhotos(2, txtFrom.getValue(), txtTo.getValue());
//        });
//
//        Button btnRecompressThumbsPhotos = new Button("Recompress Thumbs");
//        btnRecompressThumbsPhotos.addClickListener(e -> {
//            reCompressPhotos(1, txtFrom.getValue(), txtTo.getValue());
//        });


        // layoutTextFilters, btnRecompressPhotos, btnRecompressSmallPhotos, btnRecompressMediumPhotos,

        verticalLayout.add(uploadImageCard.getUploadImageCard());
    }


    private Div loadUploadedPhotos(String sqlRead, String[] arrColumnNames, boolean isEditable, boolean isThumbnails) {


        strPath = DIR_PHOTOS_SERVER + dirChar;
        String strPath;
        if (!isThumbnails) {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathMedium;
        } else {
            strPath = DIR_PHOTOS_SERVER + dirChar + subPathSmall;
        }

//
//        Div layoutLastPhotos = new Div();
//        layoutLastPhotos.addClassNames(Overflow.HIDDEN,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Margin.NONE, Padding.MEDIUM);
/*                Width.FULL, AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Horizontal.XLARGE, Margin.Vertical.SMALL,
                Padding.Horizontal.XLARGE, Padding.Vertical.SMALL,
                Gap.LARGE);*/


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        Div layoutPhotoUploaded = new Div(); //[lstRecords.size()];
        if(isThumbnails) {
            layoutPhotoUploaded.addClassName("photographer-profile-thumb-photo-container");
        }else{
            layoutPhotoUploaded.addClassName("photographer-profile-photo-container");
        }

        for (int r = 0; r < lstRecords.size(); r++) {

            Div layoutPhotoUploadedPanel = new Div();
            layoutPhotoUploadedPanel.addClassNames(
                    AlignItems.CENTER, JustifyContent.CENTER,
                   Margin.NONE, Padding.XSMALL,
                    Gap.MEDIUM,
                    TextColor.TERTIARY
            );
            layoutPhotoUploadedPanel.addClassName("photographer-profile-photo");

            Record record = lstRecords.get(r);
            String strFileName = record.getColumnData("name_new");
            String strTitle = record.getColumnData("title");
            String strSubTitle = record.getColumnData("subtitle");
            String strPhotoType = record.getColumnData("photo_type");

            String strCityName = record.getColumnData("city_name");
            String strUploader = record.getColumnData("uploader");
            String strDateUploaded = record.getColumnData("date_inserted");

            String strUsername = record.getColumnData("username");
            String strName = record.getColumnData("name");
            String strSurname = record.getColumnData("surname");
            String strAvatarPath = record.getColumnData("avatar_path");
            String strMemberSince = record.getColumnData("member_since");

            Image image = getImageThumbFromDb(record, strPath);
            image.setWidthFull();
            image.setHeightFull();
/*            image.getStyle().setWidth("auto");
            image.getStyle().setMaxHeight("200px");*/
            image.addClassNames(BorderRadius.SMALL);

            Icon iconLocation = VaadinIcon.LOCATION_ARROW_CIRCLE_O.create();
            iconLocation.getStyle().set("padding", "var(--lumo-space-xs)");
            if (strCityName == null || strCityName.trim().equalsIgnoreCase("") || strCityName.trim().equalsIgnoreCase("null") || strCityName.isEmpty()) {
                strCityName = "not defined";
            }

            Span badgeLocation = new Span(iconLocation, new Span(strCityName));
            // badgeLocation.getElement().setAttribute("theme", "badge");
            badgeLocation.getElement().getThemeList().add("badge contrast");

            Div divUploadedAt = new Div("uploaded");
            divUploadedAt.addClassNames(FontSize.XSMALL);
/*            Div divLocation = new Div("photo shoot at");
            divLocation.addClassNames(FontSize.XSMALL);*/

//            Image imgAvatarMedium = genericView.getAvatarThumbImage(strAvatarPath, strName + " " + strSurname, "70px", "70px");
//            AvatarItem avatarLargeItemMe = new AvatarItem(strName + " " + strSurname, "@" + strUsername, imgAvatarMedium);

            Icon iconDateTime = VaadinIcon.CALENDAR_CLOCK.create();
            iconDateTime.getStyle().set("padding", "var(--lumo-space-xs)");
            Span badgeDateTime = new Span(iconDateTime, new Span(strDateUploaded));
            if (strDateUploaded.trim().isEmpty() || strDateUploaded.equalsIgnoreCase("null")) {
                badgeDateTime.setText("");
                badgeDateTime.setVisible(false);
            }
            badgeDateTime.getElement().getThemeList().add("badge contrast");

//            VerticalLayout layoutMemberUp = new VerticalLayout();
//            layoutMemberUp.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, Margin.NONE, Padding.SMALL);
//            layoutMemberUp.add(avatarLargeItemMe);

            VerticalLayout layoutDateLocationUp = new VerticalLayout();
            layoutDateLocationUp.addClassNames(AlignItems.END, JustifyContent.CENTER, Margin.NONE, Padding.XSMALL, Gap.XSMALL);
            layoutDateLocationUp.add(divUploadedAt, badgeDateTime);

            layoutPhotoUploadedPanel.add(image, layoutDateLocationUp);

            layoutPhotoUploadedPanel.getStyle().setOpacity("1");
            layoutPhotoUploaded.add(layoutPhotoUploadedPanel);
        }
        return layoutPhotoUploaded;
    }


    private Image getImageThumbFromDb(Record record, String strPathIn) {
        strPath = strPathIn;

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");

        String strCityName = record.getColumnData("city_name");
        String strUploader = record.getColumnData("uploader");

        if (strTitle == null || strTitle.isEmpty()) {
            strTitle = "image";
        }

        String strImagePath = strPath + dirChar + strFileName;
        logger.info(" strImagePath " + strImagePath);
        Image image = new Image();

        Path path = Paths.get(strImagePath);
        File file = path.toFile();

        image.setSrc(DownloadHandler.forFile(file));
        return image;
    }


    private void reUpdateMyPhotoMetadata(int intUserId) {
        ImageService imageService = new ImageService();
        imageService.updatePhotoMeta(recordService, intUserId);
    }

    private boolean reUpdateMyPhotoCounts(int intUserId) {

        Object[] fieldValueCount = {intUserId};
        String[] fieldTypeCount = {"java.lang.Integer"};

        String strUpdateCount = "UPDATE dbuser_extra AS d " +
                " JOIN ( " +
                "    SELECT uploaderId, COUNT(*) AS photo_count " +
                "    FROM photo_meta " +
                "    WHERE visible_to = 'ALL' " +
                "    GROUP BY uploaderId " +
                " ) AS p ON d.user_id = p.uploaderId " +
                " SET d.username = NULL , d.count_photos = p.photo_count " +
                " WHERE d.user_id = ? ";
        int retCount = recordService.insertOneRecordWithQuery(strUpdateCount, fieldValueCount, fieldTypeCount);

        int intAlbumUpd = reUpdateMyAlbumCounts(intUserId);

        if (retCount + intAlbumUpd == 2) {
            String errorMessage = "Update Success!";
            Notification notification = Notification.show(
                    errorMessage,
                    3000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;
        } else if (retCount == 1 && intAlbumUpd == 0) {

            String okMessage = "Photos sums calculated!";
            Notification okNotification = Notification.show(
                    okMessage,
                    3000,
                    Notification.Position.TOP_CENTER
            );
            okNotification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            String errorMessage = "No albums found!";
            Notification notification = Notification.show(
                    errorMessage,
                    3000,
                    Notification.Position.TOP_CENTER
            );
            //notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            return true;
        } else if (retCount == 0 && intAlbumUpd == 0) {
            String errorMessage = "No photos and albums found!";
            Notification notification = Notification.show(
                    errorMessage,
                    3000,
                    Notification.Position.TOP_CENTER
            );
            return false;
        }

        return false;
    }

    private int reUpdateMyAlbumCounts(int intUserId) {

        Object[] fieldValueCount = {intUserId};
        String[] fieldTypeCount = {"java.lang.Integer"};

        String strUpdateCount = "UPDATE dbuser_extra AS d " +
                " JOIN ( " +
                "    SELECT user_id, COUNT(*) AS album_count " +
                "    FROM photo_album " +
                "    WHERE album_visible_to = 'ALL' " +
                "    GROUP BY user_id " +
                " ) AS p ON d.user_id = p.user_id " +
                " SET d.username = NULL , d.count_albums = p.album_count " +
                " WHERE d.user_id = ? ";
        int intAlbumCount = recordService.insertOneRecordWithQuery(strUpdateCount, fieldValueCount, fieldTypeCount);

        return intAlbumCount;
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


    private String getFileSizeAsString(File file) {

        return String.format("%.4f", getFileSizeAsDouble(file));

    }

    private double getFileSizeAsDouble(File file) {

        double filesizeMB = (double) file.length() / (1024 * 1024);// + " mb";
        return filesizeMB;
    }


    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {

        logger.info(" photo  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames, Object[] sqlParValue, String[] sqlParType) {
        logger.info(" photo  getRecordsFromDb with params:   " + sql);
        return recordService.findAll(sql, arrColumnNames, sqlParValue, sqlParType);
    }


    private VerticalLayout loadMembersProfiles(String sqlRead, String[] arrColumnNames, boolean isEditable) {

        VerticalLayout layoutMembersAll = new VerticalLayout();
        layoutMembersAll.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.MEDIUM,
                Margin.NONE,
                Gap.XLARGE
//                BorderRadius.LARGE, Background.CONTRAST_5
        );


        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);

        for (int p = 0; p < lstRecords.size(); p++) {

            VerticalLayout layoutMember = new VerticalLayout();
            layoutMember.addClassNames(Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    TextColor.TERTIARY,
                    Padding.NONE,
                    Gap.SMALL
//                BorderRadius.LARGE, Background.CONTRAST_5
            );
            layoutMember.addClassNames("member-profile");


            if (lstRecords == null) {
                logger.warn(" lstRecords is null");
            } else if (lstRecords.isEmpty()) {
                logger.warn(" lstRecords is empty");
            } else {

                Record rec = lstRecords.get(p);
                String strUserId = rec.getColumnData("userId");
                intUserId = Integer.parseInt(strUserId);

                String strName = rec.getColumnData("name");
                String strSurname = rec.getColumnData("surname");

                String strUsername = rec.getColumnData("username");
                String strCountOfPhotosOfAlbums = rec.getColumnData("photo_count");
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

                String strCountAlbums = rec.getColumnData("count_albums");
                String strCountPhotos = rec.getColumnData("count_photos");

                Anchor linkWebsite = new Anchor();
                linkWebsite.add(FontAwesome.Solid.LINK.create());

                if (strWebsite != null && !strWebsite.equalsIgnoreCase("null") && !strWebsite.isEmpty()) {
                    linkWebsite.setVisible(true);
                    linkWebsite.setHref(strWebsite);
                    linkWebsite.setTarget("_blank");
                }

                Anchor linkTutorYt = new Anchor();
                linkTutorYt.add(FontAwesome.Brands.YOUTUBE.create());
                // linkTutorYt.getStyle().setColor(strColorExternalweb);
                // linkTutorYt.setClassName("lazy-result-line-button");

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

                if (strFlickr != null && !strFlickr.equalsIgnoreCase("null") && !strFlickr.isEmpty()) {

                    linkFlickr.setHref(strFlickr);
                    linkFlickr.setTarget("_blank");
//                linkFlickr.setVisible(true);
                }

                Div divBioTitle = new Div("Short Bio");
                divBioTitle.addClassNames(TextColor.TERTIARY, FontWeight.BOLD);

                VerticalLayout layoutMemberLinks = new VerticalLayout();
                layoutMemberLinks.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN);
                layoutMemberLinks.add(linkTutorFacebook, linkTutorYt, linkTutorInsta, linkTutorYt, linkFlickr, linkWebsite);
                layoutMemberLinks.setMaxWidth("60px");
                layoutMemberLinks.setMinWidth("50px");

                Div divBio = new Div();
//            divBio.setVisible(false);
                if (strShortBio != null && !strShortBio.equalsIgnoreCase("null") && !strShortBio.isEmpty()) {
                    divBio.setVisible(true);
                    divBio.setText(strShortBio);
                    divBio.setMaxWidth("360px");
                    divBio.setMinWidth("190px");
                } else {
//                divBio.setVisible(false);
                }

                Image imgAvatar = genericView.getAvatarThumbImage(strAvatarPath, strMember, "150px", "150px");

                H3 objName = new H3(strName + " " + strSurname);
                objName.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);
                H4 objMember = new H4(strUsername);
                objMember.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);

                HorizontalLayout layoutAll = new HorizontalLayout();

                VerticalLayout layoutMemberCard = new VerticalLayout();
                layoutMemberCard.setMinWidth("220px");
                layoutMemberCard.setMaxWidth("340px");

                layoutMemberCard.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER);
                layoutMemberCard.add(imgAvatar, objName);

                layoutAll.add(layoutMemberCard, layoutMemberLinks, divBio);

                VerticalLayout layoutStories = new VerticalLayout();
                layoutStories.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);
                layoutStories.addClassName("member-photos-count");
                Icon iconAlbum = FontAwesome.Solid.PHOTO_FILM.create();
                Div spAlbums = new Div("Albums");
                H2 divAlbums = new H2(strCountAlbums);
                layoutStories.add(divAlbums, spAlbums);

                VerticalLayout layoutPhotos = new VerticalLayout();
                layoutPhotos.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);
                layoutPhotos.addClassName("member-photos-count");
                Icon iconPhotos = VaadinIcon.PICTURE.create();
                Div spPhotos = new Div("Photos");
                H2 divPhotos = new H2(strCountPhotos);
                layoutPhotos.add(divPhotos, spPhotos);

                HorizontalLayout layoutCounts = new HorizontalLayout();
                layoutCounts.addClassNames(AlignItems.STRETCH, JustifyContent.AROUND,
                        Padding.LARGE, LumoUtility.Margin.NONE,
                        Gap.XLARGE);
                layoutCounts.add(layoutStories, layoutPhotos);
                layoutCounts.addClassName("member-count-panel");

                Div divResident = new Div("Lives at " + strResident);

                layoutMember.add(layoutAll, layoutCounts);

                String sqlGallerForMember = sqlReadGallery+" AND usr.userId = "+strUserId+" "+sqlGalleryOrderBy;

                Div layoutLastPhotos = new Div();
                layoutLastPhotos.addClassNames(Overflow.HIDDEN, Width.FULL, Height.FULL,
                        AlignItems.CENTER, JustifyContent.CENTER,
                        Margin.NONE, Padding.NONE,
                        Gap.LARGE
                );


                HorizontalLayout layoutTabViewPhotos = new HorizontalLayout();
                layoutTabViewPhotos.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);
                layoutTabViewPhotos.addClassName("tab-select");
                RadioButtonGroup<String> btnGroupShowPhotos = new RadioButtonGroup<>();
                btnGroupShowPhotos.setItems("Last 5 Photos", "Last 10 Photos", "Last 15 Photos");
                btnGroupShowPhotos.addValueChangeListener(e -> {
                    String strSelection = e.getSource().getValue();
                    if (strSelection.indexOf(" 5 ") != -1) {
                        layoutLastPhotos.removeAll();
                        layoutLastPhotos.add(loadUploadedPhotos(sqlGallerForMember + " LIMIT 5", arrColumnNamesGallery, false, true));
                    } else if (strSelection.indexOf(" 10 ") != -1) {
                        layoutLastPhotos.removeAll();
                        layoutLastPhotos.add(loadUploadedPhotos(sqlGallerForMember + " LIMIT 10", arrColumnNamesGallery, false, true));
                    } else if (strSelection.indexOf(" 15 ") != -1) {
                        layoutLastPhotos.removeAll();
                        layoutLastPhotos.add(loadUploadedPhotos(sqlGallerForMember + " LIMIT 15", arrColumnNamesGallery, false, true));
                    }
                });
                btnGroupShowPhotos.setValue("Last 5 Photos");
                layoutTabViewPhotos.add(btnGroupShowPhotos);

                layoutMember.add(layoutTabViewPhotos,layoutLastPhotos);

                layoutMembersAll.add(layoutMember);
            }
        }

        return layoutMembersAll;
    }


    private VerticalLayout loadMemberInfo(String sqlRead, String[] arrColumnNames, boolean isEditable) {

        VerticalLayout layoutMember = new VerticalLayout();
        layoutMember.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                TextColor.TERTIARY,
                Padding.NONE,
                Gap.SMALL
//                BorderRadius.LARGE, Background.CONTRAST_5
        );
        layoutMember.addClassNames("photographer-profile");

        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);

        if (lstRecords == null) {
            logger.warn(" lstRecords is null");
        } else if (lstRecords.isEmpty()) {
            logger.warn(" lstRecords is empty");
        } else if (lstRecords.size() == 1) {

            Record rec = lstRecords.get(0);
            String strUserId = rec.getColumnData("userId");
            intUserId = Integer.parseInt(strUserId);

            String strName = rec.getColumnData("name");
            String strSurname = rec.getColumnData("surname");

            String strUsername = rec.getColumnData("username");
            String strCountOfPhotosOfAlbums = rec.getColumnData("photo_count");
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

            String strCountAlbums = rec.getColumnData("count_albums");
            String strCountPhotos = rec.getColumnData("count_photos");

            Anchor linkWebsite = new Anchor();
            linkWebsite.add(FontAwesome.Solid.LINK.create());

            if (strWebsite != null && !strWebsite.equalsIgnoreCase("null") && !strWebsite.isEmpty()) {
                linkWebsite.setVisible(true);
                linkWebsite.setHref(strWebsite);
                linkWebsite.setTarget("_blank");
            }

            Anchor linkTutorYt = new Anchor();
            linkTutorYt.add(FontAwesome.Brands.YOUTUBE.create());
            // linkTutorYt.getStyle().setColor(strColorExternalweb);
            // linkTutorYt.setClassName("lazy-result-line-button");

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

            if (strFlickr != null && !strFlickr.equalsIgnoreCase("null") && !strFlickr.isEmpty()) {

                linkFlickr.setHref(strFlickr);
                linkFlickr.setTarget("_blank");
//                linkFlickr.setVisible(true);
            }

            Div divBioTitle = new Div("Short Bio");
            divBioTitle.addClassNames(TextColor.TERTIARY, FontWeight.BOLD);

            VerticalLayout layoutMemberLinks = new VerticalLayout();
            layoutMemberLinks.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN);
            layoutMemberLinks.add(linkTutorFacebook, linkTutorYt, linkTutorInsta, linkTutorYt, linkFlickr, linkWebsite);
            layoutMemberLinks.setMaxWidth("60px");
            layoutMemberLinks.setMinWidth("50px");

            Div divBio = new Div();
//            divBio.setVisible(false);
            if (strShortBio != null && !strShortBio.equalsIgnoreCase("null") && !strShortBio.isEmpty()) {
                divBio.setVisible(true);
                divBio.setText(strShortBio);
                divBio.setMaxWidth("360px");
                divBio.setMinWidth("190px");
            } else {
//                divBio.setVisible(false);
            }

            Image imgAvatar = genericView.getAvatarThumbImage(strAvatarPath, strMember, "150px", "150px");
//            Image imgAvatar = getAvatarImage(strAvatarPath, strNameOfUser, "120px", "120px");

            H3 objName = new H3(strName + " " + strSurname);
            objName.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);
            H4 objMember = new H4(strUsername);
            objMember.addClassNames(TextColor.SECONDARY, FontWeight.EXTRABOLD);
//            Div divMemberSince = new Div("Member since " + strMemberSince);
//            divMemberSince.addClassNames(TextColor.SECONDARY, FontWeight.MEDIUM);

            HorizontalLayout layoutAll = new HorizontalLayout();


            VerticalLayout layoutMemberCard = new VerticalLayout();
            layoutMemberCard.setMinWidth("220px");
            layoutMemberCard.setMaxWidth("340px");

//            layoutMemberCard.getStyle().set("border", "lightgrey 1px solid");
            layoutMemberCard.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, TextAlignment.CENTER);
            layoutMemberCard.add(imgAvatar, objName);

            layoutAll.add(layoutMemberCard, layoutMemberLinks, divBio);

            VerticalLayout layoutStories = new VerticalLayout();
            layoutStories.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);
            layoutStories.addClassName("member-photos-count");
            Icon iconAlbum = FontAwesome.Solid.PHOTO_FILM.create();
            Div spAlbums = new Div("Albums");
            H2 divAlbums = new H2(strCountAlbums);
            layoutStories.add(divAlbums, spAlbums);

            VerticalLayout layoutPhotos = new VerticalLayout();
            layoutPhotos.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);
            layoutPhotos.addClassName("member-photos-count");
            Icon iconPhotos = VaadinIcon.PICTURE.create();
            Div spPhotos = new Div("Photos");
            H2 divPhotos = new H2(strCountPhotos);
            layoutPhotos.add(divPhotos, spPhotos);

            HorizontalLayout layoutCounts = new HorizontalLayout();
            layoutCounts.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    Padding.MEDIUM, LumoUtility.Margin.NONE,
                    Gap.LARGE);
            layoutCounts.add(layoutStories, layoutPhotos);
            layoutCounts.addClassName("member-count-panel");

            layoutMember.add(layoutAll, layoutCounts);

            Div divResident = new Div("Lives at " + strResident);

            String sqlGallerForMember = sqlReadGallery+" AND usr.userId = "+strUserId+" "+sqlGalleryOrderBy;

            Div layoutLastPhotos = new Div();
            layoutLastPhotos.addClassNames(Overflow.HIDDEN, Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.NONE, Padding.NONE,
                    Gap.LARGE
            );
            layoutLastPhotos.addClassName("container-uploaded-lines");

            HorizontalLayout layoutTabViewPhotos = new HorizontalLayout();
            layoutTabViewPhotos.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER);
            layoutTabViewPhotos.addClassName("tab-select");
            RadioButtonGroup<String> btnGroupShowPhotos = new RadioButtonGroup<>();
            btnGroupShowPhotos.setItems("Last 5 Photos", "Last 10 Photos", "Last 15 Photos");
            btnGroupShowPhotos.addValueChangeListener(e -> {
                String strSelection = e.getSource().getValue();
                if (strSelection.indexOf(" 5 ") != -1) {
                    layoutLastPhotos.removeAll();
                    layoutLastPhotos.add(loadUploadedPhotos(sqlGallerForMember + " LIMIT 5", arrColumnNamesGallery, false, false));
                } else if (strSelection.indexOf(" 10 ") != -1) {
                    layoutLastPhotos.removeAll();
                    layoutLastPhotos.add(loadUploadedPhotos(sqlGallerForMember + " LIMIT 10", arrColumnNamesGallery, false, false));
                } else if (strSelection.indexOf(" 15 ") != -1) {
                    layoutLastPhotos.removeAll();
                    layoutLastPhotos.add(loadUploadedPhotos(sqlGallerForMember + " LIMIT 15", arrColumnNamesGallery, false, false));
                }
            });
            btnGroupShowPhotos.setValue("Last 5 Photos");
            layoutTabViewPhotos.add(btnGroupShowPhotos);


            layoutMember.add(layoutTabViewPhotos,layoutLastPhotos);



        } else {
            logger.warn(" lstRecords is more than one record");
        }

        return layoutMember;
    }


    private Dialog displayDialogSelectAvatar() {

        Dialog dlg = new Dialog("Select Avatar");
        dlg.setResizable(true);
        dlg.setDraggable(true);
        dlg.setCloseOnEsc(true);
        dlg.setCloseOnOutsideClick(true);
        dlg.setMinWidth("370px");
        dlg.setMaxWidth("450px");
        dlg.addClassName("me-view");

        VerticalLayout layoutAlbumsPanel = new VerticalLayout();
        layoutAlbumsPanel.addClassNames(Width.FULL,
                Padding.SMALL, Margin.NONE,
                AlignItems.CENTER, JustifyContent.CENTER,
                Background.CONTRAST_5, BorderRadius.LARGE);


        String strPathOfAvailAvatars = DIR_PHOTOS_SERVER + dirChar + SUB_PATH_AVAILABLE_AVATARS + dirChar;

        String[] arrColumnsAvailableAvatars = {"id", "title", "avatar_type", "description", "path"};
        String sqlAvailableAvatars = "SELECT id, title, avatar_type, description, path FROM avail_avatars ORDER BY title ";
        List<Record> lstAvatarRecords = getRecordsFromDb(sqlAvailableAvatars, arrColumnsAvailableAvatars);


        ListBox<Record> listBoxAvatar = new ListBox<>();
        listBoxAvatar.setItems(lstAvatarRecords);
        listBoxAvatar.setRenderer(new ComponentRenderer<>(record -> {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(FlexComponent.Alignment.CENTER);

            String strAvatarFile = record.getColumnData("path");
            String imagePath = strPathOfAvailAvatars + strAvatarFile;
            File imgFile = new File(imagePath);

            Image image = new Image();
            image.setSrc(DownloadHandler.forFile(imgFile));
            image.setAlt(imagePath);
            image.setHeight("75px");
            image.setWidth("75px");
            image.getStyle().setBorderRadius("8px");

            Div divAvatar = new Div(image);


            Span title = new Span(record.getColumnData("title"));
            title.getStyle()
                    .set("color", "var(--lumo-contrast-80pct)")
                    .set("font-size", "var(--lumo-font-size-m)");
            Span description = new Span(record.getColumnData("description"));
            description.getStyle()
                    .set("color", "var(--lumo-contrast-50pct)")
                    .set("font-size", "var(--lumo-font-size-s)");
            Span avatarType = new Span(record.getColumnData("avatar_type"));
            avatarType.getStyle()
                    .set("color", "var(--lumo-contrast-40pct)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("font-weight", "800");

            VerticalLayout column = new VerticalLayout(title, description, avatarType);
            column.setPadding(false);
            column.setSpacing(false);

            row.add(divAvatar, column);
            row.getStyle().set("line-height", "var(--lumo-line-height-m)");
            row.setWidthFull();
            return row;
        }));


        layoutAlbumsPanel.add(listBoxAvatar);

        // List<Record> lstAlbums = getRecordsFromDb(sqlMembersAlbums, arrColumnsMemberAlbums);
//        List<String> lstAlbumTitle = new ArrayList<>();
//        List<String> lstAlbumUserId = new ArrayList<>();
//        List<String> lstAlbumId = new ArrayList<>();
//        for (int i = 0; i < lstAvatarRecords.size(); i++) {
//            lstAlbumTitle.add(lstAvatarRecords.get(i).getColumnData("title"));
//            lstAlbumId.add(lstAvatarRecords.get(i).getColumnData("id"));
//            lstAlbumUserId.add(lstAvatarRecords.get(i).getColumnData("user_id"));
//        }

        HorizontalLayout layoutControls = new HorizontalLayout();
        layoutControls.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Padding.MEDIUM, Margin.NONE);

        Button btnSave = new Button("Set");
        btnSave.setIcon(FontAwesome.Solid.CHECK.create());
        btnSave.addClickListener(event -> {

            if (setAvatarPhotoForMember(listBoxAvatar.getValue().getColumnData("path"), genericView.checkIfAuthMemberId())) {
                Notification notification = new Notification("Avatar Updated!");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } else {

                Notification notification = new Notification("Avatar NOT Updated! Error logged to be investigated.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            // savePhotoInAlbums(listBoxAlbums, lstAlbumTitle, lstAlbumId, lstAlbumUserId, strPhotoId);
            dlg.close();
        });

        Button btnCancel = new Button("Cancel");
        btnCancel.setIcon(FontAwesome.Solid.CLOSE.create());
        btnCancel.addClickListener(event -> {
            dlg.close();
        });
        layoutControls.add(btnSave, btnCancel);
        dlg.add(layoutAlbumsPanel, layoutControls);
        return dlg;

    }


    private boolean setAvatarPhotoForMember(String strPath, String strMemberId) {

        Object[] strValues = {strPath, strMemberId};
        String[] arrTypes = {"java.lang.String", "java.lang.Integer"};

        String sqlUpdate = "UPDATE `dbuser` SET `avatar_path`= ? WHERE `userId`= ? ";
        if (recordService.insertOneRecordWithQuery(sqlUpdate, strValues, arrTypes) == 1) {
            return true;
        }
        return false;
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
            strUrlRequestToBeLogged = strUrlRequestToBeLogged.replace("'", "");
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
                + " item = '" + logText + "' , ref = " + strUrlRequestToBeLogged + " , "
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