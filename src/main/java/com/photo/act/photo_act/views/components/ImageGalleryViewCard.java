package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.Record;

import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.ImageUtilsMeta;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.apache.commons.imaging.ImagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageGalleryViewCard extends Div {

    private static final Logger logger = LoggerFactory.getLogger(ImageGalleryViewCard.class);
    private RecordService recordService;

    public ImageGalleryViewCard(Record record, String strImagePath, boolean isMobile, int userId, String strUserName, long sessionCreation, String hostname, String publicIp, RecordService recordService) {
        this.recordService = recordService;



        if(record == null) {
            logger.error("record is null");
        }

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");
        String strUploader = record.getColumnData("uploader");
        String strDateTime = record.getColumnData("meta_date");
        String strCity = "";
        if(!record.getColumnData("city_name").isEmpty()) {
            strCity = record.getColumnData("city_name");
        }
        Path path = Paths.get(strImagePath);
        File file = path.toFile();

        final StreamResource imageResource = new StreamResource("streamResource", () -> {
            try {
                ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
                imageUtilsMeta.printPhotoMetadataValue(file);

                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
                logErrorInDb(e, "ImageGalleryViewCard StreamResource FileNotFoundException", hostname, userId, strUserName, publicIp, sessionCreation, file.getAbsolutePath());
                // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                logger.error("FileNotFoundException  " + e.getMessage());
            }
            return null;
        });

        Div divImage = new Div();

        Image image = new Image();
        image.addClassNames(Width.FULL, Height.FULL);

        image.setSrc(imageResource);
        divImage.add(image);

        VerticalLayout divPhotoInfo = new VerticalLayout();
        divPhotoInfo.addClassNames( Overflow.HIDDEN,AlignItems.CENTER, JustifyContent.BETWEEN, Padding.NONE, Margin.XSMALL, Gap.XSMALL);


        Div divPerson = new Div();
        divPerson.addClassNames(AlignItems.START, JustifyContent.CENTER, AlignItems.CENTER, Padding.XSMALL, Margin.XSMALL);

        Div divUser = new Div();
        divUser.addClassNames(FontSize.SMALL, FontWeight.BOLD);
        divUser.setText(strUploader);
        if(!strUploader.trim().isEmpty() && !strUploader.equalsIgnoreCase("null")) {
            divPerson.add(VaadinIcon.USER_CARD.create(),divUser);
        }

        Div divTextDescription = new Div();
        divTextDescription.addClassNames(AlignItems.CENTER, Width.FULL, JustifyContent.CENTER, AlignItems.CENTER, Padding.XSMALL, Margin.XSMALL);

        Div header = new Div();
        header.addClassNames(FontSize.MEDIUM,  FontWeight.SEMIBOLD, Width.FULL, AlignItems.CENTER,  JustifyContent.CENTER, Padding.XSMALL,
                TextAlignment.CENTER
             //   Margin.Horizontal.XSMALL, Margin.Vertical.NONE
        );
        header.getStyle().set("font-family", "Times-New-Roman, serif");
        header.setText(strTitle);
//        if(!strTitle.trim().isEmpty() && !strTitle.equalsIgnoreCase("null")) {
//            divTextDescription.add(header);
//        }

        Div subtitle = new Div();
        subtitle.addClassNames(FontSize.SMALL,Width.FULL, AlignItems.CENTER,  JustifyContent.CENTER,
                TextAlignment.CENTER,
                Padding.XSMALL,
                Margin.Horizontal.XSMALL
        );
        subtitle.setText(strSubTitle);
        if(!strSubTitle.trim().isEmpty() && !strSubTitle.equalsIgnoreCase("null")) {
            divTextDescription.add(subtitle);
        }


        Span badgePhotoType = new Span();
//        badgePhotoType.getElement().setAttribute("theme", "badge");
        badgePhotoType.getElement().getThemeList().add("badge contrast");
        badgePhotoType.setText(strPhotoType);

        Icon iconLocation = VaadinIcon.LOCATION_ARROW_CIRCLE_O.create();
        iconLocation.getStyle().set("padding", "var(--lumo-space-xs)");
        Span badgeLocation = new Span(iconLocation,new Span(strCity));
       // badgeLocation.getElement().setAttribute("theme", "badge");
        badgeLocation.getElement().getThemeList().add("badge contrast");
        //badgeLocation.setText(strCity);

        Icon iconDateTime = VaadinIcon.CALENDAR_CLOCK.create();
        iconDateTime.getStyle().set("padding", "var(--lumo-space-xs)");
        Span badgeDateTime = new Span(iconDateTime,new Span(strDateTime));
        //badgeDateTime.add(VaadinIcon.TIME_BACKWARD.create());
       // badgeDateTime.getElement().setAttribute("theme", "badge");
        badgeDateTime.getElement().getThemeList().add("badge contrast");
        //badgeDateTime.setText(strDateTime);

//        VerticalLayout divDataDescription = new VerticalLayout();
//        divDataDescription.addClassNames(AlignItems.END, JustifyContent.CENTER, Padding.XSMALL, Margin.XSMALL);
//        if(!strPhotoType.trim().isEmpty() && !strPhotoType.equalsIgnoreCase("null")) {
//            divDataDescription.add(badgePhotoType);
//        }
//        if(!strCity.trim().isEmpty() && !strCity.equalsIgnoreCase("null")) {
//            divDataDescription.add(badgeLocation);
//        }
//        if(!strDateTime.trim().isEmpty() && !strDateTime.equalsIgnoreCase("null")) {
//            divDataDescription.add(badgeDateTime);
//        }




        StreamResource iconLike = new StreamResource("star-empty-icon.svg",
                () -> getClass().getResourceAsStream("/icons/star-empty-icon.svg"));
        SvgIcon svgLike = new SvgIcon(iconLike);
        Button btnLike = new Button(svgLike);

        Div divInfo = new Div("1");
        divInfo.addClassName(TextColor.DISABLED);

        btnLike.setSuffixComponent(divInfo);
        btnLike.setTooltipText("Like It");
        btnLike.addClassName("btn-actions");

        StreamResource iconAction = new StreamResource("testimonial-icon.svg",
                () -> getClass().getResourceAsStream("/icons/testimonial-icon.svg"));
        SvgIcon svgAction = new SvgIcon(iconAction);
        Button btnMoreAction = new Button(VaadinIcon.BOOKMARK.create());//svgAction);
        btnMoreAction.setTooltipText("Save to list");
        btnMoreAction.addClassName("btn-actions");

        Button btnComment = new Button(VaadinIcon.COMMENT.create());
        btnComment.setTooltipText("Comment on it");
        btnComment.setClassName("btn-actions");

        Button btnMoreInfo = new Button(VaadinIcon.INFO.create());//svgAction);
        btnMoreInfo.setTooltipText("More info");
        btnMoreInfo.setClassName("btn-actions");

        StreamResource iconShare = new StreamResource("share-line-icon.svg",
                () -> getClass().getResourceAsStream("/icons/share-line-icon.svg"));
        SvgIcon svgShare = new SvgIcon(iconShare);
        Button btnShare = new Button(svgShare);
        btnShare.setTooltipText("Share it");
        btnShare.setClassName("btn-actions");

        HorizontalLayout layoutActions = new HorizontalLayout();
        layoutActions.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.SMALL,
                Padding.NONE,
                Gap.SMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.LARGE);
        //layoutActions.setWidthFull();
        // layoutActions.addClassNames("btn-actions");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);

        layoutActions.add(btnLike,btnMoreAction, btnComment,btnMoreInfo,btnShare);

        divPhotoInfo.add(divUser,divTextDescription);

        this.addClassNames(JustifyContent.EVENLY);
        this.add(divImage,header,divPhotoInfo,layoutActions);
    }

    public ImageGalleryViewCard(String strUsername, String url, boolean isMobile) {
//        addClassNames(
//                Overflow.HIDDEN,
//                //  Width.FULL,
//                Background.CONTRAST_5, Display.FLEX, FlexDirection.COLUMN,
//                BorderRadius.LARGE,
//                // Margin.Left.NONE, Margin.Right.NONE,
//                Padding.NONE,
//                Margin.NONE
//                // Margin.Left.MEDIUM, Margin.Right.MEDIUM, Margin.Top.XSMALL, Margin.Bottom.XSMALL,
//                //AlignItems.CENTER
//        );
//
//        Div divImage = new Div();
//        divImage.addClassNames( Overflow.HIDDEN, BorderRadius.LARGE);
//
//        Path path = Paths.get(url);
//        File file = path.toFile();
//
//        final StreamResource imageResource = new StreamResource("streamResource", () -> {
//            try {
//                return new FileInputStream(file);
//            } catch (final FileNotFoundException e) {
//                //logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,username,file.getAbsolutePath());
//                logger.error("FileNotFoundException  " + e.getMessage());
//                // e.printStackTrace();
//                return null;
//            }
//        });
//
//        Image image = new Image();
//        image.setWidthFull();
//        image.setHeight("auto");
//        image.setSrc(imageResource);
//        image.addClassNames(BorderRadius.MEDIUM);
//
//        divImage.add(image);
//
//        Span header = new Span();
//        header.addClassNames(FontSize.MEDIUM, TextColor.SECONDARY, FontWeight.SEMIBOLD);
//        header.getStyle().set("font-family", "Times-New-Roman, serif");
//        header.setText("title");
//
//        Span objUser = new Span();
//        objUser.addClassNames(FontSize.SMALL, TextColor.TERTIARY, FontWeight.BOLD);
//        objUser.setText("created by me");
//
//        Span subtitle = new Span();
//        subtitle.addClassNames(FontSize.SMALL, TextColor.TERTIARY);
//        subtitle.setText("Subtitle");
//
//        Span badge = new Span();
//        badge.getElement().setAttribute("theme", "badge");
//        badge.setText("Photo Tag");
//
//        if(isMobile)
//        {
//            this.setWidthFull();
//        } else {
//            divImage.setMaxHeight("500px");
//            divImage.setWidthFull();
//            // this.setMinWidth("400px");
//        }
//
//        VerticalLayout divDescription = new VerticalLayout();
//        divDescription.addClassNames(AlignItems.START, JustifyContent.AROUND, Padding.XSMALL, Margin.XSMALL);
//        divDescription.add( header,objUser, subtitle, badge);
//
//        this.add(divImage,divDescription);
    }

    private void logErrorInDb(Exception e, String function, String hostname, int userId,String strUsername, String publicIp, long sessionCreation, String info) {
        recordService.logErrorInDb(e,hostname,function,userId,strUsername,publicIp,Long.toString(sessionCreation),info);
    }

}
