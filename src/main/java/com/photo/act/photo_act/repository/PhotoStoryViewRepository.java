package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.PhotoStoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PhotoStoryViewRepository extends JpaRepository<PhotoStoryView, Long> {

    @Query("SELECT COUNT(v) FROM PhotoStoryView v WHERE v.storyId = :storyId")
    long countByStoryId(@Param("storyId") int storyId);

    /**
     * Deduplication check: true if the same IP already has a view of this
     * story+viewType recorded after {@code since}.
     */
    @Query("SELECT COUNT(v) > 0 FROM PhotoStoryView v " +
           "WHERE v.storyId = :storyId AND v.ipAddress = :ip " +
           "AND v.viewType = :viewType AND v.viewedAt >= :since")
    boolean existsRecentView(@Param("storyId") int storyId,
                             @Param("ip") String ip,
                             @Param("viewType") String viewType,
                             @Param("since") LocalDateTime since);

    /**
     * Counts distinct people who liked a story, using IP address as the
     * unique-person identifier.
     */
    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM PhotoStoryView v " +
           "WHERE v.storyId = :storyId AND v.viewType = :viewType")
    long countDistinctLikersByStoryId(@Param("storyId") int storyId,
                                      @Param("viewType") String viewType);
}
