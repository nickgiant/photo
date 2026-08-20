package com.photo.act.photo_act.seo;

import java.time.Instant;

/**
 * A single URL destined for the sitemap, independent of how it will be rendered.
 * Produced by {@link SitemapQueryService}, consumed by {@link SitemapXmlWriter}.
 */
public record SitemapUrlEntry(
        String loc,
        Instant lastmod,
        String changefreq,
        String priority
) {
}
