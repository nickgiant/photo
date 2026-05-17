package com.photo.act.photo_act.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Spring MVC fallback CDN file server.
 *
 * In production Nginx serves /cdn/** directly from the filesystem — this
 * controller is only reached when:
 *   1. Running in development without Nginx
 *   2. A CDN variant has not yet been generated
 *
 * Security: variant names are checked against an allowlist, and filename
 * is sanitised to prevent path traversal.
 *
 * URL:  GET /cdn/{variant}/{filename}
 */
@Controller
@Slf4j
public class CdnController {

    private static final Set<String> ALLOWED_VARIANTS =
            Set.of("og", "pinterest", "medium", "thumb", "original");

    @Value("${app.cdn.root:/var/www/photoact/cdn}")
    private String cdnRoot;

    @GetMapping("/cdn/{variant}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<byte[]> serve(
            @PathVariable String variant,
            @PathVariable String filename) {

        // Allowlist check
        if (!ALLOWED_VARIANTS.contains(variant)) {
            return ResponseEntity.badRequest().build();
        }

        // Sanitise filename — keep alphanumeric, dots, underscores, hyphens
        String safe = filename.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        Path filePath = Paths.get(cdnRoot, variant, safe).normalize();

        // Confirm the resolved path stays inside cdnRoot (extra traversal guard)
        if (!filePath.startsWith(Paths.get(cdnRoot).normalize())) {
            log.warn("CDN path traversal attempt: {}/{}", variant, filename);
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(filePath)) {
            log.debug("CDN file not found: {}", filePath);
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] bytes = Files.readAllBytes(filePath);
            String contentType = filePath.toString().endsWith(".webp")
                    ? "image/webp" : "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)
                            .cachePublic()
                            .immutable())
                    .header(HttpHeaders.VARY, "Accept")
                    .header("Access-Control-Allow-Origin", "*")
                    .body(bytes);
        } catch (IOException e) {
            log.error("CDN read error {}/{}: {}", variant, safe, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
