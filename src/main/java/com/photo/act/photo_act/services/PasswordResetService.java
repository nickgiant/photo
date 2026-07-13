package com.photo.act.photo_act.services;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_VALID_HOURS = 1;
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RecordService recordService;

    public PasswordResetService(RecordService recordService) {
        this.recordService = recordService;
        ensureColumnsExist();
    }

    private void ensureColumnsExist() {
        String sql = "ALTER TABLE dbuser " +
                "ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(64) NULL, " +
                "ADD COLUMN IF NOT EXISTS password_reset_expiry DATETIME NULL";
        try {
            recordService.insertOneRecordWithQuery(sql, null, null);
        } catch (Exception e) {
            logger.error("Could not ensure password reset columns exist on dbuser: " + e.getMessage());
        }
    }

    public String createResetToken(String username) {
        String token = UUID.randomUUID().toString();
        String strExpiry = LocalDateTime.now().plusHours(TOKEN_VALID_HOURS).format(DATETIME_FORMAT);

        String sql = "UPDATE dbuser SET password_reset_token = ?, password_reset_expiry = ? WHERE username = ?";
        Object[] values = {token, strExpiry, username};
        String[] types = {"java.lang.String", "java.lang.String", "java.lang.String"};
        recordService.insertOneRecordWithQuery(sql, values, types);

        return token;
    }

    public String getUsernameForToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        // Compared against an app-computed timestamp (not the DB's NOW()) so a clock/timezone
        // difference between the app server and the database can never make a fresh token look expired.
        String strNow = LocalDateTime.now().format(DATETIME_FORMAT);

        String[] cols = {"username"};
        String sql = "SELECT username FROM dbuser WHERE password_reset_token = ? AND password_reset_expiry > ?";
        Object[] values = {token, strNow};
        String[] types = {"java.lang.String", "java.lang.String"};

        List<Record> lst = recordService.findAll(sql, cols, values, types);
        if (!lst.isEmpty()) {
            return lst.get(0).getColumnData("username");
        }

        logTokenLookupFailure(token, strNow);
        return null;
    }

    private void logTokenLookupFailure(String token, String strNow) {
        try {
            String[] cols = {"username", "password_reset_expiry"};
            String sql = "SELECT username, password_reset_expiry FROM dbuser WHERE password_reset_token = ?";
            Object[] values = {token};
            String[] types = {"java.lang.String"};
            List<Record> lst = recordService.findAll(sql, cols, values, types);

            if (lst.isEmpty()) {
                logger.warn("Password reset token not found in dbuser (app now=" + strNow + ")");
            } else {
                String strExpiry = lst.get(0).getColumnData("password_reset_expiry");
                logger.warn("Password reset token found but expired for user " + lst.get(0).getColumnData("username")
                        + ": stored expiry=" + strExpiry + " app now=" + strNow);
            }
        } catch (Exception e) {
            logger.error("Failed while diagnosing password reset token lookup failure: " + e.getMessage());
        }
    }

    public void invalidateToken(String username) {
        String sql = "UPDATE dbuser SET password_reset_token = NULL, password_reset_expiry = NULL WHERE username = ?";
        Object[] values = {username};
        String[] types = {"java.lang.String"};
        recordService.insertOneRecordWithQuery(sql, values, types);
    }
}
