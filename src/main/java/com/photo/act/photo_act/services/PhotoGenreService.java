package com.photo.act.photo_act.services;

import com.photo.act.photo_act.dto.PhotoGenreDto;
import com.photo.act.photo_act.model.PhotoGenreEntity;
import com.photo.act.photo_act.repository.PhotoGenreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhotoGenreService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoGenreService.class);

    private final PhotoGenreRepository repository;

    public PhotoGenreService(PhotoGenreRepository repository) {
        this.repository = repository;
    }

    public List<PhotoGenreDto> getAllGenres() {
        try {
            return repository.findAllByOrderByTitleAsc()
                             .stream().map(PhotoGenreDto::from).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching genres: {}", e.getMessage());
            return List.of();
        }
    }

    public Optional<PhotoGenreDto> getById(Integer id) {
        return repository.findById(id).map(PhotoGenreDto::from);
    }

    @Transactional
    public PhotoGenreDto save(String title, String description) {
        PhotoGenreEntity entity = new PhotoGenreEntity(title, description);
        return PhotoGenreDto.from(repository.save(entity));
    }
}
