//  https://gitlab.com/codecamp-de/vaadin-message-dialog/-/blob/master/src/main/java/de/codecamp/vaadin/components/messagedialog/MessageDialog.java
//https://vaadin.com/directory/component/messagedialog-for-vaadin-flow/overview

package com.photo.act.photo_act.views.components;


import com.vaadin.flow.component.*;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.dom.ElementFactory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.function.Consumer;


/**
 * A dialog component providing a convenient base structure that is suitable for most message,
 * confirmation, question dialogs.
 */
public class DialogMessage extends Dialog {

    private VerticalLayout rootLayout;

    private String title = "";
    private HorizontalLayout header = new HorizontalLayout();


    private Div content = new Div();


    private Div footer = new Div();

    private ButtonBar buttonBar = new ButtonBar();

    private Hr detailsSeparator = new Hr();

    private Div details = new Div();

    private boolean continueAfterClose = false;


    public DialogMessage(String titleIn) {
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        title = titleIn;
        // getElement().getStyle().set("background-color", "var(--lumo-error-color)");

        header.setVisible(true);

        header.getElement().getClassList().add("draggable");
        this.setDraggable(true);


        //EntityData entityData = new EntityData();
        //dialog.add(caption);
        Button btnClose = new Button();
        btnClose.setIcon(VaadinIcon.CLOSE.create());//VaadinIcon.CLOSE.create());
        btnClose.setClassName("dialog-buttons");  ///---------------------------------------- css------------------------------
        btnClose.addClickListener(buttonClickEvent -> this.close());

        //LookUpMgt lookupMgt = new LookUpMgt();


        //Label lblTitle = new Label(caption);

        if (title.equalsIgnoreCase("")) {
            title = "Message";
        }
        Html lblTitle = new Html("<span>" + title + "</span>");


        header.add(lblTitle, btnClose);
        header.setFlexGrow(1, lblTitle);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setClassName("dialog-titlebar");//---------------------------css-----------------
        //header.getStyle().set("background-color",COLOR_DIALOG_TITLE_BACKGROUND);
        //header.getElement().getStyle().set("border","solid 1px");
        //header.getStyle().set("border-color", COLOR_DIALOG_TITLE_BORDER);


        rootLayout = new VerticalLayout();
        add(rootLayout);
        rootLayout.setPadding(false);

        // make the footer break out of the content area of the dialog so it covers the whole bottom
        // area
        footer.getStyle().set("margin", "calc(var(--lumo-space-l) * -1)");
        footer.getStyle().set("margin-top", "var(--lumo-space-l)");
        footer.getStyle().set(ElementConstants.STYLE_WIDTH, "calc(100% + (2 * var(--lumo-space-l)))");
        footer.getStyle().set("box-sizing", "border-box");

        footer.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        footer.getStyle().set("padding", "var(--lumo-space-s) var(--lumo-space-m)");

        rootLayout.add(header, content, footer);

        detailsSeparator.setVisible(false);
        details.setVisible(false);
        details.setWidthFull();

        footer.add(buttonBar, detailsSeparator, details);
    }

    public boolean getContinue() {
        return continueAfterClose;
    }

    public void setContinue(boolean continueIn) {
        continueAfterClose = true;
    }

    /**
     * Sets the given text as title in the header area. Previous content of the header area is
     * removed.
     * <p>
     * the title
     *
     * @return this dialog
     */
    public void setTitle(String titleIn) {
        title = titleIn;
    }

    /**
     * Sets the given icon and text as title in the header area. Previous content of the header area
     * is removed.
     *
     * @param title the title
     * @param icon  the icon
     * @return this dialog
     */
    public DialogMessage setTitle(String title, Component icon) {
        getMessageHeader().removeAll();

        getMessageHeader().getStyle().set("display", "flex");
        getMessageHeader().getStyle().set("align-items", "center");

        if (icon != null) {
            if (!icon.getElement().getStyle().has(ElementConstants.STYLE_WIDTH))
                icon.getElement().getStyle().set(ElementConstants.STYLE_WIDTH, "var(--lumo-size-l)");
            if (!icon.getElement().getStyle().has(ElementConstants.STYLE_HEIGHT))
                icon.getElement().getStyle().set(ElementConstants.STYLE_HEIGHT, "var(--lumo-size-l)");
            icon.getElement().getStyle().set("margin-right", "var(--lumo-space-m)");
            getMessageHeader().add(icon);
        }

        H3 header = new H3(title);
        header.getStyle().set("margin", "0");
        header.getElement().getStyle().set(ElementConstants.STYLE_MAX_WIDTH, "25rem");
        header.getElement().getStyle().set("display", "inline");
        getHeader().add(header);

        return this;
    }

    /**
     * Sets the given text as message in the content area. Previous content of the content area is
     * removed. Newlines will be handled appropriately to actually create line breaks; HTML tags are
     * escaped and not interpreted.
     *
     * @param message the message
     * @return this dialog
     */
    public DialogMessage setMessage(String message) {
        getContent().removeAll();

        Div content = new Div();
        getContent().add(content);
        Element contentElement = content.getElement();

        contentElement.getStyle().set(ElementConstants.STYLE_MAX_WIDTH, "30rem");

        boolean first = true;
        for (String token : message.split("\\r?\\n")) {
            if (first)
                first = false;
            else
                contentElement.appendChild(ElementFactory.createBr());

            contentElement.appendChild(Element.createText(token));
        }

        return this;
    }

    /**
     * Sets the given HTML text as message in the content area. Previous content of the content area
     * is removed. HTML tags are actually intepretet as HTML and not escaped, unlike
     * {@link #setMessage(String)}.
     *
     * @param message the message
     * @return this dialog
     */
    public DialogMessage setMessageAsHtml(String message) {
        getContent().removeAll();

        Div content = new Div();
        getContent().add(content);
        Element contentElement = content.getElement();

        contentElement.getStyle().set(ElementConstants.STYLE_MAX_WIDTH, "30rem");
        String safeHtml = Jsoup.clean(message, Safelist.relaxed());
        contentElement.setProperty("innerHTML", safeHtml);

        return this;
    }

    public DialogMessage setMessageWithHiddenArea(String captionOfHidden, String message) {

        getContent().removeAll();

        Div content = new Div();
        getContent().add(content);

        Accordion accordion = new Accordion();


        Div contentMessage = new Div();
        accordion.add(captionOfHidden, contentMessage);
        getContent().add(accordion);

        Element contentElement = contentMessage.getElement();

        contentElement.getStyle().set(ElementConstants.STYLE_MAX_WIDTH, "30rem");

        boolean first = true;
        for (String token : message.split("\\r?\\n")) {
            if (first)
                first = false;
            else
                contentElement.appendChild(ElementFactory.createBr());

            contentElement.appendChild(Element.createText(token));
        }

        return this;
    }

    /**
     * Adds the header area and returns it.
     *
     * @return the header area
     */
    public HorizontalLayout getMessageHeader() {
        header.setVisible(true);
        return header;
    }

    /**
     * Returns the content area.
     *
     * @return the content area
     */
    public Div getContent() {
        return content;
    }

    /**
     * Returns the footer area which contains the button bar, a separator and a details area by
     * default; the latter two initially invisible.
     *
     * @return the footer area
     */
    public Div getMessageFooter() {
        return footer;
    }

    /**
     * Returns the button bar.
     *
     * @return the button bar
     */
    public ButtonBar getButtonBar() {
        if (!buttonBar.getParent().isPresent())
            throw new IllegalStateException("Button bar has been removed.");

        return buttonBar;
    }

    /**
     * Adds a button to the right side of the button bar and returns it.
     *
     * @return the button
     * @see ButtonBar#addButton()
     * @see ButtonBar#add(Component)
     */
    public FluentButton addButton() {
        return getButtonBar().addButton();
    }

    /**
     * Adds a button to the left side of the button bar and returns it.
     *
     * @return the button
     * @see ButtonBar#addButtonToLeft()
     * @see ButtonBar#addToLeft(Component)
     */
    public FluentButton addButtonToLeft() {
        return getButtonBar().addButtonToLeft();
    }

    /**
     * Adds a button to the middle of the button bar and returns it.
     *
     * @return the button
     * @see ButtonBar#addButtonToMiddle()
     * @see ButtonBar#addToMiddle(Component)
     */
    public FluentButton addButtonToMiddle() {
        return getButtonBar().addButtonToMiddle();
    }


    /**
     * Returns the details area. It's hidden by default.
     *
     * @return the details area
     * @see #setDetailsVisible(boolean)
     */
    public Div getDetails() {
        if (!details.getParent().isPresent())
            throw new IllegalStateException("Details area has been removed.");

        return details;
    }

    /**
     * Returns whether the details area is currently visible.
     *
     * @return whether the details area is currently visible
     */
    public boolean isDetailsVisible() {
        return getDetails().isVisible();
    }

    /**
     * Sets the visibility of the details area.
     *
     * @param visible the new visibility of the details area
     */
    public void setDetailsVisible(boolean visible) {
        getDetails().setVisible(visible);
        if (detailsSeparator != null)
            detailsSeparator.setVisible(visible);
    }


    public class ButtonBar
            extends Composite<HorizontalLayout> {

        private Div leftSpacer = new Div();

        private Div rightSpacer = new Div();


        @Override
        protected HorizontalLayout initContent() {
            HorizontalLayout content = super.initContent();
            content.setWidthFull();
            leftSpacer.setMinWidth("var(--lumo-space-m)");
            rightSpacer.setMinWidth("var(--lumo-space-m)");
            content.add(leftSpacer, rightSpacer);
            content.setFlexGrow(1, leftSpacer);
            content.setFlexGrow(1, rightSpacer);
            return content;
        }


        /**
         * Adds the component to the right side of the button bar.
         *
         * @param <C>       the component type
         * @param component the component to add
         * @return the component
         */
        public <C extends Component> C add(C component) {
            getContent().add(component);
            return component;
        }


        /**
         * Adds the component to the left side of the button bar.
         *
         * @param <C>       the component type
         * @param component the component to add
         * @return the component
         */
        public <C extends Component> C addToLeft(C component) {
            getContent().addComponentAtIndex(getContent().indexOf(leftSpacer), component);
            return component;
        }

        /**
         * Adds the component to the middle of the button bar.
         *
         * @param <C>       the component type
         * @param component the component to add
         * @return the component
         */
        public <C extends Component> C addToMiddle(C component) {
            getContent().addComponentAtIndex(getContent().indexOf(rightSpacer), component);
            return component;
        }


        /**
         * Adds a button to the right side of the button bar and returns it.
         *
         * @return the button
         */
        public FluentButton addButton() {
            return new FluentButton(add(new Button()));
        }

        /**
         * Adds a button to the left side of the button bar and returns it.
         *
         * @return the button
         */
        public FluentButton addButtonToLeft() {
            return new FluentButton(addToLeft(new Button()));
        }

        /**
         * Adds a button to the middle of the button bar and returns it.
         *
         * @return the button
         */
        public FluentButton addButtonToMiddle() {
            return new FluentButton(addToMiddle(new Button()));
        }

    }

    public class FluentButton {

        private Button button;


        public FluentButton(Button button) {
            button.setClassName("dialog-buttons");
            this.button = button;
        }


        public Button getButton() {
            return button;
        }


        public FluentButton text(String text) {
            button.setText(text);
            return this;
        }

        public FluentButton icon(Component icon) {
            button.setIcon(icon);
            return this;
        }

        public FluentButton icon(IconFactory icon) {
            button.setIcon(icon.create());
            return this;
        }

        public FluentButton title(String title) {
            button.getElement().setAttribute("title", title);
            return this;
        }


        public FluentButton variant(ButtonVariant variant) {
            button.addThemeVariants(variant);
            return this;
        }

        public FluentButton primary() {
            return variant(ButtonVariant.LUMO_PRIMARY);
        }

        public FluentButton tertiary() {
            return variant(ButtonVariant.LUMO_TERTIARY);
        }

        public FluentButton error() {
            return variant(ButtonVariant.LUMO_ERROR);
        }

        public FluentButton success() {
            return variant(ButtonVariant.LUMO_SUCCESS);
        }

        public FluentButton contrast() {
            return variant(ButtonVariant.LUMO_CONTRAST);
        }


        public FluentButton onClick(ComponentEventListener<ClickEvent<Button>> clickListener) {
            button.addClickListener(clickListener);
            return this;
        }

        public FluentButton clickShortcut(Key key, KeyModifier... keyModifiers) {
            button.addClickShortcut(key, keyModifiers);
            return this;
        }

        public FluentButton clickShortcutEscape() {
            return clickShortcut(Key.ESCAPE);
        }

        public FluentButton clickShortcutEnter() {
            return clickShortcut(Key.ENTER);
        }

        public FluentButton closeOnClick() {
            button.addClickListener(e -> close());
            return this;
        }

        public FluentButton toggleDetails() {
            button.addClickListener(e -> {
                setDetailsVisible(!isDetailsVisible());
            });
            return this;
        }

        public FluentButton with(Consumer<Button> configurator) {
            configurator.accept(button);
            return this;
        }

    }

}
