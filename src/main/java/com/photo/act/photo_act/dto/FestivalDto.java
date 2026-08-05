package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.FestivalEntity;
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
public class FestivalDto implements Serializable {

    Long          id;
    String        nameShort;
    String        nameFull;
    String        periodOfYear;
    String        type;
    String        website;
    String        urlFacebook;
    String        urlInstagram;
    String        urlYoutube;
    String        activities;
    String        imageTop;
    String        imageLogo;
    Integer       destinationId;
    /** Denormalized "City (Country)" label, resolved from the destination table. */
    String        destinationLabel;
    LocalDateTime dateInsert;
    long          editionCount;

    public static FestivalDto from(FestivalEntity e, long editionCount, String destinationLabel) {
        return FestivalDto.builder()
                .id(e.getId())
                .nameShort(e.getNameShort())
                .nameFull(e.getNameFull())
                .periodOfYear(e.getPeriodOfYear())
                .type(e.getType())
                .website(e.getWebsite())
                .urlFacebook(e.getUrlFacebook())
                .urlInstagram(e.getUrlInstagram())
                .urlYoutube(e.getUrlYoutube())
                .activities(e.getActivities())
                .imageTop(e.getImageTop())
                .imageLogo(e.getImageLogo())
                .destinationId(e.getDestinationId())
                .destinationLabel(destinationLabel)
                .dateInsert(e.getDateInsert())
                .editionCount(editionCount)
                .build();
    }
}
