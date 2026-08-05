package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.dto.FestivalDto;
import com.photo.act.photo_act.dto.FestivalEditionDto;
import com.photo.act.photo_act.services.FestivalService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Single dialog reused for both creating and editing an event — a festival plus its
 * (current/next) edition. Saving writes the festival row first, then the edition row
 * against the resulting festival id.
 *
 * Usage:
 *   new EventDialog(festivalService, saved -> reloadResults()).open();
 *   new EventDialog(existingFestival, existingEdition, festivalService, saved -> reloadResults()).open();
 */
public class EventDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(EventDialog.class);

    private static final List<String> TYPES = List.of(
            "Festival", "Exhibition", "Photo Walk", "Competition", "Workshop", "Other");

    private final FestivalService              festivalService;
    private final FestivalDto                  editingFestival;
    private final FestivalEditionDto           editingEdition;
    private final Consumer<FestivalDto>        onSaved;

    // ── festival fields ──
    private final TextField   fldNameShort     = new TextField("Name");
    private final Select<String> fldType       = new Select<>();
    private final TextField   fldCountry       = new TextField("Country");
    private final TextField   fldPeriodOfYear  = new TextField("Period of Year");
    private final TextField   fldWebsite       = new TextField("Website");
    private final TextField   fldUrlFacebook   = new TextField("Facebook URL");
    private final TextField   fldUrlInstagram  = new TextField("Instagram URL");
    private final TextField   fldUrlYoutube    = new TextField("YouTube URL");
    private final TextField   fldImageTop      = new TextField("Banner Image Path");
    private final TextField   fldImageLogo     = new TextField("Logo Image Path");
    private final TextArea    fldActivities    = new TextArea("Activities");

    // ── edition fields ──
    private final TextField   fldEditionTitle    = new TextField("Edition Title");
    private final TextField   fldEditionSubtitle = new TextField("Edition Subtitle");
    private final DatePicker  fldDateFrom        = new DatePicker("Date From");
    private final DatePicker  fldDateTo          = new DatePicker("Date To");
    private final TextField   fldTitleOfPlace    = new TextField("Venue");
    private final TextField   fldAddressOfPlace  = new TextField("Address");
    private final TextField   fldUrlPlanned      = new TextField("Edition URL");
    private final TextField   fldUrlFb           = new TextField("Edition Facebook URL");
    private final TextField   fldUrlInsta        = new TextField("Edition Instagram URL");
    private final TextArea    fldEditionDescription = new TextArea("Edition Description");

    /** Create mode. */
    public EventDialog(FestivalService festivalService, Consumer<FestivalDto> onSaved) {
        this(null, null, festivalService, onSaved);
    }

    /** Edit mode — pre-fills form from the existing festival and (optional) edition. */
    public EventDialog(FestivalDto editingFestival, FestivalEditionDto editingEdition,
                       FestivalService festivalService, Consumer<FestivalDto> onSaved) {
        this.editingFestival = editingFestival;
        this.editingEdition  = editingEdition;
        this.festivalService = festivalService;
        this.onSaved         = onSaved;

        setDraggable(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setWidth("820px");

        add(buildLayout());
        if (editingFestival != null) populateForm(editingFestival, editingEdition);
    }

    private VerticalLayout buildLayout() {
        boolean isEdit = editingFestival != null;

        H3 title = new H3(isEdit ? "Edit Event" : "Create Event");
        title.addClassNames(Margin.NONE);

        Button btnClose = new Button(VaadinIcon.CLOSE.create());
        btnClose.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnClose.addClickListener(e -> close());

        HorizontalLayout header = new HorizontalLayout(title, btnClose);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, title);
        header.addClassNames(Padding.Bottom.SMALL);

        fldType.setLabel("Type");
        fldType.setItems(TYPES);
        fldType.setPlaceholder("Select type…");

        fldNameShort.setRequired(true);
        fldNameShort.setPlaceholder("e.g. Xposure International Photography Festival");
        fldNameShort.setWidthFull();
        fldActivities.setMinHeight("80px");

        H4 festivalHeading = new H4("Festival");
        festivalHeading.addClassNames(Margin.Top.MEDIUM, Margin.Bottom.NONE);

        FormLayout festivalForm = new FormLayout();
        festivalForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("480px", 2));
        festivalForm.add(fldNameShort, fldType,
                fldCountry, fldPeriodOfYear,
                fldWebsite, fldUrlFacebook,
                fldUrlInstagram, fldUrlYoutube,
                fldImageTop, fldImageLogo);
        festivalForm.setColspan(fldActivities, 2);
        festivalForm.add(fldActivities);

        H4 editionHeading = new H4("Current / Next Edition");
        editionHeading.addClassNames(Margin.Top.MEDIUM, Margin.Bottom.NONE);

        fldEditionTitle.setPlaceholder("e.g. 10th edition — \"Light Across Borders\"");
        fldEditionDescription.setMinHeight("80px");

        FormLayout editionForm = new FormLayout();
        editionForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("480px", 2));
        editionForm.add(fldEditionTitle, fldEditionSubtitle,
                fldDateFrom, fldDateTo,
                fldTitleOfPlace, fldAddressOfPlace,
                fldUrlPlanned, fldUrlFb, fldUrlInsta);
        editionForm.setColspan(fldEditionDescription, 2);
        editionForm.add(fldEditionDescription);

        Button btnSave = new Button(isEdit ? "Save Changes" : "Create Event");
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSave.addClickListener(e -> save());

        Button btnCancel = new Button("Cancel");
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCancel.addClickListener(e -> close());

        HorizontalLayout footer = new HorizontalLayout(btnCancel, btnSave);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.addClassNames(Padding.Top.MEDIUM);

        VerticalLayout root = new VerticalLayout(header, new Hr(),
                festivalHeading, festivalForm,
                editionHeading, editionForm,
                footer);
        root.setPadding(true);
        root.setSpacing(false);
        root.addClassNames(Gap.SMALL);
        root.setMaxHeight("80vh");
        return root;
    }

    private void populateForm(FestivalDto festival, FestivalEditionDto edition) {
        fldNameShort.setValue(nvl(festival.getNameShort()));
        if (festival.getType() != null) fldType.setValue(festival.getType());
        fldCountry.setValue(nvl(festival.getCountry()));
        fldPeriodOfYear.setValue(nvl(festival.getPeriodOfYear()));
        fldWebsite.setValue(nvl(festival.getWebsite()));
        fldUrlFacebook.setValue(nvl(festival.getUrlFacebook()));
        fldUrlInstagram.setValue(nvl(festival.getUrlInstagram()));
        fldUrlYoutube.setValue(nvl(festival.getUrlYoutube()));
        fldImageTop.setValue(nvl(festival.getImageTop()));
        fldImageLogo.setValue(nvl(festival.getImageLogo()));
        fldActivities.setValue(nvl(festival.getActivities()));

        if (edition != null) {
            fldEditionTitle.setValue(nvl(edition.getTitle()));
            fldEditionSubtitle.setValue(nvl(edition.getSubtitle()));
            fldDateFrom.setValue(edition.getDateFrom());
            fldDateTo.setValue(edition.getDateTo());
            fldTitleOfPlace.setValue(nvl(edition.getTitleOfPlace()));
            fldAddressOfPlace.setValue(nvl(edition.getAddressOfPlace()));
            fldUrlPlanned.setValue(nvl(edition.getUrlPlanned()));
            fldUrlFb.setValue(nvl(edition.getUrlFb()));
            fldUrlInsta.setValue(nvl(edition.getUrlInsta()));
            fldEditionDescription.setValue(nvl(edition.getEditionDescription()));
        }
    }

    private void save() {
        if (fldNameShort.getValue().isBlank()) {
            fldNameShort.setInvalid(true);
            fldNameShort.setErrorMessage("Name is required");
            return;
        }
        try {
            FestivalDto festivalDto = buildFestivalDto();
            FestivalDto savedFestival = editingFestival != null
                    ? festivalService.updateFestival(editingFestival.getId(), festivalDto)
                            .orElseThrow(() -> new IllegalStateException("Festival not found"))
                    : festivalService.createFestival(festivalDto);

            FestivalEditionDto editionDto = buildEditionDto(savedFestival.getId());
            if (editingEdition != null) {
                festivalService.updateEdition(editingEdition.getId(), editionDto)
                        .orElseThrow(() -> new IllegalStateException("Edition not found"));
            } else if (hasEditionData(editionDto)) {
                festivalService.createEdition(editionDto);
            }

            close();
            if (onSaved != null) onSaved.accept(savedFestival);
            Notification.show(editingFestival != null ? "Event updated." : "Event created.",
                    3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            log.error("Failed to save event", ex);
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private FestivalDto buildFestivalDto() {
        FestivalDto dto = editingFestival != null
                ? FestivalDto.builder().id(editingFestival.getId()).build()
                : FestivalDto.builder().build();
        dto.setNameShort(fldNameShort.getValue().trim());
        dto.setType(fldType.getValue());
        dto.setCountry(emptyToNull(fldCountry.getValue()));
        dto.setPeriodOfYear(emptyToNull(fldPeriodOfYear.getValue()));
        dto.setWebsite(emptyToNull(fldWebsite.getValue()));
        dto.setUrlFacebook(emptyToNull(fldUrlFacebook.getValue()));
        dto.setUrlInstagram(emptyToNull(fldUrlInstagram.getValue()));
        dto.setUrlYoutube(emptyToNull(fldUrlYoutube.getValue()));
        dto.setImageTop(emptyToNull(fldImageTop.getValue()));
        dto.setImageLogo(emptyToNull(fldImageLogo.getValue()));
        dto.setActivities(emptyToNull(fldActivities.getValue()));
        return dto;
    }

    private FestivalEditionDto buildEditionDto(Long festivalId) {
        FestivalEditionDto dto = editingEdition != null
                ? FestivalEditionDto.builder().id(editingEdition.getId()).build()
                : FestivalEditionDto.builder().build();
        dto.setFestivalId(festivalId);
        dto.setTitle(emptyToNull(fldEditionTitle.getValue()));
        dto.setSubtitle(emptyToNull(fldEditionSubtitle.getValue()));
        dto.setDateFrom(fldDateFrom.getValue());
        dto.setDateTo(fldDateTo.getValue());
        dto.setTitleOfPlace(emptyToNull(fldTitleOfPlace.getValue()));
        dto.setAddressOfPlace(emptyToNull(fldAddressOfPlace.getValue()));
        dto.setUrlPlanned(emptyToNull(fldUrlPlanned.getValue()));
        dto.setUrlFb(emptyToNull(fldUrlFb.getValue()));
        dto.setUrlInsta(emptyToNull(fldUrlInsta.getValue()));
        dto.setEditionDescription(emptyToNull(fldEditionDescription.getValue()));
        return dto;
    }

    /** Skip creating an empty edition row when the optional edition fields were all left blank. */
    private boolean hasEditionData(FestivalEditionDto dto) {
        return dto.getTitle() != null || dto.getDateFrom() != null || dto.getDateTo() != null
                || dto.getTitleOfPlace() != null || dto.getEditionDescription() != null;
    }

    private static String nvl(String s)        { return s == null ? "" : s; }
    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
}
