package com.android.pushbots;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import db.DBHelper;
import util.Lecture;
import util.Question;

/**
 * This Activity displays all questions that was received, but not answered.
 * By clicking on one of them it opens the QuestionActivity and you can answer it.
 * */
public class OpenQuestionsActivity extends NavigationBarActivity {

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_questions);
        super.setNavigationbarAndToolbarTitle(R.string.open_questions);
        dbHelper = new DBHelper(this);

        // HashMap<lectureId, List<Question>>
        HashMap<Integer, List<Question>> lectureWithQuestionsMap = getLectureWithQuestionsHashMap();

        // fill layout with lecture headers and question buttons.
        fillLayout(lectureWithQuestionsMap);
    }

    /**
     * Get a hash map with lecture:questions relation.
     *
     * @return HashMap<lectureId, List<Question>>.
     * */
    private HashMap<Integer, List<Question>> getLectureWithQuestionsHashMap() {

        HashMap<Integer, List<Question>> lectureWithQuestionsMap = new HashMap<>();
        List<Question> unansweredQuestions = dbHelper.getUnansweredQuestions();
        List<Lecture> lectures = dbHelper.getSubscribedLectures();

        for (Question question : unansweredQuestions) {
            for (Lecture lecture : lectures) {
                if (lecture.getId() == question.getLectureId()) {
                    // if no lecture entry in hash map -> create new one with new list
                    if (lectureWithQuestionsMap.get(lecture.getId()) == null) {
                        List<Question> questions = new LinkedList<>();
                        questions.add(question);
                        lectureWithQuestionsMap.put(lecture.getId(), questions);
                        // if lecture entry already exists in hash map -> add question to existing  list
                    } else {
                        List<Question> questions = lectureWithQuestionsMap.get(lecture.getId());
                        questions.add(question);
                        lectureWithQuestionsMap.put(lecture.getId(), questions);
                    }
                }
            }
        }
        return lectureWithQuestionsMap;
    }

    /**
     * Fill Activity with lecture headers and question buttons.
     * */
    private void fillLayout(HashMap<Integer, List<Question>> lectureWithQuestionsMap) {
        LinearLayout openQuestionsLayout =  (LinearLayout) findViewById(R.id.openQuestionsLayout);
        // iterate hash map
        for (Map.Entry<Integer, List<Question>> entry : lectureWithQuestionsMap.entrySet()) {
            Integer lectureId = entry.getKey();
            Lecture lecture = dbHelper.getLectureyId(lectureId);

            // set lecture headline
            TextView lectureHeader = new TextView(this);
            lectureHeader.setText(lecture.getName());
            lectureHeader.setTextSize(20);
            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            headerParams.setMargins(0, 40, 0, 40);
            lectureHeader.setGravity(Gravity.CENTER);
            lectureHeader.setLayoutParams(headerParams);
            openQuestionsLayout.addView(lectureHeader);

            // set question buttons related to the lecture
            List<Question> questions = entry.getValue();
            for (final Question q : questions) {
                Button button = new Button(this);
                button.setText(q.getQuestion());
                button.setTag(q.getId());
                button.setTextSize(15);
                button.setBackground(getResources().getDrawable(R.drawable.question_button));
                // set random background color
                GradientDrawable gradientDrawable = (GradientDrawable) button.getBackground();
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(200), rnd.nextInt(200), rnd.nextInt(200));
                gradientDrawable.setColor(color);
                button.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                buttonParams.setMargins(10, 20, 10, 20);
                button.setLayoutParams(buttonParams);

                // set onclicklistener
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // Perform action on click
                        Intent questionActivity;
                        questionActivity = new Intent(OpenQuestionsActivity.this, AnswerQuestionActivity.class);
                        questionActivity.putExtra("question_id", q.getId());
                        questionActivity.putExtra("question_id", q.getId());
                        OpenQuestionsActivity.this.startActivity(questionActivity);
                    }
                });
                openQuestionsLayout.addView(button);
            }
        }
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
