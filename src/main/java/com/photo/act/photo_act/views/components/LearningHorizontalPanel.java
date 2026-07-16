package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.dto.LearningDto;
import com.photo.act.photo_act.views.LearningsView;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Compact horizontal summary card for a learning/news post.
 *
 * Layout: [newspaper icon | title, tutor, category, format, description excerpt]
 * Shows a "posted N days ago" tag in the top-right corner while the post is
 * less than {@value #RECENT_DAYS_THRESHOLD} days old.
 *
 * The whole card links to the post's page on LearningsView (same URL scheme
 * used by the share buttons there: /news/title/{title}).
 *
 * Drop anywhere in the app — it is a self-contained RouterLink.
 *
 * Example:
 *   LearningHorizontalPanel panel = new LearningHorizontalPanel(dto);
 *   parentLayout.add(panel);
 */
public class LearningHorizontalPanel extends RouterLink {

    private static final int RECENT_DAYS_THRESHOLD = 30;

    public LearningHorizontalPanel(LearningDto dto) {
        super(LearningsView.class, new RouteParameters(new RouteParam("title", nvl(dto.getTitle()))));

        addClassNames(
                Display.FLEX,
                FlexDirection.ROW,
                Position.RELATIVE,
                Width.FULL,
                AlignItems.CENTER,
                Padding.SMALL,
                Gap.MEDIUM,
                Border.ALL, BorderRadius.LARGE
        );

//        getStyle().set("text-decoration", "none").set("color", "inherit");

        VerticalLayout info = buildInfo(dto);
        info.getStyle().set("flex-grow", "1");
        add(buildMediaIcon(), info);

        Span daysAgoTag = buildDaysAgoTag(dto);
        if (daysAgoTag != null) {
            add(daysAgoTag);
        }
    }

    private Div buildMediaIcon() {
        Icon icon = FontAwesome.Solid.NEWSPAPER.create();
        icon.setSize("2em");

        Div mediaBox = new Div(icon);
        mediaBox.addClassNames(
                Display.FLEX, AlignItems.CENTER, JustifyContent.CENTER,
                BorderRadius.MEDIUM, Overflow.HIDDEN, TextColor.SECONDARY
        );
        mediaBox.setWidth("120px");
        mediaBox.setHeight("90px");
        mediaBox.getStyle().set("flex-shrink", "0");
        return mediaBox;
    }

    private Span buildDaysAgoTag(LearningDto dto) {
        if (dto.getDateInsert() == null) {
            return null;
        }
        long daysSincePosted = ChronoUnit.DAYS.between(dto.getDateInsert().toLocalDate(), LocalDate.now());
        if (daysSincePosted < 0 || daysSincePosted >= RECENT_DAYS_THRESHOLD) {
            return null;
        }

        String label = daysSincePosted == 0 ? "Today"
                : daysSincePosted == 1 ? "1 day ago"
                : daysSincePosted + " days ago";

        Span tag = new Span(label);
        tag.addClassNames(Position.ABSOLUTE, Position.Top.XSMALL, Position.End.XSMALL,
                BorderRadius.MEDIUM, FontSize.XSMALL, FontWeight.BOLD, ZIndex.MEDIUM);
        tag.getStyle()
                .set("background-color", "darkred")
                .set("color", "white")
                .set("padding", "0.15em 0.6em")
                .set("white-space", "nowrap");
        return tag;
    }

    private VerticalLayout buildInfo(LearningDto dto) {
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.addClassNames(Gap.XSMALL, AlignItems.START);

        // Title
        H4 title = new H4(dto.getTitle());
        title.addClassNames(Margin.NONE, TextColor.BODY);

        // Tutor
        HorizontalLayout tutorRow = new HorizontalLayout();
        tutorRow.addClassNames(AlignItems.CENTER, Gap.XSMALL, Padding.NONE, Margin.NONE);
        tutorRow.setVisible(false);
        if (isPresent(dto.getTutorName())) {
            tutorRow.add(FontAwesome.Regular.USER.create(),
                         new Span(dto.getTutorName()));
            tutorRow.setVisible(true);
        }

        // Category + format row
        HorizontalLayout metaRow = new HorizontalLayout();
        metaRow.addClassNames(AlignItems.CENTER, Gap.SMALL, Padding.NONE, Margin.NONE,
                              TextColor.SECONDARY, FontSize.SMALL);
        if (isPresent(dto.getCategoryTitle())) {
            metaRow.add(FontAwesome.Solid.TAG.create(), new Span(dto.getCategoryTitle()));
        }
        if (isPresent(dto.getFormat())) {
            Span fmt = new Span(formatLabel(dto));
            fmt.getElement().getThemeList().add("badge contrast");
            metaRow.add(fmt);
        }

        // Description excerpt (max ~120 chars)
        Paragraph desc = new Paragraph(excerpt(dto.getDescription(), 120));
        desc.addClassNames(Margin.NONE, TextColor.SECONDARY, FontSize.SMALL);
        desc.setVisible(isPresent(dto.getDescription()));

        info.add(title, tutorRow, metaRow, desc);
        return info;
    }

    private String formatLabel(LearningDto dto) {
        String fmt = dto.getFormat();
        if ("YouTube".equalsIgnoreCase(fmt) && isPresent(dto.getDuration()))
            return fmt + " (" + dto.getDuration() + ")";
        if ("Book".equalsIgnoreCase(fmt) && isPresent(dto.getPages()))
            return "Book (" + dto.getPages() + " pages)";
        return fmt;
    }

    private static boolean isPresent(String s) {
        return s != null && !s.isBlank() && !"null".equalsIgnoreCase(s);
    }

    private static String excerpt(String text, int max) {
        if (text == null || "null".equalsIgnoreCase(text)) return "";
        return text.length() <= max ? text : text.substring(0, max).stripTrailing() + "…";
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
