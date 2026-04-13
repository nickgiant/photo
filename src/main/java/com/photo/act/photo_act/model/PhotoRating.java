package com.photo.act.photo_act.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photo_rating",
        uniqueConstraints = @UniqueConstraint(columnNames = {"photo_id", "user_id"}))
public class PhotoRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo_id", nullable = false)
    private int photoId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "name_new", length = 255)
    private String nameNew;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "rated_at", nullable = false)
    private LocalDateTime ratedAt;

    protected PhotoRating() {}

    public PhotoRating(int photoId, int userId, int rating, String nameNew, String ipAddress) {
        this.photoId   = photoId;
        this.userId    = userId;
        this.rating    = rating;
        this.nameNew   = nameNew;
        this.ipAddress = ipAddress;
        this.ratedAt   = LocalDateTime.now();
    }

    public Long   getId()        { return id; }
    public int    getPhotoId()   { return photoId; }
    public int    getUserId()    { return userId; }
    public int    getRating()    { return rating; }
    public String getNameNew()   { return nameNew; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getRatedAt() { return ratedAt; }

    public void updateRating(int rating, String ipAddress) {
        this.rating    = rating;
        this.ipAddress = ipAddress;
        this.ratedAt   = LocalDateTime.now();
    }
}
