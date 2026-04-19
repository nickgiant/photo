package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.NewsItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsItemRepository extends JpaRepository<NewsItemEntity, Long> {

    List<NewsItemEntity> findByNewsIdOrderBySortOrderAsc(Long newsId);

    @Modifying
    @Query("DELETE FROM NewsItemEntity i WHERE i.newsId = :newsId")
    void deleteByNewsId(@Param("newsId") Long newsId);
}
