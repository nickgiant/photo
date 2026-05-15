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
 *   frame.setPhoto(file, title, width, height);
 *   frame.animateEnter(+1);   // +1 = forward, -1 = backward
 *
 * CSS: photo-lightbox.css (.pf-frame, .pf-img--portrait/landscape/unknown)
 *      photo-frame.css    (keyframe animations)
 */
public class PhotoFrameComponent extends Div {

    public enum Effect { FADE, ZOOM, SLIDE, NONE }

    private static final String[] ORIENTATION_CLASSES =
            { "pf-img--portrait", "pf-img--landscape", "pf-img--unknown" };

    private final Image img = new Image();
    private Effect effect = Effect.FADE;

    public PhotoFrameComponent() {
        setSizeFull();        // Vaadin layout: fills the parent flex cell (width+height)
        addClassName("pf-frame");
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

    /** Display a fallback static image when the photo file is missing. */
    public void setFallback(String staticSrc) {
        img.setSrc(staticSrc);
        img.setAlt("");
        setOrientationClass("pf-img--unknown");
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
     * round-trip so the browser applies src + animation class together.
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

        // Hide immediately so the old photo doesn't flash during load.
        // Start the animation only after the browser fires 'load' on the new
        // image. For cached images img.complete is already true.
        getElement().executeJs(
                "var img = this.querySelector('img');" +
                "var cls = $0;" +
                "img.classList.remove(" +
                "  'pf-enter-fade','pf-enter-zoom'," +
                "  'pf-enter-slide-fwd','pf-enter-slide-bwd');" +
                "img.style.opacity = '0';" +
                "function go() {" +
                "  img.style.opacity = '';" +
                "  void img.offsetWidth;" +
                "  img.classList.add(cls);" +
                "}" +
                "if (img.complete && img.naturalWidth > 0) { go(); }" +
                "else { img.addEventListener('load', go, {once: true}); }",
                cls
        );
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void applyOrientation(int width, int height) {
        if (width > 0 && height > 0 && height > width) {
            setOrientationClass("pf-img--portrait");
        } else if (width > 0 && height > 0) {
            setOrientationClass("pf-img--landscape");
        } else {
            setOrientationClass("pf-img--unknown");
        }
    }

    private void setOrientationClass(String cls) {
        img.removeClassNames(ORIENTATION_CLASSES);
        img.addClassName(cls);
    }
}
