package util;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.pushbots.AnswerQuestionActivity;
import com.android.pushbots.FindLecturesActivity;
import com.android.pushbots.MyLecturesActivity;
import com.android.pushbots.R;
import com.pushbots.push.Pushbots;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import db.DBHelper;

/**
 * Android RestTask (REST) from the Android Recipes book.
 */
public class RestTask extends AsyncTask<HttpUriRequest, Void, String>
{
    private static final String TAG = "AsyncRestTask";
    public static final String HTTP_RESPONSE = "httpResponse";

    // TODO: change these urls
    private final String URL_LECTURES = "http://192.168.178.26:8000/lectures";
    private final String URL_ANSWER_QUESTION = "http://192.168.178.26:8000/api/answer_question";
    private final String URL_SUBSCRIBE = "http://192.168.178.26:8000/api/subscribe";
    private final String URL_UNSUBSCRIBE = "http://192.168.178.26:8000/api/unsubscribe";

    private Context mContext;
    private HttpClient mClient;
    private String mAction;

    public AlertDialog progressDialog;

    List<String> lectureIdsToUnsubscribe;
    List<Lecture> lecturesToSubscribe;

    static enum TASK{
        LOAD_LECTURES,
        SUBSCRIBE,
        UNSUBSCRIBE,
        SUBMIT_ANSWER
    }

    TASK isWorkingOn;

    public RestTask(Context context, String action) {
        mContext = context;
        mAction = action;
        mClient = new DefaultHttpClient();
    }

    /**
     * This function sends a HTTP GET Request to load all available lectures from LARS.
     * If we receive a HTTP 200 OK, then all received lectures will be displayed in the list view.
     * Else we get an error dialog.
     * */
    public void loadLectures() {
        // the request
        try
        {
            HttpGet httpGet = new HttpGet(new URI(URL_LECTURES));
            httpGet.addHeader("Accept", "application/json");
            execute(httpGet);
            isWorkingOn = TASK.LOAD_LECTURES;
            progressDialog = getProgressDialog();
            progressDialog.show();
        }
        catch (Exception e)
        {
            Log.e("Error", e.getMessage());
        }
    }

    /**
     * Unsubscribe the lectures from Webservice.
     * */
    public void unsubscribeLectures(JSONArray lectureIds, List<String> lectureIdsToUnsubscribe) {
        isWorkingOn = TASK.UNSUBSCRIBE;
        this.lectureIdsToUnsubscribe = lectureIdsToUnsubscribe;
        subscribeAndUnsubscribe(lectureIds, false);
    }

    /**
     * Subscribe to the lectures in Webservice.
     *
     * @param lectureIds list of lectureIds which should be subscribed.
     * */
    public void subscribeLectures(JSONArray lectureIds, List<Lecture> lecturesToSubscribe) {
        isWorkingOn = TASK.SUBSCRIBE;
        this.lecturesToSubscribe = lecturesToSubscribe;
        subscribeAndUnsubscribe(lectureIds, true);
    }

    /**
     * Executes subscribe/unsubscribe http request.
     *
     * @param lectureIds lectureIds which should be subscribed/unsubscribed
     * @param subscribe true if subscribe given lectureIds, else unsubscribe them.
     * */
    private void subscribeAndUnsubscribe(JSONArray lectureIds, boolean subscribe) {
        try {
            HttpPost httpPost;
            if (subscribe) {
                httpPost = new HttpPost(new URI(URL_SUBSCRIBE));
            } else {
                httpPost = new HttpPost(new URI(URL_UNSUBSCRIBE));
            }
            httpPost.addHeader("Content-Type", "application/json");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lecture_ids", lectureIds);
            jsonObject.put("student_id", Pushbots.sharedInstance().getUserId());
            StringEntity params = new StringEntity(jsonObject.toString());
            httpPost.setEntity(params);
            execute(httpPost);
            progressDialog = getProgressDialog();
            progressDialog.show();
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }

    /**
     * Submit an answer.
     * */
    public void submitAnswer(Question question, String answer) {
        // the request
        try
        {
            HttpPost httpPost = new HttpPost(new URI(URL_ANSWER_QUESTION));
            httpPost.addHeader("Content-Type", "application/json");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lecture_id", question.getLectureId());
            jsonObject.put("student_id", Pushbots.sharedInstance().getUserId());
            jsonObject.put("session_id", question.getSessionId());
            jsonObject.put("question_id", question.getQuestionIdLarsId());
            jsonObject.put("is_text_response", question.isTr());
            jsonObject.put("answer", answer);
            StringEntity params = new StringEntity(jsonObject.toString());
            httpPost.setEntity(params);
            execute(httpPost);
            isWorkingOn = TASK.SUBMIT_ANSWER;
        }
        catch (Exception e)
        {
            Log.e("Error", e.getMessage());
        }
    }

    /**
     * Creates a dialog with a progressbar while loading all lectures from web server.
     * */
    private AlertDialog getProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // Get the layout inflater
        Activity activity = (Activity) mContext;
        LayoutInflater inflater = activity.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View layout = inflater.inflate(R.layout.dialog_progress, null);
        builder.setView(layout);

        return builder.create();
    }

    /**
     * Creates a dialog with an error message if could not get lectures from web server.
     *
     * @param errorMessage display the received error message from server.
     * */
    public void showErrorDialog(String title, String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);
        builder.setMessage(errorMessage);
        builder.create();
        builder.show();
    }

    @Override
    protected String doInBackground(HttpUriRequest... params) {
        try {
            HttpUriRequest request = params[0];
            HttpResponse serverResponse = mClient.execute(request);
            BasicResponseHandler handler = new BasicResponseHandler();
            return handler.handleResponse(serverResponse);
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Log.i(TAG, "RESULT = " + result);
        Intent intent = new Intent(mAction);
        intent.putExtra(HTTP_RESPONSE, result);

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        String response = intent.getStringExtra(RestTask.HTTP_RESPONSE);
        Log.i("INFO", "RESPONSE = " + response);

        if (response.isEmpty()) {
            switch (isWorkingOn) {
                case SUBMIT_ANSWER:
                    AnswerQuestionActivity answerQuestionActivity = (AnswerQuestionActivity) mContext;
                    Toast.makeText(answerQuestionActivity, "Could not send answer", Toast.LENGTH_SHORT);
                    break;
                case LOAD_LECTURES:
                    showErrorDialog("Could not load lectures", "Please check your internet connection.");
                    break;
                case SUBSCRIBE:
                    showErrorDialog("Could not subscribe", "Please check your internet connection.");
                    break;
                case UNSUBSCRIBE:
                    showErrorDialog("Could not unsubscribe", "Please check your internet connection.");
                    break;
            }
        } else {
            DBHelper dbHelper;
            switch (isWorkingOn) {
                case SUBMIT_ANSWER:
                    // update db when webservice received answer
                    AnswerQuestionActivity answerQuestionActivity = (AnswerQuestionActivity) mContext;
                    dbHelper = new DBHelper(answerQuestionActivity);
                    dbHelper.questionAnswered(answerQuestionActivity.getQuestion().getId());
                    break;
                case SUBSCRIBE:
                    // update db after successful subscription
                    FindLecturesActivity findLecturesActivity = (FindLecturesActivity) mContext;
                    dbHelper = new DBHelper(findLecturesActivity);
                    for (Lecture lecture : lecturesToSubscribe) {
                        dbHelper.subscribeForLecture("" + lecture.getId(), lecture.getName());
                    }
                    // reload lectures in view
                    RestTask restTaskLoadLectures = new RestTask(findLecturesActivity,
                                                                    findLecturesActivity.ACTION_FOR_INTENT_CALLBACK);
                    restTaskLoadLectures.loadLectures();
                    Toast.makeText(findLecturesActivity, "Subscribed", Toast.LENGTH_SHORT);
                    break;
                case UNSUBSCRIBE:
                    // update db after successful unsubscription
                    MyLecturesActivity myLecturesActivity = (MyLecturesActivity) mContext;
                    dbHelper = new DBHelper(myLecturesActivity);
                    dbHelper.unsubscribeFromLecture(lectureIdsToUnsubscribe);

                    // Untag in Pushbots instance to avoid pushed questions related to this lecture.
                    for (String lectureId : lectureIdsToUnsubscribe) {
                        Pushbots.sharedInstance().untag(lectureId);
                    }
                    // empty and refill list with subscribed lectures entries.
                    ArrayList<Lecture> checkboxList = new ArrayList<>();
                    ListView myLecturesListView = (ListView) myLecturesActivity.findViewById(R.id.listview_my_lectures);
                    CustomListAdapter customListAdapter = new CustomListAdapter(myLecturesActivity,
                            R.layout.listitemrow, checkboxList);
                    myLecturesListView.setAdapter(customListAdapter);
                    myLecturesActivity.fillLecturesList();
                    Intent newMyLecturesActivity = new Intent(myLecturesActivity, MyLecturesActivity.class);
                    myLecturesActivity.startActivity(newMyLecturesActivity);
                    myLecturesActivity.finish();
                    break;
                case LOAD_LECTURES:
                    // broadcast the completion
                    mContext.sendBroadcast(intent);
                    break;
            }
        }
    }
}
