package com.example.user.database;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class User {


    private String userId;
    private String userName;
    private int scoring;
    private String photoString;
    private boolean notification;
    private boolean isOnline;

    private List <String> likeReportsKey;
    private List <String> unlikeReportsKey;
    private List <String> favoritePlacesKey;

    private static User user;


    public User(){}


    private User(String userId, String userName, String photoString)
    {
        this.userId = userId;
        this.userName = userName;
        this.scoring=0;
        this.photoString = photoString;
        this.notification=true;
        this.isOnline = true;


        likeReportsKey = new Vector<String>();
        unlikeReportsKey = new Vector<String >();
        favoritePlacesKey = new Vector<String>();
        this.likeReportsKey.add("first");
        this.unlikeReportsKey.add("first");
        this.favoritePlacesKey.add("first");
    }

    public static User getInstance(String userId, String userName, String photoString)
    {
        if(user==null)
        {
            user = new User(userId, userName, photoString);
        }
            return user;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }


    public int getScoring() {
        return scoring;
    }

    public static User getUser() {
        return user;
    }

    public List<String> getLikeReportsKey() {
        return likeReportsKey;
    }

    public List<String> getUnlikeReportsKey() {
        return unlikeReportsKey;
    }

    public List<String> getFavoritePlacesKey() {
        return favoritePlacesKey;
    }

    public String getPhotoString() {
        return photoString;
    }

    public boolean isNotification() {
        return notification;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setScoring(int scoring) {
        this.scoring = scoring;
    }

    public static void setUser(User user) {
        User.user = user;
    }

    public void setLikeReportsKey(List<String> likeReportsKey) {
        this.likeReportsKey = likeReportsKey;
    }

    public void setUnlikeReportsKey(List<String> unlikeReportsKey) {
        this.unlikeReportsKey = unlikeReportsKey;
    }

    public void setFavoritePlacesKey(List<String> favoritePlacesKey) {
        this.favoritePlacesKey = favoritePlacesKey;
    }

    public void setPhotoString(String photoString) {
        this.photoString = photoString;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void addToLikeReportsKey(String key)
    {
        this.likeReportsKey.add(1,key);
    }
    public void addToUnlikeReportsKey(String key)
    {
        this.unlikeReportsKey.add(key);
    }
    public void removeFromLikeReportsKey(String key)
    {
        for(int i=1 ; i<this.likeReportsKey.size() ; i++)
        {
            if(this.likeReportsKey.get(i).equals(key))
            {
                this.likeReportsKey.remove(i);
            }
        }
    }

    public void removeFromUnlikeReportsKey(String key)
    {
        for(int i=1 ; i<this.unlikeReportsKey.size() ; i++)
        {
            if(this.unlikeReportsKey.get(i).equals(key))
            {
                this.unlikeReportsKey.remove(i);
            }
        }
    }

    public void addToFavoritePlace(String key)
    {
        for(int i=0 ; i<this.favoritePlacesKey.size() ; i++)
        {
            if(this.favoritePlacesKey.get(i).equals(key))
            {
                return;
            }
        }
        this.favoritePlacesKey.add(key);
    }

    public void removeFromFavoritePlaces(String key)
    {
        for(int i=0 ; i<this.favoritePlacesKey.size() ; i++)
        {
            if(this.favoritePlacesKey.get(i).equals(key))
            {
                this.favoritePlacesKey.remove(i);
                break;
            }
        }
    }


    public boolean findFavoritePlace(String key)
    {
        for(int i=0 ; i<this.favoritePlacesKey.size() ; i++)
        {
            if(this.favoritePlacesKey.get(i).equals(key))
            {
                return true;
            }
        }
        return false;
    }

}
