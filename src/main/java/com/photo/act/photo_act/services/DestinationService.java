package com.photo.act.photo_act.services;

import com.photo.act.photo_act.dto.DestinationCategoryDto;
import com.photo.act.photo_act.dto.DestinationDto;
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
                                  .stream().map(DestinationDto::from).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching destinations: {}", e.getMessage());
            return List.of();
        }
    }

    public List<DestinationDto> getByCountry(String country) {
        return destinationRepo.findByCountryOrderByCityNameAsc(country)
                              .stream().map(DestinationDto::from).collect(Collectors.toList());
    }

    public List<DestinationDto> getByCategory(Integer categoryId) {
        return destinationRepo.findByCategoryIdOrderByCityNameAsc(categoryId)
                              .stream().map(DestinationDto::from).collect(Collectors.toList());
    }

    public Optional<DestinationDto> getById(Integer id) {
        return destinationRepo.findById(id).map(DestinationDto::from);
    }

    public Optional<DestinationDto> getByCityName(String cityName) {
        return destinationRepo.findByCityNameIgnoreCase(cityName).map(DestinationDto::from);
    }

    public List<DestinationDto> search(String keyword) {
        try {
            return destinationRepo.searchByKeyword(keyword)
                                  .stream().map(DestinationDto::from).collect(Collectors.toList());
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
}
