package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.Record;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilterTallCard extends RouterLink {

    private static final Logger logger = LoggerFactory.getLogger(FilterTallCard.class);
    //private final RecordService recordService;
    //    private RecordService recordService;
    private boolean isMobile;
    private GenericView genericView;
    private String hostname;
    private String publicIp;
    private long sessionCreation;

    private String dirChar = FileSystems.getDefault().getSeparator();

    private String[] arrAlbumNames;
    private String sqlReadAlbums;
    private String sqlCarousel;
    private String sqlReadAlbumPhotosOrderBy;
    private String[] arrColumnsCarousel;

    public FilterTallCard(Record record, String strImagePath, boolean isMobile, int userId, long sessionCreation,
                          String publicIp, String strCaptionCounts, Component component) {

        this.isMobile = isMobile;
        this.hostname = hostname;
        this.publicIp = publicIp;
        this.sessionCreation = sessionCreation;

        this.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN, TextAlignment.CENTER);


        if (record == null) {
            logger.error("record is null");
        }

        String strCatTitle = record.getColumnData("cat_title");
        String strCatType = record.getColumnData("cat_type");
        String strAlbumUsername = record.getColumnData("username");

        String strCatDescription = record.getColumnData("cat_description_min");
        String strCatTypeDescription = record.getColumnData("cat_type_description_min");

        String strCatTypeCount = record.getColumnData("cat_count");

//        String strAlbumTitle = record.getColumnData("album_title");

//        RouteParam routeUploaderAll = new RouteParam("member", STR_ALL_MEMBERS);

//        RouteParam routeAlbum = new RouteParam("title", strAlbumTitle);
//        RouteParam routeUploader = new RouteParam("member", strUploader);
//        //RouterLink linkUploader = new RouterLink(strUploader, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));
//        RouterLink linkAlbum = new RouterLink(strAlbumTitle, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));

//        String strImagePath = strPath + dirChar; // + strFileName;
        logger.info(" strImagePath " + strImagePath);

        VerticalLayout filterBar = new VerticalLayout();
        if (isMobile) {
            filterBar.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE, Padding.SMALL,
                    Gap.SMALL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
//                Background.CONTRAST_5,
                    TextAlignment.CENTER
            );
        } else {
            filterBar.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE, Padding.MEDIUM,
                    Gap.LARGE,
                    AlignItems.STRETCH, JustifyContent.CENTER,
                    LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
//                Background.CONTRAST_5,
                    TextAlignment.CENTER
            );
        }


        H3 header = new H3();
        header.addClassNames(FontSize.LARGE, FontWeight.SEMIBOLD,
                Width.FULL, TextAlignment.LEFT, AlignItems.CENTER, JustifyContent.CENTER,
                Padding.MEDIUM, Margin.NONE
        );
//        header.getStyle().set("font-family", "Times-New-Roman, serif");
        header.setText(strCatType);
        if (strCatType.trim().isEmpty() || strCatType.equalsIgnoreCase("null")) {
            header.setText("");
            header.setHeight("1px");
            header.setVisible(false);
        }


        Div subtitle = new Div(strCatTypeCount + " " + strCaptionCounts);
        subtitle.addClassNames(FontSize.XSMALL, FontWeight.THIN,
                Width.FULL, TextAlignment.CENTER, AlignItems.CENTER, JustifyContent.CENTER,
                Padding.NONE,
                Margin.NONE
        );
        // subtitle.addClassName("bottom-line");

        filterBar.add(header, subtitle);

        String captionCategory = record.getColumnData("cat_type");
        RouteParam routeCategory = new RouteParam("category", captionCategory);

        this.addClassName("top-tall-layout-filters");
        this.addClassName("hover-shine");
        this.add(filterBar);

        this.setRoute(component.getClass(), new RouteParameters(routeCategory));

    }


    private Image getImage(String strImagePath) {


        Path path = Paths.get(strImagePath);
        File file = path.toFile();

        final StreamResource imageResource = new StreamResource("streamResource", () -> {
            try {
                //ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
                //imageUtilsMeta.printPhotoMetadataValue(file);

                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
                //            logErrorInDb(e, "GalleryImageViewCard StreamResource FileNotFoundException", hostname, publicIp, sessionCreation, file.getAbsolutePath());
                // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                logger.error("FileNotFoundException  " + e.getMessage());
            }
            return null;
        });


        Image image = new Image();
        image.addClassNames(Width.FULL, Height.FULL);
        image.setSrc(imageResource);

        return image;

    }

}
