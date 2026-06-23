package com.photo.act.photo_act.services;

import com.photo.act.photo_act.dto.StoryWeatherDto;
import com.photo.act.photo_act.model.StoryWeatherEntity;
import com.photo.act.photo_act.repository.StoryWeatherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StoryWeatherService {

    private final StoryWeatherRepository weatherRepo;

    public StoryWeatherService(StoryWeatherRepository weatherRepo) {
        this.weatherRepo = weatherRepo;
    }

    @Transactional
    public StoryWeatherEntity save(Integer storyItemId, Integer userId, Integer storyId,
                                   String locationArea, Double lat, Double lon) {
        StoryWeatherEntity entity = weatherRepo.findByStoryItemId(storyItemId)
                .orElse(new StoryWeatherEntity());
        entity.setStoryItemId(storyItemId);
        entity.setUserId(userId);
        entity.setStoryId(storyId);
        entity.setLocationArea(locationArea);
        entity.setLat(lat);
        entity.setLon(lon);
        if (entity.getDateInserted() == null) {
            entity.setDateInserted(LocalDateTime.now());
        }
        return weatherRepo.save(entity);
    }

    @Transactional
    public void deleteByStoryItemId(Integer storyItemId) {
        weatherRepo.findByStoryItemId(storyItemId).ifPresent(weatherRepo::delete);
    }

    public Optional<StoryWeatherDto> findByStoryItemId(Integer storyItemId) {
        return weatherRepo.findByStoryItemId(storyItemId).map(this::toDto);
    }

    private StoryWeatherDto toDto(StoryWeatherEntity e) {
        StoryWeatherDto dto = new StoryWeatherDto();
        dto.setId(e.getId());
        dto.setStoryItemId(e.getStoryItemId());
        dto.setUserId(e.getUserId());
        dto.setStoryId(e.getStoryId());
        dto.setLocationArea(e.getLocationArea());
        dto.setLat(e.getLat());
        dto.setLon(e.getLon());
        dto.setDateInserted(e.getDateInserted());
        return dto;
    }
}
