package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tutor",
    indexes = {
        @Index(name = "idx_tutor_name", columnList = "tutor_name")
    })
public class TutorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutor_name", nullable = false, length = 255)
    private String tutorName;

    @Column(name = "website", length = 512)
    private String website;

    @Column(name = "website_gallery", length = 512)
    private String websiteGallery;

    @Column(name = "website_gallery_2", length = 512)
    private String websiteGallery2;

    @Column(name = "url_fb", length = 512)
    private String urlFb;

    @Column(name = "url_yt", length = 512)
    private String urlYt;

    @Column(name = "url_insta", length = 512)
    private String urlInsta;

    @Column(name = "url_flickr", length = 512)
    private String urlFlickr;

    @Column(name = "url_wikipedia", length = 512)
    private String urlWikipedia;

    @Column(name = "url_ref1", length = 512)
    private String urlRef1;

    @Column(name = "url_ref2", length = 512)
    private String urlRef2;

    @Column(name = "url_ref3", length = 512)
    private String urlRef3;

    @Column(name = "city_base", length = 100)
    private String cityBase;

    @Column(name = "country_base", length = 100)
    private String countryBase;

    @Column(name = "userIdInsert")
    private Integer userIdInsert;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "date_inserted", updatable = false)
    private LocalDateTime dateInserted;

    protected TutorEntity() {}

    public TutorEntity(String tutorName, String website, String websiteGallery, String websiteGallery2,
                       String urlFb, String urlYt,
                       String urlInsta, String urlFlickr, String urlWikipedia,
                       String urlRef1, String urlRef2, String urlRef3,
                       String cityBase, String countryBase,
                       Integer userIdInsert, String username) {
        this.tutorName       = tutorName;
        this.website         = website;
        this.websiteGallery  = websiteGallery;
        this.websiteGallery2 = websiteGallery2;
        this.urlFb           = urlFb;
        this.urlYt           = urlYt;
        this.urlInsta        = urlInsta;
        this.urlFlickr       = urlFlickr;
        this.urlWikipedia    = urlWikipedia;
        this.urlRef1         = urlRef1;
        this.urlRef2         = urlRef2;
        this.urlRef3         = urlRef3;
        this.cityBase        = cityBase;
        this.countryBase     = countryBase;
        this.userIdInsert    = userIdInsert;
        this.username        = username;
    }

    @PrePersist
    private void onPersist() {
        if (dateInserted == null) dateInserted = LocalDateTime.now();
    }

    public Long          getId()           { return id; }
    public String        getTutorName()    { return tutorName; }
    public String        getWebsite()      { return website; }
    public String        getWebsiteGallery()  { return websiteGallery; }
    public String        getWebsiteGallery2() { return websiteGallery2; }
    public String        getUrlFb()        { return urlFb; }
    public String        getUrlYt()        { return urlYt; }
    public String        getUrlInsta()     { return urlInsta; }
    public String        getUrlFlickr()    { return urlFlickr; }
    public String        getUrlWikipedia() { return urlWikipedia; }
    public String        getUrlRef1()      { return urlRef1; }
    public String        getUrlRef2()      { return urlRef2; }
    public String        getUrlRef3()      { return urlRef3; }
    public String        getCityBase()     { return cityBase; }
    public String        getCountryBase()  { return countryBase; }
    public Integer       getUserIdInsert() { return userIdInsert; }
    public String        getUsername()     { return username; }
    public LocalDateTime getDateInserted() { return dateInserted; }

    public void setTutorName(String tutorName)       { this.tutorName = tutorName; }
    public void setWebsite(String website)           { this.website = website; }
    public void setWebsiteGallery(String websiteGallery)   { this.websiteGallery = websiteGallery; }
    public void setWebsiteGallery2(String websiteGallery2) { this.websiteGallery2 = websiteGallery2; }
    public void setUrlFb(String urlFb)               { this.urlFb = urlFb; }
    public void setUrlYt(String urlYt)               { this.urlYt = urlYt; }
    public void setUrlInsta(String urlInsta)         { this.urlInsta = urlInsta; }
    public void setUrlFlickr(String urlFlickr)       { this.urlFlickr = urlFlickr; }
    public void setUrlWikipedia(String urlWikipedia) { this.urlWikipedia = urlWikipedia; }
    public void setUrlRef1(String urlRef1)           { this.urlRef1 = urlRef1; }
    public void setUrlRef2(String urlRef2)           { this.urlRef2 = urlRef2; }
    public void setUrlRef3(String urlRef3)           { this.urlRef3 = urlRef3; }
    public void setCityBase(String cityBase)         { this.cityBase = cityBase; }
    public void setCountryBase(String countryBase)   { this.countryBase = countryBase; }
}
