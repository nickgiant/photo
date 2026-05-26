package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.LearningCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface LearningCategoryRepository extends JpaRepository<LearningCategoryEntity, Long> {

    List<LearningCategoryEntity> findAllByOrderByCatOrderAsc();

    @Query("SELECT c FROM LearningCategoryEntity c WHERE LOWER(c.catType) != LOWER(:catType) ORDER BY c.catOrder ASC")
    List<LearningCategoryEntity> findByCatTypeExcludingOrderByCatOrderAsc(@Param("catType") String catType);

    Optional<LearningCategoryEntity> findByCatTitleIgnoreCase(String catTitle);

    boolean existsByCatTitle(String catTitle);

    @Query("SELECT COUNT(l) FROM LearningEntity l WHERE l.categoryId = :categoryId")
    long countLearningsByCategoryId(@Param("categoryId") Long categoryId);
}
