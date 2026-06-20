package com.photo.act.photo_act.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class StoryMapDto implements Serializable {

    private Integer id;
    private Integer storyItemId;
    private Integer userId;
    private Integer storyId;
    private String locationArea;
    private LocalDateTime dateInserted;
    private List<StoryMapPointDto> points;

    public StoryMapDto() {}

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

    public LocalDateTime getDateInserted() { return dateInserted; }
    public void setDateInserted(LocalDateTime dateInserted) { this.dateInserted = dateInserted; }

    public List<StoryMapPointDto> getPoints() { return points; }
    public void setPoints(List<StoryMapPointDto> points) { this.points = points; }
}
