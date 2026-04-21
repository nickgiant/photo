package com.photo.act.photo_act.services;

import com.photo.act.photo_act.dto.PhotoMetaDto;
import com.photo.act.photo_act.model.PhotoMetaEntity;
import com.photo.act.photo_act.repository.PhotoMetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhotoMetaService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoMetaService.class);

    private final PhotoMetaRepository repository;

    public PhotoMetaService(PhotoMetaRepository repository) {
        this.repository = repository;
    }

    public Optional<PhotoMetaDto> getById(Integer id) {
        return repository.findById(id).map(PhotoMetaDto::from);
    }

    public Page<PhotoMetaDto> getPublicPhotos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByVisibleToOrderByDateInsertedDesc("ALL", pageable)
                         .map(PhotoMetaDto::from);
    }

    public List<PhotoMetaDto> getPhotosByUploader(Integer uploaderId) {
        return repository.findByUploaderIdOrderByDateInsertedDesc(uploaderId)
                         .stream().map(PhotoMetaDto::from).collect(Collectors.toList());
    }

    public Page<PhotoMetaDto> getPhotosByUploader(Integer uploaderId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByUploaderIdAndVisibleToOrderByDateInsertedDesc(uploaderId, "ALL", pageable)
                         .map(PhotoMetaDto::from);
    }

    public Page<PhotoMetaDto> getPhotosByDestination(Integer destinationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByDestinationIdAndVisibleToOrderByDateInsertedDesc(destinationId, "ALL", pageable)
                         .map(PhotoMetaDto::from);
    }

    public Page<PhotoMetaDto> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.searchByKeyword(keyword, pageable).map(PhotoMetaDto::from);
    }

    public long countByUploader(Integer uploaderId) {
        try {
            return repository.countByUploaderId(uploaderId);
        } catch (Exception e) {
            logger.error("Error counting photos for uploader {}: {}", uploaderId, e.getMessage());
            return 0;
        }
    }

    public long countByDestination(Integer destinationId) {
        try {
            return repository.countByDestinationId(destinationId);
        } catch (Exception e) {
            logger.error("Error counting photos for destination {}: {}", destinationId, e.getMessage());
            return 0;
        }
    }

    /** Returns multiple photos by their IDs (for album cover previews). */
    public List<PhotoMetaDto> getByIds(List<Integer> ids) {
        return ids.stream()
                  .filter(id -> id != null && id > 0)
                  .map(repository::findById)
                  .filter(Optional::isPresent)
                  .map(opt -> PhotoMetaDto.from(opt.get()))
                  .collect(Collectors.toList());
    }
}
