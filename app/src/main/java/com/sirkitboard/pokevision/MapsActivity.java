package com.sirkitboard.pokevision;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sirkitboard.pokevision.util.AsyncCallback;
import com.sirkitboard.pokevision.util.JSONParse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, AsyncCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap;
    private ProgressDialog pDialog;
    GoogleApiClient mGoogleApiClient;

//    JobScheduler mJobScheduler;
    double lat = 40.736866399, lon=-73.989969349;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    enableLocation();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void enableLocation() {
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_game:
                launchBackgroundServiceActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void launchBackgroundServiceActivity() {
        Intent intent = new Intent(getApplicationContext(), BackgroundPreference.class);
        startActivity(intent);
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
        } else {
            mMap.setMyLocationEnabled(true);
            new JSONParse(this).execute("https://pokevision.com/map/data/"+ lat +"/" + lon);
        }
    }

    private String getStringResourceByName(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        return getString(resId);
    }

    private int getDrawableResourceByName(String name) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(name, "drawable", packageName);
        return resId;
    }

    public void reloadPokemon(View view) {
        Location location = mMap.getMyLocation();
        lat = location.getLatitude();
        lon = location.getLongitude();
        Log.d("Lat", lat + "");
        Log.d("Lon", lon + "");
        new JSONParse(this).execute("https://pokevision.com/map/data/"+ lat +"/" + lon);
    }

    @Override
    public void preExecute() {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Getting Data ...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    @Override
    public void asyncSuccess(JSONArray pokemons) {
        mMap.clear();
        Calendar today = Calendar.getInstance();

        for(int i = 0; i<pokemons.length(); i++) {
            try {
                JSONObject pokemon = pokemons.getJSONObject(i);
                long timeLeft = pokemon.getLong("expiration_time") - today.getTimeInMillis()/1000;
                LatLng pokePos = new LatLng(pokemon.getDouble("latitude"), pokemon.getDouble("longitude"));
                String pokeName = getStringResourceByName("pokemonId"+pokemon.getInt("pokemonId"));
                int pokeImage = getDrawableResourceByName("pokemon_"+pokemon.getInt("pokemonId"));
                mMap.addMarker(new MarkerOptions()
                        .position(pokePos)
                        .title(pokeName)
                        .snippet((timeLeft/(60)) + " min " + (timeLeft % 60) + " secs")
                        .icon(BitmapDescriptorFactory.fromResource(pokeImage)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void asyncFailure() {
        Toast.makeText(getApplicationContext(), "Pokevision Down", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void asyncCompleted() {
        pDialog.dismiss();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) throws SecurityException{
        Location location = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        lat = location.getLatitude();
        lon = location.getLongitude();

        CameraPosition camPos = new CameraPosition.Builder()
                .target(new LatLng(lat, lon))
                .zoom(15)
                .bearing(location.getBearing())
                .tilt(0)
                .build();
        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
        mMap.animateCamera(camUpd3);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
    }
}
