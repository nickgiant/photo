package com.photo.act.photo_act.seo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Serves a standard Sitemaps Protocol 0.9 XML sitemap at {@code /sitemap.xml}.
 *
 * The controller itself does no querying or XML building — it just serves the
 * string {@link SitemapGenerationJob} last cached. That job isolates the query
 * side ({@link SitemapQueryService}) from the output side ({@link SitemapXmlWriter}),
 * and reruns hourly so the sitemap stays current without hitting the database
 * on every crawler request.
 *
 * ── Why this matters for social media and SEO ────────────────────────
 *
 *  Google       — discovers new articles faster via Sitemap ping;
 *                 required for Google Discover (news cards in mobile feed)
 *  Bing         — reads sitemap for IndexNow-style fast indexing
 *  Pinterest    — uses sitemap for Rich Pin discovery
 *
 * ── Sitemaps Protocol spec ────────────────────────────────────────────
 *  https://www.sitemaps.org/protocol.html
 */
@RestController
@RequiredArgsConstructor
public class SitemapController {

    private final SitemapGenerationJob sitemapGenerationJob;
    private final SitemapQueryService sitemapQueryService;
    private final SitemapXmlWriter sitemapXmlWriter;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        String xml = sitemapGenerationJob.getCachedXml();

        // Only hit before the startup @PostConstruct run completes.
        if (xml == null) {
            xml = sitemapXmlWriter.write(sitemapQueryService.collectAllEntries());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(xml);
    }
}
