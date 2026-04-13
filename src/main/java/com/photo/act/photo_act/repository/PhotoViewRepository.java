package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.PhotoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhotoViewRepository extends JpaRepository<PhotoView, Long> {

    @Query("SELECT COUNT(v) FROM PhotoView v WHERE v.photoId = :photoId")
    long countByPhotoId(@Param("photoId") int photoId);

    /** True if this ip already viewed this photo today (prevents reload spam). */
    @Query("SELECT COUNT(v) > 0 FROM PhotoView v " +
           "WHERE v.photoId = :photoId AND v.ipAddress = :ip " +
           "AND v.viewedAt >= :since")
    boolean existsRecentView(@Param("photoId") int photoId,
                             @Param("ip") String ip,
                             @Param("since") java.time.LocalDateTime since);
}
