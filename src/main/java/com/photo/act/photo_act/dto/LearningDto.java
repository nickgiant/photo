package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.LearningEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningDto implements Serializable {

    Long          id;
    String        slug;
    String        title;
    String        picture;
    String        format;
    String        url;
    Long          tutorId;
    String        tutorName;
    String        tutorWebsite;
    String        tutorUrlYt;
    String        tutorUrlInsta;
    String        tutorUrlWikipedia;
    String        artistsRef;
    String        description;
    String        duration;
    String        pages;
    LocalDate     published;
    Long          categoryId;
    String        categoryTitle;
    Long          catGenreId;
//    String        catGenreTitle;
    Integer       userIdPost;
    LocalDateTime dateInsert;

    public static LearningDto from(LearningEntity e,
                                   String tutorName, String tutorWebsite,
                                   String tutorUrlYt, String tutorUrlInsta,
                                   String tutorUrlWikipedia,
                                   String categoryTitle, /*String catGenreTitle,*/
                                   String slug) {
        return LearningDto.builder()
                .id(e.getId())
                .slug(slug)
                .title(e.getTitle())
                .picture(e.getPicture())
                .format(e.getFormat())
                .url(e.getUrl())
                .tutorId(e.getTutorId())
                .tutorName(tutorName)
                .tutorWebsite(tutorWebsite)
                .tutorUrlYt(tutorUrlYt)
                .tutorUrlInsta(tutorUrlInsta)
                .tutorUrlWikipedia(tutorUrlWikipedia)
                .artistsRef(e.getArtistsRef())
                .description(e.getDescription())
                .duration(e.getDuration())
                .pages(e.getPages())
                .published(e.getPublished())
                .categoryId(e.getCategoryId())
                .categoryTitle(categoryTitle)
//                .catGenreId(e.getCatGenreId())
//                .catGenreTitle(catGenreTitle)
                .userIdPost(e.getUserIdPost())
                .dateInsert(e.getDateInsert())
                .build();
    }
}
