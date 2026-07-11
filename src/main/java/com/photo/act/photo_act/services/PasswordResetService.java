package com.photo.act.photo_act.services;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_VALID_HOURS = 1;

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

        String sql = "UPDATE dbuser SET password_reset_token = ?, " +
                "password_reset_expiry = DATE_ADD(NOW(), INTERVAL " + TOKEN_VALID_HOURS + " HOUR) " +
                "WHERE username = ?";
        Object[] values = {token, username};
        String[] types = {"java.lang.String", "java.lang.String"};
        recordService.insertOneRecordWithQuery(sql, values, types);

        return token;
    }

    public String getUsernameForToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        String[] cols = {"username"};
        String sql = "SELECT username FROM dbuser WHERE password_reset_token = ? AND password_reset_expiry > NOW()";
        Object[] values = {token};
        String[] types = {"java.lang.String"};

        List<Record> lst = recordService.findAll(sql, cols, values, types);
        if (lst.isEmpty()) {
            return null;
        }
        return lst.get(0).getColumnData("username");
    }

    public void invalidateToken(String username) {
        String sql = "UPDATE dbuser SET password_reset_token = NULL, password_reset_expiry = NULL WHERE username = ?";
        Object[] values = {username};
        String[] types = {"java.lang.String"};
        recordService.insertOneRecordWithQuery(sql, values, types);
    }
}
