package com.photo.act.photo_act.controllers;

import com.photo.act.photo_act.model.ContentType;
import com.photo.act.photo_act.services.OgMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * Admin REST API for cache management.
 *
 * Secure this endpoint with Spring Security in production:
 *   .requestMatchers("/admin/**").hasRole("ADMIN")
 *
 * Examples:
 *   DELETE /admin/cache/og-meta/ARTICLE/my-slug
 *   DELETE /admin/cache/all
 */
@RestController
@RequestMapping("/admin/cache")
@RequiredArgsConstructor
public class CacheAdminController {

    private final OgMetaService ogMetaService;
    private final CacheManager  cacheManager;

    /** Evict a specific content item from OG cache */
    @DeleteMapping("/og-meta/{type}/{slug}")
    public ResponseEntity<Map<String, String>> evict(
            @PathVariable String type,
            @PathVariable String slug) {
        try {
            ContentType ct = ContentType.valueOf(type.toUpperCase());
            ogMetaService.evict(ct, slug);
            return ResponseEntity.ok(Map.of("status", "evicted", "key", type + "::" + slug));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown type: " + type));
        }
    }

    /** Evict all OG meta caches */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> evictAll() {
        cacheManager.getCacheNames().forEach(name ->
                Objects.requireNonNull(cacheManager.getCache(name)).clear());
        return ResponseEntity.ok(Map.of("status", "all caches cleared"));
    }

    /** List all cache names */
    @GetMapping("/names")
    public ResponseEntity<Object> cacheNames() {
        return ResponseEntity.ok(Map.of("caches", cacheManager.getCacheNames()));
    }
}
