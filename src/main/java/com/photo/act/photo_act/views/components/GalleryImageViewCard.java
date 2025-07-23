package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.ImageUtilsMeta;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
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

public class GalleryImageViewCard extends Div {

    private static final Logger logger = LoggerFactory.getLogger(GalleryImageViewCard.class);
    private RecordService recordService;
    private boolean isMobile;
    private GenericView genericView;
    private RouterLink linkUploader;
    private RouterLink linkDestination;

    private String dirChar = FileSystems.getDefault().getSeparator();

    private Record record;
    private String strImagePath;

    private String sqlCarousel;
    private String[] arrColumnsCarousel;

    public GalleryImageViewCard(Record record, String strImagePath, boolean isMobile, int userId, String strUserName, long sessionCreation,
                                String hostname, String publicIp, boolean isEditable, RecordService recordService, String sqlCarousel, String[] arrColumnsCarousel) {
        this.recordService = recordService;
        this.isMobile = isMobile;
        this.record = record;
        this.strImagePath = strImagePath;
        this.sqlCarousel = sqlCarousel;
        this.arrColumnsCarousel = arrColumnsCarousel;


        this.addClassName("gallery-view-card");

        genericView = new GenericView(recordService, userId);


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

        String strOrgSpaceSize = record.getColumnData("space_size");
        String strMetaCameraModel = record.getColumnData("meta_camera_model");
        String strMetaLensModel = record.getColumnData("meta_lens_model");

        String strMetaFocalLengthFF = record.getColumnData("meta_focal_length_ff");
        String strMetaFocalLength = record.getColumnData("meta_focal_length");
        String strMetaIso = record.getColumnData("meta_iso");
        String strMetaSS = record.getColumnData("meta_shutter_speed");
        String strMetaAperture = record.getColumnData("meta_aperture");

        String strPhotoUserName = record.getColumnData("username");
        String strPhotoNameUser = record.getColumnData("name");
        String strPhotoSurnameUser = record.getColumnData("surname");
        String strPhotoUserResident = record.getColumnData("resident");
        String strAvatarPath = record.getColumnData("avatar_path");
        String strPhotoUserJoined = record.getColumnData("date_joined");

        String strCity = record.getColumnData("city_name");

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
        layoutImage.addClassNames(
                Border.NONE,// Background.CONTRAST_50,
                Padding.NONE, Margin.NONE //Margin.Top.LARGE,
        );

        Div divImage = new Div();
        divImage.addClassNames(Width.FULL, Height.AUTO);

        Image image = new Image();
        image.addClassNames(Width.FULL, Height.FULL);


        image.setSrc(imageResource);
        divImage.add(image);

        layoutImage.add(divImage);

        VerticalLayout divPhotoInfo = new VerticalLayout();
        divPhotoInfo.addClassNames(Overflow.HIDDEN, TextColor.TERTIARY,
                AlignItems.CENTER, JustifyContent.BETWEEN,
                Padding.NONE, Margin.NONE, //Margin.Top.LARGE,
                Gap.XSMALL
//                BorderRadius.LARGE
//                BoxShadow.SMALL
        );

        if (isMobile) {
//            this.addClassName("gallery-view-card-mobile");
            layoutImage.addClassName("image-and-info-panel");
            divPhotoInfo.addClassName("image-and-info-panel");
            layoutImage.addClassName("image-and-info-panel-mobile");
            divPhotoInfo.addClassName("image-and-info-panel-mobile");

        } else {
            layoutImage.addClassName("image-and-info-panel");
            divPhotoInfo.addClassName("image-and-info-panel");
//            this.addClassName("bottom-radius-shadow");
        }

//        Image imgAvatarSmall = getAvatarImage(strAvatar, strPhotoUserName, "40px", "40px");

        Image imgAvatarSmall = genericView.getAvatarImage(strAvatarPath, strPhotoUserName, "40px", "40px");
        Image imgAvatarMedium = genericView.getAvatarImage(strAvatarPath, strPhotoUserName, "70px", "70px");
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

        if (strCity == null || strCity.equalsIgnoreCase("null") || strCity.isEmpty()) {
            layoutLocationCount.setVisible(false);
        }

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

        Div divMetaFocalLengthTitle = new Div("Focal Length ");
        divMetaFocalLengthTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaFocalLength = new Div(strMetaFocalLength + " mm");

        Div divMetaFocalLengthFFTitle = new Div("(Full Frame) ");
        divMetaFocalLengthFFTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaFocalLengthFF = new Div(strMetaFocalLengthFF + " mm");
        if (strMetaFocalLength.equalsIgnoreCase(strMetaFocalLengthFF)) {
            divMetaFocalLengthFF.setVisible(false);
            divMetaFocalLengthFFTitle.setVisible(false);
        }
        layoutPhotoFocalLength.add(divMetaFocalLengthTitle, divMetaFocalLength, divMetaFocalLengthFFTitle, divMetaFocalLengthFF);

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
        divApertureTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaAperture = new Div(strMetaAperture);
        if (strMetaAperture.equalsIgnoreCase("null")) {
            divApertureTitle.setVisible(false);
            divMetaAperture.setVisible(false);
        }

        Div divSSTitle = new Div("Shutter Speed:");
        divSSTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaSS = new Div(strMetaSS + " sec");
        if (strMetaSS.equalsIgnoreCase("null")) {
            divSSTitle.setVisible(false);
            divMetaSS.setVisible(false);
        }

        Div divIsoTitle = new Div("ISO:");
        divIsoTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaIso = new Div(strMetaIso);


        layoutPhotoMeta.add(divApertureTitle, divMetaAperture, divSSTitle, divMetaSS, divIsoTitle, divMetaIso);

        detailsPhotoInfo.add(layoutPhotoCameraMeta, layoutPhotoFocalLength, layoutPhotoMeta);

        HorizontalLayout layoutDetailsAvatarNActions = new HorizontalLayout();


        AvatarItem avatarItemMe = new AvatarItem(strPhotoNameUser + " " + strPhotoSurnameUser, "", imgAvatarSmall);
        avatarItemMe.addClassNames(Padding.NONE, Margin.NONE, AlignItems.CENTER);

        Details detailsMember = new Details();
        detailsMember.addClassNames(Width.FULL);
        detailsMember.addClassName("member-small");

        if (isMobile) {
            layoutPhotosInfo.add(layoutViewCount, layoutLocationCount, layoutSpot, layoutDate);
            layoutDetailsAvatarNActions.add(avatarItemMe);
            detailsMember.setSummary(layoutDetailsAvatarNActions);
        } else {

            HorizontalLayout layoutPhotoInfo = new HorizontalLayout(avatarItemMe, layoutViewCount, layoutSpot, layoutDate);
            layoutPhotoInfo.addClassNames(Width.FULL, Padding.NONE, Margin.NONE, AlignItems.CENTER, JustifyContent.AROUND);
            detailsMember.setSummary(layoutPhotoInfo);
        }
        AvatarItem avatarLargeItemMe = new AvatarItem(strPhotoNameUser + " " + strPhotoSurnameUser, "@" + strPhotoUserName, imgAvatarMedium);
        avatarLargeItemMe.addClassNames(Width.FULL, Padding.MEDIUM, Margin.NONE);

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
        if (isMobile) {
            layoutMemberInfo.add(layoutMemberPhotoCount, layoutMemberViewCount, layoutLocationsCount, layoutDateJoined);
            detailsPhotoInfo.add(avatarLargeItemMe, layoutMemberInfo);
        } else {
//            layoutMemberInfo.add();
            detailsMember.add(avatarLargeItemMe, layoutPhotoCameraMeta, layoutPhotoFocalLength, layoutPhotoMeta);
        }


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

        // badgeDateTime,linkDestination,
        if (!isEditable) {
            //anyone logged in
            if (isMobile) {
                divPhotoInfo.add(header, divTextDescription, detailsPhotoInfo, detailsMember, getActions(strCity));
            } else {
                divPhotoInfo.add(header, divTextDescription, detailsMember, getActions(strCity));
            }
            this.addClassNames(JustifyContent.EVENLY);
            this.add(layoutImage, divPhotoInfo);
        } else {
            // user himself
            if (isMobile) {
                divPhotoInfo.add(header, divTextDescription, detailsPhotoInfo, layoutUserActions);
            } else {
                divPhotoInfo.add(header, divTextDescription, layoutUserActions);
            }
            this.addClassNames(JustifyContent.EVENLY);
            this.add(layoutImage, divPhotoInfo);
        }
    }

    public Div getCardForGrid() {
        Div divCard = new Div();


        return divCard;
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

    private HorizontalLayout getActions(String strCity) {

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

        Button btnMore = new Button("View Larger");
        btnMore.setIcon(VaadinIcon.ARROW_RIGHT.create());
        if (strCity == null || strCity.equalsIgnoreCase("null") || strCity.isEmpty()) {
            btnMore.setTooltipText("Larger Photo View");
        } else {
            btnMore.setTooltipText("Larger Photo View with photos from " + strCity);
        }
//        btnMore.addClassName("btn-more");
        btnMore.addClickListener(click -> {
                    showDialogWithCarousel(strCity);
//            click.getSource().getParent()
//            btnMore.getUI().ifPresent(//ui ->
//                //    ui.navigate(LearningsView.class, new RouteParameters(routeTitle))
//           // );
                }
        );


        HorizontalLayout layoutActions = new HorizontalLayout();
        if (isMobile) {
            layoutActions.addClassNames(
                    Overflow.HIDDEN, //Width.FULL,
                    AlignItems.CENTER, JustifyContent.EVENLY,
                    Margin.NONE,
                    Padding.SMALL
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
                    AlignItems.CENTER, JustifyContent.BETWEEN,
                    Margin.NONE,
                    Padding.SMALL
//                    Gap.LARGE,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //   Background.CONTRAST_5,
//                    BorderRadius.LARGE
            );
            layoutActions.addClassName("actions");// AlignItems.STRETCH, JustifyContent.EVENLY ,LumoUtility.Gap.Column.XSMALL);
        }
        //layoutActions.setWidthFull();

        if (isMobile) {
            layoutActions.add(btnLike, btnComment, btnMoreAction, btnShare);
        } else {
            layoutActions.add(btnLike, btnComment, btnMoreAction, btnShare, btnMore);
        }
        return layoutActions;
    }

    private void showDialogWithCarousel(String strCity) {

        Dialog dlgCarousel = new Dialog();
        dlgCarousel.setDraggable(true);
        dlgCarousel.setResizable(true);
        dlgCarousel.setWidth("91%");
        dlgCarousel.setHeight("97%");
        dlgCarousel.addClassNames(LumoUtility.Overflow.HIDDEN,
                Margin.NONE, Padding.SMALL,
                AlignItems.CENTER, JustifyContent.CENTER,
                BorderRadius.LARGE);
        dlgCarousel.setCloseOnOutsideClick(true);
        dlgCarousel.setCloseOnEsc(true);
        dlgCarousel.add(genericView.loadCarouselWithThumbnails(sqlCarousel, arrColumnsCarousel, strCity));

        dlgCarousel.open();
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
