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
 * Builds OgMetaDto for festivals/events straight from the festivals /
 * destination tables — no ORM/repository layer involved, just direct SQL
 * via RecordService.
 *
 * festivals has no slug column, so the {slug} path segment is the numeric
 * festivals.id — same convention the front-end would need for a deep link.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventOgService {

    private final RecordService recordService;
    private final CdnService cdnService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.og.default-image}")
    private String defaultOgImage;

    private static final String[] COLS = {
            "name_short", "name_full", "activities", "image_top", "period_of_year",
            "city_name", "country", "date_insert"
    };

    private static final String SQL =
            "SELECT f.name_short, f.name_full, f.activities, f.image_top, f.periodOfYear AS period_of_year, " +
            "  d.city_name, d.country, " +
            "  DATE_FORMAT(f.dateInsert, '%Y-%m-%dT%H:%i:%S') AS date_insert " +
            " FROM festivals f LEFT JOIN destination d ON f.destination_id = d.id " +
            " WHERE f.id = ? ";

    // unless: RedisCache rejects caching null by default, and @Cacheable's
    // built-in Optional<T> unwrapping stores Optional.empty() as null — so a
    // not-found lookup would throw IllegalArgumentException on every miss
    // without this. Simplest fix: just don't cache misses.
    @Cacheable(value = "og-meta", key = "'EVENT::' + #slug", unless = "#result == null || #result.isEmpty()")
    public Optional<OgMetaDto> resolve(String slug) {
        log.debug("Cache miss — loading event OG meta for id={}", slug);

        Long id;
        try {
            id = Long.parseLong(slug);
        } catch (NumberFormatException e) {
            log.warn("Non-numeric event id in OG lookup: {}", slug);
            return Optional.empty();
        }

        List<Record> rows = recordService.findAll(
                SQL, COLS,
                new Object[]{id},
                new String[]{"Long"});

        if (rows.isEmpty()) {
            log.warn("No festival found for id={}", id);
            return Optional.empty();
        }

        Record r = rows.get(0);
        String nameShort   = r.getColumnData("name_short");
        String nameFull    = r.getColumnData("name_full");
        String activities  = r.getColumnData("activities");
        String imageTop    = r.getColumnData("image_top");
        String periodOfYear = r.getColumnData("period_of_year");
        String cityName    = r.getColumnData("city_name");
        String country     = r.getColumnData("country");
        String published   = r.getColumnData("date_insert");

        String title = (nameFull != null && !nameFull.isBlank()) ? nameFull : nameShort;
        String location = (cityName != null && !cityName.isBlank())
                ? cityName + (country != null && !country.isBlank() ? ", " + country : "")
                : null;
        String description = (activities != null && !activities.isBlank())
                ? activities
                : ((location != null ? title + " in " + location : title)
                    + (periodOfYear != null && !periodOfYear.isBlank() ? " — " + periodOfYear : ""));
        String desc155 = description.length() > 155
                ? description.substring(0, 154) + "…"
                : description;

        String image = (imageTop != null && !imageTop.isBlank())
                ? cdnService.ogUrl(imageTop)
                : defaultOgImage;

        String canonicalUrl = baseUrl + "/events/" + id;

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
                .articlePublished(published != null ? published + "Z" : null)
                .articleSection("event")
                .twitterCard("summary_large_image")
                .twitterTitle(title)
                .twitterDescription(desc155)
                .twitterImage(image)
                .twitterImageAlt(title)
                .canonicalUrl(canonicalUrl)
                .siteName("PhotoAct")
                .schemaOrgJson(buildSchema(title, canonicalUrl, image, location, published))
                .contentType(OgContentType.EVENT)
                .slug(slug)
                .build());
    }

    private String buildSchema(String title, String url, String image, String location, String published) {
        return """
                {"@context":"https://schema.org","@type":"Event",
                 "name":"%s","url":"%s","image":"%s",
                 "location":{"@type":"Place","name":"%s"},
                 "startDate":"%s"}
                """.formatted(
                title != null ? title : "",
                url,
                image,
                location != null ? location : "",
                published != null ? published : "");
    }
}
