package com.photo.act.photo_act.dto;

import com.photo.act.photo_act.model.DestinationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationDto implements Serializable {

    Integer id;
    String  cityName;
    String  prefecture;
    String  country;
    String  nearbyCity;
    String  destinationTypeName;
    String  nameForMap;
    String  nameForWeather;
    Integer categoryId;
    String  categoryTitle;
    long    photoCount;

    public static DestinationDto from(DestinationEntity e) {
        return DestinationDto.builder()
                .id(e.getId())
                .cityName(e.getCityName())
                .prefecture(e.getPrefecture())
                .country(e.getCountry())
                .nearbyCity(e.getNearbyCity())
                .destinationTypeName(e.getDestinationTypeName())
                .nameForMap(e.getNameForMap())
                .nameForWeather(e.getNameForWeather())
                .categoryId(e.getCategoryId())
                .categoryTitle(e.getCategory() != null ? e.getCategory().getDestCatTitle() : null)
                .build();
    }
}
