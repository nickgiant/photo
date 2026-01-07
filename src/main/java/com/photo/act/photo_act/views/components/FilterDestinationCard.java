package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.Record;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.photo.act.photo_act.views.HomeView.subPathSmall;

public class FilterDestinationCard extends RouterLink {

    private static final Logger logger = LoggerFactory.getLogger(FilterDestinationCard.class);
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

    public FilterDestinationCard(Record record, boolean isMobile, int userId, long sessionCreation,
                                 String publicIp, String strPhotoPathOnServer, String strCaptionCounts, Component component) {

        this.isMobile = isMobile;
        this.hostname = hostname;
        this.publicIp = publicIp;
        this.sessionCreation = sessionCreation;

        this.addClassNames(Height.FULL, Width.FULL,
                AlignItems.STRETCH, JustifyContent.BETWEEN, TextAlignment.CENTER);


        if (record == null) {
            logger.error("record is null");
        }

        String strCityName = record.getColumnData("city_name");
        String strPrefecture = record.getColumnData("prefecture");
        String strCountry = record.getColumnData("country");
        String strDestinationType = record.getColumnData("dest_cat_title");
        String strCatTypeCount = record.getColumnData("photo_count");

        String strPhoto = record.getColumnData("name_new");

//        String strAlbumTitle = record.getColumnData("album_title");

//        RouteParam routeUploaderAll = new RouteParam("member", STR_ALL_MEMBERS);

//        RouteParam routeAlbum = new RouteParam("title", strAlbumTitle);
//        RouteParam routeUploader = new RouteParam("member", strUploader);
//        //RouterLink linkUploader = new RouterLink(strUploader, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));
//        RouterLink linkAlbum = new RouterLink(strAlbumTitle, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));

//        String strImagePath = strPath + dirChar; // + strFileName;


        VerticalLayout filterBar = new VerticalLayout();
        if (isMobile) {
            filterBar.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
//                    Margin.NONE, Padding.SMALL,
//                    Gap.XSMALL,
                    AlignItems.STRETCH, JustifyContent.CENTER,
                    FontSize.SMALL, TextColor.SECONDARY,
//                Background.CONTRAST_5,
                    TextAlignment.CENTER
            );
        } else {
            filterBar.addClassNames(
                    Overflow.HIDDEN,
                    // Margin.LARGE, //.Left.MEDIUM, Margin.Right.MEDIUM,
                    //  Padding.Left.MEDIUM, Padding.Left.MEDIUM,
                    // Margin.Horizontal.SMALL,
//                    Margin.NONE, Padding.MEDIUM,
//                    Gap.SMALL,
                    AlignItems.STRETCH, JustifyContent.CENTER,
                    FontSize.SMALL, TextColor.SECONDARY,
//                Background.CONTRAST_5,
                    TextAlignment.CENTER
            );
        }
        filterBar.addClassName("destination-filter");

        String strPath = strPhotoPathOnServer + dirChar + subPathSmall;
        String strImagePath = strPath + dirChar + strPhoto;

        Div divImage = new Div();
        divImage.addClassNames(Width.FULL, Height.AUTO,
                Padding.NONE, Margin.NONE);

        divImage.add(getImage(strImagePath));


        Div divHeader = new Div();
        divHeader.addClassName("filter-title");

        H3 header = new H3();


//        header.getStyle().set("font-family", "Times-New-Roman, serif");
        header.setText(strCityName);
        if (strCityName.trim().isEmpty() || strCityName.equalsIgnoreCase("null")) {
            header.setText("");
            header.setHeight("1px");
            header.setVisible(false);
        }

        Div divDestinationType = new Div(strDestinationType);
        divHeader.add(header, divDestinationType);

        Div divArea = new Div(strPrefecture);

        H4 divCountry = new H4(strCountry);

        H5 subtitle = new H5(strCatTypeCount + " " + strCaptionCounts);

        // subtitle.addClassName("bottom-line");
        Div divFilterCaption = new Div();
        divFilterCaption.addClassName("filter-caption");
        divFilterCaption.add(divArea, divCountry, subtitle);

        filterBar.add(divImage, divHeader, divFilterCaption);

        String captionCategory = record.getColumnData("city_name");
        RouteParam routeCategory = new RouteParam("destination", captionCategory);


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
