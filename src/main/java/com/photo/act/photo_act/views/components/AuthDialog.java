package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailSendService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Combined Sign In / Sign Up dialog.
 * <p>
 * Shows an animated header — an icon and tagline that slide/crossfade between
 * "Welcome Back" (sign in) and "Join Us" (sign up) — above a tab toggle that
 * swaps the visible form. Both forms are reused as-is from the standalone
 * dialogs rather than reimplemented: {@link LoginDialog#createStyledLoginForm()}
 * for sign in, {@link RegistrationFormPanel} for sign up — same fields, same
 * validation, same member-creation logic.
 * <p>
 * Usage:
 * <pre>
 *   new AuthDialog(strUserReferCode, publicIp, recordService, section, calledFrom, emailSendService).open();
 * </pre>
 */
public class AuthDialog extends Dialog {

    private final Div header;
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
        setWidth("460px");
        setHeight(null);

        // ── Sliding header ────────────────────────────────────────

        Div headerLoginPanel = buildHeaderPanel(VaadinIcon.SIGN_IN, "Welcome Back",
                "Sign in to keep sharing your photography news.", "auth-header-panel--login");
        Div headerRegisterPanel = buildHeaderPanel(VaadinIcon.GROUP, "Join Us",
                "Create an account to post and edit news.", "auth-header-panel--register");

        header = new Div(headerLoginPanel, headerRegisterPanel);
        header.addClassName("auth-header");

        // ── Sign In / Sign Up tabs ──────────────────────────────────

        tabLogin = new Button("Sign In");
        tabRegister = new Button("Sign Up");
        tabLogin.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        tabRegister.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        tabLogin.addClassNames("auth-tab", "auth-tab--active");
        tabRegister.addClassName("auth-tab");

        HorizontalLayout tabs = new HorizontalLayout(tabLogin, tabRegister);
        tabs.addClassName("auth-tabs");
        tabs.setSpacing(false);
        tabs.setPadding(false);

        // ── Form content — reused as-is from the standalone dialogs ─

        loginForm = LoginDialog.createStyledLoginForm();
        loginForm.setAction("login");

        registrationFormPanel = new RegistrationFormPanel(
                strUserReferCode, publicIp, recordService, section, strCalledFrom, emailSendService, this::close);
        registrationFormPanel.setVisible(false);

        Div body = new Div(loginForm, registrationFormPanel);
        body.addClassName("auth-body");

        tabLogin.addClickListener(e -> switchTo(false));
        tabRegister.addClickListener(e -> switchTo(true));

        VerticalLayout root = new VerticalLayout(header, tabs, body);
        root.addClassName("auth-root");
        root.setPadding(false);
        root.setSpacing(false);

        add(root);
    }

    private void switchTo(boolean registerMode) {
        header.getElement().getClassList().set("auth-header--mode-register", registerMode);
        loginForm.setVisible(!registerMode);
        registrationFormPanel.setVisible(registerMode);
        tabLogin.getElement().getClassList().set("auth-tab--active", !registerMode);
        tabRegister.getElement().getClassList().set("auth-tab--active", registerMode);
    }

    private Div buildHeaderPanel(VaadinIcon iconType, String title, String subtitle, String modifierClass) {
        Icon icon = iconType.create();
        icon.addClassName("auth-header-icon");

        H3 titleEl = new H3(title);
        titleEl.addClassName("auth-header-title");

        Paragraph subtitleEl = new Paragraph(subtitle);
        subtitleEl.addClassName("auth-header-subtitle");

        Div panel = new Div(icon, titleEl, subtitleEl);
        panel.addClassNames("auth-header-panel", modifierClass);
        return panel;
    }
}
