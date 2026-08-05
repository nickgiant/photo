package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "festivals_edition",
    indexes = {
        @Index(name = "idx_festival_edition_festival_id", columnList = "festival_id"),
        @Index(name = "idx_festival_edition_date_from",   columnList = "dateFrom")
    })
public class FestivalEditionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Write-side FK column → festivals.id */
    @Column(name = "festival_id", nullable = false)
    private Long festivalId;

    /** Read-side FK reference for DDL constraint and lazy loading. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_festival_edition_festival_id"))
    private FestivalEntity festival;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "subtitle", length = 255)
    private String subtitle;

    @Column(name = "dateFrom")
    private LocalDate dateFrom;

    @Column(name = "dateTo")
    private LocalDate dateTo;

    @Column(name = "edition_description", columnDefinition = "TEXT")
    private String editionDescription;

    @Column(name = "title_of_place", length = 255)
    private String titleOfPlace;

    @Column(name = "address_of_place", length = 512)
    private String addressOfPlace;

    @Column(name = "url_planned", length = 512)
    private String urlPlanned;

    @Column(name = "url_fb", length = 512)
    private String urlFb;

    @Column(name = "url_insta", length = 512)
    private String urlInsta;

    protected FestivalEditionEntity() {}

    public FestivalEditionEntity(Long festivalId, String title, String subtitle,
                                 LocalDate dateFrom, LocalDate dateTo, String editionDescription,
                                 String titleOfPlace, String addressOfPlace,
                                 String urlPlanned, String urlFb, String urlInsta) {
        this.festivalId          = festivalId;
        this.title               = title;
        this.subtitle            = subtitle;
        this.dateFrom            = dateFrom;
        this.dateTo              = dateTo;
        this.editionDescription  = editionDescription;
        this.titleOfPlace        = titleOfPlace;
        this.addressOfPlace      = addressOfPlace;
        this.urlPlanned          = urlPlanned;
        this.urlFb               = urlFb;
        this.urlInsta            = urlInsta;
    }

    public Long          getId()                  { return id; }
    public Long          getFestivalId()           { return festivalId; }
    public FestivalEntity getFestival()            { return festival; }
    public String        getTitle()                { return title; }
    public String        getSubtitle()              { return subtitle; }
    public LocalDate     getDateFrom()              { return dateFrom; }
    public LocalDate     getDateTo()                { return dateTo; }
    public String        getEditionDescription()    { return editionDescription; }
    public String        getTitleOfPlace()          { return titleOfPlace; }
    public String        getAddressOfPlace()        { return addressOfPlace; }
    public String        getUrlPlanned()            { return urlPlanned; }
    public String        getUrlFb()                 { return urlFb; }
    public String        getUrlInsta()              { return urlInsta; }

    public void setFestivalId(Long festivalId)                 { this.festivalId = festivalId; }
    public void setTitle(String title)                         { this.title = title; }
    public void setSubtitle(String subtitle)                   { this.subtitle = subtitle; }
    public void setDateFrom(LocalDate dateFrom)                { this.dateFrom = dateFrom; }
    public void setDateTo(LocalDate dateTo)                    { this.dateTo = dateTo; }
    public void setEditionDescription(String editionDescription) { this.editionDescription = editionDescription; }
    public void setTitleOfPlace(String titleOfPlace)           { this.titleOfPlace = titleOfPlace; }
    public void setAddressOfPlace(String addressOfPlace)       { this.addressOfPlace = addressOfPlace; }
    public void setUrlPlanned(String urlPlanned)               { this.urlPlanned = urlPlanned; }
    public void setUrlFb(String urlFb)                         { this.urlFb = urlFb; }
    public void setUrlInsta(String urlInsta)                   { this.urlInsta = urlInsta; }
}
