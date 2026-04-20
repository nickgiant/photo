package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news_categories")
public class NewsCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Optional RSS feed URL for this category. */
    @Column(name = "rss_url", length = 512)
    private String rssUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected NewsCategoryEntity() {}

    public NewsCategoryEntity(String title, String description) {
        this.title = title;
        this.description = description;
    }

    @PrePersist
    private void onPersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long          getId()            { return id; }
    public String        getTitle()         { return title; }
    public String        getDescription()   { return description; }
    public String        getRssUrl()        { return rssUrl; }
    public LocalDateTime getCreatedAt()     { return createdAt; }

    public void setTitle(String title)             { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setRssUrl(String rssUrl)           { this.rssUrl = rssUrl; }
}
