package com.photo.act.photo_act;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.photo.act.photo_act.config", "com.photo.act.photo_act.config.controllers", "com.photo.act.photo_act.services" ,
							"com.photo.act.photo_act.db","com.photo.act.photo_act.utils","com.photo.act.photo_act.views"})
@Theme(value = "my-app")
public class PhotoActApplication implements AppShellConfigurator {

	public static void main(String[] args)
	{
		SpringApplication.run(PhotoActApplication.class, args);
	}


	public void configurePage(AppShellSettings settings) {

		settings.addFavIcon("icon", "camera.png", "512x512");
		settings.setPageTitle("photoact.net Act and Network around Photography");
		settings.addMetaTag("description","Community website of photographers, sharing their photos, albums, learning sources and events.");
	}

}
