package com.photo.act.photo_act.dto;

import java.io.Serializable;

public class StoryMapPointDto implements Serializable {

    private Integer id;
    private Integer mapId;
    private String pointName;
    private Double lat;
    private Double lon;
    private String description;
    private Integer pointOrder;

    public StoryMapPointDto() {}

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
