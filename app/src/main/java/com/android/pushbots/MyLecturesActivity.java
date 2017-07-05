package com.android.pushbots;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import util.CustomListAdapter;
import util.Lecture;


/**
 * This class shows all lectures that a student has subscribed for. If the student wants to
 * unsubscribe from lectures, than he has to select those and click unsubscribe-button.
 *
 * */
public class MyLecturesActivity extends NavigationBarActivity {

    ListView myLecturesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_lectures);
        super.setNavigationbarAndToolbarTitle(R.string.my_lectures);

        myLecturesListView = (ListView) findViewById(R.id.listview_my_lectures);

        //TODO: load my lectures -> load lectures from DB.
        ArrayList<Lecture> checkboxList = new ArrayList<>();

        checkboxList.add(new Lecture("Pickachu", "1"));
        checkboxList.add(new Lecture("Pickachuuuuuuu", "5"));
        checkboxList.add(new Lecture("Shiggi", "2"));
        checkboxList.add(new Lecture("Bisasam", "3"));

        // get data from the table by the ListAdapter
        final CustomListAdapter customAdapter = new CustomListAdapter(this, R.layout.listitemrow, checkboxList);

        myLecturesListView.setAdapter(customAdapter);

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
        for (int x = 0; x < myLecturesListView.getChildCount(); x++) {
            LinearLayout lectureEntry = (LinearLayout) myLecturesListView.getChildAt(x);
            CheckBox lectureCheckbox = (CheckBox) lectureEntry.getChildAt(0);

            // if lecture is selected then insert into DB.
            if (lectureCheckbox.isChecked()) {
                selectedLectures += lectureCheckbox.getText() + "\n";
            }
        }

        // if no lectures selected
        if (selectedLectures.isEmpty()) {
            Toast.makeText(this, "No lectures selected.", Toast.LENGTH_SHORT);
            return;
        }

        alertDialogBuilder.setMessage(selectedLectures);

        // set dialog selectedLectures
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        // TODO: unsubscribe lectures in DB and refresh listview
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
