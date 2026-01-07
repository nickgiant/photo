package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.views.AlbumsView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
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
    private String hostname;
    private String publicIp;
    private long sessionCreation;

    private String dirChar = FileSystems.getDefault().getSeparator();

    private String[] arrAlbumNames;
    private String sqlReadAlbums;
    private String sqlCarousel;
    private String sqlReadAlbumPhotosOrderBy;
    private String[] arrColumnsCarousel;

    public AlbumViewCard(Record record, String strImagePath, boolean isMobile, int userId, String strUserName, long sessionCreation,
                         String hostname, String publicIp, int intType, RecordService recordService, String sqlCarousel, String sqlReadAlbumPhotosOrderBy,
                         String[] arrColumnsCarousel) {
        this.recordService = recordService;
        this.isMobile = isMobile;
        this.hostname = hostname;
        this.publicIp = publicIp;
        this.sessionCreation = sessionCreation;
        this.sqlCarousel = sqlCarousel;
        this.sqlReadAlbumPhotosOrderBy = sqlReadAlbumPhotosOrderBy;
        this.arrColumnsCarousel = arrColumnsCarousel;

        genericView = new GenericView(recordService);


        if (record == null) {
            logger.error("record is null");
        }

        String strFileName = record.getColumnData("name_new");
        String strAlbumTitle = record.getColumnData("album_title");
        String strDescription = record.getColumnData("description");

        String strAlbumCatType = record.getColumnData("cat_type");

        String strAlbumPhotoCount = record.getColumnData("album_photo_count").toString();
        int intAlbumPhotoCount = Integer.parseInt(strAlbumPhotoCount);
//        String strCreator = record.getColumnData("creator");
        String strVisibleTo = record.getColumnData("visible_to");
        String strPhotoUrl = record.getColumnData("name_new");

        String strPhoto1 = record.getColumnData("photo_1");
        String strMetaOrientation1 = record.getColumnData("meta_orientation1");
        String strPhoto2 = record.getColumnData("photo_2");
        String strPhoto3 = record.getColumnData("photo_3");
        String strPhoto4 = record.getColumnData("photo_4");

        String strDateAlbumCreated = record.getColumnData("datetime_album_created");

        String strAlbumMemberUserid = record.getColumnData("user_id");
        String strAlbumMemberUsername = record.getColumnData("username");
        String strAlbumMemberName = record.getColumnData("name");
        String strAlbumMemberSurname = record.getColumnData("surname");
        String strUserResident = record.getColumnData("resident");
        String strAvatarPath = record.getColumnData("avatar_path");
        String strUserJoined = record.getColumnData("date_joined");

        String strAlbumMember = strAlbumMemberName + " " + strAlbumMemberSurname;

        String strCity = "";
        if (!record.getColumnData("city_name").isEmpty()) {
            strCity = record.getColumnData("city_name");
        }

        VerticalLayout layoutAll = new VerticalLayout();
        layoutAll.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN, TextAlignment.CENTER);

        H2 header = new H2();
        header.setText(strAlbumTitle);

        if (strAlbumTitle.trim().isEmpty() || strAlbumTitle.equalsIgnoreCase("null")) {
            header.setText("");
            header.setHeight("1px");
            header.setVisible(false);
        }

        H3 albumType = new H3(strAlbumCatType);
        H5 ablbumCreator = new H5(strAlbumMember);

        Div layoutImagesBox = new Div();
        layoutImagesBox.addClassName("image-matrix");


        if (strPhoto1 != null && !strPhoto1.isEmpty() && !strPhoto1.equalsIgnoreCase("null")) {
            strPhotoUrl = strPhoto1;
        }

        String imagePath = strImagePath + dirChar + strPhotoUrl;
        Image image1 = getImage(imagePath);
        if (strMetaOrientation1.equalsIgnoreCase("8")) {
            image1.getStyle().set("rotate", "-90deg");
        }
        Div divImage1 = new Div();
        divImage1.addClassName("image1-div");
        divImage1.add(image1);


        Div divImage2 = new Div();
        if (strPhoto2 != null && !strPhoto2.isEmpty() && !strPhoto2.equalsIgnoreCase("null")) {
            Image image2 = getImage(strImagePath + dirChar + strPhoto2);
            divImage2.addClassName("image2-div");
            divImage2.add(image2);
        }
        Div divImage3 = new Div();
        if (strPhoto3 != null && !strPhoto3.isEmpty() && !strPhoto3.equalsIgnoreCase("null")) {
            Image image3 = getImage(strImagePath + dirChar + strPhoto3);
            divImage3.addClassName("image3-div");
            divImage3.add(image3);
        }
        Div divImage4 = new Div();
        if (strPhoto4 != null && !strPhoto4.isEmpty() && !strPhoto4.equalsIgnoreCase("null")) {
            Image image4 = getImage(strImagePath + dirChar + strPhoto4);
            divImage4.addClassName("image4-div");
            divImage4.add(image4);
        }

        if (intType == 1) {
            layoutImagesBox.add(divImage1, divImage2, divImage3, divImage4);
        } else if (intType == 2) {
            layoutImagesBox.add(divImage1, divImage2);
        }
        layoutImagesBox.add(header, ablbumCreator, albumType);

        VerticalLayout divPhotoInfo = new VerticalLayout();
        divPhotoInfo.addClassNames(Overflow.HIDDEN, TextColor.TERTIARY,
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.NONE, Margin.NONE, //Margin.Top.LARGE,
                Gap.XSMALL,
                BorderRadius.LARGE
        );


        Div subtitle = new Div();
        subtitle.addClassNames(FontSize.SMALL,
                Width.FULL, TextAlignment.CENTER, AlignItems.CENTER, JustifyContent.CENTER,
                Padding.NONE,
                Margin.NONE
        );
        subtitle.addClassName("bottom-line");

        if (!strDescription.trim().isEmpty() && !strDescription.equalsIgnoreCase("null")) {
            subtitle.setText(strDescription);
        } else {

        }

        HorizontalLayout layoutPhotoCountAll = new HorizontalLayout();
        layoutPhotoCountAll.addClassNames(
                //  Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        HorizontalLayout layoutPhotoCount = new HorizontalLayout();
        layoutPhotoCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        H4 divPhotoCount = new H4(intAlbumPhotoCount + "");
        layoutPhotoCount.add(FontAwesome.Regular.IMAGES.create(), divPhotoCount);
        H4 divCountLabel = new H4("Photos");
//        divCountLabel.addClassNames(FontSize.XXSMALL);
        layoutPhotoCountAll.add(layoutPhotoCount, divCountLabel);

        HorizontalLayout layoutDateAll = new HorizontalLayout();
        layoutDateAll.addClassNames(
                //  Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
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
        H4 divDateCreated = new H4("Created " + strDateAlbumCreated);
        divDateCreated.addClassNames(AlignItems.CENTER, TextAlignment.CENTER, JustifyContent.CENTER);
        layoutDate.add(FontAwesome.Solid.CALENDAR_DAY.create(), divDateCreated);

        layoutDateAll.add(layoutDate);

        HorizontalLayout divSubHeaderAll = new HorizontalLayout();
        divSubHeaderAll.addClassNames(Width.FULL, AlignItems.END,
                JustifyContent.BETWEEN, Margin.NONE, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL
        );
        divSubHeaderAll.add(layoutPhotoCountAll, layoutDateAll);

        HorizontalLayout layoutPhotosInfo = new HorizontalLayout();
        layoutPhotosInfo.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.BETWEEN,
                Margin.NONE,
                Padding.Horizontal.MEDIUM,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        layoutPhotosInfo.addClassName("summary");

        StreamResource iconRate = new StreamResource("star-empty-icon.svg",
                () -> getClass().getResourceAsStream("/icons/star-empty-icon.svg"));
        SvgIcon svgRate = new SvgIcon(iconRate);

        VerticalLayout layoutRateAll = new VerticalLayout();
        layoutRateAll.addClassNames(
                //  Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        HorizontalLayout layoutRate = new HorizontalLayout();
        layoutRate.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divRate = new Div("1");
        layoutRate.add(svgRate, divRate);
        Span divRateLabel = new Span("Rate");
        divRateLabel.addClassNames(FontSize.XXSMALL);
        layoutRateAll.add(layoutRate, divRateLabel);

//        VerticalLayout layoutPhotoCountAll = new VerticalLayout();
//        layoutPhotoCountAll.addClassNames(
//                //  Overflow.HIDDEN, Width.FULL,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Margin.NONE,
//                Padding.NONE,
//                Gap.XSMALL,
//                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                //   Background.CONTRAST_5,
//                BorderRadius.NONE
//        );
//        HorizontalLayout layoutPhotoCount = new HorizontalLayout();
//        layoutPhotoCount.addClassNames(
////                Overflow.HIDDEN, Width.FULL,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Margin.NONE,
//                Padding.NONE,
//                Gap.XSMALL,
//                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                //   Background.CONTRAST_5,
//                BorderRadius.NONE
//        );
//        Div divPhotoCount = new Div(intAlbumPhotoCount + "");
//        layoutPhotoCount.add(FontAwesome.Regular.IMAGES.create(), divPhotoCount);
//        Div divCountLabel = new Div("Photos");
//        divCountLabel.addClassNames(FontSize.XXSMALL);
//        layoutPhotoCountAll.add(layoutPhotoCount, divCountLabel);

        VerticalLayout layoutViewCountAll = new VerticalLayout();
        layoutViewCountAll.addClassNames(
                //   Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
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
        Span divViewsLabel = new Span("Views");
        divViewsLabel.addClassNames(FontSize.XXSMALL);
        layoutViewCountAll.add(layoutViewCount, divViewsLabel);

        VerticalLayout layoutLocationsCountAll = new VerticalLayout();
        layoutLocationsCountAll.addClassNames(
                // Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        HorizontalLayout layoutLocationsCount = new HorizontalLayout();
        layoutLocationsCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divLocations = new Div("1");
        layoutLocationsCount.add(FontAwesome.Regular.COMPASS.create(), divLocations);
        Span divLocationsLabel = new Span("Locations");
        divLocationsLabel.addClassNames(FontSize.XXSMALL);
        layoutLocationsCountAll.add(layoutLocationsCount, divLocationsLabel);

//        VerticalLayout layoutDateAlbumCreatedAll = new VerticalLayout();
//        layoutDateAlbumCreatedAll.addClassNames(
//                //    Overflow.HIDDEN, Width.FULL,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Margin.NONE,
//                Padding.NONE,
//                Gap.XSMALL,
//                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                //   Background.CONTRAST_5,
//                BorderRadius.NONE
//        );
//
//        HorizontalLayout layoutDateAlbumCreated = new HorizontalLayout();
//        layoutDateAlbumCreated.addClassNames(
////                Overflow.HIDDEN, Width.FULL,
//                AlignItems.CENTER, JustifyContent.CENTER,
//                Margin.NONE,
//                Padding.NONE,
//                Gap.XSMALL,
//                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                //   Background.CONTRAST_5,
//                BorderRadius.NONE
//        );
//        Div divDate = new Div(strDateAlbumCreated);
//        layoutDateAlbumCreated.add(VaadinIcon.CALENDAR_CLOCK.create(), divDate); // FontAwesome.Regular.CALENDAR.create()
//        Div divAlbumCreatedLabel = new Div("Created");
//        divAlbumCreatedLabel.addClassNames(FontSize.XXSMALL);
//        layoutDateAlbumCreatedAll.add(layoutDateAlbumCreated, divAlbumCreatedLabel);

        //RouterLink linkUploader = new RouterLink(strUploader, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));
        //RouterLink linkAlbum = new RouterLink(strTitle, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));

//        RouterLink routerLinkAlbum = new RouterLink();
//        routerLinkAlbum.setRoute(AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));
//        routerLinkAlbum.addClassNames(AlignItems.CENTER, JustifyContent.START, TextAlignment.CENTER);

        HorizontalLayout layoutUserActions = new HorizontalLayout();
        layoutUserActions.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.SMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.LARGE
        );

        Button btnMoreAction = new Button(VaadinIcon.EDIT.create());//svgAction);
        btnMoreAction.setTooltipText("Edit");
        btnMoreAction.addClassName("btn-actions");

        Button btnComment = new Button(VaadinIcon.COMMENT.create());
        btnComment.setTooltipText("Comment on it");

        Button btnMoreInfo = new Button(VaadinIcon.INFO.create());//svgAction);
        btnMoreInfo.setTooltipText("More info");

        layoutUserActions.add(btnMoreAction, btnComment, btnMoreInfo);

//         divPhotoInfo.add(layoutPhotosInfo); //, layoutUserActions);
        //       routerLinkAlbum.add(header, subtitle, layoutImage, divPhotoInfo);


//        Avatar userAvatar = new Avatar(strAlbumUserName);
//        userAvatar.setImage(strAvatar);
//        userAvatar.getElement().setAttribute("tabindex", "-1");
//        userAvatar.addThemeVariants(AvatarVariant.LUMO_SMALL);
//
//        Avatar userAvatarLarge = new Avatar(strAlbumUserName);
//        userAvatarLarge.setImage(strAvatar);
//        userAvatarLarge.getElement().setAttribute("tabindex", "-1");
//        userAvatarLarge.addThemeVariants(AvatarVariant.LUMO_XLARGE);


        Image imgAvatarSmall = genericView.getAvatarImage(strAvatarPath, strAlbumMember, "50px", "50px");
        imgAvatarSmall.addClassNames(BorderRadius.FULL);
        //Image imgAvatarSmall = getAvatarImage(strAvatar, strAlbumUserName, "40px", "40px");

        Image imgAvatarMedium = genericView.getAvatarImage(strAvatarPath, strAlbumMember, "70px", "70px");
//        Image imgAvatarMedium = getAvatarImage(strAvatar, strAlbumUserName, "70px", "70px");

        AvatarItem avatarItemMe = new AvatarItem(strAlbumMember, "", imgAvatarSmall);
        avatarItemMe.addClassNames(Width.FULL, Padding.XSMALL, Background.CONTRAST_5, BorderRadius.MEDIUM);
        avatarItemMe.setMinWidth("300px");
//        Details detailsMember = new Details();
//        detailsMember.addClassNames(Width.FULL, BorderRadius.SMALL);
//        detailsMember.addThemeVariants(DetailsVariant.FILLED);
//        detailsMember.addClassName("member-small");
//        detailsMember.setSummary(avatarItemMe);
        AvatarItem avatarLargeItemMe = new AvatarItem(strAlbumMember, "@" + strAlbumMemberUsername, imgAvatarMedium);

        HorizontalLayout layoutMemberInfo = new HorizontalLayout();
        layoutMemberInfo.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.AROUND,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
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

        HorizontalLayout layoutMemberLocationsCount = new HorizontalLayout();
        layoutMemberLocationsCount.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.XSMALL,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        Div divMemberLocations = new Div(strUserResident);
        layoutMemberLocationsCount.add(FontAwesome.Regular.COMPASS.create(), divMemberLocations);

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
        Div divDateJoined = new Div(strUserJoined);
        layoutDateJoined.add(VaadinIcon.CALENDAR_CLOCK.create(), divDateJoined); // FontAwesome.Regular.CALENDAR.create()
        layoutMemberInfo.add(layoutMemberPhotoCount, layoutMemberViewCount, layoutMemberLocationsCount, layoutDateJoined);

        layoutPhotosInfo.add(layoutRateAll, layoutViewCountAll, layoutLocationsCountAll, avatarItemMe);

        RouteParam routeAlbum = new RouteParam("title", strAlbumTitle);
        RouteParam routeMember = new RouteParam("member", strAlbumMemberUsername);

        if (intType == 1) {
            layoutAll.add(layoutImagesBox, subtitle, divSubHeaderAll, layoutPhotosInfo, getActions());
        } else if (intType == 2) {
            layoutAll.add(layoutImagesBox, subtitle, divSubHeaderAll, layoutPhotosInfo);
        }

        layoutAll.addClassName("album-info-card");
        this.addClassName("album-info");
        if (isMobile) {
            this.addClassName("album-info-mobile");
        } else {
            this.addClassName("album-info-wide");
        }
        this.setRoute(AlbumsView.class, new RouteParameters(routeMember, routeAlbum));
        this.add(layoutAll);
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
        //layoutActions.setWidthFull();

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
//        Button btnMoreAction = new Button(VaadinIcon.BOOKMARK.create());//svgAction);
//        btnMoreAction.setTooltipText("Save to list");
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

    private Image getImage(String strImagePath) {


        Path path = Paths.get(strImagePath);
        File file = path.toFile();

        final StreamResource imageResource = new StreamResource("streamResource", () -> {
            try {
                //ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
                //imageUtilsMeta.printPhotoMetadataValue(file);

                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
                logErrorInDb(e, "GalleryImageViewCard StreamResource FileNotFoundException", hostname, publicIp, sessionCreation, file.getAbsolutePath());
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


    private VerticalLayout getPhotoMetaDataLayout(Record record) {

        VerticalLayout layoutMetaData = new VerticalLayout();
//                    ,"space_size","space_size_medium", "space_size_thumb","meta_camera_make", "meta_camera_model","meta_lens_make","meta_lens_model"
//                ,"meta_focal_length", "meta_focal_length_ff", "meta_iso"
//                ,"location_by_user","location_area","location_country_code","location_lat","location_lon"

        return layoutMetaData;
    }

    private void logErrorInDb(Exception e, String function, String hostname, String publicIp, long sessionCreation, String info) {
        recordService.logErrorInDb(e, hostname, function, 1, "", publicIp, Long.toString(sessionCreation), info);
    }


}
