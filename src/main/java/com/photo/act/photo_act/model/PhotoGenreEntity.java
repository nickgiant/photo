package com.photo.act.photo_act.model;

import jakarta.persistence.*;

@Entity
@Table(name = "photo_genre",
    indexes = @Index(name = "idx_photo_genre_title", columnList = "title"))
public class PhotoGenreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    protected PhotoGenreEntity() {}

    public PhotoGenreEntity(String title, String description) {
        this.title       = title;
        this.description = description;
    }

    public Integer getId()          { return id; }
    public String  getTitle()       { return title; }
    public String  getDescription() { return description; }

    public void setTitle(String title)             { this.title = title; }
    public void setDescription(String description) { this.description = description; }
}
