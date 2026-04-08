package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.views.AlbumsView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.details.Details;
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

public class AlbumThumbViewCard extends RouterLink {

    private static final Logger logger = LoggerFactory.getLogger(AlbumThumbViewCard.class);
    private final RecordService recordService;
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

    public AlbumThumbViewCard(Record record, String strImagePath, boolean isMobile, int userId, long sessionCreation,
                              String hostname, String publicIp, RecordService recordService, int intType) {
        this.recordService = recordService;
        this.isMobile = isMobile;
        this.hostname = hostname;
        this.publicIp = publicIp;
        this.sessionCreation = sessionCreation;

        this.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN, TextAlignment.CENTER);

        genericView = new GenericView(recordService);

        if (record == null) {
            logger.error("record is null");
        }

        String strAlbumTitle = record.getColumnData("album_title");
        String strDescription = record.getColumnData("description");
        String strAlbumUsername = record.getColumnData("username");

        String strAlbumName = record.getColumnData("name");
        String strAlbumSurname = record.getColumnData("surname");

//        String strAlbumTitle = record.getColumnData("album_title");

//        RouteParam routeUploaderAll = new RouteParam("member", STR_ALL_MEMBERS);

//        RouteParam routeAlbum = new RouteParam("title", strAlbumTitle);
//        RouteParam routeUploader = new RouteParam("member", strUploader);
//        //RouterLink linkUploader = new RouterLink(strUploader, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));
//        RouterLink linkAlbum = new RouterLink(strAlbumTitle, AlbumsView.class, new RouteParameters(routeAlbum, routeUploader));

//        String strImagePath = strPath + dirChar; // + strFileName;
        logger.info(" strImagePath " + strImagePath);


        Div divAlbumTitle = new Div(strAlbumTitle);
        divAlbumTitle.addClassNames(Padding.SMALL, Width.FULL, FontWeight.BOLD, TextColor.SECONDARY, TextAlignment.CENTER, FontSize.MEDIUM);
        // Div divAlbumSubTitle = new Div(strDescription);
        Div divUser = new Div(strAlbumName + " " + strAlbumSurname);
        divUser.addClassNames(Width.FULL, FontWeight.LIGHT, TextColor.TERTIARY, TextAlignment.CENTER, FontSize.XSMALL);


        String strAlbumPhotoCount = record.getColumnData("album_photo_count");
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

        String strAlbumUserid = record.getColumnData("user_id");
        String strAlbumUserName = record.getColumnData("username");
        String strAlbumNameOfUser = record.getColumnData("username");
        String strUserResident = record.getColumnData("resident");
        String strAvatarPath = record.getColumnData("avatar_path");
        String strUserJoined = record.getColumnData("date_joined");


        String strCity = "";
        if (!record.getColumnData("city_name").isEmpty()) {
            strCity = record.getColumnData("city_name");
        }

        Div layoutImagesBox = new Div();
        layoutImagesBox.addClassName("image-matrix");
        if (strPhoto1 != null && !strPhoto1.isEmpty() && !strPhoto1.equalsIgnoreCase("null")) {
            strPhotoUrl = strPhoto1;
        }

        String imagePath = strImagePath + dirChar + strPhotoUrl;
        Image image1 = getImage(imagePath);
//        if (strMetaOrientation1.equalsIgnoreCase("8")) {
//            image1.getStyle().set("rotate", "-90deg");
//        }
        Div divImage1 = new Div();
        divImage1.addClassName("image1-div");
        divImage1.add(image1);

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
                    divImage1.addClassName("portrait");
                } else if (ratio > 1.5) {
                    divImage1.addClassName("landscape");
                }else{
                    divImage1.addClassName("square");
                }
            }
        }catch (ArithmeticException e){
            logger.error(e.getMessage());
        }

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

        VerticalLayout divPhotoInfo = new VerticalLayout();
        divPhotoInfo.addClassNames(Overflow.HIDDEN, TextColor.TERTIARY,
                AlignItems.CENTER, JustifyContent.CENTER,
                Padding.NONE, Margin.NONE, //Margin.Top.LARGE,
                Gap.XSMALL,
                BorderRadius.LARGE
        );

        H3 header = new H3();
        header.addClassNames(FontSize.LARGE, FontWeight.SEMIBOLD,
                Width.FULL, TextAlignment.LEFT, AlignItems.CENTER, JustifyContent.CENTER,
                Padding.MEDIUM, Margin.NONE
        );
//        header.getStyle().set("font-family", "Times-New-Roman, serif");
        header.setText(strAlbumTitle);
        if (strAlbumTitle.trim().isEmpty() || strAlbumTitle.equalsIgnoreCase("null")) {
            header.setText("");
            header.setHeight("1px");
            header.setVisible(false);
        }

        Div subtitle = new Div();
        subtitle.addClassNames(FontSize.SMALL,
                Width.FULL, TextAlignment.LEFT, AlignItems.CENTER, JustifyContent.CENTER,
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
        Div divPhotoCount = new Div(intAlbumPhotoCount + "");
        layoutPhotoCount.add(FontAwesome.Regular.IMAGES.create(), divPhotoCount);
        Div divCountLabel = new Div("Photos");
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
        H4 divDateCreated = new H4(strDateAlbumCreated);
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


        HorizontalLayout layoutViewCountAll = new HorizontalLayout();
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
        // layoutViewCount.add(FontAwesome.Regular.EYE.create(), divViews);
        Span divViewsLabel = new Span("Views");
//        divViewsLabel.addClassNames(FontSize.XXSMALL);
//        layoutViewCountAll.add(layoutViewCount, divViewsLabel);

        layoutViewCountAll.add(FontAwesome.Regular.EYE.create(), divViews, divViewsLabel);

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


        Image imgAvatarSmall = genericView.getAvatarThumbImage(strAvatarPath, strAlbumNameOfUser, "40px", "40px");
        //Image imgAvatarSmall = getAvatarImage(strAvatar, strAlbumUserName, "40px", "40px");

        Image imgAvatarMedium = genericView.getAvatarThumbImage(strAvatarPath, strAlbumNameOfUser, "70px", "70px");
//        Image imgAvatarMedium = getAvatarImage(strAvatar, strAlbumUserName, "70px", "70px");

        AvatarItem avatarItemMe = new AvatarItem(strAlbumNameOfUser, "", imgAvatarSmall);
        Details detailsMember = new Details();
        detailsMember.addClassNames(Width.FULL, BorderRadius.SMALL);
//        detailsMember.addThemeVariants(DetailsVariant.FILLED);
        detailsMember.addClassName("member-small");
        detailsMember.setSummary(avatarItemMe);
        AvatarItem avatarLargeItemMe = new AvatarItem(strAlbumNameOfUser, "@" + strAlbumUserName, imgAvatarMedium);

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
        detailsMember.add(avatarLargeItemMe, layoutMemberInfo);


        //   layoutPhotosInfo.add(layoutRateAll, layoutViewCountAll, layoutLocationsCountAll, detailsMember);

        RouteParam routeAlbum = new RouteParam("title", strAlbumTitle);
        RouteParam routeMember = new RouteParam("member", strAlbumUserName);

        VerticalLayout layoutAll = new VerticalLayout();
        layoutAll.addClassNames(Padding.MEDIUM, Margin.NONE, Gap.XSMALL, AlignItems.CENTER, JustifyContent.CENTER);

        HorizontalLayout layoutPhotos = new HorizontalLayout();
        layoutPhotos.addClassNames(Gap.LARGE, Padding.XSMALL);
        layoutPhotos.setHeight("90px");
        layoutPhotos.add(divImage1, divImage2);

        HorizontalLayout layoutCounts = new HorizontalLayout();
        layoutCounts.addClassNames(TextColor.TERTIARY, Padding.NONE, Margin.NONE, FontSize.XSMALL);
        layoutCounts.add(layoutPhotoCountAll, layoutViewCountAll);
        layoutAll.setHeight("190px");
        layoutAll.setWidth("320px");
        layoutAll.add(divAlbumTitle, layoutPhotos, layoutCounts, divUser);

        this.addClassName("album-thumb");
        this.add(layoutAll);

        this.setRoute(AlbumsView.class, new RouteParameters(routeMember, routeAlbum));

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


    private VerticalLayout getPhotoMetaDataLayout(Record record) {

        VerticalLayout layoutMetaData = new VerticalLayout();
//                    ,"space_size","space_size_medium", "space_size_thumb","meta_camera_make", "meta_camera_model","meta_lens_make","meta_lens_model"
//                ,"meta_focal_length", "meta_focal_length_ff", "meta_iso"
//                ,"location_by_user","location_area","location_country_code","location_lat","location_lon"

        return layoutMetaData;
    }

    //private void logErrorInDb(Exception e, String function, String hostname, String publicIp, long sessionCreation, String info) {
    //      recordService.logErrorInDb(e, hostname, function, 1, "", publicIp, Long.toString(sessionCreation), info);
    // }


}
