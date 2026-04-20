package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.PhotoRating;
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
public class PhotoRatingDto implements Serializable {

    Long          id;
    int           photoId;
    int           userId;
    int           rating;
    String        nameNew;
    String        ipAddress;
    LocalDateTime ratedAt;

    public static PhotoRatingDto from(PhotoRating r) {
        return PhotoRatingDto.builder()
                .id(r.getId())
                .photoId(r.getPhotoId())
                .userId(r.getUserId())
                .rating(r.getRating())
                .nameNew(r.getNameNew())
                .ipAddress(r.getIpAddress())
                .ratedAt(r.getRatedAt())
                .build();
    }
}
