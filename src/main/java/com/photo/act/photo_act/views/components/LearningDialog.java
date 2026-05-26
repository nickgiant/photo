package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.dto.LearningCategoryDto;
import com.photo.act.photo_act.dto.LearningDto;
import com.photo.act.photo_act.dto.TutorDto;
import com.photo.act.photo_act.services.LearningService;
import com.photo.act.photo_act.services.TutorService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Dialog for creating and editing a learning entry.
 *
 * Usage:
 *   new LearningDialog(learningService, tutorService, userId, saved -> refreshGrid()).open();
 *   new LearningDialog(existing, learningService, tutorService, userId, saved -> refresh()).open();
 */
public class LearningDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(LearningDialog.class);

    private static final List<String> FORMATS = List.of(
            "YouTube", "Book", "Url with Free e-book", "Article", "Course", "Podcast", "Other");

    private final LearningService      learningService;
    private final TutorService         tutorService;
    private final LearningDto          editing;
    private final Integer              currentUserId;
    private final Consumer<LearningDto> onSaved;

    private final TextField   fldTitle       = new TextField("Title");
    private final TextField   fldPicture     = new TextField("Cover Image Path");
    private final Select<String> fldFormat   = new Select<>();
    private final TextField   fldUrl         = new TextField("URL");
    private final TextField   fldDuration    = new TextField("Duration");
    private final TextField   fldPages       = new TextField("Pages");
    private final TextField   fldArtistsRef  = new TextField("Artists / References");
    private final TextArea    fldDescription = new TextArea("Description");
    private final DatePicker  fldPublished   = new DatePicker("Published Date");
    private final ComboBox<TutorDto>             fldTutor    = new ComboBox<>("Tutor");
    private final ComboBox<LearningCategoryDto>  fldCategory = new ComboBox<>("Category");
    /*private final ComboBox<LearningCategoryDto>  fldGenre    = new ComboBox<>("Genre");*/

    /** Create mode. */
    public LearningDialog(LearningService learningService, TutorService tutorService,
                          Integer currentUserId, Consumer<LearningDto> onSaved) {
        this(null, learningService, tutorService, currentUserId, onSaved);
    }

    /** Edit mode — pre-fills form from existing dto. */
    public LearningDialog(LearningDto editing,
                          LearningService learningService, TutorService tutorService,
                          Integer currentUserId, Consumer<LearningDto> onSaved) {
        this.editing        = editing;
        this.learningService = learningService;
        this.tutorService   = tutorService;
        this.currentUserId  = currentUserId;
        this.onSaved        = onSaved;

        setDraggable(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setWidth("760px");

        add(buildLayout());
        if (editing != null) populateForm(editing);
    }

    private VerticalLayout buildLayout() {
        boolean isEdit = editing != null;

        H3 title = new H3(isEdit ? "Edit Learning" : "New Learning");
        title.addClassNames(Margin.NONE);

        Button btnClose = new Button(VaadinIcon.CLOSE.create());
        btnClose.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnClose.addClickListener(e -> close());

        HorizontalLayout header = new HorizontalLayout(title, btnClose);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, title);
        header.addClassNames(Padding.Bottom.SMALL);

        // ── Tutor combo ──
        List<TutorDto> tutors = tutorService.getAllTutors();
        fldTutor.setItems(tutors);
        fldTutor.setItemLabelGenerator(TutorDto::getTutorName);
        fldTutor.setPlaceholder("Select tutor…");
        fldTutor.setClearButtonVisible(true);

        // ── Category combos ──
        List<LearningCategoryDto> categories = learningService.getAllCategories();
        fldCategory.setItems(categories);
        fldCategory.setItemLabelGenerator(LearningCategoryDto::getCatTitle);
        fldCategory.setPlaceholder("Select category…");
        fldCategory.setClearButtonVisible(true);

/*        fldGenre.setItems(categories);
        fldGenre.setItemLabelGenerator(LearningCategoryDto::getCatTitle);
        fldGenre.setPlaceholder("Select genre…");
        fldGenre.setClearButtonVisible(true);*/

        // ── Format select ──
        fldFormat.setLabel("Format");
        fldFormat.setItems(FORMATS);
        fldFormat.setPlaceholder("Select format…");

        fldTitle.setRequired(true);
        fldTitle.setPlaceholder("e.g. Lightroom Essentials");
        fldTitle.setWidthFull();
        fldDescription.setMinHeight("100px");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("480px", 2));

        form.add(fldTitle, fldFormat,
                 fldTutor, fldCategory,
                  fldPublished,
                 fldUrl, fldPicture,
                 fldDuration, fldPages,
                 fldArtistsRef);
        form.setColspan(fldDescription, 2);
        form.add(fldDescription);

        Button btnSave = new Button(isEdit ? "Save Changes" : "Create Learning");
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

    private void populateForm(LearningDto dto) {
        fldTitle.setValue(nvl(dto.getTitle()));
        fldPicture.setValue(nvl(dto.getPicture()));
        fldUrl.setValue(nvl(dto.getUrl()));
        fldDuration.setValue(nvl(dto.getDuration()));
        fldPages.setValue(nvl(dto.getPages()));
        fldArtistsRef.setValue(nvl(dto.getArtistsRef()));
        fldDescription.setValue(nvl(dto.getDescription()));
        if (dto.getFormat() != null)    fldFormat.setValue(dto.getFormat());
        if (dto.getPublished() != null) fldPublished.setValue(dto.getPublished());
        // Select tutor by id
        if (dto.getTutorId() != null) {
            fldTutor.getListDataView().getItems()
                    .filter(t -> t.getId().equals(dto.getTutorId()))
                    .findFirst().ifPresent(fldTutor::setValue);
        }
        // Select category and genre by id
        if (dto.getCategoryId() != null) {
            fldCategory.getListDataView().getItems()
                    .filter(c -> c.getId().equals(dto.getCategoryId()))
                    .findFirst().ifPresent(fldCategory::setValue);
        }
/*        if (dto.getCatGenreId() != null) {
            fldGenre.getListDataView().getItems()
                    .filter(c -> c.getId().equals(dto.getCatGenreId()))
                    .findFirst().ifPresent(fldGenre::setValue);
        }*/
    }

    private void save() {
        if (fldTitle.getValue().isBlank()) {
            fldTitle.setInvalid(true);
            fldTitle.setErrorMessage("Title is required");
            return;
        }
        try {
            LearningDto dto = buildDto();
            LearningDto saved;
            if (editing != null) {
                saved = learningService.updateLearning(editing.getId(), dto)
                        .orElseThrow(() -> new IllegalStateException("Learning not found"));
            } else {
                saved = learningService.createLearning(dto);
            }
            close();
            if (onSaved != null) onSaved.accept(saved);
            Notification.show(editing != null ? "Learning updated." : "Learning created.",
                    3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            log.error("Failed to save learning", ex);
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private LearningDto buildDto() {
        LearningDto dto = editing != null
                ? LearningDto.builder().id(editing.getId()).build()
                : LearningDto.builder().build();
        dto.setTitle(fldTitle.getValue().trim());
        dto.setPicture(emptyToNull(fldPicture.getValue()));
        dto.setFormat(fldFormat.getValue());
        dto.setUrl(emptyToNull(fldUrl.getValue()));
        dto.setDuration(emptyToNull(fldDuration.getValue()));
        dto.setPages(emptyToNull(fldPages.getValue()));
        dto.setArtistsRef(emptyToNull(fldArtistsRef.getValue()));
        dto.setDescription(emptyToNull(fldDescription.getValue()));
        dto.setPublished(fldPublished.getValue());
        dto.setTutorId(fldTutor.getValue() != null ? fldTutor.getValue().getId() : null);
        dto.setCategoryId(fldCategory.getValue() != null ? fldCategory.getValue().getId() : null);
        /*dto.setCatGenreId(fldGenre.getValue() != null ? fldGenre.getValue().getId() : null);*/
        dto.setUserIdPost(editing != null ? editing.getUserIdPost() : currentUserId);
        return dto;
    }

    private static String nvl(String s)        { return s == null ? "" : s; }
    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
}
