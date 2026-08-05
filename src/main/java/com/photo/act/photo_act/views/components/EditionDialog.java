package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.dto.FestivalEditionDto;
import com.photo.act.photo_act.services.FestivalService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Dialog for adding or editing a single festival edition. Opened from EventDialog's
 * editions panel — the "Add Edition" action and each card's pencil icon.
 *
 * Usage:
 *   new EditionDialog(festivalId, festivalService, saved -> refreshEditionsList()).open();
 *   new EditionDialog(existing, festivalId, festivalService, saved -> refreshEditionsList()).open();
 */
public class EditionDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(EditionDialog.class);

    private final FestivalService                festivalService;
    private final Long                            festivalId;
    private final FestivalEditionDto              editing;
    private final Consumer<FestivalEditionDto>    onSaved;

    private final TextField   fldTitle       = new TextField("Edition Title");
    private final TextField   fldSubtitle    = new TextField("Edition Subtitle");
    private final DatePicker  fldDateFrom    = new DatePicker("Date From");
    private final DatePicker  fldDateTo      = new DatePicker("Date To");
    private final TextField   fldTitleOfPlace   = new TextField("Venue");
    private final TextField   fldAddressOfPlace = new TextField("Address");
    private final TextField   fldUrlPlanned  = new TextField("Edition URL");
    private final TextField   fldUrlFb       = new TextField("Edition Facebook URL");
    private final TextField   fldUrlInsta    = new TextField("Edition Instagram URL");
    private final TextArea    fldDescription = new TextArea("Edition Description");

    /** Create mode. */
    public EditionDialog(Long festivalId, FestivalService festivalService, Consumer<FestivalEditionDto> onSaved) {
        this(null, festivalId, festivalService, onSaved);
    }

    /** Edit mode — pre-fills the form from the existing edition. */
    public EditionDialog(FestivalEditionDto editing, Long festivalId,
                         FestivalService festivalService, Consumer<FestivalEditionDto> onSaved) {
        this.editing         = editing;
        this.festivalId      = festivalId;
        this.festivalService = festivalService;
        this.onSaved         = onSaved;

        setDraggable(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setWidth("680px");

        add(buildLayout());
        if (editing != null) populateForm(editing);
    }

    private VerticalLayout buildLayout() {
        boolean isEdit = editing != null;

        H3 title = new H3(isEdit ? "Edit Edition" : "Add Edition");
        title.addClassNames(Margin.NONE);

        Button btnClose = new Button(VaadinIcon.CLOSE.create());
        btnClose.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnClose.addClickListener(e -> close());

        HorizontalLayout header = new HorizontalLayout(title, btnClose);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, title);
        header.addClassNames(Padding.Bottom.SMALL);

        fldTitle.setPlaceholder("e.g. 10th edition — \"Light Across Borders\"");
        fldTitle.setWidthFull();
        fldDescription.setMinHeight("100px");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("480px", 2));
        form.add(fldTitle, fldSubtitle,
                fldDateFrom, fldDateTo,
                fldTitleOfPlace, fldAddressOfPlace,
                fldUrlPlanned, fldUrlFb, fldUrlInsta);
        form.setColspan(fldDescription, 2);
        form.add(fldDescription);

        Button btnSave = new Button(isEdit ? "Save Changes" : "Add Edition");
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

    private void populateForm(FestivalEditionDto dto) {
        fldTitle.setValue(nvl(dto.getTitle()));
        fldSubtitle.setValue(nvl(dto.getSubtitle()));
        fldDateFrom.setValue(dto.getDateFrom());
        fldDateTo.setValue(dto.getDateTo());
        fldTitleOfPlace.setValue(nvl(dto.getTitleOfPlace()));
        fldAddressOfPlace.setValue(nvl(dto.getAddressOfPlace()));
        fldUrlPlanned.setValue(nvl(dto.getUrlPlanned()));
        fldUrlFb.setValue(nvl(dto.getUrlFb()));
        fldUrlInsta.setValue(nvl(dto.getUrlInsta()));
        fldDescription.setValue(nvl(dto.getEditionDescription()));
    }

    private void save() {
        try {
            FestivalEditionDto dto = buildDto();
            FestivalEditionDto saved = editing != null
                    ? festivalService.updateEdition(editing.getId(), dto)
                            .orElseThrow(() -> new IllegalStateException("Edition not found"))
                    : festivalService.createEdition(dto);
            close();
            if (onSaved != null) onSaved.accept(saved);
            Notification.show(editing != null ? "Edition updated." : "Edition added.",
                    3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            log.error("Failed to save edition", ex);
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private FestivalEditionDto buildDto() {
        FestivalEditionDto dto = editing != null
                ? FestivalEditionDto.builder().id(editing.getId()).build()
                : FestivalEditionDto.builder().build();
        dto.setFestivalId(festivalId);
        dto.setTitle(emptyToNull(fldTitle.getValue()));
        dto.setSubtitle(emptyToNull(fldSubtitle.getValue()));
        dto.setDateFrom(fldDateFrom.getValue());
        dto.setDateTo(fldDateTo.getValue());
        dto.setTitleOfPlace(emptyToNull(fldTitleOfPlace.getValue()));
        dto.setAddressOfPlace(emptyToNull(fldAddressOfPlace.getValue()));
        dto.setUrlPlanned(emptyToNull(fldUrlPlanned.getValue()));
        dto.setUrlFb(emptyToNull(fldUrlFb.getValue()));
        dto.setUrlInsta(emptyToNull(fldUrlInsta.getValue()));
        dto.setEditionDescription(emptyToNull(fldDescription.getValue()));
        return dto;
    }

    private static String nvl(String s)        { return s == null ? "" : s; }
    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
}
