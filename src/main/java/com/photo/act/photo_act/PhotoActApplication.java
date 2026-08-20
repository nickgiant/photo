package com.photo.act.photo_act;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.photo.act.photo_act.config", "com.photo.act.photo_act.seo", "com.photo.act.photo_act.services",
        "com.photo.act.photo_act.db", "com.photo.act.photo_act.utils", "com.photo.act.photo_act.views",
        "com.photo.act.photo_act.controllers", "com.photo.act.photo_act.repository"})
@EnableAsync
@EnableScheduling
public class PhotoActApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhotoActApplication.class, args);
    }

}
