package com.photo.act.photo_act.services;

import com.photo.act.photo_act.dto.DestinationCategoryDto;
import com.photo.act.photo_act.dto.DestinationDto;
import com.photo.act.photo_act.model.DestinationCategoryEntity;
import com.photo.act.photo_act.model.DestinationEntity;
import com.photo.act.photo_act.repository.DestinationCategoryRepository;
import com.photo.act.photo_act.repository.DestinationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DestinationService {

    private static final Logger logger = LoggerFactory.getLogger(DestinationService.class);

    private final DestinationRepository destinationRepo;
    private final DestinationCategoryRepository categoryRepo;

    public DestinationService(DestinationRepository destinationRepo,
                               DestinationCategoryRepository categoryRepo) {
        this.destinationRepo = destinationRepo;
        this.categoryRepo    = categoryRepo;
    }

    public List<DestinationDto> getAllDestinations() {
        try {
            return destinationRepo.findAllByOrderByCountryAscCityNameAsc()
                                  .stream().map(this::toDto).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching destinations: {}", e.getMessage());
            return List.of();
        }
    }

    public List<DestinationDto> getByCountry(String country) {
        return destinationRepo.findByCountryOrderByCityNameAsc(country)
                              .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<DestinationDto> getByCategory(Integer categoryId) {
        return destinationRepo.findByCategoryIdOrderByCityNameAsc(categoryId)
                              .stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<DestinationDto> getById(Integer id) {
        return destinationRepo.findById(id).map(this::toDto);
    }

    public Optional<DestinationDto> getByCityName(String cityName) {
        return destinationRepo.findByCityNameIgnoreCase(cityName).map(this::toDto);
    }

    public List<DestinationDto> search(String keyword) {
        try {
            return destinationRepo.searchByKeyword(keyword)
                                  .stream().map(this::toDto).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error searching destinations for '{}': {}", keyword, e.getMessage());
            return List.of();
        }
    }

    public List<DestinationCategoryDto> getAllCategories() {
        try {
            return categoryRepo.findAllByOrderByDestCatOrderAsc()
                               .stream().map(DestinationCategoryDto::from).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching destination categories: {}", e.getMessage());
            return List.of();
        }
    }

    // ───────────────────────── Mapping helpers ─────────────────────────────

    /**
     * Resolves categoryTitle via a fresh repository lookup rather than the entity's lazy
     * `category` relation — DestinationEntity.category is FetchType.LAZY, and touching it
     * outside the (already-closed, by the time the stream mapping runs) repository-call
     * session throws LazyInitializationException.
     */
    private DestinationDto toDto(DestinationEntity e) {
        String categoryTitle = e.getCategoryId() != null
                ? categoryRepo.findById(e.getCategoryId())
                        .map(DestinationCategoryEntity::getDestCatTitle)
                        .orElse(null)
                : null;
        return DestinationDto.from(e, categoryTitle);
    }
}
