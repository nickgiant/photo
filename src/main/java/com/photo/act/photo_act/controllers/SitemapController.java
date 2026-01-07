package com.photo.act.photo_act.controllers;

import com.photo.act.photo_act.utils.XmlUrl;
import com.photo.act.photo_act.utils.XmlUrlSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(path = "/api")
public class SitemapController {
    private List<String> URLS = List.of("/", "/home", "/albums", "/photos", "/events", "/learnings", "/members", "/upload", "/me");
    private String DOMAIN = "https://photoact.net";

//    @GetMapping(value = "/api/sitemap.xml", produces = "application/xml")
//    public XmlUrlSet main() {
//        XmlUrlSet xmlUrlSet = new XmlUrlSet();
//        for (String eachLink : URLS) {
//            create(xmlUrlSet, eachLink, XmlUrl.Priority.HIGH);
//        }
//        return xmlUrlSet;
//    }

    @GetMapping(value = "/sitemap.xml", produces = "application/xml")
    public XmlUrlSet getSitemap() {
        XmlUrlSet xmlUrlSet = new XmlUrlSet();
        for (String eachLink : URLS) {
            create(xmlUrlSet, eachLink, XmlUrl.Priority.HIGH);
        }
        return xmlUrlSet;
    }

    private void create(XmlUrlSet xmlUrlSet, String link, XmlUrl.Priority priority) {
        xmlUrlSet.addUrl(new XmlUrl(DOMAIN + link, priority));
    }

    @GetMapping(value = {"/robots.txt", "/robot.txt"})
    public String getRobotsTxt() {
        return "User-agent: *" +
                "Disallow: /admin /upload";
    }
}