package com.example.weather_demo;

public class SavedCities {
    String cityName;
    double citylatitude;
    double citylangitude;

    public SavedCities(String cityName, double citylatitude, double citylangitude) {
        this.cityName = cityName;
        this.citylatitude = citylatitude;
        this.citylangitude = citylangitude;
    }

    public String getCityName() {
        return cityName;
    }

    public double getCitylatitude() {
        return citylatitude;
    }

    public double getCitylangitude() {
        return citylangitude;
    }
}
