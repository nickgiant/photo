package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news",
    indexes = {
        @Index(name = "idx_news_category_id", columnList = "category_id"),
        @Index(name = "idx_news_user_id",     columnList = "user_id"),
        @Index(name = "idx_news_created_at",  columnList = "created_at")
    })
public class NewsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** FK → photo_meta.id (kept as plain Integer matching app convention). */
    @Column(name = "photo_id")
    private Integer photoId;

    /** FK → dbuser.userId (plain Integer matching app-wide convention). */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "original_author", length = 255)
    private String originalAuthor;

    @Column(name = "original_url", length = 512)
    private String originalUrl;

    /** Write-side column for the category FK. */
    @Column(name = "category_id")
    private Long categoryId;

    /** Read-side FK reference — gives Hibernate the FK constraint in DDL. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_news_category_id"))
    private NewsCategoryEntity category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected NewsEntity() {}

    public NewsEntity(String title, String description, Integer photoId,
                      Integer userId, String originalAuthor, String originalUrl,
                      Long categoryId) {
        this.title          = title;
        this.description    = description;
        this.photoId        = photoId;
        this.userId         = userId;
        this.originalAuthor = originalAuthor;
        this.originalUrl    = originalUrl;
        this.categoryId     = categoryId;
    }

    @PrePersist
    private void onPersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long          getId()             { return id; }
    public String        getTitle()          { return title; }
    public String        getDescription()    { return description; }
    public Integer       getPhotoId()        { return photoId; }
    public Integer       getUserId()         { return userId; }
    public String        getOriginalAuthor() { return originalAuthor; }
    public String        getOriginalUrl()    { return originalUrl; }
    public Long          getCategoryId()     { return categoryId; }
    public NewsCategoryEntity getCategory()  { return category; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }

    public void setTitle(String title)                   { this.title = title; }
    public void setDescription(String description)       { this.description = description; }
    public void setPhotoId(Integer photoId)              { this.photoId = photoId; }
    public void setOriginalAuthor(String originalAuthor) { this.originalAuthor = originalAuthor; }
    public void setOriginalUrl(String originalUrl)       { this.originalUrl = originalUrl; }
    public void setCategoryId(Long categoryId)           { this.categoryId = categoryId; }
}
