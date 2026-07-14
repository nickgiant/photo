package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailSendService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Combined Sign In / Sign Up dialog.
 * <p>
 * Same 900px split-panel layout as the standalone {@link LoginDialog} /
 * {@link RegistrationDialog}: an illustration panel on the left, the form on
 * the right. A full-width tab strip on top switches between the two modes —
 * only the illustration's icon/text crossfade and the visible form swap;
 * both columns keep their exact size, so toggling tabs never resizes the
 * dialog.
 * <p>
 * Both forms are reused as-is rather than reimplemented:
 * {@link LoginDialog#createStyledLoginForm()} for sign in,
 * {@link RegistrationFormPanel} for sign up — same fields, same validation,
 * same member-creation logic.
 * <p>
 * Usage:
 * <pre>
 *   new AuthDialog(strUserReferCode, publicIp, recordService, section, calledFrom, emailSendService).open();
 * </pre>
 */
public class AuthDialog extends Dialog {

    private final Div illustrationSide;
    private final Button tabLogin;
    private final Button tabRegister;
    private final LoginForm loginForm;
    private final RegistrationFormPanel registrationFormPanel;

    public AuthDialog(String strUserReferCode, String publicIp,
                       RecordService recordService, String section, String strCalledFrom,
                       EmailSendService emailSendService) {
        addClassName("auth-dialog");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setWidth("900px");
        setHeight(null); // auto — matches LoginDialog / RegistrationDialog

        // ── Full-width Sign In / Sign Up tabs ────────────────────

        tabLogin = new Button("Sign In");
        tabRegister = new Button("Sign Up");
        tabLogin.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        tabRegister.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        tabLogin.addClassNames("auth-tab", "auth-tab--active");
        tabRegister.addClassName("auth-tab");

        HorizontalLayout tabs = new HorizontalLayout(tabLogin, tabRegister);
        tabs.addClassName("auth-tabs");
        tabs.setWidthFull();
        tabs.setSpacing(false);
        tabs.setPadding(false);
        tabs.setFlexGrow(1, tabLogin);
        tabs.setFlexGrow(1, tabRegister);

        // ── Left side: illustration — only its icon/text crossfades ──

        Div loginIllustration = buildIllustrationPanel(VaadinIcon.SIGN_IN, "Welcome back", "auth-illustration-panel--login");
        Div registerIllustration = buildIllustrationPanel(VaadinIcon.GROUP, "Join our community", "auth-illustration-panel--register");

        illustrationSide = new Div(loginIllustration, registerIllustration);
        illustrationSide.addClassNames("reg-illustration-side", "login-illustration-side", "auth-illustration-side");
        illustrationSide.setWidth("45%");
        illustrationSide.getStyle().set("box-sizing", "border-box");

        // ── Right side: form — swaps LoginForm / RegistrationFormPanel ──

        loginForm = LoginDialog.createStyledLoginForm();
        loginForm.setAction("login");

        registrationFormPanel = new RegistrationFormPanel(
                strUserReferCode, publicIp, recordService, section, strCalledFrom, emailSendService, this::close);
        registrationFormPanel.setVisible(false);
        registrationFormPanel.setWidthFull();

        VerticalLayout formSide = new VerticalLayout(loginForm, registrationFormPanel);
        formSide.addClassNames("reg-form-side", "login-form-side");
        formSide.setWidth("55%");
        formSide.setAlignItems(FlexComponent.Alignment.CENTER);
        formSide.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        formSide.getStyle()
                .set("padding", "48px 32px")
                .set("box-sizing", "border-box");

        // ── Compose ───────────────────────────────────────────────

        HorizontalLayout columns = new HorizontalLayout(illustrationSide, formSide);
        columns.addClassNames("reg-dialog-content", "auth-columns");
        columns.setSizeFull();
        columns.setSpacing(false);
        columns.setPadding(false);

        tabLogin.addClickListener(e -> switchTo(false));
        tabRegister.addClickListener(e -> switchTo(true));

        VerticalLayout root = new VerticalLayout(tabs, columns);
        root.addClassName("auth-root");
        root.setPadding(false);
        root.setSpacing(false);

        add(root);
    }

    private void switchTo(boolean registerMode) {
        illustrationSide.getElement().getClassList().set("auth-illustration-side--mode-register", registerMode);
        loginForm.setVisible(!registerMode);
        registrationFormPanel.setVisible(registerMode);
        tabLogin.getElement().getClassList().set("auth-tab--active", !registerMode);
        tabRegister.getElement().getClassList().set("auth-tab--active", registerMode);
    }

    private Div buildIllustrationPanel(VaadinIcon iconType, String text, String modifierClass) {
        Div wrapper = new Div();
        wrapper.addClassName("reg-illustration-wrapper");

        Icon icon = iconType.create();
        icon.addClassName("reg-illustration-icon");
        wrapper.add(icon);

        Paragraph label = new Paragraph(text);
        label.addClassName("reg-illustration-text");

        Div panel = new Div(wrapper, label);
        panel.addClassNames("auth-illustration-panel", modifierClass);
        return panel;
    }
}
