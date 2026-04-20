package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.PhotoAlbumEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoAlbumRepository extends JpaRepository<PhotoAlbumEntity, Integer> {

    Page<PhotoAlbumEntity> findByAlbumVisibleToOrderByDateInsertedDesc(String visibleTo, Pageable pageable);

    List<PhotoAlbumEntity> findByUserIdOrderByDateInsertedDesc(Integer userId);

    Page<PhotoAlbumEntity> findByCategoryIdAndAlbumVisibleToOrderByDateInsertedDesc(
            Integer categoryId, String visibleTo, Pageable pageable);

    long countByUserId(Integer userId);

    @Query("SELECT a FROM PhotoAlbumEntity a WHERE a.albumVisibleTo = 'ALL' AND " +
           "(LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PhotoAlbumEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
