package com.photo.act.photo_act.services;

import com.photo.act.photo_act.model.PhotoView;
import com.photo.act.photo_act.repository.PhotoViewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PhotoViewService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoViewService.class);

    /** Minimum gap between two view events from the same IP for the same photo. */
    private static final int DEDUP_HOURS = 1;

    private final PhotoViewRepository repository;

    public PhotoViewService(PhotoViewRepository repository) {
        this.repository = repository;
    }

    /**
     * Records a view for a photo.
     * Deduplicates: if the same IP already viewed this photo within the last hour,
     * the event is skipped so page-refreshes do not inflate the counter.
     *
     * @param photoId  numeric photo id
     * @param nameNew  stored filename (name_new column)
     * @param userId   logged-in user id, or {@code null} for guests
     * @param ip       viewer's IP address
     */
    @Transactional
    public void recordView(int photoId, String nameNew, Integer userId, String ip) {
        if (ip == null || ip.isBlank()) ip = "unknown";
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(DEDUP_HOURS);
            if (!repository.existsRecentView(photoId, ip, since)) {
                repository.save(new PhotoView(photoId, nameNew, userId, ip));
                logger.debug("Recorded view for photo {} from {}", photoId, ip);
            }
        } catch (Exception e) {
            logger.error("Error recording view for photo {}: {}", photoId, e.getMessage());
        }
    }

    /**
     * Returns total view count for a photo.
     */
    public long getViewCount(int photoId) {
        try {
            return repository.countByPhotoId(photoId);
        } catch (Exception e) {
            logger.error("Error fetching view count for photo {}: {}", photoId, e.getMessage());
            return 0;
        }
    }
}
