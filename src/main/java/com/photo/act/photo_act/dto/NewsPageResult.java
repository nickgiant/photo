package com.photo.act.photo_act.dto;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.List;

/**
 * Serializable replacement for Spring's {@code Page<NewsDto>}.
 * {@code PageImpl} cannot be deserialized by Jackson, so we use this
 * flat wrapper for Redis caching of paged news results.
 */
@Value
@Builder
public class NewsPageResult implements Serializable {
    List<NewsDto> content;
    long          totalElements;
    int           totalPages;
    int           pageNumber;
    boolean       hasNext;
    boolean       hasPrevious;

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
}
