package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.DestinationCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationCategoryDto implements Serializable {

    Integer id;
    String  destCatTitle;
    String  destCatType;
    String  destCatDescrMin;
    Integer destCatOrder;
    long    destinationCount;

    public static DestinationCategoryDto from(DestinationCategoryEntity e) {
        return DestinationCategoryDto.builder()
                .id(e.getId())
                .destCatTitle(e.getDestCatTitle())
                .destCatType(e.getDestCatType())
                .destCatDescrMin(e.getDestCatDescrMin())
                .destCatOrder(e.getDestCatOrder())
                .build();
    }
}
