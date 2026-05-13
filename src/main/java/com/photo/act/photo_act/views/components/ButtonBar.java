package com.photo.act.photo_act.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.dependency.CssImport;

/**
 * Reusable horizontal icon button bar with UIverse-style tooltip effects.
 *
 * Each button is a circle with a sliding tooltip on hover.
 * Brand colors are applied via CSS class names (e.g. "btn-bar-facebook").
 *
 * Usage:
 *   ButtonBar bar = new ButtonBar();
 *   bar.addButton("Facebook", facebookIcon, () -> openFacebook(), "btn-bar-facebook");
 *   bar.addButton("Copy URL", linkIcon, this::copyUrl, "btn-bar-copy");
 */
@CssImport("./themes/my-app/components/button-bar.css")
public class ButtonBar extends Div {

    public ButtonBar() {
        addClassName("btn-bar-wrapper");
    }

    /**
     * Adds a button to the bar without a brand color class.
     */
    public Div addButton(String tooltipLabel, Component icon, Runnable onClick) {
        return addButton(tooltipLabel, icon, onClick, null);
    }

    /**
     * Adds any Vaadin component directly to the bar (e.g. LikeButton, RateButton, a Button).
     * The component renders with its own styling, inline in the flex container.
     */
    public void addComponent(Component component) {
        add(component);
    }

    /**
     * Adds a button to the bar.
     *
     * @param tooltipLabel text shown in the tooltip on hover
     * @param icon         any Vaadin Component used as the button icon
     * @param onClick      action to run on click (may be null)
     * @param brandClass   optional CSS class for brand color, e.g. "btn-bar-facebook"
     * @return the created button Div (for further customisation if needed)
     */
    public Div addButton(String tooltipLabel, Component icon, Runnable onClick, String brandClass) {

        Div btn = new Div();
        btn.addClassName("btn-bar-item");
        if (brandClass != null && !brandClass.isBlank()) {
            btn.addClassName(brandClass);
        }

        Div tooltip = new Div();
        tooltip.addClassName("btn-bar-tooltip");
        tooltip.setText(tooltipLabel);

        Span iconSpan = new Span(icon);
        iconSpan.addClassName("btn-bar-icon-span");
        // prevent the icon from swallowing clicks before they reach the btn Div
        icon.getStyle().set("pointer-events", "none");

        btn.add(tooltip, iconSpan);

        if (onClick != null) {
            btn.addClickListener(e -> onClick.run());
        }

        add(btn);
        return btn;
    }
}
