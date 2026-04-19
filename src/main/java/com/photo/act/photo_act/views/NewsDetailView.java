package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.dto.NewsDto;
import com.photo.act.photo_act.dto.NewsItemDto;
import com.photo.act.photo_act.services.NewsService;
import com.photo.act.photo_act.utils.NetUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.photo.act.photo_act.views.HomeView.*;
import static com.photo.act.photo_act.views.MainLayout.PROP_PHOTOS;

@Route(value = "news/:newsId", layout = MainLayout.class)
@PageTitle("News · PhotoAct")
@AnonymousAllowed
public class NewsDetailView extends VerticalLayout implements BeforeEnterObserver {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm");

    private final NewsService   newsService;
    private final RecordService recordService;

    private String hostname  = "localhost";
    private String publicIp  = "unknown";
    private String sessionId = "";
    private String dirPhotos = "";
    private final String sep = FileSystems.getDefault().getSeparator();

    public NewsDetailView(NewsService newsService, RecordService recordService) {
        this.newsService   = newsService;
        this.recordService = recordService;
        addClassName("ndv");
        setPadding(false);
        setSpacing(false);
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String rawId = event.getRouteParameters().get("newsId").orElse(null);
        if (rawId == null) { event.forwardTo(NewsView.class); return; }
        Long newsId;
        try { newsId = Long.parseLong(rawId); }
        catch (NumberFormatException e) { event.forwardTo(NewsView.class); return; }

        resolveEnv();

        Optional<NewsDto> opt = newsService.getNewsById(newsId);
        if (opt.isEmpty()) {
            event.forwardTo(NewsView.class);
            return;
        }
        NewsDto news = opt.get();

        // Record view asynchronously
        recordView(newsId);

        // Update page title
        UI.getCurrent().getPage().setTitle(news.getTitle() + " · PhotoAct");

        removeAll();
        add(buildHeader(news), buildArticle(news));
    }

    // ─────────────────────────── Header bar ──────────────────────────────────

    private Component buildHeader(NewsDto news) {
        Div header = new Div();
        header.addClassName("ndv-header");

        Button back = new Button(VaadinIcon.ARROW_LEFT.create());
        back.addClassName("ndv-back-btn");
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.addClickListener(e -> UI.getCurrent().navigate(NewsView.class));

        if (news.getCategoryTitle() != null) {
            Span cat = new Span(news.getCategoryTitle());
            cat.addClassName("ndv-category-badge");
            header.add(back, cat);
        } else {
            header.add(back);
        }

        return header;
    }

    // ─────────────────────────── Article body ────────────────────────────────

    private Component buildArticle(NewsDto news) {
        Div article = new Div();
        article.addClassName("ndv-article");

        // Title
        H1 title = new H1(news.getTitle());
        title.addClassName("ndv-title");
        article.add(title);

        // Meta row: author · date · stats
        article.add(buildMeta(news));

        // Cover photo
        if (news.getPhotoId() != null) {
            Component coverImg = buildPhoto(news.getPhotoId(), true);
            if (coverImg != null) article.add(coverImg);
        }

        // Description
        if (hasText(news.getDescription())) {
            Div desc = new Div();
            desc.addClassName("ndv-description");
            desc.setText(news.getDescription());
            article.add(desc);
        }

        // Original source link
        if (hasText(news.getOriginalUrl())) {
            Anchor src = new Anchor(news.getOriginalUrl(), "View original source");
            src.addClassName("ndv-source-link");
            src.setTarget("_blank");
            src.getElement().setAttribute("rel", "noopener noreferrer");
            article.add(src);
        }

        // Like button
        article.add(buildLikeBar(news));

        // News items
        if (!news.getItems().isEmpty()) {
            Div itemsSection = new Div();
            itemsSection.addClassName("ndv-items-section");
            news.getItems().forEach(item -> itemsSection.add(buildItem(item)));
            article.add(itemsSection);
        }

        return article;
    }

    private Component buildMeta(NewsDto news) {
        Div meta = new Div();
        meta.addClassName("ndv-meta");

        String author = hasText(news.getOriginalAuthor())
                ? news.getOriginalAuthor() : "Member #" + news.getUserId();
        Span authorSpan = new Span(VaadinIcon.USER.create());
        authorSpan.addClassName("ndv-meta-chip");
        Span authorTxt = new Span(author);
        authorSpan.add(authorTxt);

        meta.add(authorSpan);

        if (news.getCreatedAt() != null) {
            Span dateSpan = new Span(VaadinIcon.CLOCK.create());
            dateSpan.addClassName("ndv-meta-chip");
            dateSpan.add(new Span(news.getCreatedAt().format(DATE_FMT)));
            meta.add(dateSpan);
        }

        Span views = new Span(VaadinIcon.EYE.create());
        views.addClassName("ndv-meta-chip");
        views.add(new Span(fmtCount(news.getViewCount()) + " views"));
        meta.add(views);

        Span likes = new Span(VaadinIcon.HEART.create());
        likes.addClassName("ndv-meta-chip");
        likes.add(new Span(fmtCount(news.getLikeCount()) + " likes"));
        meta.add(likes);

        return meta;
    }

    private Component buildLikeBar(NewsDto news) {
        Div bar = new Div();
        bar.addClassName("ndv-like-bar");

        boolean alreadyLiked = newsService.hasLiked(news.getId(), publicIp);

        Button likeBtn = new Button(
                alreadyLiked ? "Liked!" : "Like",
                (alreadyLiked ? VaadinIcon.HEART : VaadinIcon.HEART_O).create());
        likeBtn.addClassName("ndv-like-btn");
        if (alreadyLiked) likeBtn.addClassName("ndv-like-btn--active");
        likeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Span countSpan = new Span(fmtCount(news.getLikeCount()));
        countSpan.addClassName("ndv-like-count");

        likeBtn.addClickListener(e -> {
            if (alreadyLiked) return;
            Integer userId = resolveUserId();
            boolean added = newsService.toggleLike(
                    news.getId(), userId, publicIp, sessionId, LocalDateTime.now());
            if (added) {
                Notification n = Notification.show("Thanks for liking!", 2000,
                        Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                likeBtn.setText("Liked!");
                likeBtn.setIcon(VaadinIcon.HEART.create());
                likeBtn.addClassName("ndv-like-btn--active");
                long newCount = newsService.getLikeCount(news.getId());
                countSpan.setText(fmtCount(newCount));
            }
        });

        bar.add(likeBtn, countSpan);
        return bar;
    }

    // ─────────────────────────── News item ───────────────────────────────────

    private Component buildItem(NewsItemDto item) {
        Div card = new Div();
        card.addClassName("ndv-item");

        if (hasText(item.getTitle())) {
            H2 t = new H2(item.getTitle());
            t.addClassName("ndv-item-title");
            card.add(t);
        }

        if (item.getPhotoId() != null) {
            Component photo = buildPhoto(item.getPhotoId(), false);
            if (photo != null) card.add(photo);
        }

        if (hasText(item.getVideo())) {
            card.add(buildVideoEmbed(item.getVideo()));
        }

        if (hasText(item.getDescription())) {
            Div desc = new Div();
            desc.addClassName("ndv-item-desc");
            desc.setText(item.getDescription());
            card.add(desc);
        }

        // Additional links
        Div links = buildLinks(item);
        if (links != null) card.add(links);

        return card;
    }

    private Component buildVideoEmbed(String video) {
        String videoId = extractYouTubeId(video);
        if (videoId == null) return new Div();

        IFrame frame = new IFrame("https://www.youtube.com/embed/" + videoId);
        frame.addClassName("ndv-video");
        frame.getElement().setAttribute("frameborder", "0");
        frame.getElement().setAttribute("allowfullscreen", "true");
        frame.getElement().setAttribute("allow",
                "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture");
        Div wrap = new Div(frame);
        wrap.addClassName("ndv-video-wrap");
        return wrap;
    }

    private Div buildLinks(NewsItemDto item) {
        String[] urls = {item.getUrlMore1(), item.getUrlMore2(),
                         item.getUrlMore3(), item.getUrlMore4()};
        boolean any = false;
        for (String u : urls) if (hasText(u)) { any = true; break; }
        if (!any) return null;

        Div linksDiv = new Div();
        linksDiv.addClassName("ndv-links");
        for (int i = 0; i < urls.length; i++) {
            if (hasText(urls[i])) {
                Anchor a = new Anchor(urls[i], "Link " + (i + 1));
                a.addClassName("ndv-link");
                a.setTarget("_blank");
                a.getElement().setAttribute("rel", "noopener noreferrer");
                linksDiv.add(a);
            }
        }
        return linksDiv;
    }

    // ─────────────────────────── Photo helper ────────────────────────────────

    private Component buildPhoto(Integer photoId, boolean isCover) {
        try {
            List<Record> rows = recordService.findAll(
                    "SELECT name_new FROM photo_meta WHERE id = " + photoId,
                    new String[]{"name_new"});
            if (rows.isEmpty()) return null;
            String nameNew = rows.get(0).getColumnData("name_new");
            if (!hasText(nameNew)) return null;

            File imgFile = new File(dirPhotos + sep + subPathMedium + sep + nameNew);
            if (!imgFile.exists()) imgFile = new File(dirPhotos + sep + subPathShow + sep + nameNew);
            if (!imgFile.exists()) return null;

            Image img = new Image();
            img.setSrc(DownloadHandler.forFile(imgFile));
            img.addClassName(isCover ? "ndv-cover-photo" : "ndv-item-photo");
            img.setAlt("");
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    // ─────────────────────────── Env init ────────────────────────────────────

    private void resolveEnv() {
        try { hostname = InetAddress.getLocalHost().getHostName(); } catch (Exception ignored) {}
        try { publicIp = new NetUtils().getClientPublicIp(hostname); } catch (Exception ignored) {}
        try { sessionId = VaadinSession.getCurrent().getSession().getId(); } catch (Exception ignored) {}
        try {
            dirPhotos = new com.photo.act.photo_act.views.components.GenericView(recordService)
                    .getAppProps(PROP_PHOTOS);
        } catch (Exception ignored) {}
    }

    private void recordView(Long newsId) {
        try {
            Integer userId = resolveUserId();
            newsService.recordView(newsId, userId, publicIp, sessionId, LocalDateTime.now());
        } catch (Exception ignored) {}
    }

    private Integer resolveUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth instanceof AnonymousAuthenticationToken) return null;
            var records = recordService.findAll(
                    "SELECT userId FROM dbuser WHERE username = '" + auth.getName() + "'",
                    new String[]{"userId"});
            if (records.isEmpty()) return null;
            String id = records.get(0).getColumnData("userId");
            return id != null ? Integer.parseInt(id) : null;
        } catch (Exception e) { return null; }
    }

    // ─────────────────────────── Utils ───────────────────────────────────────

    private static String extractYouTubeId(String input) {
        if (!hasText(input)) return null;
        input = input.trim();
        // Plain 11-char ID
        if (input.matches("[A-Za-z0-9_\\-]{11}")) return input;
        // URL patterns
        for (String pattern : new String[]{"v=", "youtu.be/", "embed/"}) {
            int idx = input.indexOf(pattern);
            if (idx >= 0) {
                String id = input.substring(idx + pattern.length());
                int end = id.indexOf('&');
                if (end < 0) end = id.indexOf('?');
                if (end >= 0) id = id.substring(0, end);
                if (id.length() >= 11) return id.substring(0, 11);
            }
        }
        return null;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static String fmtCount(long v) {
        if (v >= 1_000_000) return (v / 1_000_000) + "M+";
        if (v >= 1_000)     return (v / 1_000) + "K+";
        return String.valueOf(v);
    }
}
