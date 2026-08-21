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
 * Builds OgMetaDto for news / tutorials straight from the learnings / tutor
 * tables — no ORM/repository layer involved, just direct SQL via RecordService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewsOgService {

    private final RecordService recordService;
    private final CdnService cdnService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.og.default-image}")
    private String defaultOgImage;

    private static final String[] COLS = {
            "title", "description", "picture", "tutor_name", "date_insert"
    };

    private static final String SQL =
            "SELECT l.title, l.description, l.picture, t.tutor_name, " +
            "  DATE_FORMAT(l.date_insert, '%Y-%m-%dT%H:%i:%S') AS date_insert " +
            " FROM learnings l LEFT JOIN tutor t ON l.tutor_id = t.id " +
            " WHERE l.slug = ? ";

    // unless: RedisCache rejects caching null by default. @Cacheable unwraps
    // Optional<T> BEFORE evaluating "unless", so #result here is the plain
    // OgMetaDto (or null on a miss) — never an Optional. A not-found lookup
    // (Optional.empty() -> null) would throw IllegalArgumentException on
    // every miss without this guard. (Do NOT add "|| #result.isEmpty()":
    // OgMetaDto has no isEmpty() method, and #result is never an Optional
    // here, so that call throws SpelEvaluationException on every HIT instead.)
    @Cacheable(value = "og-meta", key = "'NEWS::' + #slug", unless = "#result == null")
    public Optional<OgMetaDto> resolve(String slug) {
        log.debug("Cache miss — loading news OG meta for slug={}", slug);

        List<Record> rows = recordService.findAll(
                SQL, COLS,
                new Object[]{slug},
                new String[]{"String"});

        if (rows.isEmpty()) {
            log.warn("No learnings found for slug={}", slug);
            return Optional.empty();
        }

        Record r = rows.get(0);
        String title       = clean(r.getColumnData("title"));
        String description = clean(r.getColumnData("description"));
        String picture     = clean(r.getColumnData("picture"));
        String tutorName   = clean(r.getColumnData("tutor_name"));
        String published   = clean(r.getColumnData("date_insert"));

        String displayTitle = title != null ? title : "News on PhotoAct";
        String canonicalUrl = baseUrl + "/news/" + slug;
        String desc155 = description != null && description.length() > 155
                ? description.substring(0, 154) + "…"
                : (description != null ? description : "");

        String image = picture != null
                ? cdnService.ogUrl(picture)
                : defaultOgImage;

        return Optional.of(OgMetaDto.builder()
                .ogTitle(displayTitle)
                .ogDescription(desc155)
                .ogUrl(canonicalUrl)
                .ogType("article")
                .ogLocale("en_US")
                .ogImage(image)
                .ogImageWidth(1200)
                .ogImageHeight(630)
                .ogImageAlt(displayTitle)
                .articleAuthor(tutorName)
                .articlePublished(published != null ? published + "Z" : null)
                .articleSection("news")
                .twitterCard("summary_large_image")
                .twitterTitle(displayTitle)
                .twitterDescription(desc155)
                .twitterImage(image)
                .twitterImageAlt(displayTitle)
                .canonicalUrl(canonicalUrl)
                .siteName("PhotoAct")
                .schemaOrgJson(buildSchema(displayTitle, canonicalUrl, image, tutorName, published))
                .contentType(OgContentType.NEWS)
                .slug(slug)
                .build());
    }

    private String buildSchema(String title, String url, String image, String author, String published) {
        return """
                {"@context":"https://schema.org","@type":"Article",
                 "headline":"%s","url":"%s","image":"%s",
                 "author":{"@type":"Person","name":"%s"},
                 "datePublished":"%s"}
                """.formatted(
                title != null ? title : "",
                url,
                image,
                author != null ? author : "",
                published != null ? published : "");
    }

    /**
     * RecordService.findAll() builds each Record via "value + \"\"", so a
     * genuine SQL NULL comes back as the literal 4-character string "null"
     * instead of Java null — normalise that (and blank) here so a missing
     * column never leaks into an og:title/description/etc as the word "null".
     * Confirmed live on PhotoOgService for photo_meta.title; same RecordService
     * quirk applies to every column read this way.
     */
    private static String clean(String v) {
        return (v == null || v.isBlank() || v.equals("null")) ? null : v;
    }
}
