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
 * Builds OgMetaDto for photos straight from the photo_meta / destination
 * tables — no ORM/repository layer involved, just direct SQL via RecordService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoOgService {

    private final RecordService recordService;
    private final CdnService cdnService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.og.default-image}")
    private String defaultOgImage;

    private static final String[] COLS = {
            "title", "subtitle", "notes", "name_new", "uploader",
            "date_inserted", "city_name", "country"
    };

    private static final String SQL =
            "SELECT pm.title, pm.subtitle, pm.notes, pm.name_new, pm.uploader, " +
            "  DATE_FORMAT(pm.date_inserted, '%Y-%m-%dT%H:%i:%S') AS date_inserted, " +
            "  d.city_name, d.country " +
            " FROM photo_meta pm LEFT JOIN destination d ON pm.destination_id = d.id " +
            " WHERE pm.slug = ? AND pm.visible_to = 'ALL' ";

    // unless: RedisCache rejects caching null by default. @Cacheable unwraps
    // Optional<T> BEFORE evaluating "unless", so #result here is the plain
    // OgMetaDto (or null on a miss) — never an Optional. A not-found lookup
    // (Optional.empty() -> null) would throw IllegalArgumentException on
    // every miss without this guard. (Do NOT add "|| #result.isEmpty()":
    // OgMetaDto has no isEmpty() method, and #result is never an Optional
    // here, so that call throws SpelEvaluationException on every HIT instead.)
    @Cacheable(value = "og-meta", key = "'PHOTO::' + #slug", unless = "#result == null")
    public Optional<OgMetaDto> resolve(String slug) {
        log.debug("Cache miss — loading photo OG meta for slug={}", slug);

        List<Record> rows = recordService.findAll(
                SQL, COLS,
                new Object[]{slug},
                new String[]{"String"});

        if (rows.isEmpty()) {
            log.warn("No photo_meta found for slug={}", slug);
            return Optional.empty();
        }

        Record r = rows.get(0);
        String title        = clean(r.getColumnData("title"));
        String subtitle     = clean(r.getColumnData("subtitle"));
        String notes        = clean(r.getColumnData("notes"));
        String nameNew      = clean(r.getColumnData("name_new"));
        String uploader     = clean(r.getColumnData("uploader"));
        String published    = clean(r.getColumnData("date_inserted"));
        String cityName     = clean(r.getColumnData("city_name"));
        String country      = clean(r.getColumnData("country"));

        String location = cityName != null
                ? cityName + (country != null ? ", " + country : "")
                : null;
        String displayTitle = title != null ? title
                : subtitle != null ? subtitle
                : location != null ? "Photo in " + location
                : "Photo on PhotoAct";
        String description = notes != null
                ? notes
                : (location != null ? "Photo taken in " + location : "");
        String desc155 = description.length() > 155
                ? description.substring(0, 154) + "…"
                : description;

        String image = nameNew != null
                ? cdnService.ogUrl(nameNew)
                : defaultOgImage;

        // /photo/{slug} is the real, shareable route — PhotoLightboxView
        // resolves it (and a legacy bare-id form) by extracting the trailing
        // digit run. og:url must match what's actually shared/visited:
        // confirmed live via a link-preview debugger that flagged og:url
        // ("/photo/{id}") not matching the input URL ("/photo/{slug}").
        String canonicalUrl = baseUrl + "/photo/" + slug;

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
                .articleAuthor(uploader)
                .articlePublished(published != null ? published + "Z" : null)
                .articleSection("photo")
                .twitterCard("summary_large_image")
                .twitterTitle(displayTitle)
                .twitterDescription(desc155)
                .twitterImage(image)
                .twitterImageAlt(displayTitle)
                .canonicalUrl(canonicalUrl)
                .siteName("PhotoAct")
                .schemaOrgJson(buildSchema(displayTitle, canonicalUrl, image, uploader, published))
                .contentType(OgContentType.PHOTO)
                .slug(slug)
                .build());
    }

    private String buildSchema(String title, String url, String image, String author, String published) {
        return """
                {"@context":"https://schema.org","@type":"Photograph",
                 "name":"%s","url":"%s","image":"%s",
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
     * Confirmed live: photo_meta.title NULL rendered og:title as "null".
     */
    private static String clean(String v) {
        return (v == null || v.isBlank() || v.equals("null")) ? null : v;
    }
}
