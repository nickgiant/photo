package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news_likes",
    indexes = {
        @Index(name = "idx_news_likes_news_id", columnList = "news_id"),
        @Index(name = "idx_news_likes_ip",      columnList = "ip_address")
    })
public class NewsLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Write-side FK column. */
    @Column(name = "news_id", nullable = false)
    private Long newsId;

    /** Read-side FK — gives Hibernate the FK constraint in DDL. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_news_likes_news_id"))
    private NewsEntity news;

    /** Null when liker is a guest (not logged in). */
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "session_date_time")
    private LocalDateTime sessionDateTime;

    @Column(name = "liked_at", nullable = false)
    private LocalDateTime likedAt;

    protected NewsLikeEntity() {}

    public NewsLikeEntity(Long newsId, Integer userId, String ipAddress,
                          String sessionId, LocalDateTime sessionDateTime) {
        this.newsId          = newsId;
        this.userId          = userId;
        this.ipAddress       = ipAddress;
        this.sessionId       = sessionId;
        this.sessionDateTime = sessionDateTime;
        this.likedAt         = LocalDateTime.now();
    }

    public Long          getId()              { return id; }
    public Long          getNewsId()          { return newsId; }
    public Integer       getUserId()          { return userId; }
    public String        getIpAddress()       { return ipAddress; }
    public String        getSessionId()       { return sessionId; }
    public LocalDateTime getSessionDateTime() { return sessionDateTime; }
    public LocalDateTime getLikedAt()         { return likedAt; }
}
