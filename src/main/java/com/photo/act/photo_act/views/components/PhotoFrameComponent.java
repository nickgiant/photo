package com.photo.act.photo_act.views.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.streams.DownloadHandler;

import java.io.File;

/**
 * Reusable full-frame photo display with orientation-aware sizing and CSS effects.
 *
 * ── Orientation ───────────────────────────────────────────────────────────────
 *   Landscape (w ≥ h) : img fills container width,  height proportional
 *   Portrait  (h > w) : img fills container height, width  proportional
 *   Unknown dimensions : img constrained by max-width/max-height (object-fit fallback)
 *   No cropping in any case.
 *
 * ── Effects ───────────────────────────────────────────────────────────────────
 *   "fade"  – opacity 0 → 1
 *   "zoom"  – scale 0.94 + opacity 0 → scale 1 + opacity 1
 *   "slide" – translateX ±40px + opacity 0 → translateX 0 + opacity 1
 *   "none"  – instant cut
 *
 * ── Usage ─────────────────────────────────────────────────────────────────────
 *   PhotoFrameComponent frame = new PhotoFrameComponent();
 *   frame.setEffect("zoom");
 *   // on navigation:
 *   frame.setPhoto(file, title, width, height);
 *   frame.animateEnter(+1);   // +1 = forward, -1 = backward
 *
 * Requires the CSS keyframes from photo-frame.css (imported in styles.css).
 */
public class PhotoFrameComponent extends Div {

    public enum Effect { FADE, ZOOM, SLIDE, NONE }

    private final Image img = new Image();
    private Effect effect = Effect.FADE;

    public PhotoFrameComponent() {
        setSizeFull();
        getStyle()
                .set("position",        "relative")   // anchors absolute-positioned overlays
                .set("display",         "flex")
                .set("align-items",     "center")
                .set("justify-content", "center")
                .set("background",      "#0d0d0d")
                .set("overflow",        "hidden")
                .set("min-height",      "0");          // critical inside flex parents

        img.getStyle()
                .set("display",      "block")
                .set("object-fit",   "contain");       // last-resort safeguard

        add(img);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Display a photo from the filesystem, sized correctly for its orientation.
     *
     * @param file   large photo file (served via DownloadHandler)
     * @param title  alt text
     * @param width  pixel width from DB metadata  (≤ 0 = unknown)
     * @param height pixel height from DB metadata (≤ 0 = unknown)
     */
    public void setPhoto(File file, String title, int width, int height) {
        img.setSrc(DownloadHandler.forFile(file));
        img.setAlt(title != null ? title : "");
        applyOrientation(width, height);
    }

    /**
     * Display a fallback static image when the photo file is missing.
     */
    public void setFallback(String staticSrc) {
        img.setSrc(staticSrc);
        img.setAlt("");
        img.getStyle()
                .set("width",      "auto")
                .set("height",     "auto")
                .set("max-width",  "100%")
                .set("max-height", "100%");
    }

    /**
     * Set the CSS transition effect used between photos.
     *
     * @param effect "fade" | "zoom" | "slide" | "none"  (default: "fade")
     */
    public void setEffect(String effect) {
        this.effect = switch (effect != null ? effect.toLowerCase() : "") {
            case "zoom"  -> Effect.ZOOM;
            case "slide" -> Effect.SLIDE;
            case "none"  -> Effect.NONE;
            default      -> Effect.FADE;
        };
    }

    /**
     * Trigger the enter-animation for the photo just set via setPhoto().
     * Call immediately after setPhoto() — both are sent in the same Vaadin
     * round-trip, so the browser applies the new src and animation together.
     *
     * @param direction +1 = forward (next), -1 = backward (prev)
     */
    public void animateEnter(int direction) {
        String cls = switch (effect) {
            case FADE  -> "pf-enter-fade";
            case ZOOM  -> "pf-enter-zoom";
            case SLIDE -> direction >= 0 ? "pf-enter-slide-fwd" : "pf-enter-slide-bwd";
            case NONE  -> null;
        };
        if (cls == null) return;

        // Remove all animation classes, force a reflow to restart the animation,
        // then add the new class. This runs in the same browser frame as the
        // src update from setPhoto(), so the image fades/slides in from scratch.
        getElement().executeJs(
                "var img = this.querySelector('img');" +
                "img.classList.remove(" +
                "  'pf-enter-fade','pf-enter-zoom'," +
                "  'pf-enter-slide-fwd','pf-enter-slide-bwd');" +
                "void img.offsetWidth;" +   // reflow: restart keyframe from 0%
                "img.classList.add($0);",
                cls
        );
    }

    // ── Private ───────────────────────────────────────────────────────────────

    /**
     * Apply CSS sizing that shows the full photo without cropping.
     *
     * Portrait  (h > w): fill container height, auto width.
     * Landscape / square: fill container width, auto height.
     * Unknown: fall back to max-constrained auto sizing.
     */
    private void applyOrientation(int width, int height) {
        if (width > 0 && height > 0 && height > width) {
            // Portrait
            img.getStyle()
                    .set("height",     "100%")
                    .set("width",      "auto")
                    .set("max-width",  "100%")
                    .set("max-height", "100%");
        } else if (width > 0 && height > 0) {
            // Landscape or square
            img.getStyle()
                    .set("width",      "100%")
                    .set("height",     "auto")
                    .set("max-width",  "100%")
                    .set("max-height", "100%");
        } else {
            // Unknown dimensions — constrain both axes, maintain aspect ratio
            img.getStyle()
                    .set("width",      "auto")
                    .set("height",     "auto")
                    .set("max-width",  "100%")
                    .set("max-height", "100%");
        }
    }
}
