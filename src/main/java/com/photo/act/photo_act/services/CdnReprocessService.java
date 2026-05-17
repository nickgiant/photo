package com.photo.act.photo_act.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Batch-processes existing photos into CDN variants.
 *
 * Scans {app.photos.dir}/photo-show/ and calls PhotoProcessingService for each
 * image that does not yet have an og/ CDN variant (unless force=true).
 *
 * Only one job runs at a time — a second request while a job is running
 * returns the current progress without starting a new one.
 */
@Service
@Slf4j
public class CdnReprocessService {

    @Value("${app.photos.dir:/home/pi/lazy-photos}")
    private String photosDir;

    @Value("${app.cdn.root:/var/www/photoact/cdn}")
    private String cdnRoot;

    private final PhotoProcessingService processor;

    // ── Job state (single-job model) ─────────────────────────────────────────

    private final AtomicBoolean running   = new AtomicBoolean(false);
    private final AtomicInteger total     = new AtomicInteger(0);
    private final AtomicInteger processed = new AtomicInteger(0);
    private final AtomicInteger skipped   = new AtomicInteger(0);
    private final AtomicInteger errors    = new AtomicInteger(0);
    private volatile String     currentFile   = "";
    private volatile String     startedAt     = "";
    private volatile String     finishedAt    = "";
    private volatile String     lastError     = "";

    public CdnReprocessService(PhotoProcessingService processor) {
        this.processor = processor;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Start the batch job asynchronously.
     *
     * @param photosDirOverride if non-null, overrides the configured app.photos.dir
     * @param force             true = reprocess even if CDN variant already exists
     * @return false if a job is already running
     */
    @Async
    public boolean start(String photosDirOverride, boolean force) {
        if (!running.compareAndSet(false, true)) {
            log.warn("CDN reprocess already running — ignoring duplicate request");
            return false;
        }

        String baseDir = (photosDirOverride != null && !photosDirOverride.isBlank())
                ? photosDirOverride : photosDir;

        reset();
        startedAt = java.time.Instant.now().toString();
        log.info("CDN backfill starting — photosDir={} force={}", baseDir, force);

        try {
            Path showDir = Paths.get(baseDir, "photo-show");
            if (!Files.isDirectory(showDir)) {
                lastError = "photo-show directory not found: " + showDir;
                log.error(lastError);
                return false;
            }

            List<Path> images = collectImages(showDir);
            total.set(images.size());
            log.info("CDN backfill: {} images found in {}", images.size(), showDir);

            for (Path img : images) {
                if (!running.get()) {
                    log.info("CDN backfill cancelled");
                    break;
                }

                String filename = img.getFileName().toString();
                currentFile = filename;

                if (!force && ogExists(filename)) {
                    skipped.incrementAndGet();
                    log.debug("CDN backfill skip (exists): {}", filename);
                    continue;
                }

                try {
                    Map<String, String> variants = processor.process(img.toString(), filename);
                    if (variants.isEmpty()) {
                        errors.incrementAndGet();
                        log.warn("CDN backfill failed (empty result): {}", filename);
                    } else {
                        processed.incrementAndGet();
                        log.info("CDN backfill ok [{}/{}]: {} → {}",
                                processed.get() + skipped.get(), images.size(),
                                filename, variants.keySet());
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                    lastError = filename + ": " + e.getMessage();
                    log.error("CDN backfill error for {}: {}", filename, e.getMessage());
                }
            }

        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("CDN backfill aborted: {}", e.getMessage(), e);
        } finally {
            currentFile  = "";
            finishedAt   = java.time.Instant.now().toString();
            running.set(false);
            log.info("CDN backfill finished — processed:{} skipped:{} errors:{}",
                    processed.get(), skipped.get(), errors.get());
        }
        return true;
    }

    /** Cancel a running job. */
    public void cancel() {
        running.set(false);
    }

    /** Snapshot of current job state — safe to call at any time. */
    public JobStatus status() {
        int done = processed.get() + skipped.get() + errors.get();
        int tot  = total.get();
        return new JobStatus(
                running.get(),
                tot,
                processed.get(),
                skipped.get(),
                errors.get(),
                tot > 0 ? (int) (done * 100.0 / tot) : 0,
                currentFile,
                startedAt,
                finishedAt,
                lastError
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void reset() {
        total.set(0);
        processed.set(0);
        skipped.set(0);
        errors.set(0);
        currentFile = "";
        finishedAt  = "";
        lastError   = "";
    }

    private List<Path> collectImages(Path dir) throws Exception {
        List<Path> list = new ArrayList<>();
        try (var stream = Files.list(dir)) {
            stream.filter(p -> {
                String name = p.getFileName().toString().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg")
                        || name.endsWith(".png") || name.endsWith(".webp");
            }).sorted().forEach(list::add);
        }
        return list;
    }

    private boolean ogExists(String filename) {
        String stem = filename.contains(".")
                ? filename.substring(0, filename.lastIndexOf('.')) : filename;
        return Paths.get(cdnRoot, "og", stem + ".jpg").toFile().exists();
    }

    // ── Status record ─────────────────────────────────────────────────────────

    public record JobStatus(
            boolean running,
            int total,
            int processed,
            int skipped,
            int errors,
            int percentDone,
            String currentFile,
            String startedAt,
            String finishedAt,
            String lastError
    ) {}
}
