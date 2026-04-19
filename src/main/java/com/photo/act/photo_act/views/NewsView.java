package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.dto.NewsCategoryDto;
import com.photo.act.photo_act.dto.NewsDto;
import com.photo.act.photo_act.services.NewsService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@Route(value = "news", layout = MainLayout.class)
@PageTitle("News · PhotoAct")
@AnonymousAllowed
public class NewsView extends VerticalLayout {

    private static final int PAGE_SIZE = 12;

    private final NewsService newsService;

    private Long selectedCategoryId = null;
    private int  currentPage        = 0;

    private final Div   categoryStrip  = new Div();
    private final Div   newsFeed       = new Div();
    private final Div   paginationRow  = new Div();
    private final List<Div> chipDivs   = new ArrayList<>();

    public NewsView(NewsService newsService) {
        this.newsService = newsService;
        addClassName("news-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();

        add(buildPageHeader(), buildCategoryStrip(), newsFeed, paginationRow);

        loadCategories();
        loadNews();
    }

    // ─────────────────────────── Page header ────────────────────────────────

    private Component buildPageHeader() {
        Div header = new Div();
        header.addClassName("news-page-header");

        Div left = new Div();
        left.addClassName("news-page-header-left");

        Span icon = new Span();
        icon.addClassName("news-page-icon");
        icon.add(FontAwesome.Solid.NEWSPAPER.create());

        H2 title = new H2("News");
        title.addClassName("news-page-title");

        left.add(icon, title);

        Button btnCreate = new Button("Write News", VaadinIcon.PLUS.create());
        btnCreate.addClassName("news-btn-create");
        btnCreate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCreate.addClickListener(e -> UI.getCurrent().navigate("news/create"));

        boolean loggedIn = isLoggedIn();
        btnCreate.setVisible(loggedIn);

        header.add(left, btnCreate);
        return header;
    }

    // ─────────────────────── Category strip ─────────────────────────────────

    private Component buildCategoryStrip() {
        Div wrapper = new Div();
        wrapper.addClassName("news-category-strip-wrapper");
        categoryStrip.addClassName("news-category-strip");
        wrapper.add(categoryStrip);
        return wrapper;
    }

    private void loadCategories() {
        categoryStrip.removeAll();
        chipDivs.clear();

        // "All" chip
        Div allChip = buildAllChip();
        chipDivs.add(allChip);
        categoryStrip.add(allChip);

        List<NewsCategoryDto> cats;
        try {
            cats = newsService.getAllCategories();
        } catch (Exception e) {
            cats = List.of();
        }

        for (NewsCategoryDto cat : cats) {
            Div chip = buildCategoryChip(cat);
            chipDivs.add(chip);
            categoryStrip.add(chip);
        }

        markActiveChip(null);
    }

    private Div buildAllChip() {
        Div chip = new Div();
        chip.addClassName("news-cat-chip");
        chip.addClassName("news-cat-chip--all");
        chip.getElement().setAttribute("data-cat-id", "all");

        Div label = new Div();
        label.addClassName("news-cat-chip-label");

        Span iconWrap = new Span();
        iconWrap.addClassName("news-cat-chip-icon");
        iconWrap.add(FontAwesome.Solid.LAYER_GROUP.create());

        Span name = new Span("All News");
        name.addClassName("news-cat-chip-name");

        label.add(iconWrap, name);
        chip.add(label);

        chip.addClickListener(e -> {
            selectedCategoryId = null;
            currentPage = 0;
            markActiveChip(null);
            loadNews();
        });
        return chip;
    }

    private Div buildCategoryChip(NewsCategoryDto cat) {
        Div chip = new Div();
        chip.addClassName("news-cat-chip");
        chip.getElement().setAttribute("data-cat-id", String.valueOf(cat.getId()));

        // Header row: icon + title
        Div header = new Div();
        header.addClassName("news-cat-chip-header");

        Span iconWrap = new Span();
        iconWrap.addClassName("news-cat-chip-icon");
        iconWrap.add(FontAwesome.Solid.TAG.create());

        Span name = new Span(cat.getTitle());
        name.addClassName("news-cat-chip-name");

        header.add(iconWrap, name);
        chip.add(header);

        // Stat cards row
        Div statsRow = new Div();
        statsRow.addClassName("news-cat-stats-row");
        statsRow.add(
            buildStatCard(cat.getNewsCount(),  "News",    "news-stat--purple", VaadinIcon.FILE_TEXT),
            buildStatCard(cat.getTotalViews(), "Views",   "news-stat--blue",   VaadinIcon.EYE),
            buildStatCard(cat.getTotalLikes(), "Likes",   "news-stat--pink",   VaadinIcon.HEART),
            buildStatCard(cat.getTotalAuthors(),"Authors","news-stat--orange",  VaadinIcon.USER)
        );
        chip.add(statsRow);

        // Last update label
        if (cat.getTimeSinceLastNews() != null && !cat.getTimeSinceLastNews().equals("—")) {
            Div lastUpdate = new Div("Updated " + cat.getTimeSinceLastNews());
            lastUpdate.addClassName("news-cat-last-update");
            chip.add(lastUpdate);
        }

        // Description popover
        if (cat.getDescription() != null && !cat.getDescription().isBlank()) {
            Popover pop = new Popover();
            pop.setOpenOnClick(false);
            pop.setOpenOnHover(true);
            pop.setHoverDelay(400);
            pop.setHideDelay(120);
            pop.setWidth("260px");
            pop.addThemeVariants(PopoverVariant.ARROW);
            pop.setPosition(PopoverPosition.BOTTOM);

            VerticalLayout popContent = new VerticalLayout();
            popContent.addClassName("news-cat-popover-content");
            popContent.setSpacing(false);
            popContent.setPadding(false);

            H4 popTitle = new H4(cat.getTitle());
            popTitle.addClassName("news-cat-popover-title");
            Paragraph popDesc = new Paragraph(cat.getDescription());
            popDesc.addClassName("news-cat-popover-desc");

            popContent.add(popTitle, popDesc);
            pop.add(popContent);
            pop.setTarget(chip);
            chip.add(pop);
        }

        chip.addClickListener(e -> {
            selectedCategoryId = cat.getId();
            currentPage = 0;
            markActiveChip(cat.getId());
            loadNews();
        });

        return chip;
    }

    /** A single colored stat tile matching the screenshot design. */
    private Div buildStatCard(long value, String label, String colorClass, VaadinIcon icon) {
        Div card = new Div();
        card.addClassName("news-stat-card");
        card.addClassName(colorClass);

        Span iconSpan = new Span();
        iconSpan.addClassName("news-stat-icon");
        iconSpan.add(icon.create());

        Div textCol = new Div();
        textCol.addClassName("news-stat-text");

        Span num = new Span(formatCount(value));
        num.addClassName("news-stat-value");

        Span lbl = new Span(label);
        lbl.addClassName("news-stat-label");

        textCol.add(num, lbl);
        card.add(iconSpan, textCol);
        return card;
    }

    private void markActiveChip(Long categoryId) {
        chipDivs.forEach(chip -> {
            chip.removeClassName("news-cat-chip--active");
            String dataCatId = chip.getElement().getAttribute("data-cat-id");
            boolean isActive = categoryId == null
                    ? "all".equals(dataCatId)
                    : String.valueOf(categoryId).equals(dataCatId);
            if (isActive) chip.addClassName("news-cat-chip--active");
        });
    }

    // ──────────────────────────── News feed ─────────────────────────────────

    private void loadNews() {
        newsFeed.removeAll();
        paginationRow.removeAll();
        newsFeed.addClassName("news-feed");

        Page<NewsDto> page;
        try {
            page = selectedCategoryId == null
                    ? newsService.getLatestNews(currentPage, PAGE_SIZE)
                    : newsService.getNewsByCategory(selectedCategoryId, currentPage, PAGE_SIZE);
        } catch (Exception e) {
            newsFeed.add(new Paragraph("Could not load news."));
            return;
        }

        if (page.isEmpty()) {
            Div empty = new Div();
            empty.addClassName("news-empty");
            empty.add(new Span("No news in this category yet. Be the first to write one!"));
            newsFeed.add(empty);
            return;
        }

        Div grid = new Div();
        grid.addClassName("news-grid");
        for (NewsDto news : page.getContent()) {
            grid.add(buildNewsCard(news));
        }
        newsFeed.add(grid);

        if (page.getTotalPages() > 1) {
            buildPagination(page.getTotalPages());
        }
    }

    private Div buildNewsCard(NewsDto news) {
        Div card = new Div();
        card.addClassName("news-card");
        card.addClickListener(e ->
            UI.getCurrent().navigate("news/" + news.getId()));

        // Category badge
        if (news.getCategoryTitle() != null) {
            Span badge = new Span(news.getCategoryTitle());
            badge.addClassName("news-card-badge");
            card.add(badge);
        }

        // Title
        H3 title = new H3(news.getTitle());
        title.addClassName("news-card-title");
        card.add(title);

        // Description excerpt
        if (news.getDescription() != null && !news.getDescription().isBlank()) {
            String excerpt = news.getDescription().length() > 120
                    ? news.getDescription().substring(0, 120) + "…"
                    : news.getDescription();
            Paragraph desc = new Paragraph(excerpt);
            desc.addClassName("news-card-desc");
            card.add(desc);
        }

        // Footer: author + date + stats
        Div footer = new Div();
        footer.addClassName("news-card-footer");

        Div authorLine = new Div();
        authorLine.addClassName("news-card-author");
        String author = news.getOriginalAuthor() != null && !news.getOriginalAuthor().isBlank()
                ? news.getOriginalAuthor()
                : "User #" + news.getUserId();
        authorLine.add(new Span(author));

        Div statsLine = new Div();
        statsLine.addClassName("news-card-stats");

        Span viewsStat = new Span();
        viewsStat.addClassName("news-card-stat");
        viewsStat.add(VaadinIcon.EYE.create(), new Span(formatCount(news.getViewCount())));

        Span likesStat = new Span();
        likesStat.addClassName("news-card-stat");
        likesStat.add(VaadinIcon.HEART.create(), new Span(formatCount(news.getLikeCount())));

        statsLine.add(viewsStat, likesStat);
        footer.add(authorLine, statsLine);
        card.add(footer);

        return card;
    }

    private void buildPagination(int totalPages) {
        paginationRow.addClassName("news-pagination");

        Button prev = new Button(new Icon(VaadinIcon.ANGLE_LEFT));
        prev.addClassName("news-page-btn");
        prev.setEnabled(currentPage > 0);
        prev.addClickListener(e -> { currentPage--; loadNews(); });

        Span pageInfo = new Span((currentPage + 1) + " / " + totalPages);
        pageInfo.addClassName("news-page-info");

        Button next = new Button(new Icon(VaadinIcon.ANGLE_RIGHT));
        next.addClassName("news-page-btn");
        next.setEnabled(currentPage < totalPages - 1);
        next.addClickListener(e -> { currentPage++; loadNews(); });

        paginationRow.add(prev, pageInfo, next);
    }

    // ─────────────────────────────── Util ───────────────────────────────────

    private String formatCount(long value) {
        if (value >= 1_000_000) return (value / 1_000_000) + "M+";
        if (value >= 1_000)     return (value / 1_000) + "K+";
        return value + (value > 0 ? "+" : "");
    }

    private boolean isLoggedIn() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal());
        } catch (Exception e) {
            return false;
        }
    }
}
