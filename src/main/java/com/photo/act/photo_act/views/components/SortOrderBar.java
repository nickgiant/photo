package com.photo.act.photo_act.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Reusable sort-order bar: a compact Select combobox for sort field
 * plus a direction-toggle button. Styled to match the existing filter chips.
 */
public class SortOrderBar extends HorizontalLayout {

    public static final String[] SORT_LABELS = {
        "Time Uploaded", "Time Shot", "Most Likes", "Most Views", "Best Rating"
    };

    private static final String[] SORT_SQL_FIELDS = {
        "pm.date_inserted",
        "pm.meta_date",
        "(SELECT COUNT(*) FROM photo_view pv WHERE pv.photo_id = pm.id AND pv.view_type = 'Like')",
        "(SELECT COUNT(*) FROM photo_view pv WHERE pv.photo_id = pm.id)",
        "(SELECT COALESCE(AVG(pr.rating), 0) FROM photo_rating pr WHERE pr.photo_id = pm.id)"
    };

    private boolean ascending = false;
    private int selectedIndex = 0;
    private final Runnable onChange;
    private Button directionBtn;

    public SortOrderBar(Runnable onChange) {
        this.onChange = onChange;
        buildUI();
    }

    /** Returns the complete SQL ORDER BY clause based on current selection and direction. */
    public String getSqlOrderBy() {
        return " ORDER BY " + SORT_SQL_FIELDS[selectedIndex] + (ascending ? " ASC" : " DESC");
    }

    private void buildUI() {
        addClassName("sort-order-bar");
        setAlignItems(FlexComponent.Alignment.CENTER);
        setSpacing(false);
        addClassNames(LumoUtility.Gap.XSMALL, LumoUtility.Padding.XSMALL);

        Select<String> sortSelect = new Select<>();
        sortSelect.addClassName("sort-select");
        sortSelect.setItems(SORT_LABELS);
        sortSelect.setValue(SORT_LABELS[selectedIndex]);
        sortSelect.addValueChangeListener(e -> {
            for (int i = 0; i < SORT_LABELS.length; i++) {
                if (SORT_LABELS[i].equals(e.getValue())) {
                    selectedIndex = i;
                    break;
                }
            }
            onChange.run();
        });

        directionBtn = new Button(new Icon(VaadinIcon.ARROW_DOWN));
        directionBtn.addClassName("sort-direction-btn");
        directionBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        directionBtn.setTooltipText("Descending");
        directionBtn.addClickListener(e -> {
            ascending = !ascending;
            updateDirectionIcon();
            onChange.run();
        });

        add(sortSelect, directionBtn);
    }

    private void updateDirectionIcon() {
        directionBtn.setIcon(new Icon(ascending ? VaadinIcon.ARROW_UP : VaadinIcon.ARROW_DOWN));
        directionBtn.setTooltipText(ascending ? "Ascending" : "Descending");
    }
}
