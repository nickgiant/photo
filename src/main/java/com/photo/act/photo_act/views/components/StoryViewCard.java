package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.model.ShareType;
import com.photo.act.photo_act.model.ShareableResource;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.photo.act.photo_act.views.StoriesView;
import com.photo.act.photo_act.views.StoryView;
import com.vaadin.flow.component.UI;
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

import static com.photo.act.photo_act.views.MainLayout.baseUrl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StoryViewCard extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(StoryViewCard.class);
    private RecordService recordService;
    private ShareService shareService;
    private ShareMetricService shareMetricService;
    private boolean isMobile;
    private GenericView genericView;

    private String dirChar = FileSystems.getDefault().getSeparator();

    public StoryViewCard(Record record, String strImagePath, boolean isMobile, int userId, String strUserName, long sessionCreation,
                         String hostname, String publicIp, boolean isEditable, RecordService recordService,
                         ShareService shareService, ShareMetricService shareMetricService) {
        this.recordService = recordService;
        this.shareService = shareService;
        this.shareMetricService = shareMetricService;
        this.isMobile = isMobile;


        genericView = new GenericView(recordService);

        this.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN, TextAlignment.CENTER);
        this.addClassName("story-list-card");


        if (record == null) {
            logger.error("record is null");
        }

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSlug = record.getColumnData("slug");
        String strDescription = record.getColumnData("description");

        String strStoryPhotoCount = record.getColumnData("story_photo_count");

//        String strCreator = record.getColumnData("creator");
        String strVisibleTo = record.getColumnData("visible_to");
        String strPhotoUrl = record.getColumnData("name_new");

        String strCategory = record.getColumnData("cat_title");
        String strCategoryGr = record.getColumnData("cat_title");

        String strDateCreated = record.getColumnData("datetime_story_created");
        String strPhoto1 = record.getColumnData("photo_1");
        String strPhoto2 = record.getColumnData("photo_2");

        String strDateAlbumCreated = record.getColumnData("datetime_album_created");

        String strStoryUserName = record.getColumnData("username");
        String strStoryNameOfUser = record.getColumnData("name") +" "+ record.getColumnData("surname");
        String strUserResident = record.getColumnData("resident");
        String strAvatarPath = record.getColumnData("avatar_path");
        String strUserJoined = record.getColumnData("date_joined");

        String strItemTitle = record.getColumnData("item_title");
        String strItemDescr = record.getColumnData("descr");


        String strCity = "";
        if (!record.getColumnData("city_name").isEmpty()) {
            strCity = record.getColumnData("city_name");
        }

        if (strPhoto1 != null && !strPhoto1.isEmpty() && !strPhoto1.equalsIgnoreCase("null")) {
            strPhotoUrl = strPhoto1;
        }

        String imagePath = strImagePath + dirChar + strPhotoUrl;
        Path path = Paths.get(imagePath);
        File file = path.toFile();

        logger.info("imagePath: " + imagePath);

        final StreamResource imageResource = new StreamResource("streamResource", () -> {
            try {
                //ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
                //imageUtilsMeta.printPhotoMetadataValue(file);
                logger.info("StoryViewCard file: " + file.getAbsolutePath());
                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
                logErrorInDb(e, "StoryViewCard StreamResource FileNotFoundException", hostname, userId, strUserName, publicIp, sessionCreation, file.getAbsolutePath());
                // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                logger.error("FileNotFoundException  " + e.getMessage());
            }
            return null;
        });


        Div divImage = new Div();

        Image image = new Image();
        image.addClassNames(Width.FULL, Height.FULL);

        image.setSrc(imageResource);
        divImage.add(image);

//        Div divTextDescription = new Div();
//        divTextDescription.addClassNames(Width.FULL, JustifyContent.CENTER, AlignItems.CENTER, Padding.NONE, Margin.NONE);

        H3 header = new H3();
        header.addClassNames(FontSize.LARGE, FontWeight.SEMIBOLD,
                Width.FULL, TextAlignment.CENTER, AlignItems.CENTER, JustifyContent.CENTER,
                Padding.XSMALL, Margin.NONE
        );
//        header.getStyle().set("font-family", "Times-New-Roman, serif");
        header.setText(strTitle);
        if (strTitle.trim().isEmpty() || strTitle.equalsIgnoreCase("null")) {
            header.setText("");
            header.setHeight("1px");
            header.setVisible(false);
        }

        HorizontalLayout layoutCategoryAll = new HorizontalLayout();
        layoutCategoryAll.addClassNames(
                //  Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        HorizontalLayout layoutCategory = new HorizontalLayout();
        layoutCategory.addClassNames(
//                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                //   Background.CONTRAST_5,
                BorderRadius.NONE
        );
        H5 divCategory = new H5(strCategory);
        divCategory.addClassNames(AlignItems.CENTER, TextAlignment.CENTER, JustifyContent.CENTER);
        layoutCategory.add(FontAwesome.Solid.TAG.create(), divCategory);

        layoutCategoryAll.add(layoutCategory);

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
        H4 divPhotoCount = new H4(strStoryPhotoCount);
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
        H4 divDateCreated = new H4(strDateCreated);
        divDateCreated.addClassNames(AlignItems.CENTER, TextAlignment.CENTER, JustifyContent.CENTER);
        layoutDate.add(FontAwesome.Solid.CALENDAR_DAY.create(), divDateCreated);

        layoutDateAll.add(layoutDate);

        HorizontalLayout divSubHeaderAll = new HorizontalLayout();
        divSubHeaderAll.addClassNames(Width.FULL, AlignItems.END,
                JustifyContent.BETWEEN, Margin.NONE, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL);
        divSubHeaderAll.add(layoutCategoryAll, layoutPhotoCountAll, layoutDateAll);

        Paragraph subtitle = new Paragraph();

        if (!strDescription.trim().isEmpty() && !strDescription.equalsIgnoreCase("null")) {
            subtitle.setText(strDescription);
        } else {

        }

        HorizontalLayout layoutPhotosInfo = new HorizontalLayout();
        layoutPhotosInfo.addClassNames(
                Overflow.HIDDEN, Width.FULL,
                AlignItems.CENTER, JustifyContent.EVENLY,
                Margin.NONE,
                Padding.NONE,
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
/*        Div divLocations = new Div("1");
        layoutLocationsCount.add(FontAwesome.Regular.COMPASS.create(), divLocations);
        Span divLocationsLabel = new Span("Locations");
        divLocationsLabel.addClassNames(FontSize.XXSMALL);
        layoutLocationsCountAll.add(layoutLocationsCount, divLocationsLabel);*/

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


        //     divPhotoInfo.add(layoutPhotosInfo); //, layoutUserActions);
//        routerLinkAlbum.add(header, subtitle, layoutImage, divPhotoInfo);


//        Avatar userAvatar = new Avatar(strAlbumUserName);
//        userAvatar.setImage(strAvatar);
//        userAvatar.getElement().setAttribute("tabindex", "-1");
//        userAvatar.addThemeVariants(AvatarVariant.LUMO_SMALL);
//
//        Avatar userAvatarLarge = new Avatar(strAlbumUserName);
//        userAvatarLarge.setImage(strAvatar);
//        userAvatarLarge.getElement().setAttribute("tabindex", "-1");
//        userAvatarLarge.addThemeVariants(AvatarVariant.LUMO_XLARGE);


        Image imgAvatarSmall = genericView.getAvatarThumbImage(strAvatarPath, strStoryNameOfUser, "40px", "40px");
        //Image imgAvatarSmall = getAvatarImage(strAvatar, strAlbumUserName, "40px", "40px");

        Image imgAvatarMedium = genericView.getAvatarThumbImage(strAvatarPath, strStoryNameOfUser, "70px", "70px");
//        Image imgAvatarMedium = getAvatarImage(strAvatar, strAlbumUserName, "70px", "70px");

        AvatarItem avatarItemMe = new AvatarItem(strStoryNameOfUser, "", imgAvatarSmall);
        avatarItemMe.setWidthFull();
/*        Details detailsMember = new Details();
        detailsMember.addClassNames(Width.FULL, BorderRadius.SMALL);
//        detailsMember.addThemeVariants(DetailsVariant.FILLED);
        detailsMember.addClassName("member-small");
        detailsMember.setSummary(avatarItemMe);*/
        AvatarItem avatarLargeItemMe = new AvatarItem(strStoryNameOfUser, "@" + strStoryUserName, imgAvatarMedium);

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
//        detailsMember.add(avatarLargeItemMe, layoutMemberInfo);

        layoutPhotosInfo.add(layoutRateAll, layoutViewCountAll, layoutLocationsCountAll, avatarItemMe);

        RouteParam routeMember = new RouteParam("member", strStoryUserName);
        RouteParam routeStory = new RouteParam("story", strSlug);

        Button btnMore = new Button("View Story");
        btnMore.setIcon(FontAwesome.Solid.ARROW_RIGHT.create());
        btnMore.setIconAfterText(true);

//        card.addClickListener(e -> UI.getCurrent().navigate("news/" + news.getId()));

        btnMore.addClickListener(click -> {
            btnMore.getUI().ifPresent(ui ->
                    ui.navigate(StoriesView.class, new RouteParameters(routeMember, routeStory))
            );
        });


            this.add(divImage, header, subtitle, divSubHeaderAll, layoutPhotosInfo, getActions(btnMore, strStoryUserName, strSlug, strDescription, strPhotoUrl, strTitle, record.getColumnData("story_id")));

    }

    private HorizontalLayout getActions(Button btnMore, String storyUsername, String storySlug, String storyDescription, String coverPhotoFile, String storyTitle, String storyId) {

        HorizontalLayout layoutActions = new HorizontalLayout();
        if (isMobile) {
            layoutActions.addClassNames(
                    Overflow.HIDDEN,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.SMALL,
                    Padding.NONE
            );
            layoutActions.addClassName("actions-mobile");
        } else {
            layoutActions.addClassNames(
                    Overflow.HIDDEN,
                    AlignItems.CENTER, JustifyContent.CENTER,
                    Margin.SMALL,
                    Padding.NONE
            );
        }

        String storyPublicUrl = baseUrl + "/stories/member/" + storyUsername + "/story/" + storySlug;
        String storyImageUrl  = baseUrl + "/photo/" + coverPhotoFile;

        ShareableResource storyResource = new ShareableResource(
                ShareType.PHOTO_STORY,
                storyId,
                storyTitle,
                storyDescription,
                storyImageUrl,
                storyPublicUrl
        );

        ShareBottomBar shareBottomBar = new ShareBottomBar(storyResource, shareService, shareMetricService);
        shareBottomBar.addShareItemMenu();

        layoutActions.add(btnMore, shareBottomBar);

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
