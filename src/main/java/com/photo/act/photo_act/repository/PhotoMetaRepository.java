package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.PhotoMetaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoMetaRepository extends JpaRepository<PhotoMetaEntity, Integer> {

    Page<PhotoMetaEntity> findByVisibleToOrderByDateInsertedDesc(String visibleTo, Pageable pageable);

    Page<PhotoMetaEntity> findByUploaderIdAndVisibleToOrderByDateInsertedDesc(
            Integer uploaderId, String visibleTo, Pageable pageable);

    Page<PhotoMetaEntity> findByDestinationIdAndVisibleToOrderByDateInsertedDesc(
            Integer destinationId, String visibleTo, Pageable pageable);

    List<PhotoMetaEntity> findByUploaderIdOrderByDateInsertedDesc(Integer uploaderId);

    @Query("SELECT p FROM PhotoMetaEntity p WHERE p.visibleTo = 'ALL' AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(p.subtitle) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PhotoMetaEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByUploaderId(Integer uploaderId);

    long countByDestinationId(Integer destinationId);
}
