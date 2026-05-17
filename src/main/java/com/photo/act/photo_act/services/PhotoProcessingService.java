package com.photo.act.photo_act.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates CDN-ready image variants from a source photo.
 *
 * Pipeline:
 *   1. Tries Python (Pillow) — best quality, HEIC support, smart crop
 *   2. Falls back to Java (Thumbnailator) if Python/script unavailable
 *
 * CDN layout under app.cdn.root:
 *   og/          1200×630   Facebook / LinkedIn / Twitter / Threads / WhatsApp
 *   pinterest/   1000×1500  Pinterest 2:3 portrait
 *   medium/      ≤1200×?    Web display (keep aspect ratio)
 *   thumb/       400×300    Gallery grid thumbnails
 *
 * Call process() synchronously, or processAsync() to fire-and-forget.
 */
@Service
@Slf4j
public class PhotoProcessingService {

    @Value("${app.cdn.root:/var/www/photoact/cdn}")
    private String cdnRoot;

    @Value("${app.cdn.script:scripts/process_photo.py}")
    private String pythonScript;

    @Value("${app.cdn.python:python3}")
    private String pythonExe;

    private final Gson gson = new Gson();

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Process a photo into all CDN variants.
     *
     * @param sourceFile absolute path to the source image (e.g. photo-show/name.jpg)
     * @param filename   output filename, including extension (e.g. "42_user_uuid.jpg")
     * @return map of variant → URL path ("/cdn/og/name.jpg", etc.)
     */
    public Map<String, String> process(String sourceFile, String filename) {
        Map<String, String> result = tryPython(sourceFile, filename);
        if (result != null && !result.isEmpty()) {
            log.info("CDN via Python — {} variants for {}", result.size(), filename);
            return result;
        }
        log.info("Python unavailable — CDN Java fallback for {}", filename);
        return processJava(sourceFile, filename);
    }

    /**
     * Async fire-and-forget — call after upload completes so the user response
     * is not blocked by image processing.
     */
    @Async
    public void processAsync(String sourceFile, String filename) {
        try {
            process(sourceFile, filename);
        } catch (Exception e) {
            log.error("Async CDN processing failed for {}: {}", filename, e.getMessage());
        }
    }

    // ── Python path ───────────────────────────────────────────────────────────

    private Map<String, String> tryPython(String sourceFile, String filename) {
        try {
            File script = new File(pythonScript);
            if (!script.exists()) {
                log.debug("Python script not found at {}", pythonScript);
                return null;
            }

            ProcessBuilder pb = new ProcessBuilder(
                    pythonExe, "-u", script.getAbsolutePath(),
                    "--input", sourceFile,
                    "--cdn-root", cdnRoot,
                    "--filename", filename
            );
            pb.redirectErrorStream(false);
            Process proc = pb.start();

            String stdout;
            try (InputStream is = proc.getInputStream()) {
                stdout = new String(is.readAllBytes());
            }
            String stderr;
            try (InputStream es = proc.getErrorStream()) {
                stderr = new String(es.readAllBytes());
            }
            int exit = proc.waitFor();

            if (exit != 0) {
                log.warn("Python CDN failed (exit {}): {}", exit, stderr.trim());
                return null;
            }
            if (!stderr.isBlank()) {
                log.debug("Python CDN stderr: {}", stderr.trim());
            }

            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> parsed = gson.fromJson(stdout, mapType);

            if (parsed == null || parsed.containsKey("error")) {
                log.warn("Python CDN error response: {}", parsed);
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> variants =
                    (Map<String, Map<String, Object>>) parsed.get("variants");
            if (variants == null) return null;

            Map<String, String> urls = new HashMap<>();
            variants.forEach((variant, info) -> {
                Object urlPath = info.get("url_path");
                if (urlPath != null) urls.put(variant, urlPath.toString());
            });
            return urls;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Python CDN interrupted for {}", filename);
            return null;
        } catch (Exception e) {
            log.warn("Python CDN invocation error: {}", e.getMessage());
            return null;
        }
    }

    // ── Java fallback ─────────────────────────────────────────────────────────

    private Map<String, String> processJava(String sourceFile, String filename) {
        Map<String, String> urls = new HashMap<>();
        File src = new File(sourceFile);
        if (!src.exists()) {
            log.error("Source file not found for CDN: {}", sourceFile);
            return urls;
        }

        String stem = filename.contains(".")
                ? filename.substring(0, filename.lastIndexOf('.'))
                : filename;
        String outName = stem + ".jpg";

        try {
            // og: 1200×630 — smart centre crop for exact social preview
            File ogDir = ensureDir(cdnRoot + "/og");
            Thumbnails.of(src)
                    .size(1200, 630)
                    .useExifOrientation(true)
                    .crop(Positions.CENTER)
                    .outputQuality(0.85)
                    .toFile(new File(ogDir, outName));
            urls.put("og", "/cdn/og/" + outName);

            // pinterest: 1000×1500 — portrait crop
            File pinDir = ensureDir(cdnRoot + "/pinterest");
            Thumbnails.of(src)
                    .size(1000, 1500)
                    .useExifOrientation(true)
                    .crop(Positions.CENTER)
                    .outputQuality(0.82)
                    .toFile(new File(pinDir, outName));
            urls.put("pinterest", "/cdn/pinterest/" + outName);

            // medium: max 1200 wide, keep original aspect ratio
            File medDir = ensureDir(cdnRoot + "/medium");
            Thumbnails.of(src)
                    .width(1200)
                    .useExifOrientation(true)
                    .outputQuality(0.82)
                    .toFile(new File(medDir, outName));
            urls.put("medium", "/cdn/medium/" + outName);

            // thumb: 400×300 centre crop
            File thumbDir = ensureDir(cdnRoot + "/thumb");
            Thumbnails.of(src)
                    .size(400, 300)
                    .useExifOrientation(true)
                    .crop(Positions.CENTER)
                    .outputQuality(0.75)
                    .toFile(new File(thumbDir, outName));
            urls.put("thumb", "/cdn/thumb/" + outName);

            log.info("Java CDN done — {} variants for {}", urls.size(), filename);
        } catch (IOException e) {
            log.error("Java CDN processing failed for {}: {}", filename, e.getMessage());
        }
        return urls;
    }

    private File ensureDir(String path) {
        File dir = new File(path);
        dir.mkdirs();
        return dir;
    }
}
