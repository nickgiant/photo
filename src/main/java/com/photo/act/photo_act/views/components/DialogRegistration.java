package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailSendService;
import com.photo.act.photo_act.utils.UtilsString;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.FileSystems;
import java.util.List;

public class DialogRegistration extends Dialog {

    private static final Logger logger = LoggerFactory.getLogger(DialogRegistration.class);
    private RecordService recordService;
    private boolean isMobile;
    private GenericView genericView;
    private UtilsString utilsString;

    private String dirChar = FileSystems.getDefault().getSeparator();

    private EmailSendService emailSendService;

    private String strMailboxSend = "info@photoact.net";
    private String strUserReferCode;
    private String publicIp;

    @Autowired
    public DialogRegistration(EmailSendService emailSendService){
        this.emailSendService = emailSendService;
    }

    public DialogRegistration(boolean isMobile, String strUserReferCode, long sessionCreation,
                              String hostname, String publicIp, RecordService recordService, String section, String strCalledFrom) {
        this.recordService = recordService;
        this.strUserReferCode = strUserReferCode;
        this.publicIp = publicIp;
        this.isMobile = isMobile;

        int userId = 1;

        utilsString = new UtilsString();
        genericView = new GenericView(recordService, userId);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, Padding.SMALL, Gap.MEDIUM);



//        genericView = new GenericView(recordService, 1);

//        this.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN, TextAlignment.CENTER);


//
//        if (record == null) {
//            logger.error("record is null");
//        }
//
//
//        String strAlbumUserName = record.getColumnData("username");
//        String strAlbumNameOfUser = record.getColumnData("username");
//        String strUserResident = record.getColumnData("resident");
//        String strAvatarPath = record.getColumnData("avatar_path");
//        String strUserJoined = record.getColumnData("date_joined");


        Div divTextDescription = new Div();
        divTextDescription.addClassNames(Width.FULL, JustifyContent.CENTER, AlignItems.CENTER, Padding.NONE, Margin.NONE);

        H3 header = new H3("Registration");
        header.addClassNames(FontSize.LARGE, FontWeight.SEMIBOLD,
                Width.FULL, TextAlignment.CENTER, AlignItems.CENTER, JustifyContent.CENTER,
                Padding.XSMALL, Margin.NONE
        );

        FormLayout formLayout = new FormLayout();
        formLayout.addClassNames(Width.FULL, Gap.LARGE);
//        formLayout.setExpandFields(true);
//        formLayout.setLabelsAside(true);

        String strWidthOfFields = "260px";

        TextField txtName = new TextField();
        txtName.setRequiredIndicatorVisible(true);
        txtName.setRequired(true);
        txtName.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtName, "Name");

        TextField txtSurname = new TextField();
        txtSurname.setRequiredIndicatorVisible(true);
        txtSurname.setRequired(true);
        txtSurname.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtSurname, "Surname");

        TextField txtEmail = new TextField();
        txtEmail.setRequiredIndicatorVisible(true);
        txtEmail.setRequired(true);
        txtEmail.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtEmail, "e-mail");

        TextField txtUserName = new TextField();
        txtUserName.setRequiredIndicatorVisible(true);
        txtUserName.setRequired(true);
        txtUserName.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtUserName, "Username");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setRequiredIndicatorVisible(true);
        txtPassword.setRequired(true);
        txtPassword.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtPassword, "Password");
        PasswordField txtConfirmPassword = new PasswordField();
        txtConfirmPassword.setRequiredIndicatorVisible(true);
        txtConfirmPassword.setRequired(true);
        txtConfirmPassword.setWidth(strWidthOfFields);
        formLayout.addFormItem(txtConfirmPassword, "Confirm password");


        Button btnOk = new Button("Create Member");
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

            if (txtPassword.getValue().isEmpty()) {
                String strMessage = "Password should not be empty!";
                txtPassword.setErrorMessage(strMessage);
                Notification.show(strMessage);
            }

            if (txtConfirmPassword.getValue().isEmpty()) {
                String strMessage = "Confirm Password should not be empty!";
                txtConfirmPassword.setErrorMessage(strMessage);
                Notification.show(strMessage);
            }

            String strEmail = txtEmail.getValue();
            String strUsername = txtUserName.getValue();


            boolean isEmailSystaxValid = utilsString.isEmailSysntaxValid(strEmail);
            if (!isEmailSystaxValid) {
                String strMessage = "Email is not valid!";
                txtEmail.setErrorMessage(strMessage);
                Notification.show(strMessage);
            }

            if (!txtPassword.getValue().equalsIgnoreCase(txtConfirmPassword.getValue())) {
                Notification.show("Password is not the same in both fields. Please retype.");
            }

            boolean doesEmailExist = checkIfValueExists("email", strEmail);
            if (doesEmailExist) {
                Notification.show("Email " + strEmail + " already exists! Please type a different one.");
//                Notification notification = new Notification("Email " + strEmail + " already exists! Please type a different one.");
//                notification.setPosition(Notification.Position.MIDDLE);
//                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            boolean doesUsernameExist = checkIfValueExists("username", strUsername);
            if (doesUsernameExist) {
                Notification.show("Username " + strUsername + " already exists! Please type a different one.");
//                Notification notification = new Notification("Username " + strUsername + " already exists! Please type a different one.");
//                notification.setPosition(Notification.Position.MIDDLE);
//                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            if (txtName.getValue().isEmpty() || txtSurname.getValue().isEmpty() || strUsername.isEmpty() || strEmail.isEmpty()
                    || !isEmailSystaxValid || txtPassword.getValue().isEmpty() || txtConfirmPassword.getValue().isEmpty()
                    || !txtPassword.getValue().equalsIgnoreCase(txtConfirmPassword.getValue())
                    || doesEmailExist || doesUsernameExist) {

            } else {
                String txt = passwordEncoder().encode(txtPassword.getValue());   //utilsString.encrypt(txtPassword.getValue());
                createMember(txtUserName.getValue(), txt, txtEmail.getValue(), txtName.getValue(), txtSurname.getValue(), section, strCalledFrom);
            }

        });

//        btnMore.addClassName("btn-more");
        //      btnOk.addClickListener(click -> {
//            btnOk.getUI().ifPresent(ui ->
//                    ui.navigate(StoriesView.class, new RouteParameters(routeMember, routeStory))
//            );
        //       });

        verticalLayout.add(header, formLayout, btnOk);
        this.setMaxWidth("460px");
        this.add(verticalLayout);

    }

    private int createMember(String strUsername, String strPass, String strEmail, String strName, String strSurname, String section, String strCalledFrom) {

        int retInt = 0;

        genericView.logVisitorToDb(section, strCalledFrom);

        String sqlInsert = "INSERT INTO dbuser (username , password , email , name , surname, referring_others) VALUES (? , ?, ?, ?, ?, ? )";


        String strCodeForReferring = utilsString.generateRandomString(6);
        Object[] objInsert = {strUsername, strPass, strEmail, strName, strSurname, strCodeForReferring};
        String[] arrTypeInsert = {"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String"};

        retInt = recordService.insertOneRecordWithQuery(sqlInsert, objInsert, arrTypeInsert);
//        Notification.show("Check your email to activate your account.", 5000, Notification.Position.MIDDLE);
        Notification notification = Notification.show("Check your email to activate your account.");
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);

        emailSendService.sendSimpleMail(strMailboxSend, "nickgiant@yahoo.com", "New member!", "From IP: " + publicIp+ " username: "+strUsername +
                " email: "+ strEmail+ " Name: " + strName +" Surname: "+ strSurname);
//        emailSendService.sendSimpleMail(strMailboxSend, strEmail, "Registration for photoact.net",
//                strName+" "+strSurname+", Please confirm you have registered for photoact.net!");

        this.setOpened(false);

        return retInt;
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private HorizontalLayout getActions(Button btnMore) {

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

//        Button btnUpload = new Button(VaadinIcon.UPLOAD.create());
//        btnUpload.setTooltipText("Upload your related photos");

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


        layoutActions.add(btnMore);

        return layoutActions;
    }

    private boolean checkIfValueExists(String strField, String strValue) {


        String sqlCheckEmail = "SELECT " + strField + " FROM dbuser WHERE " + strField + " = ? ";
        String[] arrColEmail = {strField};
        Object[] objEmail = {strValue};
        String[] arrTypeEmail = {"java.lang.String"};

        List<Record> lstEmail = recordService.findAll(sqlCheckEmail, arrColEmail, objEmail, arrTypeEmail);

        logger.info(!lstEmail.isEmpty() + "  " + strValue);
        return !lstEmail.isEmpty();

    }

    private void logErrorInDb(Exception e, String function, String hostname, int userId, String strUsername, String publicIp, long sessionCreation, String info) {
        recordService.logErrorInDb(e, hostname, function, userId, strUsername, publicIp, Long.toString(sessionCreation), info);
    }


}
