package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.StoryMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryMapRepository extends JpaRepository<StoryMapEntity, Integer> {

    Optional<StoryMapEntity> findByStoryItemId(Integer storyItemId);

    void deleteByStoryItemId(Integer storyItemId);
}
