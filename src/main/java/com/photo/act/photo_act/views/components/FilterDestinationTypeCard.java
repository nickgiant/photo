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

public class FilterDestinationTypeCard extends RouterLink {

    private static final Logger logger = LoggerFactory.getLogger(FilterDestinationTypeCard.class);
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

    public FilterDestinationTypeCard(Record record, String strImagePath, boolean isMobile, int userId, long sessionCreation,
                                     String publicIp, String strCaptionCounts, Component component) {

        this.isMobile = isMobile;
        this.hostname = hostname;
        this.publicIp = publicIp;
        this.sessionCreation = sessionCreation;

        if (record == null) {
            logger.error("record is null");
        }

        String strDestinationCatTitle = record.getColumnData("dest_cat_title");
//        String strCatType = record.getColumnData("cat_type");
        String strAlbumUsername = record.getColumnData("username");

//        String strCatDescription = record.getColumnData("cat_description_min");
//        String strCatTypeDescription = record.getColumnData("cat_type_description_min");

        String strDestinationCatCount = record.getColumnData("dest_cat_count");

//        String strAlbumTitle = record.getColumnData("album_title");

//        RouteParam routeUploaderAll = new RouteParam("member", STR_ALL_MEMBERS);

//        RouteParam routeAlbum = new RouteParam("title", strAlbumTitle);
//        RouteParam routeUploader = new RouteParam("member", strUploader);
//        //RouterLink linkUploader = new RouterLink(strUploader, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));
//        RouterLink linkAlbum = new RouterLink(strAlbumTitle, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));

//        String strImagePath = strPath + dirChar; // + strFileName;
//        logger.info(" strImagePath " + strImagePath);

        VerticalLayout filterBar = new VerticalLayout();
        if (isMobile) {
            filterBar.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
                    Margin.NONE, Padding.SMALL,
                    Gap.XSMALL,
                    AlignItems.STRETCH, JustifyContent.CENTER,
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
                    Gap.SMALL,
                    AlignItems.STRETCH, JustifyContent.CENTER,
                    LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY,
//                Background.CONTRAST_5,
                    TextAlignment.CENTER
            );
        }


        H3 header = new H3();

//        header.getStyle().set("font-family", "Times-New-Roman, serif");
        header.setText(strDestinationCatTitle);
        if (strDestinationCatTitle.trim().isEmpty() || strDestinationCatTitle.equalsIgnoreCase("null")) {
            header.setText("");
            header.setHeight("1px");
            header.setVisible(false);
        }

        Div divDescription = new Div(strDestinationCatCount + " " + strCaptionCounts);

        // H5 subtitle = new H5(strCatTypeCount + " " + strCaptionCounts);

        // subtitle.addClassName("bottom-line");

        filterBar.add(header, divDescription);

        String captionCategory = record.getColumnData("dest_cat_title");
        RouteParam routeCategory = new RouteParam("destination-type", captionCategory);


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
