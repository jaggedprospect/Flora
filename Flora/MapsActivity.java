package com.jagged.flora;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.Maps;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private static final String TAG = MapsActivity.class.getName();
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyBZ8qut_lyV4RhEvsVqHRkhEwUGxurNaVA";
    private static final String SEARCH_TYPE = "flower-shop";
    private static final int REQUEST_CODE = 101;
    private static final int MAP_ZOOM = 15;
    // TODO: Temporary constant for search radius -- Implement user defined radius.
    private static final String SEARCH_RADIUS = "5000";

    private GoogleMap mMap;
    private Location mLocation;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        FloatingActionButton backButton = (FloatingActionButton)findViewById(R.id.fab);
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MapsActivity.this,ShopFinderActivity.class);
                startActivity(intent);
                finish();
            }
        });
        fusedLocationProviderClient =LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        LatLng latLng;
        String id = getIntent().getStringExtra("id");
        if(id.equals("from_select_location")){
            latLng = getLocationFromAddress(getIntent().getStringExtra("address"));
        }else{
            latLng=new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        }
        // Set working Location
        mLocation = new Location(LocationManager.GPS_PROVIDER);
        mLocation.setLatitude(latLng.latitude);
        mLocation.setLongitude(latLng.longitude);
        // Invoke process for finding nearby flower shops!
        StringBuilder sb = new StringBuilder(generateQuery());
        PlacesTask placesTask = new PlacesTask();
        placesTask.execute(sb.toString());
        // Show location on Google Map with marker and zoom camera
        mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM));
    }

    private void fetchLocation(){
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>(){
            @Override
            public void onSuccess(Location location){
                if(location != null){
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(),currentLocation.getLatitude()+
                            ","+currentLocation.getLongitude(),Toast.LENGTH_SHORT).show();
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment=(SupportMapFragment)getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    assert mapFragment != null;
                    mapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestcode,@NonNull String[] permissions,@NonNull int[] grantResults){
        switch(requestcode){
            case REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    fetchLocation();
                break;
        }
    }

    private LatLng getLocationFromAddress(String s){
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng point = null;
        try{
            address = coder.getFromLocationName(s,5);
            if(address == null) return null;
            Address location = address.get(0);
            point = new LatLng(location.getLatitude(),location.getLongitude());
        }catch(Exception e){
            Log.w(TAG,"Error occurred while getting location from address.");
        }
        return point;
    }

    /**
     * This is the beginning of code originally provided by Daniel Nugent found on Stack Overflow.
     * (https://stackoverflow.com/questions/30161395/im-trying-to-search-nearby-places-such-as-banks-restaurants-atms-inside-the-d)
     * Any significant changes that have been made are denoted with the appropriate comments.
     * I do not present this as my own original work -- although the original code provided may be
     * altered, the principle functionality remains the same.
     */
    // Renamed from 'sbMethod()' -- Generates query string for API to use
    private StringBuilder generateQuery(){
        // Renamed from 'mLatitude', 'mLongitude'
        double lat = mLocation.getLatitude();
        double lng = mLocation.getLongitude();

        // Refactored key String information as Constants
        StringBuilder sb =new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location="+lat+","+lng);
        sb.append("&radius=5000");
        sb.append("&types="+SEARCH_TYPE);
        sb.append("&sensor=true");
        sb.append("&key="+GOOGLE_MAPS_API_KEY);

        Log.d("Map","api: " + sb.toString());
        return sb;
    }

    // Unedited -- AsyncTask class used for queries to Google Places API
    private class PlacesTask extends AsyncTask<String,Integer,String>{

        public String data;

        @Override
        protected String doInBackground(String... strings){
            try{
                data = downloadUrl(strings[0]);
            }catch(Exception e){
                Log.w(TAG, "Error occurred within PlacesTask.");
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result){
            ParserTask pt = new ParserTask();
            pt.execute(result);
        }

        private String downloadUrl(String string) throws IOException{
            String data = "";
            InputStream is = null;
            HttpURLConnection connection = null;
            try{
                URL url =new URL(string);
                // Create http connection
                connection = (HttpURLConnection)url.openConnection();
                // Connect to URL
                connection.connect();
                // Read data from URL
                is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while((line = br.readLine()) != null) sb.append(line);
                data = sb.toString();
                br.close();
            }catch(Exception e){
                Log.w(TAG,"Error occurred within PlacesTask - downloadUrl().");
            }finally{
                is.close();
                connection.disconnect();
            }
            return data;
        }
    }

    private class ParserTask extends AsyncTask<String,Integer,List<HashMap<String,String>>>{

        // Renamed from 'jObject'
        JSONObject jsonObject;

        @Override
        protected List<HashMap<String,String>> doInBackground(String... strings){
            List<HashMap<String,String>> places = null;
            Place_JSON placeJson = new Place_JSON();
            try{
                jsonObject = new JSONObject(strings[0]);
                places = placeJson.parse(jsonObject);
            }catch(Exception e){
                Log.w(TAG,"Error occurred within ParserTask.");
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){
            Log.d("Map","list size: " + list.size());
            mMap.clear(); // clear existing markers
            for(int i = 0; i < list.size(); i++){
                // Renamed from 'markerOptions' -- Create marker
                MarkerOptions mo = new MarkerOptions();
                // Renamed from 'hmPlace' -- Get a place from places list
                HashMap<String,String> placeMap = list.get(i);
                // Get coordinates of place
                double lat = Double.parseDouble(placeMap.get("lat"));
                double lng = Double.parseDouble(placeMap.get("lng"));
                // Get name of place
                String name = placeMap.get("place_name");
                Log.d("Map","place: " + name);
                // Get vicinity of place
                String vicinity = placeMap.get("vicinity");
                LatLng latLng = new LatLng(lat,lng);
                mo.position(latLng);
                mo.title(name + " : " + vicinity);
                // Changed hue from Magenta to Azure
                mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                Marker marker = mMap.addMarker(mo);
;            }
        }
    }

    private class Place_JSON {

        public List<HashMap<String,String>> parse(JSONObject jsonObject){
            JSONArray places = null;
            try{
                places = jsonObject.getJSONArray("results");
            }catch(JSONException e){
                Log.w(TAG,"Error occurred within Place_JSON - parse().");
            }
            return getPlaces(places);
        }

        private List<HashMap<String,String>> getPlaces(JSONArray places){
            int count = places.length();
            List<HashMap<String,String>> placesList = new ArrayList<>();
            HashMap<String,String> placeMap;
            for(int i = 0; i < count; i++){
                try{
                    placeMap = getPlace((JSONObject)places.get(i));
                    placesList.add(placeMap);
                }catch(Exception e){
                    Log.w(TAG,"Error occurred within Place_JSON - getPlaces().");
                }
            }
            return placesList;
        }

        private HashMap<String,String> getPlace(JSONObject place){
            HashMap<String,String> placeMap = new HashMap<>();
            String name = null,vicinity = null, lat = null,lng = null,ref = null;
            try{
                if(!place.isNull("name")) name = place.getString("name");
                if(!place.isNull("vicinity")) vicinity = place.getString("vicinity");
                lat = place.getJSONObject("geometry").getJSONObject("location").getString("lat");
                lng = place.getJSONObject("geometry").getJSONObject("location").getString("lng");
                ref = place.getString("reference");
                placeMap.put("name",name);
                placeMap.put("vicinity",vicinity);
                placeMap.put("lat",lat);
                placeMap.put("lng",lng);
                placeMap.put("ref",ref);
            }catch(JSONException e){
                Log.w(TAG,"Error occurred within Place_JSON - getPlace().");
            }
            return placeMap;
        }
    }
    /**
     * End modified code.
     */
}