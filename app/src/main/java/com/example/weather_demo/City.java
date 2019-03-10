package com.example.weather_demo;

import android.text.Spanned;

public class City {
    String name;
    String temp;
    String imageUrl;
    String disc;
    double latitude;
    double longitude;

    City(String name, String temp, String imageUrl,String disc,double latitude,double longitude){
        this.name=name;
        this.temp=temp;
        this.imageUrl=imageUrl;
        this.disc=disc;
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public String getName() {
        return name;
    }

    public String getTemp() {
        return temp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDisc() {
        return disc;
    }

    public double getLatitude() {
        return latitude;
    }



    public double getLongitude() {
        return longitude;
    }

}
