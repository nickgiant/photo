package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.Record;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.streams.DownloadHandler;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Horizontally scrollable filmstrip of photo thumbnails.
 *
 * ── Layout ───────────────────────────────────────────────────────────────────
 *   [❮] ──── scrollable strip ──── [❯]
 *
 *   • Fixed height: 100 px  (thumbnails use 84 px inside, per CSS)
 *   • Thumbnail widths are proportional to meta_i_width/meta_i_height so
 *     portrait shots appear narrow and landscape shots appear wide.
 *   • Active thumb gets the .ts-thumb--active CSS class.
 *   • setActiveIndex() smooth-scrolls the chosen thumb to the centre.
 *   • ❮/❯ buttons scroll the strip by ~3 thumbnail widths per click.
 *
 * CSS: photo-lightbox.css
 *   .ts-strip, .ts-scroll, .ts-strip-inner, .ts-thumb, .ts-thumb--active,
 *   .ts-scroll-btn
 */
public class ThumbnailStrip extends Div {

    private static final int THUMB_H     = 84;   // px — must match .ts-strip CSS height
    private static final int SCROLL_STEP = 240;  // px per arrow-button click

    private final Div         scrollContainer = new Div();
    private final Div         strip           = new Div();
    private final List<Div>   thumbs          = new ArrayList<>();

    private Div activeThumb = null;
    private int  activeIndex = -1;   // -1 → first setActiveIndex() call always runs

    private final String dirChar = FileSystems.getDefault().getSeparator();

    // ── Constructor ───────────────────────────────────────────────────────────

    public ThumbnailStrip(List<Record> photos, String strPathThumbs,
                          BiConsumer<Integer, Long> onSelect) {
        addClassName("ts-strip");

        scrollContainer.addClassName("ts-scroll");
        strip.addClassName("ts-strip-inner");

        // ── Thumbnails ────────────────────────────────────────────────────────
        for (int i = 0; i < photos.size(); i++) {
            Record photo  = photos.get(i);
            final int idx = i;

            int w      = parseIntSafe(photo.getColumnData("meta_i_width"));
            int h      = parseIntSafe(photo.getColumnData("meta_i_height"));
            int thumbW = computeThumbWidth(w, h);

            File   file = Paths.get(strPathThumbs + dirChar + photo.getColumnData("name_new")).toFile();
            String alt  = photo.getColumnData("title") != null ? photo.getColumnData("title") : "";

            Div thumb = new Div();
            thumb.addClassName("ts-thumb");
            thumb.setWidth(thumbW + "px");
            thumb.setHeight(THUMB_H + "px");

            Image img = new Image(DownloadHandler.forFile(file), alt);
            thumb.add(img);

            thumb.addClickListener(e -> {
                setActiveThumb(thumb, idx);
                onSelect.accept(idx, Long.parseLong(photo.getColumnData("id")));
            });

            strip.add(thumb);
            thumbs.add(thumb);
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
     * centre of the strip. Safe to call during beforeEnter() — the JS is
     * queued and runs after the first browser render.
     */
    public void setActiveIndex(int index) {
        if (index < 0 || index >= thumbs.size()) return;
        if (index == activeIndex) return;
        Div thumb = thumbs.get(index);
        setActiveThumb(thumb, index);
        thumb.getElement().executeJs(
                "this.scrollIntoView({behavior:'smooth', block:'nearest', inline:'center'})");
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void setActiveThumb(Div thumb, int index) {
        if (activeThumb != null) activeThumb.removeClassName("ts-thumb--active");
        thumb.addClassName("ts-thumb--active");
        activeThumb = thumb;
        activeIndex = index;
    }

    private static Div makeScrollBtn(String symbol) {
        Div btn = new Div();
        btn.setText(symbol);
        btn.addClassName("ts-scroll-btn");
        return btn;
    }

    /** Width proportional to aspect ratio, clamped to [40, 160] px. */
    private static int computeThumbWidth(int w, int h) {
        if (w > 0 && h > 0) {
            int computed = (int) Math.round((double) THUMB_H * w / h);
            return Math.max(40, Math.min(160, computed));
        }
        return 100;
    }

    private static int parseIntSafe(String s) {
        if (s == null) return 0;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}
