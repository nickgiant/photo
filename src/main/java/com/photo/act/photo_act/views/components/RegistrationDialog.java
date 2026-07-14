package com.photo.act.photo_act.views.components;


import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailSendService;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registration dialog inspired by the Colorlib RegForm 7 design.
 * <p>
 * Features a split layout with a form on the left and an illustration
 * area on the right, styled with a modern purple/violet accent theme.
 * <p>
 * The actual form fields and submit logic live in {@link RegistrationFormPanel},
 * so the same "Sign Up" form can be reused elsewhere (e.g. {@link AuthDialog})
 * without duplicating validation / member-creation logic.
 * <p>
 * Usage:
 * <pre>
 *   RegistrationDialog dialog = new RegistrationDialog(isMobile, "", sessionCreation,
 *       hostname, publicIp, recordService, section, calledFrom, emailSendService);
 *   dialog.open();
 * </pre>
 */
public class RegistrationDialog extends Dialog {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationDialog.class);

    public RegistrationDialog(boolean isMobile, String strUserReferCode, long sessionCreation,
                              String hostname, String publicIp, RecordService recordService, String section, String strCalledFrom,
                              EmailSendService emailSendService
    ) {
        this.setCloseOnOutsideClick(false);
        this.setDraggable(true);
        this.setCloseOnEsc(true);
        this.setModal(true);
        addClassName("registration-dialog");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setWidth("900px");
        setHeight(null); // auto height

        // ── Left side: Form ──────────────────────────────────────

        VerticalLayout formSide = new VerticalLayout();
        formSide.addClassName("reg-form-side");
        formSide.setPadding(true);
        formSide.setSpacing(false);
        formSide.setWidth("55%");
        formSide.getStyle()
                .set("padding", "48px 40px")
                .set("box-sizing", "border-box")
                .set("gap", "0");

        H2 title = new H2("Sign Up");
        title.addClassName("reg-title");

        RegistrationFormPanel formPanel = new RegistrationFormPanel(
                strUserReferCode, publicIp, recordService, section, strCalledFrom, emailSendService, this::close);

        formSide.add(title, formPanel);

        // ── Right side: Illustration ─────────────────────────────

        VerticalLayout illustrationSide = new VerticalLayout();
        illustrationSide.addClassName("reg-illustration-side");
        illustrationSide.setWidth("45%");
        illustrationSide.setAlignItems(FlexComponent.Alignment.CENTER);
        illustrationSide.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        illustrationSide.getStyle()
                .set("padding", "48px 32px")
                .set("box-sizing", "border-box");

        Div illustrationWrapper = new Div();
        illustrationWrapper.addClassName("reg-illustration-wrapper");

        Icon illustrationIcon = VaadinIcon.GROUP.create();
        illustrationIcon.addClassName("reg-illustration-icon");

        Paragraph illustrationText = new Paragraph("Join our community");
        illustrationText.addClassName("reg-illustration-text");

        illustrationWrapper.add(illustrationIcon);
        illustrationSide.add(illustrationWrapper, illustrationText);

        // ── Compose dialog ───────────────────────────────────────

        HorizontalLayout dialogContent = new HorizontalLayout(formSide, illustrationSide);
        dialogContent.addClassName("reg-dialog-content");
        dialogContent.setSizeFull();
        dialogContent.setSpacing(false);
        dialogContent.setPadding(false);

        add(dialogContent);
    }
}
