package com.photo.act.photo_act.seo;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.photo.act.photo_act.views.MainLayout.baseUrl;
import static org.jsoup.nodes.Document.OutputSettings.Syntax.xml;

/**
 * Serves a standard Sitemaps Protocol 0.9 XML sitemap at {@code /sitemap.xml}.
 *
 * ── Why this matters for social media and SEO ────────────────────────
 *
 *  Google       — discovers new articles faster via Sitemap ping;
 *                 required for Google Discover (news cards in mobile feed)
 *  Bing         — reads sitemap for IndexNow-style fast indexing
 *  Pinterest    — uses sitemap for Rich Pin discovery
 *  Facebook     — does not read sitemaps directly, but Google-indexed
 *                 pages surface better in FB search and link previews
 *  Twitter/X    — same as Facebook; SEO authority improves card display
 *
 * ── Sitemap contents ─────────────────────────────────────────────────
 *
 *  Static pages  : home (/), included with weekly changefreq
 * ── Sitemaps Protocol spec ────────────────────────────────────────────
 *  https://www.sitemaps.org/protocol.html
 *  https://developers.google.com/search/docs/crawling-indexing/sitemaps/image-sitemaps

 * ── Priority values used ──────────────────────────────────────────────
 *  1.0  reserved — never use (Google ignores it as "default spam")
 *  0.9  breaking news / featured articles (not used here)
 *  0.8  regular article pages           ← articles use this
 *  0.5  standard pages (home, about)    ← static pages use this
 *  0.3  archive / tag / category pages  (add if needed)
 *  0.1  utility pages (privacy, terms)  (add if needed)
 * ─────────────────────────────────────────────────────────────────────
 */
@RestController
public class SitemapController {

    /** W3C datetime format required by the Sitemaps 0.9 spec */
    private static final DateTimeFormatter W3C_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    /** Full ISO-8601 for articles that have precise publish times */
    private static final DateTimeFormatter W3C_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX").withZone(ZoneOffset.UTC);



    private List<String> URLS = List.of("/", "/home", "/albums", "/photos", "/events", "/learnings", "/members");


    // ─────────────────────────────────────────────────────────────────
    // Endpoints
    // ─────────────────────────────────────────────────────────────────
/// //////////////////


/// ////////////////////////
    /**
     * Main sitemap — suitable for sites with < 50,000 URLs.
     * For larger sites, replace with a sitemap index pointing to
     * paginated sitemaps (see commented SitemapIndexController below).
     *
     * Content-Type: application/xml  (required by all search engines)
     * Cache-Control: public, max-age=3600
     */
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {

//        List<Article> articles = articleService.findAll();
        String today = W3C_DATE.format(Instant.now());

        StringBuilder xml = new StringBuilder(2048);

        // ── XML declaration + namespace ───────────────────────────────
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset\n");
        xml.append("  xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n");
        // Google Image Sitemap extension — surfaces article images in
        // Google Image Search and increases Discover impression potential
        xml.append("  xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\"\n");
        // Google News Sitemap extension — required for Google News inclusion
        xml.append("  xmlns:news=\"http://www.google.com/schemas/sitemap-news/0.9\">\n\n");

        // ── Static pages ──────────────────────────────────────────────
        appendUrl(xml,
                baseUrl + "/",
                today,
                "weekly",
                "0.5",
                null, null, null);

        // Add more static pages here as your site grows:
         appendUrl(xml, baseUrl + "/photos",   today, "weekly", "0.7", null, null, null);
         appendUrl(xml, baseUrl + "/albums", today, "monthly", "0.6", null, null, null);
        appendUrl(xml, baseUrl + "/learnings", today, "weekly", "0.7", null, null, null);
        appendUrl(xml, baseUrl + "/photographers", today, "monthly", "0.6", null, null, null);
        appendUrl(xml, baseUrl + "/events", today, "monthly", "0.5", null, null, null);


/*        // ── Article pages ─────────────────────────────────────────────
        for (Article article : articles) {
            String loc        = baseUrl + "/articles/" + escXml(article.slug());
            String lastmod    = formatLastmod(article.publishedIso());
            String imageTitle = escXml(article.title());
            String imageUrl   = escXml(article.imageUrl());
            String imageCaption = escXml(article.description());

            appendUrl(xml,
                    loc,
                    lastmod,
                    "monthly",
                    "0.8",
                    imageUrl,
                    imageTitle,
                    imageCaption,
                    article);
        }*/

        xml.append("</urlset>\n");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(xml.toString());
    }

    /**
     * Appends a {@code <url>} block to the sitemap StringBuilder.
     *
     * When {@code imageUrl} is non-null, also appends a
     * {@code <image:image>} child block (Google Image Sitemap extension).

     */
    private void appendUrl(StringBuilder xml,
                            String loc,
                            String lastmod,
                            String changefreq,
                            String priority,
                            String imageUrl,
                            String imageTitle,
                            String imageCaption) {

        xml.append("  <url>\n");
        xml.append("    <loc>").append(loc).append("</loc>\n");
        xml.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        xml.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        xml.append("    <priority>").append(priority).append("</priority>\n");

        // ── Google Image Sitemap extension ────────────────────────────
        // Helps article images appear in Google Image Search.
        // The image URL here should match og:image exactly.
        if (imageUrl != null) {
            xml.append("    <image:image>\n");
            xml.append("      <image:loc>").append(imageUrl).append("</image:loc>\n");
            xml.append("      <image:title>").append(imageTitle).append("</image:title>\n");
            if (imageCaption != null && !imageCaption.isBlank()) {
                xml.append("      <image:caption>").append(imageCaption).append("</image:caption>\n");
            }
            xml.append("    </image:image>\n");
        }


        xml.append("  </url>\n\n");
    }

    /**
     * Parses an ISO-8601 datetime string and reformats it to W3C date
     * (yyyy-MM-dd) as required by the Sitemaps spec.
     * Falls back to today's date if parsing fails.
     */
    private String formatLastmod(String publishedIso) {
        if (publishedIso == null || publishedIso.isBlank()) {
            return W3C_DATE.format(Instant.now());
        }
        try {
            Instant instant = Instant.parse(publishedIso);
            return W3C_DATE.format(instant);
        } catch (Exception e) {
            return W3C_DATE.format(Instant.now());
        }
    }

    /**
     * Escapes the five XML special characters.
     * Must be applied to every dynamic value inserted into XML.
     */
    private String escXml(String value) {
        if (value == null) return "";
        return value
                .replace("&",  "&amp;")   // must be first
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&apos;");
    }
}
