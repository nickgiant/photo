package com.photo.act.photo_act.dto;

import java.time.LocalDateTime;

public class StoryWeatherDto {

    private Integer id;
    private Integer storyItemId;
    private Integer userId;
    private Integer storyId;
    private String locationArea;
    private Double lat;
    private Double lon;
    private LocalDateTime dateInserted;

    public StoryWeatherDto() {}

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
