package com.example.user.database;

import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Report {


    private float vibe;
    private float prices;
    private float crowding;
    private String music;
    private int parking;
    private String summary;
    private String reportKey;
    private String status;
    private String userName;
    private String userId;
    private Date date;
    private boolean relevant;

    private int likes;
    private int unlikes;

    private String imagePath;



    private FirebaseAuth mAuth;


    public  Report(){}

    public Report(String reportKey, String userName, float vibe, float prices, float crowding, String music, int parking, String summary, String imagePath)
    {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        this.userName = userName;
        this.userId = user.getUid();
        this.date = new Date();
        this.reportKey = reportKey;
        this.vibe = vibe;
        this.prices = prices;
        this.crowding = crowding;
        this.music = music;
        this.parking = parking;
        this.summary = summary;
        this.relevant=true;
        this.likes=0;
        this.unlikes=0;
        this.imagePath = imagePath;


        this.status = calcStatus(vibe, prices, crowding, parking);
    }


    public float getVibe() {
        return vibe;
    }

    public float getPrices() {
        return prices;
    }

    public float getCrowding() {
        return crowding;
    }

    public String getMusic() {
        return music;
    }

    public int getParking() {
        return parking;
    }

    public String getSummary() {
        return summary;
    }

    public String getReportKey() {
        return reportKey;
    }

    public String getStatus() {
        return status;
    }

    public String getUserName() {
        return userName;
    }
    public Date getDate(){
        return date;
    }

    public boolean isRelevant() {
        return relevant;
    }

    public int getLikes() {
        return likes;
    }

    public int getUnlikes() {
        return unlikes;
    }

    public String getUserId() {
        return userId;
    }

    public String getImagePath() {
        return imagePath;
    }


    public void setVibe(float vibe) {
        this.vibe = vibe;
    }

    public void setPrices(float prices) {
        this.prices = prices;
    }

    public void setCrowding(float crowding) {
        this.crowding = crowding;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public void setParking(int parking) {
        this.parking = parking;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setReportKey(String reportKey) {
        this.reportKey = reportKey;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setRelevant(boolean relevant) {
        this.relevant = relevant;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setUnlikes(int unlikes) {
        this.unlikes = unlikes;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    private String calcStatus(float vibe, float prices, float crowding, int parking)
    {
        float sumGrades = vibe + prices + crowding + parking;
        String status;

        if(sumGrades<6)
        {
            status = "Suffering";
        }
        else if(sumGrades>6 && sumGrades<11)
        {
            status="Nice";
        }
        else
        {
            status = "Fun";
        }
        return status;
    }
}
