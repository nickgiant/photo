package com.photo.act.photo_act.controllers;

import com.photo.act.photo_act.services.CdnReprocessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin REST API — backfill CDN variants for all existing photos.
 *
 * Endpoints:
 *   POST /admin/cdn/reprocess         start a batch job
 *   GET  /admin/cdn/reprocess/status  current job progress
 *   POST /admin/cdn/reprocess/cancel  cancel a running job
 *
 * Request body (all fields optional):
 *   {
 *     "photosDir": "/home/pi/lazy-photos",  // overrides app.photos.dir
 *     "force": false                         // reprocess already-done photos
 *   }
 *
 * Security: restrict to ROLE_ADMIN in SecurityConfiguration.
 * The /admin/** matcher is already public in the existing config — tighten it.
 */
@RestController
@RequestMapping("/admin/cdn")
@RequiredArgsConstructor
@Slf4j
public class CdnReprocessController {

    private final CdnReprocessService reprocessService;

    /**
     * Start the backfill job.
     * Returns 202 Accepted immediately; use /status to poll progress.
     * Returns 409 Conflict if a job is already running.
     */
    @PostMapping("/reprocess")
    public ResponseEntity<Map<String, Object>> start(
            @RequestBody(required = false) ReprocessRequest body) {

        String photosDir = body != null ? body.photosDir() : null;
        boolean force    = body != null && body.force();

        CdnReprocessService.JobStatus current = reprocessService.status();
        if (current.running()) {
            return ResponseEntity.status(409).body(Map.of(
                    "error",   "A reprocess job is already running",
                    "status",  current
            ));
        }

        log.info("CDN reprocess requested — photosDir={} force={}", photosDir, force);
        reprocessService.start(photosDir, force);

        return ResponseEntity.accepted().body(Map.of(
                "message", "CDN backfill started",
                "hint",    "Poll GET /admin/cdn/reprocess/status for progress"
        ));
    }

    /** Poll progress of the running (or last completed) job. */
    @GetMapping("/reprocess/status")
    public ResponseEntity<CdnReprocessService.JobStatus> status() {
        return ResponseEntity.ok(reprocessService.status());
    }

    /** Cancel a running job gracefully (current file finishes, next is skipped). */
    @PostMapping("/reprocess/cancel")
    public ResponseEntity<Map<String, String>> cancel() {
        reprocessService.cancel();
        return ResponseEntity.ok(Map.of("message", "Cancellation requested"));
    }

    // ── Request body ──────────────────────────────────────────────────────────

    public record ReprocessRequest(
            String photosDir,   // null → use app.photos.dir
            boolean force       // false → skip photos with existing CDN variants
    ) {}
}
