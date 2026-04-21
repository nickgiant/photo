package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photo_meta",
    indexes = {
        @Index(name = "idx_photo_meta_uploader_id", columnList = "uploaderId"),
        @Index(name = "idx_photo_meta_destination_id", columnList = "destination_id"),
        @Index(name = "idx_photo_meta_date_inserted", columnList = "date_inserted")
    })
public class PhotoMetaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name_new", length = 255)
    private String nameNew;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "subtitle", length = 255)
    private String subtitle;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "photo_type", length = 50)
    private String photoType;

    @Column(name = "uploader", length = 100)
    private String uploader;

    @Column(name = "creator", length = 100)
    private String creator;

    @Column(name = "visible_to", length = 17)
    private String visibleTo;

    @Column(name = "meta_date")
    private LocalDateTime metaDate;

    @Column(name = "date_inserted")
    private LocalDateTime dateInserted;

    @Column(name = "space_size")
    private Long spaceSize;

    @Column(name = "space_size_medium")
    private Long spaceSizeMedium;

    @Column(name = "space_size_thumb")
    private Long spaceSizeThumb;

    @Column(name = "meta_camera_make", length = 100)
    private String metaCameraMake;

    @Column(name = "meta_camera_model", length = 100)
    private String metaCameraModel;

    @Column(name = "meta_lens_make", length = 100)
    private String metaLensMake;

    @Column(name = "meta_lens_model", length = 100)
    private String metaLensModel;

    @Column(name = "meta_focal_length", length = 50)
    private String metaFocalLength;

    @Column(name = "meta_focal_length_ff", length = 50)
    private String metaFocalLengthFf;

    @Column(name = "meta_iso")
    private Integer metaIso;

    @Column(name = "meta_aperture", length = 50)
    private String metaAperture;

    @Column(name = "meta_shutter_speed", length = 50)
    private String metaShutterSpeed;

    @Column(name = "meta_orientation")
    private Integer metaOrientation;

    @Column(name = "meta_i_height")
    private Integer metaIHeight;

    @Column(name = "meta_i_length")
    private Integer metaILength;

    @Column(name = "meta_i_width")
    private Integer metaIWidth;

    @Column(name = "location_by_user", length = 255)
    private String locationByUser;

    @Column(name = "location_area", length = 255)
    private String locationArea;

    @Column(name = "location_country_code", length = 10)
    private String locationCountryCode;

    @Column(name = "location_lat")
    private Double locationLat;

    @Column(name = "location_lon")
    private Double locationLon;

    @Column(name = "uploaderId")
    private Integer uploaderId;

    @Column(name = "destination_id")
    private Integer destinationId;

    @Column(name = "subject_id")
    private Integer subjectId;

    protected PhotoMetaEntity() {}

    public Integer getId()               { return id; }
    public String  getNameNew()          { return nameNew; }
    public String  getTitle()            { return title; }
    public String  getSubtitle()         { return subtitle; }
    public String  getNotes()            { return notes; }
    public String  getPhotoType()        { return photoType; }
    public String  getUploader()         { return uploader; }
    public String  getCreator()          { return creator; }
    public String  getVisibleTo()        { return visibleTo; }
    public LocalDateTime getMetaDate()   { return metaDate; }
    public LocalDateTime getDateInserted() { return dateInserted; }
    public Long    getSpaceSize()        { return spaceSize; }
    public Long    getSpaceSizeMedium()  { return spaceSizeMedium; }
    public Long    getSpaceSizeThumb()   { return spaceSizeThumb; }
    public String  getMetaCameraMake()   { return metaCameraMake; }
    public String  getMetaCameraModel()  { return metaCameraModel; }
    public String  getMetaLensMake()     { return metaLensMake; }
    public String  getMetaLensModel()    { return metaLensModel; }
    public String  getMetaFocalLength()  { return metaFocalLength; }
    public String  getMetaFocalLengthFf() { return metaFocalLengthFf; }
    public Integer getMetaIso()          { return metaIso; }
    public String  getMetaAperture()     { return metaAperture; }
    public String  getMetaShutterSpeed() { return metaShutterSpeed; }
    public Integer getMetaOrientation()  { return metaOrientation; }
    public Integer getMetaIHeight()      { return metaIHeight; }
    public Integer getMetaILength()      { return metaILength; }
    public Integer getMetaIWidth()       { return metaIWidth; }
    public String  getLocationByUser()   { return locationByUser; }
    public String  getLocationArea()     { return locationArea; }
    public String  getLocationCountryCode() { return locationCountryCode; }
    public Double  getLocationLat()      { return locationLat; }
    public Double  getLocationLon()      { return locationLon; }
    public Integer getUploaderId()       { return uploaderId; }
    public Integer getDestinationId()    { return destinationId; }
    public Integer getSubjectId()        { return subjectId; }

    public void setVisibleTo(String visibleTo)        { this.visibleTo = visibleTo; }
    public void setTitle(String title)                { this.title = title; }
    public void setSubtitle(String subtitle)          { this.subtitle = subtitle; }
    public void setNotes(String notes)                { this.notes = notes; }
    public void setDestinationId(Integer destinationId) { this.destinationId = destinationId; }
    public void setSubjectId(Integer subjectId)       { this.subjectId = subjectId; }
}
