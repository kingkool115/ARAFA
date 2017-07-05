package com.android.pushbots;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;

/**
 * This Activity displays all questions that was received, but not answered.
 * By clicking on one of them it opens the QuestionActivity and you can answer it.
 * */
public class CurrentSurveyActivity extends NavigationBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_survey);
        super.setNavigationbarAndToolbarTitle(R.string.current_survey);
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
}
