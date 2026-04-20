package com.photo.act.photo_act.services;

import com.photo.act.photo_act.dto.PhotoAlbumCategoryDto;
import com.photo.act.photo_act.dto.PhotoAlbumDto;
import com.photo.act.photo_act.dto.PhotoAlbumPhotoDto;
import com.photo.act.photo_act.model.PhotoAlbumEntity;
import com.photo.act.photo_act.model.PhotoAlbumPhotoEntity;
import com.photo.act.photo_act.repository.PhotoAlbumCategoryRepository;
import com.photo.act.photo_act.repository.PhotoAlbumPhotoRepository;
import com.photo.act.photo_act.repository.PhotoAlbumRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhotoAlbumService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoAlbumService.class);

    private final PhotoAlbumRepository albumRepo;
    private final PhotoAlbumPhotoRepository albumPhotoRepo;
    private final PhotoAlbumCategoryRepository categoryRepo;

    public PhotoAlbumService(PhotoAlbumRepository albumRepo,
                              PhotoAlbumPhotoRepository albumPhotoRepo,
                              PhotoAlbumCategoryRepository categoryRepo) {
        this.albumRepo       = albumRepo;
        this.albumPhotoRepo  = albumPhotoRepo;
        this.categoryRepo    = categoryRepo;
    }

    public Page<PhotoAlbumDto> getPublicAlbums(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return albumRepo.findByAlbumVisibleToOrderByDateInsertedDesc("ALL", pageable)
                        .map(e -> toDto(e));
    }

    public Page<PhotoAlbumDto> getPublicAlbumsByCategory(Integer categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return albumRepo.findByCategoryIdAndAlbumVisibleToOrderByDateInsertedDesc(categoryId, "ALL", pageable)
                        .map(e -> toDto(e));
    }

    public List<PhotoAlbumDto> getAlbumsByUser(Integer userId) {
        return albumRepo.findByUserIdOrderByDateInsertedDesc(userId)
                        .stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<PhotoAlbumDto> getAlbumById(Integer id) {
        return albumRepo.findById(id).map(this::toDto);
    }

    public List<PhotoAlbumCategoryDto> getAllCategories() {
        try {
            return categoryRepo.findAllByOrderByCatOrderAsc()
                               .stream().map(PhotoAlbumCategoryDto::from).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching album categories: {}", e.getMessage());
            return List.of();
        }
    }

    public List<PhotoAlbumPhotoDto> getAlbumPhotos(Integer albumId) {
        return albumPhotoRepo.findByPhotoAlbumIdOrderByIncAsc(albumId)
                             .stream().map(PhotoAlbumPhotoDto::from).collect(Collectors.toList());
    }

    public List<Integer> getPhotoIdsByAlbum(Integer albumId) {
        return albumPhotoRepo.findPhotoIdsByAlbumId(albumId);
    }

    public long countPhotosInAlbum(Integer albumId) {
        return albumPhotoRepo.countByPhotoAlbumId(albumId);
    }

    @Transactional
    public PhotoAlbumDto createAlbum(String title, String description, String visibleTo,
                                      Integer userId, Integer categoryId) {
        PhotoAlbumEntity album = new PhotoAlbumEntity(title, description, visibleTo, userId, categoryId);
        album = albumRepo.save(album);
        logger.info("Created album '{}' (id={}) for user {}", title, album.getId(), userId);
        return toDto(album);
    }

    @Transactional
    public void addPhotoToAlbum(Integer albumId, Integer photoId, Integer userId) {
        if (albumPhotoRepo.existsByPhotoAlbumIdAndPhotoId(albumId, photoId)) {
            logger.debug("Photo {} already in album {}", photoId, albumId);
            return;
        }
        int nextInc = (int) albumPhotoRepo.countByPhotoAlbumId(albumId) + 1;
        albumPhotoRepo.save(new PhotoAlbumPhotoEntity(albumId, photoId, userId, nextInc));
        logger.info("Added photo {} to album {} (inc={})", photoId, albumId, nextInc);
    }

    @Transactional
    public void removePhotoFromAlbum(Integer albumId, Integer photoId) {
        albumPhotoRepo.deleteById(
            new com.photo.act.photo_act.model.PhotoAlbumPhotoId(albumId, photoId));
    }

    private PhotoAlbumDto toDto(PhotoAlbumEntity e) {
        String categoryTitle = null;
        if (e.getCategory() != null) {
            categoryTitle = e.getCategory().getCatTitle();
        } else if (e.getCategoryId() != null) {
            categoryTitle = categoryRepo.findById(e.getCategoryId())
                                        .map(c -> c.getCatTitle()).orElse(null);
        }
        long photoCount = albumPhotoRepo.countByPhotoAlbumId(e.getId());
        return PhotoAlbumDto.from(e, categoryTitle, photoCount);
    }
}
