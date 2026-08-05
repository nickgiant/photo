package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.dto.DestinationDto;
import com.photo.act.photo_act.dto.FestivalDto;
import com.photo.act.photo_act.dto.FestivalEditionDto;
import com.photo.act.photo_act.services.DestinationService;
import com.photo.act.photo_act.services.FestivalService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dialog for creating and editing a festival. Its editions are managed as a separate,
 * self-contained panel underneath: a card per edition (far right: edit / delete icon
 * buttons), each opening its own EditionDialog rather than an inline form.
 *
 * Usage:
 *   new EventDialog(festivalService, destinationService, saved -> reloadResults()).open();                    // create
 *   new EventDialog(existingFestival, festivalService, destinationService, saved -> reloadResults()).open();  // edit
 *   new EventDialog(existingFestival, editionToOpen, festivalService, destinationService, saved -> ...)       // edit, jumps
 *       .open();                                                                                              // straight into
 *                                                                                                              // editing that edition
 */
public class EventDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(EventDialog.class);

    private static final List<String> TYPES = List.of(
            "Festival", "Exhibition", "Photo Walk", "Competition", "Workshop", "Other");

    private static final DateTimeFormatter EDITION_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final FestivalService       festivalService;
    private final DestinationService    destinationService;
    private final Consumer<FestivalDto> onSaved;

    /** Flips from false to true once a brand-new festival is first saved. */
    private boolean editMode;
    /** Null until the festival has been saved at least once (create mode, before first save). */
    private Long festivalId;

    // ── festival fields ──
    private final TextField   fldNameShort     = new TextField("Short Name");
    private final TextField   fldNameFull      = new TextField("Full Name");
    private final Select<String> fldType       = new Select<>();
    private final ComboBox<DestinationDto> fldDestination = new ComboBox<>("Destination");
    private final TextField   fldPeriodOfYear  = new TextField("Period of Year");
    private final TextField   fldWebsite       = new TextField("Website");
    private final TextField   fldUrlFacebook   = new TextField("Facebook URL");
    private final TextField   fldUrlInstagram  = new TextField("Instagram URL");
    private final TextField   fldUrlYoutube    = new TextField("YouTube URL");
    private final TextField   fldImageTop      = new TextField("Banner Image Path");
    private final TextField   fldImageLogo     = new TextField("Logo Image Path");
    private final TextArea    fldActivities    = new TextArea("Activities");

    // ── mutable chrome, updated when create mode flips to edit mode after first save ──
    private final H3     dialogTitle    = new H3();
    private final Button btnSaveFestival = new Button();

    private final VerticalLayout editionsListContainer = new VerticalLayout();
    private final TabSheet tabSheet = new TabSheet();
    private Tab editionsTab;

    private Registration deferredEditionOpenReg;

    /** Create mode. */
    public EventDialog(FestivalService festivalService, DestinationService destinationService,
                       Consumer<FestivalDto> onSaved) {
        this.festivalService   = festivalService;
        this.destinationService = destinationService;
        this.onSaved           = onSaved;
        this.editMode           = false;
        this.festivalId         = null;

        setDraggable(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setWidth("820px");

        add(buildLayout());
    }

    /** Edit mode — pre-fills the festival form; lists all its editions as cards. */
    public EventDialog(FestivalDto editingFestival, FestivalService festivalService,
                       DestinationService destinationService, Consumer<FestivalDto> onSaved) {
        this(editingFestival, null, festivalService, destinationService, onSaved);
    }

    /** Edit mode, opening straight into editing one specific edition (e.g. from a timeline card). */
    public EventDialog(FestivalDto editingFestival, FestivalEditionDto initialEdition,
                       FestivalService festivalService, DestinationService destinationService,
                       Consumer<FestivalDto> onSaved) {
        this.festivalService   = festivalService;
        this.destinationService = destinationService;
        this.onSaved           = onSaved;
        this.editMode           = true;
        this.festivalId         = editingFestival.getId();

        setDraggable(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setWidth("820px");

        add(buildLayout());
        populateFestivalForm(editingFestival);
        refreshEditionsList();
        if (initialEdition != null) {
            // Land straight on the Editions tab, then deferred-open EditionDialog once this
            // dialog is actually open, so EditionDialog stacks on top of it rather than the
            // other way around — the caller opens this dialog after construction.
            tabSheet.setSelectedTab(editionsTab);
            deferredEditionOpenReg = addOpenedChangeListener(ev -> {
                if (ev.isOpened() && deferredEditionOpenReg != null) {
                    openEditEditionDialog(initialEdition);
                    deferredEditionOpenReg.remove();
                    deferredEditionOpenReg = null;
                }
            });
        }
    }

    private VerticalLayout buildLayout() {

        dialogTitle.setText(editMode ? "Edit Event" : "Create Event");
        dialogTitle.addClassNames(Margin.NONE);

        Button btnClose = new Button(VaadinIcon.CLOSE.create());
        btnClose.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnClose.addClickListener(e -> close());

        HorizontalLayout header = new HorizontalLayout(dialogTitle, btnClose);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, dialogTitle);
        header.addClassNames(Padding.Bottom.SMALL);

        // ── festival form ──
        fldType.setLabel("Type");
        fldType.setItems(TYPES);
        fldType.setPlaceholder("Select type…");

        fldNameShort.setRequired(true);
        fldNameShort.setPlaceholder("e.g. Xposure");
        fldNameFull.setPlaceholder("e.g. Xposure International Photography Festival");
        fldActivities.setMinHeight("80px");

        List<DestinationDto> destinations = destinationService.getAllDestinations(); // already ordered by country, city
        fldDestination.setItems(destinations);
        fldDestination.setItemLabelGenerator(EventDialog::destinationLabel);
        fldDestination.setPlaceholder("Select destination…");
        fldDestination.setClearButtonVisible(true);

        FormLayout festivalForm = new FormLayout();
        festivalForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("480px", 2));
        festivalForm.add(fldNameShort, fldNameFull,
                fldType, fldDestination,
                fldPeriodOfYear, fldWebsite,
                fldUrlFacebook, fldUrlInstagram,
                fldUrlYoutube, fldImageTop,
                fldImageLogo);
        festivalForm.setColspan(fldActivities, 2);
        festivalForm.add(fldActivities);

        btnSaveFestival.setText(editMode ? "Save Changes" : "Create Event");
        btnSaveFestival.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSaveFestival.addClickListener(e -> saveFestival());

        Button btnCancel = new Button("Cancel");
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCancel.addClickListener(e -> close());

        HorizontalLayout festivalFooter = new HorizontalLayout(btnCancel, btnSaveFestival);
        festivalFooter.setWidthFull();
        festivalFooter.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        festivalFooter.addClassNames(Padding.Top.MEDIUM);

        VerticalLayout eventPanel = new VerticalLayout(festivalForm, festivalFooter);
        eventPanel.setPadding(false);
        eventPanel.addClassNames(Gap.SMALL, Padding.Top.SMALL);

        // ── editions panel: cards, each edited/deleted via its own dialog ──
        H4 editionsHeading = new H4("Editions");
        editionsHeading.addClassNames(Margin.Top.NONE, Margin.Bottom.NONE);

        Button btnAddEdition = new Button("Add Edition", VaadinIcon.PLUS.create());
        btnAddEdition.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnAddEdition.addClickListener(e -> openCreateEditionDialog());

        HorizontalLayout editionsHeadingRow = new HorizontalLayout(editionsHeading, btnAddEdition);
        editionsHeadingRow.setWidthFull();
        editionsHeadingRow.setAlignItems(FlexComponent.Alignment.CENTER);
        editionsHeadingRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        editionsListContainer.setWidthFull();
        editionsListContainer.setPadding(false);
        editionsListContainer.addClassNames(Gap.SMALL);

        VerticalLayout editionsPanel = new VerticalLayout(editionsHeadingRow, editionsListContainer);
        editionsPanel.setPadding(false);
        editionsPanel.addClassNames(Gap.SMALL, Padding.Top.SMALL);

        // ── tabs: Event | Editions ──
        tabSheet.setWidthFull();
        tabSheet.add("Event", eventPanel);
        editionsTab = tabSheet.add("Editions", editionsPanel);
        // Editions only make sense once the festival exists — hidden until the first save in
        // create mode, already visible when opened in edit mode.
        editionsTab.setVisible(editMode);

        VerticalLayout root = new VerticalLayout(header, new Hr(), tabSheet);
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
        if (festival.getDestinationId() != null) {
            fldDestination.getListDataView().getItems()
                    .filter(d -> d.getId().equals(festival.getDestinationId()))
                    .findFirst().ifPresent(fldDestination::setValue);
        }
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

            if (onSaved != null) onSaved.accept(saved);

            if (!editMode) {
                // Newly created — flip this dialog into edit mode in place so editions can be
                // added right away, instead of closing and forcing a reopen.
                editMode = true;
                dialogTitle.setText("Edit Event");
                btnSaveFestival.setText("Save Changes");
                editionsTab.setVisible(true);
                refreshEditionsList();
                tabSheet.setSelectedTab(editionsTab);
                Notification.show("Event created — add its edition(s) in the Editions tab.",
                        4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                close();
                Notification.show("Event updated.", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
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
        dto.setDestinationId(fldDestination.getValue() != null ? fldDestination.getValue().getId() : null);
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
            Span empty = new Span("No editions yet — add one above.");
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
        btnEdit.addClickListener(e -> openEditEditionDialog(edition));

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

    private void openCreateEditionDialog() {
        if (festivalId == null) {
            Notification.show("Save the festival first.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        new EditionDialog(festivalId, festivalService, saved -> refreshEditionsList()).open();
    }

    private void openEditEditionDialog(FestivalEditionDto edition) {
        new EditionDialog(edition, festivalId, festivalService, saved -> refreshEditionsList()).open();
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
            refreshEditionsList();
            Notification.show("Edition deleted.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            log.error("Failed to delete edition", ex);
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private static String destinationLabel(DestinationDto d) {
        return d.getCityName() + " (" + d.getCountry() + ")";
    }

    private static String nvl(String s)        { return s == null ? "" : s; }
    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
}
