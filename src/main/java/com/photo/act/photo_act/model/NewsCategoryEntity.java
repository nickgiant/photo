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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected NewsCategoryEntity() {}

    public NewsCategoryEntity(String title, String description) {
        this.title = title;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId()              { return id; }
    public String getTitle()         { return title; }
    public String getDescription()   { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTitle(String title)             { this.title = title; }
    public void setDescription(String description) { this.description = description; }
}
