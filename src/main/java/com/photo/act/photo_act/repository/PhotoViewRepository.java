package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.PhotoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PhotoViewRepository extends JpaRepository<PhotoView, Long> {

    @Query("SELECT COUNT(v) FROM PhotoView v WHERE v.photoId = :photoId")
    long countByPhotoId(@Param("photoId") int photoId);

    /**
     * Deduplication check: true if the same IP already has a view of this
     * photo+viewType recorded after {@code since}.  List and Full are treated
     * independently so each type contributes its own dedup window.
     */
    @Query("SELECT COUNT(v) > 0 FROM PhotoView v " +
           "WHERE v.photoId = :photoId AND v.ipAddress = :ip " +
           "AND v.viewType = :viewType AND v.viewedAt >= :since")
    boolean existsRecentView(@Param("photoId") int photoId,
                             @Param("ip") String ip,
                             @Param("viewType") String viewType,
                             @Param("since") LocalDateTime since);

    /**
     * Counts distinct people who liked a photo, using IP address as the
     * unique-person identifier (logged-in users and guests are both tracked by IP).
     */
    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM PhotoView v " +
           "WHERE v.photoId = :photoId AND v.viewType = :viewType")
    long countDistinctLikersByPhotoId(@Param("photoId") int photoId,
                                      @Param("viewType") String viewType);
}
