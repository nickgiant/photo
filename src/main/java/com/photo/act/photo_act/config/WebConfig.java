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
//@Configuration
@EnableVaadin("com.photo.act.photo_act.views")   // Restrict Vaadin scanning to views package
public class WebConfig implements WebMvcConfigurer {

    /**
     * Serve static assets from /static/** (og-default.jpg, robots.txt, etc.)
     * Nginx should intercept these in production — this is the Java fallback.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400); // 1 day browser cache
    }
}
