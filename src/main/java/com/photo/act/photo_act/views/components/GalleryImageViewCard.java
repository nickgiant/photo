package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.ImageUtilsMeta;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.photo.act.photo_act.views.HomeView.subPathLarge;

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
    private String strAvailableAlbumsMemberId;

    private String sqlCarousel;
    private String sqlCarouselOrderBy;
    private String[] arrColumnsCarousel;

    private String[] arrDestinationAllNames = {"id", "city_name", "prefecture", "country"};
    private String sqlReadDestinationAll = "SELECT distinct city_name, id, prefecture, country " +
            " FROM destination d " +
            " ORDER BY country ASC, city_name ASC ";

    private String[] arrDestinationAssignedNames = {"id", "city_name", "prefecture", "country"};
    private String sqlReadDestinationAssigned = "SELECT distinct city_name, d.id, prefecture, country " +
            " FROM photo_meta pm LEFT JOIN destination d ON pm.destination_id = d.id " +
            " ORDER BY country ASC, city_name ASC ";


    private String[] arrSubjectNames = {"id", "subject_name", "subject_description", "subject_type"};
    private String sqlReadSubject = "SELECT distinct subject_name, id,  subject_description, subject_type " +
            " FROM subject s " +
            " ORDER BY subject_name ASC ";

    private String[] arrSubjectAssignedNames = {"id", "subject_name", "subject_description", "subject_type"};
    private String sqlReadSubjectAssigned = "SELECT distinct subject_name, s.id, subject_description, subject_type " +
            " FROM photo_meta pm LEFT JOIN subject s ON pm.subject_id = s.id " +
            " ORDER BY subject_name ASC ";

    private String[] arrAlbumNames = new String[]{"user_id", "id", "title", "description", "city_name", "country"};
    private String sqlReadAlbums = "SELECT distinct a.title , a.id, a.description, a.user_id, d.city_name, d.country " +
            " FROM  destination d RIGHT JOIN photo_album a  ON (d.id = a.destination_id )  LEFT JOIN photo_album_photo pap ON (pap.photo_album_id = a.id AND a.user_id = pap.user_id), dbuser usr " +
            " WHERE usr.userId = a.user_id ";
    //     "  AND usr.username = '" + strAlbumUsername + "' " +
    private String sqlReadAlbumsOrderby = " ORDER BY title ASC ";

    public GalleryImageViewCard(Record record, String strImagePath, boolean isMobile, int userId, String strUserName, long sessionCreation,
                                String hostname, String publicIp, boolean isEditable, RecordService recordService, int isType, String sqlCarousel, String sqlCarouselOrderBy,
                                String[] arrColumnsCarousel) {
        this.recordService = recordService;
        this.isMobile = isMobile;
        this.record = record;
        this.strImagePath = strImagePath;
        this.sqlCarousel = sqlCarousel;
        this.sqlCarouselOrderBy = sqlCarouselOrderBy;
        this.arrColumnsCarousel = arrColumnsCarousel;


        this.addClassName("gallery-view-card");

        genericView = new GenericView(recordService);

        this.strAvailableAlbumsMemberId = genericView.checkIfAuthMemberId();

        if (record == null) {
            logger.error("record is null");
        }

        String strPhotoId = record.getColumnData("id");
        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPersonalNotes = record.getColumnData("notes");
        String strPhotoType = record.getColumnData("photo_type");
        String strUploader = record.getColumnData("uploader");
        String strDateTime = record.getColumnData("meta_date");
        String strPhotoDate = record.getColumnData("photo_date");
        String strPhotoTimeShot = record.getColumnData("photo_time_shot");
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

        String strMetaOrientation = record.getColumnData("meta_orientation");
        String strMetaLength = record.getColumnData("meta_i_length");
        String strMetaWidth = record.getColumnData("meta_i_width");
        String strMetaHeight = record.getColumnData("meta_i_height");

        String strPhotoUserName = record.getColumnData("username");
        String strPhotoNameUser = record.getColumnData("name");
        String strPhotoSurnameUser = record.getColumnData("surname");
        String strPhotoUserResident = record.getColumnData("resident");
        String strAvatarPath = record.getColumnData("avatar_path");
        String strPhotoUserJoined = record.getColumnData("date_joined");

        String strUserRights = record.getColumnData("user_rights_id");

        String strCityId = record.getColumnData("destination_id");
        String strSubjectId = record.getColumnData("subject_id");

        String strCity = record.getColumnData("city_name");
        String strSubject = record.getColumnData("subject_name");

        String strDateUploaded = record.getColumnData("date_inserted");

        logger.info(" gallery card city and subject:" + strCity + "_" + strSubject);

        String strAlbumUsername = "";
        String strAlbumUserId = "";
        String strSelection = "";

        if (isType == 1) {
            strAlbumUserId = record.getColumnData("user_id");
            strAlbumUsername = record.getColumnData("username");
            strSelection = record.getColumnData("album_title");
        } else if (isType == 2) {
            strSelection = strCity;
        } else if (isType == 3) {
            strSelection = strSubject;

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

//        HorizontalLayout layoutImage = new HorizontalLayout();
//        layoutImage.addClassNames(
//                Width.FULL, Height.FULL,
//                Border.NONE,// Background.CONTRAST_50,
//                Padding.NONE, Margin.NONE //Margin.Top.LARGE,
//        );

        Div divImage = new Div();
        divImage.addClassNames(Width.FULL, Height.AUTO,
                Padding.NONE, Margin.NONE);

        Image image = new Image();
        image.addClassNames(Width.FULL, Height.FULL,
                Padding.NONE, Margin.NONE);
        image.setSrc(imageResource);
        int intW = 1;
        int intH = 1;
        try {
            intW = Integer.parseInt(strMetaLength);
            intH = Integer.parseInt(strMetaHeight);
        } catch (NumberFormatException e) {

            logger.error(e.getMessage());
        }


        int ratio = intW / intH;

        if (ratio < 0.8) {
            divImage.addClassName("tall");

        } else if (ratio > 1.5) {
            divImage.addClassName("wide");
        }

        if (strMetaOrientation.equalsIgnoreCase("8")) {
            image.getStyle().set("rotate", "-90deg");
            if (isEditable) {
                // image.getStyle().set("scale", "0.66");
            }
        } else if (strMetaOrientation.equalsIgnoreCase("6")) {
            image.getStyle().set("rotate", "90deg");
            if (isEditable) {
                //image.getStyle().set("scale", "0.66");
            }
        } else {
            divImage.addClassName("wide");
            if (isEditable) {
                // image.getStyle().set("scale", "0.85");
            }
        }

        divImage.add(image);


        HorizontalLayout layoutUser = new HorizontalLayout();
        layoutUser.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div h3User = new Div(strPhotoNameUser + " " + strPhotoSurnameUser);
        h3User.addClassNames(Margin.NONE, Padding.XSMALL);

        layoutUser.add(VaadinIcon.USER.create(), h3User);


        VerticalLayout divPhotoInfo = new VerticalLayout();
        divPhotoInfo.addClassNames(TextColor.TERTIARY,
                AlignItems.CENTER, JustifyContent.BETWEEN,
                Padding.NONE, Margin.NONE, //Margin.Top.LARGE,
                Gap.XSMALL
//                BorderRadius.LARGE
//                BoxShadow.SMALL
        );

        if (isMobile) {
//            this.addClassName("gallery-view-card-mobile");
            divImage.addClassName("image-and-info-panel");
            // divPhotoInfo.addClassName("image-and-info-panel");
            // divImage.addClassName("image-and-info-panel-mobile");
            divPhotoInfo.addClassName("image-and-info-panel-mobile");

        } else {
            divImage.addClassName("image-and-info-panel");
            // divPhotoInfo.addClassName("image-and-info-panel");
//            this.addClassName("bottom-radius-shadow");
        }

        Image imgAvatarSmall = genericView.getAvatarImage(strAvatarPath, strPhotoUserName, "40px", "40px");
        Image imgAvatarMedium = genericView.getAvatarImage(strAvatarPath, strPhotoUserName, "70px", "70px");


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


        HorizontalLayout layoutMemberTimeInfo = new HorizontalLayout();
        layoutMemberTimeInfo.addClassNames(
                TextColor.TERTIARY,
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.BETWEEN,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );


        HorizontalLayout layoutSpot = new HorizontalLayout();
        layoutSpot.addClassNames(
                Overflow.HIDDEN,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divSpot = new Div(strCity);
        layoutSpot.add(VaadinIcon.LOCATION_ARROW_CIRCLE_O.create(), divSpot);

        if (strCity == null || strCity.equalsIgnoreCase("null") || strCity.isEmpty()) {
            layoutSpot.setVisible(false);
        }

        HorizontalLayout layoutDateShot = new HorizontalLayout();
        layoutDateShot.addClassNames(
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
        layoutDateShot.add(VaadinIcon.CALENDAR.create(), divDate);


        HorizontalLayout layoutDateUploaded = new HorizontalLayout();
        layoutDateUploaded.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divDateUploaded = new Div(strDateUploaded);
        layoutDateUploaded.add(FontAwesome.Solid.UPLOAD.create(), divDateUploaded);

        HorizontalLayout layoutDateTimeShot = new HorizontalLayout();
        layoutDateTimeShot.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divDateTimeShot = new Div(strPhotoTimeShot);
        layoutDateTimeShot.add(FontAwesome.Solid.CAMERA_ALT.create(), divDateTimeShot);

//        HorizontalLayout layoutPhotoCameraMeta = new HorizontalLayout();
//        layoutPhotoCameraMeta.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
//                AlignItems.CENTER, JustifyContent.EVENLY,
//                Margin.NONE,
//                Padding.XSMALL,
//                Gap.XSMALL,
//                BorderRadius.NONE
//        );
        Div divMetaCamera = new Div(strMetaCameraModel);
        Div divMetaLens = new Div(strMetaLensModel);
//        layoutPhotoCameraMeta.add(divMetaCamera, divMetaLens);

        HorizontalLayout layoutPhotoFocalLength = new HorizontalLayout();

        Div divMetaFocalLengthTitle = new Div("Focal Length:");
        divMetaFocalLengthTitle.addClassNames(TextColor.TERTIARY, Padding.Vertical.NONE, FontSize.XSMALL);
        //SvgIcon svgFL = new SvgIcon(DownloadHandler.forClassResource(this.getClass(), "/icons/like-icon.svg"));
        Div divMetaFocalLength = new Div(strMetaFocalLength + " mm");

        Div divMetaFocalLengthFFTitle = new Div(" ( for Full Frame) ");
        divMetaFocalLengthFFTitle.addClassNames(TextColor.TERTIARY, Padding.Vertical.NONE, FontSize.XSMALL);
        Div divMetaFocalLengthFF = new Div(strMetaFocalLengthFF + " mm");
        if (strMetaFocalLength.equalsIgnoreCase(strMetaFocalLengthFF)) {
            divMetaFocalLengthFF.setVisible(false);
            divMetaFocalLengthFFTitle.setVisible(false);
        }
        layoutPhotoFocalLength.add(divMetaFocalLengthTitle, divMetaFocalLength, divMetaFocalLengthFFTitle, divMetaFocalLengthFF);

//        HorizontalLayout layoutPhotoMeta = new HorizontalLayout();
//        layoutPhotoMeta.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
//                AlignItems.CENTER, JustifyContent.EVENLY,
//                Margin.NONE,
//                Padding.XSMALL,
//                Gap.XSMALL,
//                BorderRadius.NONE
//        );

        Div divApertureTitle = new Div("Aperture:");
//        divApertureTitle.addClassNames(TextColor.TERTIARY, Padding.Vertical.NONE, FontSize.XSMALL);
        Div divMetaAperture = new Div(strMetaAperture);
        if (strMetaAperture.equalsIgnoreCase("null")) {
            divApertureTitle.setVisible(false);
            divMetaAperture.setVisible(false);
        }

        Div divSSTitle = new Div("Shutter Speed:");
//        divSSTitle.addClassNames(TextColor.TERTIARY, Padding.Vertical.NONE, FontSize.XSMALL);
        Div divMetaSS = new Div(strMetaSS + " sec");
        if (strMetaSS.equalsIgnoreCase("null")) {
            divSSTitle.setVisible(false);
            divMetaSS.setVisible(false);
        }

        Div divIsoTitle = new Div("ISO:");
//        divIsoTitle.addClassNames(TextColor.TERTIARY, Padding.Vertical.NONE, FontSize.XSMALL);
        Div divMetaIso = new Div(strMetaIso);

        HorizontalLayout layoutAperture = new HorizontalLayout();
        layoutAperture.add(divApertureTitle, divMetaAperture);


        HorizontalLayout layoutShutterSpeed = new HorizontalLayout();
        layoutShutterSpeed.add(divSSTitle, divMetaSS);

        HorizontalLayout layoutIso = new HorizontalLayout();
        layoutIso.add(divIsoTitle, divMetaIso);
//        layoutPhotoMeta.add(, divSSTitle, divMetaSS, divIsoTitle, divMetaIso);

//        detailsPhotoInfo.add(layoutPhotoCameraMeta, layoutPhotoFocalLength, layoutPhotoMeta);

//        HorizontalLayout layoutDetailsAvatarNActions = new HorizontalLayout();


//        AvatarItem avatarItemMe = new AvatarItem(strPhotoNameUser + " " + strPhotoSurnameUser, "", imgAvatarSmall);
//        avatarItemMe.addClassNames(Padding.NONE, Margin.NONE, AlignItems.CENTER);

        VerticalLayout layoutPhotoDetails = new VerticalLayout();
        layoutPhotoDetails.addClassName("figcaption");
        layoutPhotoDetails.addClassNames(Width.FULL);

        layoutPhotoDetails.add(divMetaCamera, divMetaLens, layoutPhotoFocalLength, layoutAperture, layoutShutterSpeed, layoutIso);

        VerticalLayout layoutInfoPanel = new VerticalLayout();
        layoutInfoPanel.addClassNames(TextColor.BODY, Padding.Vertical.NONE, FontSize.SMALL);
        if (isEditable) {

            layoutInfoPanel.add(layoutUser, layoutDateUploaded, layoutDateTimeShot);
        } else {
            layoutMemberTimeInfo.add(layoutUser, layoutSpot, layoutDateShot);
            layoutInfoPanel.add(layoutMemberTimeInfo);
        }
        divImage.add(layoutPhotoDetails);

//        AvatarItem avatarLargeItemMe = new AvatarItem(strPhotoNameUser + " " + strPhotoSurnameUser, "@" + strPhotoUserName, imgAvatarMedium);
//        avatarLargeItemMe.addClassNames(Width.FULL, Padding.MEDIUM, Margin.NONE);

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
        Div divDateJoined = new Div(strPhotoDate);
        layoutDateJoined.add(VaadinIcon.CALENDAR_CLOCK.create(), divDateJoined); // FontAwesome.Regular.CALENDAR.create()
//        if (isMobile) {
//            layoutMemberInfo.add(layoutMemberPhotoCount, layoutMemberViewCount, layoutLocationsCount, layoutDateJoined);
//            detailsPhotoInfo.add(avatarLargeItemMe, layoutMemberInfo);
//        } else {
//            layoutMemberInfo.add();
//            layoutPhotoDetails.add(layoutPhotoCameraMeta, layoutPhotoFocalLength, layoutPhotoMeta);
//        }


//        Div divTextDescription = new Div();
//        divTextDescription.addClassNames(Width.FULL, JustifyContent.CENTER, AlignItems.CENTER, Padding.NONE, Margin.SMALL);

//        Div header = new Div();
//        header.addClassNames(FontSize.MEDIUM, FontWeight.SEMIBOLD, Width.FULL, AlignItems.CENTER, JustifyContent.CENTER, Padding.XSMALL,
//                TextAlignment.CENTER,
//                Margin.Horizontal.XSMALL, Margin.Vertical.NONE
//        );
//        header.getStyle().set("font-family", "Times-New-Roman, serif");
//        header.setText(strTitle);
//        if (strTitle.trim().isEmpty() || strTitle.equalsIgnoreCase("null")) {
//            header.setText("");
//            header.setHeight("1px");
//            header.setVisible(false);
//        }

        Div subtitle = new Div();
        subtitle.addClassNames(FontSize.SMALL, Width.FULL, AlignItems.CENTER, JustifyContent.CENTER,
                TextAlignment.CENTER,
                Padding.XSMALL,
                Margin.Horizontal.XSMALL
        );
        subtitle.setText(strSubTitle);
        if (strSubTitle.trim().isEmpty() || strSubTitle.equalsIgnoreCase("null")) {
            subtitle.addClassNames(Padding.NONE, Margin.NONE);
        }

        Span badgePhotoType = new Span();
//        badgePhotoType.getElement().setAttribute("theme", "badge");
        badgePhotoType.getElement().getThemeList().add("badge contrast");
        badgePhotoType.setText(strPhotoType);

        if (!isEditable) {
            //anyone logged in
            this.add(divImage, layoutInfoPanel, divPhotoInfo);
            if (isMobile) {
                divPhotoInfo.add(subtitle, getActions(strPhotoId, isType, strSelection, strAlbumUsername));
            } else {
                divPhotoInfo.add(subtitle, getActions(strPhotoId, isType, strSelection, strAlbumUsername));
            }
            //this.addClassNames(JustifyContent.EVENLY);

        } else {
            // user himself
            this.add(divImage, layoutInfoPanel, divPhotoInfo);
            if (isMobile) {
                divPhotoInfo.add(getEditPanel(strPhotoId, strAvailableAlbumsMemberId, strUserRights, strSubTitle, strCityId, strSubjectId, strPersonalNotes));
            } else {
                divPhotoInfo.add(getEditPanel(strPhotoId, strAvailableAlbumsMemberId, strUserRights, strSubTitle, strCityId, strSubjectId, strPersonalNotes));
            }
            // this.addClassNames(JustifyContent.EVENLY);

        }
    }


    private HorizontalLayout getActions(String strPhotoId, int isType, String strSelection, String strAlbumUsername) {

        SvgIcon svgLike = new SvgIcon(DownloadHandler.forClassResource(GalleryImageViewCard.class, "/icons/like-icon.svg"));
        Div divLike = new Div();
        divLike.addClassName("tooltip-container");
        Button btnLike = new Button(svgLike);

        Div tooltipLike = new Div("Like it!");
        tooltipLike.addClassName("tooltip-top");

        Div divLikeInfo = new Div("1");
        divLikeInfo.addClassName(TextColor.DISABLED);

        btnLike.setSuffixComponent(divLikeInfo);
        divLike.add(btnLike, tooltipLike);

        // btnLike.setTooltipText("Like It");

        Checkbox chkLike = new Checkbox();

        Div divLists = new Div();
        divLists.addClassName("tooltip-container");

        Div tooltipLists = new Div("Save to list");
        tooltipLists.addClassName("tooltip-top");

        Div divListsInfo = new Div("");
        divListsInfo.addClassName(TextColor.DISABLED);
        Button btnLists = new Button(VaadinIcon.BOOKMARK.create());//svgAction);
        //btnLists.setTooltipText("Save to list");
        btnLists.setSuffixComponent(divListsInfo);
        divLists.add(btnLists, tooltipLists);


        Div divShare = new Div();
        divShare.addClassName("tooltip-container");

        Div tooltipShare = new Div("Share it");
        tooltipShare.addClassName("tooltip-top");

        Div divSharesInfo = new Div("");
        divSharesInfo.addClassName(TextColor.DISABLED);
        SvgIcon svgShare = new SvgIcon(DownloadHandler.forClassResource(GalleryImageViewCard.class, "/icons/share-line-icon.svg"));
        Button btnShare = new Button(svgShare);
        //btnShare.setTooltipText("Share it");
        btnShare.setSuffixComponent(divSharesInfo);
        divShare.add(btnShare, tooltipShare);

        Div divRate = new Div();
        divRate.addClassName("tooltip-container");

        Div tooltipRate = new Div("Rate it");
        tooltipRate.addClassName("tooltip-top");
        Div divRatesInfo = new Div("");
        divRatesInfo.addClassName(TextColor.DISABLED);
        SvgIcon svgStar = new SvgIcon(DownloadHandler.forClassResource(GalleryImageViewCard.class, "/icons/star-empty-icon.svg"));
        Button btnRate = new Button(svgStar);
        //btnRate.setTooltipText("Rate it");
        btnRate.setSuffixComponent(divRatesInfo);
        divRate.add(btnRate, tooltipRate);

        btnRate.addClickListener(click -> {
                    showDialogWithCarousel(isType, strSelection, strPhotoId, strAlbumUsername, true);
//            click.getSource().getParent()
//            btnMore.getUI().ifPresent(//ui ->
//                //    ui.navigate(LearningsView.class, new RouteParameters(routeTitle))
//           // );
                }
        );

        Div divFullView = new Div();
        divFullView.addClassName("tooltip-container");
        Div tooltipFullView = new Div("View Larger");
        tooltipFullView.addClassName("tooltip-top");
        Button btnMore = new Button("Full View");
        btnMore.setIcon(VaadinIcon.VIEWPORT.create());
//        if (strSelection == null || strSelection.equalsIgnoreCase("null") || strSelection.isEmpty()) {
//            // btnMore.setTooltipText("Larger Photo View");
//            tooltipFullView.setText("Larger Photo View");
//        } else {
//            if (isType == 1) {
//                tooltipFullView.setText("Photos from album: " + strSelection + " by: " + strAlbumUsername);
//            } else if (isType == 2) {
//                tooltipFullView.setText("Photos from location: " + strSelection);
//            } else if (isType == 3) {
//                tooltipFullView.setText("Photos with subject:" + strSelection);
//            }
//        }
        divFullView.add(btnMore, tooltipFullView);

        btnMore.addClickListener(click -> {
                    showDialogWithCarousel(isType, strSelection, strPhotoId, strAlbumUsername, false);
//            click.getSource().getParent()
//            btnMore.getUI().ifPresent(//ui ->
//                //    ui.navigate(LearningsView.class, new RouteParameters(routeTitle))
//           // );
                }
        );


        HorizontalLayout layoutActions = new HorizontalLayout();
        if (isMobile) {
            layoutActions.addClassNames(
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
            layoutActions.add(divLike, btnLists, btnShare);
        } else {
            if (strImagePath.contains(subPathLarge)) {
                layoutActions.add(divLike, divLists, divShare, divRate);
            } else {
                layoutActions.add(divLike, divLists, divShare, divRate, divFullView);
            }
        }
        return layoutActions;
    }


    private void showDialogWithCarousel(int isType, String strSelection, String strPhotoId, String strAlbumUsername, boolean isOnlyRating) {

        String[] arrNames = null;
        String sqlRead = "";


        String strFilterColumn = "";

        if (isType == 1) {
            arrNames = arrAlbumNames;
            sqlRead = sqlReadAlbums + " AND usr.username = '" + strAlbumUsername + "' " + sqlReadAlbumsOrderby;
            strFilterColumn = "a.title";
        } else if (isType == 2) {
            arrNames = arrDestinationAssignedNames;
            sqlRead = sqlReadDestinationAssigned;
            strFilterColumn = "city_name";
        } else if (isType == 3) {
            arrNames = arrSubjectAssignedNames;
            sqlRead = sqlReadSubjectAssigned;
            strFilterColumn = "subject_name";
        } else {
            logger.error(" isType in not defined");

        }


        Dialog dlgCarousel = new Dialog();
        dlgCarousel.setDraggable(true);
        dlgCarousel.setResizable(true);
        dlgCarousel.setWidth("91%");
        dlgCarousel.setHeight("97%");
        dlgCarousel.addClassNames(Overflow.HIDDEN,
                Margin.NONE, Padding.SMALL,
                AlignItems.CENTER, JustifyContent.CENTER,
                BorderRadius.NONE);
        dlgCarousel.setCloseOnOutsideClick(true);
        dlgCarousel.setCloseOnEsc(true);
        dlgCarousel = genericView.showCarouselDialog(isType, sqlCarousel, sqlCarouselOrderBy, arrColumnsCarousel, strSelection, strFilterColumn,
                sqlRead, arrNames, strPhotoId, strAlbumUsername, isOnlyRating);
        dlgCarousel.setWidth("1590px");

        dlgCarousel.open();
    }

    private VerticalLayout getEditPanel(String strPhotoId, String strAvailableAlbumsMemberId, String strUserRights, String strSubTitle, String strCityIdDb, String strSubjectIdDb,
                                        String strPersonalNotes) {

        logger.info(" end destination_Id:" + strCityIdDb + " subject_id:" + strSubjectIdDb);

        VerticalLayout layoutEdit = new VerticalLayout();
        layoutEdit.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.XSMALL,
                Gap.MEDIUM);

        Select<String> cmbPhotoGenre = new Select<>();
        cmbPhotoGenre.setLabel("Genre");
        cmbPhotoGenre.setHelperText("Select the photo genre which the photo belongs.");

        Select<String> cmbDestination = new Select<>();
        cmbDestination.setLabel("Location");
        cmbDestination.setHelperText("Select a location. Avoid to select, when there are identifiable humans.");

        Select<String> cmbSubject = new Select<>();
        cmbSubject.setLabel("Main Subject");
        cmbSubject.setHelperText("Select a subject when is the main object and location can be anywhere.");


        cmbDestination.setWidthFull();
        ArrayList<String> lstDestinations = new ArrayList<>();
        ArrayList<String> lstDestinationsId = new ArrayList<>();

        List<Record> lstDestinationRecs = getRecordsFromDb(sqlReadDestinationAll, arrDestinationAllNames);

        String strDestination = "";
        for (int r = 0; r < lstDestinationRecs.size(); r++) {
            String strDestinationId = "";

            String destination = lstDestinationRecs.get(r).getColumnData("city_name") + " (" + lstDestinationRecs.get(r).getColumnData("country") + ")";
            lstDestinations.add(destination);
            strDestinationId = lstDestinationRecs.get(r).getColumnData("Id");
            lstDestinationsId.add(strDestinationId);
            if (strCityIdDb.equalsIgnoreCase(strDestinationId)) {
                strDestination = destination;
            }
        }
        cmbDestination.setItems(lstDestinations);
        cmbDestination.setValue(strDestination);


        cmbSubject.setWidthFull();
        ArrayList<String> lstSubjects = new ArrayList<>();
        ArrayList<String> lstSubjectsId = new ArrayList<>();

        List<Record> lstSubjectRecs = getRecordsFromDb(sqlReadSubject, arrSubjectNames);

        String strSubject = "";
        for (int r = 0; r < lstSubjectRecs.size(); r++) {
            String strSubjectId = "";

            String subject = lstSubjectRecs.get(r).getColumnData("subject_name");
            lstSubjects.add(subject);
            strSubjectId = lstSubjectRecs.get(r).getColumnData("Id");
            lstSubjectsId.add(strSubjectId);
            if (strSubjectIdDb.equalsIgnoreCase(strSubjectId)) {
                strSubject = subject;
            }
        }
        cmbSubject.setItems(lstSubjects);
        cmbSubject.setValue(strSubject);


        TextArea txtSubtitle = new TextArea("Short Description", "What differentiates this photo from the rest?");
        txtSubtitle.setWidthFull();
        txtSubtitle.setValue(strSubTitle);
        txtSubtitle.setMinRows(4);
        txtSubtitle.setMaxLength(120);

        TextArea txtPersonalNotes = new TextArea("Notes");
        txtPersonalNotes.setHelperText("Notes only visible to you");
        txtPersonalNotes.setWidthFull();
        txtPersonalNotes.setValue(strPersonalNotes);
        txtPersonalNotes.setMinRows(2);
        txtPersonalNotes.setMaxLength(120);

        Button btnAlbums = new Button("Add Photo to Albums ...");
        btnAlbums.setIcon(FontAwesome.Solid.PHOTO_FILM.create());
        btnAlbums.setWidthFull();
        btnAlbums.addClickListener(click -> {
            displayDialogAlbumsOfMember(strAvailableAlbumsMemberId, strPhotoId);

        });

        Button btnSave = new Button("Save");
        btnSave.setIcon(FontAwesome.Regular.SAVE.create());

        btnSave.addClickListener(event -> {

            String strDestinationId = "";

            String strSelectedDestination = cmbDestination.getValue();
            for (int i = 0; i < lstDestinations.size(); i++) {
                if (lstDestinations.get(i).equalsIgnoreCase(strSelectedDestination)) {
                    //event.getSource().setTooltipText(lstDestinationsId.get(i));
                    strDestinationId = lstDestinationsId.get(i);
                }
            }

            String strSubjectId = "";

            String strSelectedSubject = cmbSubject.getValue();
            for (int i = 0; i < lstSubjects.size(); i++) {
                if (lstSubjects.get(i).equalsIgnoreCase(strSelectedSubject)) {
                    strSubjectId = lstSubjectsId.get(i);
                }
            }


            if (!strDestinationId.isEmpty()) {
                String strUpdateDest = "UPDATE photo_meta SET " +
                        " destination_id = '" + strDestinationId + "' " +
                        " WHERE id = '" + strPhotoId + "'";
                recordService.insertOneRecordWithQuery(strUpdateDest, null, null);
            }

            if (!strSubjectId.isEmpty()) {
                String strUpdateSubj = "UPDATE photo_meta SET " +
                        " subject_id = '" + strSubjectId + "' " +
                        " WHERE id = '" + strPhotoId + "'";
                recordService.insertOneRecordWithQuery(strUpdateSubj, null, null);
            }

            String strTxtSubtitle = txtSubtitle.getValue().trim();
            String strTxtPersonalNotes = txtPersonalNotes.getValue().trim();
            Object[] fieldValue = {strTxtSubtitle, strTxtPersonalNotes};
            String[] fieldType = {"java.lang.String", "java.lang.String"};

            String strUpdateSubj = "UPDATE photo_meta SET " +
                    " subtitle = ?, notes = ? " +
                    " WHERE id = '" + strPhotoId + "'";
            int ret = recordService.insertOneRecordWithQuery(strUpdateSubj, fieldValue, fieldType);

            if (ret == 1) {
                String message = "Photo Updated !";
                Notification notification = Notification.show(message, 4000, Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });

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


        Button btnMoreAction = new Button(FontAwesome.Regular.EDIT.create());//svgAction);
        btnMoreAction.setTooltipText("Edit");
        btnMoreAction.addClassName("btn-actions");

        Button btnComment = new Button(VaadinIcon.COMMENT.create());
        btnComment.setTooltipText("Comment on it");

        Button btnMoreInfo = new Button(VaadinIcon.INFO.create());//svgAction);
        btnMoreInfo.setTooltipText("More info");

        layoutUserActions.add(btnSave);//, btnMoreAction, btnComment, btnMoreInfo);

        layoutEdit.add(txtSubtitle, cmbDestination, cmbSubject, txtPersonalNotes, btnAlbums, layoutUserActions);

        return layoutEdit;

    }

    private void displayDialogAlbumsOfMember(String strAlbumUserId, String strPhotoId) {
        Dialog dlg = new Dialog("My Albums");

        String[] arrColumnsMemberAlbums = {"id", "user_id", "title", "description", "album_visible_to", "category_id"
                , "username", "name", "surname", "resident", "date_joined", "member_since", "avatar_path"
        };

        String sqlMemberOfAlbums = "SELECT a.id, a.user_id, a.title, a.description, a.album_visible_to, a.category_id " +
                " , usr.username, usr.name, usr.surname, usr.resident, DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined " +
                " , DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since " +
                " , usr.avatar_path " +
                " FROM dbuser usr, photo_album a " +
                " WHERE a.user_id = usr.userId " +
                " AND a.album_visible_to = 'ALL' ";
        String sqlMemberId = "  AND usr.userId = '" + strAlbumUserId + "' ";
        String sqlMemberOfAlbumsOrderBy = " ORDER BY a.title ASC";


        VerticalLayout layoutAlbumsPanel = new VerticalLayout();
        layoutAlbumsPanel.addClassNames(Width.FULL,
                Padding.LARGE, Margin.NONE,
                AlignItems.CENTER, JustifyContent.CENTER,
                Background.CONTRAST_5, BorderRadius.LARGE);
        layoutAlbumsPanel.setMinWidth("370px");
        layoutAlbumsPanel.setMaxWidth("440px");

        Div divAlbumsCaption = new Div("Add to Albums");
        divAlbumsCaption.addClassNames(TextAlignment.CENTER,
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.MEDIUM, Margin.NONE);

        String sqlMembersAlbums = sqlMemberOfAlbums + sqlMemberId + sqlMemberOfAlbumsOrderBy;

        MultiSelectListBox<String> listBoxAlbums = loadAlbumsInfoPanel(sqlMembersAlbums, arrColumnsMemberAlbums, strAlbumUserId, strPhotoId);
        layoutAlbumsPanel.add(listBoxAlbums);

        List<Record> lstAlbums = getRecordsFromDb(sqlMembersAlbums, arrColumnsMemberAlbums);
        List<String> lstAlbumTitle = new ArrayList<>();
        List<String> lstAlbumUserId = new ArrayList<>();
        List<String> lstAlbumId = new ArrayList<>();
        for (int i = 0; i < lstAlbums.size(); i++) {
            lstAlbumTitle.add(lstAlbums.get(i).getColumnData("title"));
            lstAlbumId.add(lstAlbums.get(i).getColumnData("id"));
            lstAlbumUserId.add(lstAlbums.get(i).getColumnData("user_id"));
        }

        HorizontalLayout layoutControls = new HorizontalLayout();
        layoutControls.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Padding.MEDIUM, Margin.NONE);

        Button btnSave = new Button("Save");
        btnSave.setIcon(FontAwesome.Solid.CHECK.create());
        btnSave.addClickListener(event -> {

            savePhotoInAlbums(listBoxAlbums, lstAlbumTitle, lstAlbumId, lstAlbumUserId, strPhotoId);
            dlg.close();
        });

        Button btnCancel = new Button("Cancel");
        btnCancel.setIcon(FontAwesome.Solid.CLOSE.create());
        btnCancel.addClickListener(event -> {
            dlg.close();
        });
        layoutControls.add(btnSave, btnCancel);


        dlg.add(layoutAlbumsPanel, layoutControls);
        dlg.open();
    }

    private MultiSelectListBox<String> loadAlbumsInfoPanel(String sqlMemberOfAlbums, String[] arrColumnsMemberAlbums,
                                                           String strAlbumUserId, String strPhotoId) {


        List<Record> lstAlbums = getRecordsFromDb(sqlMemberOfAlbums, arrColumnsMemberAlbums);
        List<String> lstAlbumTitle = new ArrayList<>();
        List<String> lstAlbumId = new ArrayList<>();
        for (int i = 0; i < lstAlbums.size(); i++) {
            lstAlbumTitle.add(lstAlbums.get(i).getColumnData("title"));
            lstAlbumId.add(lstAlbums.get(i).getColumnData("id"));
        }


        MultiSelectListBox<String> listBoxAlbums;
        listBoxAlbums = new MultiSelectListBox<>();
        listBoxAlbums.addClassNames(Background.BASE, BorderRadius.SMALL);
        listBoxAlbums.setWidthFull();
        listBoxAlbums.setMinHeight("260px");
        listBoxAlbums.setItems(lstAlbumTitle);

        String[] field = {"album_title"};
        String sqlCountPhotosOfTheAlbum = "SELECT a.title AS album_title, a.user_id, a. album_visible_to, a.description " +
                " " +
                " FROM photo_album_photo pap, photo_album a " +
                " WHERE pap.photo_album_id = a.id AND pap.user_id = a.user_id AND pap.user_id = " + strAlbumUserId + " AND pap.photo_id = " + strPhotoId +
                " ORDER BY a.title ";
        List<Record> lstAlbumsPhotoBelongs = getRecordsFromDb(sqlCountPhotosOfTheAlbum, field);

        ArrayList<String> lstAlbumTitles = new ArrayList<>();
        lstAlbumsPhotoBelongs.forEach(value -> {
            lstAlbumTitles.add(value.getColumnData("album_title"));
        });
        Set<String> setAlbumsPhotoBelongs = new HashSet<>(lstAlbumTitles);


        listBoxAlbums.setValue(setAlbumsPhotoBelongs);


        return listBoxAlbums;
    }

    private boolean savePhotoInAlbums(MultiSelectListBox<String> photoInAlbums, List<String> lstAlbumTitle, List<String> lstAlbumId,
                                      List<String> lstAlbumUserId, String strPhotoId) {


        for (int i = 0; i < photoInAlbums.getSelectedItems().toArray().length; i++) {
            for (int x = 0; x < lstAlbumTitle.size(); x++) {

                if (photoInAlbums.getSelectedItems().toArray()[i].toString().equalsIgnoreCase(lstAlbumTitle.get(x))) {

                    String strAlbumId = lstAlbumId.get(x);
                    String strAlbumUserId = lstAlbumUserId.get(x);

                    Object[] fieldValue = new Object[4];
                    String[] fieldValueType = new String[4];

                    fieldValue[0] = strAlbumUserId;
                    fieldValueType[0] = "java.lang.Integer";
                    fieldValue[1] = strAlbumId;
                    fieldValueType[1] = "java.lang.Integer";
                    fieldValue[2] = strPhotoId;
                    fieldValueType[2] = "java.lang.Integer";

                    String sqlDelete = "DELETE FROM photo_album_photo WHERE `user_id` = " + strAlbumUserId + " AND `photo_album_id` = " + strAlbumId + " AND photo_id = " + strPhotoId;

                    recordService.insertOneRecordWithQuery(sqlDelete, null, null);

                    StringBuilder strInsert = new StringBuilder();

                    String[] field = {"count_photos"};
                    String sqlCountPhotosOfTheAlbum = "SELECT COUNT(PHOTO_ALBUM_ID) AS count_photos FROM photo_album_photo WHERE `user_id` = " + strAlbumUserId + " AND `photo_album_id` = " + strAlbumId + " GROUP BY PHOTO_ALBUM_ID ORDER BY PHOTO_ALBUM_ID";
                    List<Record> lstPhotoCount = getRecordsFromDb(sqlCountPhotosOfTheAlbum, field);
                    String strPhotoCount = "0";
                    if (lstPhotoCount != null && !lstPhotoCount.isEmpty()) {
                        strPhotoCount = lstPhotoCount.get(0).getColumnData("count_photos");
                    }
                    int intPhotoInc = Integer.parseInt(strPhotoCount) + x * 2;
                    logger.info(" -- intPhotoInc:" + intPhotoInc + " strPhotoCount:" + strPhotoCount + " x:" + x);


                    fieldValue[3] = intPhotoInc;
                    fieldValueType[3] = "java.lang.Integer";

                    strInsert.append("INSERT INTO photo_album_photo (`user_id`, `photo_album_id`, `photo_id`, `inc`) VALUES (?, ?, ?, ?)");


                    if (recordService.insertOneRecordWithQuery(strInsert.toString(), fieldValue, fieldValueType) == 1) {

                        String[] fieldCount = {"count_photos"};
                        String sqlCountPhotosOfTheMemberAlbum = "SELECT COUNT(PHOTO_ALBUM_ID) AS count_photos FROM photo_album_photo WHERE `user_id` = " + strAlbumUserId + " AND `photo_album_id` = " + strAlbumId + " GROUP BY PHOTO_ALBUM_ID ORDER BY PHOTO_ALBUM_ID";
                        List<Record> lstAlbumPhotoCount = getRecordsFromDb(sqlCountPhotosOfTheMemberAlbum, fieldCount);
                        String strAlbumPhotoCount = lstAlbumPhotoCount.get(0).getColumnData("count_photos");

                        if (Integer.parseInt(strAlbumPhotoCount) == 1) {
                            String strInsert1and2Photos = "UPDATE photo_album SET `photo_id1` = " + strPhotoId + " WHERE user_id = " + strAlbumUserId + " " +
                                    " AND id = " + strAlbumId;
                            recordService.insertOneRecordWithQuery(strInsert1and2Photos, fieldValue, fieldValueType);
                        } else if (Integer.parseInt(strAlbumPhotoCount) == 2) {
                            String strInsert1and2Photos = "UPDATE photo_album SET `photo_id2` = " + strPhotoId + " WHERE user_id = " + strAlbumUserId + " " +
                                    " AND id = " + strAlbumId;
                            recordService.insertOneRecordWithQuery(strInsert1and2Photos, fieldValue, fieldValueType);
                        } else if (Integer.parseInt(strAlbumPhotoCount) == 3) {
                            String strInsert1and2Photos = "UPDATE photo_album SET `photo_id3` = " + strPhotoId + " WHERE user_id = " + strAlbumUserId + " " +
                                    " AND id = " + strAlbumId;
                            recordService.insertOneRecordWithQuery(strInsert1and2Photos, fieldValue, fieldValueType);
                        } else if (Integer.parseInt(strAlbumPhotoCount) == 4) {
                            String strInsert1and2Photos = "UPDATE photo_album SET `photo_id4` = " + strPhotoId + " WHERE user_id = " + strAlbumUserId + " " +
                                    " AND id = " + strAlbumId;
                            recordService.insertOneRecordWithQuery(strInsert1and2Photos, fieldValue, fieldValueType);
                        }


                    } else {
                        String messageUp = "Photo Not Added on Albums !";
                        Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.MIDDLE);
                        notificationUp.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }

                }
            }
        }

        String messageUp = "Photo Added in  " + photoInAlbums.getSelectedItems().toArray().length + "  Albums !";
        Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.MIDDLE);
        notificationUp.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        logger.info(" length: " + photoInAlbums.getSelectedItems().toArray().length);

        return true;
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

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {

        logger.info(" photo  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

    private void logErrorInDb(Exception e, String function, String hostname, int userId, String strUsername, String publicIp, long sessionCreation, String info) {
        recordService.logErrorInDb(e, hostname, function, userId, strUsername, publicIp, Long.toString(sessionCreation), info);
    }

}