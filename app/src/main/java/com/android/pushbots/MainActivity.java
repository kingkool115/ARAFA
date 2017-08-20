package com.android.pushbots;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.pushbots.push.Pushbots;

public class MainActivity extends AppCompatActivity {
    private String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toggle notification button
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle);
        //Set checked status
        toggle.setChecked(Pushbots.sharedInstance().isNotificationEnabled());
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Pushbots.sharedInstance().toggleNotifications(isChecked);
            }
        });
        Context context = getApplicationContext();
        CharSequence text = "Hello MainActiivty!";
        int duration = Toast.LENGTH_SHORT;
        //Pushbots.sharedInstance().setCustomHandler(customHandler.class);
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        String token = Pushbots.sharedInstance().getGCMRegistrationId();
        //Register for Push Notifications
        Pushbots.sharedInstance().registerForRemoteNotifications();

        //Toggle notifications
        //Pushbots.sharedInstance().toggleNotifications(true);
        //set alias
        //Pushbots.sharedInstance().setAlias("test");
        //remove alias
        //Pushbots.sharedInstance().removeAlias();
        //Tag
        //Pushbots.sharedInstance().untag("test1");
        //debug
        //Pushbots.sharedInstance().debug(false);


        /*
        try{
            JSONArray tags = new JSONArray();
            tags.put("test1");
            tags.put("test6");

            JSONObject json = new JSONObject();
            json.put("alias", "test");
            json.put("tags_add", tags);

            Pushbots.sharedInstance().update(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        */

        //Get regsitation Id
        String gcm_token = Pushbots.sharedInstance().getGCMRegistrationId();
        Log.i(LOG_TAG, "TOKEN=" + gcm_token );

        String user_id = Pushbots.sharedInstance().getUserId();
        Log.i(LOG_TAG, "USERID=" + user_id );

        //Track userId(On pushbots) and registrationId on first run
        Pushbots.sharedInstance().registered(new Pushbots.registeredHandler() {
            @Override
            public void registered(String userId, String registrationId) {
                Log.d(LOG_TAG, "userId on PushBots :" + userId);
                if (registrationId != null){
                    Log.d(LOG_TAG, "registration GCM Id:" + registrationId);
                    //Update device Data

                }
            }
        });




    }
}
