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

    public static final String[] STORY_SORT_LABELS = {
        "Time Created", "Most Likes", "Most Views"
    };

    public static final String[] STORY_SORT_SQL_FIELDS = {
        "s.date_inserted",
        "(SELECT COUNT(*) FROM photo_stories_view sv WHERE sv.story_id = s.id AND sv.view_type = 'Like')",
        "(SELECT COUNT(*) FROM photo_stories_view sv WHERE sv.story_id = s.id AND sv.view_type = 'Full')"
    };

    private final String[] sortLabels;
    private final String[] sortSqlFields;

    private boolean ascending = false;
    private int selectedIndex = 0;
    private final Runnable onChange;
    private Button directionBtn;

    public SortOrderBar(Runnable onChange) {
        this(SORT_LABELS, SORT_SQL_FIELDS, onChange);
    }

    public SortOrderBar(String[] sortLabels, String[] sortSqlFields, Runnable onChange) {
        this.sortLabels = sortLabels;
        this.sortSqlFields = sortSqlFields;
        this.onChange = onChange;
        buildUI();
    }

    /** Returns the complete SQL ORDER BY clause based on current selection and direction. */
    public String getSqlOrderBy() {
        return " ORDER BY " + sortSqlFields[selectedIndex] + (ascending ? " ASC" : " DESC");
    }

    private void buildUI() {
        addClassNames(LumoUtility.AlignItems.BASELINE, LumoUtility.Padding.NONE, LumoUtility.Margin.NONE, LumoUtility.Gap.XSMALL);
        addClassName("sort-order-bar");

        Select<String> sortSelect = new Select<>();
        sortSelect.setLabel("Sort by");
        sortSelect.addClassName("sort-select");
        sortSelect.setItems(sortLabels);
        sortSelect.setValue(sortLabels[selectedIndex]);
        sortSelect.addValueChangeListener(e -> {
            for (int i = 0; i < sortLabels.length; i++) {
                if (sortLabels[i].equals(e.getValue())) {
                    selectedIndex = i;
                    break;
                }
            }
            onChange.run();
        });

        directionBtn = new Button(new Icon(VaadinIcon.ARROW_DOWN));
        directionBtn.addClassName("sort-direction-btn");
        directionBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY,  ButtonVariant.LUMO_ICON);
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
