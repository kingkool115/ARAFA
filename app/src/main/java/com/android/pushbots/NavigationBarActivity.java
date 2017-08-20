package com.android.pushbots;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * This activity is a super class of all other activities which provide the navigation bar.
 * It initializes the navigation bar and its toggle button.
 */
public class NavigationBarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Init the navigation bar and set the title of current activity.
     *
     * @param title for the toolbar.
     **/
    protected void setNavigationbarAndToolbarTitle(int title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Close navigation bar if opened and back is pressed.
     * */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Handles click on item in navigation bar.
     * */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Intent intent = null;
        if (id == R.id.find_lectures) {
            this.getApplicationContext();
            intent = new Intent(getApplicationContext(), FindLecturesActivity.class);
        } else if (id == R.id.my_lectures) {
            // TODO: open settings activity
            intent = new Intent(getApplicationContext(), MyLecturesActivity.class);
        } else if (id == R.id.multiple_choice) {
            // TODO: open settings activity
            intent = new Intent(getApplicationContext(), AnswerQuestionActivity.class);
        } else if (id == R.id.current_survey) {
            // TODO: open current survey activity
            intent = new Intent(getApplicationContext(), OpenQuestionsActivity.class);
        }
        if (intent != null) {
            this.startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
