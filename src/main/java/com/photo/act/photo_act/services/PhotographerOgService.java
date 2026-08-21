package com.photo.act.photo_act.services;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.model.OgContentType;
import com.photo.act.photo_act.model.OgMetaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Builds OgMetaDto for photographer profiles straight from the dbuser /
 * dbuser_extra tables — no ORM/repository layer involved, just direct SQL
 * via RecordService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhotographerOgService {

    private final RecordService recordService;
    private final CdnService cdnService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.og.default-image}")
    private String defaultOgImage;

    private static final String[] COLS = {
            "username", "name", "surname", "short_bio", "avatar_path",
            "resident_country", "count_photos", "count_stories"
    };

    private static final String SQL =
            "SELECT usr.username, usr.name, usr.surname, usr.short_bio, usr.avatar_path, usr.resident_country, " +
            "  ux.count_photos, ux.count_stories " +
            " FROM dbuser usr LEFT JOIN dbuser_extra ux ON usr.userId = ux.user_id " +
            " WHERE usr.username = ? ";

    // unless: RedisCache rejects caching null by default, and @Cacheable's
    // built-in Optional<T> unwrapping stores Optional.empty() as null — so a
    // not-found lookup would throw IllegalArgumentException on every miss
    // without this. Simplest fix: just don't cache misses.
    @Cacheable(value = "og-meta", key = "'PHOTOGRAPHER::' + #slug", unless = "#result == null || #result.isEmpty()")
    public Optional<OgMetaDto> resolve(String slug) {
        log.debug("Cache miss — loading photographer OG meta for username={}", slug);

        List<Record> rows = recordService.findAll(
                SQL, COLS,
                new Object[]{slug},
                new String[]{"String"});

        if (rows.isEmpty()) {
            log.warn("No dbuser found for username={}", slug);
            return Optional.empty();
        }

        Record r = rows.get(0);
        String username        = r.getColumnData("username");
        String name             = r.getColumnData("name");
        String surname          = r.getColumnData("surname");
        String shortBio         = r.getColumnData("short_bio");
        String avatarPath       = r.getColumnData("avatar_path");
        String residentCountry  = r.getColumnData("resident_country");
        String countPhotos      = r.getColumnData("count_photos");
        String countStories     = r.getColumnData("count_stories");

        String fullName = (name != null ? name : "") + (surname != null && !surname.isBlank() ? " " + surname : "");
        fullName = fullName.isBlank() ? username : fullName.trim();

        String description = (shortBio != null && !shortBio.isBlank())
                ? shortBio
                : "Photographer" + (residentCountry != null && !residentCountry.isBlank() ? " from " + residentCountry : "")
                    + " — " + countPhotos + " photos, " + countStories + " stories on PhotoAct.";
        String desc155 = description.length() > 155
                ? description.substring(0, 154) + "…"
                : description;

        String image = (avatarPath != null && !avatarPath.isBlank())
                ? cdnService.ogUrl(avatarPath)
                : defaultOgImage;

        String canonicalUrl = baseUrl + "/photographer/" + username;

        return Optional.of(OgMetaDto.builder()
                .ogTitle(fullName)
                .ogDescription(desc155)
                .ogUrl(canonicalUrl)
                .ogType("profile")
                .ogLocale("en_US")
                .ogImage(image)
                .ogImageWidth(400)
                .ogImageHeight(400)
                .ogImageAlt(fullName)
                .articleAuthor(fullName)
                .twitterCard("summary")
                .twitterTitle(fullName)
                .twitterDescription(desc155)
                .twitterImage(image)
                .twitterImageAlt(fullName)
                .canonicalUrl(canonicalUrl)
                .siteName("PhotoAct")
                .schemaOrgJson(buildSchema(fullName, canonicalUrl, image))
                .contentType(OgContentType.PHOTOGRAPHER)
                .slug(slug)
                .build());
    }

    private String buildSchema(String name, String url, String image) {
        return """
                {"@context":"https://schema.org","@type":"Person",
                 "name":"%s","url":"%s","image":"%s"}
                """.formatted(
                name != null ? name : "",
                url,
                image);
    }
}
