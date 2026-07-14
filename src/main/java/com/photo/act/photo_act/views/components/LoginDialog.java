package com.photo.act.photo_act.views.components;


import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * A login dialog that embeds Vaadin's {@link LoginForm} inside a
 * Colorlib RegForm-7 inspired split-panel layout.
 * <p>
 * The dialog has a purple illustration panel on the left and the
 * LoginForm on the right, fully styled to match the registration
 * dialog's look and feel.
 * <p>
 * Usage:
 * <pre>
 *   LoginDialog dialog = new LoginDialog();
 *
 *   // Handle login attempt
 *   dialog.getLoginForm().addLoginListener(event -&gt; {
 *       String username = event.getUsername();
 *       String password = event.getPassword();
 *       // authenticate ...
 *       // on failure: dialog.getLoginForm().setError(true);
 *       // on success: dialog.close();
 *   });
 *
 *   // Handle forgot-password
 *   dialog.getLoginForm().addForgotPasswordListener(event -&gt; {
 *       // navigate to password recovery ...
 *   });
 *
 *   dialog.open();
 * </pre>
 *
 * For Spring Security form-based login, set the action instead:
 * <pre>
 *   dialog.getLoginForm().setAction("login");
 * </pre>
 */
public class LoginDialog extends Dialog {

    private final LoginForm loginForm;

    public LoginDialog() {
        addClassName("login-dialog");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setWidth("900px");
        setHeight(null);

        // ── Left side: Illustration panel ────────────────────────

        VerticalLayout illustrationSide = new VerticalLayout();
        illustrationSide.addClassName("reg-illustration-side");
        illustrationSide.addClassName("login-illustration-side");
        illustrationSide.setWidth("45%");
        illustrationSide.setAlignItems(FlexComponent.Alignment.CENTER);
        illustrationSide.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        illustrationSide.getStyle()
                .set("padding", "48px 32px")
                .set("box-sizing", "border-box");

        Div illustrationWrapper = new Div();
        illustrationWrapper.addClassName("reg-illustration-wrapper");

        Icon illustrationIcon = VaadinIcon.SIGN_IN.create();
        illustrationIcon.addClassName("reg-illustration-icon");
        illustrationWrapper.add(illustrationIcon);

        Paragraph illustrationText = new Paragraph("Welcome back");
        illustrationText.addClassName("reg-illustration-text");

//        Anchor createAccountLink = new Anchor("#", "Create an account");
//        createAccountLink.addClassName("login-create-account-link");

        illustrationSide.add(illustrationWrapper, illustrationText);

        // ── Right side: LoginForm ────────────────────────────────

        loginForm = createStyledLoginForm();

        // Wrap LoginForm in a centred layout
        VerticalLayout formSide = new VerticalLayout(loginForm);
        formSide.addClassName("reg-form-side");
        formSide.addClassName("login-form-side");
        formSide.setWidth("55%");
        formSide.setAlignItems(FlexComponent.Alignment.CENTER);
        formSide.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        formSide.getStyle()
                .set("padding", "48px 32px")
                .set("box-sizing", "border-box");

        // ── Compose dialog ───────────────────────────────────────

        HorizontalLayout dialogContent = new HorizontalLayout(illustrationSide, formSide);
        dialogContent.addClassName("reg-dialog-content");
        dialogContent.setSizeFull();
        dialogContent.setSpacing(false);
        dialogContent.setPadding(false);

        add(dialogContent);
    }

    /**
     * Returns the embedded {@link LoginForm} so callers can
     * register login / forgot-password listeners, set actions,
     * toggle error state, etc.
     */
    public LoginForm getLoginForm() {
        return loginForm;
    }

    /**
     * Builds a {@link LoginForm} styled to match this dialog (and the
     * matching {@link RegistrationDialog}), so other containers — e.g.
     * {@link AuthDialog} — can reuse the exact same "Sign In" form.
     */
    public static LoginForm createStyledLoginForm() {
        LoginForm form = new LoginForm();
        form.addClassName("login-styled-form");
        form.setForgotPasswordButtonVisible(false);

        LoginI18n i18n = LoginI18n.createDefault();
        LoginI18n.Form formI18n = i18n.getForm();
        formI18n.setTitle("Sign in");
        formI18n.setUsername("Username");
        formI18n.setPassword("Password");
        formI18n.setSubmit("Log in");
        formI18n.setForgotPassword("Forgot password?");
        i18n.setForm(formI18n);

        LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle("Incorrect username or password");
        errorMessage.setMessage("Please check your credentials and try again.");
        i18n.setErrorMessage(errorMessage);

        form.setI18n(i18n);
        return form;
    }
}