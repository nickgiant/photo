package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.LearningView;
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
public class LearningViewDto implements Serializable {

    Long          id;
    int           learningId;
    String        slug;
    Integer       userId;
    String        ipAddress;
    String        viewType;
    LocalDateTime viewedAt;

    public static LearningViewDto from(LearningView v) {
        return LearningViewDto.builder()
                .id(v.getId())
                .learningId(v.getLearningId())
                .slug(v.getSlug())
                .userId(v.getUserId())
                .ipAddress(v.getIpAddress())
                .viewType(v.getViewType())
                .viewedAt(v.getViewedAt())
                .build();
    }
}
