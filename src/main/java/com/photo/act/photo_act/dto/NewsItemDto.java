package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.NewsItemEntity;
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
public class NewsItemDto implements Serializable {

    Long          id;
    Long          newsId;
    String        title;
    String        description;
    Integer       photoId;
    String        video;
    String        urlMore1;
    String        urlMore2;
    String        urlMore3;
    String        urlMore4;
    Integer       sortOrder;
    LocalDateTime createdAt;

    public static NewsItemDto from(NewsItemEntity e) {
        return NewsItemDto.builder()
                .id(e.getId())
                .newsId(e.getNewsId())
                .title(e.getTitle())
                .description(e.getDescription())
                .photoId(e.getPhotoId())
                .video(e.getVideo())
                .urlMore1(e.getUrlMore1())
                .urlMore2(e.getUrlMore2())
                .urlMore3(e.getUrlMore3())
                .urlMore4(e.getUrlMore4())
                .sortOrder(e.getSortOrder())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
