package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.ImageUtilsMeta;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
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
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GalleryImageViewCard extends Div {

    private static final Logger logger = LoggerFactory.getLogger(GalleryImageViewCard.class);
    private RecordService recordService;
    private boolean isMobile;
    private GenericView genericView;
    private RouterLink linkUploader;
    private RouterLink linkDestination;

    public GalleryImageViewCard(Record record, String strImagePath, boolean isMobile, int userId, String strUserName, long sessionCreation,
                                String hostname, String publicIp, boolean isEditable, RouterLink linkDestination, RouterLink linkUploader, RecordService recordService) {
        this.recordService = recordService;
        this.isMobile = isMobile;

        this.linkDestination = linkDestination;
        this.linkUploader = linkUploader;

        this.addClassName("gallery-view-card");

        genericView = new GenericView();


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
        String strPhotoTime = record.getColumnData("photo_time");
        String strCreator = record.getColumnData("creator");
        String strVisibleTo = record.getColumnData("visible_to");

        String strSpotName = record.getColumnData("spot_name");
        String strSpotType = record.getColumnData("spot_type");

        String strPhotoUserName = record.getColumnData("username");
        String strPhotoNameOfUser = record.getColumnData("nameOfUser");
        String strPhotoUserResident = record.getColumnData("resident");
        String strAvatar = record.getColumnData("avatar");
        String strPhotoUserJoined = record.getColumnData("date_joined");

        String strCity = record.getColumnData("city_name");
        if (strCity == null || strCity.equalsIgnoreCase("null") || strCity.isEmpty()) {
            strCity = "not defined";
        }
        Path path = Paths.get(strImagePath);
        File file = path.toFile();

        final StreamResource imageResource = new StreamResource("streamResource", () -> {
            try {
                ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
                imageUtilsMeta.printPhotoMetadataValue(file);

                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
                logErrorInDb(e, "GalleryImageViewCard StreamResource FileNotFoundException", hostname, userId, strUserName, publicIp, sessionCreation, file.getAbsolutePath());
                // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                logger.error("FileNotFoundException  " + e.getMessage());
            }
            return null;
        });

        HorizontalLayout layoutImage = new HorizontalLayout();
//        layoutImage.addClassName("id-card");
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
                Padding.NONE, Margin.NONE, //Margin.Top.LARGE,
                Gap.XSMALL,
                BorderRadius.LARGE
//                BoxShadow.SMALL
        );

        if (isMobile) {
//            this.addClassName("gallery-view-card-mobile");
            layoutImage.addClassName("info-panel");
            divPhotoInfo.addClassName("info-panel");
        } else {
            layoutImage.addClassName("info-panel");
            divPhotoInfo.addClassName("info-panel");
//            this.addClassName("bottom-radius-shadow");
        }


        Avatar userAvatar = new Avatar(strPhotoUserName);
        userAvatar.setImage(strAvatar);
        userAvatar.getElement().setAttribute("tabindex", "-1");
        userAvatar.addThemeVariants(AvatarVariant.LUMO_SMALL);

        Avatar userAvatarLarge = new Avatar(strPhotoUserName);
        userAvatarLarge.setImage(strAvatar);
        userAvatarLarge.getElement().setAttribute("tabindex", "-1");
        userAvatarLarge.addThemeVariants(AvatarVariant.LUMO_XLARGE);

        AvatarItem avatarItemMe = new AvatarItem(strPhotoNameOfUser, "", userAvatar);
        Details detailsMember = new Details();
        detailsMember.addClassNames(Width.FULL);
//        detailsMember.addThemeVariants(DetailsVariant.FILLED);
        detailsMember.addClassName("member-small");

        detailsMember.setSummary(avatarItemMe);

        AvatarItem avatarLargeItemMe = new AvatarItem(strPhotoNameOfUser, "@" + strPhotoUserName, userAvatarLarge);

        HorizontalLayout layoutMemberInfo = new HorizontalLayout();
        layoutMemberInfo.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.AROUND,
                Margin.NONE,
                Padding.XSMALL,
                Gap.SMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );

        HorizontalLayout layoutMemberPhotoCount = new HorizontalLayout();
        layoutMemberPhotoCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divMemberPhotoCount = new Div("111");
        layoutMemberPhotoCount.add(FontAwesome.Regular.IMAGES.create(), divMemberPhotoCount);

        HorizontalLayout layoutMemberViewCount = new HorizontalLayout();
        layoutMemberViewCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divMemberViews = new Div("1");
        layoutMemberViewCount.add(FontAwesome.Regular.EYE.create(), divMemberViews);

        HorizontalLayout layoutLocationsCount = new HorizontalLayout();
        layoutLocationsCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divLocations = new Div(strPhotoUserResident);
        layoutLocationsCount.add(FontAwesome.Regular.COMPASS.create(), divLocations);

        HorizontalLayout layoutDateJoined = new HorizontalLayout();
        layoutDateJoined.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divDateJoined = new Div(strPhotoUserJoined);
        layoutDateJoined.add(VaadinIcon.CALENDAR_CLOCK.create(), divDateJoined); // FontAwesome.Regular.CALENDAR.create()

        layoutMemberInfo.add(layoutMemberPhotoCount, layoutMemberViewCount, layoutLocationsCount, layoutDateJoined);

        detailsMember.add(avatarLargeItemMe, layoutMemberInfo);


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

        Div subtitle = new Div();
        subtitle.addClassNames(FontSize.SMALL, Width.FULL, AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER,
                Padding.XSMALL,
                Margin.Horizontal.XSMALL
        );
        subtitle.setText(strSubTitle);
        if (!strSubTitle.trim().isEmpty() && !strSubTitle.equalsIgnoreCase("null")) {
            divTextDescription.add(subtitle);
        }

        Span badgePhotoType = new Span();
//        badgePhotoType.getElement().setAttribute("theme", "badge");
        badgePhotoType.getElement().getThemeList().add("badge contrast");
        badgePhotoType.setText(strPhotoType);

//        Icon iconLocation = VaadinIcon.LOCATION_ARROW_CIRCLE_O.create();
//        iconLocation.getStyle().set("padding", "var(--lumo-space-xs)");
//        Span badgeLocation = new Span(iconLocation, new Span(strCity));
//        // badgeLocation.getElement().setAttribute("theme", "badge");
//        badgeLocation.getElement().getThemeList().add("badge contrast");
        //badgeLocation.setText(strCity);

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

        linkUploader.add(userAvatar);
        linkUploader.addClassName("member-small");

        if (strUploader.trim().isEmpty() || strUploader.equalsIgnoreCase("null")) {
            linkUploader.setText("");
            linkUploader.setVisible(false);
        }

        HorizontalLayout layoutPhotosInfo = new HorizontalLayout();
        layoutPhotosInfo.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.BETWEEN,
                Margin.NONE,
                Padding.MEDIUM,
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
                Padding.XSMALL,
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
                Padding.XSMALL,
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
                Padding.XSMALL,
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
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divDate = new Div(strPhotoDate);
        layoutDate.add(VaadinIcon.CALENDAR.create(), divDate);

        layoutPhotosInfo.add(layoutViewCount, layoutLocationCount, layoutSpot, layoutDate);


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

        // badgeDateTime,linkDestination,
        if (!isEditable) {
            //anyone logged in
            divPhotoInfo.add(header, divTextDescription, detailsMember, layoutPhotosInfo, getActions());
            this.addClassNames(JustifyContent.EVENLY);
            this.add(layoutImage, divPhotoInfo);
        } else {
            // user himself
            divPhotoInfo.add(header, divTextDescription, layoutPhotosInfo, layoutUserActions);
            this.addClassNames(JustifyContent.EVENLY);
            this.add(layoutImage, divPhotoInfo);
        }
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


    public GalleryImageViewCard(String strUsername, String url, boolean isMobile) {
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
