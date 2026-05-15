package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.Record;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.streams.DownloadHandler;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Horizontally scrollable filmstrip of photo thumbnails.
 *
 * ── Layout ───────────────────────────────────────────────────────────────────
 *   [❮] ──── scrollable strip ──── [❯]
 *
 *   • Fixed height: 100 px  (thumbnails use 84 px inside)
 *   • Thumbnail widths are proportional to the photo's aspect ratio so
 *     portrait shots appear narrow and landscape shots appear wide.
 *   • Active thumb gets a 2 px primary-colour border and full opacity.
 *   • The ❮/❯ buttons scroll the strip by ~3 thumbnail widths per click.
 *   • setActiveIndex() scrolls the chosen thumb to the centre of the strip.
 */
public class ThumbnailStrip extends Div {

    private static final String ACTIVE_BORDER   = "2px solid var(--lumo-primary-color)";
    private static final String INACTIVE_BORDER = "2px solid transparent";
    private static final int    THUMB_H = 84;          // image height inside 100 px strip
    private static final int    SCROLL_STEP = 240;     // px per arrow-button click (~3 thumbs)

    private final HorizontalLayout strip          = new HorizontalLayout();
    private final Div               scrollContainer = new Div();

    private Div activeThumb = null;
    private int  activeIndex = -1;   // -1 → first setActiveIndex() call always runs

    private final String dirChar = FileSystems.getDefault().getSeparator();

    // ── Constructor ───────────────────────────────────────────────────────────

    public ThumbnailStrip(List<Record> photos, String strPathThumbs,
                          BiConsumer<Integer, Long> onSelect) {
        setWidthFull();
        setHeight("100px");
        getStyle()
                .set("flex-shrink",  "0")
                .set("display",      "flex")
                .set("align-items",  "stretch")
                .set("border-top",   "0.5px solid var(--lumo-contrast-10pct)");

        // ── Scroll container ──────────────────────────────────────────────────
        scrollContainer.addClassName("ts-scroll");
        scrollContainer.getStyle()
                .set("flex",       "1 1 auto")
                .set("min-width",  "0")
                .set("overflow-x", "auto")
                .set("overflow-y", "hidden");

        strip.setPadding(false);
        strip.setSpacing(false);
        strip.getStyle()
                .set("gap",         "6px")
                .set("padding",     "8px")
                .set("align-items", "center")
                .set("height",      "100%")
                .set("min-width",   "max-content");  // grow to content width → triggers scroll

        // ── Thumbnails ────────────────────────────────────────────────────────
        for (int i = 0; i < photos.size(); i++) {
            Record photo  = photos.get(i);
            final int idx = i;

            int w      = parseIntSafe(photo.getColumnData("meta_i_width"));
            int h      = parseIntSafe(photo.getColumnData("meta_i_height"));
            int thumbW = computeThumbWidth(w, h);

            File  file  = Paths.get(strPathThumbs + dirChar + photo.getColumnData("name_new")).toFile();
            String alt  = photo.getColumnData("title") != null ? photo.getColumnData("title") : "";

            Div thumb = new Div();
            thumb.setWidth(thumbW + "px");
            thumb.setHeight(THUMB_H + "px");
            thumb.getStyle()
                    .set("flex-shrink",  "0")
                    .set("border-radius","var(--lumo-border-radius-s)")
                    .set("overflow",     "hidden")
                    .set("cursor",       "pointer")
                    .set("border",       INACTIVE_BORDER)
                    .set("transition",   "border-color 0.2s, opacity 0.2s")
                    .set("opacity",      "0.7");

            Image img = new Image(DownloadHandler.forFile(file), alt);
            img.setWidth("100%");
            img.setHeight("100%");
            img.getStyle().set("object-fit", "cover");

            thumb.add(img);
            thumb.addClickListener(e -> {
                setActiveThumb(thumb, idx);
                onSelect.accept(idx, Long.parseLong(photo.getColumnData("id")));
            });

            strip.add(thumb);
        }

        scrollContainer.add(strip);

        // ── Scroll buttons ────────────────────────────────────────────────────
        Div prevBtn = makeScrollBtn("❮");
        Div nextBtn = makeScrollBtn("❯");
        prevBtn.addClickListener(e ->
                scrollContainer.getElement().executeJs("this.scrollLeft -= " + SCROLL_STEP));
        nextBtn.addClickListener(e ->
                scrollContainer.getElement().executeJs("this.scrollLeft += " + SCROLL_STEP));

        add(prevBtn, scrollContainer, nextBtn);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Highlight the thumbnail at {@code index} and smooth-scroll it to the
     * centre of the strip.  Safe to call during beforeEnter() — the JS is
     * queued and runs after the first browser render.
     */
    public void setActiveIndex(int index) {
        if (index < 0 || index >= strip.getComponentCount()) return;
        if (index == activeIndex) return;
        Div thumb = (Div) strip.getComponentAt(index);
        setActiveThumb(thumb, index);
        // scrollIntoView walks up the DOM tree and scrolls the nearest
        // overflow container (scrollContainer) to centre the thumb.
        thumb.getElement().executeJs(
                "this.scrollIntoView({behavior:'smooth', block:'nearest', inline:'center'})");
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void setActiveThumb(Div thumb, int index) {
        if (activeThumb != null) {
            activeThumb.getStyle()
                    .set("border",  INACTIVE_BORDER)
                    .set("opacity", "0.7");
        }
        thumb.getStyle()
                .set("border",  ACTIVE_BORDER)
                .set("opacity", "1");
        activeThumb = thumb;
        activeIndex = index;
    }

    private static Div makeScrollBtn(String symbol) {
        Div btn = new Div();
        btn.setText(symbol);
        btn.getStyle()
                .set("flex-shrink",      "0")
                .set("width",            "32px")
                .set("background",       "rgba(0,0,0,0.45)")
                .set("color",            "#fff")
                .set("font-size",        "18px")
                .set("cursor",           "pointer")
                .set("display",          "flex")
                .set("align-items",      "center")
                .set("justify-content",  "center")
                .set("user-select",      "none");
        return btn;
    }

    /** Width proportional to aspect ratio, clamped to [40, 160] px. */
    private static int computeThumbWidth(int w, int h) {
        if (w > 0 && h > 0) {
            int computed = (int) Math.round((double) THUMB_H * w / h);
            return Math.max(40, Math.min(160, computed));
        }
        return 100;   // default for photos with unknown dimensions
    }

    private static int parseIntSafe(String s) {
        if (s == null) return 0;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}
