package com.android.pushbots;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.pushbots.push.Pushbots;
import com.pushbots.push.utils.PBConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import db.DBHelper;
import util.Answer;
import util.Question;


public class customHandler extends BroadcastReceiver
{
    private String TAG = "PB3:CustomHandler:";

    int lastQuestionId;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Log.d(TAG, "action=" + action);

        DBHelper dbHelper = new DBHelper(context);

        //Bundle containing all fields of the opened notification
        Bundle bundle = null;

        // Handle Push Message when opened
        if (action.equals(PBConstants.EVENT_MSG_OPEN)) {
            bundle = intent.getExtras().getBundle(PBConstants.EVENT_MSG_OPEN);
        // Handle Push Message when received.
        } else if (action.equals(PBConstants.EVENT_MSG_RECEIVE)) {
            bundle = intent.getExtras().getBundle(PBConstants.EVENT_MSG_RECEIVE);
        }

        // received question as JSONObject
        JSONObject jsonQuestion = null;
        String questionId;
        String sessionId = null;
        Question question = null;
        try {
            jsonQuestion = new JSONObject(bundle.getString("question"));
            sessionId = bundle.getString("session_id");
            questionId = jsonQuestion.getString("id");
            question = dbHelper.getQuestionByLarsId(questionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Handle Push Message when opened
        if (action.equals(PBConstants.EVENT_MSG_OPEN)) {

            //Record opened notification
            Pushbots.PushNotificationOpened(context, bundle);

            Log.i(TAG, "User clicked notification with Message: " + bundle.get("message"));

            // create new intent for activity that will show the question
            Intent answerQuestionIntent = null;
            if (question.isTr()) {
                // TODO: activity for text response
                // answerQuestionIntent = new Intent(context, AnswerQuestionActivity.class);
            } else {
                answerQuestionIntent = new Intent(context, AnswerQuestionActivity.class);
            }
            answerQuestionIntent.putExtra("question_id", question.getId());

            //Open activity or URL with pushData.
            if(answerQuestionIntent != null) {
                answerQuestionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(answerQuestionIntent);
            }

        } else if(action.equals(PBConstants.EVENT_MSG_RECEIVE)){

            Log.i(TAG, "User received notification with Message: " + bundle.get("message"));

            try {
                // save question into DB
                int newQuestionId = (int) dbHelper.receiveQuestion(
                        Integer.parseInt(jsonQuestion.getString("id")),
                        Integer.parseInt(jsonQuestion.getString("lecture_id")),
                        sessionId,
                        jsonQuestion.getString("question"),
                        Integer.parseInt(jsonQuestion.getString("is_text_response")),
                        Integer.parseInt(jsonQuestion.getString("is_multi_select")),
                        jsonQuestion.getString("image_path")
                );

                lastQuestionId = newQuestionId;

                // save related answers into DB
                JSONArray jsonArray = new JSONArray(bundle.getString("answers"));
                List<Answer> answers = new LinkedList<>();
                for (int x = 0; x < jsonArray.length(); x++) {
                    JSONObject jsonAnswer = (JSONObject)jsonArray.get(x);
                    int id = Integer.parseInt(jsonAnswer.getString("id"));
                    String answer = jsonAnswer.getString("answer");
                    answers.add(new Answer(id, newQuestionId, answer));
                }
                dbHelper.receiveAnswers(answers);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
