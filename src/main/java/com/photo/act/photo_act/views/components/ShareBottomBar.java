package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.model.ShareableResource;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class ShareBottomBar extends ButtonBar {

    private final ShareableResource resource;
    private final ShareService shareService;
    private final ShareMetricService metricService;

    public ShareBottomBar(ShareableResource resource,
                          ShareService shareService,
                          ShareMetricService metricService) {

        this.resource = resource;
        this.shareService = shareService;
        this.metricService = metricService;
    }

    /**
     * Appends the standard share buttons (Facebook, Pinterest, LinkedIn, Copy URL)
     * plus an optional Web Share API button when the browser supports it.
     * Call this after adding any extra buttons so that share actions appear last.
     */
    public void addShareItemMenu() {

        // Native Web Share – shown only when navigator.share is available;
        // detected client-side via JS injection below.
        SvgIcon svgShare = new SvgIcon(
                DownloadHandler.forClassResource(GalleryImageViewCard.class,
                        "/icons/share-line-icon.svg"));
        svgShare.getStyle().set("pointer-events", "none");



        Popover popover = new Popover();
        popover.addClassNames(LumoUtility.Padding.LARGE, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
//        popover.setHoverDelay(500);
//        popover.setOpenOnHover(true);
        popover.setOpenOnClick(true);

        popover.addClassName("btn-bar-wrapper");

        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Padding.Top.XLARGE, LumoUtility.Padding.Left.MEDIUM, LumoUtility.Padding.Right.MEDIUM, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        popover.add(layout);

        layout.add(addButton("Facebook",
                FontAwesome.Brands.FACEBOOK_F.create(),
                () -> {
                    openInNewTab(shareService.facebook(resource.publicUrl()));
                    metricService.increment("facebook", resource);
                },
                "btn-bar-facebook")
        );

        layout.add(addButton("Pinterest",
                FontAwesome.Brands.PINTEREST_P.create(),
                () -> {
                    openInNewTab(shareService.pinterest(
                            resource.publicUrl(),
                            resource.imageUrl(),
                            resource.description()));
                    metricService.increment("pinterest", resource);
                },
                "btn-bar-pinterest")
        );

        layout.add(addButton("LinkedIn",
                FontAwesome.Brands.LINKEDIN_IN.create(),
                () -> {
                    openInNewTab(shareService.linkedIn(resource.publicUrl()));
                    metricService.increment("linkedin", resource);
                },
                "btn-bar-linkedin")
        );




      Div divShare =  addButton("Share", svgShare,
                () ->{
                this.triggerWebShare();
                }
                , "btn-bar-share");

        popover.setTarget(divShare);
        add(divShare);

/*
        addButton("Facebook",
                FontAwesome.Brands.FACEBOOK_F.create(),
                () -> {
                    openInNewTab(shareService.facebook(resource.publicUrl()));
                    metricService.increment("facebook", resource);
                },
                "btn-bar-facebook");

        addButton("Pinterest",
                FontAwesome.Brands.PINTEREST_P.create(),
                () -> {
                    openInNewTab(shareService.pinterest(
                            resource.publicUrl(),
                            resource.imageUrl(),
                            resource.description()));
                    metricService.increment("pinterest", resource);
                },
                "btn-bar-pinterest");

        addButton("LinkedIn",
                FontAwesome.Brands.LINKEDIN_IN.create(),
                () -> {
                    openInNewTab(shareService.linkedIn(resource.publicUrl()));
                    metricService.increment("linkedin", resource);
                },
                "btn-bar-linkedin");
*/

        addButton("Copy URL",
                FontAwesome.Solid.LINK.create(),
                this::copyUrl,
                "btn-bar-copy");
    }

    /* ── Private helpers ── */

    private void triggerWebShare() {
        getUI().ifPresent(ui ->
                ui.getPage().executeJs("""
                    if (navigator.share) {
                        navigator.share({
                            title: $0,
                            text: $1,
                            url: $2
                        }).then(() => {
                            $3.$server.webShareCompleted();
                        }).catch(() => {});
                    } else {
                        // no native share – nothing to do, other buttons are visible
                    }
                    """,
                        resource.title(),
                        resource.description(),
                        resource.publicUrl(),
                        getElement()));


    }

    @ClientCallable
    private void webShareCompleted() {
        metricService.increment("native", resource);
    }

    private void copyUrl() {
        getUI().ifPresent(ui -> {
            ui.getPage().executeJs(
                    "navigator.clipboard.writeText($0)", resource.publicUrl());
            metricService.increment("copy", resource);
            Notification.show("Copied!", 3000, Notification.Position.MIDDLE);
        });
    }

    private void openInNewTab(String url) {
        getUI().ifPresent(ui -> ui.getPage().open(url, "_blank"));
    }
}
