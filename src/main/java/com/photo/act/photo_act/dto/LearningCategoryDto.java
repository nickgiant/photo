package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.LearningCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningCategoryDto implements Serializable {

    Long    id;
    String  catTitle;
    String  catTitleType;
    String  catType;
    Integer catOrder;
    String  catDescriptionMin;
    String  catDescriptionBig;
    long    learningCount;

    public static LearningCategoryDto from(LearningCategoryEntity e, long learningCount) {
        return LearningCategoryDto.builder()
                .id(e.getId())
                .catTitle(e.getCatTitle())
                .catTitleType(e.getCatTitleType())
                .catType(e.getCatType())
                .catOrder(e.getCatOrder())
                .catDescriptionMin(e.getCatDescriptionMin())
                .catDescriptionBig(e.getCatDescriptionBig())
                .learningCount(learningCount)
                .build();
    }
}
