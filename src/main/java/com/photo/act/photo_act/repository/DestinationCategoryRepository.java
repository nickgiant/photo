package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.DestinationCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DestinationCategoryRepository extends JpaRepository<DestinationCategoryEntity, Integer> {

    List<DestinationCategoryEntity> findAllByOrderByDestCatOrderAsc();
}
