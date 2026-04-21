package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.PhotoAlbumEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoAlbumDto implements Serializable {

    Integer       id;
    String        title;
    String        description;
    String        albumVisibleTo;
    Integer       userId;
    Integer       categoryId;
    String        categoryTitle;
    Integer       photoId1;
    Integer       photoId2;
    Integer       photoId3;
    Integer       photoId4;
    LocalDateTime dateInserted;
    long          photoCount;
    String        uploaderUsername;

    public static PhotoAlbumDto from(PhotoAlbumEntity e, String categoryTitle, long photoCount) {
        return PhotoAlbumDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .albumVisibleTo(e.getAlbumVisibleTo())
                .userId(e.getUserId())
                .categoryId(e.getCategoryId())
                .categoryTitle(categoryTitle)
                .photoId1(e.getPhotoId1())
                .photoId2(e.getPhotoId2())
                .photoId3(e.getPhotoId3())
                .photoId4(e.getPhotoId4())
                .dateInserted(e.getDateInserted())
                .photoCount(photoCount)
                .build();
    }
}
