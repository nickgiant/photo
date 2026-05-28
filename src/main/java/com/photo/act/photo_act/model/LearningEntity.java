package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "learnings",
    indexes = {
        @Index(name = "idx_learning_category_id",  columnList = "category_id"),
        @Index(name = "idx_learning_tutor_id",      columnList = "tutor_id"),
        @Index(name = "idx_learning_date_insert",   columnList = "date_insert")
    })
public class LearningEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 512)
    private String picture;

    @Column(length = 100)
    private String format;

    @Column(length = 512)
    private String url;

    /** Write-side FK → tutor.id */
    @Column(name = "tutor_id")
    private Long tutorId;

    /** Read-side FK reference for DDL constraint and lazy loading. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_learning_tutor_id"))
    private TutorEntity tutor;

    @Column(name = "tutor_id_team")
    private Long tutorIdTeam;

    @Column(name = "artists_ref", length = 512)
    private String artistsRef;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String duration;

    @Column(length = 50)
    private String pages;

    private LocalDate published;

    /** Write-side FK → learnings_categories.id (main category) */
    @Column(name = "category_id")
    private Long categoryId;

    /** Read-side reference for main category. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_learning_category_id"))
    private LearningCategoryEntity category;

    /** Write-side FK → learnings_categories.id (genre) */
    @Column(name = "cat_genre_id")
    private Long catGenreId;

    /** Read-side reference for genre category. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cat_genre_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_learning_cat_genre_id"))
    private LearningCategoryEntity catGenre;

    @Column(name = "user_Id_post")
    private Integer userIdPost;

    @Column(name = "date_insert", updatable = false)
    private LocalDateTime dateInsert;

    @Column(length = 255)
    private String slug;

    protected LearningEntity() {}

    public LearningEntity(String title, String picture, String format, String url,
                          Long tutorId, String artistsRef, String description,
                          String duration, String pages, LocalDate published,
                          Long categoryId, Long catGenreId, Integer userIdPost) {
        this.title       = title;
        this.picture     = picture;
        this.format      = format;
        this.url         = url;
        this.tutorId     = tutorId;
        this.artistsRef  = artistsRef;
        this.description = description;
        this.duration    = duration;
        this.pages       = pages;
        this.published   = published;
        this.categoryId  = categoryId;
        this.catGenreId  = catGenreId;
        this.userIdPost  = userIdPost;
    }

    @PrePersist
    private void onPersist() {
        if (dateInsert == null) dateInsert = LocalDateTime.now();
    }

    public Long          getId()          { return id; }
    public String        getTitle()       { return title; }
    public String        getPicture()     { return picture; }
    public String        getFormat()      { return format; }
    public String        getUrl()         { return url; }
    public Long          getTutorId()     { return tutorId; }
    public TutorEntity   getTutor()       { return tutor; }
    public Long          getTutorIdTeam() { return tutorIdTeam; }
    public String        getArtistsRef()  { return artistsRef; }
    public String        getDescription() { return description; }
    public String        getDuration()    { return duration; }
    public String        getPages()       { return pages; }
    public LocalDate     getPublished()   { return published; }
    public Long          getCategoryId()  { return categoryId; }
    public LearningCategoryEntity getCategory()  { return category; }
    public Long          getCatGenreId()  { return catGenreId; }
    public LearningCategoryEntity getCatGenre()  { return catGenre; }
    public Integer       getUserIdPost()  { return userIdPost; }
    public LocalDateTime getDateInsert()  { return dateInsert; }
    public String        getSlug()        { return slug; }

    public void setTitle(String title)             { this.title = title; }
    public void setPicture(String picture)         { this.picture = picture; }
    public void setFormat(String format)           { this.format = format; }
    public void setUrl(String url)                 { this.url = url; }
    public void setTutorId(Long tutorId)           { this.tutorId = tutorId; }
    public void setTutorIdTeam(Long tutorIdTeam)   { this.tutorIdTeam = tutorIdTeam; }
    public void setArtistsRef(String artistsRef)   { this.artistsRef = artistsRef; }
    public void setDescription(String description) { this.description = description; }
    public void setDuration(String duration)       { this.duration = duration; }
    public void setPages(String pages)             { this.pages = pages; }
    public void setPublished(LocalDate published)  { this.published = published; }
    public void setCategoryId(Long categoryId)     { this.categoryId = categoryId; }
    public void setCatGenreId(Long catGenreId)     { this.catGenreId = catGenreId; }
    public void setSlug(String slug)               { this.slug = slug; }
}
