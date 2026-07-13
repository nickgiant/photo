package com.photo.act.photo_act.views;

import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.PasswordResetService;
import com.photo.act.photo_act.views.components.GenericView;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@AnonymousAllowed
@Route(value = "change-password/:token?")
public class ChangePasswordView extends Main implements HasUrlParameter<String>, HasComponents, HasDynamicTitle, HasStyle {

    private static final Logger logger = LoggerFactory.getLogger(ChangePasswordView.class);

    private final RecordService recordService;
    private final PasswordResetService passwordResetService;
    private final GenericView genericView;
    private boolean isMobile;

    public ChangePasswordView(RecordService recordService, PasswordResetService passwordResetService) {
        this.recordService = recordService;
        this.passwordResetService = passwordResetService;
        this.genericView = new GenericView(recordService);

        addClassName("background");
        addClassNames(Width.FULL, Padding.LARGE, AlignItems.CENTER, JustifyContent.CENTER);
    }

    @Override
    public String getPageTitle() {
        return "Change Password";
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String token) {

        isMobile = VaadinSession.getCurrent().getBrowser().isAndroid()
                || VaadinSession.getCurrent().getBrowser().isIPhone()
                || VaadinSession.getCurrent().getBrowser().isWindowsPhone();

        removeAll();

        logger.warn("ChangePasswordView.setParameter reached with token=" + token);

        String strUsernameLookup;
        try {
            strUsernameLookup = passwordResetService.getUsernameForToken(token);
        } catch (Exception e) {
            logger.error("Failed to validate password reset token (" + e.getClass().getName() + "): " + e.getMessage(), e);
            strUsernameLookup = null;
        }
        final String strUsername = strUsernameLookup;

        Div card = new Div();
        card.addClassNames(Width.FULL, Background.BASE, BorderRadius.LARGE, Padding.LARGE, Margin.Top.XLARGE);
        card.setMaxWidth("420px");
        card.getStyle().set("box-shadow", "1px 1px 6px lightgrey");
        card.getStyle().set("box-sizing", "border-box");

        H2 title = new H2("Change Password");
        title.addClassNames(Margin.Top.NONE, TextColor.SECONDARY);
        card.add(title);

        if (strUsername == null) {
            Paragraph error = new Paragraph("This password change link is invalid or has expired. " +
                    "Please go back to your account and request a new one from Security & Tools.");
            error.addClassNames(TextColor.ERROR);
            card.add(error);
        } else {
            Paragraph instructions = new Paragraph("Choose a new password for your account.");
            instructions.addClassNames(TextColor.SECONDARY);

            PasswordField txtNewPassword = new PasswordField("New Password");
            txtNewPassword.setWidthFull();
            txtNewPassword.setRequiredIndicatorVisible(true);

            PasswordField txtConfirmPassword = new PasswordField("Confirm Password");
            txtConfirmPassword.setWidthFull();
            txtConfirmPassword.setRequiredIndicatorVisible(true);

            Button btnSetPassword = new Button("Set New Password");
            btnSetPassword.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnSetPassword.setWidthFull();

            VerticalLayout layoutForm = new VerticalLayout(instructions, txtNewPassword, txtConfirmPassword, btnSetPassword);
            layoutForm.setPadding(false);
            card.add(layoutForm);

            btnSetPassword.addClickListener(click -> {

                String strNewPassword = txtNewPassword.getValue();
                String strConfirmPassword = txtConfirmPassword.getValue();

                if (strNewPassword == null || strNewPassword.isEmpty()) {
                    Notification.show("Please type a new password.", 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                if (strNewPassword.length() < 8) {
                    Notification.show("Password must be at least 8 characters long.", 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                if (!strNewPassword.equals(strConfirmPassword)) {
                    Notification.show("Passwords do not match.", 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                String strEncodedPassword = new BCryptPasswordEncoder().encode(strNewPassword);

                String sqlUpdate = "UPDATE dbuser SET password = ? WHERE username = ?";
                Object[] values = {strEncodedPassword, strUsername};
                String[] types = {"java.lang.String", "java.lang.String"};
                int result = recordService.insertOneRecordWithQuery(sqlUpdate, values, types);

                if (result > 0) {
                    passwordResetService.invalidateToken(strUsername);

                    layoutForm.removeAll();
                    Paragraph success = new Paragraph("Your password has been updated. You can now log in with your new password.");
                    success.addClassNames(TextColor.SUCCESS);

                    Button btnGoToLogin = new Button("Go to Login", e -> getUI().ifPresent(ui -> ui.navigate("login")));
                    btnGoToLogin.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                    layoutForm.add(success, btnGoToLogin);
                } else {
                    Notification.show("Something went wrong updating your password. Please try again.", 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Password update failed for user: " + strUsername);
                }
            });
        }

        add(card);
        add(genericView.loadFooter(isMobile));
    }
}
