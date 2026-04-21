package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.PhotoAlbumPhotoEntity;
import com.photo.act.photo_act.model.PhotoAlbumPhotoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoAlbumPhotoRepository extends JpaRepository<PhotoAlbumPhotoEntity, PhotoAlbumPhotoId> {

    List<PhotoAlbumPhotoEntity> findByPhotoAlbumIdOrderByIncAsc(Integer photoAlbumId);

    boolean existsByPhotoAlbumIdAndPhotoId(Integer photoAlbumId, Integer photoId);

    long countByPhotoAlbumId(Integer photoAlbumId);

    @Modifying
    @Query("DELETE FROM PhotoAlbumPhotoEntity p WHERE p.photoAlbumId = :albumId")
    void deleteByPhotoAlbumId(@Param("albumId") Integer albumId);

    @Query("SELECT p.photoId FROM PhotoAlbumPhotoEntity p WHERE p.photoAlbumId = :albumId ORDER BY p.inc ASC")
    List<Integer> findPhotoIdsByAlbumId(@Param("albumId") Integer albumId);
}
