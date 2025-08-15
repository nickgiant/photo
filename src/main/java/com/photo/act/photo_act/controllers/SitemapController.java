package com.photo.act.photo_act.controllers;

import com.photo.act.photo_act.utils.XmlUrl;
import com.photo.act.photo_act.utils.XmlUrlSet;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Controller
//@RequestMapping(path = "/api")
public class SitemapController {
    private List<String> URLS = List.of("/", "/home",  "/albums", "/photos", "/events", "/learnings","/members","/me");
    private String DOMAIN = "https://photoact.net";

    @GetMapping(value = "/api/sitemap.xml")
    @ResponseBody
    public XmlUrlSet main() {
        XmlUrlSet xmlUrlSet = new XmlUrlSet();
        for (String eachLink : URLS) {
            create(xmlUrlSet, eachLink, XmlUrl.Priority.HIGH);
        }
        return xmlUrlSet;
    }

    @GetMapping(value = "/sitemap.xml")
    @ResponseBody
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
    @ResponseBody
    public String getRobotsTxt() {
        return "User-agent: *\n" +
                "Disallow: /admin /upload\n";
    }
}