package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.LearningView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface LearningViewRepository extends JpaRepository<LearningView, Long> {

    @Query("SELECT COUNT(v) FROM LearningView v WHERE v.learningId = :learningId")
    long countByLearningId(@Param("learningId") int learningId);

    @Query("SELECT COUNT(v) FROM LearningView v WHERE v.learningId = :learningId AND v.viewType = :viewType")
    long countByLearningIdAndViewType(@Param("learningId") int learningId,
                                      @Param("viewType") String viewType);

    /**
     * Deduplication check: true if the same IP already has a view of this
     * learning+viewType recorded after {@code since}.
     */
    @Query("SELECT COUNT(v) > 0 FROM LearningView v " +
           "WHERE v.learningId = :learningId AND v.ipAddress = :ip " +
           "AND v.viewType = :viewType AND v.viewedAt >= :since")
    boolean existsRecentView(@Param("learningId") int learningId,
                             @Param("ip") String ip,
                             @Param("viewType") String viewType,
                             @Param("since") LocalDateTime since);

    /**
     * Counts distinct people who liked a learning, using IP address as the
     * unique-person identifier.
     */
    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM LearningView v " +
           "WHERE v.learningId = :learningId AND v.viewType = :viewType")
    long countDistinctLikersByLearningId(@Param("learningId") int learningId,
                                          @Param("viewType") String viewType);
}
