package com.android.pushbots;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.pushbots.push.Pushbots;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import db.DBHelper;
import util.CustomListAdapter;
import util.Lecture;
import util.RestTask;


/**
 * This Class is receiving all available lectures from LARS. It displays only lectures, the user
 * haven't subscribed yet. Once the user subscribes for a lecture it disappears from that view and
 * will be visible in MyLecturesActivity. Whenever a lecture is subscribed, it will be saved into DB.
 * After Subscribe-Button is clicked, the activity will refresh the lectures in that view.
 * */
public class FindLecturesActivity extends NavigationBarActivity {

    ArrayList<Lecture> checkboxList;

    public static final String ACTION_FOR_INTENT_CALLBACK = "THIS_IS_A_UNIQUE_KEY_WE_USE_TO_COMMUNICATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_lectures);
        super.setNavigationbarAndToolbarTitle(R.string.find_lectures);

        checkboxList = new ArrayList<>();
        RestTask restTask = new RestTask(this, ACTION_FOR_INTENT_CALLBACK);
        restTask.loadLectures();
    }

    /**
     * Init subscribe button and add onClickListener to it. When this button is clicked,
     * the selected lectures will be saved to DB.
     **/
    public void clickSubscribe(View view) {
        final DBHelper dbHelper = new DBHelper(this);

        //Do stuff here
        ListView listViewLayout = (ListView) findViewById(R.id.listview_find_lectures);

        // iterate all lecture checkboxes
        List<Lecture> lecturesToSubscribe = new LinkedList<>();
        for (int i = 0; i < listViewLayout.getChildCount(); i++) {
            LinearLayout lectureEntry = (LinearLayout) listViewLayout.getChildAt(i);
            CheckBox lectureCheckbox = (CheckBox) lectureEntry.getChildAt(0);

            // if lecture is selected then insert into DB.
            if (lectureCheckbox.isChecked()) {
                String lectureId = lectureCheckbox.getTag().toString();
                String lectureName = lectureCheckbox.getText().toString();
                if (!dbHelper.lectureExists(lectureId)) {
                    lecturesToSubscribe.add(new Lecture(lectureName, Integer.parseInt(lectureId)));
                }
            }
        }

        // Tag lectures in PushBots
        JSONArray lectureIds = new JSONArray();
        for (Lecture lecture : lecturesToSubscribe) {
            lectureIds.put("" + lecture.getId());
        }
        for (Lecture lecture : dbHelper.getSubscribedLectures()) {
            lectureIds.put("" + lecture.getId());
        }
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("tags", lectureIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // resets all old tags and add new tags
        Pushbots.sharedInstance().update(jsonObject);

        // subscribe for Lectures in Webservice
        RestTask restTaskSubscribe = new RestTask(this, ACTION_FOR_INTENT_CALLBACK);
        restTaskSubscribe.subscribeLectures(lectureIds, lecturesToSubscribe);
    }

    /**
     * HTTP Response is parsed to an JSONArray of lectures. Every lecture will be added as an
     * CheckBox to the list view.
     *
     * @param response HTTP response content from server.
     * */
    private void fillLecturesList(String response) throws JSONException {
        JSONArray lecturesArray = new JSONArray(response);
        ListView listViewLectures = (ListView) findViewById(R.id.listview_find_lectures);

        // needed for refill lecture list after clicking subscribe button
        checkboxList = new ArrayList<>();

        // iterate all lectures
        for (int x = 0; x < lecturesArray.length(); x++) {
            JSONObject lecture = (JSONObject) lecturesArray.get(x);

            // if not subscribed for that lecture, then add it to the list.
            DBHelper dbHelper = new DBHelper(this);
            if (!dbHelper.lectureExists(lecture.get("id").toString())) {
                String lectureName = lecture.get("name").toString();
                int lectureId = lecture.getInt("id");
                checkboxList.add(new Lecture(lectureName, lectureId));
            }
        }

        // get data from the table by the ListAdapter
        final CustomListAdapter customAdapter = new CustomListAdapter(this, R.layout.listitemrow, checkboxList);
        listViewLectures.setAdapter(customAdapter);

        EditText filterText = (EditText) findViewById(R.id.filterLectures);
        filterText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                customAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

            @Override
            public void afterTextChanged(Editable arg0) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(ACTION_FOR_INTENT_CALLBACK));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /**
     * Our Broadcast Receiver. We get notified that the data is ready, and then we
     * put the content we receive (a string) into the TextView.
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String response = intent.getStringExtra(RestTask.HTTP_RESPONSE);
            Log.i("INFO", "RESPONSE = " + response);

            try {
                if (!response.isEmpty()) {
                    fillLecturesList(response);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
