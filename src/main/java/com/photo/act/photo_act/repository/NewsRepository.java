package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.NewsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsRepository extends JpaRepository<NewsEntity, Long> {

    Page<NewsEntity> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    Page<NewsEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<NewsEntity> findByUserIdOrderByCreatedAtDesc(Integer userId);

    @Query("SELECT n FROM NewsEntity n WHERE " +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.originalAuthor) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<NewsEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByCategoryId(Long categoryId);
}
