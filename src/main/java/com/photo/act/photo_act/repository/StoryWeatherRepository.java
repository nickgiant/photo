package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.StoryWeatherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryWeatherRepository extends JpaRepository<StoryWeatherEntity, Integer> {

    Optional<StoryWeatherEntity> findByStoryItemId(Integer storyItemId);

    void deleteByStoryItemId(Integer storyItemId);
}
