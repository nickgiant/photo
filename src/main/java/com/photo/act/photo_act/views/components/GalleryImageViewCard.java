package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.model.ShareType;
import com.photo.act.photo_act.model.ShareableResource;
import com.photo.act.photo_act.services.PhotoRatingService;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.photo.act.photo_act.services.WeatherService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.photo.act.photo_act.views.HomeView.subPathLarge;
import static com.photo.act.photo_act.views.MainLayout.baseUrl;

public class GalleryImageViewCard extends Div {



    private static final Logger logger = LoggerFactory.getLogger(GalleryImageViewCard.class);
    private RecordService recordService;
    private ShareService shareService;
    private ShareMetricService shareMetricService;
    private PhotoRatingService photoRatingService;

    private final WeatherService weatherService;
    private LocalWeatherForecast weatherForecast;


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

    private String[] arrGenreNames = {"id", "title"};
    private String sqlReadGenre = "SELECT id,  title " +
            " FROM  photo_genres " +
            " ORDER BY title ASC ";

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

    private boolean isTypeProfile = false;

    public GalleryImageViewCard(Record record, String strImagePath, boolean isMobile, int userId, String strUserName, long sessionCreation,
                                String hostname, String publicIp, boolean isEditable, RecordService recordService, int isType, String sqlCarousel, String sqlCarouselOrderBy,
                                String[] arrColumnsCarousel, ShareService shareService, ShareMetricService shareMetricService, WeatherService weatherService,
                                PhotoRatingService photoRatingService) {
        this.recordService = recordService;
        this.shareService = shareService;
        this.shareMetricService = shareMetricService;
        this.photoRatingService = photoRatingService;
        this.isMobile = isMobile;
        this.record = record;
        this.strImagePath = strImagePath;
        this.sqlCarousel = sqlCarousel;
        this.sqlCarouselOrderBy = sqlCarouselOrderBy;
        this.arrColumnsCarousel = arrColumnsCarousel;
        this.weatherService = weatherService;


        this.addClassName("gallery-view-card");

        genericView = new GenericView(recordService);
        genericView.setPhotoRatingService(photoRatingService);

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
//        String strMetaLength = record.getColumnData("meta_i_length");
//        String strMetaWidth = record.getColumnData("meta_i_width");
//        String strMetaHeight = record.getColumnData("meta_i_height");

        String strPhotoUserName = record.getColumnData("username");
        String strPhotoNameUser = record.getColumnData("name");
        String strPhotoSurnameUser = record.getColumnData("surname");
        String strPhotoUserResident = record.getColumnData("resident");
        String strAvatarPath = record.getColumnData("avatar_path");
        String strPhotoUserJoined = record.getColumnData("date_joined");

        String strUserRights = record.getColumnData("user_rights_id");

        String strGenreId = record.getColumnData("genre_id");
        String strCityId = record.getColumnData("destination_id");
        String strSubjectId = record.getColumnData("subject_id");

        String strCity = record.getColumnData("city_name");
        String strSubject = record.getColumnData("subject_name");

        String strDateUploaded = record.getColumnData("date_inserted");
        String strDateUploadedRelative = record.getColumnData("date_inserted_diff_from_now");

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





//        HorizontalLayout layoutImage = new HorizontalLayout();
//        layoutImage.addClassNames(
//                Width.FULL, Height.FULL,
//                Border.NONE,// Background.CONTRAST_50,
//                Padding.NONE, Margin.NONE //Margin.Top.LARGE,
//        );

        Div divImage = new Div();
        divImage.addClassName("image-container");

        Image image = new Image();
        image.setSrc(DownloadHandler.forFile(file));

        String strMetaLength = record.getColumnData("meta_i_length");
        String strMetaWidth = record.getColumnData("meta_i_width");
        String strMetaHeight = record.getColumnData("meta_i_height");
        int intW = 1;
        int intH = 1;
        try {
            intW = Integer.parseInt(strMetaLength);
            intH = Integer.parseInt(strMetaHeight);
        } catch (NumberFormatException e) {

            logger.error(e.getMessage());
        }
        try {

            if(intW!=0 && intH!= 0) {
                    int ratio = intW / intH;
                    if (ratio < 0.8) {
                        divImage.addClassName("portrait");
                    } else if (ratio > 1.5) {
                        divImage.addClassName("landscape");
                    }else{
                        divImage.addClassName("square");
                    }
                        }
        }catch (ArithmeticException e){
            logger.error(e.getMessage());
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


        VerticalLayout layoutPhotographer = fetchPhotoCreator(record,false);

        Popover popover = new Popover();
        popover.setTarget(layoutUser);
        popover.setOpenOnClick(false);
        popover.setOpenOnHover(true);
        popover.setHoverDelay(400);
        popover.setHideDelay(100);

        popover.setWidth("300px");
        popover.addThemeVariants(PopoverVariant.ARROW,
                PopoverVariant.LUMO_NO_PADDING);
        popover.setPosition(PopoverPosition.TOP);
        popover.setModal(true);
        popover.setAriaLabelledBy("member-popup");
        popover.add(layoutPhotographer);

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

        HorizontalLayout layoutDateRelUploaded = new HorizontalLayout();
        layoutDateRelUploaded.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divDateRelUploaded = new Div(strDateUploadedRelative);
        layoutDateRelUploaded.add(FontAwesome.Solid.UPLOAD.create(), divDateRelUploaded);


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
        layoutDateShot.add(FontAwesome.Solid.CAMERA_ALT.create(), divDate);

        HorizontalLayout layoutDateTimeUploaded = new HorizontalLayout();
        layoutDateTimeUploaded.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divDateTimeUploaded = new Div(strDateUploaded);
        layoutDateTimeUploaded.add(FontAwesome.Solid.UPLOAD.create(), divDateTimeUploaded);

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
            layoutInfoPanel.add(popover,layoutUser, layoutDateTimeUploaded, layoutDateTimeShot);
        } else {
            layoutMemberTimeInfo.add(popover,layoutUser, layoutSpot, layoutDateRelUploaded);
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
                divPhotoInfo.add(subtitle, getActions(strPhotoId, strSubTitle,strFileName, strCity,isType, strSelection, strAlbumUsername));
            } else {
                divPhotoInfo.add(subtitle, getActions(strPhotoId, strSubTitle,strFileName,strCity,isType, strSelection, strAlbumUsername));
            }
            //this.addClassNames(JustifyContent.EVENLY);

        } else {
            // user himself
            this.add(divImage, layoutInfoPanel, divPhotoInfo);
            if (isMobile) {
                divPhotoInfo.add(getEditPanel(strPhotoId, strAvailableAlbumsMemberId, strUserRights, strSubTitle,strGenreId, strCityId, strSubjectId, strPersonalNotes));
            } else {
                divPhotoInfo.add(getEditPanel(strPhotoId, strAvailableAlbumsMemberId, strUserRights, strSubTitle, strGenreId, strCityId, strSubjectId, strPersonalNotes));
            }
            // this.addClassNames(JustifyContent.EVENLY);

        }
    }


    private HorizontalLayout getActions(String strPhotoId, String strSubTitle, String strFileName, String strCity, int isType, String strSelection, String strAlbumUsername) {








        ShareableResource photo = new ShareableResource(
                ShareType.PHOTO,
                strPhotoId,
                strSubTitle,
                "",
                baseUrl+"/photo/"+strFileName,
                baseUrl+"/photo/"+strPhotoId
        );

        ShareBottomBar shareBottomBar = new ShareBottomBar(photo,shareService,shareMetricService);

        MenuItem viewLarger = createIconItem(shareBottomBar, VaadinIcon.VIEWPORT.create(), "View Larger", "View Larger");
        viewLarger.addClickListener(click->{
            showDialogWithCarousel(isType, strSelection, strPhotoId, strAlbumUsername, false);
        });

        // Query avg rating and count for this photo
        double avgRating = 0.0;
        long ratingCount = 0;
        if (photoRatingService != null) {
            try {
                int photoIdInt = Integer.parseInt(strPhotoId);
                avgRating = photoRatingService.getAverageRating(photoIdInt);
                ratingCount = photoRatingService.getRatingCount(photoIdInt);
            } catch (NumberFormatException ignored) {}
        }
        String ratingLabel = ratingCount > 0
                ? String.format("Rate it  \u2605%.1f (%d)", avgRating, ratingCount)
                : "Rate it";

        SvgIcon svgRate = new SvgIcon(DownloadHandler.forClassResource(GalleryImageViewCard.class, "/icons/star-empty-icon.svg"));

        MenuItem viewRate = createIconItem(shareBottomBar, svgRate, ratingLabel, "rate it");
        viewRate.addClickListener(click->{
            showDialogWithCarousel(isType, strSelection, strPhotoId, strAlbumUsername, true);
        });


        if (strCity!= null && !strCity.isEmpty()) {
            MenuItem viewCityInfo = createIconItem(shareBottomBar, VaadinIcon.LOCATION_ARROW_CIRCLE_O.create(), "", null);
            viewCityInfo.addClickListener(click -> {
                Dialog dialog = showDialogWeatherForCity(strCity, "");
                dialog.open();
            });
        }


        shareBottomBar.addShareItemMenu();



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

//        Div divSharesInfo = new Div("");
//        divSharesInfo.addClassName(TextColor.DISABLED);
//        SvgIcon svgShare = new SvgIcon(DownloadHandler.forClassResource(GalleryImageViewCard.class, "/icons/share-line-icon.svg"));
//        Button btnShare = new Button(svgShare);
//        //btnShare.setTooltipText("Share it");
//        btnShare.setSuffixComponent(divSharesInfo);
//        divShare.add(btnShare, tooltipShare);

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
 //           layoutActions.add(divLike, btnLists, btnShare);
        } else {
            if (strImagePath.contains(subPathLarge)) {
 //               layoutActions.add(divLike, divLists, divShare, divRate);
            } else {
//                layoutActions.add(divLike, divLists, divShare, divRate, divFullView);
                layoutActions.add(shareBottomBar);
            }
        }
        return layoutActions;
    }


    private MenuItem createIconItem(HasMenuItems menu, Component iconName,
                                    String label, String ariaLabel) {

//                Icon icon = new Icon(iconName);
        return createIconItem(menu, iconName, label, ariaLabel, false);
    }

    private MenuItem createIconItem(HasMenuItems menu, Component icon,
                                    String label, String ariaLabel, boolean isChild) {
//        Icon icon = new Icon(iconName);

        if (isChild) {
            icon.getStyle().setWidth("var(--lumo-icon-size-s)");
            icon.getStyle().setHeight("var(--lumo-icon-size-s)");
            icon.getStyle().setMarginRight("var(--lumo-space-s)");
        }else{
            icon.getStyle().setWidth("var(--lumo-icon-size-m)");
            icon.getStyle().setHeight("var(--lumo-icon-size-m)");
            icon.getStyle().setMarginRight("var(--lumo-space-s)");
        }

        MenuItem item = menu.addItem(icon, e -> {
        });

        if (ariaLabel != null) {
            item.setAriaLabel(ariaLabel);
        }

        Text lbl =  new Text(" "+label);


        if (label != null) {
            item.add(lbl);
        }

        return item;
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

    private VerticalLayout getEditPanel(String strPhotoId, String strAvailableAlbumsMemberId, String strUserRights, String strSubTitle,
                                        String strGenreDbId, String strCityIdDb, String strSubjectIdDb,
                                        String strPersonalNotes) {

        logger.info(" end destination_Id:" + strCityIdDb + " subject_id:" + strSubjectIdDb);

        VerticalLayout layoutEdit = new VerticalLayout();
        layoutEdit.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE, Padding.XSMALL,
                Gap.MEDIUM);

        Select<String> cmbGenre = new Select<>();
        cmbGenre.setLabel("Genre");
        cmbGenre.setHelperText("Select the Genre which describes best the photo.");
        cmbGenre.setWidthFull();

        Select<String> cmbDestination = new Select<>();
        cmbDestination.setLabel("Location");
        cmbDestination.setHelperText("Avoid to select, when there are identifiable humans.");
        cmbDestination.setWidthFull();

        Select<String> cmbSubject = new Select<>();
        cmbSubject.setLabel("Main Subject");
        cmbSubject.setHelperText("Select a subject when is the main object and location can be anywhere.");
        cmbSubject.setWidthFull();

        List<Record> lstGenreRecs = getRecordsFromDb(sqlReadGenre, arrGenreNames);
        ArrayList<String> lstGenres = new ArrayList<>();
        ArrayList<String> lstGenreId = new ArrayList<>();
        String strGenre = "";
        for (int r = 0; r < lstGenreRecs.size(); r++) {
            String strGenreId ="";
            String genre = lstGenreRecs.get(r).getColumnData("title");
            lstGenres.add(genre);
            strGenreId = lstGenreRecs.get(r).getColumnData("id");
            lstGenreId.add(strGenreId);
            if (strGenreDbId.equalsIgnoreCase(strGenreId)) {
                strGenre = genre;
            }
        }
        cmbGenre.setItems(lstGenres);
        cmbGenre.setValue(strGenre);

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


        Checkbox chkIsTypeProfile = new Checkbox("Is for your Profile");
        chkIsTypeProfile.addValueChangeListener(event->{
            isTypeProfile = event.getValue();

        });

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
        String sqlMemberId = "  AND usr.userId = '" + strAvailableAlbumsMemberId + "' ";
        String sqlMemberOfAlbumsOrderBy = " ORDER BY a.title ASC";

        List<Record> lstAlbums = getRecordsFromDb(sqlMemberOfAlbums+sqlMemberId+sqlMemberOfAlbumsOrderBy, arrColumnsMemberAlbums);
        List<String> lstAlbumTitle = new ArrayList<>();
        List<String> lstAlbumId = new ArrayList<>();
        List<String> lstAlbumUserId = new ArrayList<>();
        for (int i = 0; i < lstAlbums.size(); i++) {
            lstAlbumTitle.add(lstAlbums.get(i).getColumnData("title"));
            lstAlbumId.add(lstAlbums.get(i).getColumnData("id"));
            lstAlbumUserId.add(lstAlbums.get(i).getColumnData("user_id"));
        }

                String[] field = {"album_title"};
        String sqlCountPhotosOfTheAlbum = "SELECT a.title AS album_title, a.user_id, a. album_visible_to, a.description " +
                " " +
                " FROM photo_album_photo pap, photo_album a " +
                " WHERE pap.photo_album_id = a.id AND pap.user_id = a.user_id AND pap.user_id = " + strAvailableAlbumsMemberId + " AND pap.photo_id = " + strPhotoId +
                " ORDER BY a.title ";
        List<Record> lstAlbumsPhotoBelongs = getRecordsFromDb(sqlCountPhotosOfTheAlbum, field);

        ArrayList<String> lstAlbumSelectedTitles = new ArrayList<>();
        lstAlbumsPhotoBelongs.forEach(value -> {
            lstAlbumSelectedTitles.add(value.getColumnData("album_title"));
        });
        Set<String> setAlbumsPhotoBelongs = new HashSet<>(lstAlbumSelectedTitles);


        MultiSelectComboBox cmbAlbums = new MultiSelectComboBox<>();
        cmbAlbums.setLabel("Albums");
        cmbAlbums.setItems(lstAlbumTitle);
        cmbAlbums.select(setAlbumsPhotoBelongs);
        cmbAlbums.setWidthFull();
        cmbAlbums.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);

//        Button btnAlbums = new Button("Add Photo to Albums ...");
//        btnAlbums.setIcon(FontAwesome.Solid.PHOTO_FILM.create());
//        btnAlbums.setWidthFull();
//        btnAlbums.addClickListener(click -> {
//            displayDialogAlbumsOfMember(strAvailableAlbumsMemberId, strPhotoId);
//        });

        Button btnSave = new Button("Save");
        btnSave.setIcon(FontAwesome.Regular.SAVE.create());

        btnSave.addClickListener(event -> {

            String strGenreId = "";
            String strGenreTitle = cmbGenre.getValue();
            for (int i = 0; i < lstGenres.size(); i++) {
                if (lstGenres.get(i).equalsIgnoreCase(strGenreTitle)) {
                    strGenreId = lstGenreId.get(i);
                }
            }

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

            if (!strGenreId.isEmpty()) {
                String strUpdateGenre = "UPDATE photo_meta SET " +
                        " genre_id = '" + strGenreId + "' " +
                        " WHERE id = '" + strPhotoId + "'";
                recordService.insertOneRecordWithQuery(strUpdateGenre, null, null);
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

            if(isTypeProfile){

                String strUpdateIsProfile = "UPDATE photo_meta SET " +
                        " visible_to = 'Profile' " +
                        " WHERE id = '" + strPhotoId + "'";
                int retProf = recordService.insertOneRecordWithQuery(strUpdateIsProfile, null, null);
            }

            String strTxtSubtitle = txtSubtitle.getValue().trim();
            String strTxtPersonalNotes = txtPersonalNotes.getValue().trim();
            Object[] fieldValue = {strTxtSubtitle, strTxtPersonalNotes};
            String[] fieldType = {"java.lang.String", "java.lang.String"};

            String strUpdateSubj = "UPDATE photo_meta SET " +
                    " subtitle = ?, notes = ? " +
                    " WHERE id = '" + strPhotoId + "'";
            int ret = recordService.insertOneRecordWithQuery(strUpdateSubj, fieldValue, fieldType);

            savePhotoInAlbums(cmbAlbums, lstAlbumTitle, lstAlbumId, lstAlbumUserId, strPhotoId);

            if (ret == 1) {
                String message = "Photo Updated ! ";
                String messageUp = "Belongs in  " + cmbAlbums.getSelectedItems().toArray().length + "  Albums !";
               // Notification notificationUp = Notification.show(messageUp, 3000, Notification.Position.MIDDLE);
               // notificationUp.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                logger.info(" length: " + cmbAlbums.getSelectedItems().toArray().length);

                Notification notification = Notification.show(message+messageUp, 4000, Notification.Position.MIDDLE);
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

        layoutEdit.add(txtSubtitle, cmbGenre, cmbDestination, cmbSubject, txtPersonalNotes,chkIsTypeProfile, cmbAlbums, layoutUserActions);
        return layoutEdit;
    }

/*
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
*/

//    private MultiSelectListBox<String> loadAlbumsInfoPanel(String sqlMemberOfAlbums, String[] arrColumnsMemberAlbums,
//                                                           String strAlbumUserId, String strPhotoId) {
//
//
//        List<Record> lstAlbums = getRecordsFromDb(sqlMemberOfAlbums, arrColumnsMemberAlbums);
//        List<String> lstAlbumTitle = new ArrayList<>();
//        List<String> lstAlbumId = new ArrayList<>();
//        for (int i = 0; i < lstAlbums.size(); i++) {
//            lstAlbumTitle.add(lstAlbums.get(i).getColumnData("title"));
//            lstAlbumId.add(lstAlbums.get(i).getColumnData("id"));
//        }
//
//
//        MultiSelectListBox<String> listBoxAlbums;
//        listBoxAlbums = new MultiSelectListBox<>();
//        listBoxAlbums.addClassNames(Background.BASE, BorderRadius.SMALL);
//        listBoxAlbums.setWidthFull();
//        listBoxAlbums.setMinHeight("260px");
//        listBoxAlbums.setItems(lstAlbumTitle);
//
//        String[] field = {"album_title"};
//        String sqlCountPhotosOfTheAlbum = "SELECT a.title AS album_title, a.user_id, a. album_visible_to, a.description " +
//                " " +
//                " FROM photo_album_photo pap, photo_album a " +
//                " WHERE pap.photo_album_id = a.id AND pap.user_id = a.user_id AND pap.user_id = " + strAlbumUserId + " AND pap.photo_id = " + strPhotoId +
//                " ORDER BY a.title ";
//        List<Record> lstAlbumsPhotoBelongs = getRecordsFromDb(sqlCountPhotosOfTheAlbum, field);
//
//        ArrayList<String> lstAlbumTitles = new ArrayList<>();
//        lstAlbumsPhotoBelongs.forEach(value -> {
//            lstAlbumTitles.add(value.getColumnData("album_title"));
//        });
//        Set<String> setAlbumsPhotoBelongs = new HashSet<>(lstAlbumTitles);
//
//
//        listBoxAlbums.setValue(setAlbumsPhotoBelongs);
//
//
//        return listBoxAlbums;
//    }

    private boolean savePhotoInAlbums(MultiSelectComboBox<String> photoInAlbums, List<String> lstAlbumTitle, List<String> lstAlbumId,
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

                    String sqlDelete = "DELETE FROM photo_album_photo WHERE `user_id` = " + strAlbumUserId + " AND photo_id = " + strPhotoId;
                    //  " AND `photo_album_id` = " + strAlbumId +

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

        return true;
    }

    private Dialog  showDialogWeatherForCity(String city, String country) {

        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);

        HorizontalLayout layoutWeather = new HorizontalLayout();
        layoutWeather.getStyle().setColor("#8b94a0");
        layoutWeather.addClassNames(
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER
        );

        LocalWeatherForecast weatherForecast = new LocalWeatherForecast(weatherService, city, country);
        weatherForecast.setMaxWidth("900px");
        layoutWeather.add(weatherForecast);

        dialog.add(layoutWeather);
        return dialog;
    }

    private VerticalLayout fetchPhotoCreator(Record record, boolean showMinimum) {

        VerticalLayout layoutCreatorInfo = new VerticalLayout();
        layoutCreatorInfo.addClassNames(
                LumoUtility.Width.FULL, LumoUtility.Height.FULL,
                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.AlignItems.START, LumoUtility.JustifyContent.START);
        layoutCreatorInfo.addClassNames("member-profile-design");
        layoutCreatorInfo.addClassName("info-to-show");
        layoutCreatorInfo.setMaxHeight("160px");
//        layoutCreatorInfo.getStyle().setOpacity("1");

        String strCreatorId = record.getColumnData("uploaderId");
        String strUsername = record.getColumnData("username");
        String strName = record.getColumnData("name");
        String strSurname = record.getColumnData("surname");
        String strShortBio = record.getColumnData("short_bio");
        String strMemberSince = record.getColumnData("member_since");
        String strAvatarPath = record.getColumnData("avatar_path");
        String strResident = record.getColumnData("resident");
        String strResidentCountry = record.getColumnData("resident_country");

        String strCountPhotos = record.getColumnData("count_photos");
        String strCountAlbums = record.getColumnData("count_albums");

        Div divImgAvatar = new Div();
        divImgAvatar.addClassNames(LumoUtility.Padding.NONE, LumoUtility.Margin.NONE);

        String strAvatarSize = "50px";
        Image imageAvatar = genericView.getAvatarThumbImage(strAvatarPath, strUsername, strAvatarSize, strAvatarSize);
        divImgAvatar.add(imageAvatar);


        HorizontalLayout horizontalLayout = new HorizontalLayout();

        layoutCreatorInfo.getStyle().setOpacity("1");


        H4 objMember = new H4(strUsername);
        objMember.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.NORMAL, LumoUtility.FontSize.SMALL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.Gap.XSMALL);

        H4 objName = new H4(strName + " " + strSurname);
        objName.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.SMALL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.Gap.XSMALL);

        Div divMemberSince = new Div("Member since "+strMemberSince);
        divMemberSince.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.EXTRALIGHT, LumoUtility.FontSize.XSMALL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
                LumoUtility.Gap.XSMALL);



        Icon iconPhoto = VaadinIcon.PICTURE.create();
        Icon iconAlbum = FontAwesome.Solid.PHOTO_FILM.create();
        Span spPhotos = new Span(" Photos");
        spPhotos.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.FontSize.SMALL);
        Span divPhotos = new Span(strCountPhotos);
        divPhotos.add(spPhotos);
        divPhotos.addClassNames(LumoUtility.TextColor.SECONDARY);
        Span spAlbums = new Span(" Albums");
        spAlbums.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.FontSize.SMALL);
        Span divAlbums = new Span(strCountAlbums);
        divAlbums.addClassNames(LumoUtility.TextColor.SECONDARY);
        divAlbums.add(spAlbums);

        HorizontalLayout layoutCounts = new HorizontalLayout();
        layoutCounts.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                LumoUtility.Padding.SMALL, LumoUtility.Margin.NONE,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.LARGE, LumoUtility.Background.CONTRAST_5,
                LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);
        layoutCounts.add(iconPhoto, divPhotos, iconAlbum, divAlbums);

        VerticalLayout layoutMemberCard = new VerticalLayout();
//            layoutMemberCard.getStyle().setMaxWidth("300px");
//            layoutMemberCard.getStyle().set("border", "lightgrey 1px solid");
        layoutMemberCard.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        layoutMemberCard.setMaxWidth("60px");
        layoutMemberCard.add(divImgAvatar);

        Div divResidentCaption = new Div("Resident");
        Div divResident = new Div(strResident);
        divResident.addClassNames(LumoUtility.FontWeight.BOLD);

        VerticalLayout layoutAdditional = new VerticalLayout();
        layoutAdditional.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
                LumoUtility.Gap.XSMALL);
        layoutAdditional.add(objMember, objName, divMemberSince); //, divBioTitle, divBio);//, divResidentCaption, divResident);

        horizontalLayout.add(layoutMemberCard, layoutAdditional);

        if(showMinimum){
            layoutCreatorInfo.add(horizontalLayout);
        }else {
            layoutCreatorInfo.add(horizontalLayout, layoutCounts);
        }

        return layoutCreatorInfo;
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