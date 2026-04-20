package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.NewsCategoryEntity;
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
public class NewsCategoryDto implements Serializable {

    Long          id;
    String        title;
    String        description;
    String        rssUrl;
    LocalDateTime createdAt;

    long          newsCount;
    LocalDateTime lastNewsAt;
    String        timeSinceLastNews;
    long          totalViews;
    long          totalLikes;
    long          totalAuthors;

    public static NewsCategoryDto from(NewsCategoryEntity e, long newsCount,
                                       LocalDateTime lastNewsAt, String timeSinceLastNews,
                                       long totalViews, long totalLikes, long totalAuthors) {
        return NewsCategoryDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .rssUrl(e.getRssUrl())
                .createdAt(e.getCreatedAt())
                .newsCount(newsCount)
                .lastNewsAt(lastNewsAt)
                .timeSinceLastNews(timeSinceLastNews)
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .totalAuthors(totalAuthors)
                .build();
    }
}
