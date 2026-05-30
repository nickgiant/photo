package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.dto.NewsCategoryDto;
import com.photo.act.photo_act.dto.NewsDto;
import com.photo.act.photo_act.dto.NewsPageResult;
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

import java.util.ArrayList;
import java.util.List;

@Route(value = "news", layout = MainLayout.class)
@PageTitle("News · PhotoAct")
@AnonymousAllowed
public class NewsView extends VerticalLayout {

    /* Gradient palette — each category cycles through these */
    private static final String[] CAT_GRADIENTS = {
        "news-cat--grape", "news-cat--ocean", "news-cat--teal",
        "news-cat--rose",  "news-cat--amber", "news-cat--forest",
        "news-cat--indigo","news-cat--coral"
    };

    private static final int PAGE_SIZE = 12;

    private final NewsService newsService;

    private Long selectedCategoryId   = null;
    private NewsCategoryDto selectedCat = null;
    private int  currentPage          = 0;

    private final Div statBar      = new Div();
    private final Div catStrip     = new Div();
    private final Div newsFeed     = new Div();
    private final Div pagination   = new Div();
    private final List<Div> chips  = new ArrayList<>();
    private List<NewsCategoryDto> allCategories = List.of();

    public NewsView(NewsService newsService) {
        this.newsService = newsService;
        addClassName("news-view");
        setPadding(true);
        setSpacing(false);
        setSizeFull();

        add(buildPageHeader(), buildCategoryStrip(), newsFeed, pagination);

        loadAll();
    }

    // ─────────────────────────── Page header ────────────────────────────────

    private Component buildPageHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.addClassName("header-layout");

        HorizontalLayout left = new HorizontalLayout();
//        left.addClassName("nv-header-left");
        Span ico = new Span(FontAwesome.Solid.NEWSPAPER.create());
//        ico.addClassName("nv-header-icon");
        H1 title = new H1("News");
//        title.addClassName("nv-header-title");
        left.add(ico, title);

        Button btnCreate = new Button("Write News", VaadinIcon.PLUS.create());
        btnCreate.addClassNames("nv-btn-create");
        btnCreate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCreate.addClickListener(e -> UI.getCurrent().navigate("news/create"));
        btnCreate.setVisible(isLoggedIn());

        header.add(left, btnCreate);
        return header;
    }



    // ─────────────────────── Category strip ─────────────────────────────────

    private Component buildCategoryStrip() {
        Div wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.addClassName("nv-cat-wrapper");
        catStrip.setWidthFull();
        catStrip.addClassName("nv-cat-strip");
        wrapper.add(catStrip);
        return wrapper;
    }

    private void refreshCategoryStrip() {
        catStrip.removeAll();
        chips.clear();

        // "All" chip
        Div allChip = buildChip(null, "All", null, "nv-cat--all");
        chips.add(allChip);
        catStrip.add(allChip);

        int i = 0;
        for (NewsCategoryDto cat : allCategories) {
            String gradClass = CAT_GRADIENTS[i % CAT_GRADIENTS.length];
            Div chip = buildChip(cat.getId(), cat.getTitle(), cat, gradClass);
            chips.add(chip);
            catStrip.add(chip);
            i++;
        }
        markActive(selectedCategoryId);
    }

    private Div buildChip(Long catId, String name, NewsCategoryDto cat, String gradClass) {
        Div chip = new Div();
        chip.addClassNames("nv-chip", gradClass);
        chip.getElement().setAttribute("data-id", catId == null ? "all" : String.valueOf(catId));

        Span chipName = new Span(name);
        chipName.addClassName("nv-chip-name");
        chip.add(chipName);

        if (cat != null && cat.getNewsCount() > 0) {
            Span badge = new Span(fmtCount(cat.getNewsCount()));
            badge.addClassName("nv-chip-badge");
            chip.add(badge);
        }

        if (cat != null && cat.getDescription() != null && !cat.getDescription().isBlank()) {
            Popover pop = buildChipPopover(cat);
            pop.setTarget(chip);
            chip.add(pop);
        }

        chip.addClickListener(e -> {
            selectedCategoryId = catId;
            selectedCat        = cat;
            currentPage        = 0;
            markActive(catId);

            loadNews();
        });
        return chip;
    }

    private Popover buildChipPopover(NewsCategoryDto cat) {
        Popover pop = new Popover();
        pop.setOpenOnClick(false);
        pop.setOpenOnHover(true);
        pop.setHoverDelay(350);
        pop.setHideDelay(100);
        pop.setWidth("240px");
        pop.addThemeVariants(PopoverVariant.ARROW);
        pop.setPosition(PopoverPosition.BOTTOM);

        VerticalLayout body = new VerticalLayout();
        body.setSpacing(false);
        body.setPadding(false);
        body.addClassName("nv-chip-popover");

        H4 pt = new H4(cat.getTitle());
        pt.addClassName("nv-chip-popover-title");
        Paragraph pd = new Paragraph(cat.getDescription());
        pd.addClassName("nv-chip-popover-desc");

        Div stats = new Div();
        stats.addClassName("nv-chip-popover-stats");
        stats.add(popStat(VaadinIcon.FILE_TEXT, fmtCount(cat.getNewsCount()), "news"),
                  popStat(VaadinIcon.EYE,       fmtCount(cat.getTotalViews()),  "views"),
                  popStat(VaadinIcon.HEART,      fmtCount(cat.getTotalLikes()),  "likes"),
                  popStat(VaadinIcon.USERS,      fmtCount(cat.getTotalAuthors()),"authors"));

        body.add(pt, pd, stats);
        pop.add(body);
        return pop;
    }

    private Span popStat(VaadinIcon icon, String value, String label) {
        Span s = new Span();
        s.addClassName("nv-chip-popover-stat");
        s.add(icon.create(), new Span(value + " " + label));
        return s;
    }

    private void markActive(Long catId) {
        chips.forEach(chip -> {
            chip.removeClassName("nv-chip--active");
            String dataId = chip.getElement().getAttribute("data-id");
            boolean active = catId == null
                    ? "all".equals(dataId)
                    : String.valueOf(catId).equals(dataId);
            if (active) chip.addClassName("nv-chip--active");
        });
    }

    // ──────────────────────────── News feed ─────────────────────────────────

    private void loadNews() {
        newsFeed.removeAll();
        pagination.removeAll();
        newsFeed.addClassName("nv-feed");

        NewsPageResult page;
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
            empty.addClassName("nv-empty");
            empty.add(FontAwesome.Regular.NEWSPAPER.create());
            empty.add(new Span("No news yet — be the first to write one!"));
            newsFeed.add(empty);
        } else {
            Div grid = new Div();
            grid.addClassName("nv-grid");
            page.getContent().forEach(n -> grid.add(buildNewsCard(n)));
            newsFeed.add(grid);

            if (page.getTotalPages() > 1) buildPagination(page.getTotalPages());
        }
    }

    private Div buildNewsCard(NewsDto news) {
        Div card = new Div();
        card.addClassName("nv-card");
        card.addClickListener(e -> UI.getCurrent().navigate("news/" + news.getId()));

        if (news.getCategoryTitle() != null) {
            Span badge = new Span(news.getCategoryTitle());
            badge.addClassName("nv-card-badge");
            card.add(badge);
        }

        H3 title = new H3(news.getTitle());
        title.addClassName("nv-card-title");
        card.add(title);

        if (news.getDescription() != null && !news.getDescription().isBlank()) {
            String ex = news.getDescription().length() > 120
                    ? news.getDescription().substring(0, 120) + "…"
                    : news.getDescription();
            Paragraph desc = new Paragraph(ex);
            desc.addClassName("nv-card-desc");
            card.add(desc);
        }

        Div footer = new Div();
        footer.addClassName("nv-card-footer");

        String author = news.getOriginalAuthor() != null && !news.getOriginalAuthor().isBlank()
                ? news.getOriginalAuthor() : "Member #" + news.getUserId();
        Span authorSpan = new Span(author);
        authorSpan.addClassName("nv-card-author");

        Div statsRow = new Div();
        statsRow.addClassName("nv-card-stats");
        statsRow.add(cardStat(VaadinIcon.EYE,   fmtCount(news.getViewCount())),
                     cardStat(VaadinIcon.HEART,  fmtCount(news.getLikeCount())));

        footer.add(authorSpan, statsRow);
        card.add(footer);
        return card;
    }

    private Span cardStat(VaadinIcon icon, String value) {
        Span s = new Span();
        s.addClassName("nv-card-stat");
        s.add(icon.create(), new Span(value));
        return s;
    }

    private void buildPagination(int totalPages) {
        pagination.addClassName("nv-pagination");

        Button prev = new Button(new Icon(VaadinIcon.ANGLE_LEFT));
        prev.addClassName("nv-page-btn");
        prev.setEnabled(currentPage > 0);
        prev.addClickListener(e -> { currentPage--; loadNews(); });

        Span info = new Span((currentPage + 1) + " / " + totalPages);
        info.addClassName("nv-page-info");

        Button next = new Button(new Icon(VaadinIcon.ANGLE_RIGHT));
        next.addClassName("nv-page-btn");
        next.setEnabled(currentPage < totalPages - 1);
        next.addClickListener(e -> { currentPage++; loadNews(); });

        pagination.add(prev, info, next);
    }

    // ─────────────────────────────── init ───────────────────────────────────

    private void loadAll() {
        try {
            allCategories = newsService.getAllCategories();
        } catch (Exception e) {
            allCategories = List.of();
        }
        refreshCategoryStrip();
        loadNews();
    }

    // ─────────────────────────────── util ───────────────────────────────────

    private String fmtCount(long v) {
        if (v >= 1_000_000) return (v / 1_000_000) + "M+";
        if (v >= 1_000)     return (v / 1_000) + "K+";
        return v + (v > 0 ? "+" : "");
    }

    private boolean isLoggedIn() {
        try {
            var a = org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();
            return a != null && a.isAuthenticated()
                    && !"anonymousUser".equals(a.getPrincipal());
        } catch (Exception e) { return false; }
    }
}
