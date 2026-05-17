package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.model.ShareType;
import com.photo.act.photo_act.model.ShareableResource;
import com.photo.act.photo_act.services.PhotoStoryViewService;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.photo.act.photo_act.views.StoriesView;
import com.photo.act.photo_act.views.StoryView;
import com.photo.act.photo_act.views.components.LikeButton;
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
import com.vaadin.flow.component.notification.Notification;
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
import java.time.LocalDateTime;

public class StoryViewCard extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(StoryViewCard.class);
    private RecordService recordService;
    private boolean isMobile;
    private GenericView genericView;

    private String dirChar = FileSystems.getDefault().getSeparator();

    public StoryViewCard(Record record, String strImagePath, boolean isMobile, int userId, String strUserName, long sessionCreation,
                         String hostname, String publicIp, boolean isEditable, RecordService recordService,
                         PhotoStoryViewService photoStoryViewService, String clientIp,
                         String sessionId, LocalDateTime sessionDateTime,
                         ShareService shareService, ShareMetricService shareMetricService, String baseUrl) {
        this.recordService = recordService;
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
        String strStoryId = record.getColumnData("story_id");

        // Parse story id and record a List view
        int storyId = 0;
        try { storyId = Integer.parseInt(strStoryId); } catch (NumberFormatException ignored) {}
        if (photoStoryViewService != null && storyId > 0) {
            Integer viewUserId = userId > 0 ? userId : null;
            photoStoryViewService.recordView(storyId, strSlug, viewUserId, clientIp,
                    PhotoStoryViewService.TYPE_LIST, sessionId, sessionDateTime);
        }

        // Fetch real counts
        long viewCount = photoStoryViewService != null && storyId > 0
                ? photoStoryViewService.getViewCount(storyId) : 0;
        long likeCount = photoStoryViewService != null && storyId > 0
                ? photoStoryViewService.getLikeCount(storyId) : 0;

        final int finalStoryId = storyId;
        final String finalSlug  = strSlug;

        String strStoryPhotoCount = record.getColumnData("story_photo_count");

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

        String storyPublicUrl = baseUrl + "/stories/member/" + strStoryUserName + "/story/" + strSlug;
        ShareableResource storyResource = new ShareableResource(
                ShareType.PHOTO_STORY,
                String.valueOf(storyId),
                strTitle.isBlank() || strTitle.equalsIgnoreCase("null") ? "Photo Story" : strTitle,
                strDescription.isBlank() || strDescription.equalsIgnoreCase("null") ? "" : strDescription,
                "",
                storyPublicUrl
        );

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
        H3 header = new H3();
        header.addClassNames(FontSize.LARGE, FontWeight.SEMIBOLD,
                Width.FULL, TextAlignment.CENTER, AlignItems.CENTER, JustifyContent.CENTER,
                Padding.XSMALL, Margin.NONE
        );
        header.setText(strTitle);
        if (strTitle.trim().isEmpty() || strTitle.equalsIgnoreCase("null")) {
            header.setText("");
            header.setHeight("1px");
            header.setVisible(false);
        }

        HorizontalLayout layoutCategoryAll = new HorizontalLayout();
        layoutCategoryAll.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                BorderRadius.NONE
        );
        HorizontalLayout layoutCategory = new HorizontalLayout();
        layoutCategory.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                BorderRadius.NONE
        );
        H5 divCategory = new H5(strCategory);
        divCategory.addClassNames(AlignItems.CENTER, TextAlignment.CENTER, JustifyContent.CENTER);
        layoutCategory.add(FontAwesome.Solid.TAG.create(), divCategory);

        layoutCategoryAll.add(layoutCategory);

        HorizontalLayout layoutPhotoCountAll = new HorizontalLayout();
        layoutPhotoCountAll.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                BorderRadius.NONE
        );
        HorizontalLayout layoutPhotoCount = new HorizontalLayout();
        layoutPhotoCount.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                BorderRadius.NONE
        );

        HorizontalLayout layoutDateAll = new HorizontalLayout();
        layoutDateAll.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                BorderRadius.NONE
        );
        HorizontalLayout layoutDate = new HorizontalLayout();
        layoutDate.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                BorderRadius.NONE
        );
        Div divDateCreated = new Div(strDateCreated);
        divDateCreated.addClassNames(AlignItems.CENTER, TextAlignment.CENTER, JustifyContent.CENTER);
        layoutDate.add(FontAwesome.Solid.CALENDAR_DAY.create(), divDateCreated);

        layoutDateAll.add(layoutDate);

        Image imgAvatarSmall = genericView.getAvatarThumbImage(strAvatarPath, strStoryNameOfUser, "40px", "40px");
        Image imgAvatarMedium = genericView.getAvatarThumbImage(strAvatarPath, strStoryNameOfUser, "70px", "70px");

        AvatarItem avatarItemMe = new AvatarItem(strStoryNameOfUser, "", imgAvatarSmall);
        AvatarItem avatarLargeItemMe = new AvatarItem(strStoryNameOfUser, "@" + strStoryUserName, imgAvatarMedium);

        HorizontalLayout divSubHeaderAll = new HorizontalLayout();
        divSubHeaderAll.addClassNames(Width.FULL,
                AlignItems.CENTER, JustifyContent.BETWEEN,
                Margin.NONE, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL);
        divSubHeaderAll.add(layoutCategoryAll, layoutDateAll, avatarItemMe);

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
                BorderRadius.NONE
        );
        layoutPhotosInfo.addClassName("summary");

        LikeButton btnLike = new LikeButton(likeCount);

        HorizontalLayout layoutViewCount = new HorizontalLayout();
        layoutViewCount.addClassNames(
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.NONE,
                Padding.NONE,
                Gap.XSMALL,
                BorderRadius.NONE
        );
        Span divViews = new Span(viewCount > 0 ? String.valueOf(viewCount) : "");

        layoutViewCount.add(FontAwesome.Regular.EYE.create(),divViews);

        RouteParam routeMember = new RouteParam("member", strStoryUserName);
        RouteParam routeStory = new RouteParam("story", strSlug);

        // ── Compose the single action bar ────────────────────────────────────
        ShareBottomBar shareBar = new ShareBottomBar(storyResource, shareService, shareMetricService);
        shareBar.addComponent(layoutViewCount);
        shareBar.addButton("Like it!",btnLike,
                ()-> {
                    if (photoStoryViewService != null && finalStoryId > 0) {
                        Integer likeUserId = userId > 0 ? userId : null;
                        photoStoryViewService.recordLike(finalStoryId, finalSlug, likeUserId, clientIp,
                                sessionId, sessionDateTime);
                        btnLike.setCount(photoStoryViewService.getLikeCount(finalStoryId));
                    }
                }
                ,"btn-bar-share");
        shareBar.addButton("View Story",
                FontAwesome.Solid.ARROW_RIGHT.create(),
                () -> getUI().ifPresent(ui ->
                        ui.navigate(StoriesView.class, new RouteParameters(routeMember, routeStory))),
                "btn-bar-view");

        shareBar.addShareItemMenu();

        this.add(divImage, header, subtitle, divSubHeaderAll,
                buildActionBar(shareBar));

    }

    /** Wraps the composed ShareBottomBar in a full-width action bar row. */
    private HorizontalLayout buildActionBar(ShareBottomBar shareBar) {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassNames(Width.FULL, AlignItems.CENTER, JustifyContent.CENTER,
                Padding.XSMALL, Margin.NONE);
        bar.addClassName("story-bottom-bar");
        bar.add(shareBar);
        return bar;
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
