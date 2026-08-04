package com.photo.act.photo_act;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import static com.photo.act.photo_act.views.MainLayout.baseMoto;

@SpringBootApplication
@ComponentScan(basePackages = {"com.photo.act.photo_act.config", "com.photo.act.photo_act.seo", "com.photo.act.photo_act.services",
        "com.photo.act.photo_act.db", "com.photo.act.photo_act.utils", "com.photo.act.photo_act.views",
        "com.photo.act.photo_act.controllers", "com.photo.act.photo_act.repository"})
@EnableAsync
@EnableScheduling
@Push
@Theme(value = "my-app")
@Viewport("width=device-width, initial-scale=1")
public class PhotoActApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(PhotoActApplication.class, args);
    }

    /**
     * configurePage() is called once per bootstrap HTML response.
     * Sets default OG tags shown to bots that somehow bypass Nginx,
     * and any global head elements real users need.
     */
    @Override
    public void configurePage(AppShellSettings settings) {
//        settings.addFavIcon("icon", "/static/favicon.ico", "32x32");

        // Default OG image — shown when no specific content meta is set
        settings.addMetaTag("og:image", "https://www.photoact.net/static/photographer.jpg");
        settings.addMetaTag("og:image:width",  "1200");
        settings.addMetaTag("og:image:height", "630");
//        settings.setPageTitle("YourSiteName — Discover Amazing Content");
        settings.addFavIcon("icon", "camera.png", "512x512");
        settings.setPageTitle(baseMoto);
        settings.addMetaTag("description", "Community website of photographers, sharing their photos, stories, learning sources and events.");
    }

}
