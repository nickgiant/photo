package com.photo.act.photo_act.views.components;


import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.model.ShareableResource;
import com.photo.act.photo_act.services.ShareMetricService;
import com.photo.act.photo_act.services.ShareService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.server.streams.DownloadHandler;

public class ShareBottomBar extends MenuBar {

    private final ShareableResource resource;
    private final ShareService shareService;
    private final ShareMetricService metricService;

    private MenuItem rootItem;

    public ShareBottomBar(ShareableResource resource,
                          ShareService shareService,
                          ShareMetricService metricService) {

        this.resource = resource;
        this.shareService = shareService;
        this.metricService = metricService;

        addClassName("share-bottom-menubar");
        addThemeVariants(MenuBarVariant.LUMO_DROPDOWN_INDICATORS, MenuBarVariant.LUMO_ICON);

    }

    public void addShareItemMenu(){

        addCopyItem();
        SvgIcon svgShare = new SvgIcon(DownloadHandler.forClassResource(GalleryImageViewCard.class, "/icons/share-line-icon.svg"));
        rootItem = createIconItem(this, svgShare, "", null);

        Tooltip tooltip = Tooltip.forComponent(rootItem);
//        tooltip.setAllowHtml(true);
        tooltip.setText("Share Menu");

        buildSubMenu();



        enableWebShareFirst();
        enableClickOutsideClose();
        enableEscapeClose();


    }

    /* ----------------------------------
       Build Sub Menu
     ---------------------------------- */

    private void buildSubMenu() {

        addShareItem("Facebook", FontAwesome.Brands.FACEBOOK_F.create(),
                shareService.facebook(resource.publicUrl()),
                "facebook",null, true);

        addShareItem("Pinterest", FontAwesome.Brands.PINTEREST_P.create(),
                shareService.pinterest(
                        resource.publicUrl(),
                        resource.imageUrl(),
                        resource.description()),
                "pinterest", null, true);

        addShareItem("LinkedIn", FontAwesome.Brands.LINKEDIN_IN.create(),
                shareService.linkedIn(resource.publicUrl()),
                "linkedin", null, true);

/*        addShareItem("Email", VaadinIcon.MAILBOX.create(),
                shareService.email(resource.title(),
                        resource.publicUrl()),
                "email", null,true);*/

    }

    private void addShareItem(String label,
                              Component icon,
                              String url,
                              String platform, String ariaLabel, boolean isChild) {


        //        Icon icon = new Icon(iconName);

        if (isChild) {
            icon.getStyle().setWidth("var(--lumo-icon-size-s)");
            icon.getStyle().setHeight("var(--lumo-icon-size-s)");
            icon.getStyle().setMarginRight("var(--lumo-space-s)");
        }


        long count = metricService.getCount(platform, resource);

        MenuItem item = createIconItem(rootItem.getSubMenu(),icon, label,null,true, url,platform);


//                = rootItem.getSubMenu()
//                .addItem(icon + " (" + count + ")", e -> {
//
//                    openInNewTab(url);
//                    metricService.increment(platform, resource);
//                    close();
//                });
//
//        if (ariaLabel != null) {
//            item.setAriaLabel(ariaLabel);
//        }
//
//        if (label != null) {
//            item.add(new Text(label));
//        }

//        Tooltip tooltip = Tooltip.forComponent(item);
////        tooltip.setAllowHtml(true);
//        tooltip.setText("""
//                <b>%s</b><br>
//                %s<br>
//                <i>%d shares</i>
//                """.formatted(label,
//                resource.title(),
//                count));
    }

    private void addCopyItem() {

      //  MenuItem item = rootItem.getSubMenu()
        MenuItem item = addItem(FontAwesome.Solid.LINK.create(), e -> {

                    getUI().get().getPage()
                            .executeJs("navigator.clipboard.writeText($0)",
                                    resource.publicUrl());

                    metricService.increment("copy", resource);
                    close();

                    Notification.show("Copied !",3000, Notification.Position.MIDDLE);
                });

        Tooltip tooltip = Tooltip.forComponent(item);
//        tooltip.setAllowHtml(true);
        tooltip.setText("Copy URL to clipboard");
    }

    private MenuItem createIconItem(HasMenuItems menu, Component iconName,
                                    String label, String ariaLabel) {

//                Icon icon = new Icon(iconName);
        return createIconItem(menu, iconName, label, ariaLabel, false, "","");
    }

    private MenuItem createIconItem(HasMenuItems menu, Component icon,
                                    String label, String ariaLabel, boolean isChild, String url, String platform) {
//        Icon icon = new Icon(iconName);

        if (isChild) {
            icon.getStyle().setWidth("var(--lumo-icon-size-s)");
            icon.getStyle().setHeight("var(--lumo-icon-size-s)");
            icon.getStyle().setMarginRight("var(--lumo-space-s)");
        }

        MenuItem item = menu.addItem(icon, e -> {
            if(isChild) {
                openInNewTab(url);
                metricService.increment(platform, resource);
                close();
            }
        });

        if (ariaLabel != null) {
            item.setAriaLabel(ariaLabel);
        }

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }

    /* ----------------------------------
       Web Share API First
     ---------------------------------- */

    private void enableWebShareFirst() {

        rootItem.addClickListener(e -> {

            Page page = getUI().get().getPage();

            page.executeJs("""
                if (navigator.share) {
                    navigator.share({
                        title: $0,
                        text: $1,
                        url: $2
                    }).then(() => {
                        $3.$server.webShareCompleted();
                    }).catch(() => {});
                }
            """,
                    resource.title(),
                    resource.description(),
                    resource.publicUrl(),
                    getElement());
        });
    }

    @ClientCallable
    private void webShareCompleted() {
        metricService.increment("native", resource);
        close();
    }

    /* ----------------------------------
       Behavior Controls
     ---------------------------------- */

    public void open() {
        getElement().getClassList().add("sheet-open");
    }

    public void close() {
        getElement().getClassList().remove("sheet-open");
    }

    private void enableClickOutsideClose() {

        getElement().executeJs("""
            const sheet = this;
            document.addEventListener('click', function(event) {
                if (!sheet.contains(event.target)) {
                    sheet.$server.closeFromClient();
                }
            });
        """);
    }

    private void enableEscapeClose() {

        getElement().executeJs("""
            document.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') {
                    $0.$server.closeFromClient();
                }
            });
        """, getElement());
    }

    @ClientCallable
    private void closeFromClient() {
        close();
    }

    private void openInNewTab(String url) {
        getUI().get().getPage().open(url, "_blank");
    }
}

