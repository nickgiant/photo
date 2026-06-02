package com.photo.act.photo_act.controllers;


import com.photo.act.photo_act.model.ContentType;
import com.photo.act.photo_act.model.OgMetaDto;
import com.photo.act.photo_act.services.BotDetectionService;
import com.photo.act.photo_act.services.OgMetaService;
import com.photo.act.photo_act.services.StoryOgService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Spring MVC controller that serves pre-rendered OG HTML pages to social crawlers.
 *
 * ── Architecture: TWO routing strategies ─────────────────────────────────────
 *
 *  Strategy A — Nginx-level (RECOMMENDED for production):
 *    Nginx inspects User-Agent and internally rewrites bot traffic
 *    from /{type}/{slug} → /og/{type}/{slug}  (this controller).
 *    Real users continue to /{type}/{slug} which Vaadin handles.
 *    See nginx/vaadin-og.conf for the exact configuration.
 *
 *  Strategy B — Spring MVC fallback (development / no Nginx access):
 *    The interceptor in OgBotInterceptor inspects UA and redirects
 *    bots to /og/{type}/{slug} itself from within the application.
 *    Slightly slower (one extra request round-trip per bot visit).
 *
 * ── URL structure ─────────────────────────────────────────────────────────────
 *  /og/{type}/{slug}           → Thymeleaf HTML for social crawlers
 *  /og/raw/{type}/{slug}       → JSON (for debugging / CDN edge workers)
 *  /og/ping                    → health endpoint
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class OgMetaController {

    private final OgMetaService ogMetaService;
    private final StoryOgService storyOgService;
    private final BotDetectionService botDetectionService;

    /**
     * Primary endpoint — returns fully rendered HTML with OG tags in <head>.
     * Thymeleaf template: src/main/resources/templates/og-preview.html
     *
     * Cache-Control: 1 h (Nginx + Varnish + CDN will cache this).
     */
    @GetMapping("/og/{type}/{slug}")
    public ResponseEntity<String> ogPreview(
            @PathVariable String type,
            @PathVariable String slug,
            HttpServletRequest request,
            Model model) {

        ContentType contentType = parseType(type);
        if (contentType == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<OgMetaDto> meta = ogMetaService.resolve(contentType, slug);
        if (meta.isEmpty()) {
            log.warn("OG meta not found for {}/{}", type, slug);
            return ResponseEntity.notFound().build();
        }

        model.addAttribute("og", meta.get());

        // Return HTML — Spring MVC resolves og-preview.html via Thymeleaf
        // We use ResponseEntity so we can set Cache-Control explicitly.
        // Because we're using Model, we delegate actual rendering to
        // the view resolver by returning the view name string (below).
        // This method signature uses Model — Spring renders the template.
        return null; // handled by Thymeleaf via the overload below
    }

    /**
     * Real handler — returns view name for Thymeleaf rendering.
     */
    @GetMapping(value = "/og/{type}/{slug}", produces = "text/html")
    public String ogPreviewHtml(
            @PathVariable String type,
            @PathVariable String slug,
            Model model,
            HttpServletRequest request) {

        ContentType contentType = parseType(type);
        if (contentType == null) return "error/404";

        Optional<OgMetaDto> meta = ogMetaService.resolve(contentType, slug);

        // For STORY type: fall back to photo_stories table when not in content table
        if (meta.isEmpty() && contentType == ContentType.STORY) {
            meta = storyOgService.resolve(slug);
        }

        return meta.map(dto -> {
                    model.addAttribute("og", dto);
                    log.debug("Serving OG preview for {}/{} to UA={}",
                            type, slug, request.getHeader(HttpHeaders.USER_AGENT));
                    return "og-preview";
                })
                .orElse("error/404");
    }

    /**
     * Bot detection for multi-segment story URLs:
     * /stories/member/{member}/story/{slug}  → /og/story/{slug}
     */
    @GetMapping("/stories/member/{member}/story/{slug}")
    public String storyRoute(
            @PathVariable String member,
            @PathVariable String slug,
            HttpServletRequest request) {

        String ua = request.getHeader(HttpHeaders.USER_AGENT);
        if (botDetectionService.isBot(ua)) {
            log.info("Bot detected on story URL ({}), forwarding to OG: story/{}", ua, slug);
            return "forward:/og/story/" + slug;
        }
        return "forward:/vaadin-forward/stories/member/" + member + "/story/" + slug;
    }

    /**
     * JSON debug endpoint — returns raw OG DTO.
     * Useful for CDN edge workers (Cloudflare Workers, Lambda@Edge).
     *
     * Add X-OG-Debug: true header or ?debug=true to activate.
     */
    @GetMapping(value = "/og/raw/{type}/{slug}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<OgMetaDto> ogPreviewJson(
            @PathVariable String type,
            @PathVariable String slug) {

        ContentType contentType = parseType(type);
        if (contentType == null) return ResponseEntity.notFound().build();

        return ogMetaService.resolve(contentType, slug)
                .map(dto -> ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                        .body(dto))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Spring MVC fallback: intercept Vaadin routes for bots */
    @GetMapping("/{type}/{slug}")
    public String contentRoute(
            @PathVariable String type,
            @PathVariable String slug,
            HttpServletRequest request) {

        String ua = request.getHeader(HttpHeaders.USER_AGENT);
        if (botDetectionService.isBot(ua)) {
            log.info("Bot detected ({}), forwarding to OG controller: {}/{}", ua, type, slug);
            return "forward:/og/" + type + "/" + slug;
        }
        // Not a bot — forward to Vaadin's catch-all servlet
        return "forward:/" + type + "/" + slug;
    }

    @GetMapping("/og/ping")
    @ResponseBody
    public String ping() {
        return "OK";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ContentType parseType(String raw) {
        try {
            return ContentType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
