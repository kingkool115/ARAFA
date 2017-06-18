package com.android.pushbots;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBHelper;


/**
 * This Class is receiving all available lectures from LARS. It displays only lectures, the user
 * haven't subscribed yet. Once the user subscribes for a lecture it disappears from that view and
 * will be visible in MyLecturesActivity. Whenever a lecture is subscribed, it will be saved into DB.
 * After Subscribe-Button is clicked, the activity will refresh the lectures in that view.
 * */
public class FindLecturesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // TODO: change this url
    String URL_LECTURES = "http://192.168.178.26:8000/api/lectures";
    AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_lectures);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar );
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.find_lectures);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        initSubscribeButton();
        loadLectures();
    }

    /**
     * Init subscribe button and add onClickListener to it. When this button is clicked,
     * the selected lectures will be saved to DB.
     * */
    private void initSubscribeButton() {
        Button subscribeButton = (Button) findViewById(R.id.subscribe_button);
        final DBHelper dbHelper = new DBHelper(this);

        subscribeButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                //Do stuff here
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout_lectures);

                // iterate all lecture radioButtons
                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    RadioButton lectureRadio = (RadioButton) linearLayout.getChildAt(i);

                    // if lecture is selected then insert into DB.
                    if (lectureRadio.isChecked()) {
                        String lectureId = lectureRadio.getTag().toString();
                        String lectuteName = lectureRadio.getText().toString();
                        if (!dbHelper.lectureExists(lectureId)) {
                            dbHelper.insertLecture(lectureId, lectuteName);
                        }
                    }
                }

                // reload lectures in view
                loadLectures();
            }
        });
    }

    /**
     * This function sends a HTTP GET Request to load all available lectures from LARS.
     * If we receive a HTTP 200 OK, then all received lectures will be displayed in the list view.
     * Else we get an error dialog.
     * */
    private void loadLectures() {
        showLoadingDialog();
        RequestQueue queue = Volley.newRequestQueue(this);

        final TextView textView = (TextView) findViewById(R.id.filterLectures);

        StringRequest request = new StringRequest
            (Request.Method.GET, URL_LECTURES,  new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    try {
                        fillLecturesList(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    progressDialog.dismiss();
                    showErrorDialog(error.getMessage());
            }
        });
        queue.add(request);
    }

    /**
     * HTTP Response is parsed to an JSONArray of lectures. Every lecture will be added as an
     * RadioButton to the list view.
     *
     * @param response HTTP response content from server.
     * */
    private void fillLecturesList(String response) throws JSONException {
        JSONArray lecturesArray = new JSONArray(response);
        LinearLayout linearLayoutLectures = (LinearLayout) findViewById(R.id.linearLayout_lectures);

        // needed for refill lecture list after clicking subscribe button
        if (linearLayoutLectures.getChildCount() > 0) {
            linearLayoutLectures.removeAllViews();
        }

        // iterate all lectures
        for (int x = 0; x < lecturesArray.length(); x++) {
            JSONObject lecture = (JSONObject) lecturesArray.get(x);

            // if not subscribed for that lecture, then add it to the list.
            DBHelper dbHelper = new DBHelper(this);
            if (!dbHelper.lectureExists(lecture.get("id").toString())) {

                // create a new RadioButton
                RadioButton lectureEntry = new RadioButton(this);
                lectureEntry.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                lectureEntry.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                lectureEntry.setPadding(0, 5, 0, 5);

                lectureEntry.setText(lecture.get("name").toString());
                lectureEntry.setTag(lecture.get("id").toString());
                linearLayoutLectures.addView(lectureEntry);
            }
        }
    }

    /**
     * Creates a dialog with a progressbar while loading all lectures from web server.
     * */
    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View layout = inflater.inflate(R.layout.dialog_progress, null);
        builder.setView(layout);

        progressDialog = builder.create();
        progressDialog.show();
    }

    /**
     * Creates a dialog with an error message if could not get lectures from web server.
     *
     * @param errorMessage display the received error message from server.
     * */
    private void showErrorDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Could not load lectures");
        builder.setMessage(errorMessage);
        builder.create();
        builder.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_manage) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
