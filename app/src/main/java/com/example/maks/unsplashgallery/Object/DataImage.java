package com.example.maks.unsplashgallery.Object;

/**
 * Created by zinch on 06.09.2017.
 */

public class DataImage {

    String id;
    String linkDownload;
    Integer likes;
    String date;
    boolean isLiked;

    public DataImage(String id,String linkDownload, Integer likes, String date, boolean  isLiked) {
        this.id=id;
        this.linkDownload = linkDownload;
        this.likes = likes;
        this.date = date;
        this.isLiked=isLiked;
    }

    public DataImage() {
    }

    public String getLinkDownload() {
        return linkDownload;
    }

    public void setLinkDownload(String linkDownload) {
        this.linkDownload = linkDownload;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean liked) {
        isLiked = liked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
