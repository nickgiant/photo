package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.model.ShareableResource;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Horizontal share button bar for photos and stories.
 *
 * Platforms:
 *   Facebook   — og:image preview (1200×630 via CDN og/ variant)
 *   Instagram  — Web Share API on mobile; desktop shows copy prompt
 *   Threads    — text + URL intent; og:image preview via crawler
 *   Pinterest  — explicit image URL (CDN pinterest/ variant 1000×1500)
 *   LinkedIn   — og:image preview (1200×630)
 *   Twitter/X  — og:image / twitter:image preview
 *   WhatsApp   — message with URL; og:image preview
 *   Copy URL   — copies canonical URL to clipboard
 *   Web Share  — native device share sheet (all platforms + apps)
 *
 * OG image requirements enforced in CDN pipeline:
 *   og/          1200×630  JPEG ≤ 8 MB   Facebook / Instagram / LinkedIn / Threads / WhatsApp / Twitter
 *   pinterest/   1000×1500 JPEG ≤ 10 MB  Pinterest portrait
 */
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
     * Appends the full share button set inside a popover, plus a Copy URL button.
     * Call this after any extra view-specific buttons so share actions appear last.
     */
    public void addShareItemMenu() {

        // The share icon opens a popover containing all platform buttons
        SvgIcon svgShare = new SvgIcon(
                DownloadHandler.forClassResource(GalleryImageViewCard.class,
                        "/icons/share-line-icon.svg"));
        svgShare.getStyle().set("pointer-events", "none");

        Popover popover = new Popover();
        popover.addClassNames(
                LumoUtility.Padding.LARGE,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER);
        popover.setOpenOnClick(true);
        popover.addClassName("btn-bar-wrapper");

        // Row 1 — primary platforms
        HorizontalLayout row1 = new HorizontalLayout();
        row1.addClassNames(
                LumoUtility.Padding.SMALL,
                LumoUtility.Padding.Top.XLARGE,
                LumoUtility.Padding.Left.LARGE,
                LumoUtility.Padding.Right.LARGE,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER);

        // Row 2 — secondary platforms
        HorizontalLayout row2 = new HorizontalLayout();
        row2.addClassNames(
                LumoUtility.Padding.SMALL,
                LumoUtility.Padding.Left.LARGE,
                LumoUtility.Padding.Right.LARGE,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER);

        popover.add(row1, row2);

        // ── Row 1: Facebook, Instagram, Threads, Pinterest ────────────────────

        row1.add(addButton("Facebook",
                FontAwesome.Brands.FACEBOOK_F.create(),
                () -> {
                    openTab(shareService.facebook(resource.publicUrl()));
                    metricService.increment("facebook", resource);
                },
                "btn-bar-facebook"));

        row1.add(addButton("Instagram",
                FontAwesome.Brands.INSTAGRAM.create(),
                this::triggerInstagramShare,
                "btn-bar-instagram"));

        row1.add(addButton("Threads",
                buildTextIcon("T"),
                () -> {
                    openTab(shareService.threads(resource.publicUrl(), resource.title()));
                    metricService.increment("threads", resource);
                },
                "btn-bar-threads"));

        row1.add(addButton("Pinterest",
                FontAwesome.Brands.PINTEREST_P.create(),
                () -> {
                    openTab(shareService.pinterest(
                            resource.publicUrl(),
                            resource.imageUrl(),
                            resource.description()));
                    metricService.increment("pinterest", resource);
                },
                "btn-bar-pinterest"));

        // ── Row 2: LinkedIn, Twitter/X, WhatsApp, Web Share ──────────────────

        row2.add(addButton("LinkedIn",
                FontAwesome.Brands.LINKEDIN_IN.create(),
                () -> {
                    openTab(shareService.linkedIn(resource.publicUrl()));
                    metricService.increment("linkedin", resource);
                },
                "btn-bar-linkedin"));

        row2.add(addButton("Twitter / X",
                FontAwesome.Brands.TWITTER.create(),
                () -> {
                    openTab(shareService.twitter(resource.publicUrl(), resource.title()));
                    metricService.increment("twitter", resource);
                },
                "btn-bar-twitter"));

        row2.add(addButton("WhatsApp",
                FontAwesome.Brands.WHATSAPP.create(),
                () -> {
                    openTab(shareService.whatsApp(resource.publicUrl(), resource.title()));
                    metricService.increment("whatsapp", resource);
                },
                "btn-bar-whatsapp"));

        // Native Web Share — falls back gracefully on desktop
        row2.add(addButton("More…",
                FontAwesome.Solid.SHARE.create(),
                this::triggerWebShare,
                "btn-bar-native"));

        // The share icon is the popover trigger
        Div divShare = addButton("Share", svgShare, this::triggerWebShare, "btn-bar-share");
        popover.setTarget(divShare);
        add(divShare);

        // Copy URL — always visible outside the popover
        addButton("Copy URL",
                FontAwesome.Solid.LINK.create(),
                this::copyUrl,
                "btn-bar-copy");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Instagram: on mobile, navigator.share() shows Instagram as a target.
     * On desktop there is no share URL — we copy the link and show a hint.
     */
    private void triggerInstagramShare() {
        getUI().ifPresent(ui -> ui.getPage().executeJs("""
                if (navigator.share) {
                    navigator.share({ title: $0, text: $1, url: $2 })
                        .then(() => $3.$server.webShareCompleted())
                        .catch(() => {});
                } else {
                    navigator.clipboard.writeText($2)
                        .then(() => $3.$server.instagramCopyCompleted())
                        .catch(() => {});
                }
                """,
                resource.title(),
                resource.description(),
                resource.publicUrl(),
                getElement()));
        metricService.increment("instagram", resource);
    }

    private void triggerWebShare() {
        getUI().ifPresent(ui -> ui.getPage().executeJs("""
                if (navigator.share) {
                    navigator.share({ title: $0, text: $1, url: $2 })
                        .then(() => $3.$server.webShareCompleted())
                        .catch(() => {});
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

    @ClientCallable
    private void instagramCopyCompleted() {
        Notification.show(
                "Link copied! Open Instagram and paste it in your story or bio.",
                4000,
                Notification.Position.MIDDLE);
    }

    private void copyUrl() {
        getUI().ifPresent(ui -> {
            ui.getPage().executeJs("navigator.clipboard.writeText($0)", resource.publicUrl());
            metricService.increment("copy", resource);
            Notification.show("Link copied!", 2500, Notification.Position.MIDDLE);
        });
    }

    private void openTab(String url) {
        getUI().ifPresent(ui -> ui.getPage().open(url, "_blank"));
    }

    /** Text-based icon for platforms whose FA icon may not be in the current addon version. */
    private Component buildTextIcon(String text) {
        Span s = new Span(text);
        s.getStyle()
                .set("font-weight", "700")
                .set("font-size", "14px")
                .set("pointer-events", "none");
        return s;
    }
}
