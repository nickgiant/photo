package com.photo.act.photo_act.seo;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.model.LearningEntity;
import com.photo.act.photo_act.model.PhotoMetaEntity;
import com.photo.act.photo_act.repository.LearningRepository;
import com.photo.act.photo_act.repository.PhotoMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.photo.act.photo_act.views.MainLayout.baseUrl;

/**
 * Gathers every URL the sitemap should list, one content type at a time.
 * Pure query/aggregation — no XML, no formatting. {@link SitemapXmlWriter} handles output.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SitemapQueryService {

    /** Sitemaps Protocol hard cap on URLs per file: https://www.sitemaps.org/protocol.html */
    private static final int MAX_ENTRIES = 50_000;

    private final RecordService recordService;
    private final LearningRepository learningRepository;
    private final PhotoMetaRepository photoMetaRepository;

    public List<SitemapUrlEntry> collectAllEntries() {
        List<SitemapUrlEntry> entries = new ArrayList<>();
        entries.addAll(staticEntries());
        entries.addAll(safely("stories", this::storyEntries));
        entries.addAll(safely("news", this::newsEntries));
        entries.addAll(safely("photos", this::photoEntries));
        entries.addAll(safely("events", this::eventEntries));
        entries.addAll(safely("photographers", this::photographerEntries));

        if (entries.size() > MAX_ENTRIES) {
            log.warn("Sitemap collected {} URLs, truncating to the Sitemaps Protocol max of {}", entries.size(), MAX_ENTRIES);
            entries = entries.subList(0, MAX_ENTRIES);
        }
        return entries;
    }

    /** One content type failing to query (bad SQL, DB hiccup) must not blank out the whole sitemap. */
    private List<SitemapUrlEntry> safely(String label, Supplier<List<SitemapUrlEntry>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Sitemap: failed to collect '{}' entries, skipping this section", label, e);
            return List.of();
        }
    }

    private List<SitemapUrlEntry> staticEntries() {
        Instant now = Instant.now();
        return List.of(
                new SitemapUrlEntry(baseUrl + "/", now, "weekly", "0.5"),
                new SitemapUrlEntry(baseUrl + "/photos", now, "weekly", "0.7"),
                new SitemapUrlEntry(baseUrl + "/stories", now, "monthly", "0.6"),
                new SitemapUrlEntry(baseUrl + "/news", now, "weekly", "0.7"),
                new SitemapUrlEntry(baseUrl + "/photographers", now, "monthly", "0.6"),
                new SitemapUrlEntry(baseUrl + "/events", now, "monthly", "0.5")
        );
    }

    // ── Stories — photo_stories, no JPA entity, same pattern as StoryOgService ─────

    private static final String[] STORY_COLS = {"slug", "username", "date_inserted"};
    private static final String STORY_SQL =
            "SELECT s.slug, usr.username, " +
            "  DATE_FORMAT(s.date_inserted, '%Y-%m-%dT%H:%i:%S') AS date_inserted " +
            " FROM photo_stories s JOIN dbuser usr ON s.user_id = usr.userId " +
            " WHERE s.slug IS NOT NULL " +
            " ORDER BY s.date_inserted DESC";

    private List<SitemapUrlEntry> storyEntries() {
        List<SitemapUrlEntry> out = new ArrayList<>();
        for (Record r : recordService.findAll(STORY_SQL, STORY_COLS)) {
            String slug = r.getColumnData("slug");
            String username = r.getColumnData("username");
            if (slug.isBlank() || username.isBlank()) continue;

            String loc = baseUrl + "/stories/member/" + encode(username) + "/story/" + encode(slug);
            out.add(new SitemapUrlEntry(loc, parseSqlDatetime(r.getColumnData("date_inserted")), "monthly", "0.8"));
        }
        return out;
    }

    // ── News — LearningEntity backs the /news routes ────────────────────────────

    private List<SitemapUrlEntry> newsEntries() {
        List<SitemapUrlEntry> out = new ArrayList<>();
        List<LearningEntity> learnings =
                learningRepository.findAllByOrderByDateInsertDesc(PageRequest.of(0, MAX_ENTRIES)).getContent();

        for (LearningEntity learning : learnings) {
            String slug = learning.getSlug();
            if (slug == null || slug.isBlank()) continue;

            String loc = baseUrl + "/news/" + slug;
            Instant lastmod = learning.getDateInsert() != null
                    ? learning.getDateInsert().toInstant(ZoneOffset.UTC)
                    : Instant.now();
            out.add(new SitemapUrlEntry(loc, lastmod, "monthly", "0.7"));
        }
        return out;
    }

    // ── Photos — detail route is /photo/{slug-or-id}; slug (destination-based) preferred when set ──

    private List<SitemapUrlEntry> photoEntries() {
        List<SitemapUrlEntry> out = new ArrayList<>();
        List<PhotoMetaEntity> photos =
                photoMetaRepository.findByVisibleToOrderByDateInsertedDesc("ALL", PageRequest.of(0, MAX_ENTRIES)).getContent();

        for (PhotoMetaEntity photo : photos) {
            if (photo.getId() == null) continue;

            String pathSegment = photo.getSlug() != null && !photo.getSlug().isBlank()
                    ? photo.getSlug()
                    : String.valueOf(photo.getId());
            String loc = baseUrl + "/photo/" + pathSegment;
            Instant lastmod = photo.getDateInserted() != null
                    ? photo.getDateInserted().toInstant(ZoneOffset.UTC)
                    : Instant.now();
            out.add(new SitemapUrlEntry(loc, lastmod, "monthly", "0.6"));
        }
        return out;
    }

    // ── Events — festivals/festivals_edition, no JPA entity, no slug column ─────

    private static final String[] EVENT_COLS = {"title", "date_insert"};
    private static final String EVENT_SQL =
            "SELECT e.title, " +
            "  DATE_FORMAT(f.dateInsert, '%Y-%m-%dT%H:%i:%S') AS date_insert " +
            " FROM festivals f JOIN festivals_edition e ON f.id = e.festival_id " +
            " WHERE e.title IS NOT NULL " +
            " ORDER BY f.dateInsert DESC";

    private List<SitemapUrlEntry> eventEntries() {
        List<SitemapUrlEntry> out = new ArrayList<>();
        for (Record r : recordService.findAll(EVENT_SQL, EVENT_COLS)) {
            String title = r.getColumnData("title");
            if (title.isBlank()) continue;

            String loc = baseUrl + "/events/title/" + encode(title);
            out.add(new SitemapUrlEntry(loc, parseSqlDatetime(r.getColumnData("date_insert")), "monthly", "0.5"));
        }
        return out;
    }

    // ── Photographers — dbuser/dbuser_rights, same "real member" filter as PhotographersView ─

    private static final String[] PHOTOGRAPHER_COLS = {"username", "date_joined"};
    private static final String PHOTOGRAPHER_SQL =
            "SELECT usr.username, " +
            "  DATE_FORMAT(usr.date_joined, '%Y-%m-%dT%H:%i:%S') AS date_joined " +
            " FROM dbuser usr, dbuser_rights usrr " +
            " WHERE usrr.id = usr.user_rights_id AND usrr.role <> 'Guest' " +
            " ORDER BY usr.username";

    private List<SitemapUrlEntry> photographerEntries() {
        List<SitemapUrlEntry> out = new ArrayList<>();
        for (Record r : recordService.findAll(PHOTOGRAPHER_SQL, PHOTOGRAPHER_COLS)) {
            String username = r.getColumnData("username");
            if (username.isBlank()) continue;

            String loc = baseUrl + "/photographer/" + encode(username);
            out.add(new SitemapUrlEntry(loc, parseSqlDatetime(r.getColumnData("date_joined")), "monthly", "0.4"));
        }
        return out;
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    /** Route params are consumed raw/percent-encoded elsewhere (see UtilsString#decodeRouteParam) — this is the encode side. */
    private String encode(String value) {
        return UriUtils.encodePathSegment(value, StandardCharsets.UTF_8);
    }

    private Instant parseSqlDatetime(String value) {
        if (value == null || value.isBlank()) return Instant.now();
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
