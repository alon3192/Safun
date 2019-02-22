package com.example.user.database;

import java.util.ArrayList;
import java.util.List;

public class Place {

    private String name;
    private String address;
    private String telephone;
    private String imagePath;
    private double latitude;
    private double longitude;
    private String type;

    public Place(){}

    public Place(String name, String address, String telephone, String imagePath, double latitude, double longitude, String type) {
        this.name = name;
        this.address = address;
        this.telephone = telephone;
        this.imagePath = imagePath;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getImagePath() {
        return imagePath;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setType(String type) {
        this.type = type;
    }

}
