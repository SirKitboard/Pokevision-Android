package com.sirkitboard.pokevision;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sirkitboard.pokevision.util.AsyncCallback;
import com.sirkitboard.pokevision.util.JSONParse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PokeCheck extends JobService implements AsyncCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    public PokeCheck() {
    }

    private double lat;
    private double lon;


    GoogleApiClient mGoogleApiClient;

    SharedPreferences prefs;
    SharedPreferences.Editor prefEditor;
    boolean[] pokemonPref;

    @Override
    public boolean onStartJob(JobParameters params) {
	    Log.d("Job", "Started");

        prefs = getApplicationContext().getSharedPreferences("pokemon_notif_prefs", Context.MODE_PRIVATE);
        String currentPrefs = prefs.getString("notif_prefs", null);
        prefEditor = prefs.edit();
        if (currentPrefs == null) {
            pokemonPref = new boolean[151];
            prefEditor.putString("notif_prefs", boolArrayToString(pokemonPref));
            prefEditor.apply();
        } else {
            pokemonPref = stringToBoolArray(currentPrefs);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGoogleApiClient.connect();
        return false;
    }

    private String boolArrayToString(boolean[] bools) {
        String[] strings = new String[bools.length];
        for (int i = 0; i < bools.length; i++) {
            strings[i] = (bools[i] ? "1" : "0");
        }
        return TextUtils.join(",", strings);
    }

    private boolean[] stringToBoolArray(String string) {
        String[] strings = string.split(",");
        boolean[] bools = new boolean[strings.length];
        for (int i = 0; i < strings.length; i++) {
            String bool = strings[i];
            bools[i] = bool.equalsIgnoreCase("1");
        }
        return bools;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public void preExecute() {

    }

    private String getStringResourceByName(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        return getString(resId);
    }

    @Override
    public void asyncSuccess(JSONArray pokemons) {
        Log.d("Job", "Success");
        ArrayList<String> pokeNames = new ArrayList<>();

        for(int i = 0; i<pokemons.length(); i++) {
            try {
                JSONObject pokemon = pokemons.getJSONObject(i);
                int pokeID = pokemon.getInt("pokemonId");
                if(!pokemonPref[pokeID - 1]) {
                    continue;
                }

                Location location = new Location("poke");
                location.setLatitude(pokemon.getDouble("latitude"));
                location.setLongitude(pokemon.getDouble("longitude"));
                Location me = new Location("me");

                me.setLatitude(lat);
                me.setLongitude(lon);

                float distance = location.distanceTo(me);
                if(distance < 500) {
                    String pokeName = getStringResourceByName("pokemonId"+pokemon.getInt("pokemonId"));
                    System.out.println(pokeName);
                    if(!pokeNames.contains(pokeName))
                        pokeNames.add(getStringResourceByName("pokemonId"+pokemon.getInt("pokemonId")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Notification noti = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("PokeFound")
                .setContentText(TextUtils.join(",", pokeNames))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(TextUtils.join(", ", pokeNames)))
                .setSmallIcon(R.drawable.pokemon_1)
                .build();

        NotificationCompat.Builder mBuilder;
        int mNotifID = 001;

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotifID, noti);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void asyncFailure() {

    }

    @Override
    public void asyncCompleted() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) throws SecurityException{
        Location location = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        lat = location.getLatitude();
        lon = location.getLongitude();
        new JSONParse(this).execute("https://pokevision.com/map/data/"+ lat +"/" + lon);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
