package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.PhotoView;
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
public class PhotoViewDto implements Serializable {

    Long          id;
    int           photoId;
    String        nameNew;
    Integer       userId;
    String        ipAddress;
    String        viewType;
    LocalDateTime viewedAt;

    public static PhotoViewDto from(PhotoView v) {
        return PhotoViewDto.builder()
                .id(v.getId())
                .photoId(v.getPhotoId())
                .nameNew(v.getNameNew())
                .userId(v.getUserId())
                .ipAddress(v.getIpAddress())
                .viewType(v.getViewType())
                .viewedAt(v.getViewedAt())
                .build();
    }
}
