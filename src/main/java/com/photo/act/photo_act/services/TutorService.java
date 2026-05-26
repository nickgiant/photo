package com.photo.act.photo_act.services;

import com.photo.act.photo_act.dto.TutorDto;
import com.photo.act.photo_act.model.TutorEntity;
import com.photo.act.photo_act.repository.LearningRepository;
import com.photo.act.photo_act.repository.TutorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TutorService {

    private static final Logger log = LoggerFactory.getLogger(TutorService.class);

    private final TutorRepository    tutorRepo;
    private final LearningRepository learningRepo;

    public TutorService(TutorRepository tutorRepo, LearningRepository learningRepo) {
        this.tutorRepo    = tutorRepo;
        this.learningRepo = learningRepo;
    }

    public List<TutorDto> getAllTutors() {
        return tutorRepo.findAllByOrderByTutorNameAsc().stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<TutorDto> getTutorById(Long id) {
        return tutorRepo.findById(id).map(this::toDto);
    }

    public List<TutorDto> searchTutors(String keyword) {
        return tutorRepo.searchByKeyword(keyword).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public TutorDto createTutor(TutorDto dto) {
        TutorEntity entity = new TutorEntity(
                dto.getTutorName(), dto.getWebsite(),
                dto.getUrlFb(), dto.getUrlYt(), dto.getUrlInsta(),
                dto.getUrlFlickr(), dto.getUrlWikipedia(),
                dto.getUrlRef1(), dto.getUrlRef2(), dto.getUrlRef3(),
                dto.getCityBase(), dto.getCountryBase(),
                dto.getUserIdInsert(), dto.getUsername());
        return toDto(tutorRepo.save(entity));
    }

    @Transactional
    public Optional<TutorDto> updateTutor(Long id, TutorDto dto) {
        return tutorRepo.findById(id).map(entity -> {
            entity.setTutorName(dto.getTutorName());
            entity.setWebsite(dto.getWebsite());
            entity.setUrlFb(dto.getUrlFb());
            entity.setUrlYt(dto.getUrlYt());
            entity.setUrlInsta(dto.getUrlInsta());
            entity.setUrlFlickr(dto.getUrlFlickr());
            entity.setUrlWikipedia(dto.getUrlWikipedia());
            entity.setUrlRef1(dto.getUrlRef1());
            entity.setUrlRef2(dto.getUrlRef2());
            entity.setUrlRef3(dto.getUrlRef3());
            entity.setCityBase(dto.getCityBase());
            entity.setCountryBase(dto.getCountryBase());
            return toDto(tutorRepo.save(entity));
        });
    }

    @Transactional
    public void deleteTutor(Long id) {
        tutorRepo.deleteById(id);
    }

    private TutorDto toDto(TutorEntity e) {
        long count = learningRepo.countByTutorId(e.getId());
        return TutorDto.from(e, count);
    }
}
