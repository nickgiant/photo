package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.FestivalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FestivalRepository extends JpaRepository<FestivalEntity, Long> {

    List<FestivalEntity> findAllByOrderByNameShortAsc();

    List<FestivalEntity> findByCountryOrderByNameShortAsc(String country);

    List<FestivalEntity> findByTypeOrderByNameShortAsc(String type);

    Optional<FestivalEntity> findByNameShortIgnoreCase(String nameShort);

    boolean existsByNameShort(String nameShort);

    @Query("SELECT f FROM FestivalEntity f WHERE " +
           "LOWER(f.nameShort) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.country) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.type) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FestivalEntity> searchByKeyword(@Param("keyword") String keyword);
}
