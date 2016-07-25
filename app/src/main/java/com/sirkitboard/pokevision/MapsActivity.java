package com.sirkitboard.pokevision;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, AsyncCallback {
    private GoogleMap mMap;
    private ProgressDialog pDialog;

    double lat = 40.736866399, lon=-73.989969349;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        }

         new JSONParse().execute("https://pokevision.com/map/data/"+ lat +"/" + lon);
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
        new JSONParse().execute("hi");
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
        for(int i = 0; i<pokemons.length(); i++) {
            try {
                JSONObject pokemon = pokemons.getJSONObject(i);
                LatLng pokePos = new LatLng(pokemon.getDouble("latitude"), pokemon.getDouble("longitude"));
                String pokeName = getStringResourceByName("pokemonId"+pokemon.getInt("pokemonId"));
                int pokeImage = getDrawableResourceByName("pokemon_"+pokemon.getInt("pokemonId"));
                mMap.addMarker(new MarkerOptions()
                        .position(pokePos)
                        .title(pokeName)
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
}
