import GLightbox from 'glightbox';
import 'glightbox/dist/css/glightbox.min.css';

class GlightboxViewer extends HTMLElement {

    connectedCallback() {
        // Wrapper div that GLightbox renders into (inline mode)
        this._container = document.createElement('div');
        this._container.style.cssText =
            'width:100%;height:100%;display:flex;align-items:center;' +
            'justify-content:center;background:#111;overflow:hidden;';
        this.appendChild(this._container);

        this._slides    = [];
        this._currentIdx = 0;
        this._effect    = 'zoom';  // default transition
        this._glb       = null;
    }

    // ── Called from Java ──────────────────────────────────────────────────────

    /**
     * Receives JSON slide array and initialises GLightbox.
     * Each slide: { href, title, description, photoId, type:"image" }
     */
    setSlides(slidesJson) {
        this._slides = JSON.parse(slidesJson);
        this._initGlightbox();
    }

    /**
     * Jump to slide at index with transition.
     * Called when user clicks a thumbnail in the Vaadin filmstrip.
     */
    openSlide(index) {
        if (!this._glb) return;
        this._currentIdx = index;
        this._glb.openAt(index);
    }

    setEffect(effect) {
        this._effect = effect;
        if (this._glb) {
            this._glb.destroy();
            this._initGlightbox();
        }
    }

    // ── GLightbox init ────────────────────────────────────────────────────────

    _initGlightbox() {
        if (this._glb) this._glb.destroy();

        // Build <a> elements that GLightbox reads
        this._container.innerHTML = '';
        const gallery = document.createElement('div');
        gallery.className = 'glightbox-inline-gallery';
        gallery.style.cssText = 'width:100%;height:100%;';

        this._slides.forEach((slide, i) => {
            const a = document.createElement('a');
            a.href        = slide.href;
            a.dataset.glightbox = `type: image`;
            a.dataset.title       = slide.title       || '';
            a.dataset.description = slide.description || '';
            a.dataset.photoId     = slide.photoId;
            a.style.display       = i === 0 ? 'block' : 'none';  // only first visible
            gallery.appendChild(a);
        });
        this._container.appendChild(gallery);

        this._glb = GLightbox({
            elements:        this._slides.map(s => ({
                href:        s.href,
                type:        'image',
                title:       s.title       || '',
                description: s.description || '',
            })),
            startAt:         this._currentIdx,
            openEffect:      this._effect,  // fade | zoom | slide | none
            closeEffect:     this._effect,
            slideEffect:     'slide',        // slide between photos always
            loop:            true,
            draggable:       true,           // swipe/drag
            dragToleranceX:  40,
            dragToleranceY:  65,
            closeButton:     false,          // we control closing via Vaadin
            touchNavigation: true,
            keyboard:        true,
            zoomable:        true,

            // ── Callbacks → notify Vaadin server ──────────────────────────
            onOpen: () => {},
            onSlideChanged: (slide) => {
                const idx     = this._glb.getActiveSlideIndex();
                const photoId = this._slides[idx]?.photoId || 0;
                this._currentIdx = idx;

                // Notify Vaadin server (updates right panel + thumbnail highlight)
                this.$server.onSlideChanged(idx, photoId);

                // Dispatch DOM event so the filmstrip JS can also react
                this.dispatchEvent(new CustomEvent('glightbox-slide-change', {
                    bubbles: true,
                    detail: { index: idx, photoId }
                }));
            },
            onZoomIn:  (img) => this.$server.onZoomChanged(2.0),
            onZoomOut: (img) => this.$server.onZoomChanged(1.0),
        });

        // Open immediately in the container (inline, not as page overlay)
        this._glb.open();
    }
}

customElements.define('glightbox-viewer', GlightboxViewer);