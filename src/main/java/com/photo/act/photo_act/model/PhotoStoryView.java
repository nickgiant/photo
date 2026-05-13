package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photo_stories_view",
        indexes = @Index(name = "idx_photo_stories_view_story_id", columnList = "story_id"))
public class PhotoStoryView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "story_id", nullable = false)
    private int storyId;

    @Column(name = "slug", nullable = false, length = 255)
    private String slug;

    /** Null when viewer is a guest (not logged in). */
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /**
     * "List" — story visible in stories list (card view).
     * "Full" — story opened in full detail view.
     * "Like" — user liked the story.
     */
    @Column(name = "view_type", nullable = false, length = 10)
    private String viewType;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "session_date_time")
    private LocalDateTime sessionDateTime;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    protected PhotoStoryView() {}

    public PhotoStoryView(int storyId, String slug, Integer userId, String ipAddress, String viewType,
                          String sessionId, LocalDateTime sessionDateTime) {
        this.storyId           = storyId;
        this.slug              = slug;
        this.userId            = userId;
        this.ipAddress         = ipAddress;
        this.viewType          = viewType;
        this.sessionId         = sessionId;
        this.sessionDateTime   = sessionDateTime;
        this.viewedAt          = LocalDateTime.now();
    }

    public Long          getId()              { return id; }
    public int           getStoryId()         { return storyId; }
    public String        getSlug()            { return slug; }
    public Integer       getUserId()          { return userId; }
    public String        getIpAddress()       { return ipAddress; }
    public String        getViewType()        { return viewType; }
    public String        getSessionId()       { return sessionId; }
    public LocalDateTime getSessionDateTime() { return sessionDateTime; }
    public LocalDateTime getViewedAt()        { return viewedAt; }
}
