package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.PhotoAlbumCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoAlbumCategoryRepository extends JpaRepository<PhotoAlbumCategoryEntity, Integer> {

    List<PhotoAlbumCategoryEntity> findAllByOrderByCatOrderAsc();
}
