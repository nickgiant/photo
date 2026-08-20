package com.photo.act.photo_act.seo;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.model.LearningEntity;
import com.photo.act.photo_act.repository.LearningRepository;
import com.photo.act.photo_act.utils.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Backfills missing slug values for News and Photos.
 * Events and Photographers don't have a slug column; their URLs are title/username-based.
 *
 * Slugs are generated once and never regenerated ({slugified-source}-{id}); the trailing
 * id guarantees uniqueness without needing a DB-level unique constraint. Going forward,
 * News gets its slug on create/update (see LearningService), and Photos get theirs whenever
 * a destination is (re)assigned (see UploadImageCard / GalleryImageViewCard).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlugMaintenanceService {

    /** Rows fetched/updated per round-trip, so a large backlog doesn't run as one giant query/transaction. */
    private static final int PHOTO_BATCH_SIZE = 200;

    private final LearningRepository learningRepository;
    private final RecordService recordService;

    public record BackfillResult(int newsUpdated, int photosUpdated) {
    }

    public BackfillResult backfillAllMissingSlugs() {
        int news = backfillNewsSlugs();
        int photos = backfillPhotoSlugs();
        log.info("Slug backfill complete — news: {}, photos: {}", news, photos);
        return new BackfillResult(news, photos);
    }

    private int backfillNewsSlugs() {
        List<LearningEntity> missing = learningRepository.findAll().stream()
                .filter(l -> l.getSlug() == null || l.getSlug().isBlank())
                .toList();

        for (LearningEntity learning : missing) {
            learning.setSlug(SlugUtil.toSlug(learning.getTitle()) + "-" + learning.getId());
        }
        // JpaRepository.saveAll() is transactional on its own — no need to wrap this method too.
        learningRepository.saveAll(missing);
        return missing.size();
    }

    private static final String[] PHOTO_COLS = {"id", "city_name", "country", "subtitle"};
    private static final String PHOTO_SQL =
            "SELECT pm.id, d.city_name, d.country, pm.subtitle " +
            " FROM photo_meta pm JOIN destination d ON pm.destination_id = d.id " +
            " WHERE pm.destination_id IS NOT NULL AND (pm.slug IS NULL OR pm.slug = '') " +
            " ORDER BY pm.id " +
            " LIMIT " + PHOTO_BATCH_SIZE;

    private static final String[] PHOTO_NO_DEST_COLS = {"id"};
    private static final String PHOTO_NO_DEST_SQL =
            "SELECT pm.id " +
            " FROM photo_meta pm " +
            " WHERE pm.destination_id IS NULL " +
            "   AND (pm.slug IS NULL OR pm.slug = '') " +
            " ORDER BY pm.id " +
            " LIMIT " + PHOTO_BATCH_SIZE;

    /**
     * Photos with a destination get a destination(+description)-based slug; photos with neither
     * a destination nor a description fall back to a fixed "010"-prefixed id (see PhotoMetaEntity.slug).
     * Photos with a description but no destination are left alone — not currently generated anywhere.
     * Each case runs one bounded batch at a time — its own round-trip/commit — until a batch comes
     * back smaller than the page size, so a large backlog is spread over several small updates
     * instead of one big query + transaction.
     */
    private int backfillPhotoSlugs() {
        int totalUpdated = 0;
        int batchUpdated;
        do {
            batchUpdated = backfillPhotoSlugsBatch();
            totalUpdated += batchUpdated;
        } while (batchUpdated == PHOTO_BATCH_SIZE);

        do {
            batchUpdated = backfillPhotoSlugsNoDestinationBatch();
            totalUpdated += batchUpdated;
        } while (batchUpdated == PHOTO_BATCH_SIZE);

        return totalUpdated;
    }

    private int backfillPhotoSlugsBatch() {
        List<Record> rows = recordService.findAll(PHOTO_SQL, PHOTO_COLS);
        for (Record r : rows) {
            String id = r.getColumnData("id");
            String display = r.getColumnData("city_name") + " (" + r.getColumnData("country") + ")";
            String subtitle = r.getColumnData("subtitle");
            String slugBase = subtitle != null && !subtitle.isBlank() ? display + " " + subtitle : display;
            String slug = SlugUtil.toSlug(slugBase) + "-" + id;

            recordService.insertOneRecordWithQuery(
                    "UPDATE photo_meta SET slug = ? WHERE id = ?",
                    new Object[]{slug, Integer.parseInt(id)},
                    new String[]{"java.lang.String", "java.lang.Integer"});
        }
        return rows.size();
    }

    private int backfillPhotoSlugsNoDestinationBatch() {
        List<Record> rows = recordService.findAll(PHOTO_NO_DEST_SQL, PHOTO_NO_DEST_COLS);
        for (Record r : rows) {
            String id = r.getColumnData("id");
            // Hyphenated (not "010" + id run together) — PhotoLightboxView resolves a photo
            // route by extracting the trailing digit run, which only works when something
            // non-numeric (here, the hyphen) marks where the id actually starts.
            String slug = "010-" + id;

            recordService.insertOneRecordWithQuery(
                    "UPDATE photo_meta SET slug = ? WHERE id = ?",
                    new Object[]{slug, Integer.parseInt(id)},
                    new String[]{"java.lang.String", "java.lang.Integer"});
        }
        return rows.size();
    }
}
