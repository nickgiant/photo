package com.photo.act.photo_act.seo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Periodically rebuilds the sitemap XML and caches it in memory so
 * {@link SitemapController} only ever serves a pre-built string — it never
 * touches the database on a crawler request.
 *
 * Runs once at startup (so the cache isn't empty before the first crawl)
 * and then hourly, matching the Cache-Control max-age served to clients.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SitemapGenerationJob {

    private final SitemapQueryService sitemapQueryService;
    private final SitemapXmlWriter sitemapXmlWriter;

    private final AtomicReference<String> cachedXml = new AtomicReference<>();

    @PostConstruct
    @Scheduled(cron = "0 0 * * * *")
    public void regenerate() {
        try {
            List<SitemapUrlEntry> entries = sitemapQueryService.collectAllEntries();
            cachedXml.set(sitemapXmlWriter.write(entries));
            log.info("Sitemap regenerated with {} URLs", entries.size());
        } catch (Exception e) {
            log.error("Sitemap regeneration failed, keeping the previously cached version", e);
        }
    }

    /** Null only in the brief window before the first {@link #regenerate()} completes. */
    public String getCachedXml() {
        return cachedXml.get();
    }
}
