package com.photo.act.photo_act.services;

import com.photo.act.photo_act.model.PhotoStoryView;
import com.photo.act.photo_act.repository.PhotoStoryViewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PhotoStoryViewService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoStoryViewService.class);

    /** Dedup window: same IP + same viewType within this many hours counts as one view. */
    private static final int DEDUP_HOURS = 800;

    public static final String TYPE_LIST = "List";
    public static final String TYPE_FULL = "Full";
    public static final String TYPE_LIKE = "Like";

    private final PhotoStoryViewRepository repository;

    public PhotoStoryViewService(PhotoStoryViewRepository repository) {
        this.repository = repository;
    }

    /**
     * Records a view event for a story.
     *
     * <p>Deduplication: if the same IP already has a view of the same story+viewType
     * within the last {@value #DEDUP_HOURS} hours, the insert is skipped.
     *
     * @param storyId         numeric story id
     * @param slug            story slug
     * @param userId          logged-in user id, or {@code null} for guests
     * @param ip              viewer IP address
     * @param viewType        {@link #TYPE_LIST}, {@link #TYPE_FULL}, or {@link #TYPE_LIKE}
     * @param sessionId       HTTP session id
     * @param sessionDateTime session creation time
     */
    @Transactional
    public void recordView(int storyId, String slug, Integer userId, String ip, String viewType,
                           String sessionId, LocalDateTime sessionDateTime) {
        if (ip == null || ip.isBlank()) ip = "unknown";
        if (viewType == null || viewType.isBlank()) viewType = TYPE_LIST;
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(DEDUP_HOURS);
            if (!repository.existsRecentView(storyId, ip, viewType, since)) {
                repository.save(new PhotoStoryView(storyId, slug, userId, ip, viewType, sessionId, sessionDateTime));
                logger.debug("Recorded {} view for story {} (slug={}) from {}", viewType, storyId, slug, ip);
            }
        } catch (Exception e) {
            logger.error("Error recording {} view for story {}: {}", viewType, storyId, e.getMessage());
        }
    }

    /**
     * Returns total view count (List + Full types) for a story.
     */
    public long getViewCount(int storyId) {
        try {
            return repository.countByStoryId(storyId);
        } catch (Exception e) {
            logger.error("Error fetching view count for story {}: {}", storyId, e.getMessage());
            return 0;
        }
    }

    /**
     * Records a like for a story.
     */
    @Transactional
    public void recordLike(int storyId, String slug, Integer userId, String ip,
                           String sessionId, LocalDateTime sessionDateTime) {
        recordView(storyId, slug, userId, ip, TYPE_LIKE, sessionId, sessionDateTime);
    }

    /**
     * Returns the count of distinct people (by IP) who liked a story.
     */
    public long getLikeCount(int storyId) {
        try {
            return repository.countDistinctLikersByStoryId(storyId, TYPE_LIKE);
        } catch (Exception e) {
            logger.error("Error fetching like count for story {}: {}", storyId, e.getMessage());
            return 0;
        }
    }
}
