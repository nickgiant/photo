package com.photo.act.photo_act.services;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableCaching
//@CacheConfig(cacheNames = "learnings")
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    @Autowired
    private RecordService recordService;
    private List<Record> lstRecords;
    private String columnPk;

    private List<Record> lstLearnings;
    private String columnPkLearnings;

    private List<Record> lstPhotos;
    private String columnPkPhotos;


    public List<Record> getAllRecords(String sql, String[] arrColumnNames, String columnPk) {
        this.columnPk = columnPk;
        lstRecords = getRecordsFromDb(sql, arrColumnNames);
        return lstRecords;
    }

    public List<Record> getAllLearnings(String sql, String[] arrColumnNames, String columnPk) {
        this.columnPkLearnings = columnPk;
        lstLearnings = getRecordsFromDb(sql, arrColumnNames);
        return lstLearnings;
    }

    @Cacheable(value = "learnings", key = "#id")
    public Record getLearningById(String id) {
        logger.info("Fetching learning from in-memory list..." + id);
        for (int r = 0; r < lstLearnings.size(); r++) {
            Record rec = lstLearnings.get(r);
            if (rec.getColumnData(columnPkLearnings).equalsIgnoreCase(id)) {
                return rec;
            }
        }
        return null;
    }


    public List<Record> getAllPhotos(String sql, String[] arrColumnNames, String columnPk) {
        this.columnPkPhotos = columnPk;
        lstPhotos = getRecordsFromDb(sql, arrColumnNames);
        return lstPhotos;
    }

    @Cacheable(value = "photos", key = "#id")
    public Record getPhotoById(String id) {
        logger.info("Fetching photo from in-memory list..." + id);
        for (int r = 0; r < lstPhotos.size(); r++) {
            Record rec = lstPhotos.get(r);
            if (rec.getColumnData(columnPkPhotos).equalsIgnoreCase(id)) {
                return rec;
            }
        }
        return null;
    }

    @CacheEvict(value = "photos", allEntries = true)
    public void evictAllPhotos() {
    }

    @CacheEvict(value = "photos", key = "#id")
    public void evictPhotoId(String id) {

    }

    @CacheEvict(value = "learnings", allEntries = true)
    public void evictAllLearnings() {
    }

    public List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {

        logger.info(" cache  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames, Object[] sqlParValue, String[] sqlParType) {
        logger.info(" cache  getRecordsFromDb with params:   " + sql);
        return recordService.findAll(sql, arrColumnNames, sqlParValue, sqlParType);
    }

}
