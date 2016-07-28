package com.sirkitboard.pokevision;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sirkitboard.pokevision.util.AsyncCallback;
import com.sirkitboard.pokevision.util.JSONParse;

import org.json.JSONArray;

public class PokeCheck extends JobService implements AsyncCallback{
    public PokeCheck() {
    }

    private double lat;
    private double lon;

    @Override
    public boolean onStartJob(JobParameters params) {
	    Log.d("Job", "Started");
        Location location = getMyLocation();
        lat = location.getLatitude();
        lon = location.getLongitude();
	    System.out.println(lat+","+lon);
	    new JSONParse(this).execute("https://pokevision.com/map/data/"+ lat +"/" + lon);
        return false;
    }


    private Location getMyLocation() throws SecurityException{
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = lm.getBestProvider(criteria, true);
            myLocation = lm.getLastKnownLocation(provider);
        }

        return myLocation;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public void preExecute() {

    }

    @Override
    public void asyncSuccess(JSONArray pokemons) {
        Log.d("Job", "Success");
        Notification noti = new Notification.Builder(getApplicationContext())
                .setContentTitle("PokeFound")
                .setContentText("Some pokemon")
                .setSmallIcon(R.drawable.pokemon_1)
                .build();

        NotificationCompat.Builder mBuilder;
        int mNotifID = 001;

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotifID, noti);
    }

    @Override
    public void asyncFailure() {

    }

    @Override
    public void asyncCompleted() {

    }
}
