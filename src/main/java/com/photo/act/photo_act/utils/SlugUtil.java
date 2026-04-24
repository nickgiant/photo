package com.photo.act.photo_act.utils;

import java.text.Normalizer;
import java.util.Locale;

public class SlugUtil {

    public static String toSlug(String input) {
        if (input == null) {
            return "";
        }

        // Normalize (remove accents)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Convert to lowercase
        String lower = normalized.toLowerCase(Locale.ENGLISH);

        // Replace non-alphanumeric characters with hyphens
        String slug = lower.replaceAll("[^a-z0-9]+", "-");

        // Trim leading and trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");

        return slug;
    }
}