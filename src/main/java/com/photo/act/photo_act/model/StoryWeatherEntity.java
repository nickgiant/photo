package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photo_story_weather",
    indexes = {
        @Index(name = "idx_story_weather_item_id", columnList = "story_item_id"),
        @Index(name = "idx_story_weather_story_id", columnList = "story_id")
    })
public class StoryWeatherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "story_item_id", nullable = false)
    private Integer storyItemId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "story_id", nullable = false)
    private Integer storyId;

    @Column(name = "location_area", length = 255)
    private String locationArea;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lon")
    private Double lon;

    @Column(name = "date_inserted")
    private LocalDateTime dateInserted;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getStoryItemId() { return storyItemId; }
    public void setStoryItemId(Integer storyItemId) { this.storyItemId = storyItemId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getStoryId() { return storyId; }
    public void setStoryId(Integer storyId) { this.storyId = storyId; }

    public String getLocationArea() { return locationArea; }
    public void setLocationArea(String locationArea) { this.locationArea = locationArea; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }

    public LocalDateTime getDateInserted() { return dateInserted; }
    public void setDateInserted(LocalDateTime dateInserted) { this.dateInserted = dateInserted; }
}
