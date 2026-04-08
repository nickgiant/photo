package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Maps to the existing `content` table in MariaDB.
 * Column names reflect a realistic schema — adjust to match yours.
 *
 * Example DDL (your table may differ):
 *
 *   CREATE TABLE content (
 *     id            BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     slug          VARCHAR(255) UNIQUE NOT NULL,
 *     content_type  VARCHAR(50)  NOT NULL,
 *     title         VARCHAR(512) NOT NULL,
 *     description   TEXT,
 *     cover_image   VARCHAR(1024),       -- CDN-relative path, e.g. /uploads/photo/abc.jpg
 *     author_name   VARCHAR(255),
 *     site_name     VARCHAR(255),
 *     published_at  DATETIME,
 *     locale        VARCHAR(10)  DEFAULT 'en_US',
 *     keywords      VARCHAR(1024)
 *   );
 */
@Entity
@Table(name = "content", indexes = {
    @Index(name = "idx_content_slug", columnList = "slug")
})
@Getter
@Setter
@NoArgsConstructor
public class ContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** URL slug — used in /og/{type}/{slug} paths */
    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 50)
    private ContentType contentType;

    @Column(nullable = false, length = 512)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Relative CDN path to the cover image, e.g. /uploads/articles/hero.jpg
     * Must be made absolute by OgMetaService using app.base-url.
     */
    @Column(name = "cover_image", length = 1024)
    private String coverImage;

    @Column(name = "author_name", length = 255)
    private String authorName;

    @Column(name = "site_name", length = 255)
    private String siteName;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /** BCP-47 locale, e.g. en_US, el_GR — used in og:locale */
    @Column(length = 10)
    private String locale = "en_US";

    /** Comma-separated — used for <meta name="keywords"> */
    @Column(length = 1024)
    private String keywords;
}
