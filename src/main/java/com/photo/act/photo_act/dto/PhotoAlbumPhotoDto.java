package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.PhotoAlbumPhotoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoAlbumPhotoDto implements Serializable {

    Integer photoAlbumId;
    Integer photoId;
    Integer userId;
    Integer inc;
    String  nameNew;
    String  title;

    public static PhotoAlbumPhotoDto from(PhotoAlbumPhotoEntity e) {
        return PhotoAlbumPhotoDto.builder()
                .photoAlbumId(e.getPhotoAlbumId())
                .photoId(e.getPhotoId())
                .userId(e.getUserId())
                .inc(e.getInc())
                .build();
    }
}
