package com.photo.act.photo_act.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Business-level counters — inject this into any service that generates events
 * you want tracked in Prometheus/Grafana.
 *
 * Usage in a service:
 *   @Autowired AppMetrics metrics;
 *   ...
 *   metrics.photoUploads.increment();
 */
@Component
public class AppMetrics {

    // ── Authentication ────────────────────────────────────────────────────────
    public final Counter userLogins;
    public final Counter loginFailures;

    // ── Photos ────────────────────────────────────────────────────────────────
    public final Counter photoUploads;
    public final Counter photoViews;
    public final Counter photoDownloads;

    // ── Albums ────────────────────────────────────────────────────────────────
    public final Counter albumCreations;
    public final Counter albumViews;

    // ── Social ────────────────────────────────────────────────────────────────
    public final Counter photoLikes;

    public AppMetrics(MeterRegistry registry) {
        this.userLogins = Counter.builder("app.users.logins")
                .description("Successful user logins")
                .register(registry);

        this.loginFailures = Counter.builder("app.users.login.failures")
                .description("Failed login attempts")
                .register(registry);

        this.photoUploads = Counter.builder("app.photos.uploads")
                .description("Total photo uploads")
                .register(registry);

        this.photoViews = Counter.builder("app.photos.views")
                .description("Total photo views")
                .register(registry);

        this.photoDownloads = Counter.builder("app.photos.downloads")
                .description("Total photo downloads")
                .register(registry);

        this.albumCreations = Counter.builder("app.albums.created")
                .description("Total albums created")
                .register(registry);

        this.albumViews = Counter.builder("app.albums.views")
                .description("Total album views")
                .register(registry);

        this.photoLikes = Counter.builder("app.photos.likes")
                .description("Total photo likes")
                .register(registry);
    }
}
