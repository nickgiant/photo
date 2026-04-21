package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.PhotoAlbumCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoAlbumCategoryDto implements Serializable {

    Integer id;
    String  catTitle;
    String  catType;
    String  catDescriptionMin;
    String  catTypeDescriptionMin;
    String  catDescriptionBig;
    Integer catOrder;
    long    albumCount;

    public static PhotoAlbumCategoryDto from(PhotoAlbumCategoryEntity e) {
        return PhotoAlbumCategoryDto.builder()
                .id(e.getId())
                .catTitle(e.getCatTitle())
                .catType(e.getCatType())
                .catDescriptionMin(e.getCatDescriptionMin())
                .catTypeDescriptionMin(e.getCatTypeDescriptionMin())
                .catDescriptionBig(e.getCatDescriptionBig())
                .catOrder(e.getCatOrder())
                .build();
    }
}
