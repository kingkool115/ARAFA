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

import com.android.pushbots.AnswerQuestionActivity;
import com.android.pushbots.R;
import com.pushbots.push.Pushbots;

import java.net.URI;
import java.util.List;

import db.DBHelper;

/**
 * Android RestTask (REST) from the Android Recipes book.
 */
public class RestTask extends AsyncTask<HttpUriRequest, Void, String>
{
    private static final String TAG = "AsyncRestTask";
    public static final String HTTP_RESPONSE = "httpResponse";

    // TODO: change this url
    private final String URL_LECTURES = "http://192.168.178.26:8000/lectures";
    private final String URL_ANSWER_QUESTION = "http://192.168.178.26:8000/api/answer_question";
    private final String URL_SUBSCRIBE = "http://192.168.178.26:8000/api/subscribe";
    private final String URL_UNSUBSCRIBE = "http://192.168.178.26:8000/api/unsubscribe";

    private Context mContext;
    private HttpClient mClient;
    private String mAction;

    public AlertDialog progressDialog;

    public RestTask(Context context, String action) {
        mContext = context;
        mAction = action;
        mClient = new DefaultHttpClient();
    }

    public RestTask(Context context, String action, HttpClient client) {
        mContext = context;
        mAction = action;
        mClient = client;
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
    public void unsubscribeLectures(JSONArray lectureIds) {
        subscribeAndUnsubscribe(lectureIds, false);
    }

    /**
     * Subscribe to the lectures in Webservice.
     *
     * @param lectureIds list of lectureIds which should be subscribed.
     * */
    public void subscribeLectures(JSONArray lectureIds) {
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
    public void submitAnswer(Question question, String answer, boolean is_text_response) {
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
            if (is_text_response) {
                jsonObject.put("answer", answer);
            } else {
                jsonObject.put("answer_ids", answer);
            }
            StringEntity params = new StringEntity(jsonObject.toString());
            httpPost.setEntity(params);
            execute(httpPost);
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
    public void showErrorDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Could not load lectures");
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
            // TODO handle this properly
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
            showErrorDialog("Please check your internet connection.");
        }

        if (result.contains("Answer received")) {
            AnswerQuestionActivity activity = (AnswerQuestionActivity) mContext;
            DBHelper dbHelper = new DBHelper(activity);
            dbHelper.questionAnswered(activity.getQuestion().getId());
        }

        // broadcast the completion
        mContext.sendBroadcast(intent);
    }

}
