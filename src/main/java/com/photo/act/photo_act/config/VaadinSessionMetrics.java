package com.photo.act.photo_act.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks active Vaadin sessions as a Prometheus gauge.
 *
 * Vaadin's Spring integration auto-discovers VaadinServiceInitListener beans,
 * so @Component is sufficient — no extra registration needed.
 *
 * Metric: vaadin.sessions.active
 */
@Component
public class VaadinSessionMetrics implements VaadinServiceInitListener {

    private final AtomicLong activeSessions = new AtomicLong(0);

    public VaadinSessionMetrics(MeterRegistry registry) {
        Gauge.builder("vaadin.sessions.active", activeSessions, AtomicLong::doubleValue)
             .description("Number of active Vaadin UI sessions")
             .register(registry);
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(e -> activeSessions.incrementAndGet());
        event.getSource().addSessionDestroyListener(e -> activeSessions.decrementAndGet());
    }
}
