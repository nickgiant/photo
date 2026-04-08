package com.photo.act.photo_act.views.components;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.List;

/**
 * Vaadin wrapper around GLightbox.
 *
 * GLightbox (MIT, ~13 KB gzipped) — https://github.com/biati-digital/glightbox
 * Chosen because:
 *   • Zero dependencies
 *   • Smooth CSS transitions (fade, zoom, slide, none)
 *   • Built-in zoom, pan, keyboard navigation
 *   • Works as inline viewer (no overlay) when looped into a container
 *   • Very small bundle footprint vs Fancybox 5 or PhotoSwipe
 *
 * Communication model:
 *   Server → Client : callJsFunction("openSlide", index)
 *                     callJsFunction("setSlides", jsonArray)
 *   Client → Server : this.$server.onSlideChanged(index, photoId)
 *                     this.$server.onZoomChanged(scale)
 */
@Tag("glightbox-viewer")
@NpmPackage(value = "glightbox", version = "3.3.0")
@JsModule("./glightbox-viewer.js")
public class GlightboxComponent extends Component {

    // listeners for slide change events coming from the client
    private final List<SlideChangeListener> listeners = new ArrayList<>();

    public GlightboxComponent() {
        getElement().getStyle()
                .set("display", "block")
                .set("width", "100%")
                .set("height", "100%")
                .set("min-height", "0");     // critical — prevents flex overflow
    }

    // ── Server → Client ───────────────────────────────────────────────────────

    /**
     * Load a full album into GLightbox.
     * slides: JSON array string, e.g.:
     *   [{"href":"/uploads/img1.jpg","title":"Dawn","description":"f/5.6 · ISO 400","photoId":42}, ...]
     */
    public void setSlides(String slidesJson) {
        getElement().callJsFunction("setSlides", slidesJson);
    }

    /**
     * Navigate to a slide by 0-based index with GLightbox transition effect.
     * This is called by the thumbnail strip when user clicks a thumb.
     */
    public void openSlide(int index) {
        getElement().callJsFunction("openSlide", index);
    }

    /**
     * Change transition effect at runtime.
     * Values: "fade" | "zoom" | "slide" | "none"
     */
    public void setEffect(String effect) {
        getElement().callJsFunction("setEffect", effect);
    }

    // ── Client → Server ───────────────────────────────────────────────────────

    /**
     * Called by GLightbox JS whenever the user navigates to a new slide
     * (prev/next arrows, keyboard, or openSlide() call).
     * The right Vaadin panel reacts by loading that photo's metadata.
     */
    @ClientCallable
    public void onSlideChanged(int index, long photoId) {
        listeners.forEach(l -> l.slideChanged(index, photoId));
    }

    /**
     * Called when the user zooms the image in the viewer.
     * Useful for showing a "zoom level" indicator in the info panel.
     */
    @ClientCallable
    public void onZoomChanged(double scale) {
        // optional — wire to a zoom indicator in the info panel
    }

    public Registration addSlideChangeListener(SlideChangeListener listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    @FunctionalInterface
    public interface SlideChangeListener {
        void slideChanged(int index, long photoId);
    }
}
