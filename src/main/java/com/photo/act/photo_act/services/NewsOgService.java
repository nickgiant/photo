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

    // unless: RedisCache rejects caching null by default, and @Cacheable's
    // built-in Optional<T> unwrapping stores Optional.empty() as null — so a
    // not-found lookup would throw IllegalArgumentException on every miss
    // without this. Simplest fix: just don't cache misses.
    @Cacheable(value = "og-meta", key = "'NEWS::' + #slug", unless = "#result == null || #result.isEmpty()")
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
        String title       = r.getColumnData("title");
        String description = r.getColumnData("description");
        String picture     = r.getColumnData("picture");
        String tutorName   = r.getColumnData("tutor_name");
        String published   = r.getColumnData("date_insert");

        String canonicalUrl = baseUrl + "/news/" + slug;
        String desc155 = description != null && description.length() > 155
                ? description.substring(0, 154) + "…"
                : (description != null ? description : "");

        String image = (picture != null && !picture.isBlank())
                ? cdnService.ogUrl(picture)
                : defaultOgImage;

        return Optional.of(OgMetaDto.builder()
                .ogTitle(title)
                .ogDescription(desc155)
                .ogUrl(canonicalUrl)
                .ogType("article")
                .ogLocale("en_US")
                .ogImage(image)
                .ogImageWidth(1200)
                .ogImageHeight(630)
                .ogImageAlt(title)
                .articleAuthor(tutorName)
                .articlePublished(published != null ? published + "Z" : null)
                .articleSection("news")
                .twitterCard("summary_large_image")
                .twitterTitle(title)
                .twitterDescription(desc155)
                .twitterImage(image)
                .twitterImageAlt(title)
                .canonicalUrl(canonicalUrl)
                .siteName("PhotoAct")
                .schemaOrgJson(buildSchema(title, canonicalUrl, image, tutorName, published))
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
}
