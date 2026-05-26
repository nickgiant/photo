package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.dto.TutorDto;
import com.photo.act.photo_act.services.TutorService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Dialog for creating and editing a tutor.
 *
 * Usage:
 *   new TutorDialog(tutorService, saved -> refreshGrid()).open();
 *   new TutorDialog(existingDto, tutorService, saved -> refreshGrid()).open();
 */
public class TutorDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(TutorDialog.class);

    private final TutorService        tutorService;
    private final TutorDto            editing;
    private final Consumer<TutorDto>  onSaved;

    private final TextField fldTutorName    = new TextField("Tutor Name");
    private final TextField fldWebsite      = new TextField("Website");
    private final TextField fldUrlYt        = new TextField("YouTube URL");
    private final TextField fldUrlFb        = new TextField("Facebook URL");
    private final TextField fldUrlInsta     = new TextField("Instagram URL");
    private final TextField fldUrlFlickr    = new TextField("Flickr URL");
    private final TextField fldUrlWikipedia = new TextField("Wikipedia URL");
    private final TextField fldUrlRef1      = new TextField("Reference URL 1");
    private final TextField fldUrlRef2      = new TextField("Reference URL 2");
    private final TextField fldUrlRef3      = new TextField("Reference URL 3");
    private final TextField fldCityBase     = new TextField("City");
    private final TextField fldCountryBase  = new TextField("Country");

    /** Create mode. */
    public TutorDialog(TutorService tutorService, Consumer<TutorDto> onSaved) {
        this(null, tutorService, onSaved);
    }

    /** Edit mode — pre-fills form from existing dto. */
    public TutorDialog(TutorDto editing, TutorService tutorService, Consumer<TutorDto> onSaved) {
        this.editing      = editing;
        this.tutorService = tutorService;
        this.onSaved      = onSaved;

        setDraggable(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setWidth("680px");

        add(buildLayout());
        if (editing != null) populateForm(editing);
    }

    private VerticalLayout buildLayout() {
        boolean isEdit = editing != null;

        H3 title = new H3(isEdit ? "Edit Tutor" : "New Tutor");
        title.addClassNames(Margin.NONE);

        Button btnClose = new Button(VaadinIcon.CLOSE.create());
        btnClose.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnClose.addClickListener(e -> close());

        HorizontalLayout header = new HorizontalLayout(title, btnClose);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, title);
        header.addClassNames(Padding.Bottom.SMALL);

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",    1),
                new FormLayout.ResponsiveStep("480px", 2));

        fldTutorName.setRequired(true);
        fldTutorName.setPlaceholder("e.g. Scott Kelby");
        fldTutorName.setWidthFull();

        form.add(fldTutorName, fldWebsite,
                 fldUrlYt, fldUrlFb,
                 fldUrlInsta, fldUrlFlickr,
                 fldUrlWikipedia, fldUrlRef1,
                 fldUrlRef2, fldUrlRef3,
                 fldCityBase, fldCountryBase);

        Button btnSave = new Button(isEdit ? "Save Changes" : "Create Tutor");
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSave.addClickListener(e -> save());

        Button btnCancel = new Button("Cancel");
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCancel.addClickListener(e -> close());

        HorizontalLayout footer = new HorizontalLayout(btnCancel, btnSave);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.addClassNames(Padding.Top.MEDIUM);

        VerticalLayout root = new VerticalLayout(header, new Hr(), form, footer);
        root.setPadding(true);
        root.setSpacing(false);
        root.addClassNames(Gap.SMALL);
        return root;
    }

    private void populateForm(TutorDto dto) {
        fldTutorName.setValue(nvl(dto.getTutorName()));
        fldWebsite.setValue(nvl(dto.getWebsite()));
        fldUrlYt.setValue(nvl(dto.getUrlYt()));
        fldUrlFb.setValue(nvl(dto.getUrlFb()));
        fldUrlInsta.setValue(nvl(dto.getUrlInsta()));
        fldUrlFlickr.setValue(nvl(dto.getUrlFlickr()));
        fldUrlWikipedia.setValue(nvl(dto.getUrlWikipedia()));
        fldUrlRef1.setValue(nvl(dto.getUrlRef1()));
        fldUrlRef2.setValue(nvl(dto.getUrlRef2()));
        fldUrlRef3.setValue(nvl(dto.getUrlRef3()));
        fldCityBase.setValue(nvl(dto.getCityBase()));
        fldCountryBase.setValue(nvl(dto.getCountryBase()));
    }

    private void save() {
        if (fldTutorName.getValue().isBlank()) {
            fldTutorName.setInvalid(true);
            fldTutorName.setErrorMessage("Tutor name is required");
            return;
        }
        try {
            TutorDto dto = buildDto();
            TutorDto saved;
            if (editing != null) {
                saved = tutorService.updateTutor(editing.getId(), dto)
                        .orElseThrow(() -> new IllegalStateException("Tutor not found"));
            } else {
                saved = tutorService.createTutor(dto);
            }
            close();
            if (onSaved != null) onSaved.accept(saved);
            Notification.show(editing != null ? "Tutor updated." : "Tutor created.",
                    3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            log.error("Failed to save tutor", ex);
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private TutorDto buildDto() {
        TutorDto dto = editing != null ? TutorDto.builder()
                .id(editing.getId())
                .userIdInsert(editing.getUserIdInsert())
                .username(editing.getUsername())
                .build()
                : TutorDto.builder().build();
        dto.setTutorName(fldTutorName.getValue().trim());
        dto.setWebsite(emptyToNull(fldWebsite.getValue()));
        dto.setUrlYt(emptyToNull(fldUrlYt.getValue()));
        dto.setUrlFb(emptyToNull(fldUrlFb.getValue()));
        dto.setUrlInsta(emptyToNull(fldUrlInsta.getValue()));
        dto.setUrlFlickr(emptyToNull(fldUrlFlickr.getValue()));
        dto.setUrlWikipedia(emptyToNull(fldUrlWikipedia.getValue()));
        dto.setUrlRef1(emptyToNull(fldUrlRef1.getValue()));
        dto.setUrlRef2(emptyToNull(fldUrlRef2.getValue()));
        dto.setUrlRef3(emptyToNull(fldUrlRef3.getValue()));
        dto.setCityBase(emptyToNull(fldCityBase.getValue()));
        dto.setCountryBase(emptyToNull(fldCountryBase.getValue()));
        return dto;
    }

    private static String nvl(String s)        { return s == null ? "" : s; }
    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
}
