package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photo_album",
    indexes = {
        @Index(name = "idx_photo_album_user_id", columnList = "user_id"),
        @Index(name = "idx_photo_album_category_id", columnList = "category_id"),
        @Index(name = "idx_photo_album_date_inserted", columnList = "date_inserted")
    })
public class PhotoAlbumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "album_visible_to", length = 10)
    private String albumVisibleTo;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /** Write-side FK to photo_album_categories. */
    @Column(name = "category_id")
    private Integer categoryId;

    /** Read-side FK reference. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_photo_album_category_id"))
    private PhotoAlbumCategoryEntity category;

    @Column(name = "photo_id1")
    private Integer photoId1;

    @Column(name = "photo_id2")
    private Integer photoId2;

    @Column(name = "photo_id3")
    private Integer photoId3;

    @Column(name = "photo_id4")
    private Integer photoId4;

    @Column(name = "date_inserted")
    private LocalDateTime dateInserted;

    protected PhotoAlbumEntity() {}

    public PhotoAlbumEntity(String title, String description, String albumVisibleTo,
                             Integer userId, Integer categoryId) {
        this.title          = title;
        this.description    = description;
        this.albumVisibleTo = albumVisibleTo;
        this.userId         = userId;
        this.categoryId     = categoryId;
        this.dateInserted   = LocalDateTime.now();
    }

    @PrePersist
    private void onPersist() {
        if (dateInserted == null) dateInserted = LocalDateTime.now();
        if (albumVisibleTo == null) albumVisibleTo = "ALL";
    }

    public Integer getId()             { return id; }
    public String  getTitle()          { return title; }
    public String  getDescription()    { return description; }
    public String  getAlbumVisibleTo() { return albumVisibleTo; }
    public Integer getUserId()         { return userId; }
    public Integer getCategoryId()     { return categoryId; }
    public PhotoAlbumCategoryEntity getCategory() { return category; }
    public Integer getPhotoId1()       { return photoId1; }
    public Integer getPhotoId2()       { return photoId2; }
    public Integer getPhotoId3()       { return photoId3; }
    public Integer getPhotoId4()       { return photoId4; }
    public LocalDateTime getDateInserted() { return dateInserted; }

    public void setTitle(String title)                 { this.title = title; }
    public void setDescription(String description)     { this.description = description; }
    public void setAlbumVisibleTo(String albumVisibleTo) { this.albumVisibleTo = albumVisibleTo; }
    public void setCategoryId(Integer categoryId)      { this.categoryId = categoryId; }
    public void setPhotoId1(Integer photoId1)          { this.photoId1 = photoId1; }
    public void setPhotoId2(Integer photoId2)          { this.photoId2 = photoId2; }
    public void setPhotoId3(Integer photoId3)          { this.photoId3 = photoId3; }
    public void setPhotoId4(Integer photoId4)          { this.photoId4 = photoId4; }
}
