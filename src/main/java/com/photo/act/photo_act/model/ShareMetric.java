package com.photo.act.photo_act.model;





import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "share_metrics",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"resource_type", "resource_id", "platform"}))
public class ShareMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resourceType;
    private String resourceId;
    private String platform;

    private long shareCount;

    private Instant lastSharedAt;

    protected ShareMetric() {}

    public ShareMetric(String resourceType,
                       String resourceId,
                       String platform) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.platform = platform;
        this.shareCount = 0;
    }

    public void increment() {
        shareCount++;
        lastSharedAt = Instant.now();
    }

    public long getShareCount() {
        return shareCount;
    }
}
