package com.photo.act.photo_act.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Builds absolute CDN URLs for photo variants.
 *
 * CDN variant summary (dimensions × purpose):
 *   og          1200×630   Facebook, LinkedIn, Twitter/X, Threads, WhatsApp, Instagram OG
 *   pinterest   1000×1500  Pinterest portrait (2:3 optimal ratio)
 *   medium      ≤1200×?    Web display — keeps original aspect ratio
 *   thumb       400×300    Gallery grid thumbnails
 *
 * Nginx serves /cdn/** directly from {app.cdn.root} with 1-year immutable cache.
 * The Spring MVC fallback is CdnController (used only in dev or if nginx misses).
 */
@Service
public class CdnService {

    @Value("${app.base-url:https://photoact.net}")
    private String baseUrl;

    @Value("${app.cdn.root:/var/www/photoact/cdn}")
    private String cdnRoot;

    // ── Absolute URL builders (for og:image — must be https://) ──────────────

    /** 1200×630 JPEG — use for og:image, Facebook, LinkedIn, Twitter, Threads, WhatsApp. */
    public String ogUrl(String filename) {
        return baseUrl + "/cdn/og/" + jpg(filename);
    }

    /** 1000×1500 JPEG — use for Pinterest og:image when sharing a pin. */
    public String pinterestUrl(String filename) {
        return baseUrl + "/cdn/pinterest/" + jpg(filename);
    }

    /** ≤1200px wide JPEG — web display, keeps original aspect. */
    public String mediumUrl(String filename) {
        return baseUrl + "/cdn/medium/" + jpg(filename);
    }

    /** 400×300 JPEG — grid thumbnail. */
    public String thumbUrl(String filename) {
        return baseUrl + "/cdn/thumb/" + jpg(filename);
    }

    // ── Relative URL path builders (for HTML src attributes) ─────────────────

    public String ogPath(String filename) {
        return "/cdn/og/" + jpg(filename);
    }

    public String pinterestPath(String filename) {
        return "/cdn/pinterest/" + jpg(filename);
    }

    public String mediumPath(String filename) {
        return "/cdn/medium/" + jpg(filename);
    }

    public String thumbPath(String filename) {
        return "/cdn/thumb/" + jpg(filename);
    }

    // ── Filesystem path helpers ───────────────────────────────────────────────

    /** Absolute filesystem path for the og variant. */
    public Path ogFilePath(String filename) {
        return Paths.get(cdnRoot, "og", jpg(filename));
    }

    /** True if the og variant already exists on disk. */
    public boolean ogExists(String filename) {
        return ogFilePath(filename).toFile().exists();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Normalise filename: strip path separators, ensure .jpg extension. */
    private String jpg(String filename) {
        if (filename == null || filename.isBlank()) return "default.jpg";
        String f = Paths.get(filename).getFileName().toString();
        int dot = f.lastIndexOf('.');
        String stem = dot > 0 ? f.substring(0, dot) : f;
        return stem + ".jpg";
    }
}
