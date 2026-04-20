package com.photo.act.photo_act.model;

import jakarta.persistence.*;

@Entity
@Table(name = "photo_album_photo",
    indexes = {
        @Index(name = "idx_pap_album_id", columnList = "photo_album_id"),
        @Index(name = "idx_pap_photo_id", columnList = "photo_id")
    })
@IdClass(PhotoAlbumPhotoId.class)
public class PhotoAlbumPhotoEntity {

    @Id
    @Column(name = "photo_album_id", nullable = false)
    private Integer photoAlbumId;

    @Id
    @Column(name = "photo_id", nullable = false)
    private Integer photoId;

    @Column(name = "user_id")
    private Integer userId;

    /** Sort order within the album. */
    @Column(name = "inc")
    private Integer inc;

    protected PhotoAlbumPhotoEntity() {}

    public PhotoAlbumPhotoEntity(Integer photoAlbumId, Integer photoId, Integer userId, Integer inc) {
        this.photoAlbumId = photoAlbumId;
        this.photoId      = photoId;
        this.userId       = userId;
        this.inc          = inc;
    }

    public Integer getPhotoAlbumId() { return photoAlbumId; }
    public Integer getPhotoId()      { return photoId; }
    public Integer getUserId()       { return userId; }
    public Integer getInc()          { return inc; }

    public void setInc(Integer inc)      { this.inc = inc; }
    public void setUserId(Integer userId) { this.userId = userId; }
}
