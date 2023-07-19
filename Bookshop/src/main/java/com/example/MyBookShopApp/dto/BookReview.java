package com.example.MyBookShopApp.dto;

import java.time.LocalDateTime;

public class BookReview {

    private int id;

    private String shownText;

    private String hiddenText;

    private int likesCount;

    private int dislikesCount;

    private String userRating;

    private String userName;

    private LocalDateTime time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShownText() {
        return shownText;
    }

    public void setShownText(String shownText) {
        this.shownText = shownText;
    }

    public String getHiddenText() {
        return hiddenText;
    }

    public void setHiddenText(String hiddenText) {
        this.hiddenText = hiddenText;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(int dislikesCount) {
        this.dislikesCount = dislikesCount;
    }

    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

}
