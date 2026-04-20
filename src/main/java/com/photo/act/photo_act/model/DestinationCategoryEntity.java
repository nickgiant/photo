package com.photo.act.photo_act.model;

import jakarta.persistence.*;

@Entity
@Table(name = "destination_categories",
    indexes = @Index(name = "idx_dest_cat_order", columnList = "dest_cat_order"))
public class DestinationCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dest_cat_title", nullable = false, length = 100)
    private String destCatTitle;

    @Column(name = "dest_cat_type", length = 100)
    private String destCatType;

    @Column(name = "dest_cat_descr_min", length = 500)
    private String destCatDescrMin;

    @Column(name = "dest_cat_order")
    private Integer destCatOrder;

    protected DestinationCategoryEntity() {}

    public DestinationCategoryEntity(String destCatTitle, String destCatType,
                                      String destCatDescrMin, Integer destCatOrder) {
        this.destCatTitle    = destCatTitle;
        this.destCatType     = destCatType;
        this.destCatDescrMin = destCatDescrMin;
        this.destCatOrder    = destCatOrder;
    }

    public Integer getId()             { return id; }
    public String  getDestCatTitle()   { return destCatTitle; }
    public String  getDestCatType()    { return destCatType; }
    public String  getDestCatDescrMin() { return destCatDescrMin; }
    public Integer getDestCatOrder()   { return destCatOrder; }

    public void setDestCatTitle(String destCatTitle)     { this.destCatTitle = destCatTitle; }
    public void setDestCatType(String destCatType)       { this.destCatType = destCatType; }
    public void setDestCatDescrMin(String destCatDescrMin) { this.destCatDescrMin = destCatDescrMin; }
    public void setDestCatOrder(Integer destCatOrder)    { this.destCatOrder = destCatOrder; }
}
