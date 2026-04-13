package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.PhotoRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PhotoRatingRepository extends JpaRepository<PhotoRating, Long> {

    Optional<PhotoRating> findByPhotoIdAndUserId(int photoId, int userId);

    @Query("SELECT AVG(r.rating) FROM PhotoRating r WHERE r.photoId = :photoId")
    Double findAverageRatingByPhotoId(@Param("photoId") int photoId);

    @Query("SELECT COUNT(r) FROM PhotoRating r WHERE r.photoId = :photoId")
    long countByPhotoId(@Param("photoId") int photoId);
}
