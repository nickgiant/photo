package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.dto.FestivalDto;
import com.photo.act.photo_act.dto.FestivalEditionDto;
import com.photo.act.photo_act.services.FestivalService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Single dialog reused for creating and editing an event: the festival itself, plus its
 * editions. The festival has its own save; editions are managed underneath as a small,
 * self-contained CRUD panel — a card per edition (far right: edit / delete icon buttons)
 * above a form that either adds a new edition or edits whichever card was picked.
 *
 * Usage:
 *   new EventDialog(festivalService, saved -> reloadResults()).open();                          // create
 *   new EventDialog(existingFestival, festivalService, saved -> reloadResults()).open();         // edit
 *   new EventDialog(existingFestival, editionToPreload, festivalService, saved -> ...).open();   // edit, one edition pre-loaded
 */
public class EventDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(EventDialog.class);

    private static final List<String> TYPES = List.of(
            "Festival", "Exhibition", "Photo Walk", "Competition", "Workshop", "Other");

    private static final DateTimeFormatter EDITION_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final FestivalService       festivalService;
    private final boolean               editMode;
    private final Consumer<FestivalDto> onSaved;

    /** Null until the festival has been saved at least once (create mode, before first save). */
    private Long festivalId;
    /** Null while the edition form is in "add new" mode; set to the id being edited otherwise. */
    private Long currentEditingEditionId;

    // ── festival fields ──
    private final TextField   fldNameShort     = new TextField("Short Name");
    private final TextField   fldNameFull      = new TextField("Full Name");
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

    // ── edition fields (shared by "add" and "edit" — see currentEditingEditionId) ──
    private final TextField   fldEditionTitle       = new TextField("Edition Title");
    private final TextField   fldEditionSubtitle    = new TextField("Edition Subtitle");
    private final DatePicker  fldDateFrom           = new DatePicker("Date From");
    private final DatePicker  fldDateTo             = new DatePicker("Date To");
    private final TextField   fldTitleOfPlace       = new TextField("Venue");
    private final TextField   fldAddressOfPlace     = new TextField("Address");
    private final TextField   fldUrlPlanned         = new TextField("Edition URL");
    private final TextField   fldUrlFb              = new TextField("Edition Facebook URL");
    private final TextField   fldUrlInsta           = new TextField("Edition Instagram URL");
    private final TextArea    fldEditionDescription = new TextArea("Edition Description");

    private final H4               editionFormHeading = new H4("New Edition");
    private final Button            btnSaveEdition     = new Button("Add Edition");
    private final Button            btnCancelEditEdition = new Button("Cancel");
    private final VerticalLayout    editionsListContainer = new VerticalLayout();
    private final VerticalLayout    editionsSection = new VerticalLayout();

    /** Create mode. */
    public EventDialog(FestivalService festivalService, Consumer<FestivalDto> onSaved) {
        this.festivalService = festivalService;
        this.onSaved         = onSaved;
        this.editMode         = false;
        this.festivalId       = null;

        setDraggable(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setWidth("880px");

        add(buildLayout());
        editionsSection.setVisible(false);
    }

    /** Edit mode — pre-fills the festival form; lists all its editions as cards. */
    public EventDialog(FestivalDto editingFestival, FestivalService festivalService, Consumer<FestivalDto> onSaved) {
        this(editingFestival, null, festivalService, onSaved);
    }

    /** Edit mode with one specific edition pre-loaded into the edition form (e.g. from a timeline card). */
    public EventDialog(FestivalDto editingFestival, FestivalEditionDto initialEdition,
                       FestivalService festivalService, Consumer<FestivalDto> onSaved) {
        this.festivalService = festivalService;
        this.onSaved         = onSaved;
        this.editMode         = true;
        this.festivalId       = editingFestival.getId();

        setDraggable(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setWidth("880px");

        add(buildLayout());
        populateFestivalForm(editingFestival);
        editionsSection.setVisible(true);
        refreshEditionsList();
        if (initialEdition != null) {
            loadEditionIntoForm(initialEdition);
        }
    }

    private VerticalLayout buildLayout() {

        H3 title = new H3(editMode ? "Edit Event" : "Create Event");
        title.addClassNames(Margin.NONE);

        Button btnClose = new Button(VaadinIcon.CLOSE.create());
        btnClose.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnClose.addClickListener(e -> close());

        HorizontalLayout header = new HorizontalLayout(title, btnClose);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, title);
        header.addClassNames(Padding.Bottom.SMALL);

        // ── festival form ──
        fldType.setLabel("Type");
        fldType.setItems(TYPES);
        fldType.setPlaceholder("Select type…");

        fldNameShort.setRequired(true);
        fldNameShort.setPlaceholder("e.g. Xposure");
        fldNameFull.setPlaceholder("e.g. Xposure International Photography Festival");
        fldActivities.setMinHeight("80px");

        FormLayout festivalForm = new FormLayout();
        festivalForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("480px", 2));
        festivalForm.add(fldNameShort, fldNameFull,
                fldType, fldCountry,
                fldPeriodOfYear, fldWebsite,
                fldUrlFacebook, fldUrlInstagram,
                fldUrlYoutube, fldImageTop,
                fldImageLogo);
        festivalForm.setColspan(fldActivities, 2);
        festivalForm.add(fldActivities);

        Button btnSaveFestival = new Button(editMode ? "Save Changes" : "Create Event");
        btnSaveFestival.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSaveFestival.addClickListener(e -> saveFestival());

        Button btnCancel = new Button("Cancel");
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCancel.addClickListener(e -> close());

        HorizontalLayout festivalFooter = new HorizontalLayout(btnCancel, btnSaveFestival);
        festivalFooter.setWidthFull();
        festivalFooter.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        festivalFooter.addClassNames(Padding.Top.MEDIUM);

        // ── editions panel: cards + add/edit form ──
        H4 editionsHeading = new H4("Editions");
        editionsHeading.addClassNames(Margin.Top.NONE, Margin.Bottom.NONE);

        editionsListContainer.setWidthFull();
        editionsListContainer.setPadding(false);
        editionsListContainer.addClassNames(Gap.SMALL);

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

        btnSaveEdition.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSaveEdition.addClickListener(e -> saveEdition());

        btnCancelEditEdition.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCancelEditEdition.setVisible(false);
        btnCancelEditEdition.addClickListener(e -> clearEditionForm());

        HorizontalLayout editionFormFooter = new HorizontalLayout(btnCancelEditEdition, btnSaveEdition);
        editionFormFooter.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        editionFormFooter.setWidthFull();

        editionsSection.setPadding(false);
        editionsSection.addClassNames(Gap.SMALL, Margin.Top.MEDIUM);
        editionsSection.add(editionsHeading, editionsListContainer, new Hr(),
                editionFormHeading, editionForm, editionFormFooter);

        VerticalLayout root = new VerticalLayout(header, new Hr(),
                festivalForm, festivalFooter,
                editionsSection);
        root.setPadding(true);
        root.setSpacing(false);
        root.addClassNames(Gap.SMALL);
        root.setMaxHeight("82vh");
        return root;
    }

    private void populateFestivalForm(FestivalDto festival) {
        fldNameShort.setValue(nvl(festival.getNameShort()));
        fldNameFull.setValue(nvl(festival.getNameFull()));
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
    }

    private void saveFestival() {
        if (fldNameShort.getValue().isBlank()) {
            fldNameShort.setInvalid(true);
            fldNameShort.setErrorMessage("Short name is required");
            return;
        }
        try {
            FestivalDto festivalDto = buildFestivalDto();
            FestivalDto saved = festivalId != null
                    ? festivalService.updateFestival(festivalId, festivalDto)
                            .orElseThrow(() -> new IllegalStateException("Festival not found"))
                    : festivalService.createFestival(festivalDto);
            festivalId = saved.getId();

            if (!editMode) {
                // Preserve the original one-shot flow: bundle whatever was typed into the
                // edition form as the festival's first edition, if anything was entered.
                FestivalEditionDto editionDto = buildEditionDto(festivalId, null);
                if (hasEditionData(editionDto)) {
                    festivalService.createEdition(editionDto);
                }
            }

            close();
            if (onSaved != null) onSaved.accept(saved);
            Notification.show(editMode ? "Event updated." : "Event created.",
                    3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            log.error("Failed to save event", ex);
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private FestivalDto buildFestivalDto() {
        FestivalDto dto = festivalId != null ? FestivalDto.builder().id(festivalId).build() : FestivalDto.builder().build();
        dto.setNameShort(fldNameShort.getValue().trim());
        dto.setNameFull(emptyToNull(fldNameFull.getValue()));
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

    // ───────────────────────── Editions panel ───────────────────────────

    private void refreshEditionsList() {
        editionsListContainer.removeAll();
        if (festivalId == null) return;

        List<FestivalEditionDto> editions = festivalService.getEditionsByFestival(festivalId);
        if (editions.isEmpty()) {
            Span empty = new Span("No editions yet — add one below.");
            empty.addClassNames(TextColor.TERTIARY, FontSize.SMALL);
            editionsListContainer.add(empty);
            return;
        }
        for (FestivalEditionDto edition : editions) {
            editionsListContainer.add(buildEditionCard(edition));
        }
    }

    private HorizontalLayout buildEditionCard(FestivalEditionDto edition) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.BETWEEN,
                Padding.SMALL, Gap.SMALL,
                Background.CONTRAST_5, BorderRadius.MEDIUM);

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.addClassNames(Gap.XSMALL);

        String titleText = (edition.getTitle() == null || edition.getTitle().isBlank())
                ? "Untitled edition" : edition.getTitle();
        Span cardTitle = new Span(titleText);
        cardTitle.addClassNames(FontWeight.SEMIBOLD);

        Span cardMeta = new Span(editionMetaLine(edition));
        cardMeta.addClassNames(FontSize.SMALL, TextColor.SECONDARY);

        info.add(cardTitle, cardMeta);

        Button btnEdit = new Button(VaadinIcon.PENCIL.create());
        btnEdit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        btnEdit.setTooltipText("Edit edition");
        btnEdit.addClickListener(e -> loadEditionIntoForm(edition));

        Button btnDelete = new Button(VaadinIcon.TRASH.create());
        btnDelete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
        btnDelete.setTooltipText("Delete edition");
        btnDelete.addClickListener(e -> confirmDeleteEdition(edition));

        HorizontalLayout actions = new HorizontalLayout(btnEdit, btnDelete);
        actions.addClassNames(Gap.XSMALL);

        card.add(info, actions);
        return card;
    }

    private String editionMetaLine(FestivalEditionDto edition) {
        List<String> parts = new ArrayList<>();
        if (edition.getDateFrom() != null) {
            String range = EDITION_DATE_FORMAT.format(edition.getDateFrom());
            if (edition.getDateTo() != null) {
                range += " – " + EDITION_DATE_FORMAT.format(edition.getDateTo());
            }
            parts.add(range);
        }
        if (edition.getTitleOfPlace() != null && !edition.getTitleOfPlace().isBlank()) {
            parts.add(edition.getTitleOfPlace());
        }
        return parts.isEmpty() ? "No dates set" : String.join(" · ", parts);
    }

    private void loadEditionIntoForm(FestivalEditionDto edition) {
        currentEditingEditionId = edition.getId();
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

        String label = (edition.getTitle() == null || edition.getTitle().isBlank())
                ? "Untitled edition" : edition.getTitle();
        editionFormHeading.setText("Editing: " + label);
        btnSaveEdition.setText("Save Edition");
        btnCancelEditEdition.setVisible(true);
    }

    private void clearEditionForm() {
        currentEditingEditionId = null;
        fldEditionTitle.clear();
        fldEditionSubtitle.clear();
        fldDateFrom.clear();
        fldDateTo.clear();
        fldTitleOfPlace.clear();
        fldAddressOfPlace.clear();
        fldUrlPlanned.clear();
        fldUrlFb.clear();
        fldUrlInsta.clear();
        fldEditionDescription.clear();

        editionFormHeading.setText("New Edition");
        btnSaveEdition.setText("Add Edition");
        btnCancelEditEdition.setVisible(false);
    }

    private void saveEdition() {
        if (festivalId == null) {
            Notification.show("Save the festival first.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        try {
            FestivalEditionDto dto = buildEditionDto(festivalId, currentEditingEditionId);
            if (currentEditingEditionId != null) {
                festivalService.updateEdition(currentEditingEditionId, dto)
                        .orElseThrow(() -> new IllegalStateException("Edition not found"));
                Notification.show("Edition updated.", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                festivalService.createEdition(dto);
                Notification.show("Edition added.", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            clearEditionForm();
            refreshEditionsList();
        } catch (Exception ex) {
            log.error("Failed to save edition", ex);
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeleteEdition(FestivalEditionDto edition) {
        String label = (edition.getTitle() == null || edition.getTitle().isBlank())
                ? "this edition" : "\"" + edition.getTitle() + "\"";

        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Delete edition");
        confirm.setText("Delete " + label + "? This cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> deleteEdition(edition));
        confirm.open();
    }

    private void deleteEdition(FestivalEditionDto edition) {
        try {
            festivalService.deleteEdition(edition.getId());
            if (edition.getId().equals(currentEditingEditionId)) {
                clearEditionForm();
            }
            refreshEditionsList();
            Notification.show("Edition deleted.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            log.error("Failed to delete edition", ex);
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private FestivalEditionDto buildEditionDto(Long festivalId, Long editionId) {
        FestivalEditionDto dto = editionId != null
                ? FestivalEditionDto.builder().id(editionId).build()
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
