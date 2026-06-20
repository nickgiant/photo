package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.ImageUtilsMeta;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.xdev.vaadin.maps.leaflet.MapContainer;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.raster.LTileLayer;
import software.xdev.vaadin.maps.leaflet.layer.ui.LMarker;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;
import software.xdev.vaadin.maps.leaflet.registry.LDefaultComponentManagementRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class StoryItemViewCard extends Div {

    private static final Logger logger = LoggerFactory.getLogger(StoryItemViewCard.class);
    private RecordService recordService;
    private boolean isMobile;
    private GenericView genericView;
    private RouterLink linkUploader;
    private RouterLink linkDestination;

    private String dirChar = FileSystems.getDefault().getSeparator();

    private Record record;
    private String strImagePath;


    public StoryItemViewCard(Record record, String strImagePath, boolean isMobile, int userId, String strUserName, long sessionCreation,
                             String hostname, String publicIp, boolean isEditable, RecordService recordService) { //, String sqlCarousel, String[] arrColumnsCarousel) {
        this.recordService = recordService;
        this.isMobile = isMobile;
        this.record = record;
        this.strImagePath = strImagePath;


        this.addClassName("story-item-panel");

        genericView = new GenericView(recordService);


        if (record == null) {
            logger.error("record is null");
        }

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");
        String strUploader = record.getColumnData("uploader");
        String strDateTime = record.getColumnData("meta_date");
        String strPhotoDate = record.getColumnData("photo_date");
        String strPhotoTime = record.getColumnData("photo_time_shot");
        String strCreator = record.getColumnData("creator");
        String strVisibleTo = record.getColumnData("visible_to");
        String strItemType = record.getColumnData("item_type");
        String strSpotName = record.getColumnData("spot_name");
        String strSpotType = record.getColumnData("spot_type");

        String strOrgSpaceSize = record.getColumnData("space_size");
        String strMetaCameraModel = record.getColumnData("meta_camera_model");
        String strMetaLensModel = record.getColumnData("meta_lens_model");

        String strMetaFocalLengthFF = record.getColumnData("meta_focal_length_ff");
        String strMetaFocalLength = record.getColumnData("meta_focal_length");
        String strMetaIso = record.getColumnData("meta_iso");
        String strMetaSS = record.getColumnData("meta_shutter_speed");
        String strMetaAperture = record.getColumnData("meta_aperture");

        String strPhotoUserName = record.getColumnData("username");
        String strPhotoNameOfUser = record.getColumnData("username");
        String strPhotoUserResident = record.getColumnData("resident");
        String strAvatarPath = record.getColumnData("avatar_path");
        String strPhotoUserJoined = record.getColumnData("date_joined");

        String strWidth = record.getColumnData("meta_i_width");
        String strHeight = record.getColumnData("meta_i_height");
        String strOrientation = record.getColumnData("meta_orientation");


        String strItemTitle = record.getColumnData("item_title");
        String strItemDescr = record.getColumnData("descr");

        String strCity = record.getColumnData("city_name");
        if (strCity == null || strCity.equalsIgnoreCase("null") || strCity.isEmpty()) {
            strCity = "not defined";
        }
        Path path = Paths.get(strImagePath);
        File file = path.toFile();

/*        final StreamResource imageResource = new StreamResource("streamResource", () -> {
            try {
                ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
                imageUtilsMeta.printPhotoMetadataValue(file);

                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
                logErrorInDb(e, "StoryItemViewCard StreamResource FileNotFoundException", hostname, userId, strUserName, publicIp, sessionCreation, file.getAbsolutePath());
                // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                logger.error("FileNotFoundException  " + e.getMessage());
            }
            return null;
        });*/



        HorizontalLayout layoutImage = new HorizontalLayout();
        layoutImage.addClassNames(
                Border.NONE,// Background.CONTRAST_50,
                Padding.NONE, Margin.NONE //Margin.Top.LARGE,
        );

        Div divImage = new Div();
        divImage.addClassName("photo-item");

        Image image = new Image();
        image.addClassNames(Width.FULL, Height.FULL);
        image.addClassNames(BorderRadius.LARGE);

        image.setSrc( DownloadHandler.forFile(file));
        divImage.add(image);
        layoutImage.add(divImage);




//        logger.info("--> "+strFileName +"       "+strMetaIso+" ."+strMetaFocalLength+". "+strWidth+"  - "+strHeight);
        if(strWidth.isEmpty() || strHeight.isEmpty()){
            divImage.addClassName("photo-item-wide");
        }else {
            int width = Integer.parseInt(strWidth);
            int height = Integer.parseInt(strHeight);
            if (width > height) {
                divImage.addClassName("photo-item-wide");
            } else if (width < height ) {
                divImage.addClassName("photo-item-wide"); // tall
//                logger.warn(width + " --- "+width+"  <  " + height+"  should rotate");
            } else {

            }
        }





        VerticalLayout divPhotoInfo = new VerticalLayout();
        divPhotoInfo.addClassNames(Overflow.HIDDEN, TextColor.TERTIARY,
                AlignItems.CENTER, JustifyContent.BETWEEN,
                Padding.NONE, Margin.NONE, //Margin.Top.LARGE,
                Gap.XSMALL
        );


//        Image imgAvatarSmall = getAvatarImage(strAvatar, strPhotoUserName, "40px", "40px");

        Image imgAvatarSmall = genericView.getAvatarThumbImage(strAvatarPath, strPhotoUserName, "40px", "40px");
        Image imgAvatarMedium = genericView.getAvatarThumbImage(strAvatarPath, strPhotoUserName, "70px", "70px");
//        Image imgAvatarMedium = getAvatarImage(strAvatar, strPhotoUserName, "70px", "70px");


        HorizontalLayout layoutPhotosInfo = new HorizontalLayout();
        layoutPhotosInfo.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.BETWEEN,
                Margin.NONE,
                Padding.NONE,
                Gap.SMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );

        HorizontalLayout layoutViewCount = new HorizontalLayout();
        layoutViewCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divViews = new Div("1");
        layoutViewCount.add(FontAwesome.Regular.EYE.create(), divViews);

        HorizontalLayout layoutLocationCount = new HorizontalLayout();
        layoutLocationCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divLocation = new Div(strCity);
        layoutLocationCount.add(FontAwesome.Regular.COMPASS.create(), divLocation);

        HorizontalLayout layoutSpot = new HorizontalLayout();
        layoutSpot.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divSpot = new Div(strSpotName);
        layoutSpot.add(VaadinIcon.LOCATION_ARROW_CIRCLE_O.create(), divSpot);

        if (strSpotName == null || strSpotName.equalsIgnoreCase("null") || strSpotName.equalsIgnoreCase("") || strSpotName.isEmpty()) {
            layoutSpot.setVisible(false);
        }

        HorizontalLayout layoutDate = new HorizontalLayout();
        layoutDate.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divDate = new Div(strPhotoDate);
        layoutDate.add(VaadinIcon.CALENDAR.create(), divDate);

        layoutPhotosInfo.add(layoutViewCount, layoutLocationCount, layoutSpot, layoutDate);

        Details detailsPhotoInfo = new Details();
        detailsPhotoInfo.addClassNames(Width.FULL);
//        detailsPhotoInfo.addThemeVariants(DetailsVariant.FILLED);
        detailsPhotoInfo.addClassName("photo-meta-info");
        detailsPhotoInfo.setSummary(layoutPhotosInfo);

        HorizontalLayout layoutPhotoCameraMeta = new HorizontalLayout();
        layoutPhotoCameraMeta.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.EVENLY,
                Margin.NONE,
                Padding.MEDIUM,
                Gap.XSMALL,
                BorderRadius.NONE
        );
        Div divMetaCamera = new Div(strMetaCameraModel);
        Div divMetaLens = new Div(strMetaLensModel);
        layoutPhotoCameraMeta.add(divMetaCamera, divMetaLens);

        HorizontalLayout layoutPhotoFocalLength = new HorizontalLayout();
        layoutPhotoFocalLength.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.EVENLY,
                Margin.NONE,
                Padding.MEDIUM,
                Gap.XSMALL,
                BorderRadius.NONE
        );
        Div divMetaFocalLength = new Div("Focal Length " + strMetaFocalLength + " mm");
        Div divMetaFocalLengthFF = new Div("(FF) " + strMetaFocalLengthFF + " mm");
        if (strMetaFocalLength.equalsIgnoreCase(strMetaFocalLengthFF)) {
            divMetaFocalLengthFF.setVisible(false);
        }
        layoutPhotoFocalLength.add(divMetaFocalLength, divMetaFocalLengthFF);

        HorizontalLayout layoutPhotoMeta = new HorizontalLayout();
        layoutPhotoMeta.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.EVENLY,
                Margin.NONE,
                Padding.MEDIUM,
                Gap.XSMALL,
                BorderRadius.NONE
        );


        Div divApertureTitle = new Div("Aperture:");
        divApertureTitle.addClassNames(TextColor.TERTIARY, Padding.Vertical.NONE, FontSize.XSMALL);
        Div divMetaAperture = new Div(strMetaAperture);
        if (strMetaAperture.equalsIgnoreCase("null")) {
            divApertureTitle.setVisible(false);
            divMetaAperture.setVisible(false);
        }

        Div divSSTitle = new Div("Shutter Speed:");
        divSSTitle.addClassNames(TextColor.TERTIARY, Padding.Vertical.NONE, FontSize.XSMALL);
        Div divMetaSS = new Div(strMetaSS + " sec");
        if (strMetaSS.equalsIgnoreCase("null")) {
            divSSTitle.setVisible(false);
            divMetaSS.setVisible(false);
        }

        Div divIsoTitle = new Div("ISO:");
        divIsoTitle.addClassNames(TextColor.TERTIARY, Padding.Vertical.NONE, FontSize.XSMALL);
        Div divMetaIso = new Div(strMetaIso);


        layoutPhotoMeta.add(divApertureTitle, divMetaAperture, divSSTitle, divMetaSS, divIsoTitle, divMetaIso);

        detailsPhotoInfo.add(layoutPhotoCameraMeta, layoutPhotoFocalLength, layoutPhotoMeta);


        Div divTextDescription = new Div();
        divTextDescription.addClassNames(Width.FULL, JustifyContent.CENTER, AlignItems.CENTER, Padding.NONE, Margin.SMALL);

        Div header = new Div();
        header.addClassNames(FontSize.MEDIUM, FontWeight.SEMIBOLD, Width.FULL, AlignItems.CENTER, JustifyContent.CENTER, Padding.XSMALL,
                TextAlignment.CENTER,
                Margin.Horizontal.XSMALL, Margin.Vertical.NONE
        );
        header.getStyle().set("font-family", "Times-New-Roman, serif");
        header.setText(strTitle);
        if (strTitle.trim().isEmpty() || strTitle.equalsIgnoreCase("null")) {
            header.setText("");
            header.setHeight("1px");
            header.setVisible(false);
        }

//        VerticalLayout layoutTextItem = new VerticalLayout();
        H4 divTitle = new H4(strItemTitle);
        Div divDescr = new Div(strItemDescr);
        divTextDescription.add(divTitle,divDescr);

        if(strItemTitle == null || strItemTitle.isEmpty()) {
            divTitle.setVisible(false);
        }

        if(strItemDescr == null || strItemDescr.isEmpty()) {
            divDescr.setVisible(false);
        }

/*        Div description = new Div();
        description.addClassNames(FontSize.SMALL, Width.FULL, AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER,
                Padding.XSMALL,
                Margin.Horizontal.XSMALL
        );
        description.setText(strDescr);
        if (!strDescr.trim().isEmpty() && !strDescr.equalsIgnoreCase("null")) {
            divTextDescription.add(description);
        }*/

        Span badgePhotoType = new Span();
//        badgePhotoType.getElement().setAttribute("theme", "badge");
        badgePhotoType.getElement().getThemeList().add("badge contrast");
        badgePhotoType.setText(strPhotoType);

        HorizontalLayout layoutUserActions = new HorizontalLayout();
        layoutUserActions.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.SMALL,
                Padding.NONE,
                Gap.SMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.LARGE);


        if (isEditable) {
            Button btnMoreAction = new Button(VaadinIcon.EDIT.create());//svgAction);
            btnMoreAction.setTooltipText("Edit");
            btnMoreAction.addClassName("btn-actions");

            Button btnComment = new Button(VaadinIcon.COMMENT.create());
            btnComment.setTooltipText("Comment on it");

            Button btnMoreInfo = new Button(VaadinIcon.INFO.create());//svgAction);
            btnMoreInfo.setTooltipText("More info");

            layoutUserActions.add(btnMoreAction, btnComment, btnMoreInfo);
        }

        if (strFileName == null || strFileName.isEmpty() || strFileName.equalsIgnoreCase("null")) {
            layoutImage.setVisible(false);
        }


        if (strItemType.equalsIgnoreCase("Map")) {
            this.addClassName("map-item");
            final String storyItemIdFinal = record.getColumnData("story_item_id");
            this.addAttachListener(event -> buildMapPanel(storyItemIdFinal));
            return;
        }

        if (strItemType.contains("Header")) {
            divTextDescription.addClassName("header-item");
        } else if (strItemType.contains("Summary")) {
            divTextDescription.addClassName("footer-item");
        } else if (strItemType.contains("Tip")) {
            divTextDescription.addClassName("tip-item");
        } else if (strItemType.contains("Photo")) {
            divTextDescription.addClassName("photo-text-item");
        }else if(strItemType.equalsIgnoreCase("YouTube")){
            divTextDescription.addClassName("video-descr");
        } else {
            divTextDescription.addClassName("text-item");
        }

        if(!strItemTitle.isEmpty() || !strItemDescr.isEmpty()) {
            this.add(divTextDescription);
        }
            // badgeDateTime,linkDestination,
            if (!isEditable) {
                //anyone logged in
                if (isMobile) {
                    divPhotoInfo.add(header, divTextDescription);
                } else {
                    divPhotoInfo.add(header, divTextDescription);
                }
                this.addClassNames(JustifyContent.EVENLY);
                this.add(layoutImage, divPhotoInfo);
            } else {
                // user himself
                if (isMobile) {
                    divPhotoInfo.add(header, divTextDescription, layoutUserActions);
                } else {
                    divPhotoInfo.add(header, divTextDescription, layoutUserActions);
                }
                this.addClassNames(JustifyContent.EVENLY);
                this.add(layoutImage, divPhotoInfo);
            }

    }

    private void buildMapPanel(String strStoryItemId) {
        if (strStoryItemId == null || strStoryItemId.isEmpty() || strStoryItemId.equalsIgnoreCase("null")) {
            return;
        }

        String[] mapCols = {"id", "location_area"};
        String sqlMap = "SELECT id, location_area FROM photo_story_map WHERE story_item_id = " + strStoryItemId;
        List<Record> lstMap = recordService.findAll(sqlMap, mapCols);
        if (lstMap.isEmpty()) return;

        Record mapRecord = lstMap.get(0);
        String mapId = mapRecord.getColumnData("id");
        String locationArea = mapRecord.getColumnData("location_area");

        String[] pointCols = {"point_name", "lat", "lon", "description"};
        String sqlPoints = "SELECT point_name, lat, lon, description FROM photo_story_map_point WHERE map_id = " + mapId + " ORDER BY point_order ASC";
        List<Record> lstPoints = recordService.findAll(sqlPoints, pointCols);
        if (lstPoints.isEmpty()) return;

        if (locationArea != null && !locationArea.isEmpty() && !locationArea.equalsIgnoreCase("null")) {
            H4 areaTitle = new H4(locationArea);
            areaTitle.addClassName("map-area-title");
            this.add(areaTitle);
        }

        final LComponentManagementRegistry reg = new LDefaultComponentManagementRegistry(this);
        final MapContainer mapContainer = new MapContainer(reg);
        mapContainer.addClassName("story-map-container");
        mapContainer.setHeight("400px");
        mapContainer.setWidthFull();

        final LMap lmap = mapContainer.getlMap();
        lmap.addLayer(LTileLayer.createDefaultForOpenStreetMapTileServer(reg));

        double firstLat = 0, firstLon = 0;
        boolean first = true;
        for (Record pt : lstPoints) {
            try {
                double lat = Double.parseDouble(pt.getColumnData("lat"));
                double lon = Double.parseDouble(pt.getColumnData("lon"));
                String name = pt.getColumnData("point_name");
                String desc = pt.getColumnData("description");

                if (first) {
                    firstLat = lat;
                    firstLon = lon;
                    first = false;
                }

                String popup = (name != null && !name.equalsIgnoreCase("null")) ? name : "";
                if (desc != null && !desc.isEmpty() && !desc.equalsIgnoreCase("null")) {
                    popup = popup.isEmpty() ? desc : popup + "<br>" + desc;
                }

                LMarker marker = new LMarker(reg, new LLatLng(reg, lat, lon));
                if (!popup.isEmpty()) {
                    marker.bindPopup(popup);
                }
                marker.addTo(lmap);
            } catch (NumberFormatException ignored) {}
        }

        if (!first) {
            lmap.setView(new LLatLng(reg, firstLat, firstLon), lstPoints.size() == 1 ? 13 : 10);
        }

        this.add(mapContainer);
    }


    private HorizontalLayout getMemberActions() {

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

        StreamResource iconLike = new StreamResource("star-empty-icon.svg",
                () -> getClass().getResourceAsStream("/icons/star-empty-icon.svg"));
        SvgIcon svgLike = new SvgIcon(iconLike);
        MenuItem btnLike = menuBar.addItem(svgLike, "Like It");

        Div divInfo = new Div("1");
        divInfo.addClassName(TextColor.DISABLED);

//        StreamResource iconAction = new StreamResource("stories.svg",
//                () -> getClass().getResourceAsStream("/icons/stories.svg"));
//        SvgIcon svgAction = new SvgIcon(iconAction);
//        MenuItem btnMoreAction = menuBar.addItem(svgAction, "Save to list");

        MenuItem btnComment = menuBar.addItem(VaadinIcon.COMMENT.create(), "Comment on it");

//        MenuItem btnMoreInfo = menuBar.addItem(VaadinIcon.COMMENT.create() ,"More info");

        StreamResource iconShare = new StreamResource("share-line-icon.svg",
                () -> getClass().getResourceAsStream("/icons/share-line-icon.svg"));
        SvgIcon svgShare = new SvgIcon(iconShare);
        MenuItem btnShare = menuBar.addItem(svgShare, "Share it");

        Icon icon = new Icon(VaadinIcon.PENCIL);

        HorizontalLayout layoutActions = new HorizontalLayout();
        if (isMobile) {
            layoutActions.addClassNames(
                    Overflow.HIDDEN, //Width.FULL
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.SMALL,
                    Padding.NONE
//                    Gap.XSMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //   Background.CONTRAST_5,
//                    BorderRadius.LARGE
            );
            layoutActions.addClassName("actions-toolbar");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
        } else {
            layoutActions.addClassNames(
                    Overflow.HIDDEN, //Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.SMALL,
                    Padding.NONE
//                    Gap.LARGE,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //   Background.CONTRAST_5,
//                    BorderRadius.LARGE
            );
            layoutActions.addClassName("actions-toolbar");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
        }
        //layoutActions.setWidthFull();


        layoutActions.add(menuBar);

        return layoutActions;
    }

    private HorizontalLayout getActions() {

        StreamResource iconLike = new StreamResource("star-empty-icon.svg",
                () -> getClass().getResourceAsStream("/icons/star-empty-icon.svg"));
        SvgIcon svgLike = new SvgIcon(iconLike);
        Button btnLike = new Button(svgLike);

        Div divInfo = new Div("1");
        divInfo.addClassName(TextColor.DISABLED);

        btnLike.setSuffixComponent(divInfo);
        btnLike.setTooltipText("Like It");


//        StreamResource iconAction = new StreamResource("stories.svg",
//                () -> getClass().getResourceAsStream("/icons/stories.svg"));
//        SvgIcon svgAction = new SvgIcon(iconAction);
        Button btnMoreAction = new Button(VaadinIcon.BOOKMARK.create());//svgAction);
        btnMoreAction.setTooltipText("Save to list");


        Button btnComment = new Button(VaadinIcon.COMMENT.create());
        btnComment.setTooltipText("Comment on it");

//        Button btnUpload = new Button(VaadinIcon.UPLOAD.create());
//        btnUpload.setTooltipText("Upload your related photos");

        StreamResource iconShare = new StreamResource("share-line-icon.svg",
                () -> getClass().getResourceAsStream("/icons/share-line-icon.svg"));
        SvgIcon svgShare = new SvgIcon(iconShare);
        Button btnShare = new Button(svgShare);
        btnShare.setTooltipText("Share it");


        HorizontalLayout layoutActions = new HorizontalLayout();
        if (isMobile) {
            layoutActions.addClassNames(
                    Overflow.HIDDEN, //Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.SMALL,
                    Padding.NONE
//                    Gap.XSMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //   Background.CONTRAST_5,
//                    BorderRadius.LARGE
            );
            layoutActions.addClassName("actions");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
            layoutActions.addClassName("actions-mobile");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
        } else {
            layoutActions.addClassNames(
                    Overflow.HIDDEN, //Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.SMALL,
                    Padding.NONE
//                    Gap.LARGE,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //   Background.CONTRAST_5,
//                    BorderRadius.LARGE
            );
            layoutActions.addClassName("actions");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
        }

        layoutActions.add(btnLike, btnComment, btnMoreAction, btnShare);

        return layoutActions;
    }

    private HorizontalLayout getActions_as_a_backup() {

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

        StreamResource iconLike = new StreamResource("star-empty-icon.svg",
                () -> getClass().getResourceAsStream("/icons/star-empty-icon.svg"));
        SvgIcon svgLike = new SvgIcon(iconLike);
//        Button btnLike = new Button(svgLike);
        MenuItem btnLike = menuBar.addItem(svgLike, "Like It");

        Div divInfo = new Div("1");
        divInfo.addClassName(TextColor.DISABLED);

//        btnLike.setSuffixComponent(divInfo);
//        btnLike.setTooltipText("Like It");


//        StreamResource iconAction = new StreamResource("stories.svg",
//                () -> getClass().getResourceAsStream("/icons/stories.svg"));
//        SvgIcon svgAction = new SvgIcon(iconAction);
////        Button btnMoreAction = new Button(VaadinIcon.BOOKMARK.create());//svgAction);
////        btnMoreAction.setTooltipText("Save to list");
//        MenuItem btnMoreAction = menuBar.addItem(svgAction, "Save to list");


//        Button btnComment = new Button(VaadinIcon.COMMENT.create());
//        btnComment.setTooltipText("Comment on it");
        MenuItem btnComment = menuBar.addItem(VaadinIcon.COMMENT.create(), "Comment on it");

//        Button btnMoreInfo = new Button(VaadinIcon.INFO_CIRCLE_O.create());
//        btnMoreInfo.setTooltipText("More info");
        MenuItem btnMoreInfo = menuBar.addItem(VaadinIcon.COMMENT.create(), "More info");

        StreamResource iconShare = new StreamResource("share-line-icon.svg",
                () -> getClass().getResourceAsStream("/icons/share-line-icon.svg"));
        SvgIcon svgShare = new SvgIcon(iconShare);
        MenuItem btnShare = menuBar.addItem(svgShare, "Share it");
//        Button btnShare = new Button(svgShare);
//        btnShare.setTooltipText("Share it");


        Icon icon = new Icon(VaadinIcon.PENCIL);


//
//        MenuItem share = genericView.createIconItem(menuBar, VaadinIcon.SHARE, "Share",
//                null);
//        SubMenu shareSubMenu = share.getSubMenu();
//        genericView.createIconItem(shareSubMenu, VaadinIcon.SHARE, "By Email", null, true);
//        genericView.createIconItem(shareSubMenu, VaadinIcon.LINK, "Get link", null, true);
//        genericView.createIconItem(menuBar, VaadinIcon.COPY, null, "duplicate");

        HorizontalLayout layoutActions = new HorizontalLayout();
        if (isMobile) {
            layoutActions.addClassNames(
                    Overflow.HIDDEN, //Width.FULL
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.SMALL,
                    Padding.NONE
//                    Gap.XSMALL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //   Background.CONTRAST_5,
//                    BorderRadius.LARGE
            );
            layoutActions.addClassName("actions-toolbar");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);

        } else {
            layoutActions.addClassNames(
                    Overflow.HIDDEN, //Width.FULL,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.SMALL,
                    Padding.NONE
//                    Gap.LARGE,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //   Background.CONTRAST_5,
//                    BorderRadius.LARGE
            );
            layoutActions.addClassName("actions-toolbar");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
        }
        //layoutActions.setWidthFull();


        layoutActions.add(menuBar);

        return layoutActions;
    }

    private VerticalLayout getPhotoMetaDataLayout(Record record) {

        VerticalLayout layoutMetaData = new VerticalLayout();
//                    ,"space_size","space_size_medium", "space_size_thumb","meta_camera_make", "meta_camera_model","meta_lens_make","meta_lens_model"
//                ,"meta_focal_length", "meta_focal_length_ff", "meta_iso"
//                ,"location_by_user","location_area","location_country_code","location_lat","location_lon"

        return layoutMetaData;
    }

    private void logErrorInDb(Exception e, String function, String hostname, int userId, String strUsername, String publicIp, long sessionCreation, String info) {
        recordService.logErrorInDb(e, hostname, function, userId, strUsername, publicIp, Long.toString(sessionCreation), info);
    }

}
