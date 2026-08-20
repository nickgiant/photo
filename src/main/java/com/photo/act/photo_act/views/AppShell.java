package com.photo.act.photo_act.views;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;

/**
 * Vaadin app-shell configuration (theme, push, viewport, default page head).
 *
 * Lives inside the {@code views} package — not on {@link com.photo.act.photo_act.PhotoActApplication}
 * — because {@code WebConfig} restricts Vaadin's classpath scan to
 * {@code com.photo.act.photo_act.views} via {@code @EnableVaadin}. Vaadin requires the single
 * {@link AppShellConfigurator} implementation to be inside a scanned package, or the "my-app"
 * theme (and its CSS) silently fails to apply and the app falls back to default Lumo styling.
 */
@Push
@Theme(value = "my-app")
@Viewport("width=device-width, initial-scale=1")
public class AppShell implements AppShellConfigurator {
    // NOTE: AppShell (the AppShellConfigurator carrying @Theme("my-app")) lives in this
// same "views" package for exactly this reason — see AppShell.java.

    /**
     * configurePage() is called once per bootstrap HTML response.
     * Sets default OG tags shown to bots that somehow bypass Nginx,
     * and any global head elements real users need.
     */
    @Override
    public void configurePage(AppShellSettings settings) {
        // Default OG image — shown when no specific content meta is set
        settings.addMetaTag("og:image", "https://www.photoact.net/static/photographer.jpg");
        settings.addMetaTag("og:image:width",  "1200");
        settings.addMetaTag("og:image:height", "630");
        settings.addFavIcon("icon", "camera.png", "512x512");
        settings.setPageTitle(MainLayout.baseMoto);
        settings.addMetaTag("description", "Community website of photographers, sharing their photos, stories, learning sources and events.");
    }
}
