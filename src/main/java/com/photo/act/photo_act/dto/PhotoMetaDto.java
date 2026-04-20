package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.PhotoMetaEntity;
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
public class PhotoMetaDto implements Serializable {

    Integer       id;
    String        nameNew;
    String        title;
    String        subtitle;
    String        notes;
    String        photoType;
    String        uploader;
    String        visibleTo;
    LocalDateTime metaDate;
    LocalDateTime dateInserted;
    Long          spaceSize;
    String        metaCameraMake;
    String        metaCameraModel;
    String        metaLensMake;
    String        metaLensModel;
    String        metaFocalLength;
    Integer       metaIso;
    String        metaAperture;
    String        metaShutterSpeed;
    Integer       metaOrientation;
    Integer       metaIHeight;
    Integer       metaILength;
    Integer       metaIWidth;
    String        locationByUser;
    String        locationArea;
    String        locationCountryCode;
    Double        locationLat;
    Double        locationLon;
    Integer       uploaderId;
    Integer       destinationId;
    String        cityName;

    public static PhotoMetaDto from(PhotoMetaEntity e) {
        return PhotoMetaDto.builder()
                .id(e.getId())
                .nameNew(e.getNameNew())
                .title(e.getTitle())
                .subtitle(e.getSubtitle())
                .notes(e.getNotes())
                .photoType(e.getPhotoType())
                .uploader(e.getUploader())
                .visibleTo(e.getVisibleTo())
                .metaDate(e.getMetaDate())
                .dateInserted(e.getDateInserted())
                .spaceSize(e.getSpaceSize())
                .metaCameraMake(e.getMetaCameraMake())
                .metaCameraModel(e.getMetaCameraModel())
                .metaLensMake(e.getMetaLensMake())
                .metaLensModel(e.getMetaLensModel())
                .metaFocalLength(e.getMetaFocalLength())
                .metaIso(e.getMetaIso())
                .metaAperture(e.getMetaAperture())
                .metaShutterSpeed(e.getMetaShutterSpeed())
                .metaOrientation(e.getMetaOrientation())
                .metaIHeight(e.getMetaIHeight())
                .metaILength(e.getMetaILength())
                .metaIWidth(e.getMetaIWidth())
                .locationByUser(e.getLocationByUser())
                .locationArea(e.getLocationArea())
                .locationCountryCode(e.getLocationCountryCode())
                .locationLat(e.getLocationLat())
                .locationLon(e.getLocationLon())
                .uploaderId(e.getUploaderId())
                .destinationId(e.getDestinationId())
                .build();
    }
}
