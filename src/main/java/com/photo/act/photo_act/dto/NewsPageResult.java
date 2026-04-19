package com.photo.act.photo_act.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Serializable replacement for Spring's {@code Page<NewsDto>}.
 * {@code PageImpl} cannot be deserialized by Jackson, so we use this
 * flat wrapper for Redis caching of paged news results.
 *
 * Uses @Data + @NoArgsConstructor (non-final class) so Jackson can
 * store @class type info and round-trip through Redis correctly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
