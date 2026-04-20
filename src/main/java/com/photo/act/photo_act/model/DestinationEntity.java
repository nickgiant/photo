package com.photo.act.photo_act.model;

import jakarta.persistence.*;

@Entity
@Table(name = "destination",
    indexes = {
        @Index(name = "idx_destination_country", columnList = "country"),
        @Index(name = "idx_destination_city_name", columnList = "city_name"),
        @Index(name = "idx_destination_category_id", columnList = "category_id")
    })
public class DestinationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "city_name", length = 100)
    private String cityName;

    @Column(name = "prefecture", length = 100)
    private String prefecture;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "nearby_city", length = 100)
    private String nearbyCity;

    @Column(name = "destination_type_name", length = 100)
    private String destinationTypeName;

    @Column(name = "name_for_map", length = 200)
    private String nameForMap;

    @Column(name = "name_for_weather", length = 200)
    private String nameForWeather;

    /** Write-side FK column. */
    @Column(name = "category_id")
    private Integer categoryId;

    /** Read-side FK reference for DDL constraint. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_destination_category_id"))
    private DestinationCategoryEntity category;

    protected DestinationEntity() {}

    public DestinationEntity(String cityName, String prefecture, String country,
                              String nearbyCity, String destinationTypeName,
                              String nameForMap, String nameForWeather, Integer categoryId) {
        this.cityName            = cityName;
        this.prefecture          = prefecture;
        this.country             = country;
        this.nearbyCity          = nearbyCity;
        this.destinationTypeName = destinationTypeName;
        this.nameForMap          = nameForMap;
        this.nameForWeather      = nameForWeather;
        this.categoryId          = categoryId;
    }

    public Integer getId()                 { return id; }
    public String  getCityName()           { return cityName; }
    public String  getPrefecture()         { return prefecture; }
    public String  getCountry()            { return country; }
    public String  getNearbyCity()         { return nearbyCity; }
    public String  getDestinationTypeName() { return destinationTypeName; }
    public String  getNameForMap()         { return nameForMap; }
    public String  getNameForWeather()     { return nameForWeather; }
    public Integer getCategoryId()         { return categoryId; }
    public DestinationCategoryEntity getCategory() { return category; }

    public void setCityName(String cityName)         { this.cityName = cityName; }
    public void setPrefecture(String prefecture)     { this.prefecture = prefecture; }
    public void setCountry(String country)           { this.country = country; }
    public void setNearbyCity(String nearbyCity)     { this.nearbyCity = nearbyCity; }
    public void setCategoryId(Integer categoryId)    { this.categoryId = categoryId; }
    public void setNameForMap(String nameForMap)     { this.nameForMap = nameForMap; }
    public void setNameForWeather(String nameForWeather) { this.nameForWeather = nameForWeather; }
}
