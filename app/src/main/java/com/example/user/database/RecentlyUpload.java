package com.example.user.database;

import java.util.Date;

public class RecentlyUpload {   /*Concentration of all reports from all places*/

    private String placeName;
    private Date date;
    private String userId;

    public RecentlyUpload(){}

    public RecentlyUpload(String placeName, String userId)
    {
        this.placeName = placeName;
        this.date = new Date();
        this.userId = userId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public Date getDate() {
        return date;
    }

    public String getUserId() {
        return userId;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
