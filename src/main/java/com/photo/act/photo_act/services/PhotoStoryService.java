package com.photo.act.photo_act.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Service
public class PhotoStoryService {

    private final JdbcTemplate jdbc;


    public PhotoStoryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> findAll(String sql) {
        return jdbc.queryForList(sql);
    }


    //Fetch paged data
    public List<Map<String, Object>> fetch(String strSql, String[] columnNames, int limit, int offset, String sortField, boolean asc) {


        Set<String> allowedSorts = new HashSet<>(Arrays.stream(columnNames).toList());
        ;
        if (!allowedSorts.contains(sortField)) {
            sortField = "date_inserted";
        }

//        String sql = """
//                    ORDER BY %s %s
//                    LIMIT ? OFFSET ?
//                """.formatted(sortField, asc ? "ASC" : "DESC");
        String sql = """ 
                    LIMIT ? OFFSET ?
                """;

        return jdbc.queryForList(strSql + sql, limit, offset);
    }


    public int count(String strQuery) {
        int intReturn = 0;
        try {
            intReturn = jdbc.queryForObject(
                    "SELECT COUNT(1) FROM (" + strQuery + ") AS q ",
                    Integer.class
            );
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return intReturn;
    }


    public void insert(Map<String, Object> row) {
        jdbc.update("""
                    INSERT INTO users(name, email)
                    VALUES(?, ?)
                """, row.get("name"), row.get("email"));
    }

    public void update(Map<String, Object> row) {
        jdbc.update("""
                    UPDATE users
                    SET name=?, email=?
                    WHERE id=?
                """, row.get("name"), row.get("email"), row.get("id"));
    }

    public Integer insertAndGetGeneratedId(String sql, Object... args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : null;
    }
}
