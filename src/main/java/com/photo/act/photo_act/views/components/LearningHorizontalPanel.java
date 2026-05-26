package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.dto.LearningDto;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Compact horizontal summary card for a learning.
 *
 * Layout:  [thumbnail | title, tutor, category, format, description excerpt]
 *
 * Reuses the visual conventions from LearningsView.getLearningItem().
 * Drop anywhere in the app — it is a self-contained HorizontalLayout.
 *
 * Example:
 *   LearningHorizontalPanel panel = new LearningHorizontalPanel(dto, "/home/pi/lazy-photos/");
 *   parentLayout.add(panel);
 */
public class LearningHorizontalPanel extends HorizontalLayout {

    private static final Logger log = LoggerFactory.getLogger(LearningHorizontalPanel.class);

    public LearningHorizontalPanel(LearningDto dto, String photoBasePath) {
        addClassNames(
                Width.FULL,
                AlignItems.CENTER,
                Padding.SMALL,
                Gap.MEDIUM,
                Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE,
                Background.CONTRAST_5
        );
        addClassName("learning-horizontal-panel");

        add(buildMedia(dto, photoBasePath), buildInfo(dto));
        setFlexGrow(1, getComponentAt(1));
    }

    private Div buildMedia(LearningDto dto, String photoBasePath) {
        Div mediaBox = new Div();
        mediaBox.addClassNames(BorderRadius.MEDIUM, Overflow.HIDDEN);
        mediaBox.getStyle().set("flex-shrink", "0");

        String picture = dto.getPicture();
        String url     = dto.getUrl();
        boolean isYouTube = "YouTube".equalsIgnoreCase(dto.getFormat())
                && url != null && !url.isBlank();

        if (isYouTube) {
            String videoId = url.replace("https://www.youtube.com/watch?v=", "");
            String embed   = "<div><iframe class='video-iframe' style='width:160px;height:90px;' " +
                    "src='https://www.youtube.com/embed/" + videoId + "' " +
                    "title='" + escapeHtml(dto.getTitle()) + "' " +
                    "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' " +
                    "allowfullscreen></iframe></div>";
            Html video = new Html(embed);
            mediaBox.add(video);
        } else if (picture != null && !picture.isBlank()) {
            String fullPath = photoBasePath + picture;
            try {
                StreamResource res = new StreamResource("cover",
                        () -> {
                            try { return new FileInputStream(new File(fullPath)); }
                            catch (FileNotFoundException e) { return null; }
                        });
                Image img = new Image(res, dto.getTitle());
                img.setWidth("120px");
                img.setHeight("90px");
                img.getStyle().set("object-fit", "cover");
                img.addClassNames(BorderRadius.MEDIUM);
                mediaBox.add(img);
            } catch (Exception ex) {
                log.debug("Cover not found: {}", fullPath);
                mediaBox.add(buildBookIcon());
            }
        } else {
            mediaBox.add(buildBookIcon());
        }
        return mediaBox;
    }

    private Div buildBookIcon() {
        Div icon = new Div(FontAwesome.Solid.BOOK.create());
        icon.addClassNames(Padding.MEDIUM, TextColor.SECONDARY);
        return icon;
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

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
