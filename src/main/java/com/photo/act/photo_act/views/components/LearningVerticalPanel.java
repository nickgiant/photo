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
 * Compact vertical summary card for a learning.
 *
 * Layout:
 *   [thumbnail / video]
 *   [title]
 *   [tutor, category badge, format badge]
 *   [description excerpt]
 *   [external links]
 *
 * Reuses the visual conventions from LearningsView.getLearningItem().
 * Drop anywhere in the app — it is a self-contained VerticalLayout.
 *
 * Example:
 *   LearningVerticalPanel panel = new LearningVerticalPanel(dto, "/home/pi/lazy-photos/");
 *   parentLayout.add(panel);
 */
public class LearningVerticalPanel extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(LearningVerticalPanel.class);

    public LearningVerticalPanel(LearningDto dto, String photoBasePath) {
        setPadding(false);
        setSpacing(false);
        addClassNames(
                AlignItems.CENTER,
                Gap.SMALL,
                Padding.MEDIUM,
                Border.ALL, BorderColor.CONTRAST_10, BorderRadius.LARGE,
                Background.CONTRAST_5
        );
        addClassName("learning-vertical-panel");
        setMaxWidth("320px");

        add(buildMedia(dto, photoBasePath));
        add(buildTitle(dto));
        add(buildMeta(dto));
        add(buildDescription(dto));
        add(buildLinks(dto));
    }

    private Div buildMedia(LearningDto dto, String photoBasePath) {
        Div mediaBox = new Div();
        mediaBox.addClassNames(BorderRadius.MEDIUM, Overflow.HIDDEN);
        mediaBox.setWidthFull();

        String picture = dto.getPicture();
        String url     = dto.getUrl();
        boolean isYouTube = "YouTube".equalsIgnoreCase(dto.getFormat())
                && url != null && !url.isBlank();

        if (isYouTube) {
            String videoId = url.replace("https://www.youtube.com/watch?v=", "");
            String embed   = "<div><iframe class='video-iframe' style='width:100%;max-width:280px;aspect-ratio:16/9;' " +
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
                img.setWidthFull();
                img.setMaxWidth("280px");
                img.setMaxHeight("200px");
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

    private H4 buildTitle(LearningDto dto) {
        H4 title = new H4(dto.getTitle());
        title.addClassNames(Margin.NONE, TextColor.BODY, TextAlignment.CENTER);
        return title;
    }

    private VerticalLayout buildMeta(LearningDto dto) {
        VerticalLayout meta = new VerticalLayout();
        meta.setPadding(false);
        meta.setSpacing(false);
        meta.addClassNames(Gap.XSMALL, AlignItems.CENTER);

        // Tutor
        if (isPresent(dto.getTutorName())) {
            HorizontalLayout tutorRow = new HorizontalLayout();
            tutorRow.addClassNames(AlignItems.CENTER, Gap.XSMALL,
                                   Padding.NONE, Margin.NONE, TextColor.SECONDARY);
            tutorRow.add(FontAwesome.Regular.USER.create(), new Span(dto.getTutorName()));
            meta.add(tutorRow);
        }

        // Category + format badges
        HorizontalLayout badges = new HorizontalLayout();
        badges.addClassNames(AlignItems.CENTER, Gap.XSMALL,
                             JustifyContent.CENTER, Padding.NONE, Margin.NONE);
        badges.getStyle().set("flex-wrap", "wrap");

        if (isPresent(dto.getCategoryTitle())) {
            HorizontalLayout catRow = new HorizontalLayout();
            catRow.addClassNames(AlignItems.CENTER, Gap.XSMALL);
            catRow.add(FontAwesome.Solid.TAG.create(), new Span(dto.getCategoryTitle()));
            badges.add(catRow);
        }
        if (isPresent(dto.getFormat())) {
            Span fmt = new Span(formatLabel(dto));
            fmt.getElement().getThemeList().add("badge contrast");
            badges.add(fmt);
        }
        if (dto.getPublished() != null) {
            Span year = new Span(String.valueOf(dto.getPublished().getYear()));
            year.getElement().getThemeList().add("badge");
            badges.add(year);
        }
        meta.add(badges);
        return meta;
    }

    private Paragraph buildDescription(LearningDto dto) {
        Paragraph desc = new Paragraph(excerpt(dto.getDescription(), 160));
        desc.addClassNames(Margin.NONE, TextColor.SECONDARY, FontSize.SMALL, TextAlignment.CENTER);
        desc.setVisible(isPresent(dto.getDescription()));
        return desc;
    }

    private HorizontalLayout buildLinks(LearningDto dto) {
        HorizontalLayout links = new HorizontalLayout();
        links.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                            Gap.MEDIUM, Padding.NONE, Margin.NONE);
        links.setVisible(false);

        if (isPresent(dto.getTutorWebsite())) {
            Anchor a = new Anchor(dto.getTutorWebsite(), FontAwesome.Solid.LINK.create());
            a.setTarget("_blank");
            links.add(a);
            links.setVisible(true);
        }
        if (isPresent(dto.getTutorUrlYt())) {
            Anchor a = new Anchor(dto.getTutorUrlYt(), FontAwesome.Brands.YOUTUBE.create());
            a.setTarget("_blank");
            links.add(a);
            links.setVisible(true);
        }
        if (isPresent(dto.getTutorUrlInsta())) {
            Anchor a = new Anchor(dto.getTutorUrlInsta(), FontAwesome.Brands.INSTAGRAM.create());
            a.setTarget("_blank");
            links.add(a);
            links.setVisible(true);
        }
        if (isPresent(dto.getTutorUrlWikipedia())) {
            Anchor a = new Anchor(dto.getTutorUrlWikipedia(), FontAwesome.Brands.WIKIPEDIA_W.create());
            a.setTarget("_blank");
            links.add(a);
            links.setVisible(true);
        }
        return links;
    }

    private Div buildBookIcon() {
        Div icon = new Div(FontAwesome.Solid.BOOK.create());
        icon.addClassNames(Padding.LARGE, TextColor.SECONDARY);
        icon.setWidthFull();
        icon.getStyle().set("text-align", "center");
        return icon;
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
