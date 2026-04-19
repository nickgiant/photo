package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.dto.NewsCategoryDto;
import com.photo.act.photo_act.dto.NewsCreateDto;
import com.photo.act.photo_act.dto.NewsDto;
import com.photo.act.photo_act.services.NewsService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@Route(value = "news/create", layout = MainLayout.class)
@PageTitle("Write News · PhotoAct")
@PermitAll
public class NewsCreateView extends VerticalLayout implements BeforeEnterObserver {

    private final NewsService  newsService;
    private final RecordService recordService;

    // Main form fields
    private final TextField   fTitle          = new TextField("Title");
    private final Select<NewsCategoryDto> fCategory = new Select<>();
    private final TextField   fOriginalAuthor = new TextField("Original Author / Source");
    private final IntegerField fPhotoId       = new IntegerField("Cover Photo ID");
    private final TextArea    fDescription    = new TextArea("Description");

    // Items list
    private final VerticalLayout itemsContainer = new VerticalLayout();
    private final List<ItemRow>  itemRows       = new ArrayList<>();
    private int itemCounter = 1;

    public NewsCreateView(NewsService newsService, RecordService recordService) {
        this.newsService   = newsService;
        this.recordService = recordService;
        addClassName("ncv");
        setPadding(false);
        setSpacing(false);
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        buildUi();
    }

    private void buildUi() {
        removeAll();
        add(buildHeader(), buildForm(), buildItemsSection(), buildActions());
    }

    // ─────────────────────────── Header ─────────────────────────────────────

    private Component buildHeader() {
        Div header = new Div();
        header.addClassName("ncv-header");

        Button back = new Button(VaadinIcon.ARROW_LEFT.create());
        back.addClassName("ncv-back-btn");
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.addClickListener(e -> UI.getCurrent().navigate(NewsView.class));

        Div left = new Div();
        left.addClassName("ncv-header-left");
        Span ico = new Span(FontAwesome.Solid.PEN_TO_SQUARE.create());
        ico.addClassName("ncv-header-icon");
        H2 title = new H2("Write News");
        title.addClassName("ncv-header-title");
        left.add(ico, title);

        header.add(back, left);
        return header;
    }

    // ─────────────────────────── Main form ──────────────────────────────────

    private Component buildForm() {
        Div section = new Div();
        section.addClassName("ncv-section");

        H3 sectionTitle = new H3("News Details");
        sectionTitle.addClassName("ncv-section-title");
        section.add(sectionTitle);

        // Configure fields
        fTitle.setRequired(true);
        fTitle.setPlaceholder("Enter a compelling news title…");
        fTitle.addClassName("ncv-field-title");
        fTitle.setWidthFull();

        List<NewsCategoryDto> cats;
        try { cats = newsService.getAllCategories(); }
        catch (Exception e) { cats = List.of(); }

        fCategory.setLabel("Category");
        fCategory.setItems(cats);
        fCategory.setItemLabelGenerator(NewsCategoryDto::getTitle);
        fCategory.setPlaceholder("Select category");
        fCategory.setWidthFull();

        fOriginalAuthor.setPlaceholder("Original author or news source");
        fOriginalAuthor.setWidthFull();

        fPhotoId.setPlaceholder("Photo ID (optional)");
        fPhotoId.setMin(0);
        fPhotoId.setWidthFull();

        fDescription.setPlaceholder("Write a short summary or introduction…");
        fDescription.setMinRows(4);
        fDescription.setMaxRows(10);
        fDescription.setWidthFull();

        FormLayout form = new FormLayout();
        form.addClassName("ncv-form");
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0",    1),
            new FormLayout.ResponsiveStep("600px", 2)
        );
        form.add(fTitle, fCategory, fOriginalAuthor, fPhotoId);
        form.setColspan(fTitle, 2);
        form.add(fDescription);
        form.setColspan(fDescription, 2);

        section.add(form);
        return section;
    }

    // ─────────────────────── News items section ──────────────────────────────

    private Component buildItemsSection() {
        Div section = new Div();
        section.addClassName("ncv-section");

        Div itemsHeader = new Div();
        itemsHeader.addClassName("ncv-items-header");

        H3 title = new H3("News Items");
        title.addClassName("ncv-section-title");

        Paragraph hint = new Paragraph("Add one or more items — each can have its own media and links.");
        hint.addClassName("ncv-section-hint");

        Button btnAdd = new Button("Add Item", VaadinIcon.PLUS_CIRCLE.create());
        btnAdd.addClassNames("ncv-btn-add-item");
        btnAdd.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnAdd.addClickListener(e -> addItemRow());

        itemsHeader.add(title, hint, btnAdd);

        itemsContainer.addClassName("ncv-items-list");
        itemsContainer.setPadding(false);
        itemsContainer.setSpacing(false);

        section.add(itemsHeader, itemsContainer);
        return section;
    }

    private void addItemRow() {
        ItemRow row = new ItemRow(itemCounter++, this::removeItemRow);
        itemRows.add(row);
        itemsContainer.add(row);
    }

    private void removeItemRow(ItemRow row) {
        itemRows.remove(row);
        itemsContainer.remove(row);
    }

    // ─────────────────────────── Actions bar ────────────────────────────────

    private Component buildActions() {
        Div bar = new Div();
        bar.addClassName("ncv-actions");

        Button cancel = new Button("Cancel");
        cancel.addClassName("ncv-btn-cancel");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.addClickListener(e -> UI.getCurrent().navigate(NewsView.class));

        Button publish = new Button("Publish News", VaadinIcon.ARROW_CIRCLE_RIGHT_O.create());
        publish.addClassName("ncv-btn-publish");
        publish.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        publish.addClickListener(e -> handleSubmit());

        bar.add(cancel, publish);
        return bar;
    }

    // ─────────────────────────── Submit logic ───────────────────────────────

    private void handleSubmit() {
        if (fTitle.isEmpty()) {
            showError("Title is required.");
            fTitle.focus();
            return;
        }

        Integer userId = resolveUserId();
        if (userId == null) {
            showError("Could not resolve user identity. Please log in again.");
            return;
        }

        NewsCreateDto dto = new NewsCreateDto();
        dto.setTitle(fTitle.getValue().trim());
        dto.setDescription(fDescription.getValue().trim());
        dto.setOriginalAuthor(fOriginalAuthor.getValue().trim());
        dto.setPhotoId(fPhotoId.getValue());
        if (fCategory.getValue() != null) {
            dto.setCategoryId(fCategory.getValue().getId());
        }

        for (int i = 0; i < itemRows.size(); i++) {
            dto.getItems().add(itemRows.get(i).toDto(i));
        }

        try {
            NewsDto created = newsService.createNews(dto, userId);
            showSuccess("News published!");
            UI.getCurrent().navigate("news/" + created.getId());
        } catch (Exception e) {
            showError("Could not publish news: " + e.getMessage());
        }
    }

    private Integer resolveUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth instanceof AnonymousAuthenticationToken) return null;
            String username = auth.getName();
            String sql = "SELECT userId FROM dbuser WHERE username = '" + username + "'";
            var records = recordService.findAll(sql, new String[]{"userId"});
            if (records.isEmpty()) return null;
            String id = records.get(0).getColumnData("userId");
            return id != null ? Integer.parseInt(id) : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ─────────────────────────── Notifications ──────────────────────────────

    private void showError(String msg) {
        Notification n = Notification.show(msg, 4000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String msg) {
        Notification n = Notification.show(msg, 3000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    // ═══════════════════════ Inner — one news item row ═══════════════════════

    static class ItemRow extends Div {

        private final TextField    fItemTitle  = new TextField("Item Title");
        private final TextArea     fItemDesc   = new TextArea("Description");
        private final IntegerField fItemPhoto  = new IntegerField("Photo ID");
        private final TextField    fVideo      = new TextField("YouTube Video URL / ID");
        private final TextField    fUrl1       = new TextField("URL 1");
        private final TextField    fUrl2       = new TextField("URL 2");
        private final TextField    fUrl3       = new TextField("URL 3");
        private final TextField    fUrl4       = new TextField("URL 4");

        ItemRow(int index, java.util.function.Consumer<ItemRow> onRemove) {
            addClassName("ncv-item-row");

            // Header bar
            Div rowHeader = new Div();
            rowHeader.addClassName("ncv-item-header");

            Span label = new Span("Item " + index);
            label.addClassName("ncv-item-label");

            Button btnRemove = new Button(VaadinIcon.CLOSE.create());
            btnRemove.addClassName("ncv-item-remove");
            btnRemove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            btnRemove.addClickListener(e -> onRemove.accept(this));

            rowHeader.add(label, btnRemove);

            // Field grid
            fItemTitle.setPlaceholder("Optional item title");
            fItemTitle.setWidthFull();
            fVideo.setPlaceholder("https://youtube.com/watch?v=... or video ID");
            fVideo.setWidthFull();
            fItemPhoto.setPlaceholder("Photo ID");
            fItemPhoto.setMin(0);
            fItemPhoto.setWidthFull();
            fItemDesc.setPlaceholder("Item description / body text…");
            fItemDesc.setMinRows(3);
            fItemDesc.setWidthFull();
            fUrl1.setPlaceholder("https://…");
            fUrl1.setWidthFull();
            fUrl2.setPlaceholder("https://…");
            fUrl2.setWidthFull();
            fUrl3.setPlaceholder("https://…");
            fUrl3.setWidthFull();
            fUrl4.setPlaceholder("https://…");
            fUrl4.setWidthFull();

            FormLayout form = new FormLayout();
            form.addClassName("ncv-item-form");
            form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("480px", 2),
                new FormLayout.ResponsiveStep("800px", 3)
            );

            form.add(fItemTitle, fVideo, fItemPhoto);
            form.add(fItemDesc);
            form.setColspan(fItemDesc, 3);

            H5 urlsLabel = new H5("Additional Links");
            urlsLabel.addClassName("ncv-item-urls-label");
            form.add(urlsLabel);
            form.setColspan(urlsLabel, 3);

            form.add(fUrl1, fUrl2, fUrl3);
            form.add(fUrl4);

            add(rowHeader, form);
        }

        NewsCreateDto.NewsItemCreateDto toDto(int sortOrder) {
            NewsCreateDto.NewsItemCreateDto d = new NewsCreateDto.NewsItemCreateDto();
            d.setTitle(fItemTitle.getValue());
            d.setDescription(fItemDesc.getValue());
            d.setPhotoId(fItemPhoto.getValue());
            d.setVideo(fVideo.getValue().trim());
            d.setUrlMore1(fUrl1.getValue().trim());
            d.setUrlMore2(fUrl2.getValue().trim());
            d.setUrlMore3(fUrl3.getValue().trim());
            d.setUrlMore4(fUrl4.getValue().trim());
            d.setSortOrder(sortOrder);
            return d;
        }
    }
}
