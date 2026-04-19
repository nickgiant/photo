package com.photo.act.photo_act.model;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class NewsItemDto implements Serializable {

    Long    id;
    Long    newsId;
    String  title;
    String  description;
    Integer photoId;
    String  video;
    String  urlMore1;
    String  urlMore2;
    String  urlMore3;
    String  urlMore4;
    Integer sortOrder;

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
                .build();
    }
}
