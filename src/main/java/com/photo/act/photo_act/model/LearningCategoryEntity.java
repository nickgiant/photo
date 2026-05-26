package com.photo.act.photo_act.model;

import jakarta.persistence.*;

@Entity
@Table(name = "learnings_categories",
    indexes = {
        @Index(name = "idx_learning_cat_order", columnList = "cat_order")
    })
public class LearningCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cat_title", nullable = false, length = 255)
    private String catTitle;

    @Column(name = "cat_title_type", length = 255)
    private String catTitleType;

    @Column(name = "cat_type", length = 100)
    private String catType;

    @Column(name = "cat_order")
    private Integer catOrder;

    @Column(name = "cat_description_min", columnDefinition = "TEXT")
    private String catDescriptionMin;

    @Column(name = "cat_description_big", columnDefinition = "TEXT")
    private String catDescriptionBig;

    protected LearningCategoryEntity() {}

    public LearningCategoryEntity(String catTitle, String catTitleType, String catType,
                                   Integer catOrder, String catDescriptionMin, String catDescriptionBig) {
        this.catTitle         = catTitle;
        this.catTitleType     = catTitleType;
        this.catType          = catType;
        this.catOrder         = catOrder;
        this.catDescriptionMin = catDescriptionMin;
        this.catDescriptionBig = catDescriptionBig;
    }

    public Long    getId()                { return id; }
    public String  getCatTitle()          { return catTitle; }
    public String  getCatTitleType()      { return catTitleType; }
    public String  getCatType()           { return catType; }
    public Integer getCatOrder()          { return catOrder; }
    public String  getCatDescriptionMin() { return catDescriptionMin; }
    public String  getCatDescriptionBig() { return catDescriptionBig; }

    public void setCatTitle(String catTitle)                   { this.catTitle = catTitle; }
    public void setCatTitleType(String catTitleType)           { this.catTitleType = catTitleType; }
    public void setCatType(String catType)                     { this.catType = catType; }
    public void setCatOrder(Integer catOrder)                  { this.catOrder = catOrder; }
    public void setCatDescriptionMin(String catDescriptionMin) { this.catDescriptionMin = catDescriptionMin; }
    public void setCatDescriptionBig(String catDescriptionBig) { this.catDescriptionBig = catDescriptionBig; }
}
