package com.photo.act.photo_act.services;

import com.photo.act.photo_act.model.LearningView;
import com.photo.act.photo_act.repository.LearningViewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LearningViewService {

    private static final Logger logger = LoggerFactory.getLogger(LearningViewService.class);

    /** Dedup window: same IP + same viewType within this many hours counts as one view. */
    private static final int DEDUP_HOURS = 800;

    public static final String TYPE_LIST = "List";
    public static final String TYPE_FULL = "Full";
    public static final String TYPE_LIKE = "Like";

    private final LearningViewRepository repository;

    public LearningViewService(LearningViewRepository repository) {
        this.repository = repository;
    }

    /**
     * Records a view event for a learning.
     *
     * <p>Deduplication: if the same IP already has a view of the same learning+viewType
     * within the last {@value #DEDUP_HOURS} hours, the insert is skipped.
     *
     * @param learningId      numeric learning id
     * @param slug            learning slug
     * @param userId          logged-in user id, or {@code null} for guests
     * @param ip              viewer IP address
     * @param viewType        {@link #TYPE_LIST}, {@link #TYPE_FULL}, or {@link #TYPE_LIKE}
     * @param sessionId       HTTP session id
     * @param sessionDateTime session creation time
     */
    @Transactional
    public void recordView(int learningId, String slug, Integer userId, String ip, String viewType,
                           String sessionId, LocalDateTime sessionDateTime) {
        if (ip == null || ip.isBlank()) ip = "unknown";
        if (viewType == null || viewType.isBlank()) viewType = TYPE_LIST;
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(DEDUP_HOURS);
            if (!repository.existsRecentView(learningId, ip, viewType, since)) {
                repository.save(new LearningView(learningId, slug, userId, ip, viewType, sessionId, sessionDateTime));
                logger.debug("Recorded {} view for learning {} (slug={}) from {}", viewType, learningId, slug, ip);
            }
        } catch (Exception e) {
            logger.error("Error recording {} view for learning {}: {}", viewType, learningId, e.getMessage());
        }
    }

    /**
     * Returns total view count (all types) for a learning.
     */
    public long getViewCount(int learningId) {
        try {
            return repository.countByLearningId(learningId);
        } catch (Exception e) {
            logger.error("Error fetching view count for learning {}: {}", learningId, e.getMessage());
            return 0;
        }
    }

    /**
     * Returns view count for a specific view type.
     */
    public long getViewCountByType(int learningId, String viewType) {
        try {
            return repository.countByLearningIdAndViewType(learningId, viewType);
        } catch (Exception e) {
            logger.error("Error fetching {} view count for learning {}: {}", viewType, learningId, e.getMessage());
            return 0;
        }
    }

    /**
     * Records a like for a learning.
     */
    @Transactional
    public void recordLike(int learningId, String slug, Integer userId, String ip,
                           String sessionId, LocalDateTime sessionDateTime) {
        recordView(learningId, slug, userId, ip, TYPE_LIKE, sessionId, sessionDateTime);
    }

    /**
     * Returns the count of distinct people (by IP) who liked a learning.
     */
    public long getLikeCount(int learningId) {
        try {
            return repository.countDistinctLikersByLearningId(learningId, TYPE_LIKE);
        } catch (Exception e) {
            logger.error("Error fetching like count for learning {}: {}", learningId, e.getMessage());
            return 0;
        }
    }
}
