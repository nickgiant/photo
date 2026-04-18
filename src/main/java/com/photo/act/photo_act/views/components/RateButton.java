package com.photo.act.photo_act.views.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.streams.DownloadHandler;

/**
 * Reusable like button: SVG thumbs-up icon + a count of distinct people who clicked.
 *
 * <p>Animations (CSS-driven, all reusable via class names):
 * <ul>
 *   <li><b>Hover</b>: the icon rotates 30° (class {@code like-btn-icon}, rule in like-button.css)</li>
 *   <li><b>Click</b>: scale-up then fade-out burst (class {@code icon-pop-fade} applied via JS,
 *       reusable on any element)</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 *   LikeButton btn = new LikeButton(initialCount);
 *   btn.addLikeClickListener(e -> {
 *       photoViewService.recordLike(...);
 *       btn.setCount(photoViewService.getLikeCount(photoId));
 *   });
 *   layout.add(btn);
 * </pre>
 */
public class RateButton extends Div {

    private final Div    iconWrap;
    private final Span   countSpan;

    /**
     * @param initialCount number of distinct people who already liked this photo
     */
    public RateButton(long initialCount) {
        addClassName("like-btn");

        SvgIcon icon = new SvgIcon(
                DownloadHandler.forClassResource(RateButton.class, "/icons/star-empty-icon.svg"));
        icon.getStyle()
                .set("width",  "1.9em")
                .set("height", "1.9em");

        iconWrap = new Div(icon);
        iconWrap.addClassName("like-btn-icon");

        if(initialCount==0){
            countSpan = new Span();
        }else {
            countSpan = new Span(String.valueOf(initialCount));
        }
        countSpan.addClassName("like-count");

        add(iconWrap, countSpan);

        // Trigger the pop-fade animation on the icon when clicked.
        // The class "icon-pop-fade" is reusable — see like-button.css.
        addClickListener(e ->
            iconWrap.getElement().executeJs(
                "const el = this;" +
                "el.classList.remove('icon-pop-fade');" +
                // Force reflow so re-adding the class restarts the animation
                "void el.offsetWidth;" +
                "el.classList.add('icon-pop-fade');" +
                "el.addEventListener('animationend'," +
                "  () => el.classList.remove('icon-pop-fade'), {once: true});"
            )
        );
    }

    /** Updates the displayed like count. */
    public void setCount(long count) {
        if(count>0) {
            countSpan.setText(String.valueOf(count));
        }
    }

    /** Returns the current displayed count. */
    public long getCount() {
        try {
            return Long.parseLong(countSpan.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Adds a server-side click listener that fires after the animation JS is sent.
     * Use this to record the like and refresh the count.
     */
    public void addRateClickListener(ComponentEventListener<ClickEvent<Div>> listener) {
        addClickListener(listener);
    }
}
