package com.photo.act.photo_act.model;

/**
 * Discriminator for the /og/{type}/{slug} route — each value is resolved by
 * its own dedicated *OgService, running direct SQL against the real table
 * that backs that content (no shared "content" table involved):
 *
 *   STORY        → StoryOgService        → photo_stories
 *   PHOTO        → PhotoOgService        → photo_meta / destination
 *   NEWS         → NewsOgService         → learnings / tutor
 *   EVENT        → EventOgService        → festivals / destination
 *   PHOTOGRAPHER → PhotographerOgService → dbuser / dbuser_extra
 */
public enum OgContentType {

    STORY,        // og:type=article
    PHOTO,        // og:type=article
    NEWS,         // og:type=article
    EVENT,        // og:type=article  (custom schema.org Event)
    PHOTOGRAPHER; // og:type=profile

    /** Maps to standard Open Graph types */
    public String toOgType() {
        return switch (this) {
            case PHOTOGRAPHER -> "profile";
            default           -> "article";
        };
    }

    /**
     * Twitter card type.
     * summary_large_image  → wide 1200×630 image (most content)
     * summary              → square 1:1 thumb  (profile / small item)
     */
    public String toTwitterCard() {
        return switch (this) {
            case PHOTOGRAPHER -> "summary";
            default           -> "summary_large_image";
        };
    }
}
