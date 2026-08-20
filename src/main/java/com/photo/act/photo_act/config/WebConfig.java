package com.photo.act.photo_act.config;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures Spring MVC to coexist with Vaadin Flow.
 *
 * ── Key design decisions ──────────────────────────────────────────────────────
 *
 * 1. Vaadin registers its own DispatcherServlet at "/*" by default.
 *    OgMetaController needs to intercept /{type}/{slug} BEFORE Vaadin does.
 *
 * 2. The solution: declare Spring MVC routes at /og/** and
 *    use the bot-detection forward (controller returns "forward:/og/...")
 *    OR rely on Nginx to rewrite bot traffic before it hits Spring.
 *
 * 3. Static resources (CDN fallback) are served from /static/**:
 *    Nginx should serve these directly — Spring is the last resort.
 *
 * ── Vaadin servlet mapping ────────────────────────────────────────────────────
 *   vaadin.url-mapping=/  (default) — Vaadin owns everything not explicitly
 *   claimed by a Spring MVC @Controller or @RestController.
 *   Spring MVC controllers registered with @GetMapping take priority.
 *
 * ── Thread model ──────────────────────────────────────────────────────────────
 *   OG controller runs on plain Servlet thread (fast, stateless).
 *   Vaadin UI runs on Vaadin's session-scoped thread pool.
 *   The two thread models never interfere.
 */
@Configuration
@EnableVaadin("com.photo.act.photo_act.views")   // Restrict Vaadin scanning to views package
// NOTE: AppShell (the AppShellConfigurator carrying @Theme("my-app")) lives in this
// same "views" package for exactly this reason — see AppShell.java.
public class WebConfig implements WebMvcConfigurer {

    /**
     * Serve static assets and CDN files via Spring MVC.
     * Nginx intercepts /cdn/** and /static/** in production — this is the Java fallback
     * used in development or when a CDN variant has not yet been generated.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400); // 1 day

        // CDN files served from the local filesystem.
        // CdnController handles /cdn/** with path-traversal protection;
        // this handler covers any files not matched by the controller pattern.
        registry.addResourceHandler("/cdn/**")
                .addResourceLocations("file:/var/www/photoact/cdn/")
                .setCachePeriod(31536000); // 1 year
    }
}
