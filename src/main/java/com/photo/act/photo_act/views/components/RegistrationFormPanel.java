package com.photo.act.photo_act.views.components;


import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailSendService;
import com.photo.act.photo_act.utils.UtilsString;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static com.photo.act.photo_act.views.ConfirmView.STR_DUMP_CODE;

/**
 * The registration form fields + submit logic used by {@link RegistrationDialog},
 * extracted so the same "Sign Up" form can also be embedded inside {@link AuthDialog}
 * (or any other container) without duplicating validation / member-creation logic.
 * <p>
 * Contains no dialog chrome (no illustration side, no width/close handling) — it is
 * a plain {@link VerticalLayout} meant to be dropped into a parent layout or dialog.
 * <p>
 * Usage:
 * <pre>
 *   RegistrationFormPanel panel = new RegistrationFormPanel(
 *       strUserReferCode, publicIp, recordService, section, strCalledFrom,
 *       emailSendService, () -&gt; myDialog.close());
 * </pre>
 */
public class RegistrationFormPanel extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationFormPanel.class);

    private final TextField nameField;
    private final TextField lastNameField;
    private final EmailField txtEmail;
    private final TextField txtCode;
    private final TextField txtUserName;
    private final PasswordField txtPassword;
    private final PasswordField txtConfirmPassword;
    private final Checkbox termsCheckbox;

    private final RecordService recordService;
    private final GenericView genericView;
    private final UtilsString utilsString;
    private final EmailSendService emailSendService;
    private final String strMailboxRegister = "registration@photoact.net";
    private final String publicIp;
    private final Runnable onRegistered;

    public RegistrationFormPanel(String strUserReferCode, String publicIp,
                                  RecordService recordService, String section, String strCalledFrom,
                                  EmailSendService emailSendService, Runnable onRegistered) {
        this.recordService = recordService;
        this.publicIp = publicIp;
        this.emailSendService = emailSendService;
        this.onRegistered = onRegistered;

        this.utilsString = new UtilsString();
        this.genericView = new GenericView(recordService);

        setPadding(false);
        setSpacing(false);
        addClassName("reg-form-panel");
        getStyle().set("gap", "0");

        Div divTextDescription = new Div();
        divTextDescription.addClassNames(LumoUtility.Width.FULL,
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER,
                LumoUtility.TextColor.PRIMARY,
                LumoUtility.Padding.SMALL, LumoUtility.Margin.NONE);

        // Name row (first + last on same line)
        nameField = createStyledTextField("Name", VaadinIcon.USER);
        nameField.setRequiredIndicatorVisible(true);
        nameField.setRequired(true);
        nameField.setHelperComponent(divTextDescription);
        nameField.setAllowedCharPattern("^[a-zA-Z.\\-]+$");
        nameField.setMinLength(3);
        nameField.setMaxLength(20);
        nameField.setErrorMessage("Min 3 to max 20 characters. Valid are: letters and - . ");
        nameField.setValueChangeMode(ValueChangeMode.EAGER);
        nameField.addValueChangeListener(event -> {
            String txtValue = event.getValue();
            nameField.setInvalid(txtValue == null || txtValue.isEmpty() || txtValue.length() < 3);
        });

        lastNameField = createStyledTextField("Last Name", VaadinIcon.USER);
        lastNameField.setRequiredIndicatorVisible(true);
        lastNameField.setRequired(true);
        lastNameField.setHelperComponent(divTextDescription);
        lastNameField.setAllowedCharPattern("^[a-zA-Z.\\-]+$");
        lastNameField.setMinLength(3);
        lastNameField.setMaxLength(20);
        lastNameField.setErrorMessage("Min 3 to max 20 characters. Valid are: letters and - . ");
        lastNameField.setValueChangeMode(ValueChangeMode.EAGER);
        lastNameField.addValueChangeListener(event -> {
            String txtValue = event.getValue();
            lastNameField.setInvalid(txtValue == null || txtValue.isEmpty() || txtValue.length() < 3);
        });

        HorizontalLayout nameRow = new HorizontalLayout(nameField, lastNameField);
        nameRow.setWidthFull();
        nameRow.setSpacing(true);
        nameRow.getStyle().set("gap", "16px");
        nameRow.expand(nameField, lastNameField);

        // Other fields
        txtEmail = createStyledEmailField("Email", VaadinIcon.ENVELOPE);
        txtEmail.setRequiredIndicatorVisible(true);
        txtEmail.setPattern("^[\\w\\-.]+@([\\w-]+\\.)+[\\w-]{2,}$");
        txtEmail.setI18n(new EmailField.EmailFieldI18n()
                .setRequiredErrorMessage("Email is required.")
                .setPatternErrorMessage("A valid email address is required, in order to enable your account."));
        txtEmail.setValueChangeMode(ValueChangeMode.EAGER);
        txtEmail.addValueChangeListener(event -> {
            String strEmail = event.getSource().getValue();
            if (strEmail == null || strEmail.isEmpty() || strEmail.length() < 7) {
                event.getSource().setErrorMessage("A valid email address is required.");
                txtEmail.setInvalid(true);
            } else if (strEmail.matches("^[\\w\\-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$")) {
                boolean doesEmailExist = genericView.checkIfMemberValueExists("email", strEmail);
                if (doesEmailExist) {
                    event.getSource().setErrorMessage("Email already exists in the system! Try a different one.");
                    txtEmail.setInvalid(true);
                } else {
                    event.getSource().setErrorMessage(null);
                    txtEmail.setInvalid(false);
                }
            } else {
                event.getSource().setErrorMessage("A valid email address is required.");
                txtEmail.setInvalid(true);
            }
        });

        String strApplied = "Code is OK!";

        txtCode = createStyledTextField("Code", VaadinIcon.QRCODE);
        txtCode.setRequiredIndicatorVisible(true);
        txtCode.setHelperComponent(divTextDescription);
        txtCode.setHelperText("Type your code (6 characters) for free uploads.");
        txtCode.setAllowedCharPattern("^[a-zA-Z0-9]+$");
        txtCode.setErrorMessage("6 characters. Valid are: letters and numbers");
        txtCode.setMinLength(6);
        txtCode.setMaxLength(6);
        txtCode.setValueChangeMode(ValueChangeMode.EAGER);
        txtCode.addValueChangeListener(event -> {
            String txtValue = event.getValue();
            if (txtValue == null || txtValue.isEmpty() || txtValue.length() != 6) {
                txtCode.setInvalid(true);
                event.getSource().setHelperText(null);
                event.getSource().setErrorMessage("6 characters. Valid are: letters and numbers");
            } else {
                String strMessage = genericView.checkIfCodeExistsOrAppliedForMember(txtValue, "0");
                if (strMessage.equalsIgnoreCase(strApplied)) {
                    txtCode.setInvalid(false);
                    event.getSource().setHelperText(strMessage);
                    event.getSource().setErrorMessage(null);
                } else {
                    txtCode.setInvalid(true);
                    event.getSource().setHelperText(null);
                    event.getSource().setErrorMessage(strMessage);
                }
            }
        });

        txtUserName = createStyledTextField("Username", VaadinIcon.USER);
        txtUserName.setRequiredIndicatorVisible(true);
        txtUserName.setRequired(true);
        txtUserName.setHelperComponent(divTextDescription);
        txtUserName.setAllowedCharPattern("^[a-z0-9_\\-]+$");
        txtUserName.setErrorMessage("Min 6 to max 20 characters. Valid are: letters, numbers _ and - ");
        txtUserName.setMinLength(6);
        txtUserName.setMaxLength(20);
        txtUserName.setValueChangeMode(ValueChangeMode.EAGER);
        txtUserName.addValueChangeListener(event -> {
            String txtValue = event.getValue();
            if (txtValue == null && txtValue.isEmpty() || txtValue.length() < 6) {
                txtUserName.setInvalid(true);
            } else {
                boolean doesUsernameExist = genericView.checkIfMemberValueExists("username", txtValue);
                if (doesUsernameExist) {
                    String strMessage = "Username " + txtValue + " already exists! Please type a different one.";
                    txtUserName.setInvalid(true);
                    event.getSource().setErrorMessage(strMessage);
                } else {
                    txtUserName.setInvalid(false);
                    event.getSource().setErrorMessage(null);
                }
            }
        });

        String strMessagePass = "Password is not the same in both fields. Please retype.";

        txtPassword = createStyledPasswordField("Password", VaadinIcon.LOCK);
        txtConfirmPassword = createStyledPasswordField("Repeat Password", VaadinIcon.LOCK);

        txtPassword.setRequiredIndicatorVisible(true);
        txtPassword.setMinLength(8);
        txtPassword.setMaxLength(20);
        txtPassword.setAllowedCharPattern("^[a-zA-Z0-9_#.\\-]+$");
        txtPassword.setErrorMessage("Min 8 to max 20 characters. Valid are: letters numbers and - . _ #");
        txtPassword.setHelperComponent(divTextDescription);
        txtPassword.setValueChangeMode(ValueChangeMode.EAGER);
        txtPassword.addValueChangeListener(event -> {
            String txtValue = event.getValue();
            if (txtValue == null && txtValue.isEmpty() || txtValue.length() < 8) {
                txtPassword.setInvalid(true);
            } else {
                if (!event.getSource().getValue().equalsIgnoreCase(txtConfirmPassword.getValue())) {
                    txtPassword.setInvalid(true);
                    event.getSource().setErrorMessage(strMessagePass);
                } else {
                    txtPassword.setInvalid(false);
                    txtConfirmPassword.setInvalid(false);
                    txtPassword.setErrorMessage(null);
                    txtConfirmPassword.setErrorMessage(null);
                }
            }
        });

        txtConfirmPassword.setRequiredIndicatorVisible(true);
        txtConfirmPassword.setMinLength(8);
        txtConfirmPassword.setMaxLength(20);
        txtConfirmPassword.setAllowedCharPattern("^[a-zA-Z0-9_#.\\-]+$");
        txtConfirmPassword.setErrorMessage("Min 8 to max 20 characters. Valid are: letters numbers and - . _ #");
        txtConfirmPassword.setHelperComponent(divTextDescription);
        txtConfirmPassword.setValueChangeMode(ValueChangeMode.EAGER);
        txtConfirmPassword.addValueChangeListener(event -> {
            String txtValue = event.getValue();
            if (txtValue == null && txtValue.isEmpty() || txtValue.length() < 8) {
                txtConfirmPassword.setInvalid(true);
            } else if (!event.getSource().getValue().equalsIgnoreCase(txtPassword.getValue())) {
                txtConfirmPassword.setInvalid(true);
                event.getSource().setErrorMessage(strMessagePass);
            } else {
                txtPassword.setInvalid(false);
                txtConfirmPassword.setInvalid(false);
                txtPassword.setErrorMessage(null);
                txtConfirmPassword.setErrorMessage(null);
            }
        });

        // Terms checkbox
        termsCheckbox = new Checkbox();
        termsCheckbox.addClassName("reg-terms-checkbox");

        Span termsLabel = new Span();
        termsLabel.add(new Span("I agree with statements in "));
        Anchor termsLink = new Anchor("#", "Terms of Service");
        termsLink.addClassName("reg-terms-link");
        termsLabel.add(termsLink);
        termsLabel.addClassName("reg-terms-label");

        HorizontalLayout termsRow = new HorizontalLayout(termsCheckbox, termsLabel);
        termsRow.setAlignItems(FlexComponent.Alignment.CENTER);
        termsRow.setSpacing(false);
        termsRow.getStyle().set("gap", "8px").set("margin-top", "8px");

        // Register button
        Button registerButton = new Button("Register");
        registerButton.addClassName("reg-submit-btn");
        registerButton.addClickListener(e -> {

            boolean arePasswordsEqual = txtPassword.getValue().equalsIgnoreCase(txtConfirmPassword.getValue());

            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Please correct the following");

            StringBuilder strErrorMessage = new StringBuilder();
            strErrorMessage.append("<ul>");
            if (nameField.isEmpty() || nameField.isInvalid())
                strErrorMessage.append("<li>Check Name</li> ");
            if (lastNameField.isEmpty() || lastNameField.isInvalid())
                strErrorMessage.append("<li>Check Last Name</li> ");
            if (txtEmail.isEmpty() || txtEmail.isInvalid())
                strErrorMessage.append("<li>Check Email</li> ");
            if (txtCode.isEmpty() || txtCode.isInvalid())
                strErrorMessage.append("<li>Check Code</li> ");
            if (txtUserName.isEmpty() || txtUserName.isInvalid())
                strErrorMessage.append("<li>Check Username</li> ");
            if (txtPassword.isEmpty() || txtPassword.isInvalid())
                strErrorMessage.append("<li>Check Password Text</li> ");
            if (txtConfirmPassword.isEmpty() || txtConfirmPassword.isInvalid())
                strErrorMessage.append("<li>Check Password Text Confirm</li> ");
            if (!arePasswordsEqual) {
                strErrorMessage.append("<li>Password is not the same on both texts!</li> ");
            }

            boolean isOk = !(nameField.isEmpty() || nameField.isInvalid()
                    || lastNameField.isEmpty() || lastNameField.isInvalid()
                    || txtEmail.isEmpty() || txtEmail.isInvalid()
                    || txtCode.isEmpty() || txtCode.isInvalid()
                    || txtUserName.isEmpty() || txtUserName.isInvalid()
                    || txtPassword.isEmpty() || txtPassword.isInvalid()
                    || txtConfirmPassword.isEmpty() || txtConfirmPassword.isInvalid() || !arePasswordsEqual);

            strErrorMessage.append("</ul>");
            Html htmlMessage = new Html(strErrorMessage.toString());

            dialog.setText(htmlMessage);
            dialog.setConfirmText("OK");
            dialog.addConfirmListener(event -> {
            });

            if (isOk) {
                dialog = null;
            }

            if (dialog != null) {
                dialog.open();
            } else {
                if (!termsCheckbox.getValue()) {
                    Notification.show("Please agree to the Terms of Service",
                            3000, Notification.Position.MIDDLE);
                } else {
                    createMember(txtUserName.getValue(), passwordEncoder().encode(txtPassword.getValue()), txtEmail.getValue(),
                            nameField.getValue(), lastNameField.getValue(),
                            txtCode.getValue(), publicIp, section, strCalledFrom);

                    if (onRegistered != null) onRegistered.run();
                }
            }
        });

        add(nameRow, txtEmail, txtCode, txtUserName, txtPassword, txtConfirmPassword, termsRow, registerButton);
    }

    // ── Field factory methods ────────────────────────────────────

    private TextField createStyledTextField(String placeholder, VaadinIcon iconType) {
        TextField field = new TextField();
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.addClassName("reg-text-field");
        field.setPrefixComponent(iconType.create());
        return field;
    }

    private EmailField createStyledEmailField(String placeholder, VaadinIcon iconType) {
        EmailField field = new EmailField();
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.addClassName("reg-text-field");
        field.setPrefixComponent(iconType.create());
        return field;
    }

    private PasswordField createStyledPasswordField(String placeholder, VaadinIcon iconType) {
        PasswordField field = new PasswordField();
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.addClassName("reg-password-field");
        field.setPrefixComponent(iconType.create());
        return field;
    }

    // ── Public accessors ─────────────────────────────────────────

    public TextField getNameField() { return nameField; }
    public TextField getLastNameField() { return lastNameField; }
    public EmailField getEmailField() { return txtEmail; }
    public TextField getCodeField() { return txtCode; }
    public TextField getUsernameField() { return txtUserName; }
    public PasswordField getPasswordField() { return txtPassword; }
    public PasswordField getRepeatPasswordField() { return txtConfirmPassword; }
    public Checkbox getTermsCheckbox() { return termsCheckbox; }

    // ── Registration handler ─────────────────────────────────────

    private int createMember(String strUsername, String strPass, String strEmail, String strName, String strSurname, String strCode, String ipJoined,
                             String section, String strCalledFrom) {

        int retInt;

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

        Notification notification = Notification.show("Check your email and activate your account.", 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);

        emailSendService.sendSimpleMail(strMailboxRegister, "nickgiant@yahoo.com", "New member!", "From IP: " + publicIp + " username: " + strUsername + " \n" +
                " Sent to mail:" + strEmail);

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

        return retInt;
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
