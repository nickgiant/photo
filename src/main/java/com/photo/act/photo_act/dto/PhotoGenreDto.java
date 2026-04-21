package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.PhotoGenreEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoGenreDto implements Serializable {

    Integer id;
    String  title;
    String  description;

    public static PhotoGenreDto from(PhotoGenreEntity e) {
        return PhotoGenreDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .build();
    }
}
