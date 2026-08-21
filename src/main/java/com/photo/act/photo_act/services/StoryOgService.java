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
 * Builds OgMetaDto for photo stories straight from the photo_stories table —
 * no ORM/repository layer involved, just direct SQL via RecordService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoryOgService {

    private final RecordService recordService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.og.default-image}")
    private String defaultOgImage;

    private static final String[] COLS = {
            "title", "slug", "description", "date_inserted", "author_name", "username"
    };

    private static final String SQL =
            "SELECT s.title, s.slug, s.description, " +
            "  DATE_FORMAT(s.date_inserted, '%Y-%m-%dT%H:%i:%S') AS date_inserted, " +
            "  CONCAT(usr.name, ' ', usr.surname) AS author_name, " +
            "  usr.username " +
            " FROM photo_stories s JOIN dbuser usr ON s.user_id = usr.userId " +
            " WHERE s.slug = ? ";

    // unless: RedisCache rejects caching null by default. @Cacheable unwraps
    // Optional<T> BEFORE evaluating "unless", so #result here is the plain
    // OgMetaDto (or null on a miss) — never an Optional. A not-found lookup
    // (Optional.empty() -> null) would throw IllegalArgumentException on
    // every miss without this guard. (Do NOT add "|| #result.isEmpty()":
    // OgMetaDto has no isEmpty() method, and #result is never an Optional
    // here, so that call throws SpelEvaluationException on every HIT instead —
    // i.e. every time real content is actually found.)
    @Cacheable(value = "og-meta", key = "'STORY::' + #slug", unless = "#result == null")
    public Optional<OgMetaDto> resolve(String slug) {
        log.debug("Cache miss — loading story OG meta for slug={}", slug);

        List<Record> rows = recordService.findAll(
                SQL, COLS,
                new Object[]{slug},
                new String[]{"String"});

        if (rows.isEmpty()) {
            log.warn("No photo_story found for slug={}", slug);
            return Optional.empty();
        }

        Record r = rows.get(0);
        String title       = r.getColumnData("title");
        String description = r.getColumnData("description");
        String author      = r.getColumnData("author_name");
        String username    = r.getColumnData("username");
        String published   = r.getColumnData("date_inserted");

        String canonicalUrl = baseUrl + "/stories/member/" + username + "/story/" + slug;
        String desc155 = description != null && description.length() > 155
                ? description.substring(0, 154) + "…"
                : (description != null ? description : "");

        return Optional.of(OgMetaDto.builder()
                .ogTitle(title)
                .ogDescription(desc155)
                .ogUrl(canonicalUrl)
                .ogType("article")
                .ogLocale("en_US")
                .ogImage(defaultOgImage)
                .ogImageWidth(1200)
                .ogImageHeight(630)
                .ogImageAlt(title)
                .articleAuthor(author)
                .articlePublished(published != null ? published + "Z" : null)
                .articleSection("story")
                .twitterCard("summary_large_image")
                .twitterTitle(title)
                .twitterDescription(desc155)
                .twitterImage(defaultOgImage)
                .twitterImageAlt(title)
                .canonicalUrl(canonicalUrl)
                .siteName("PhotoAct")
                .schemaOrgJson(buildSchema(title, canonicalUrl, author, published))
                .contentType(OgContentType.STORY)
                .slug(slug)
                .build());
    }

    private String buildSchema(String title, String url, String author, String published) {
        return """
                {"@context":"https://schema.org","@type":"Article",
                 "headline":"%s","url":"%s","image":"%s",
                 "author":{"@type":"Person","name":"%s"},
                 "datePublished":"%s"}
                """.formatted(
                title != null ? title : "",
                url,
                defaultOgImage,
                author != null ? author : "",
                published != null ? published : "");
    }
}
