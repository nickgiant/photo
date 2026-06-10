package com.photo.act.photo_act.seo;

import com.photo.act.photo_act.services.BotDetectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Strategy B: detect bots on content routes within Spring, before Vaadin handles them.
 *
 * In production, Nginx performs the same detection. This interceptor is the fallback
 * for environments where Nginx is unavailable (e.g., local dev or direct port access).
 *
 * Bots are redirected to /og/{type}/{slug} so OgMetaController returns pre-rendered
 * OG HTML. Real users pass through unchanged and Vaadin's router takes over.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OgBotInterceptor implements HandlerInterceptor {

    private static final Set<String> CONTENT_TYPES = Set.of(
            "photographer", "album", "photo", "event", "learning", "article", "story", "news"
    );

    private final BotDetectionService botDetectionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String path = request.getRequestURI();
        String[] segments = path.split("/", -1);

        // Only intercept two-segment content routes: /{type}/{slug}
        if (segments.length != 3 || !CONTENT_TYPES.contains(segments[1])) {
            return true;
        }

        String ua = request.getHeader(HttpHeaders.USER_AGENT);
        if (botDetectionService.isBot(ua)) {
            String ogPath = "/og" + path;
            log.info("Bot intercepted on {} (UA={}), redirecting to {}", path, ua, ogPath);
            response.sendRedirect(request.getContextPath() + ogPath);
            return false;
        }

        return true; // not a bot — let Vaadin handle the request
    }
}
