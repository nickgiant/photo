package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.NewsCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NewsCategoryRepository extends JpaRepository<NewsCategoryEntity, Long> {

    Optional<NewsCategoryEntity> findByTitle(String title);

    boolean existsByTitle(String title);

    /** Count of published news entries belonging to a category. */
    @Query("SELECT COUNT(n) FROM NewsEntity n WHERE n.categoryId = :categoryId")
    long countNewsByCategoryId(@Param("categoryId") Long categoryId);

    /** Timestamp of the most recently created news in a category. */
    @Query("SELECT MAX(n.createdAt) FROM NewsEntity n WHERE n.categoryId = :categoryId")
    Optional<LocalDateTime> findLastNewDateByCategoryId(@Param("categoryId") Long categoryId);

    /** Total view events across all news in a category. */
    @Query("SELECT COUNT(v) FROM NewsViewEntity v " +
           "WHERE v.newsId IN (SELECT n.id FROM NewsEntity n WHERE n.categoryId = :categoryId)")
    long countViewsByCategoryId(@Param("categoryId") Long categoryId);

    /** Total distinct likers (by IP) across all news in a category. */
    @Query("SELECT COUNT(DISTINCT l.ipAddress) FROM NewsLikeEntity l " +
           "WHERE l.newsId IN (SELECT n.id FROM NewsEntity n WHERE n.categoryId = :categoryId)")
    long countLikesByCategoryId(@Param("categoryId") Long categoryId);

    /** All categories ordered by title. */
    List<NewsCategoryEntity> findAllByOrderByTitleAsc();
}
