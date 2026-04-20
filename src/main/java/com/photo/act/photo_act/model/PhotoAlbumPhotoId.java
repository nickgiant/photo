package com.photo.act.photo_act.model;

import java.io.Serializable;
import java.util.Objects;

public class PhotoAlbumPhotoId implements Serializable {

    private Integer photoAlbumId;
    private Integer photoId;

    public PhotoAlbumPhotoId() {}

    public PhotoAlbumPhotoId(Integer photoAlbumId, Integer photoId) {
        this.photoAlbumId = photoAlbumId;
        this.photoId      = photoId;
    }

    public Integer getPhotoAlbumId() { return photoAlbumId; }
    public Integer getPhotoId()      { return photoId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhotoAlbumPhotoId)) return false;
        PhotoAlbumPhotoId that = (PhotoAlbumPhotoId) o;
        return Objects.equals(photoAlbumId, that.photoAlbumId) &&
               Objects.equals(photoId, that.photoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(photoAlbumId, photoId);
    }
}
