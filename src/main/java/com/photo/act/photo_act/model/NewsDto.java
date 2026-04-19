package com.photo.act.photo_act.model;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class NewsDto implements Serializable {

    Long             id;
    String           title;
    String           description;
    Integer          photoId;
    Integer          userId;
    String           originalAuthor;
    Long             categoryId;
    String           categoryTitle;
    LocalDateTime    createdAt;
    LocalDateTime    updatedAt;
    List<NewsItemDto> items;
    long             viewCount;
    long             likeCount;

    public static NewsDto from(NewsEntity e, String categoryTitle,
                               List<NewsItemDto> items, long views, long likes) {
        return NewsDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .photoId(e.getPhotoId())
                .userId(e.getUserId())
                .originalAuthor(e.getOriginalAuthor())
                .categoryId(e.getCategoryId())
                .categoryTitle(categoryTitle)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .items(items)
                .viewCount(views)
                .likeCount(likes)
                .build();
    }
}
