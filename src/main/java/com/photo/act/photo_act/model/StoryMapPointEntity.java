package com.photo.act.photo_act.model;

import jakarta.persistence.*;

@Entity
@Table(name = "photo_story_map_point",
    indexes = {
        @Index(name = "idx_story_map_point_map_id", columnList = "map_id")
    })
public class StoryMapPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "map_id", nullable = false)
    private Integer mapId;

    @Column(name = "point_name", length = 255)
    private String pointName;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lon", nullable = false)
    private Double lon;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "point_order")
    private Integer pointOrder;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getMapId() { return mapId; }
    public void setMapId(Integer mapId) { this.mapId = mapId; }

    public String getPointName() { return pointName; }
    public void setPointName(String pointName) { this.pointName = pointName; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPointOrder() { return pointOrder; }
    public void setPointOrder(Integer pointOrder) { this.pointOrder = pointOrder; }
}
