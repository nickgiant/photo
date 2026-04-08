package com.photo.act.photo_act.services;


import com.photo.act.photo_act.model.ContentEntity;
import com.photo.act.photo_act.model.ContentType;
import com.photo.act.photo_act.model.OgMetaDto;
import com.photo.act.photo_act.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.photo.act.photo_act.model.ContentType.EVENT;
import static com.photo.act.photo_act.model.ContentType.PHOTOGRAPHER;

/**
 * Builds OgMetaDto from MariaDB data and caches it in Redis.
 *
 * Cache key: "og-meta::{type}::{slug}"
 * TTL: configured via spring.cache.redis.time-to-live (default 1 h)
 *
 * ── Image optimisation rules per platform ────────────────────────────────────
 *
 *  Facebook / Instagram:
 *    • Minimum: 600 × 315 px   Recommended: 1200 × 630 px
 *    • Max file size: 8 MB     Format: JPG/PNG (no GIF for preview)
 *    • Aspect ratio: 1.91:1    (1200:630)
 *    • og:image:width + og:image:height MUST be declared
 *    • Use og:image:secure_url (HTTPS) — insecure URLs are blocked
 *
 *  Twitter / X:
 *    • summary_large_image: 1200 × 628 px minimum 300 × 157
 *    • summary card (profile): 144 × 144 px square, max 4 MB
 *    • Format: JPG, PNG, WEBP, GIF (static). Max 5 MB.
 *    • twitter:image must be HTTPS; CDN URL is fine.
 *
 *  LinkedIn:
 *    • Minimum: 1200 × 627 px   Recommended: 1200 × 628 px
 *    • Format: JPG/PNG, max 5 MB
 *    • Post Inspector URL: https://www.linkedin.com/post-inspector/
 *    • LinkedIn caches OG for 7 days — use versioned query param to bust.
 *
 *  Pinterest:
 *    • Optimal: 1000 × 1500 px (2:3 portrait) for Pin display
 *    • Minimum for Rich Pins: 600 × 315 px (uses og:image)
 *    • Rich Pins: require og:type=article + article:published_time
 *    • Format: JPG/PNG, < 10 MB
 *
 *  YouTube (community posts / descriptions):
 *    • Thumbnail: 1280 × 720 px (16:9)
 *    • og:image is not used for video embeds but IS used in link shares
 *    • Recommended: 1200 × 630 px for link-share cards
 *
 *  WhatsApp / Telegram:
 *    • Read og:image — minimum 300 × 200 px, prefer 1200 × 630
 *    • Max file size: 300 KB practical limit (Telegram compresses)
 *
 *  Slack / Discord:
 *    • Read og:* tags — 1200 × 630 recommended
 *    • Discord also reads twitter:image as fallback
 *
 * ── Conclusion: universal safe image ─────────────────────────────────────────
 *   Size: 1200 × 630 px  │  Format: JPG (80 % quality)  │  Max: 1 MB
 *   For PHOTOGRAPHER profile: additionally provide 400 × 400 square variant.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OgMetaService {

    private final ContentRepository contentRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.og.default-image}")
    private String defaultOgImage;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Resolve OG metadata by content type + slug.
     * Result is Redis-cached; key = og-meta::{type}::{slug}
     */
    @Cacheable(value = "og-meta", key = "#type.name() + '::' + #slug")
    public Optional<OgMetaDto> resolve(ContentType type, String slug) {
        log.debug("Cache miss — loading OG meta for {}/{}", type, slug);
        return contentRepository.findByContentTypeAndSlug(type, slug)
                .map(this::buildDto);
    }

    /**
     * Resolve by slug alone (slug is globally unique).
     */
    @Cacheable(value = "og-meta", key = "'slug::' + #slug")
    public Optional<OgMetaDto> resolveBySlug(String slug) {
        log.debug("Cache miss — loading OG meta for slug={}", slug);
        return contentRepository.findBySlug(slug)
                .map(this::buildDto);
    }

    /**
     * Evict cache when content is updated.
     */
    @CacheEvict(value = "og-meta", key = "#type.name() + '::' + #slug")
    public void evict(ContentType type, String slug) {
        log.info("Evicted OG meta cache for {}/{}", type, slug);
    }

    // ── DTO builder ───────────────────────────────────────────────────────────

    private OgMetaDto buildDto(ContentEntity e) {
        String absoluteImage = resolveAbsoluteImageUrl(e.getCoverImage());
        String canonicalUrl  = buildCanonicalUrl(e);
        String description   = truncate(e.getDescription(), 155);
        String published     = e.getPublishedAt() != null
                ? e.getPublishedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"
                : null;

        // For PHOTOGRAPHER: square image 400×400; for all others: 1200×630
        boolean isProfile = e.getContentType() == PHOTOGRAPHER;
        int imgW = isProfile ? 400 : 1200;
        int imgH = isProfile ? 400 : 630;

        String twitterImage = isProfile
                ? resolveAbsoluteImageUrl(e.getCoverImage()) // same image, square crop via Nginx
                : absoluteImage;

        return OgMetaDto.builder()
                // OG core
                .ogTitle(e.getTitle())
                .ogDescription(description)
                .ogUrl(canonicalUrl)
                .ogType(e.getContentType().toOgType())
                .ogLocale(e.getLocale() != null ? e.getLocale() : "en_US")
                // OG image
                .ogImage(absoluteImage)
                .ogImageWidth(imgW)
                .ogImageHeight(imgH)
                .ogImageAlt(e.getTitle())
                // Article-specific
                .articleAuthor(e.getAuthorName())
                .articlePublished(published)
                .articleSection(e.getContentType().name().toLowerCase())
                // Twitter Card
                .twitterCard(e.getContentType().toTwitterCard())
                .twitterTitle(e.getTitle())
                .twitterDescription(description)
                .twitterImage(twitterImage)
                .twitterImageAlt(e.getTitle())
                .twitterSite("@YourTwitterHandle")   // ← replace
                .twitterCreator("@YourTwitterHandle")
                // General
                .canonicalUrl(canonicalUrl)
                .siteName(e.getSiteName() != null ? e.getSiteName() : "YourSiteName")
                .keywords(e.getKeywords())
                // Schema.org
                .schemaOrgJson(buildSchemaOrgJson(e, canonicalUrl, absoluteImage))
                // Internal
                .contentType(e.getContentType())
                .slug(e.getSlug())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String resolveAbsoluteImageUrl(String coverImage) {
        if (coverImage == null || coverImage.isBlank()) return defaultOgImage;
        if (coverImage.startsWith("http")) return coverImage;
        return baseUrl + coverImage;   // e.g. https://cdn.yourdomain.com/uploads/img.jpg
    }

    private String buildCanonicalUrl(ContentEntity e) {
        String type = e.getContentType().name().toLowerCase();
        return baseUrl + "/" + type + "/" + e.getSlug();
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 1) + "…";
    }

    /**
     * Minimal Schema.org JSON-LD — extend per content type as needed.
     */
    private String buildSchemaOrgJson(ContentEntity e, String url, String imageUrl) {
        return switch (e.getContentType()) {
            case PHOTOGRAPHER -> """
                    {"@context":"https://schema.org","@type":"Person",
                     "name":"%s","url":"%s","image":"%s"}
                    """.formatted(e.getTitle(), url, imageUrl);
            case EVENT -> """
                    {"@context":"https://schema.org","@type":"Event",
                     "name":"%s","url":"%s","image":"%s",
                     "startDate":"%s"}
                    """.formatted(e.getTitle(), url, imageUrl,
                    e.getPublishedAt() != null ? e.getPublishedAt().toString() : "");
            default -> """
                    {"@context":"https://schema.org","@type":"Article",
                     "headline":"%s","url":"%s","image":"%s",
                     "author":{"@type":"Person","name":"%s"},
                     "datePublished":"%s"}
                    """.formatted(e.getTitle(), url, imageUrl,
                    e.getAuthorName() != null ? e.getAuthorName() : "",
                    e.getPublishedAt() != null ? e.getPublishedAt().toString() : "");
        };
    }
}
