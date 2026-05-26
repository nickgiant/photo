package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.dto.LearningDto;
import com.photo.act.photo_act.services.LearningService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility.*;

import java.util.List;

/**
 * A Grid that shows a member's learnings as {@link LearningHorizontalPanel} rows.
 *
 * Each row renders a full-width horizontal summary card.  The header row is
 * hidden; borders between rows are removed so the result looks like a plain
 * scrollable list.
 *
 * Usage:
 * <pre>
 *   MemberLearningsGrid grid = new MemberLearningsGrid(memberId, learningService, photoBasePath);
 *   parentLayout.add(grid);
 *
 *   // To reload after an edit:
 *   grid.refresh(memberId);
 * </pre>
 */
public class MemberLearningsGrid extends VerticalLayout {

    private final Grid<LearningDto> grid         = new Grid<>();
    private final Div               emptyState   = new Div();
    private final LearningService   learningService;
    private final String            photoBasePath;

    public MemberLearningsGrid(Integer memberId,
                                LearningService learningService,
                                String photoBasePath) {
        this.learningService = learningService;
        this.photoBasePath   = photoBasePath;

        setPadding(false);
        setSpacing(false);
        addClassNames(Width.FULL, Gap.SMALL);

        configureGrid();
        configureEmptyState();

        add(grid, emptyState);

        refresh(memberId);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void configureGrid() {
        grid.addColumn(new ComponentRenderer<>(
                dto -> new LearningHorizontalPanel(dto, photoBasePath)))
            .setAutoWidth(true)
            .setFlexGrow(1);

        // Hide default column header — single visual column, no header needed
        grid.getHeaderRows().forEach(row ->
            row.getCells().forEach(cell -> cell.setComponent(new Span())));

        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS);

        grid.setWidthFull();
        grid.setAllRowsVisible(true);  // expands to fit all rows, no inner scroll bar
    }

    private void configureEmptyState() {
        emptyState.setText("No learnings posted yet.");
        emptyState.addClassNames(
                TextColor.SECONDARY, FontSize.SMALL,
                Padding.MEDIUM, TextAlignment.CENTER,
                Width.FULL);
        emptyState.setVisible(false);
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reloads the grid with learnings posted by the given member.
     * Call this after a create / edit / delete operation.
     */
    public void refresh(Integer memberId) {
        List<LearningDto> items = learningService.getLearningsByUser(memberId);
        grid.setItems(items);
        boolean empty = items.isEmpty();
        grid.setVisible(!empty);
        emptyState.setVisible(empty);
    }

    /** Exposes the underlying Grid for callers that need to attach listeners. */
    public Grid<LearningDto> getGrid() {
        return grid;
    }
}
