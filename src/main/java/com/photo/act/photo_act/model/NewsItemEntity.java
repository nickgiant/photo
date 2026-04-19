package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news_items",
    indexes = @Index(name = "idx_news_items_news_id", columnList = "news_id"))
public class NewsItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Write-side FK column. */
    @Column(name = "news_id", nullable = false)
    private Long newsId;

    /** Read-side FK — gives Hibernate the FK constraint in DDL. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_news_items_news_id"))
    private NewsEntity news;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** FK → photo_meta.id (plain Integer matching app convention). */
    @Column(name = "photo_id")
    private Integer photoId;

    @Column(length = 255)
    private String video;

    @Column(name = "url_more1", length = 512)
    private String urlMore1;

    @Column(name = "url_more2", length = 512)
    private String urlMore2;

    @Column(name = "url_more3", length = 512)
    private String urlMore3;

    @Column(name = "url_more4", length = 512)
    private String urlMore4;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected NewsItemEntity() {}

    public NewsItemEntity(Long newsId, String title, String description,
                          Integer photoId, String video,
                          String urlMore1, String urlMore2,
                          String urlMore3, String urlMore4,
                          Integer sortOrder) {
        this.newsId      = newsId;
        this.title       = title;
        this.description = description;
        this.photoId     = photoId;
        this.video       = video;
        this.urlMore1    = urlMore1;
        this.urlMore2    = urlMore2;
        this.urlMore3    = urlMore3;
        this.urlMore4    = urlMore4;
        this.sortOrder   = sortOrder != null ? sortOrder : 0;
    }

    @PrePersist
    private void onPersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long          getId()          { return id; }
    public Long          getNewsId()      { return newsId; }
    public String        getTitle()       { return title; }
    public String        getDescription() { return description; }
    public Integer       getPhotoId()     { return photoId; }
    public String        getVideo()       { return video; }
    public String        getUrlMore1()    { return urlMore1; }
    public String        getUrlMore2()    { return urlMore2; }
    public String        getUrlMore3()    { return urlMore3; }
    public String        getUrlMore4()    { return urlMore4; }
    public Integer       getSortOrder()   { return sortOrder; }
    public LocalDateTime getCreatedAt()   { return createdAt; }

    public void setTitle(String title)             { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPhotoId(Integer photoId)        { this.photoId = photoId; }
    public void setVideo(String video)             { this.video = video; }
    public void setUrlMore1(String urlMore1)       { this.urlMore1 = urlMore1; }
    public void setUrlMore2(String urlMore2)       { this.urlMore2 = urlMore2; }
    public void setUrlMore3(String urlMore3)       { this.urlMore3 = urlMore3; }
    public void setUrlMore4(String urlMore4)       { this.urlMore4 = urlMore4; }
    public void setSortOrder(Integer sortOrder)    { this.sortOrder = sortOrder; }
}
