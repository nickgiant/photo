package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.TutorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<TutorEntity, Long> {

    List<TutorEntity> findAllByOrderByTutorNameAsc();

    Optional<TutorEntity> findByTutorNameIgnoreCase(String tutorName);

    boolean existsByTutorName(String tutorName);

    @Query("SELECT t FROM TutorEntity t WHERE " +
           "LOWER(t.tutorName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.cityBase) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.countryBase) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TutorEntity> searchByKeyword(@Param("keyword") String keyword);
}
