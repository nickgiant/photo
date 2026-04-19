package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.photo.act.photo_act.views.HomeView.*;
import static com.photo.act.photo_act.views.MainLayout.PROP_PHOTOS;

/**
 * Compact photo upload widget for news / news-item cover photos.
 * Compresses the uploaded JPEG to thumb / small / medium sizes,
 * inserts a record into photo_meta with visible_to = 'photo-news',
 * then calls onPhotoSet with the new photo_meta.id.
 */
public class NewsPhotoUpload extends Div {

    private static final Logger log = LoggerFactory.getLogger(NewsPhotoUpload.class);

    private final RecordService recordService;
    private final int           userId;
    private final String        username;
    private final String        hostname;
    private final String        publicIp;
    private final String        sessionId;
    private final Consumer<Integer> onPhotoSet;

    private final Div previewBox = new Div();
    private final Span statusLabel = new Span();

    private Integer currentPhotoId;

    public NewsPhotoUpload(RecordService recordService,
                           int userId, String username,
                           String hostname, String publicIp, String sessionId,
                           Integer initialPhotoId,
                           Consumer<Integer> onPhotoSet) {
        this.recordService = recordService;
        this.userId        = userId;
        this.username      = username;
        this.hostname      = hostname;
        this.publicIp      = publicIp;
        this.sessionId     = sessionId;
        this.onPhotoSet    = onPhotoSet;
        this.currentPhotoId = initialPhotoId;

        addClassName("npu-wrap");
        buildUi(initialPhotoId);
    }

    private void buildUi(Integer initialPhotoId) {
        removeAll();

        // Preview
        previewBox.addClassName("npu-preview");
        if (initialPhotoId != null) showPreview(initialPhotoId);

        // Upload component
        UploadHandler handler = UploadHandler.toTempFile((meta, tempFile) ->
                getUI().ifPresent(ui -> ui.access(() -> processUpload(meta.getFileName(), tempFile))));

        Upload upload = new Upload(handler);
        upload.setMaxFiles(1);
        upload.setMaxFileSize(12 * 1024 * 1024);
        upload.setAcceptedFileTypes("image/jpeg");
        upload.addClassName("npu-upload");

        // Clear button
        Button clear = new Button("Remove photo");
        clear.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        clear.addClassName("npu-clear");
        clear.setVisible(initialPhotoId != null);
        clear.addClickListener(e -> {
            currentPhotoId = null;
            onPhotoSet.accept(null);
            previewBox.removeAll();
            statusLabel.setText("");
            clear.setVisible(false);
        });

        statusLabel.addClassName("npu-status");

        add(previewBox, upload, statusLabel, clear);
    }

    private void processUpload(String originalName, File tempFile) {
        try {
            String dirPhotos = new GenericView(recordService).getAppProps(PROP_PHOTOS);
            String sep       = FileSystems.getDefault().getSeparator();
            String newName   = UUID.randomUUID() + ".jpg";

            // Copy to show directory
            File showDir  = new File(dirPhotos + sep + subPathShow);
            File showFile = new File(showDir, newName);
            FileUtils.copyFile(tempFile, showFile);

            // Compress to 3 sizes
            compress(showFile, new File(dirPhotos + sep + subPathThumbs + sep + newName), 140, 0.6);
            compress(showFile, new File(dirPhotos + sep + subPathSmall  + sep + newName), 660, 0.7);
            compress(showFile, new File(dirPhotos + sep + subPathMedium + sep + newName), 1040, 0.75);

            Integer photoId = insertPhotoMeta(originalName, newName, showFile.length());
            if (photoId == null) throw new RuntimeException("DB insert failed");

            currentPhotoId = photoId;
            onPhotoSet.accept(photoId);

            // Show preview
            File mediumFile = new File(dirPhotos + sep + subPathMedium + sep + newName);
            Image img = new Image();
            img.setSrc(DownloadHandler.forFile(mediumFile));
            img.addClassName("npu-preview-img");
            previewBox.removeAll();
            previewBox.add(img);
            statusLabel.setText("Photo saved (ID " + photoId + ")");

            // Make clear button visible
            getChildren()
                .filter(c -> c instanceof Button)
                .map(c -> (Button) c)
                .forEach(b -> b.setVisible(true));

        } catch (Exception e) {
            log.error("NewsPhotoUpload error: {}", e.getMessage(), e);
            showError("Upload failed: " + e.getMessage());
        }
    }

    private void compress(File src, File dest, int size, double quality) throws Exception {
        Thumbnails.of(src).size(size, size).useExifOrientation(true).outputQuality(quality).toFile(dest);
    }

    private Integer insertPhotoMeta(String orgName, String newName, long fileSize) {
        try {
            recordService.setGlobalInfo(hostname, userId, username, publicIp, sessionId);
            String sql = "INSERT INTO photo_meta SET id = 0, date_fromapp = now()" +
                    ", uploaderId = " + userId +
                    ", uploader = '" + username + "'" +
                    ", name_org = '" + orgName.replace("'", "''") + "'" +
                    ", name_new = '" + newName + "'" +
                    ", hostname = '" + hostname + "'" +
                    ", visible_to = 'photo-news'" +
                    ", space_size = '" + fileSize + "'" +
                    ", space_size_large = '0', space_size_medium = '0'" +
                    ", space_size_small = '0', space_size_thumb = '0'" +
                    ", meta_all = '', meta_date = now()" +
                    ", meta_camera_make = '', meta_camera_model = ''" +
                    ", meta_lens_make = '', meta_lens_model = ''" +
                    ", meta_focal_length = '0', meta_focal_length_ff = '0'" +
                    ", meta_iso = '0', meta_shutter_speed = '0'" +
                    ", meta_aperture = '0', meta_metering_mode = ''" +
                    ", meta_i_height = '0', meta_i_length = '0', meta_i_width = '0'" +
                    ", meta_orientation = ''";
            int result = recordService.insertOneRecordWithQuery(sql, null, null);
            if (result != 1) return null;

            List<Record> rows = recordService.findAll(
                    "SELECT id FROM photo_meta WHERE name_new = '" + newName + "'",
                    new String[]{"id"});
            if (rows.isEmpty()) return null;
            String idStr = rows.get(0).getColumnData("id");
            return idStr != null ? Integer.parseInt(idStr) : null;
        } catch (Exception e) {
            log.error("insertPhotoMeta error: {}", e.getMessage(), e);
            return null;
        }
    }

    private void showPreview(Integer photoId) {
        try {
            String dirPhotos = new GenericView(recordService).getAppProps(PROP_PHOTOS);
            String sep       = FileSystems.getDefault().getSeparator();
            List<Record> rows = recordService.findAll(
                    "SELECT name_new FROM photo_meta WHERE id = " + photoId,
                    new String[]{"name_new"});
            if (rows.isEmpty()) return;
            String nameNew = rows.get(0).getColumnData("name_new");
            if (nameNew == null || nameNew.isBlank()) return;

            File imgFile = new File(dirPhotos + sep + subPathMedium + sep + nameNew);
            if (!imgFile.exists()) imgFile = new File(dirPhotos + sep + subPathShow + sep + nameNew);
            if (!imgFile.exists()) return;

            Image img = new Image();
            img.setSrc(DownloadHandler.forFile(imgFile));
            img.addClassName("npu-preview-img");
            previewBox.add(img);
        } catch (Exception e) {
            log.warn("Could not load preview for photoId {}: {}", photoId, e.getMessage());
        }
    }

    private void showError(String msg) {
        Notification n = Notification.show(msg, 4000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public Integer getCurrentPhotoId() { return currentPhotoId; }
}
