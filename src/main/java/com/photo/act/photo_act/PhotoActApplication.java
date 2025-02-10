package com.photo.act.photo_act;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Theme(value = "my-app")
public class PhotoActApplication implements AppShellConfigurator {

	public static void main(String[] args)
	{
		SpringApplication.run(PhotoActApplication.class, args);
	}

//	@Bean
//	public TomcatServletWebServerFactory servletContainer() {
//		return new TomcatServletWebServerFactory() {
//			@Override
//			protected void postProcessContext(org.apache.catalina.Context context) {
//				// Allow larger file uploads
//				context.setMaxSwallowSize(52428800); // 50MB in bytes
//			}
//		};
//	}

}
