package com.photo.act.photo_act.services;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

/**
 * Builds social-platform share URLs for a given public resource URL.
 *
 * Platform notes:
 *   Facebook  — reads og:image (1200×630) via its crawler; no image param needed here
 *   Instagram — no web share URL; mobile users share via navigator.share (Web Share API)
 *   Threads   — Meta's text-first platform; supports intent URL like Twitter
 *   Pinterest — requires explicit media (image) URL for Pin creation
 *   LinkedIn  — reads og:image; caches 7 days (use Post Inspector to bust)
 *   Twitter/X — share via intent URL with optional text pre-fill
 *   WhatsApp  — wa.me/?text= with URL embedded in the message
 *   Email     — mailto: with subject + body
 */
@Service
public class ShareService {

    // ── Platform share URLs ───────────────────────────────────────────────────

    /** Facebook — reads og:image automatically via crawler. */
    public String facebook(String url) {
        return "https://www.facebook.com/sharer/sharer.php?u=" + enc(url);
    }

    /**
     * Threads (by Meta) — text-based post with the URL embedded.
     * Image preview is populated from og:image via crawler.
     */
    public String threads(String url, String text) {
        String message = (text != null && !text.isBlank() ? text + " " : "") + url;
        return "https://www.threads.net/intent/post?text=" + enc(message);
    }

    /** Threads — URL only, no pre-filled text. */
    public String threads(String url) {
        return threads(url, null);
    }

    /**
     * Pinterest — Pin creation button.
     * mediaUrl must be an absolute https:// image URL (ideally 1000×1500 for portrait).
     */
    public String pinterest(String url, String mediaUrl, String description) {
        return "https://pinterest.com/pin/create/button/"
                + "?url=" + enc(url)
                + "&media=" + enc(mediaUrl)
                + "&description=" + enc(description);
    }

    /** LinkedIn — reads og:image and og:title automatically. */
    public String linkedIn(String url) {
        return "https://www.linkedin.com/sharing/share-offsite/?url=" + enc(url);
    }

    /**
     * Twitter / X — share with optional pre-filled text.
     * Image preview is populated from og:image or twitter:image via crawler.
     */
    public String twitter(String url, String text) {
        StringBuilder sb = new StringBuilder("https://twitter.com/intent/tweet?url=").append(enc(url));
        if (text != null && !text.isBlank()) {
            sb.append("&text=").append(enc(text));
        }
        return sb.toString();
    }

    /** Twitter/X — URL only. */
    public String twitter(String url) {
        return twitter(url, null);
    }

    /**
     * WhatsApp — opens the app (or web.whatsapp.com) with a pre-filled message.
     * On mobile this opens the WhatsApp app directly.
     */
    public String whatsApp(String url, String text) {
        String message = (text != null && !text.isBlank() ? text + "\n" : "") + url;
        return "https://wa.me/?text=" + enc(message);
    }

    /** WhatsApp — URL only. */
    public String whatsApp(String url) {
        return whatsApp(url, null);
    }

    /**
     * Instagram — no direct web share URL exists.
     * On mobile the Web Share API (navigator.share) can target Instagram.
     * This returns the Instagram homepage as a fallback for desktop users.
     */
    public String instagram(String url) {
        return "https://www.instagram.com/";
    }

    /** Email — mailto: link with subject + body. */
    public String email(String subject, String body) {
        return "mailto:?subject=" + enc(subject) + "&body=" + enc(body);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String enc(String value) {
        return UriUtils.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }
}
