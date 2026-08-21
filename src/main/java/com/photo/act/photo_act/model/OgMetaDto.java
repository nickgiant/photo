package com.photo.act.photo_act.model;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Immutable DTO holding all Open Graph + Twitter Card + Schema.org data
 * required to render the <head> block for social crawlers.
 *
 * Must implement Serializable — stored in Redis via Java serialization
 * (or Jackson JSON if you configure GenericJackson2JsonRedisSerializer).
 */
@Getter
@Builder
public class OgMetaDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ── Core OG ──────────────────────────────────────────────────────────────
    private final String ogTitle;           // og:title
    private final String ogDescription;     // og:description  (155 chars max)
    private final String ogUrl;             // og:url          (canonical absolute URL)
    private final String ogType;            // og:type         article / website / profile
    private final String ogLocale;          // og:locale       en_US

    // ── OG Image (primary — landscape 1200×630) ───────────────────────────────
    private final String ogImage;           // og:image        absolute URL
    private final int    ogImageWidth;      // og:image:width
    private final int    ogImageHeight;     // og:image:height
    private final String ogImageAlt;        // og:image:alt

    // ── Article-specific OG (used when og:type=article) ──────────────────────
    private final String articleAuthor;     // article:author
    private final String articlePublished;  // article:published_time  ISO-8601
    private final String articleSection;    // article:section

    // ── Twitter Card ─────────────────────────────────────────────────────────
    private final String twitterCard;       // twitter:card   summary / summary_large_image
    private final String twitterTitle;      // twitter:title
    private final String twitterDescription;// twitter:description
    private final String twitterImage;      // twitter:image  (square 1:1 for summary)
    private final String twitterImageAlt;   // twitter:image:alt
    private final String twitterSite;       // twitter:site   @handle (optional)
    private final String twitterCreator;    // twitter:creator @handle (optional)

    // ── Pinterest ─────────────────────────────────────────────────────────────
    // Pinterest reads og:image — must be ≥ 600×315. No extra tags needed.
    // Rich Pins use og:type=article + article:* tags above.

    // ── LinkedIn ──────────────────────────────────────────────────────────────
    // LinkedIn reads og:* tags. Caches aggressively — use Post Inspector to refresh.

    // ── Schema.org JSON-LD (structured data) ─────────────────────────────────
    private final String schemaOrgJson;     // raw JSON-LD string injected as <script type="application/ld+json">

    // ── General HTML head ────────────────────────────────────────────────────
    private final String canonicalUrl;
    private final String siteName;
    private final String keywords;

    // ── Content type (for conditional rendering in Thymeleaf) ────────────────
    private final OgContentType contentType;
    private final String slug;
}
