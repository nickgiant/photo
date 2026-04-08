package com.photo.act.photo_act.repository;


import com.photo.act.photo_act.model.ContentEntity;
import com.photo.act.photo_act.model.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<ContentEntity, Long> {

    /**
     * Primary lookup: find by slug alone (slug must be globally unique across types).
     */
    Optional<ContentEntity> findBySlug(String slug);

    /**
     * Scoped lookup: find by content type + slug (use when slugs are unique per type only).
     */
    Optional<ContentEntity> findByContentTypeAndSlug(ContentType contentType, String slug);

    /**
     * Existence check for cache warm-up — avoids loading full entity.
     */
    @Query("SELECT COUNT(c) > 0 FROM ContentEntity c WHERE c.slug = :slug")
    boolean existsBySlug(String slug);
}
