package com.photo.act.photo_act.model;

import java.util.ArrayList;
import java.util.List;

/** Mutable DTO used to carry user input when creating or editing a news entry. */
public class NewsCreateDto {

    private String  title;
    private String  description;
    private Integer photoId;
    private String  originalAuthor;
    private Long    categoryId;
    private List<NewsItemCreateDto> items = new ArrayList<>();

    public NewsCreateDto() {}

    public NewsCreateDto(String title, String description, Integer photoId,
                         String originalAuthor, Long categoryId) {
        this.title          = title;
        this.description    = description;
        this.photoId        = photoId;
        this.originalAuthor = originalAuthor;
        this.categoryId     = categoryId;
    }

    public String  getTitle()          { return title; }
    public String  getDescription()    { return description; }
    public Integer getPhotoId()        { return photoId; }
    public String  getOriginalAuthor() { return originalAuthor; }
    public Long    getCategoryId()     { return categoryId; }
    public List<NewsItemCreateDto> getItems() { return items; }

    public void setTitle(String title)                   { this.title = title; }
    public void setDescription(String description)       { this.description = description; }
    public void setPhotoId(Integer photoId)              { this.photoId = photoId; }
    public void setOriginalAuthor(String originalAuthor) { this.originalAuthor = originalAuthor; }
    public void setCategoryId(Long categoryId)           { this.categoryId = categoryId; }
    public void setItems(List<NewsItemCreateDto> items)  { this.items = items; }

    /** Inner DTO for individual news items. */
    public static class NewsItemCreateDto {

        private String  title;
        private String  description;
        private Integer photoId;
        private String  video;
        private String  urlMore1;
        private String  urlMore2;
        private String  urlMore3;
        private String  urlMore4;
        private Integer sortOrder;

        public NewsItemCreateDto() {}

        public String  getTitle()       { return title; }
        public String  getDescription() { return description; }
        public Integer getPhotoId()     { return photoId; }
        public String  getVideo()       { return video; }
        public String  getUrlMore1()    { return urlMore1; }
        public String  getUrlMore2()    { return urlMore2; }
        public String  getUrlMore3()    { return urlMore3; }
        public String  getUrlMore4()    { return urlMore4; }
        public Integer getSortOrder()   { return sortOrder; }

        public void setTitle(String title)             { this.title = title; }
        public void setDescription(String description) { this.description = description; }
        public void setPhotoId(Integer photoId)        { this.photoId = photoId; }
        public void setVideo(String video)             { this.video = video; }
        public void setUrlMore1(String urlMore1)       { this.urlMore1 = urlMore1; }
        public void setUrlMore2(String urlMore2)       { this.urlMore2 = urlMore2; }
        public void setUrlMore3(String urlMore3)       { this.urlMore3 = urlMore3; }
        public void setUrlMore4(String urlMore4)       { this.urlMore4 = urlMore4; }
        public void setSortOrder(Integer sortOrder)    { this.sortOrder = sortOrder; }
    }
}
