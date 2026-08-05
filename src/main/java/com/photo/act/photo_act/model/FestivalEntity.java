package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "festivals",
    indexes = {
        @Index(name = "idx_festival_country",     columnList = "country"),
        @Index(name = "idx_festival_type",         columnList = "type"),
        @Index(name = "idx_festival_date_insert",  columnList = "dateInsert")
    })
public class FestivalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nameShort", nullable = false, length = 255)
    private String nameShort;

    @Column(name = "nameFull", length = 512)
    private String nameFull;

    @Column(name = "periodOfYear", length = 100)
    private String periodOfYear;

    @Column(name = "type", length = 100)
    private String type;

    @Column(name = "website", length = 512)
    private String website;

    @Column(name = "url_facebook", length = 512)
    private String urlFacebook;

    @Column(name = "url_instagram", length = 512)
    private String urlInstagram;

    @Column(name = "url_youtube", length = 512)
    private String urlYoutube;

    @Column(name = "activities", columnDefinition = "TEXT")
    private String activities;

    @Column(name = "image_top", length = 512)
    private String imageTop;

    @Column(name = "image_logo", length = 512)
    private String imageLogo;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "dateInsert", updatable = false)
    private LocalDateTime dateInsert;

    protected FestivalEntity() {}

    public FestivalEntity(String nameShort, String nameFull, String periodOfYear, String type, String website,
                          String urlFacebook, String urlInstagram, String urlYoutube,
                          String activities, String imageTop, String imageLogo, String country) {
        this.nameShort    = nameShort;
        this.nameFull     = nameFull;
        this.periodOfYear = periodOfYear;
        this.type         = type;
        this.website      = website;
        this.urlFacebook  = urlFacebook;
        this.urlInstagram = urlInstagram;
        this.urlYoutube   = urlYoutube;
        this.activities   = activities;
        this.imageTop     = imageTop;
        this.imageLogo    = imageLogo;
        this.country      = country;
    }

    @PrePersist
    private void onPersist() {
        if (dateInsert == null) dateInsert = LocalDateTime.now();
    }

    public Long          getId()           { return id; }
    public String        getNameShort()    { return nameShort; }
    public String        getNameFull()     { return nameFull; }
    public String        getPeriodOfYear() { return periodOfYear; }
    public String        getType()         { return type; }
    public String        getWebsite()      { return website; }
    public String        getUrlFacebook()  { return urlFacebook; }
    public String        getUrlInstagram() { return urlInstagram; }
    public String        getUrlYoutube()   { return urlYoutube; }
    public String        getActivities()   { return activities; }
    public String        getImageTop()     { return imageTop; }
    public String        getImageLogo()    { return imageLogo; }
    public String        getCountry()      { return country; }
    public LocalDateTime getDateInsert()   { return dateInsert; }

    public void setNameShort(String nameShort)       { this.nameShort = nameShort; }
    public void setNameFull(String nameFull)         { this.nameFull = nameFull; }
    public void setPeriodOfYear(String periodOfYear) { this.periodOfYear = periodOfYear; }
    public void setType(String type)                 { this.type = type; }
    public void setWebsite(String website)           { this.website = website; }
    public void setUrlFacebook(String urlFacebook)   { this.urlFacebook = urlFacebook; }
    public void setUrlInstagram(String urlInstagram) { this.urlInstagram = urlInstagram; }
    public void setUrlYoutube(String urlYoutube)     { this.urlYoutube = urlYoutube; }
    public void setActivities(String activities)     { this.activities = activities; }
    public void setImageTop(String imageTop)         { this.imageTop = imageTop; }
    public void setImageLogo(String imageLogo)       { this.imageLogo = imageLogo; }
    public void setCountry(String country)           { this.country = country; }
}
