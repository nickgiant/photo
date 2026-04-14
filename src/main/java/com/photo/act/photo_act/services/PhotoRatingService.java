package com.photo.act.photo_act.services;

import com.photo.act.photo_act.model.PhotoRating;
import com.photo.act.photo_act.repository.PhotoRatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PhotoRatingService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoRatingService.class);

    private final PhotoRatingRepository repository;

    public PhotoRatingService(PhotoRatingRepository repository) {
        this.repository = repository;
    }

    /**
     * Save or update a rating for a photo by a user.
     * If the user already rated this photo, updates the rating and rater IP.
     *
     * @param photoId   numeric photo id
     * @param userId    logged-in user id
     * @param rating    score 1–7
     * @param nameNew   stored filename (name_new column)
     * @param ipAddress rater's IP address
     */
    @Transactional
    public void saveOrUpdateRating(int photoId, int userId, int rating, String nameNew, String ipAddress,
                                   String sessionId, String sessionDateTime) {
        Optional<PhotoRating> existing = repository.findByPhotoIdAndUserId(photoId, userId);
        if (existing.isPresent()) {
            existing.get().updateRating(rating, ipAddress, sessionId, sessionDateTime);
            repository.save(existing.get());
            logger.info("Updated rating for photo {} by user {} to {}", photoId, userId, rating);
        } else {
            repository.save(new PhotoRating(photoId, userId, rating, nameNew, ipAddress, sessionId, sessionDateTime));
            logger.info("Saved new rating for photo {} by user {} = {}", photoId, userId, rating);
        }
    }

    /** Returns the average rating for a photo, or 0.0 if no ratings exist. */
    public double getAverageRating(int photoId) {
        Double avg = repository.findAverageRatingByPhotoId(photoId);
        return avg != null ? avg : 0.0;
    }

    /** Returns the total number of ratings for a photo. */
    public long getRatingCount(int photoId) {
        return repository.countByPhotoId(photoId);
    }

    /** Returns the rating a user gave to a photo, or 0 if none. */
    public int getUserRating(int photoId, int userId) {
        return repository.findByPhotoIdAndUserId(photoId, userId)
                .map(PhotoRating::getRating)
                .orElse(0);
    }
}
