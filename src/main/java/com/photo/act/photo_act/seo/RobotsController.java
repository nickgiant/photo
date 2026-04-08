package com.photo.act.photo_act.seo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import static com.photo.act.photo_act.views.MainLayout.baseUrl;

/**
 * Serves a dynamic {@code /robots.txt} that explicitly allow-lists
 * Facebook's scraper bot ({@code facebookexternalhit}) and all other
 * major social-media / search-engine crawlers needed for OG previews.
 *
 * ── Why a controller instead of a static file? ───────────────────────
 *
 * Vaadin maps its servlet to {@code /*} which intercepts everything,
 * including {@code /robots.txt}. A {@code @RestController} is resolved
 * by Spring MVC BEFORE Vaadin's catch-all servlet, so this mapping
 * wins reliably without any extra servlet configuration.
 *
 * A static file in {@code src/main/resources/static/robots.txt} also
 * works but cannot be environment-specific (e.g. different rules for
 * staging vs production). This controller reads the base URL from
 * {@code application.properties} so the sitemap URL is always correct.
 *
 * ── Facebook scraping requirements ───────────────────────────────────
 *
 * Facebook's crawler identifies itself as:
 *   User-Agent: facebookexternalhit/1.1
 *   User-Agent: facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)
 *   User-Agent: Facebot
 *
 * For OG tags to appear in Facebook / Instagram link previews:
 *   1. The page must return HTTP 200 (not redirect, not 403).
 *   2. robots.txt must NOT disallow facebookexternalhit.
 *   3. The HTML <head> must contain og:title, og:image, og:url.
 *   4. og:image must be publicly reachable over HTTPS (no auth).
 *   5. og:image must be ≥ 200×200 px (1200×630 recommended).
 *
 * ── robots.txt rule summary ───────────────────────────────────────────
 *
 *   facebookexternalhit  → Allow: /          (OG scraping)
 *   Facebot              → Allow: /          (OG scraping alias)
 *   Twitterbot           → Allow: /          (Twitter/X cards)
 *   LinkedInBot          → Allow: /          (LinkedIn previews)
 *   Pinterest            → Allow: /          (Pinterest rich pins)
 *   Slackbot             → Allow: /          (Slack unfurling)
 *   Discordbot           → Allow: /          (Discord embeds)
 *   WhatsApp             → Allow: /          (WhatsApp previews)
 *   TelegramBot          → Allow: /          (Telegram link previews)
 *   Googlebot            → Allow: /          (SEO indexing)
 *   User-agent: *        → Disallow: /VAADIN/ (Vaadin internals — no crawl value)
 *   User-agent: *        → Allow: / (everything else — adjust for private apps)
 *
 * ─────────────────────────────────────────────────────────────────────
 */
@RestController
public class RobotsController {



    /**
     * Serves {@code /robots.txt} with correct {@code Content-Type: text/plain}.
     *
     * Cached for 1 hour by CDN / reverse proxies. Crawlers also
     * cache robots.txt internally (Google caches it for ~24 hours).
     */
    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robotsTxt() {

        String robots = buildRobotsTxt();

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(robots);
    }

    // ─────────────────────────────────────────────────────────────────
    // Private builder
    // ─────────────────────────────────────────────────────────────────

    private String buildRobotsTxt() {
        return """
                # robots.txt — %s
                # Generated dynamically by RobotsController.java
                # Reference: https://developers.facebook.com/docs/sharing/webmasters/
                #            https://developers.google.com/search/docs/crawling-indexing/robots/intro

                # ── Facebook scraper (required for OG link previews) ──────────
                # facebookexternalhit fetches pages when a URL is shared on
                # Facebook or Instagram. Allowing it is REQUIRED for og:image,
                # og:title, og:description to appear in link previews.
                User-agent: facebookexternalhit
                Allow: /

                # Facebot is Facebook's secondary crawler alias
                User-agent: Facebot
                Allow: /

                # ── Twitter / X Cards ────────────────────────────────────────
                User-agent: Twitterbot
                Allow: /

                # ── LinkedIn link previews ────────────────────────────────────
                User-agent: LinkedInBot
                Allow: /

                # ── Pinterest Rich Pins ───────────────────────────────────────
                User-agent: Pinterest
                Allow: /

                # ── Slack unfurling ───────────────────────────────────────────
                User-agent: Slackbot
                Allow: /

                User-agent: Slackbot-LinkExpanding
                Allow: /

                # ── Discord embeds ────────────────────────────────────────────
                User-agent: Discordbot
                Allow: /

                # ── WhatsApp link previews ────────────────────────────────────
                User-agent: WhatsApp
                Allow: /

                # ── Telegram link previews ────────────────────────────────────
                User-agent: TelegramBot
                Allow: /

                # ── iMessage / Apple ──────────────────────────────────────────
                User-agent: Applebot
                Allow: /

                # ── Google (SEO + Discover + rich results) ────────────────────
                User-agent: Googlebot
                Allow: /
                Disallow: /VAADIN/

                # ── Bing / Microsoft ──────────────────────────────────────────
                User-agent: Bingbot
                Allow: /
                Disallow: /VAADIN/

                # ── All other crawlers ────────────────────────────────────────
                # Disallow Vaadin internal resources (no SEO value, large bundles)
                User-agent: *
                Disallow: /VAADIN/
                Disallow: /vaadinServlet/
                Allow: /

                # ── Sitemap ───────────────────────────────────────────────────
                Sitemap: %s/sitemap.xml
                """.formatted(baseUrl, baseUrl);
    }
}
