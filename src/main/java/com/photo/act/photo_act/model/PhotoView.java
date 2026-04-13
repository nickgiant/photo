package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photo_view",
        indexes = @Index(name = "idx_photo_view_photo_id", columnList = "photo_id"))
public class PhotoView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo_id", nullable = false)
    private int photoId;

    @Column(name = "name_new", nullable = false, length = 255)
    private String nameNew;

    /** Null when viewer is a guest (not logged in). */
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /**
     * "List" — photo visible in gallery card (thumbnail / medium).
     * "Full" — photo opened in full-screen dialog via View Larger or Rate it.
     */
    @Column(name = "view_type", nullable = false, length = 10)
    private String viewType;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    protected PhotoView() {}

    public PhotoView(int photoId, String nameNew, Integer userId, String ipAddress, String viewType) {
        this.photoId   = photoId;
        this.nameNew   = nameNew;
        this.userId    = userId;
        this.ipAddress = ipAddress;
        this.viewType  = viewType;
        this.viewedAt  = LocalDateTime.now();
    }

    public Long    getId()        { return id; }
    public int     getPhotoId()   { return photoId; }
    public String  getNameNew()   { return nameNew; }
    public Integer getUserId()    { return userId; }
    public String  getIpAddress() { return ipAddress; }
    public String  getViewType()  { return viewType; }
    public LocalDateTime getViewedAt() { return viewedAt; }
}
