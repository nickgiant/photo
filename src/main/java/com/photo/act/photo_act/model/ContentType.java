package com.photo.act.photo_act.model;

/**
 * Mirrors the content_type discriminator column in MariaDB.
 * Maps to og:type and twitter:card values per platform.
 */
public enum ContentType {

    ARTICLE,      // og:type=article  — news / story / learning
    PHOTO,        // og:type=article  — single photographer image
    ALBUM,        // og:type=website  — photo gallery
    LEARNING,     // og:type=article  — tutorial / course
    EVENT,        // og:type=event    (custom schema.org)
    STORY,        // og:type=article
    NEWS,         // og:type=article
    PHOTOGRAPHER; // og:type=profile

    /** Maps to standard Open Graph types */
    public String toOgType() {
        return switch (this) {
            case PHOTOGRAPHER -> "profile";
            case ALBUM        -> "website";
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
