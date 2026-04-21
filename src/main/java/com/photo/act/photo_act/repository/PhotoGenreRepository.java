package com.photo.act.photo_act.repository;

import com.photo.act.photo_act.model.PhotoGenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhotoGenreRepository extends JpaRepository<PhotoGenreEntity, Integer> {

    List<PhotoGenreEntity> findAllByOrderByTitleAsc();

    Optional<PhotoGenreEntity> findByTitleIgnoreCase(String title);
}
