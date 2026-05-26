package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.dto.LearningDto;
import com.photo.act.photo_act.services.LearningService;
import com.photo.act.photo_act.services.TutorService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility.*;

import java.util.List;

/**
 * A Grid that shows a member's learnings as {@link LearningHorizontalPanel} rows,
 * with an edit button on every row that opens {@link LearningDialog} in edit mode.
 *
 * Usage:
 * <pre>
 *   MemberLearningsGrid grid = new MemberLearningsGrid(
 *       memberId, learningService, tutorService, photoBasePath);
 *   parentLayout.add(grid);
 *
 *   // To reload after an external create/delete:
 *   grid.refresh(memberId);
 * </pre>
 */
public class MemberLearningsGrid extends VerticalLayout {

    private final Grid<LearningDto> grid            = new Grid<>();
    private final Div               emptyState      = new Div();
    private final LearningService   learningService;
    private final TutorService      tutorService;
    private final String            photoBasePath;
    private Integer                 currentMemberId;

    public MemberLearningsGrid(Integer memberId,
                                LearningService learningService,
                                TutorService tutorService,
                                String photoBasePath) {
        this.learningService  = learningService;
        this.tutorService     = tutorService;
        this.photoBasePath    = photoBasePath;
        this.currentMemberId  = memberId;

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
        // ── Main content column — full-width horizontal panel ────────────────
        grid.addColumn(new ComponentRenderer<>(
                dto -> new LearningHorizontalPanel(dto, photoBasePath)))
            .setAutoWidth(true)
            .setFlexGrow(1);

        // ── Actions column — fixed-width, edit button only ───────────────────
        grid.addColumn(new ComponentRenderer<>(dto -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            editBtn.setTooltipText("Edit learning");
            editBtn.addClickListener(e -> openEditDialog(dto));
            return editBtn;
        }))
        .setWidth("56px")
        .setFlexGrow(0);

        // Suppress all column headers — no label needed for this layout
        grid.getHeaderRows().forEach(row ->
            row.getCells().forEach(cell -> cell.setComponent(new Span())));

        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS);

        grid.setWidthFull();
//        grid.setHeightFull();
        //grid.setAllRowsVisible(true);  // expands to fit all rows, no inner scroll bar
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

    private void openEditDialog(LearningDto dto) {
        new LearningDialog(dto, learningService, tutorService,
                dto.getUserIdPost(), saved -> refresh(currentMemberId))
            .open();
    }

    /**
     * Reloads the grid with learnings posted by the given member.
     * Call this after a create / delete operation from outside this component.
     */
    public void refresh(Integer memberId) {
        this.currentMemberId = memberId;
        List<LearningDto> items = learningService.getLearningsByUser(memberId);
        grid.setItems(items);
        boolean empty = items.isEmpty();
        grid.setVisible(!empty);
        emptyState.setVisible(empty);
    }

    /** Exposes the underlying Grid for callers that need to attach extra listeners. */
    public Grid<LearningDto> getGrid() {
        return grid;
    }
}
