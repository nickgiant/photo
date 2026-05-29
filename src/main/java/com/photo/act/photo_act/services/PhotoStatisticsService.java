package com.photo.act.photo_act.services;

import org.springframework.stereotype.Service;

@Service
public class PhotoStatisticsService {

    public static final String[] STATS_COLUMNS = {
            "id", "name_new", "title", "subtitle", "notes", "photo_type", "uploader", "uploaderId", "creator", "visible_to",
            "meta_date", "photo_date", "photo_time", "photo_time_shot",
            "space_size", "space_size_medium", "space_size_thumb",
            "meta_camera_make", "meta_camera_model", "meta_lens_make", "meta_lens_model",
            "meta_focal_length", "meta_focal_length_ff", "meta_iso", "meta_aperture", "meta_shutter_speed",
            "meta_orientation", "meta_i_height", "meta_i_length", "meta_i_width",
            "location_by_user", "location_area", "location_country_code", "location_lat", "location_lon",
            "city_name",
            "subject_name", "subject_description", "subject_type",
            "date_inserted_diff_from_now",
            "username", "surname", "name", "resident", "resident_country",
            "date_joined", "member_since", "avatar_path", "short_bio",
            "count_photos", "count_stories"
    };

    private static final String STATS_SELECT =
            " SELECT pm.id, pm.name_new, pm.title, pm.subtitle, pm.notes, pm.photo_type, pm.uploader, pm.uploaderId, pm.creator, pm.visible_to," +
            " DATE_FORMAT(pm.meta_date, '%W %D %M %Y %H:%i %p') AS meta_date," +
            " DATE_FORMAT(pm.meta_date, '%M %Y') AS photo_date," +
            " DATE_FORMAT(pm.meta_date, '%H:%i') AS photo_time," +
            " DATE_FORMAT(pm.meta_date, '%d/%m/%Y - %H:%i:%S') AS photo_time_shot," +
            " pm.space_size, pm.space_size_medium, pm.space_size_thumb," +
            " pm.meta_camera_make, pm.meta_camera_model, pm.meta_lens_make, pm.meta_lens_model," +
            " pm.meta_focal_length, pm.meta_focal_length_ff, pm.meta_iso, pm.meta_aperture, pm.meta_shutter_speed," +
            " pm.meta_orientation, pm.meta_i_height, pm.meta_i_length, pm.meta_i_width," +
            " pm.location_by_user, pm.location_area, pm.location_country_code, pm.location_lat, pm.location_lon," +
            " getDateDiffFromNow(pm.date_inserted) AS date_inserted_diff_from_now," +
            " d.city_name," +
            " s.subject_name, s.subject_description, s.subject_type," +
            " usr.username, usr.surname, usr.name, usr.resident, usr.resident_country," +
            " DATE_FORMAT(usr.date_joined, '%d-%m-%Y') AS date_joined," +
            " DATE_FORMAT(usr.date_joined, '%M %Y') AS member_since," +
            " usr.avatar_path, usr.short_bio," +
            " ux.count_photos, ux.count_stories";

    private static final String STATS_FROM =
            " FROM dbuser usr" +
            " JOIN dbuser_extra ux ON usr.userId = ux.user_id" +
            " JOIN photo_meta pm ON pm.uploaderId = usr.userId" +
            " LEFT JOIN destination d ON pm.destination_id = d.id" +
            " LEFT JOIN subject s ON pm.subject_id = s.id";

    private static final String STATS_WHERE =
            " WHERE pm.visible_to = 'ALL'";

    public String getMostViewedSql(int limit) {
        return STATS_SELECT + STATS_FROM +
                " LEFT JOIN (SELECT photo_id, COUNT(*) AS view_count FROM photo_view" +
                " WHERE view_type IN ('List', 'Full') GROUP BY photo_id) pv ON pm.id = pv.photo_id" +
                STATS_WHERE +
                " ORDER BY COALESCE(pv.view_count, 0) DESC LIMIT " + limit;
    }

    public String getMostLikedSql(int limit) {
        return STATS_SELECT + STATS_FROM +
                " LEFT JOIN (SELECT photo_id, COUNT(DISTINCT ip_address) AS like_count FROM photo_view" +
                " WHERE view_type = 'Like' GROUP BY photo_id) pl ON pm.id = pl.photo_id" +
                STATS_WHERE +
                " ORDER BY COALESCE(pl.like_count, 0) DESC LIMIT " + limit;
    }

    public String getMostRecentSql(int limit) {
        return STATS_SELECT + STATS_FROM +
                STATS_WHERE +
                " ORDER BY pm.date_inserted DESC LIMIT " + limit;
    }

    public String getBestRatingSql(int limit) {
        return STATS_SELECT + STATS_FROM +
                " LEFT JOIN (SELECT photo_id, AVG(rating) AS avg_rating, COUNT(*) AS rating_count" +
                " FROM photo_rating GROUP BY photo_id) pr ON pm.id = pr.photo_id" +
                STATS_WHERE +
                " ORDER BY COALESCE(pr.avg_rating, 0) DESC, COALESCE(pr.rating_count, 0) DESC LIMIT " + limit;
    }
}
