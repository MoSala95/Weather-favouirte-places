package com.example.weather_demo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback , GoogleMap.OnMapClickListener {

    private static final String TAG = "MapActivity";

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public static final float DEFAULT_ZOOM = 14f;
    public static final String MY_PREFS_CITIES="mySavedCities";
    public static final String ADD_NEW_CITY="addNewCity";
    public List<SavedCities> citiesList=new ArrayList<>();
    public double latitude=0;
    public double longitude=0;
    public String cityname="";
    boolean mlocationPermissiongranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<String> cityList;
    AutoCompleteTextView mAutoCompleteSearch;
    ImageView mGps;
    ImageView searchIcon;
    SpeedDialView speedDialView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
         mGps=findViewById(R.id.ic_gps);
         searchIcon=findViewById(R.id.search_icon);
         searchIcon.bringToFront();

        mAutoCompleteSearch = findViewById(R.id.input_search);
        speedDialView = findViewById(R.id.speedDial);

        new LoadData().execute();
        getLocationPermission();

    }
    private void init(){
        Log.d(TAG, "init: gets called");
        mAutoCompleteSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int  actionId, KeyEvent keyEvent) {

                if(actionId== EditorInfo.IME_ACTION_SEARCH|| actionId==EditorInfo.IME_ACTION_DONE
                        ||actionId== EditorInfo.IME_ACTION_NEXT){
                    Log.d(TAG, "onEditorAction: yes");
                    //Excute Search Method
                    hideSoftKeyboard();
                    geoLocateMethod();
                    return true;
                }

                return false;
            }
        });
       mAutoCompleteSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                geoLocateMethod();
            }
        });
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(mAutoCompleteSearch.getText().toString())){
                    hideSoftKeyboard();
                   geoLocateMethod();
                }
            }
        });
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Gps clicked ");
                getDeviceLocation();
            }
        });

    }
    private void geoLocateMethod(){
        Log.d(TAG, "geoLocateMethod: gets called");
        String searchingString= mAutoCompleteSearch.getText().toString();
        Geocoder geocoder=new Geocoder(MapActivity.this);
        List<Address> list=new ArrayList<>();
        try{
            list=geocoder.getFromLocationName(searchingString,1);
        }catch (IOException e){
            Log.e(TAG, "geoLocateMethod: IOException: "+e.getMessage() );
        }
        if(list.size()>0){
            Address address=list.get(0);
            Log.d(TAG, "geoLocateMethod: Find Location Address: "+ address.toString());
            if(address.getLocality()!=null)
            move_camera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getLocality());
            else move_camera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));
            add_dialog();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        if (mlocationPermissiongranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
        }
    }
    private void initMap(){
        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        String[] permissions= {FINE_LOCATION,COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                mlocationPermissiongranted=true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mlocationPermissiongranted=false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0){
                    for(int i=0;i<permissions.length;i++){
                        if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                            mlocationPermissiongranted=false;
                            return;
                        }
                    }
                    mlocationPermissiongranted=true;
                    initMap();
                }
        }
    }

    private void getDeviceLocation(){

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(MapActivity.this);
        try {
            if(mlocationPermissiongranted){
                Task location=fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()&& task.getResult() != null) {
                            Log.d(TAG, "onComplete: found user location");
                            Location currentLocation = (Location) task.getResult();
                            move_camera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM,"My Location");
                        }else {
                            Toast.makeText(MapActivity.this, "unable to get your current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: securitylocation "+e.getMessage() );
        }
    }

    private void move_camera(LatLng latLng ,float zoom,String title){
        Log.d(TAG, "move_camera: to lat: "+ latLng.latitude + " lan: "+ latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        if(!title.equals("My Location")){
            MarkerOptions markerOptions = new MarkerOptions().
                    position(latLng).
                    title(title);
            mMap.addMarker(markerOptions);
            latitude=latLng.latitude;
            longitude=latLng.longitude;
            cityname=title;
        }

    }


    @Override
    public void onMapClick(LatLng latLng) {

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            String address = "";
            String locality="";
        try {
                List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                if (listAddresses != null && listAddresses.size() > 0) {
                    if (listAddresses.get(0).getLocality()!=null) {
                        locality = listAddresses.get(0).getLocality();
                        Log.i("citynameloc ",locality);
                    }
                    else {
                        if(listAddresses.get(0).getThoroughfare() != null) {

                        if (listAddresses.get(0).getSubThoroughfare() != null) {

                            address += listAddresses.get(0).getSubThoroughfare() + " ";

                        }

                        address += listAddresses.get(0).getThoroughfare();
                            Log.i("citynameadd ",address);
                    } }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            if(!locality.equals("")) {

                mMap.addMarker(new MarkerOptions().position(latLng).title(locality));
                mAutoCompleteSearch.setText(locality);
                add_dialog();
                cityname=locality;
                Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();
            }
            else if(!address.equals("")){
                mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                mAutoCompleteSearch.setText(address);
                add_dialog();
                cityname=address;
                Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();
            }
            else {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");
                locality = sdf.format(new Date());
                mMap.addMarker(new MarkerOptions().position(latLng).title(locality));
                mAutoCompleteSearch.setText(locality);
                add_dialog();
                cityname=locality;
                Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();
            }
        latitude=latLng.latitude;
        longitude=latLng.longitude;

        }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = MapActivity.this.getAssets().open("citylist.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public class LoadData extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            try {
                cityList=new ArrayList<>();
                JSONArray m_jArry  = new JSONArray(loadJSONFromAsset());
                for (int i = 0; i < m_jArry.length(); i++) {
                    JSONObject jo_inside = m_jArry.getJSONObject(i);
                    String city_mame = jo_inside.getString("name");
                    //Add your values in your `ArrayList` as below:
                    cityList.add(city_mame);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return cityList;
        }

        @Override
        protected void onPostExecute(List<String> cities) {
            super.onPostExecute(cities);
            // Doing anything with your view
            // This run on UI Thread
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapActivity.this,
                    R.layout.custom_list_item, R.id.text_view_list_item, cities);
            mAutoCompleteSearch.setAdapter(adapter);
        }
    }
    private void add_dialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapActivity.this);
         final TextView input = new TextView(MapActivity.this);
        input.setTextSize(20);
        input.setTextColor( Color.parseColor("#000000"));
        input.setText("would you like to add "+mAutoCompleteSearch.getText().toString()+" ?!");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.gravity=Gravity.START;
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        lp.setMargins(45, 60, 15, 20);
        layout.setLayoutParams(lp);
        input.setLayoutParams(lp);
         layout.addView(input);
        alertDialog.setView(layout);

        alertDialog.setPositiveButton("Add",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SavedCities city=new SavedCities(cityname,latitude,longitude);
                        citiesList.add(city);
                        for(int i=0;i<citiesList.size();i++){
                            Log.i("citiesList",citiesList.get(i).cityName +"/ "+citiesList.get(i).citylatitude+"/ "+citiesList.get(i).citylangitude);
                        }
                         speedDialView.addActionItem(
                                new SpeedDialActionItem.Builder(View.generateViewId(), R.drawable.ic_place)
                                        .setLabel(mAutoCompleteSearch.getText().toString())
                                        .create()
                        );
                        addInJSONCityArray(city);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }
    private void hideSoftKeyboard(){
        // MapActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager iMgr = null;
        iMgr = (InputMethodManager)MapActivity.this.getSystemService( Context.INPUT_METHOD_SERVICE );
        iMgr.hideSoftInputFromWindow( mAutoCompleteSearch.getWindowToken(), 0 );
    }
    private void addInJSONCityArray(SavedCities cityToAdd){

        Gson gson = new Gson();
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(MY_PREFS_CITIES, Context.MODE_PRIVATE);

        String jsonSaved = sharedPref.getString(ADD_NEW_CITY, "");
        String jsonNewproductToAdd = gson.toJson(cityToAdd);

        JSONArray jsonArrayProduct= new JSONArray();

        try {
            if(jsonSaved.length()!=0){
                jsonArrayProduct = new JSONArray(jsonSaved);
            }
            jsonArrayProduct.put(new JSONObject(jsonNewproductToAdd));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //SAVE NEW ARRAY
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ADD_NEW_CITY, String.valueOf(jsonArrayProduct));
        editor.commit();
    }


}


