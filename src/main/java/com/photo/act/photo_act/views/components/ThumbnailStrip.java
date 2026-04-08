package com.photo.act.photo_act.views.components;


import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.model.ContentEntity;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.server.streams.DownloadHandler;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.photo.act.photo_act.views.AlbumsView.subPathThumbs;

/**
 * Horizontally scrollable filmstrip of photo thumbnails.
 *
 * ── Layout rules ──────────────────────────────────────────────────────────────
 *  • Fixed height: 100 px
 *  • Overflow-x: auto (scrolls when photos > screen width)
 *  • Each thumbnail: 130×80 px with object-fit:cover
 *  • Active thumb gets a 2 px accent border
 *  • Clicking a thumb calls the provided onSelect callback with (index, photoId)
 *    which then calls GlightboxComponent.openSlide(index) server-side
 *
 * ── Thumbnail image source ─────────────────────────────────────────────────
 *  Uses {name}-thumb-sm.jpg  (200×150 px, from process-photo.sh)
 *  Small file size keeps the filmstrip fast to load.
 */
public class ThumbnailStrip extends Div {

    private static final String ACTIVE_BORDER  = "2px solid var(--lumo-primary-color)";
    private static final String INACTIVE_BORDER = "2px solid transparent";
    private static final int    THUMB_W = 130;
    private static final int    THUMB_H = 80;

    private final HorizontalLayout strip = new HorizontalLayout();
    private Div activeThumb = null;
    private int activeIndex = 0;

    private String dirChar = FileSystems.getDefault().getSeparator();

    /**
     * @param photos   ordered list of ContentEntity (one per photo in the album)
     * @param onSelect callback(index, photoId) — called when user clicks a thumb
     */
    public ThumbnailStrip(@MonotonicNonNull List<Record> photos, String strPathThumbs, BiConsumer<Integer, Long> onSelect) {
        setWidthFull();
        setHeight("100px");
        getStyle()
                .set("flex-shrink", "0")             // never shrink in the outer VerticalLayout
                .set("border-top", "0.5px solid var(--lumo-contrast-10pct)");






        // Use Vaadin Scroller for horizontal scrolling — handles overflow correctly
        Scroller scroller = new Scroller(strip);
        scroller.setScrollDirection(Scroller.ScrollDirection.HORIZONTAL);
        scroller.setSizeFull();

        strip.setPadding(false);
        strip.setSpacing(false);
        strip.getStyle()
                .set("gap", "6px")
                .set("padding", "8px 8px")
                .set("align-items", "center")
                .set("height", "100%");

        for (int i = 0; i < photos.size(); i++) {
            Record photo = photos.get(i);
            final int idx = i;

            // Build thumb image src: swap cover_image path to -thumb-sm variant
            String thumbSrc = strPathThumbs+ dirChar + photo.getColumnData("name_new");

            // Thumb container
            Div thumb = new Div();
            thumb.setWidth(THUMB_W + "px");
            thumb.setHeight(THUMB_H + "px");
            thumb.getStyle()
                    .set("flex-shrink", "0")
                    .set("border-radius", "var(--lumo-border-radius-s)")
                    .set("overflow", "hidden")
                    .set("cursor", "pointer")
                    .set("border", i == 0 ? ACTIVE_BORDER : INACTIVE_BORDER)
                    .set("transition", "border-color 0.2s, opacity 0.2s")
                    .set("opacity", i == 0 ? "1" : "0.7");

            // Image inside thumb

            Path path = Paths.get(thumbSrc);
            File file = path.toFile();

            Image img = new Image(DownloadHandler.forFile(file), photo.getColumnData("title"));
            img.setWidth(THUMB_W + "px");
            img.setHeight(THUMB_H + "px");
            img.getStyle().set("object-fit", "cover");

            thumb.add(img);

            // Click handler — notify parent view
            thumb.addClickListener(e -> {
                setActiveThumb(thumb, idx);
                onSelect.accept(idx, Long.parseLong(photo.getColumnData("id").toString()));
            });

            strip.add(thumb);

            // Keep first thumb as active reference
            if (i == 0) activeThumb = thumb;
        }

        add(scroller);
    }

    /**
     * Activate a thumbnail programmatically (called when GLightbox navigates
     * via keyboard/swipe, so the filmstrip stays in sync).
     */
    public void setActiveIndex(int index) {
        if (index < 0 || index >= strip.getComponentCount()) return;
        if (index == activeIndex) return;
        Div thumb = (Div) strip.getComponentAt(index);
        setActiveThumb(thumb, index);
        // Scroll thumb into view via JS
        thumb.getElement().callJsFunction(
                "el => el.scrollIntoView({behavior:'smooth',block:'nearest',inline:'center'})");
    }

    private void setActiveThumb(Div thumb, int index) {
        if (activeThumb != null) {
            activeThumb.getStyle()
                    .set("border", INACTIVE_BORDER)
                    .set("opacity", "0.7");
        }
        thumb.getStyle()
                .set("border", ACTIVE_BORDER)
                .set("opacity", "1");
        activeThumb = thumb;
        activeIndex = index;
    }

    /**
     * Convert a cover image path to its -thumb-sm variant.
     * e.g. /uploads/processed/42/photo-preview.jpg
     *   →  /uploads/processed/42/photo-thumb-sm.jpg
     */
    private String toThumbSrc(String coverImage) {
        if (coverImage == null) return "/static/placeholder-thumb.jpg";
        return coverImage
                .replaceAll("-(og|og-blur|preview|watermark)\\.jpg$", "-thumb-sm.jpg")
                .replaceAll("\\.jpg$", "-thumb-sm.jpg");
    }

}