/**
 * Inline photo viewer custom element.
 *
 * Replaces the GLightbox-overlay approach with a simple embedded viewer:
 *   - Full-size <img> that fills the container
 *   - Overlaid prev / next arrow buttons
 *   - Overlaid close (×) button — fires window.history.back()
 *   - Keyboard: ArrowLeft / ArrowRight / Escape
 *
 * API (called from Java via callJsFunction):
 *   setSlides(slidesJson)   — load slide array, show first slide
 *   openSlide(index)        — jump to a specific slide (no server callback)
 *   setEffect(effect)       — accepted but ignored (kept for API compat)
 *
 * Server callbacks (this.$server.*):
 *   onSlideChanged(index, photoId)  — user navigated via arrows / keyboard
 */
class GlightboxViewer extends HTMLElement {

    connectedCallback() {
        this.style.cssText =
            'display:block;width:100%;height:100%;position:relative;' +
            'background:#111;overflow:hidden;';

        // ── Main image ────────────────────────────────────────────────────────
        this._img = document.createElement('img');
        this._img.style.cssText =
            'width:100%;height:100%;object-fit:contain;display:block;' +
            'user-select:none;-webkit-user-drag:none;';

        // ── Prev / Next buttons ───────────────────────────────────────────────
        this._prevBtn = this._navBtn('&#10094;', 'left:0');
        this._nextBtn = this._navBtn('&#10095;', 'right:0');

        this._prevBtn.addEventListener('click', () => this._go(-1));
        this._nextBtn.addEventListener('click', () => this._go(+1));

        // ── Close button (top-right) ──────────────────────────────────────────
        this._closeBtn = document.createElement('button');
        this._closeBtn.innerHTML = '&times;';
        this._closeBtn.title = 'Close';
        this._closeBtn.style.cssText =
            'position:absolute;top:10px;right:10px;z-index:20;' +
            'background:rgba(0,0,0,0.55);color:#fff;border:none;' +
            'border-radius:50%;width:38px;height:38px;font-size:24px;' +
            'line-height:1;cursor:pointer;display:flex;align-items:center;' +
            'justify-content:center;transition:background 0.2s;';
        this._closeBtn.addEventListener('mouseenter',
            () => (this._closeBtn.style.background = 'rgba(0,0,0,0.85)'));
        this._closeBtn.addEventListener('mouseleave',
            () => (this._closeBtn.style.background = 'rgba(0,0,0,0.55)'));
        this._closeBtn.addEventListener('click', () => window.history.back());

        this.append(this._img, this._prevBtn, this._nextBtn, this._closeBtn);

        // ── Keyboard navigation ───────────────────────────────────────────────
        this._keyHandler = (e) => {
            if (e.key === 'ArrowLeft')  this._go(-1);
            if (e.key === 'ArrowRight') this._go(+1);
            if (e.key === 'Escape')     window.history.back();
        };
        document.addEventListener('keydown', this._keyHandler);

        this._slides = [];
        this._idx    = 0;
    }

    disconnectedCallback() {
        document.removeEventListener('keydown', this._keyHandler);
    }

    // ── Helper: arrow nav button ──────────────────────────────────────────────
    _navBtn(html, side) {
        const btn = document.createElement('button');
        btn.innerHTML = html;
        btn.style.cssText =
            `position:absolute;top:50%;transform:translateY(-50%);${side};z-index:20;` +
            'background:rgba(0,0,0,0.45);color:#fff;border:none;' +
            'width:44px;height:80px;font-size:30px;cursor:pointer;' +
            'border-radius:3px;display:flex;align-items:center;' +
            'justify-content:center;transition:background 0.2s;';
        btn.addEventListener('mouseenter',
            () => (btn.style.background = 'rgba(0,0,0,0.80)'));
        btn.addEventListener('mouseleave',
            () => (btn.style.background = 'rgba(0,0,0,0.45)'));
        return btn;
    }

    // ── Called from Java ──────────────────────────────────────────────────────

    setSlides(slidesJson) {
        this._slides = JSON.parse(slidesJson);
        this._show(this._idx);
    }

    /** Jump to slide without firing a server callback (thumb-click path). */
    openSlide(index) {
        this._idx = index;
        this._show(index);
    }

    /** Kept for API compatibility — no-op in this implementation. */
    setEffect(effect) {}

    // ── Internal navigation ───────────────────────────────────────────────────

    _go(dir) {
        if (!this._slides.length) return;
        this._idx = (this._idx + dir + this._slides.length) % this._slides.length;
        this._show(this._idx);
        const photoId = this._slides[this._idx]?.photoId;
        if (this.$server) this.$server.onSlideChanged(this._idx, Number(photoId) || 0);
    }

    _show(idx) {
        const slide = this._slides[idx];
        if (!slide) return;
        this._img.src = slide.href;
        this._img.alt = slide.title || '';
    }
}

customElements.define('glightbox-viewer', GlightboxViewer);
