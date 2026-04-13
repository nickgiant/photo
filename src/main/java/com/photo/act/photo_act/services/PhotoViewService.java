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

    /** Dedup window: same IP + same viewType within this many hours counts as one view. */
    private static final int DEDUP_HOURS = 1;

    public static final String TYPE_LIST = "List";
    public static final String TYPE_FULL = "Full";

    private final PhotoViewRepository repository;

    public PhotoViewService(PhotoViewRepository repository) {
        this.repository = repository;
    }

    /**
     * Records a view event for a photo.
     *
     * <p>Deduplication: if the same IP already has a view of the same photo+viewType
     * within the last {@value #DEDUP_HOURS} hour(s), the insert is skipped.
     * List and Full views are deduped independently.
     *
     * @param photoId  numeric photo id
     * @param nameNew  stored filename (name_new column)
     * @param userId   logged-in user id, or {@code null} for guests
     * @param ip       viewer IP address
     * @param viewType {@link #TYPE_LIST} or {@link #TYPE_FULL}
     */
    @Transactional
    public void recordView(int photoId, String nameNew, Integer userId, String ip, String viewType) {
        if (ip == null || ip.isBlank()) ip = "unknown";
        if (viewType == null || viewType.isBlank()) viewType = TYPE_LIST;
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(DEDUP_HOURS);
            if (!repository.existsRecentView(photoId, ip, viewType, since)) {
                repository.save(new PhotoView(photoId, nameNew, userId, ip, viewType));
                logger.debug("Recorded {} view for photo {} from {}", viewType, photoId, ip);
            }
        } catch (Exception e) {
            logger.error("Error recording {} view for photo {}: {}", viewType, photoId, e.getMessage());
        }
    }

    /**
     * Returns total view count (all types combined) for a photo.
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
