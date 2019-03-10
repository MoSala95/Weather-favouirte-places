package com.example.weather_demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    // Project Created by Ferdousur Rahman Shajib
    // www.androstock.com

    TextView cityField, detailsField, currentTemperatureField, humidity_field, pressure_field, weatherIcon, updatedField;
    Button selectCity;
    ProgressBar loader;
    Typeface weatherFont;
    String city_name = "";
    double latitude;
    double longtuide;
    /* Please Put your API KEY here */
    String OPEN_WEATHER_MAP_API = "5dde7cf28968da7a9cb5f690ee6c5fac";
    /* Please Put your API KEY here */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_detail);

        loader = (ProgressBar) findViewById(R.id.loader);
        selectCity = (Button) findViewById(R.id.selectCity);
        cityField = (TextView) findViewById(R.id.city_field);
        updatedField = (TextView) findViewById(R.id.updated_field);
        detailsField = (TextView) findViewById(R.id.details_field);
        currentTemperatureField = (TextView) findViewById(R.id.current_temperature_field);
        humidity_field = (TextView) findViewById(R.id.humidity_field);
        pressure_field = (TextView) findViewById(R.id.pressure_field);
        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);
        Intent intent = getIntent();
        city_name = intent.getStringExtra("CityName");
        latitude = intent.getDoubleExtra("Lat",0);
        longtuide = intent.getDoubleExtra("Long",0);
        final LatLng latLng=new LatLng(latitude,longtuide);

        taskLoadUp(latLng);

        selectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intent =new Intent(DetailActivity.this,MapActivity.class);
              startActivity(intent);
              finish();
            }
        });

    }

    public void taskLoadUp(LatLng query) {
        if (WeatherMapConnection.isNetworkAvailable(getApplicationContext())) {
            DownloadWeather task = new DownloadWeather();
            task.execute(query);
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }



    class DownloadWeather extends AsyncTask < LatLng, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loader.setVisibility(View.VISIBLE);

        }
        protected String doInBackground(LatLng...args) {
        String xml = WeatherMapConnection.excuteGet("http://api.openweathermap.org/data/2.5/weather?lat="+args[0].latitude+"&lon=" + args[0].longitude +
                    "&units=metric&appid=" + OPEN_WEATHER_MAP_API);
            /*String xml = WeatherMapConnection.excuteGet("http://api.openweathermap.org/data/2.5/forecast?q="+args[0]+"&units=metric"+"&cnt=3"+"&APPID="+
                    OPEN_WEATHER_MAP_API);*/
            Log.i("jsonresult ",xml);
            return xml;
        }
        @Override
        protected void onPostExecute(String xml) {

            try {
                JSONObject json = new JSONObject(xml);
                if (json != null) {
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    /*JSONObject firstday = json.getJSONArray("list").getJSONObject(0);
                    Log.i("jsonresultfirst",firstday.toString());*/
                   /* JSONObject details = firstday.getJSONArray("weather").getJSONObject(0);
                    Log.i("jsonresultdetail",details.toString());*/
                    JSONObject main = json.getJSONObject("main");
                    /*JSONObject main = firstday.getJSONObject("main");
                    Log.i("jsonresultmain",main.toString());*/
                   DateFormat df = DateFormat.getDateTimeInstance();
                    if(!city_name.equals(""))
                    cityField.setText(json.getString("name").toUpperCase(Locale.US)+" ," +city_name);
                    else cityField.setText(json.getString("name").toUpperCase(Locale.US)+" ," +city_name  + ", " + json.getJSONObject("sys").getString("country"));

                    //  cityField.setText(json.getJSONObject("city").getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("city").getString("country"));

                    detailsField.setText(details.getString("description").toUpperCase(Locale.US));
                    currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp")) + "°c");
                    humidity_field.setText("Humidity: " + main.getString("humidity") + "%");
                    pressure_field.setText("Pressure: " + main.getString("pressure") + " hPa");
                   updatedField.setText(df.format(new Date(json.getLong("dt") * 1000)));
                    weatherIcon.setText(Html.fromHtml(WeatherMapConnection.setWeatherIcon(details.getInt("id"),
                            json.getJSONObject("sys").getLong("sunrise") * 1000,
                            json.getJSONObject("sys").getLong("sunset") * 1000)));

                    loader.setVisibility(View.GONE);

                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
            }

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}