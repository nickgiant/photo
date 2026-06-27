package com.photo.act.photo_act.views;


import com.photo.act.photo_act.model.ContentEntity;
import com.photo.act.photo_act.model.ContentType;
import com.photo.act.photo_act.repository.ContentRepository;
import com.photo.act.photo_act.utils.PageSeoUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Vaadin view for /article/{slug}
 *
 * Real human users land here via their browser.
 * Social bots are intercepted at Nginx before reaching Vaadin.
 *
 * This view dynamically updates the browser <title> via @PageTitle
 * (static fallback) and PageTitle API (dynamic, set in BeforeEnter).
 *
 * NOTE: For actual OG tags in the real Vaadin page (for cases where
 * a user shares the URL directly from a browser with JS enabled and
 * a sophisticated scraper), inject meta tags via UI.getCurrent()
 * and JavaScript as shown in injectDynamicOgMeta().
 */

@AnonymousAllowed
@Route(value = "article/:slug", layout = MainLayout.class)
@PageTitle("PhotoAct.net - Photography Community | Article")
public class ArticleView extends VerticalLayout implements BeforeEnterObserver {

    private final ContentRepository contentRepository;

    private final H1 titleEl        = new H1();
    private final Paragraph descEl  = new Paragraph();
    private final Image coverImg    = new Image();
    private final Paragraph metaEl  = new Paragraph();

    public ArticleView(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
        setSpacing(true);
        setPadding(true);
        add(coverImg, titleEl, metaEl, descEl);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String slug = event.getRouteParameters().get("slug").orElse("");

        contentRepository.findByContentTypeAndSlug(ContentType.ARTICLE, slug)
                .ifPresentOrElse(
                        this::populateView,
                        () -> event.rerouteToError(IllegalArgumentException.class, "Article not found: " + slug)
                );
    }

    private void populateView(ContentEntity entity) {
        // Update browser title dynamically
        UI.getCurrent().getPage().setTitle(entity.getTitle() + " | PhotoAct.net");
        if (entity.getDescription() != null && !entity.getDescription().isBlank()) {
            PageSeoUtil.setMetaDescription(entity.getDescription());
        }

        titleEl.setText(entity.getTitle());
        descEl.setText(entity.getDescription() != null ? entity.getDescription() : "");
        metaEl.setText("By " + entity.getAuthorName()
                + (entity.getPublishedAt() != null ? " · " + entity.getPublishedAt().toLocalDate() : ""));

        if (entity.getCoverImage() != null && !entity.getCoverImage().isBlank()) {
            coverImg.setSrc(entity.getCoverImage());
            coverImg.setAlt(entity.getTitle());
            coverImg.setWidth("100%");
            coverImg.setMaxHeight("400px");
            coverImg.getStyle().set("object-fit", "cover");
        }

        // Inject OG meta into the live Vaadin page (helps some JS-capable bots)
        injectDynamicOgMeta(entity);
    }

    /**
     * Injects OG meta tags into the current Vaadin page via JavaScript.
     *
     * This is NOT a replacement for the server-side Thymeleaf approach —
     * most social crawlers do NOT execute JavaScript. This helps with
     * Slack, Discord, and similar crawlers that do limited JS execution.
     */
    private void injectDynamicOgMeta(ContentEntity entity) {
        String imageUrl = entity.getCoverImage() != null
                ? entity.getCoverImage()
                : "/static/og-default.jpg";

        UI.getCurrent().getPage().executeJs(
            """
            (function() {
                function setMeta(property, content, attr) {
                    attr = attr || 'property';
                    var el = document.querySelector('meta[' + attr + '="' + property + '"]');
                    if (!el) { el = document.createElement('meta'); el.setAttribute(attr, property); document.head.appendChild(el); }
                    el.setAttribute('content', content);
                }
                setMeta('og:title',       $0);
                setMeta('og:description', $1);
                setMeta('og:image',       $2);
                setMeta('og:type',        'article');
                setMeta('twitter:card',   'summary_large_image', 'name');
                setMeta('twitter:title',  $0, 'name');
                setMeta('twitter:image',  $2, 'name');
            })();
            """,
            entity.getTitle(),
            entity.getDescription() != null ? entity.getDescription() : "",
            imageUrl
        );
    }
}
