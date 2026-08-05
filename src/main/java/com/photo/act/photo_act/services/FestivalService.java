package com.photo.act.photo_act.services;

import com.photo.act.photo_act.dto.FestivalDto;
import com.photo.act.photo_act.dto.FestivalEditionDto;
import com.photo.act.photo_act.model.FestivalEditionEntity;
import com.photo.act.photo_act.model.FestivalEntity;
import com.photo.act.photo_act.repository.DestinationRepository;
import com.photo.act.photo_act.repository.FestivalEditionRepository;
import com.photo.act.photo_act.repository.FestivalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FestivalService {

    private static final Logger log = LoggerFactory.getLogger(FestivalService.class);

    private final FestivalRepository        festivalRepo;
    private final FestivalEditionRepository editionRepo;
    private final DestinationRepository     destinationRepo;

    public FestivalService(FestivalRepository festivalRepo, FestivalEditionRepository editionRepo,
                           DestinationRepository destinationRepo) {
        this.festivalRepo   = festivalRepo;
        this.editionRepo    = editionRepo;
        this.destinationRepo = destinationRepo;
    }

    // ─────────────────────── Festivals ─────────────────────────────────────

    public List<FestivalDto> getAllFestivals() {
        try {
            return festivalRepo.findAllByOrderByNameShortAsc().stream()
                    .map(this::toDto).toList();
        } catch (Exception e) {
            log.error("Error fetching festivals: {}", e.getMessage());
            return List.of();
        }
    }

    public Optional<FestivalDto> getFestivalById(Long id) {
        return festivalRepo.findById(id).map(this::toDto);
    }

    public Optional<FestivalDto> getFestivalByNameShort(String nameShort) {
        return festivalRepo.findByNameShortIgnoreCase(nameShort).map(this::toDto);
    }

    public List<FestivalDto> getFestivalsByDestination(Integer destinationId) {
        return festivalRepo.findByDestinationIdOrderByNameShortAsc(destinationId).stream()
                .map(this::toDto).toList();
    }

    public List<FestivalDto> getFestivalsByType(String type) {
        return festivalRepo.findByTypeOrderByNameShortAsc(type).stream()
                .map(this::toDto).toList();
    }

    public List<FestivalDto> searchFestivals(String keyword) {
        try {
            return festivalRepo.searchByKeyword(keyword).stream()
                    .map(this::toDto).toList();
        } catch (Exception e) {
            log.error("Error searching festivals for '{}': {}", keyword, e.getMessage());
            return List.of();
        }
    }

    @Transactional
    public FestivalDto createFestival(FestivalDto dto) {
        FestivalEntity entity = new FestivalEntity(
                dto.getNameShort(), dto.getNameFull(), dto.getPeriodOfYear(), dto.getType(), dto.getWebsite(),
                dto.getUrlFacebook(), dto.getUrlInstagram(), dto.getUrlYoutube(),
                dto.getActivities(), dto.getImageTop(), dto.getImageLogo(), dto.getDestinationId());
        return toDto(festivalRepo.save(entity));
    }

    @Transactional
    public Optional<FestivalDto> updateFestival(Long id, FestivalDto dto) {
        return festivalRepo.findById(id).map(entity -> {
            entity.setNameShort(dto.getNameShort());
            entity.setNameFull(dto.getNameFull());
            entity.setPeriodOfYear(dto.getPeriodOfYear());
            entity.setType(dto.getType());
            entity.setWebsite(dto.getWebsite());
            entity.setUrlFacebook(dto.getUrlFacebook());
            entity.setUrlInstagram(dto.getUrlInstagram());
            entity.setUrlYoutube(dto.getUrlYoutube());
            entity.setActivities(dto.getActivities());
            entity.setImageTop(dto.getImageTop());
            entity.setImageLogo(dto.getImageLogo());
            entity.setDestinationId(dto.getDestinationId());
            return toDto(festivalRepo.save(entity));
        });
    }

    @Transactional
    public void deleteFestival(Long id) {
        // A festival owns its editions — remove them first so no orphaned rows are left behind.
        editionRepo.deleteByFestivalId(id);
        festivalRepo.deleteById(id);
    }

    // ───────────────────── Festival editions ───────────────────────────────

    public Page<FestivalEditionDto> getAllEditions(int page, int size) {
        return editionRepo.findAllByOrderByDateFromDesc(PageRequest.of(page, size))
                .map(this::toDto);
    }

    public Optional<FestivalEditionDto> getEditionById(Long id) {
        return editionRepo.findById(id).map(this::toDto);
    }

    public List<FestivalEditionDto> getEditionsByFestival(Long festivalId) {
        return editionRepo.findByFestivalIdOrderByDateFromDesc(festivalId).stream()
                .map(this::toDto).toList();
    }

    public List<FestivalEditionDto> getUpcomingEditions() {
        return editionRepo.findByDateFromGreaterThanEqualOrderByDateFromAsc(LocalDate.now()).stream()
                .map(this::toDto).toList();
    }

    public long countEditionsByFestival(Long festivalId) {
        return editionRepo.countByFestivalId(festivalId);
    }

    public Page<FestivalEditionDto> searchEditions(String keyword, int page, int size) {
        try {
            return editionRepo.searchByKeyword(keyword, PageRequest.of(page, size))
                    .map(this::toDto);
        } catch (Exception e) {
            log.error("Error searching festival editions for '{}': {}", keyword, e.getMessage());
            return Page.empty();
        }
    }

    @Transactional
    public FestivalEditionDto createEdition(FestivalEditionDto dto) {
        FestivalEditionEntity entity = new FestivalEditionEntity(
                dto.getFestivalId(), dto.getTitle(), dto.getSubtitle(),
                dto.getDateFrom(), dto.getDateTo(), dto.getEditionDescription(),
                dto.getTitleOfPlace(), dto.getAddressOfPlace(),
                dto.getUrlPlanned(), dto.getUrlFb(), dto.getUrlInsta());
        return toDto(editionRepo.save(entity));
    }

    @Transactional
    public Optional<FestivalEditionDto> updateEdition(Long id, FestivalEditionDto dto) {
        return editionRepo.findById(id).map(entity -> {
            entity.setFestivalId(dto.getFestivalId());
            entity.setTitle(dto.getTitle());
            entity.setSubtitle(dto.getSubtitle());
            entity.setDateFrom(dto.getDateFrom());
            entity.setDateTo(dto.getDateTo());
            entity.setEditionDescription(dto.getEditionDescription());
            entity.setTitleOfPlace(dto.getTitleOfPlace());
            entity.setAddressOfPlace(dto.getAddressOfPlace());
            entity.setUrlPlanned(dto.getUrlPlanned());
            entity.setUrlFb(dto.getUrlFb());
            entity.setUrlInsta(dto.getUrlInsta());
            return toDto(editionRepo.save(entity));
        });
    }

    @Transactional
    public void deleteEdition(Long id) {
        editionRepo.deleteById(id);
    }

    // ───────────────────────── Mapping helpers ─────────────────────────────

    private FestivalDto toDto(FestivalEntity e) {
        String destinationLabel = e.getDestinationId() != null
                ? destinationRepo.findById(e.getDestinationId())
                        .map(d -> d.getCityName() + " (" + d.getCountry() + ")")
                        .orElse(null)
                : null;
        return FestivalDto.from(e, editionRepo.countByFestivalId(e.getId()), destinationLabel);
    }

    private FestivalEditionDto toDto(FestivalEditionEntity e) {
        String festivalNameShort = e.getFestivalId() != null
                ? festivalRepo.findById(e.getFestivalId()).map(FestivalEntity::getNameShort).orElse(null)
                : null;
        return FestivalEditionDto.from(e, festivalNameShort);
    }
}
