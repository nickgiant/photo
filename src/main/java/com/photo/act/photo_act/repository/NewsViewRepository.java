package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.NewsViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NewsViewRepository extends JpaRepository<NewsViewEntity, Long> {

    @Query("SELECT COUNT(v) FROM NewsViewEntity v WHERE v.newsId = :newsId")
    long countByNewsId(@Param("newsId") Long newsId);

    /**
     * Deduplication check: true if the same IP already viewed this news
     * entry within the given time window.
     */
    @Query("SELECT COUNT(v) > 0 FROM NewsViewEntity v " +
           "WHERE v.newsId = :newsId AND v.ipAddress = :ip AND v.viewedAt >= :since")
    boolean existsRecentView(@Param("newsId") Long newsId,
                             @Param("ip") String ip,
                             @Param("since") LocalDateTime since);
}
