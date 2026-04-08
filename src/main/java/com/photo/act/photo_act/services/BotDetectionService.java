package com.photo.act.photo_act.services;

import org.springframework.stereotype.Service;

/**
 * Detects social-media crawler bots by User-Agent.
 *
 * When a bot visits /article/my-slug, Nginx rewrites the request to
 * /og/article/my-slug (Spring MVC controller, returns Thymeleaf HTML
 * with OG tags). Real users hit Vaadin at the original path.
 *
 * This class is the fallback for cases where Nginx cannot be updated
 * and the detection must happen inside Spring.
 */
@Service
public class BotDetectionService {

    private static final String[] BOT_SIGNATURES = {
        // Facebook / Instagram
        "facebookexternalhit", "facebot",
        // Twitter / X
        "twitterbot",
        // LinkedIn
        "linkedinbot",
        // Pinterest
        "pinterest",
        // Slack
        "slackbot", "slack-imgproxy",
        // Discord
        "discordbot",
        // Telegram
        "telegrambot",
        // WhatsApp
        "whatsapp",
        // Google (Discover, Search preview)
        "googlebot", "google-inspectiontool",
        // Bing
        "bingbot", "bingpreview",
        // Apple iMessage
        "applebot",
        // Reddit
        "redditbot",
        // YouTube / Google crawler
        "mediapartners-google",
        // General crawlers
        "crawl", "spider", "bot"
    };

    /**
     * Returns true if the User-Agent belongs to a social crawler.
     *
     * @param userAgent value of the HTTP User-Agent header
     */
    public boolean isBot(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return false;
        String ua = userAgent.toLowerCase();
        for (String sig : BOT_SIGNATURES) {
            if (ua.contains(sig)) return true;
        }
        return false;
    }
}
