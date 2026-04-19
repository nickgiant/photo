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

    @Column(name = "photo_id")
    private Integer photoId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "original_author", length = 255)
    private String originalAuthor;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected NewsEntity() {}

    public NewsEntity(String title, String description, Integer photoId,
                      Integer userId, String originalAuthor, Long categoryId) {
        this.title          = title;
        this.description    = description;
        this.photoId        = photoId;
        this.userId         = userId;
        this.originalAuthor = originalAuthor;
        this.categoryId     = categoryId;
        this.createdAt      = LocalDateTime.now();
    }

    public Long          getId()             { return id; }
    public String        getTitle()          { return title; }
    public String        getDescription()    { return description; }
    public Integer       getPhotoId()        { return photoId; }
    public Integer       getUserId()         { return userId; }
    public String        getOriginalAuthor() { return originalAuthor; }
    public Long          getCategoryId()     { return categoryId; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }

    public void setTitle(String title)                   { this.title = title; }
    public void setDescription(String description)       { this.description = description; }
    public void setPhotoId(Integer photoId)              { this.photoId = photoId; }
    public void setOriginalAuthor(String originalAuthor) { this.originalAuthor = originalAuthor; }
    public void setCategoryId(Long categoryId)           { this.categoryId = categoryId; }
    public void setUpdatedAt(LocalDateTime updatedAt)    { this.updatedAt = updatedAt; }
}
