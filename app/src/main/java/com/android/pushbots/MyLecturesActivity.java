package com.android.pushbots;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.pushbots.push.Pushbots;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import db.DBHelper;
import util.CustomListAdapter;
import util.Lecture;


/**
 * This class shows all lectures that a student has subscribed for. If the student wants to
 * unsubscribe from lectures, than he has to select those and click unsubscribe-button.
 *
 * */
public class MyLecturesActivity extends NavigationBarActivity {

    ListView myLecturesListView;
    ArrayList<Lecture> checkboxList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pushbots.sharedInstance().registerForRemoteNotifications();
        setContentView(R.layout.activity_my_lectures);
        super.setNavigationbarAndToolbarTitle(R.string.my_lectures);
        myLecturesListView = (ListView) findViewById(R.id.listview_my_lectures);

        checkboxList = new ArrayList<>();

        // get data from the table by the ListAdapter
        final CustomListAdapter customAdapter = new CustomListAdapter(this, R.layout.listitemrow, checkboxList);

        myLecturesListView.setAdapter(customAdapter);
        fillLecturesList();
    }

    /**
     * Fill lectures list with subscribed lectures from DB.
     * */
    public void fillLecturesList() {
        DBHelper dbHelper = new DBHelper(this);
        List<Lecture> subscribedLectures = dbHelper.getSubscribedLectures();
        for (Lecture lecture : subscribedLectures) {
            checkboxList.add(lecture);
        }
    }

    /**
     * A AlertDialog will popup when you want to unsubscribe from some lectures.
     * */
    public void openAlertDialog(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Doy you really want to unsubscribe these lectures?");

        // set selectedLectures with marked lectures
        String selectedLectures = "";
        List<String> selectedLectureIds = new LinkedList<>();
        for (int x = 0; x < myLecturesListView.getChildCount(); x++) {
            LinearLayout lectureEntry = (LinearLayout) myLecturesListView.getChildAt(x);
            CheckBox lectureCheckbox = (CheckBox) lectureEntry.getChildAt(0);

            // if lecture is selected then insert into DB.
            if (lectureCheckbox.isChecked()) {
                selectedLectureIds.add(lectureCheckbox.getTag().toString());
                selectedLectures += lectureCheckbox.getText() + "\n";
            }
        }

        // if no lectures selected
        if (selectedLectures.isEmpty()) {
            Toast.makeText(this, "No lectures selected.", Toast.LENGTH_SHORT);
            return;
        }

        alertDialogBuilder.setMessage(selectedLectures);

        final List<String> lectureIdsToUnsubscribe = selectedLectureIds;
        final DBHelper dbHelper = new DBHelper(this);
        // set dialog selectedLectures
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dbHelper.unsubscribeFromLecture(lectureIdsToUnsubscribe);
                        // Untag in Pushbots instance to avoid pushed questions related to this lecture.
                        for (String lectureId : lectureIdsToUnsubscribe) {
                            Pushbots.sharedInstance().untag("lectureId");
                        }
                        // empty and refill list with subscribed lectures entries.
                        checkboxList = new ArrayList<>();
                        myLecturesListView.setAdapter(
                                                    new CustomListAdapter(MyLecturesActivity.this,
                                                            R.layout.listitemrow, checkboxList));
                        fillLecturesList();
                        Toast.makeText(MyLecturesActivity.this, "Subscribed", Toast.LENGTH_SHORT);
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
