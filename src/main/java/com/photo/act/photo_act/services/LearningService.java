package com.photo.act.photo_act.services;

import com.photo.act.photo_act.dto.LearningCategoryDto;
import com.photo.act.photo_act.dto.LearningDto;
import com.photo.act.photo_act.model.LearningCategoryEntity;
import com.photo.act.photo_act.model.LearningEntity;
import com.photo.act.photo_act.model.TutorEntity;
import com.photo.act.photo_act.repository.LearningCategoryRepository;
import com.photo.act.photo_act.repository.LearningRepository;
import com.photo.act.photo_act.repository.TutorRepository;
import com.photo.act.photo_act.utils.SlugUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LearningService {

    private static final Logger log = LoggerFactory.getLogger(LearningService.class);

    private final LearningRepository         learningRepo;
    private final TutorRepository            tutorRepo;
    private final LearningCategoryRepository categoryRepo;

    public LearningService(LearningRepository learningRepo,
                           TutorRepository tutorRepo,
                           LearningCategoryRepository categoryRepo) {
        this.learningRepo = learningRepo;
        this.tutorRepo    = tutorRepo;
        this.categoryRepo = categoryRepo;
    }

    // ─────────────────────── Categories ────────────────────────────────────

    public List<LearningCategoryDto> getAllCategories() {
        return categoryRepo.findAllByOrderByCatOrderAsc().stream()
                .map(c -> LearningCategoryDto.from(c, categoryRepo.countLearningsByCategoryId(c.getId())))
                .toList();
    }

    public Optional<LearningCategoryDto> getCategoryById(Long id) {
        return categoryRepo.findById(id)
                .map(c -> LearningCategoryDto.from(c, categoryRepo.countLearningsByCategoryId(c.getId())));
    }

    @Transactional
    public LearningCategoryDto createCategory(LearningCategoryDto dto) {
        LearningCategoryEntity entity = new LearningCategoryEntity(
                dto.getCatTitle(), dto.getCatTitleType(), dto.getCatType(),
                dto.getCatOrder(), dto.getCatDescriptionMin(), dto.getCatDescriptionBig());
        LearningCategoryEntity saved = categoryRepo.save(entity);
        return LearningCategoryDto.from(saved, 0L);
    }

    @Transactional
    public Optional<LearningCategoryDto> updateCategory(Long id, LearningCategoryDto dto) {
        return categoryRepo.findById(id).map(entity -> {
            entity.setCatTitle(dto.getCatTitle());
            entity.setCatTitleType(dto.getCatTitleType());
            entity.setCatType(dto.getCatType());
            entity.setCatOrder(dto.getCatOrder());
            entity.setCatDescriptionMin(dto.getCatDescriptionMin());
            entity.setCatDescriptionBig(dto.getCatDescriptionBig());
            LearningCategoryEntity saved = categoryRepo.save(entity);
            return LearningCategoryDto.from(saved, categoryRepo.countLearningsByCategoryId(saved.getId()));
        });
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepo.deleteById(id);
    }

    // ──────────────────────── Learnings ────────────────────────────────────

    public Page<LearningDto> getLatestLearnings(int page, int size) {
        return learningRepo.findAllByOrderByDateInsertDesc(PageRequest.of(page, size))
                .map(this::toDto);
    }

    public Optional<LearningDto> getLearningById(Long id) {
        return learningRepo.findById(id).map(this::toDto);
    }

    public List<LearningDto> getLearningsByCategory(Long categoryId) {
        return learningRepo.findByCategoryIdOrderByDateInsertDesc(categoryId).stream()
                .map(this::toDto).toList();
    }

    public List<LearningDto> getLearningsByGenre(Long catGenreId) {
        return learningRepo.findByCatGenreIdOrderByDateInsertDesc(catGenreId).stream()
                .map(this::toDto).toList();
    }

    public List<LearningDto> getLearningsByTutor(Long tutorId) {
        return learningRepo.findByTutorIdOrderByDateInsertDesc(tutorId).stream()
                .map(this::toDto).toList();
    }

    public List<LearningDto> getLearningsByUser(Integer userIdPost) {
        return learningRepo.findByUserIdPostOrderByDateInsertDesc(userIdPost).stream()
                .map(this::toDto).toList();
    }

    public long countLearningsByUser(Integer userIdPost) {
        return learningRepo.countByUserIdPost(userIdPost);
    }

    public Page<LearningDto> searchLearnings(String keyword, int page, int size) {
        return learningRepo.searchByKeyword(keyword, PageRequest.of(page, size))
                .map(this::toDto);
    }

    @Transactional
    public LearningDto createLearning(LearningDto dto) {
        LearningEntity entity = new LearningEntity(
                dto.getTitle(), dto.getPicture(), dto.getFormat(), dto.getUrl(),
                dto.getTutorId(), dto.getArtistsRef(), dto.getDescription(),
                dto.getDuration(), dto.getPages(), dto.getPublished(),
                dto.getCategoryId(), dto.getCatGenreId(), dto.getUserIdPost());
        LearningEntity saved = learningRepo.save(entity);
        saved.setSlug(SlugUtil.toSlug(saved.getTitle()) + "-" + saved.getId());
        return toDto(learningRepo.save(saved));
    }

    @Transactional
    public Optional<LearningDto> updateLearning(Long id, LearningDto dto) {
        return learningRepo.findById(id).map(entity -> {
            entity.setTitle(dto.getTitle());
            entity.setPicture(dto.getPicture());
            entity.setFormat(dto.getFormat());
            entity.setUrl(dto.getUrl());
            entity.setTutorId(dto.getTutorId());
            entity.setArtistsRef(dto.getArtistsRef());
            entity.setDescription(dto.getDescription());
            entity.setDuration(dto.getDuration());
            entity.setPages(dto.getPages());
            entity.setPublished(dto.getPublished());
            entity.setCategoryId(dto.getCategoryId());
            entity.setCatGenreId(dto.getCatGenreId());
            entity.setSlug(SlugUtil.toSlug(dto.getTitle()) + "-" + id);
            return toDto(learningRepo.save(entity));
        });
    }

    @Transactional
    public void deleteLearning(Long id) {
        learningRepo.deleteById(id);
    }

    private LearningDto toDto(LearningEntity e) {
        String tutorName      = null;
        String tutorWebsite   = null;
        String tutorUrlYt     = null;
        String tutorUrlInsta  = null;
        String tutorUrlWiki   = null;
        if (e.getTutorId() != null) {
            Optional<TutorEntity> tutor = tutorRepo.findById(e.getTutorId());
            if (tutor.isPresent()) {
                tutorName     = tutor.get().getTutorName();
                tutorWebsite  = tutor.get().getWebsite();
                tutorUrlYt    = tutor.get().getUrlYt();
                tutorUrlInsta = tutor.get().getUrlInsta();
                tutorUrlWiki  = tutor.get().getUrlWikipedia();
            }
        }
        String categoryTitle = e.getCategoryId() != null
                ? categoryRepo.findById(e.getCategoryId())
                              .map(LearningCategoryEntity::getCatTitle).orElse(null)
                : null;
        String catGenreTitle = e.getCatGenreId() != null
                ? categoryRepo.findById(e.getCatGenreId())
                              .map(LearningCategoryEntity::getCatTitle).orElse(null)
                : null;
        return LearningDto.from(e, tutorName, tutorWebsite, tutorUrlYt, tutorUrlInsta,
                                tutorUrlWiki, categoryTitle, catGenreTitle, e.getSlug());
    }
}
