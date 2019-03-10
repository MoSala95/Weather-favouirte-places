package com.example.weather_demo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kwabenaberko.openweathermaplib.constants.Units;
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper;
import com.kwabenaberko.openweathermaplib.implementation.callbacks.CurrentWeatherCallback;
import com.kwabenaberko.openweathermaplib.models.common.Main;
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.weather_demo.MapActivity.ADD_NEW_CITY;
import static com.example.weather_demo.MapActivity.MY_PREFS_CITIES;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private CityAdapter mCityAdapter;
    private ArrayList<City> mCityList;
    private FloatingActionButton add_btn;
    private List<SavedCities> savedCities=new ArrayList<>();
    OpenWeatherMapHelper helper = new OpenWeatherMapHelper("5dde7cf28968da7a9cb5f690ee6c5fac");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView=findViewById(R.id.recycler_view);
        add_btn=findViewById(R.id.add_btn);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent);
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

       mCityList=new ArrayList<>();

       /*  mCityList.add(new City("Cairo","22",R.drawable.cloudy_icon));
        mCityAdapter=new CityAdapter(MainActivity.this,mCityList);
        mRecyclerView.setAdapter(mCityAdapter);*/

        helper.setUnits(Units.METRIC);
        mCityAdapter = new CityAdapter(MainActivity.this, mCityList);
        mRecyclerView.setAdapter(mCityAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("onresumecall ","yes");
        if(getDataFromSharedPreferences( )!=null){
            savedCities.clear();
            mCityList.clear();
            mCityAdapter.notifyDataSetChanged();
            savedCities=getDataFromSharedPreferences( );
        }
        if(savedCities.size()>0) {
            for ( int i = 0; i < savedCities.size(); i++) {
                Log.i("MainActivityc",savedCities.get(i).cityName);
                final int finalI = i;
                helper.getCurrentWeatherByGeoCoordinates(savedCities.get(i).citylatitude, savedCities.get(i).citylangitude, new CurrentWeatherCallback() {
                    @Override
                    public void onSuccess(CurrentWeather currentWeather) {
                        Log.v("MainActivityw", "Coordinates: " + currentWeather.getCoord().getLat() + ", " + currentWeather.getCoord().getLon() + "\n"
                                + "Weather Description: " + currentWeather.getWeather().get(0).getDescription() + "\n"
                                + "Temperature: " + currentWeather.getMain().getTempMax() + "\n"
                                + "Wind Speed: " + currentWeather.getWind().getSpeed() + "\n"
                                + "City, Country: " + currentWeather.getName() + ", " + currentWeather.getSys().getCountry()
                        );
                        mCityList.add(new City(savedCities.get(finalI).cityName+" ," + currentWeather.getSys().getCountry(), String.valueOf((int) currentWeather.getMain().getTempMax()),
                                currentWeather.getWeather().get(0).getIcon(), currentWeather.getWeather().get(0).getDescription(),
                                currentWeather.getCoord().getLat(),currentWeather.getCoord().getLon()));
                        mCityAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.v(".MainActivity", throwable.getMessage());
                    }
                });
            }

        }
    }

    private List<SavedCities> getDataFromSharedPreferences(){
        Gson gson = new Gson();
        List<SavedCities> productFromShared = new ArrayList<>();
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(MY_PREFS_CITIES, Context.MODE_PRIVATE);
        String jsonPreferences = sharedPref.getString(ADD_NEW_CITY, "");

        Type type = new TypeToken<List<SavedCities>>() {}.getType();
        productFromShared = gson.fromJson(jsonPreferences, type);

        return productFromShared;
    }
}
