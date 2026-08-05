package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.FestivalEditionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FestivalEditionRepository extends JpaRepository<FestivalEditionEntity, Long> {

    List<FestivalEditionEntity> findByFestivalIdOrderByDateFromDesc(Long festivalId);

    Page<FestivalEditionEntity> findAllByOrderByDateFromDesc(Pageable pageable);

    long countByFestivalId(Long festivalId);

    void deleteByFestivalId(Long festivalId);

    List<FestivalEditionEntity> findByDateFromGreaterThanEqualOrderByDateFromAsc(LocalDate fromDate);

    @Query("SELECT e FROM FestivalEditionEntity e WHERE " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.titleOfPlace) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.addressOfPlace) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<FestivalEditionEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
