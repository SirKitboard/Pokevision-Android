package com.sirkitboard.pokevision.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.sirkitboard.pokevision.MapsActivity;

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

/**
 * Created by abalwani on 25/07/2016.
 */
public class JSONParse extends AsyncTask<String, String, JSONObject> {
    private AsyncCallback callback;

    public JSONParse(AsyncCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callback.preExecute();
    }


    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
    @Override
    protected JSONObject doInBackground(String... args) {
        JSONObject json = null;
        try {
            URL url = new URL(args[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                json = new JSONObject(readStream(in));
            } catch(JSONException e) {
                Log.e("ERRRRORRRRR", "idk");
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Getting JSON from URL
        return json;
    }
    @Override
    protected void onPostExecute(JSONObject json) {
        callback.asyncCompleted();
        JSONArray pokemon;
        try {
            pokemon = json.getJSONArray("pokemon");
            callback.asyncSuccess(pokemon);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            callback.asyncFailure();
            e.printStackTrace();
        }
    }
}
