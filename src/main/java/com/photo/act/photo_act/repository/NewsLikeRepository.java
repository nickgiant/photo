package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.NewsLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsLikeRepository extends JpaRepository<NewsLikeEntity, Long> {

    /** Distinct IP likers so a single user only contributes one like. */
    @Query("SELECT COUNT(DISTINCT l.ipAddress) FROM NewsLikeEntity l WHERE l.newsId = :newsId")
    long countDistinctLikersByNewsId(@Param("newsId") Long newsId);

    /**
     * True if this IP has already liked this news entry (no time window —
     * a like is permanent unless explicitly removed).
     */
    @Query("SELECT COUNT(l) > 0 FROM NewsLikeEntity l " +
           "WHERE l.newsId = :newsId AND l.ipAddress = :ip")
    boolean existsByNewsIdAndIpAddress(@Param("newsId") Long newsId,
                                       @Param("ip") String ip);
}
