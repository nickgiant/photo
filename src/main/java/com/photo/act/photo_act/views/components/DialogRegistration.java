package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailSendService;
import com.photo.act.photo_act.utils.UtilsString;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.FileSystems;
import java.util.List;

import static com.photo.act.photo_act.views.ConfirmView.STR_DUMP_CODE;

public class DialogRegistration extends Dialog {

    private static final Logger logger = LoggerFactory.getLogger(DialogRegistration.class);
    private RecordService recordService;
    private boolean isMobile;
    private GenericView genericView;
    private UtilsString utilsString;

    private String dirChar = FileSystems.getDefault().getSeparator();


    private EmailSendService emailSendService;

    private String strMailboxRegister = "registration@photoact.net";
    private String strUserReferCode;
    private String publicIp;

    public DialogRegistration(boolean isMobile, String strUserReferCode, long sessionCreation,
                              String hostname, String publicIp, RecordService recordService, String section, String strCalledFrom,
                              EmailSendService emailSendService
    ) {
        this.recordService = recordService;
        this.strUserReferCode = strUserReferCode;
        this.emailSendService = emailSendService;
        this.publicIp = publicIp;
        this.isMobile = isMobile;


        utilsString = new UtilsString();
        genericView = new GenericView(recordService);

        this.setCloseOnOutsideClick(false);
        this.setDraggable(true);
        this.setCloseOnEsc(true);
        this.setModal(true);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, Padding.LARGE, Gap.MEDIUM);

//        genericView = new GenericView(recordService);

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
        divTextDescription.addClassNames(Width.FULL,
                TextAlignment.CENTER,
                JustifyContent.CENTER, AlignItems.CENTER,
                TextColor.PRIMARY,
                Padding.SMALL, Margin.NONE);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.BETWEEN, Padding.SMALL, Margin.NONE);

        H3 header = new H3("Registration");
        header.addClassNames(FontSize.LARGE, FontWeight.SEMIBOLD,
                Width.FULL, TextAlignment.CENTER, AlignItems.CENTER, JustifyContent.CENTER,
                Padding.LARGE, Margin.NONE
        );

        Button btnClose = new Button();
        btnClose.addClickListener(e -> {
            this.close();
        });
        btnClose.setIcon(FontAwesome.Solid.CLOSE.create());

        layoutTitle.add(header, btnClose);
        Paragraph paragraph = new Paragraph();
        paragraph.setText("Type the code and necessary data to create an account.");

        String strWidthOfFields = "";
        if (isMobile) {
            strWidthOfFields = "220px";
            this.setMaxWidth("500px");
        } else {
            strWidthOfFields = "240px";
            this.setMaxWidth("520px");
        }

        FormLayout formLayoutBasic = new FormLayout();
        formLayoutBasic.addClassNames(Width.FULL, Gap.LARGE, Padding.LARGE, Margin.NONE, BorderColor.CONTRAST_5, BorderRadius.MEDIUM);
        formLayoutBasic.setResponsiveSteps(new FormLayout.ResponsiveStep(strWidthOfFields, 1));
        formLayoutBasic.setVisible(false);
        formLayoutBasic.setLabelsAside(true);
//        formLayout.setAutoResponsive(true);
//        formLayout.setColumnWidth("10em");
//        formLayout.setExpandColumns(true);
//        formLayout.setExpandFields(true);


        var ref = new Object() {
            private String strName;
            private String strSurname;
            private String strEmail;
            private String strCode;
            private String strUsername;
            private String strPassword;

            public String getStrPassword() {
                return strPassword;
            }

            public void setStrPassword(String strPassword) {
                this.strPassword = strPassword;
            }

            public String getStrUsername() {
                return strUsername;
            }

            public void setStrUsername(String strUsername) {
                this.strUsername = strUsername;
            }

            public String getStrCode() {
                return strCode;
            }

            public void setStrCode(String strCode) {
                this.strCode = strCode;
            }

            public String getStrEmail() {
                return strEmail;
            }

            public void setStrEmail(String strEmail) {
                this.strEmail = strEmail;
            }

            public String getStrSurname() {
                return strSurname;
            }

            public void setStrSurname(String strSurname) {
                this.strSurname = strSurname;
            }


            public String getStrName() {
                return strName;
            }

            public void setStrName(String strName) {
                this.strName = strName;
            }
        };
        TextField txtName = new TextField();
        txtName.setRequiredIndicatorVisible(true);
        txtName.setRequired(true);
        txtName.setWidth(strWidthOfFields);
        txtName.setHelperComponent(divTextDescription);
        //txtName.setI18n(new TextField.TextFieldI18n().setRequiredErrorMessage("Name is required").setPatternErrorMessage("Only letters and . - allowed"));
        txtName.setAllowedCharPattern("^[a-zA-Z.\\-]+$");
        txtName.setMinLength(3);
        txtName.setMaxLength(20);
        txtName.setErrorMessage("Min 3 to max 20 characters. Valid are: letters and - . ");
        txtName.addFocusListener(tc -> {
            String txtValue = tc.getSource().getValue();
            if (txtValue.length() >= 3) {
                ref.setStrName(txtValue);
            } else {
                ref.setStrName(null);
            }
        });
        txtName.addBlurListener(tc -> {
            String txtValue = tc.getSource().getValue();
            if (txtValue.length() >= 3) {
                ref.setStrName(txtValue);
            } else {
                ref.setStrName(null);
            }
        });
        formLayoutBasic.addFormItem(txtName, "Name");

        TextField txtSurname = new TextField();
        txtSurname.setRequiredIndicatorVisible(true);
        txtSurname.setRequired(true);
        txtSurname.setWidth(strWidthOfFields);
        txtSurname.setHelperComponent(divTextDescription);
//        String strMessageS = "Surname should not be empty!";
        //txtSurname.setI18n(new TextField.TextFieldI18n().setRequiredErrorMessage("Surname is required. Min 3 to max 20 characters.").setPatternErrorMessage("Only letters and . - allowed"));
        txtSurname.setAllowedCharPattern("^[a-zA-Z.\\-]+$");
        txtSurname.setMinLength(3);
        txtSurname.setMaxLength(20);
        txtSurname.setErrorMessage("Min 3 to max 20 characters. Valid are: letters and - . ");
        txtSurname.addFocusListener(tc -> {
            String txtValue = tc.getSource().getValue();
            if (txtValue.length() >= 3) {
                ref.setStrSurname(txtValue);
            } else {
                ref.setStrSurname(null);
            }
        });
        txtSurname.addBlurListener(tc -> {
            String txtValue = tc.getSource().getValue();
            if (txtValue.length() >= 3) {
                ref.setStrSurname(txtValue);
            } else {
                ref.setStrSurname(null);
            }
        });
        formLayoutBasic.addFormItem(txtSurname, "Surname");

        EmailField txtEmail = new EmailField();
        txtEmail.setWidth(strWidthOfFields);
        txtEmail.setRequiredIndicatorVisible(true);
//        txtEmail.setManualValidation(true);
        txtEmail.setPattern("^[\\w\\-.]+@([\\w-]+\\.)+[\\w-]{2,}$");
        txtEmail.setI18n(new EmailField.EmailFieldI18n()
                //  .setRequiredErrorMessage("Email is required.")
                .setPatternErrorMessage("A valid email address is required, in order to enable your account."));
        txtEmail.addFocusListener(em -> {
            String strEmail = em.getSource().getValue();
            boolean doesEmailExist = genericView.checkIfMemberValueExists("email", strEmail);
            if (doesEmailExist) {
                em.getSource().setHelperText("The email you typed, already exists. Please type an other one.");
                ref.setStrEmail(null);
            } else {
                if (strEmail.matches("^[\\w\\-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$")) {
                    ref.setStrEmail(em.getSource().getValue());
                } else {
                    ref.setStrEmail(null);
                }
                em.getSource().setHelperText("");
            }
        });
        txtEmail.addBlurListener(em -> {
            String strEmail = em.getSource().getValue();
            boolean doesEmailExist = genericView.checkIfMemberValueExists("email", strEmail);
            if (doesEmailExist) {
                em.getSource().setHelperText("The email you typed, already exists. Please type an other one.");
                ref.setStrEmail(null);
            } else {
                if (strEmail.matches("^[\\w\\-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$")) {
                    ref.setStrEmail(em.getSource().getValue());
                } else {
                    ref.setStrEmail(null);
                }
                em.getSource().setHelperText("");
            }
        });
        formLayoutBasic.addFormItem(txtEmail, "e-mail");

        FormLayout formLayoutCode = new FormLayout();
        formLayoutCode.addClassNames(Width.FULL, Gap.LARGE, Padding.LARGE, Margin.NONE, BorderColor.CONTRAST_5, BorderRadius.MEDIUM);
        formLayoutCode.setResponsiveSteps(new FormLayout.ResponsiveStep(strWidthOfFields, 1));
        formLayoutCode.setLabelsAside(true);
        String strApplied = "Code is OK!";

        TextField txtCode = new TextField();
        txtCode.setRequiredIndicatorVisible(true);
//        txtCode.setRequired(true);
        txtCode.setWidth(strWidthOfFields);
        txtCode.setHelperComponent(divTextDescription);
        txtCode.setHelperText("Type your code (6 characters) for free uploads.");
        // txtCode.setI18n(new TextField.TextFieldI18n().setRequiredErrorMessage("Code is required. (6 characters)").setPatternErrorMessage("Only letters and numbers allowed"));
        txtCode.setAllowedCharPattern("^[a-zA-Z0-9]+$");
        txtCode.setErrorMessage("6 characters. Valid are: letters and numbers");
        txtCode.setMinLength(6);
        txtCode.setMaxLength(6);
        txtCode.addFocusListener(tc -> {
            String txtValue = tc.getSource().getValue();
            if (txtValue.length() == 6) {
                String strMessage = genericView.checkIfCodeExistsOrAppliedForMember(txtValue, "0");
                if (strMessage.equalsIgnoreCase(strApplied)) {
                    // tc.getSource().setHelperText("Type your code (6 characters) for free uploads.");
//                    tc.getSource().setErrorMessage("");
                    tc.getSource().setHelperText(strMessage);
                    ref.setStrCode(tc.getSource().getValue());
                } else {
                    ref.setStrCode(null);
                    tc.getSource().setHelperText(strMessage);
                }
            } else if (txtValue.isEmpty()) {
                ref.setStrCode(null);
                tc.getSource().setHelperText("Type your code (6 characters) for free uploads.");
                tc.getSource().setErrorMessage("");
            } else {
                ref.setStrCode(null);
                tc.getSource().setHelperText("");
                tc.getSource().setErrorMessage("Type your code (6 characters) for free uploads.");
            }
        });
        txtCode.addBlurListener(tc -> {
            String txtValue = tc.getSource().getValue();
            if (txtValue.length() == 6) {
                String strMessage = genericView.checkIfCodeExistsOrAppliedForMember(txtValue, "0");
                if (strMessage.equalsIgnoreCase(strApplied)) {
                    // tc.getSource().setHelperText("Type your code (6 characters) for free uploads.");
//                    tc.getSource().setErrorMessage("");
                    tc.getSource().setHelperText(strMessage);
                    ref.setStrCode(tc.getSource().getValue());
                } else {
                    ref.setStrCode(null);
                    tc.getSource().setHelperText(strMessage);
                }
            } else if (txtValue.isEmpty()) {
                ref.setStrCode(null);
                tc.getSource().setHelperText("Type your code (6 characters) for free uploads.");
                tc.getSource().setErrorMessage("");
            } else {
                ref.setStrCode(null);
                tc.getSource().setHelperText("");
                tc.getSource().setErrorMessage("Type your code (6 characters) for free uploads.");
            }
        });
        formLayoutCode.addFormItem(txtCode, "Code");

        TextField txtUserName = new TextField();
        txtUserName.setRequiredIndicatorVisible(true);
//        txtUserName.setRequired(true);
        txtUserName.setWidth(strWidthOfFields);
        txtUserName.setHelperComponent(divTextDescription);
//        txtUserName.setManualValidation(true);
        //txtUserName.setI18n(new TextField.TextFieldI18n().setRequiredErrorMessage("Username is required").setPatternErrorMessage("Only lower case letters, numbers _ and - allowed"));
        txtUserName.setAllowedCharPattern("^[a-z0-9_\\-]+$");
        txtUserName.setErrorMessage("Min 6 to max 20 characters. Valid are: letters, numbers _ and - ");
        txtUserName.setMinLength(6);
        txtUserName.setMaxLength(20);
        txtUserName.addFocusListener(tu -> {
            String txtValue = tu.getSource().getValue();
            if (txtValue.length() >= 6) {
                boolean doesUsernameExist = genericView.checkIfMemberValueExists("username", txtValue);
                if (doesUsernameExist) {
                    String strMessage = "Username " + txtValue + " already exists! Please type a different one.";
                    tu.getSource().setHelperText(strMessage);
                    ref.setStrUsername(null);
                } else {
                    tu.getSource().setHelperText("");
                    ref.setStrUsername(tu.getSource().getValue());
                }
            } else {
                ref.setStrUsername(null);
                tu.getSource().setHelperText("");
            }
        });
        txtUserName.addBlurListener(tu -> {
            String txtValue = tu.getSource().getValue();
            if (txtValue.length() >= 6) {
                boolean doesUsernameExist = genericView.checkIfMemberValueExists("username", txtValue);
                if (doesUsernameExist) {
                    String strMessage = "Username " + txtValue + " already exists! Please type a different one.";
                    tu.getSource().setHelperText(strMessage);
                    ref.setStrUsername(null);
                } else {
                    tu.getSource().setHelperText("");
                    ref.setStrUsername(tu.getSource().getValue());
                }
            } else {
                ref.setStrUsername(null);
                tu.getSource().setHelperText("");
            }
        });
        formLayoutCode.addFormItem(txtUserName, "Username");

        String strMessagePass = "Password is not the same in both fields. Please retype.";

        PasswordField txtPassword = new PasswordField();
        txtPassword.setRequiredIndicatorVisible(true);
//        txtPassword.setRequired(true);
        txtPassword.setWidth(strWidthOfFields);
        txtPassword.setMinLength(8);
        txtPassword.setMaxLength(20);
        txtPassword.setAllowedCharPattern("^[a-zA-Z0-9_#.\\-]+$");
        txtPassword.setErrorMessage("Min 8 to max 20 characters. Valid are: letters numbers and - . _ #");
        txtPassword.setHelperComponent(divTextDescription);
        formLayoutCode.addFormItem(txtPassword, "Password");

        PasswordField txtConfirmPassword = new PasswordField();
        txtConfirmPassword.setRequiredIndicatorVisible(true);
//        txtConfirmPassword.setRequired(true);
        txtConfirmPassword.setWidth(strWidthOfFields);
        txtConfirmPassword.setMinLength(8);
        txtConfirmPassword.setMaxLength(20);
        txtConfirmPassword.setHelperComponent(divTextDescription);
        txtConfirmPassword.addFocusListener(fc -> {
            if (!fc.getSource().getValue().equalsIgnoreCase(txtPassword.getValue())) {
                fc.getSource().setHelperText(strMessagePass);
                ref.setStrPassword(null);
            } else {
                String txtValue = fc.getSource().getValue();
                if (txtValue.length() >= 8) {
                    ref.setStrPassword(txtValue);
                    fc.getSource().setHelperText("");
                } else {
                    ref.setStrPassword(null);
                }
            }
        });
        txtConfirmPassword.addBlurListener(fc -> {
            if (!fc.getSource().getValue().equalsIgnoreCase(txtPassword.getValue())) {
                fc.getSource().setHelperText(strMessagePass);
                ref.setStrPassword(null);
            } else {
                String txtValue = fc.getSource().getValue();
                if (txtValue.length() >= 8) {
                    ref.setStrPassword(txtValue);
                    fc.getSource().setHelperText("");
                } else {
                    ref.setStrPassword(null);
                }
            }
        });
        formLayoutCode.addFormItem(txtConfirmPassword, "Confirm password");

        VerticalLayout verticalForm = new VerticalLayout();
        verticalForm.addClassNames(AlignItems.CENTER, JustifyContent.CENTER, Padding.LARGE, Gap.MEDIUM);

        Button btnRegister = new Button("Create Member");
        btnRegister.setVisible(false);


        Button btnOk = new Button("OK");
        btnOk.addClassName(Margin.LARGE);
        btnOk.addClickListener(ok -> {
            if (ok.getSource().getText().equalsIgnoreCase("OK")) {

                ConfirmDialog dialog1 = new ConfirmDialog();
                dialog1.setHeader("Please correct the following");

                StringBuilder strErrorMessage = new StringBuilder();
                strErrorMessage.append("<ul>");

                if (ref.getStrCode() == null || ref.getStrCode().isEmpty())
                    strErrorMessage.append("<li>Check Code</li> ");
                if (ref.getStrUsername() == null || ref.getStrUsername().isEmpty())
                    strErrorMessage.append("<li>Check Username</li> ");
                if (ref.getStrPassword() == null || ref.getStrPassword().isEmpty())
                    strErrorMessage.append("<li>Check Password Texts</li> ");

                strErrorMessage.append("</ul>");
                Html htmlMessage = new Html(strErrorMessage.toString()); //

                dialog1.setText(htmlMessage);
                dialog1.setConfirmText("OK");
                dialog1.addConfirmListener(event -> {
                        }
                );

                if (ref.getStrCode() == null || ref.getStrCode().isEmpty() ||
                        ref.getStrUsername() == null || ref.getStrUsername().isEmpty() || ref.getStrPassword() == null || ref.getStrPassword().isEmpty()) {
                    dialog1.open();
                } else {
                    formLayoutBasic.setVisible(true);
                    btnRegister.setVisible(true);
                    formLayoutCode.setVisible(false);
                    paragraph.setText("Complete the requested data and click on 'Create Member'.");
                    ok.getSource().setText("Back");
                }
            } else {
                formLayoutBasic.setVisible(false);
                btnRegister.setVisible(false);
                formLayoutCode.setVisible(true);
                paragraph.setText("Type the code and necessary data to create an account.");
                ok.getSource().setText("OK");
            }
        });

        btnRegister.addClickListener(click -> {

            String strEmail = txtEmail.getValue();
            String strUsername = txtUserName.getValue();

            boolean isEmailSyntaxValid = utilsString.isEmailSysntaxValid(strEmail);
            boolean doesEmailExist = genericView.checkIfMemberValueExists("email", strEmail);
            boolean doesUsernameExist = genericView.checkIfMemberValueExists("username", strUsername);

            String strCode = genericView.checkIfCodeExistsOrAppliedForMember(txtCode.getValue(), "0");
            boolean isCodeValid = strCode.equalsIgnoreCase(strApplied);
            boolean arePasswordsEqual = txtPassword.getValue().equalsIgnoreCase(txtConfirmPassword.getValue());


            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Please correct the following");

            StringBuilder strErrorMessage = new StringBuilder();
            strErrorMessage.append("<ul>");
            if (ref.getStrName() == null || ref.getStrName().isEmpty()) strErrorMessage.append("<li>Check Name</li> ");
            if (ref.getStrSurname() == null || ref.getStrSurname().isEmpty())
                strErrorMessage.append("<li>Check Surname</li> ");
            if (ref.getStrEmail() == null || ref.getStrEmail().isEmpty())
                strErrorMessage.append("<li>Check Email</li> ");

            if (ref.getStrCode() == null || ref.getStrCode().isEmpty()) strErrorMessage.append("<li>Check Code</li> ");

            if (ref.getStrUsername() == null || ref.getStrUsername().isEmpty())
                strErrorMessage.append("<li>Check Username</li> ");
            if (ref.getStrPassword() == null || ref.getStrPassword().isEmpty())
                strErrorMessage.append("<li>Check Password Texts</li> ");

            strErrorMessage.append("</ul>");
            Html htmlMessage = new Html(strErrorMessage.toString()); //

            dialog.setText(htmlMessage);
            dialog.setConfirmText("OK");
            dialog.addConfirmListener(event -> {
                    }
            );

            if (ref.getStrName() == null || ref.getStrName().isEmpty() || ref.getStrSurname() == null || ref.getStrSurname().isEmpty() ||
                    ref.getStrEmail() == null || ref.getStrEmail().isEmpty() || ref.getStrCode() == null || ref.getStrCode().isEmpty() ||
                    ref.getStrUsername() == null || ref.getStrUsername().isEmpty() || ref.getStrPassword() == null || ref.getStrPassword().isEmpty()) {

                dialog.open();
            } else {
                //utilsString.encrypt(txtPassword.getValue());
                createMember(txtUserName.getValue(), passwordEncoder().encode(txtPassword.getValue()), txtEmail.getValue(), txtName.getValue(), txtSurname.getValue(),
                        txtCode.getValue(), publicIp, section, strCalledFrom);
                this.close();
            }

        });

//        btnMore.addClassName("btn-more");
        //      btnOk.addClickListener(click -> {
//            btnOk.getUI().ifPresent(ui ->
//                    ui.navigate(StoriesView.class, new RouteParameters(routeMember, routeStory))
//            );
        //       });

        verticalLayout.add(layoutTitle, paragraph, formLayoutBasic, formLayoutCode, divTextDescription, btnRegister, btnOk);
        this.add(verticalLayout);

    }

    private int createMember(String strUsername, String strPass, String strEmail, String strName, String strSurname, String strCode, String ipJoined,
                             String section, String strCalledFrom) {

        int retInt = 0;

        genericView.logVisitorToDb(section, strCalledFrom);

        String sqlInsert = "INSERT INTO dbuser (username , password , email , name , surname, referring_others, validation, ip_joined, date_joined) VALUES (? , ?, ?, ?, ?, ?, ?, ?, now())";


        String strCodeForReferring = utilsString.generateRandomString(6);
        String strCodeForValidation = utilsString.generateRandomString(6);
        Object[] objInsert = {strUsername, strPass, strEmail, strName, strSurname, strCodeForReferring, strCodeForValidation, ipJoined};
        String[] arrTypeInsert = {"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String"};

        retInt = recordService.insertOneRecordWithQuery(sqlInsert, objInsert, arrTypeInsert);

        String[] arrUserId = {"userid"};
        List<Record> lstUserId = recordService.findAll("SELECT userid FROM dbuser WHERE username = '" + strUsername + "'", arrUserId);

        String strUserId = lstUserId.get(0).getColumnData("userid");

        String sqlInsertExtra = "INSERT INTO dbuser_extra (user_id, id, user_extra_type) VALUES ('" + strUserId + "', '1' ,'Aggregate')";
        recordService.insertOneRecordWithQuery(sqlInsertExtra, null, null);

        String[] arrCode = {"avail_code", "space_mb_earn"};
        List<Record> lstCode = recordService.findAll("SELECT avail_code, space_mb_earn FROM avail_code WHERE avail_code = '" + strCode + "' ", arrCode);
        String strSpace = lstCode.get(0).getColumnData("space_mb_earn");

        String sqlInsertCode = "INSERT INTO dbuser_code (user_id, code, date_entered, space_mb ) VALUES ('" + strUserId + "','" + strCode + "', now(), '" + strSpace + "')";
        recordService.insertOneRecordWithQuery(sqlInsertCode, null, null);

//        Notification.show("Check your email to activate your account.", 5000, Notification.Position.MIDDLE);
        Notification notification = Notification.show("Check your email and activate your account.", 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);

        emailSendService.sendSimpleMail(strMailboxRegister, "nickgiant@yahoo.com", "New member!", "From IP: " + publicIp + " username: " + strUsername + " \n" +
                " Sent to mail:" + strEmail);
        //              " email: " + strEmail + " Name: " + strName + " Surname: " + strSurname);

        emailSendService.sendSimpleMail(strMailboxRegister, strEmail, "Registration of member at photoact.net",
                " Hi " + strName + " " + strSurname + " \n " +
                        " Welcome to photoact.net \n" +
                        "  \n" +
                        " Please confirm your registration with the two steps bellow: \n" +
                        "  \n " +
                        "  1.  follow this link https://photoact.net/confirm/" + strUsername + "/" + STR_DUMP_CODE + strCodeForValidation + STR_DUMP_CODE + STR_DUMP_CODE + "  \n " +
                        "  \n " +
                        "  2.  type   " + strCodeForValidation + "   and click on 'Confirm' button. \n " +
                        "  \n" +
                        "  \n" +
                        " Thank you!\n" +
                        " The photoact.net team");

//        emailSendService.sendSimpleMail(strMailboxRegister, strEmail, "Registration for photoact.net",
//                strName+" "+strSurname+", Please confirm you have registered for photoact.net!");


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


    private void logErrorInDb(Exception e, String function, String hostname, int userId, String strUsername, String publicIp, long sessionCreation, String info) {
        recordService.logErrorInDb(e, hostname, function, userId, strUsername, publicIp, Long.toString(sessionCreation), info);
    }


}
