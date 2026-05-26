package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.LearningEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LearningRepository extends JpaRepository<LearningEntity, Long> {

    Page<LearningEntity> findAllByOrderByDateInsertDesc(Pageable pageable);

    List<LearningEntity> findByCategoryIdOrderByDateInsertDesc(Long categoryId);

    List<LearningEntity> findByCatGenreIdOrderByDateInsertDesc(Long catGenreId);

    List<LearningEntity> findByTutorIdOrderByDateInsertDesc(Long tutorId);

    List<LearningEntity> findByFormatOrderByDateInsertDesc(String format);

    long countByCategoryId(Long categoryId);

    long countByTutorId(Long tutorId);

    @Query("SELECT l FROM LearningEntity l WHERE " +
           "LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.artistsRef) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<LearningEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
