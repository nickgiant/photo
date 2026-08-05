package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.FestivalEditionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalEditionDto implements Serializable {

    Long      id;
    Long      festivalId;
    String    festivalNameShort;
    String    title;
    String    subtitle;
    LocalDate dateFrom;
    LocalDate dateTo;
    String    editionDescription;
    String    titleOfPlace;
    String    addressOfPlace;
    String    urlPlanned;
    String    urlFb;
    String    urlInsta;

    public static FestivalEditionDto from(FestivalEditionEntity e, String festivalNameShort) {
        return FestivalEditionDto.builder()
                .id(e.getId())
                .festivalId(e.getFestivalId())
                .festivalNameShort(festivalNameShort)
                .title(e.getTitle())
                .subtitle(e.getSubtitle())
                .dateFrom(e.getDateFrom())
                .dateTo(e.getDateTo())
                .editionDescription(e.getEditionDescription())
                .titleOfPlace(e.getTitleOfPlace())
                .addressOfPlace(e.getAddressOfPlace())
                .urlPlanned(e.getUrlPlanned())
                .urlFb(e.getUrlFb())
                .urlInsta(e.getUrlInsta())
                .build();
    }
}
