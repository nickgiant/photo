package com.photo.act.photo_act.services;

import com.photo.act.photo_act.dto.StoryMapDto;
import com.photo.act.photo_act.dto.StoryMapPointDto;
import com.photo.act.photo_act.model.StoryMapEntity;
import com.photo.act.photo_act.model.StoryMapPointEntity;
import com.photo.act.photo_act.repository.StoryMapPointRepository;
import com.photo.act.photo_act.repository.StoryMapRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StoryMapService {

    private final StoryMapRepository mapRepo;
    private final StoryMapPointRepository pointRepo;

    public StoryMapService(StoryMapRepository mapRepo, StoryMapPointRepository pointRepo) {
        this.mapRepo = mapRepo;
        this.pointRepo = pointRepo;
    }

    @Transactional
    public StoryMapEntity saveMap(Integer storyItemId, Integer userId, Integer storyId, String locationArea) {
        StoryMapEntity entity = mapRepo.findByStoryItemId(storyItemId).orElse(new StoryMapEntity());
        entity.setStoryItemId(storyItemId);
        entity.setUserId(userId);
        entity.setStoryId(storyId);
        entity.setLocationArea(locationArea);
        if (entity.getDateInserted() == null) {
            entity.setDateInserted(LocalDateTime.now());
        }
        return mapRepo.save(entity);
    }

    @Transactional
    public void savePoints(Integer mapId, List<StoryMapPointDto> points) {
        pointRepo.deleteByMapId(mapId);
        for (int i = 0; i < points.size(); i++) {
            StoryMapPointDto dto = points.get(i);
            StoryMapPointEntity p = new StoryMapPointEntity();
            p.setMapId(mapId);
            p.setPointName(dto.getPointName());
            p.setLat(dto.getLat());
            p.setLon(dto.getLon());
            p.setDescription(dto.getDescription());
            p.setPointOrder(i + 1);
            p.setColor(dto.getColor() != null && !dto.getColor().isBlank() ? dto.getColor() : "#3498db");
            pointRepo.save(p);
        }
    }

    @Transactional
    public void deleteByStoryItemId(Integer storyItemId) {
        mapRepo.findByStoryItemId(storyItemId).ifPresent(m -> {
            pointRepo.deleteByMapId(m.getId());
            mapRepo.delete(m);
        });
    }

    public Optional<StoryMapDto> findByStoryItemId(Integer storyItemId) {
        return mapRepo.findByStoryItemId(storyItemId).map(this::toDto);
    }

    private StoryMapDto toDto(StoryMapEntity entity) {
        StoryMapDto dto = new StoryMapDto();
        dto.setId(entity.getId());
        dto.setStoryItemId(entity.getStoryItemId());
        dto.setUserId(entity.getUserId());
        dto.setStoryId(entity.getStoryId());
        dto.setLocationArea(entity.getLocationArea());
        dto.setDateInserted(entity.getDateInserted());
        List<StoryMapPointDto> points = pointRepo.findByMapIdOrderByPointOrder(entity.getId())
                .stream().map(this::toPointDto).toList();
        dto.setPoints(points);
        return dto;
    }

    private StoryMapPointDto toPointDto(StoryMapPointEntity p) {
        StoryMapPointDto dto = new StoryMapPointDto();
        dto.setId(p.getId());
        dto.setMapId(p.getMapId());
        dto.setPointName(p.getPointName());
        dto.setLat(p.getLat());
        dto.setLon(p.getLon());
        dto.setDescription(p.getDescription());
        dto.setPointOrder(p.getPointOrder());
        dto.setColor(p.getColor());
        return dto;
    }
}
