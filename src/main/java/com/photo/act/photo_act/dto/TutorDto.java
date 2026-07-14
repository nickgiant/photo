package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.TutorEntity;
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
public class TutorDto implements Serializable {

    Long          id;
    String        tutorName;
    String        website;
    String        websiteGallery;
    String        websiteGallery2;
    String        urlFb;
    String        urlYt;
    String        urlInsta;
    String        urlFlickr;
    String        urlWikipedia;
    String        urlRef1;
    String        urlRef2;
    String        urlRef3;
    String        cityBase;
    String        countryBase;
    Integer       userIdInsert;
    String        username;
    LocalDateTime dateInserted;
    long          learningCount;

    public static TutorDto from(TutorEntity e, long learningCount) {
        return TutorDto.builder()
                .id(e.getId())
                .tutorName(e.getTutorName())
                .website(e.getWebsite())
                .websiteGallery(e.getWebsiteGallery())
                .websiteGallery2(e.getWebsiteGallery2())
                .urlFb(e.getUrlFb())
                .urlYt(e.getUrlYt())
                .urlInsta(e.getUrlInsta())
                .urlFlickr(e.getUrlFlickr())
                .urlWikipedia(e.getUrlWikipedia())
                .urlRef1(e.getUrlRef1())
                .urlRef2(e.getUrlRef2())
                .urlRef3(e.getUrlRef3())
                .cityBase(e.getCityBase())
                .countryBase(e.getCountryBase())
                .userIdInsert(e.getUserIdInsert())
                .username(e.getUsername())
                .dateInserted(e.getDateInserted())
                .learningCount(learningCount)
                .build();
    }
}
