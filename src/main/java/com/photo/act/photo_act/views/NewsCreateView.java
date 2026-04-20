package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.dto.NewsCategoryDto;
import com.photo.act.photo_act.dto.NewsCreateDto;
import com.photo.act.photo_act.dto.NewsDto;
import com.photo.act.photo_act.services.NewsService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.views.components.NewsPhotoUpload;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Route(value = "news/create", layout = MainLayout.class)
@PageTitle("Write News · PhotoAct")
@PermitAll
public class NewsCreateView extends VerticalLayout implements BeforeEnterObserver {

    private final NewsService   newsService;
    private final RecordService recordService;

    private String hostname  = "localhost";
    private String publicIp  = "unknown";
    private String sessionId = "";

    // Main form fields
    private final TextField fTitle          = new TextField("Title");
    private final Select<NewsCategoryDto> fCategory = new Select<>();
    private final TextField fOriginalAuthor = new TextField("Original Author / Source");
    private final TextField fOriginalUrl    = new TextField("Original Source URL");
    private final TextArea  fDescription    = new TextArea("Description");

    private NewsPhotoUpload coverPhotoUpload;
    private Integer         coverPhotoId;

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
        resolveNetInfo();
        buildUi();
    }

    private void resolveNetInfo() {
        try { hostname = InetAddress.getLocalHost().getHostName(); } catch (Exception ignored) {}
        try { publicIp = new NetUtils().getClientPublicIp(hostname); } catch (Exception ignored) {}
        try { sessionId = VaadinSession.getCurrent().getSession().getId(); } catch (Exception ignored) {}
    }

    private void buildUi() {
        removeAll();
        itemRows.clear();
        itemsContainer.removeAll();
        itemCounter = 1;

        Integer userId = resolveUserId();
        coverPhotoUpload = userId != null
                ? new NewsPhotoUpload(recordService, userId, resolveUsername(),
                        hostname, publicIp, sessionId, coverPhotoId, id -> coverPhotoId = id)
                : null;

        add(buildHeader(), buildForm(), buildItemsSection(), buildActions());
    }

    // ─────────────────────────── Header ──────────────────────────────────────

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

    // ─────────────────────────── Main form ───────────────────────────────────

    private Component buildForm() {
        Div section = new Div();
        section.addClassName("ncv-section");

        H3 sectionTitle = new H3("News Details");
        sectionTitle.addClassName("ncv-section-title");
        section.add(sectionTitle);

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

        fOriginalAuthor.setPlaceholder("Author name or news agency");
        fOriginalAuthor.setWidthFull();

        fOriginalUrl.setPlaceholder("https://source-site.com/article…");
        fOriginalUrl.setWidthFull();

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
        form.add(fTitle);
        form.setColspan(fTitle, 2);
        form.add(fCategory, fOriginalAuthor);
        form.add(fOriginalUrl);
        form.setColspan(fOriginalUrl, 2);
        form.add(fDescription);
        form.setColspan(fDescription, 2);
        section.add(form);

        if (coverPhotoUpload != null) {
            H4 photoLabel = new H4("Cover Photo");
            photoLabel.addClassName("ncv-sub-label");
            section.add(photoLabel, coverPhotoUpload);
        }

        return section;
    }

    // ──────────────────────── News items section ──────────────────────────────

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
        Integer userId = resolveUserId();
        NewsPhotoUpload itemPhoto = userId != null
                ? new NewsPhotoUpload(recordService, userId, resolveUsername(),
                        hostname, publicIp, sessionId, null, id -> {})
                : null;
        ItemRow row = new ItemRow(itemCounter++, itemPhoto, this::removeItemRow);
        itemRows.add(row);
        itemsContainer.add(row);
    }

    private void removeItemRow(ItemRow row) {
        itemRows.remove(row);
        itemsContainer.remove(row);
    }

    // ─────────────────────────── Actions bar ─────────────────────────────────

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

    // ─────────────────────────── Submit logic ────────────────────────────────

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
        dto.setOriginalUrl(fOriginalUrl.getValue().trim());
        dto.setPhotoId(coverPhotoId);
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

    // ─────────────────────────── Auth helpers ────────────────────────────────

    private Integer resolveUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth instanceof AnonymousAuthenticationToken) return null;
            String uname = auth.getName();
            var records = recordService.findAll(
                    "SELECT userId FROM dbuser WHERE username = '" + uname + "'",
                    new String[]{"userId"});
            if (records.isEmpty()) return null;
            String id = records.get(0).getColumnData("userId");
            return id != null ? Integer.parseInt(id) : null;
        } catch (Exception e) { return null; }
    }

    private String resolveUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "unknown";
        } catch (Exception e) { return "unknown"; }
    }

    // ─────────────────────────── Notifications ───────────────────────────────

    private void showError(String msg) {
        Notification n = Notification.show(msg, 4000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String msg) {
        Notification n = Notification.show(msg, 3000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    // ═══════════════════════ Inner class — one news item row ══════════════════

    static class ItemRow extends Div {

        private final TextField fItemTitle = new TextField("Item Title");
        private final TextArea  fItemDesc  = new TextArea("Description");
        private final TextField fVideo     = new TextField("YouTube Video URL / ID");
        private final TextField fUrl1      = new TextField("URL 1");
        private final TextField fUrl2      = new TextField("URL 2");
        private final TextField fUrl3      = new TextField("URL 3");
        private final TextField fUrl4      = new TextField("URL 4");
        private final NewsPhotoUpload photoUpload;

        ItemRow(int index, NewsPhotoUpload photoUpload,
                java.util.function.Consumer<ItemRow> onRemove) {
            this.photoUpload = photoUpload;
            addClassName("ncv-item-row");

            Div rowHeader = new Div();
            rowHeader.addClassName("ncv-item-header");
            Span label = new Span("Item " + index);
            label.addClassName("ncv-item-label");
            Button btnRemove = new Button(VaadinIcon.CLOSE.create());
            btnRemove.addClassName("ncv-item-remove");
            btnRemove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            btnRemove.addClickListener(e -> onRemove.accept(this));
            rowHeader.add(label, btnRemove);

            fItemTitle.setPlaceholder("Optional item title");      fItemTitle.setWidthFull();
            fVideo.setPlaceholder("https://youtube.com/watch?v=… or video ID"); fVideo.setWidthFull();
            fItemDesc.setPlaceholder("Item description / body text…");
            fItemDesc.setMinRows(3); fItemDesc.setWidthFull();
            fUrl1.setPlaceholder("https://…"); fUrl1.setWidthFull();
            fUrl2.setPlaceholder("https://…"); fUrl2.setWidthFull();
            fUrl3.setPlaceholder("https://…"); fUrl3.setWidthFull();
            fUrl4.setPlaceholder("https://…"); fUrl4.setWidthFull();

            FormLayout form = new FormLayout();
            form.addClassName("ncv-item-form");
            form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("480px", 2),
                new FormLayout.ResponsiveStep("800px", 3)
            );
            form.add(fItemTitle, fVideo);
            form.add(fItemDesc);
            form.setColspan(fItemDesc, 3);
            add(rowHeader, form);

            if (photoUpload != null) {
                Div photoWrap = new Div();
                photoWrap.addClassName("ncv-item-photo-wrap");
                H5 photoLabel = new H5("Item Photo");
                photoLabel.addClassName("ncv-item-urls-label");
                photoWrap.add(photoLabel, photoUpload);
                add(photoWrap);
            }

            FormLayout urlForm = new FormLayout();
            urlForm.addClassName("ncv-item-urls-form");
            urlForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("480px", 2)
            );
            H5 urlsLabel = new H5("Additional Links");
            urlsLabel.addClassName("ncv-item-urls-label");
            urlForm.add(urlsLabel);
            urlForm.setColspan(urlsLabel, 2);
            urlForm.add(fUrl1, fUrl2, fUrl3, fUrl4);
            add(urlForm);
        }

        NewsCreateDto.NewsItemCreateDto toDto(int sortOrder) {
            NewsCreateDto.NewsItemCreateDto d = new NewsCreateDto.NewsItemCreateDto();
            d.setTitle(fItemTitle.getValue());
            d.setDescription(fItemDesc.getValue());
            d.setPhotoId(photoUpload != null ? photoUpload.getCurrentPhotoId() : null);
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
