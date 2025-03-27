package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.views.ImageAlbumsView;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

public class AlbumViewCard extends RouterLink {

    private static final Logger logger = LoggerFactory.getLogger(AlbumViewCard.class);
    private RecordService recordService;
    private boolean isMobile;
    private GenericView genericView;

    private String dirChar = FileSystems.getDefault().getSeparator();

    public AlbumViewCard(Record record, String strImagePath, boolean isMobile, int userId, String strUserName, long sessionCreation,
                         String hostname, String publicIp, boolean isEditable, RecordService recordService) {
        this.recordService = recordService;
        this.isMobile = isMobile;


        this.addClassName("album-info-card");

        genericView = new GenericView();


        if (record == null) {
            logger.error("record is null");
        }

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strDescription = record.getColumnData("description");
        String strUploader = record.getColumnData("uploader");
        String strAlbumPhotoCount = record.getColumnData("album_photo_count").toString();
        int intAlbumPhotoCount = Integer.parseInt(strAlbumPhotoCount);
        String strCreator = record.getColumnData("creator");
        String strVisibleTo = record.getColumnData("visible_to");
        String strPhotoUrl = record.getColumnData("name_new");

        String strPhoto1 = record.getColumnData("photo_1");
        String strPhoto2 = record.getColumnData("photo_2");

        String strCity = "";
        if (!record.getColumnData("city_name").isEmpty()) {
            strCity = record.getColumnData("city_name");
        }


        RouteParam routeAlbum = new RouteParam("title", strTitle);
        RouteParam routeUploader = new RouteParam("member", strUploader);
        //RouterLink linkUploader = new RouterLink(strUploader, ImageAlbumsView.class, new RouteParameters(routeAlbum, routeUploader));
        //RouterLink linkAlbum = new RouterLink(strTitle, ImageAlbumsView.class, new RouteParameters(routeAlbum, routeUploader));

        this.setRoute(ImageAlbumsView.class, new RouteParameters(routeAlbum, routeUploader));

        if (strPhoto1 != null && !strPhoto1.isEmpty() && !strPhoto1.equalsIgnoreCase("null")) {
            strPhotoUrl = strPhoto1;
        }

        String imagePath = strImagePath + dirChar + strPhotoUrl;
        Path path = Paths.get(imagePath);
        File file = path.toFile();

        final StreamResource imageResource = new StreamResource("streamResource", () -> {
            try {
                //ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
                //imageUtilsMeta.printPhotoMetadataValue(file);

                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
                logErrorInDb(e, "ImageGalleryViewCard StreamResource FileNotFoundException", hostname, userId, strUserName, publicIp, sessionCreation, file.getAbsolutePath());
                // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                logger.error("FileNotFoundException  " + e.getMessage());
            }
            return null;
        });

        HorizontalLayout layoutImage = new HorizontalLayout();
        layoutImage.addClassNames(
                Padding.NONE, Border.NONE,// Background.CONTRAST_50,
                BorderRadius.LARGE
//                BoxShadow.SMALL
        );

        Div divImage = new Div();

        Image image = new Image();
        image.addClassNames(Width.FULL, Height.FULL);
        image.addClassNames(BorderRadius.LARGE);

        image.setSrc(imageResource);
        divImage.add(image);

        layoutImage.add(divImage);


        VerticalLayout divPhotoInfo = new VerticalLayout();
        divPhotoInfo.addClassNames(Overflow.HIDDEN, TextColor.TERTIARY,
                AlignItems.CENTER, JustifyContent.BETWEEN,
                Padding.SMALL, Margin.NONE, //Margin.Top.LARGE,
                Gap.XSMALL,
                BorderRadius.LARGE
        );

//            layoutImage.addClassName("info-panel");
//            divPhotoInfo.addClassName("info-panel");


        Avatar userAvatar = new Avatar(strUserName);
        userAvatar.setImage("https://randomuser.me/api/portraits/men/17.jpg");
        userAvatar.getElement().setAttribute("tabindex", "-1");
        userAvatar.addThemeVariants(AvatarVariant.LUMO_SMALL);

        AvatarItem avatarItemMe = new AvatarItem(strUserName, "", userAvatar);


        Div divTextDescription = new Div();
        divTextDescription.addClassNames(Width.FULL, JustifyContent.CENTER, AlignItems.CENTER, Padding.NONE, Margin.SMALL);

        H4 header = new H4();
        header.addClassNames(FontSize.LARGE, FontWeight.SEMIBOLD, Width.FULL, AlignItems.CENTER, JustifyContent.CENTER, Padding.MEDIUM,
                TextAlignment.CENTER,
                Margin.Horizontal.MEDIUM, Margin.Vertical.NONE
        );
        header.getStyle().set("font-family", "Times-New-Roman, serif");
        header.setText(strTitle);
        if (strTitle.trim().isEmpty() || strTitle.equalsIgnoreCase("null")) {
            header.setText("");
            header.setHeight("1px");
            header.setVisible(false);
        }

        Div subtitle = new Div();
        subtitle.addClassNames(FontSize.SMALL, Width.FULL, AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER,
                Padding.NONE,
                Margin.Horizontal.NONE
        );

        if (!strDescription.trim().isEmpty() && !strDescription.equalsIgnoreCase("null")) {
            subtitle.setText(strDescription + " (" + intAlbumPhotoCount + ")");
        } else {
            subtitle.setText("(" + intAlbumPhotoCount + " photos)");
        }
        divTextDescription.add(subtitle);

//        Icon iconDateTime = VaadinIcon.CALENDAR_CLOCK.create();
//        iconDateTime.getStyle().set("padding", "var(--lumo-space-xs)");
//        Span badgeDateTime = new Span(iconDateTime, new Span(strDateTime));
//        if (strDateTime.trim().isEmpty() || strDateTime.equalsIgnoreCase("null")) {
//            badgeDateTime.setText("");
//            badgeDateTime.setVisible(false);
//        }
//        badgeDateTime.getElement().getThemeList().add("badge contrast");

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


//        if (strUploader.trim().isEmpty() || strUploader.equalsIgnoreCase("null")) {
//            linkUploader.setText("");
//            linkUploader.setVisible(false);
//        }
//
//        if (!strCity.isEmpty()) {
//            linkDestination.setVisible(true);
//
//        } else {
//            linkDestination.setVisible(false);
//        }


        Button btnMoreAction = new Button(VaadinIcon.EDIT.create());//svgAction);
        btnMoreAction.setTooltipText("Edit");
        btnMoreAction.addClassName("btn-actions");

        Button btnComment = new Button(VaadinIcon.COMMENT.create());
        btnComment.setTooltipText("Comment on it");

        Button btnMoreInfo = new Button(VaadinIcon.INFO.create());//svgAction);
        btnMoreInfo.setTooltipText("More info");

        layoutUserActions.add(btnMoreAction, btnComment, btnMoreInfo);


        // user himself
        divPhotoInfo.add(header, subtitle); //, layoutUserActions);
        this.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);
        this.add(layoutImage, divPhotoInfo);

    }

    private HorizontalLayout getActions() {

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

        StreamResource iconLike = new StreamResource("star-empty-icon.svg",
                () -> getClass().getResourceAsStream("/icons/star-empty-icon.svg"));
        SvgIcon svgLike = new SvgIcon(iconLike);
        MenuItem btnLike = menuBar.addItem(svgLike, "Like It");

        Div divInfo = new Div("1");
        divInfo.addClassName(TextColor.DISABLED);

        StreamResource iconAction = new StreamResource("testimonial-icon.svg",
                () -> getClass().getResourceAsStream("/icons/testimonial-icon.svg"));
        SvgIcon svgAction = new SvgIcon(iconAction);
        MenuItem btnMoreAction = menuBar.addItem(svgAction, "Save to list");

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


        StreamResource iconAction = new StreamResource("testimonial-icon.svg",
                () -> getClass().getResourceAsStream("/icons/testimonial-icon.svg"));
        SvgIcon svgAction = new SvgIcon(iconAction);
//        Button btnMoreAction = new Button(VaadinIcon.BOOKMARK.create());//svgAction);
//        btnMoreAction.setTooltipText("Save to list");
        MenuItem btnMoreAction = menuBar.addItem(svgAction, "Save to list");


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
