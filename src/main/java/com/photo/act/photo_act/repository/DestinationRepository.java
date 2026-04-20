package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.DestinationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DestinationRepository extends JpaRepository<DestinationEntity, Integer> {

    List<DestinationEntity> findByCountryOrderByCityNameAsc(String country);

    List<DestinationEntity> findByCategoryIdOrderByCityNameAsc(Integer categoryId);

    List<DestinationEntity> findAllByOrderByCountryAscCityNameAsc();

    Optional<DestinationEntity> findByCityNameIgnoreCase(String cityName);

    @Query("SELECT d FROM DestinationEntity d WHERE " +
           "LOWER(d.cityName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.country) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DestinationEntity> searchByKeyword(@Param("keyword") String keyword);
}
