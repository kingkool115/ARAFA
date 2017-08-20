package com.android.pushbots;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.pushbots.push.Pushbots;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

         // Initialize Pushbots Library
        Pushbots.sharedInstance().init(this);
        Pushbots.sharedInstance().setCustomHandler(customHandler.class);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);

        // Check if app is started for the first time
        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.d("Comments", "First time");


            // resets all tags on first start
            JSONArray tags = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            try{
                jsonObject.put("tags", tags);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Pushbots.sharedInstance().update(jsonObject);

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).commit();
        }
    }
}