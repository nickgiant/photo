package com.photo.act.photo_act.seo;

import com.vaadin.flow.server.VaadinRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Detects social-media crawler bots via the HTTP User-Agent header.
 *
 * Used by OgMetaContributor to decide whether to perform the
 * article lookup on every request, or only when a bot is detected.
 *
 * Bot list covers:
 *   Facebook, Instagram, Twitter/X, LinkedIn, Pinterest,
 *   Slack, Discord, WhatsApp, Telegram, iMessage (Apple),
 *   Google, Bing, Reddit, Embedly, Quora, VK
 */
@Component
public class BotDetector {

    private static final Pattern BOT_PATTERN = Pattern.compile(
            "facebookexternalhit|facebot|twitterbot|linkedinbot|" +
            "pinterestbot|slackbot|discordbot|whatsapp|telegrambot|" +
            "googlebot|bingbot|applebot|redditbot|embedly|" +
            "quora.link.preview|rogerbot|vkshare|w3c_validator",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Returns true if the current Vaadin request originates from
     * a known social-media or search-engine crawler.
     *
     * Safe to call from configurePage() — VaadinRequest.getCurrent()
     * is a thread-local bound for the duration of each request.
     */
    public boolean isBot(VaadinRequest request) {
        if (request == null) return false;
        String ua = request.getHeader("User-Agent");
        return ua != null && BOT_PATTERN.matcher(ua).find();
    }

    /**
     * Overload for use outside Vaadin context (e.g. Spring MVC controllers).
     */
    public boolean isBot(HttpServletRequest request) {
        if (request == null) return false;
        String ua = request.getHeader("User-Agent");
        return ua != null && BOT_PATTERN.matcher(ua).find();
    }
}
