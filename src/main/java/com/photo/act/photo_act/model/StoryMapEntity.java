package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photo_story_map",
    indexes = {
        @Index(name = "idx_story_map_item_id", columnList = "story_item_id"),
        @Index(name = "idx_story_map_story_id", columnList = "story_id")
    })
public class StoryMapEntity {

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

    public LocalDateTime getDateInserted() { return dateInserted; }
    public void setDateInserted(LocalDateTime dateInserted) { this.dateInserted = dateInserted; }
}
