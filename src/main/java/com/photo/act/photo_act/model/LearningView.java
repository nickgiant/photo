package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learnings_view",
        indexes = @Index(name = "idx_learnings_view_learning_id", columnList = "learning_id"))
public class LearningView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "learning_id", nullable = false)
    private int learningId;

    @Column(name = "slug", length = 255)
    private String slug;

    /** Null when viewer is a guest (not logged in). */
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /**
     * "List" — learning visible in learnings list.
     * "Full" — learning opened in full detail view.
     * "Like" — user liked the learning.
     */
    @Column(name = "view_type", nullable = false, length = 10)
    private String viewType;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "session_date_time")
    private LocalDateTime sessionDateTime;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    protected LearningView() {}

    public LearningView(int learningId, String slug, Integer userId, String ipAddress, String viewType,
                        String sessionId, LocalDateTime sessionDateTime) {
        this.learningId      = learningId;
        this.slug            = slug;
        this.userId          = userId;
        this.ipAddress       = ipAddress;
        this.viewType        = viewType;
        this.sessionId       = sessionId;
        this.sessionDateTime = sessionDateTime;
        this.viewedAt        = LocalDateTime.now();
    }

    public Long          getId()              { return id; }
    public int           getLearningId()      { return learningId; }
    public String        getSlug()            { return slug; }
    public Integer       getUserId()          { return userId; }
    public String        getIpAddress()       { return ipAddress; }
    public String        getViewType()        { return viewType; }
    public String        getSessionId()       { return sessionId; }
    public LocalDateTime getSessionDateTime() { return sessionDateTime; }
    public LocalDateTime getViewedAt()        { return viewedAt; }
}
