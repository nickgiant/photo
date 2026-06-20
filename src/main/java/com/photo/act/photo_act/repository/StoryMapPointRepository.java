package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.StoryMapPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryMapPointRepository extends JpaRepository<StoryMapPointEntity, Integer> {

    List<StoryMapPointEntity> findByMapIdOrderByPointOrder(Integer mapId);

    void deleteByMapId(Integer mapId);
}
