package com.photo.act.photo_act.model;

import jakarta.persistence.*;

@Entity
@Table(name = "photo_album_categories",
    indexes = @Index(name = "idx_photo_album_cat_order", columnList = "cat_order"))
public class PhotoAlbumCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cat_title", nullable = false, length = 100)
    private String catTitle;

    @Column(name = "cat_type", length = 100)
    private String catType;

    @Column(name = "cat_description_min", length = 500)
    private String catDescriptionMin;

    @Column(name = "cat_type_description_min", length = 500)
    private String catTypeDescriptionMin;

    @Column(name = "cat_description_big", columnDefinition = "TEXT")
    private String catDescriptionBig;

    @Column(name = "cat_order")
    private Integer catOrder;

    protected PhotoAlbumCategoryEntity() {}

    public PhotoAlbumCategoryEntity(String catTitle, String catType,
                                     String catDescriptionMin, String catTypeDescriptionMin,
                                     String catDescriptionBig, Integer catOrder) {
        this.catTitle              = catTitle;
        this.catType               = catType;
        this.catDescriptionMin     = catDescriptionMin;
        this.catTypeDescriptionMin = catTypeDescriptionMin;
        this.catDescriptionBig     = catDescriptionBig;
        this.catOrder              = catOrder;
    }

    public Integer getId()                  { return id; }
    public String  getCatTitle()            { return catTitle; }
    public String  getCatType()             { return catType; }
    public String  getCatDescriptionMin()   { return catDescriptionMin; }
    public String  getCatTypeDescriptionMin() { return catTypeDescriptionMin; }
    public String  getCatDescriptionBig()   { return catDescriptionBig; }
    public Integer getCatOrder()            { return catOrder; }

    public void setCatTitle(String catTitle)                  { this.catTitle = catTitle; }
    public void setCatType(String catType)                    { this.catType = catType; }
    public void setCatDescriptionMin(String catDescriptionMin) { this.catDescriptionMin = catDescriptionMin; }
    public void setCatDescriptionBig(String catDescriptionBig) { this.catDescriptionBig = catDescriptionBig; }
    public void setCatOrder(Integer catOrder)                  { this.catOrder = catOrder; }
}
