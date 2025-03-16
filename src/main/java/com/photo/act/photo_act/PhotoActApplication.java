package com.photo.act.photo_act;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme(value = "my-app")
public class PhotoActApplication implements AppShellConfigurator {

	public static void main(String[] args)
	{
		SpringApplication.run(PhotoActApplication.class, args);
	}


	public void configurePage(AppShellSettings settings) {
		settings.addFavIcon("icon", "camera.png", "512x512");
	}

}
