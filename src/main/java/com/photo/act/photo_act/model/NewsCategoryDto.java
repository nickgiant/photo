package com.photo.act.photo_act.model;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

@Value
@Builder
public class NewsCategoryDto implements Serializable {

    Long          id;
    String        title;
    String        description;
    LocalDateTime createdAt;

    /** Total news entries published in this category. */
    long          newsCount;

    /** Timestamp of the most recently created news in this category. */
    LocalDateTime lastNewsAt;

    /** Human-readable time since last news (e.g. "3 hours ago"). */
    String        timeSinceLastNews;

    /** Total view events logged across all news in this category. */
    long          totalViews;

    /** Total distinct likes logged across all news in this category. */
    long          totalLikes;

    public static NewsCategoryDto from(NewsCategoryEntity e, long newsCount,
                                       LocalDateTime lastNewsAt, String timeSinceLastNews,
                                       long totalViews, long totalLikes) {
        return NewsCategoryDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .createdAt(e.getCreatedAt())
                .newsCount(newsCount)
                .lastNewsAt(lastNewsAt)
                .timeSinceLastNews(timeSinceLastNews)
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .build();
    }
}
