package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

import util.Answer;
import util.Lecture;
import util.Question;

/**
 * Diese Klasse hilft uns dabei Datenbankzugriffe auszuführen.
 */
public class DBHelper extends SQLiteOpenHelper{

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ars.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String BOOLEAN_TYPE = " BOOLEAN";

    /**
     * Create table 'subscriptions'
     */
    private static final String CREATE_TABLE_SUBSCRIPTION =
            "CREATE TABLE " + DatabaseTable.Subscription.TABLE_NAME + " (" +
                    DatabaseTable.Subscription.COLUMN_LECTURE_ID + INTEGER_TYPE + "," +
                    DatabaseTable.Subscription.COLUMN_LECTURE_NAME + TEXT_TYPE + " )";

    /**
     * Create table 'questions'
     */
    private static final String CREATE_TABLE_QUESTION =
            "CREATE TABLE " + DatabaseTable.Question.TABLE_NAME + " (" +
                    DatabaseTable.Question.COLUMN_ID + TEXT_TYPE + " ," +
                    DatabaseTable.Question.COLUMN_QUESTION_ID_LARS + INTEGER_TYPE + " ," +
                    DatabaseTable.Question.COLUMN_LECTURE_ID + INTEGER_TYPE + "," +
                    DatabaseTable.Question.COLUMN_SESSION_ID + TEXT_TYPE + "," +
                    DatabaseTable.Question.COLUMN_QUESTION + TEXT_TYPE + "," +
                    DatabaseTable.Question.COLUMN_IS_TR + BOOLEAN_TYPE + "," +
                    DatabaseTable.Question.COLUMN_IS_MULTI_SELECT + BOOLEAN_TYPE + "," +
                    DatabaseTable.Question.COLUMN_IS_ANSWERED + BOOLEAN_TYPE + "," +
                    DatabaseTable.Question.COLUMN_IMAGE_URL + TEXT_TYPE + " )";

    /**
     * Create table 'answers'.
     */
    private static final String CREATE_TABLE_ANSWER =
            "CREATE TABLE " + DatabaseTable.Answer.TABLE_NAME + " (" +
                    DatabaseTable.Answer.COLUMN_ID + INTEGER_TYPE + "," +
                    DatabaseTable.Answer.COLUMN_QUESTION_ID + TEXT_TYPE + "," +
                    DatabaseTable.Answer.COLUMN_ANSWER + TEXT_TYPE + " )";

    /**
     * Select one subscribed lecture.
     */
    private static final String SELECT_ONE_SUBSCRIBED_LECTURE =
            "SELECT " + DatabaseTable.Subscription.COLUMN_LECTURE_ID + ", "
                    + DatabaseTable.Subscription.COLUMN_LECTURE_NAME +
                    " FROM " + DatabaseTable.Subscription.TABLE_NAME +
                    " WHERE id = ?";

    /**
     * Select all subscribed lectures.
     * */
    private static final String SELECT_ALL_SUBSCRIBED_LECTURES =
            "SELECT " + DatabaseTable.Subscription.COLUMN_LECTURE_ID + ", "
            + DatabaseTable.Subscription.COLUMN_LECTURE_NAME +
            " FROM " + DatabaseTable.Subscription.TABLE_NAME;

    /**
    * Get a question by its id.
    * **/
    private static final String SELECT_ANSWERS_OF_QUESTION =
            "SELECT * FROM " + DatabaseTable.Answer.TABLE_NAME + " WHERE question_id = ?";

    /**
     * Get questions
     * **/
    private static final String SELECT_QUESTION_BY_ID =
            "SELECT * FROM " + DatabaseTable.Question.TABLE_NAME + " WHERE id = ?";

    /**
     * Get lecture.
     * **/
    private static final String SELECT_LECTURE_BY_ID =
            "SELECT * FROM " + DatabaseTable.Subscription.TABLE_NAME + " WHERE id = ?";

    /**
     * Get possible answers of a question.
     * **/
    private static final String SELECT_QUESTION_BY_LARS_ID =
            "SELECT * FROM " + DatabaseTable.Question.TABLE_NAME + " WHERE question_id_lars = ?";

    /**
     * Get possible answers of a question.
     * **/
    private static final String SELECT_UNANSWERED_QUESTIONS =
            "SELECT * FROM " + DatabaseTable.Question.TABLE_NAME + " WHERE is_answered = 0";

    /**
     * Drop table 'profile'
     */
    private static final String SQL_DELETE_PROFILES =
            "DROP TABLE IF EXISTS " + DatabaseTable.Subscription.TABLE_NAME;

    /**
     * Constructor
     */
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when application installed.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SUBSCRIPTION);
        db.execSQL(CREATE_TABLE_QUESTION);
        db.execSQL(CREATE_TABLE_ANSWER);
    }

    /**
     * Called when app gets updated.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_PROFILES);
        onCreate(db);
    }

    /**
     * Called when app gets downgraded.
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Subscribe for a lecture.
     */
    public void subscribeForLecture(String id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insertValues = new ContentValues();
        insertValues.put(DatabaseTable.Subscription.COLUMN_LECTURE_ID, id);
        insertValues.put(DatabaseTable.Subscription.COLUMN_LECTURE_NAME, name);
        db.insert(DatabaseTable.Subscription.TABLE_NAME, null, insertValues);
        db.close();
    }

    /**
     * Unsubscribe from lecture.
     *
     * @param lectureIds List of lecture ids which should be removed.
     * */
    public void unsubscribeFromLecture(List<String> lectureIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        // remove lecture from subscription table
        List<Integer> deletedQuestionIds = new LinkedList<>();
        for (String lectureId : lectureIds) {
            // remove lecture from subscription table
            db.delete(DatabaseTable.Subscription.TABLE_NAME, "id=?",new String[]{lectureId});
            // remove questions related to that lecture
            int questionId = db.delete(DatabaseTable.Question.TABLE_NAME, "lecture_id=?",new String[]{lectureId});
            deletedQuestionIds.add(questionId);
        }
        // remove answers related to deleted questions
        for (int questionId : deletedQuestionIds) {
            db.delete(DatabaseTable.Answer.TABLE_NAME, "question_id=?",new String[]{"" + questionId});
        }
        db.close();
    }

    /**
     * Get all subscribed lectures of the user.
     *
     * @return a list of subscribed lectures.
     * */
    public List<Lecture> getSubscribedLectures() {
        List<Lecture> result = new LinkedList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL_SUBSCRIBED_LECTURES, null);
        try {
            while (cursor.moveToNext()) {
                int lectureId = cursor.getInt(cursor.getColumnIndex(DatabaseTable.Subscription.COLUMN_LECTURE_ID));
                String lectureName = cursor.getString(cursor.getColumnIndex(DatabaseTable.Subscription.COLUMN_LECTURE_NAME));
                result.add(new Lecture(lectureName, lectureId));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    /**
     * Get a lecture by its id.
     *
     * @param id of the lecture.
     **/
    public Lecture getLectureyId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_LECTURE_BY_ID, new String[] {"" + id});
        try {
            while (cursor.moveToNext()) {
                int lectureId = cursor.getInt(cursor.getColumnIndex(DatabaseTable.Subscription.COLUMN_LECTURE_ID));
                String name = cursor.getString(cursor.getColumnIndex(DatabaseTable.Subscription.COLUMN_LECTURE_NAME));
                return new Lecture(name, lectureId);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    /**
     * Get a question by its id.
     *
     * @param id of the question.
     **/
    public Question getQuestionById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUESTION_BY_ID, new String[] {id});
        try {
            while (cursor.moveToNext()) {
                String questionId = cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_ID));
                int questionIdLars = cursor.getInt(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_QUESTION_ID_LARS));
                String question = cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_QUESTION));
                String sessionId = cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_SESSION_ID));
                int lectureId = cursor.getInt(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_LECTURE_ID));
                boolean isTextResponse = 1 == cursor.getInt(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_IS_TR));
                boolean isMultiSelect = 1 == cursor.getInt(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_IS_MULTI_SELECT));
                boolean isAnswered = 1 == cursor.getInt(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_IS_ANSWERED));
                String imageURL = cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_IMAGE_URL));
                return new Question(questionId, questionIdLars, lectureId, sessionId, question, isTextResponse, isMultiSelect, isAnswered, imageURL);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    /**
     * Get all questions which are not answered yet.
     *
     * @return a list with Question objects.
     * */
    public List<Question> getUnansweredQuestions() {
        List<Question> result = new LinkedList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_UNANSWERED_QUESTIONS, null);
        try {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_ID));
                int lectureId = cursor.getInt(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_LECTURE_ID));
                String question = cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_QUESTION));
                String imageUrl = cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_IMAGE_URL));
                boolean isTr = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_IS_TR)));
                result.add(new Question(id, lectureId, question, imageUrl, isTr));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    /**
     * Get a question by its id.
     *
     * @param id of the question.
     **/
    public Question getQuestionByLarsId(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUESTION_BY_LARS_ID, new String[] {id});
        try {
            while (cursor.moveToNext()) {
                String questionId = cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_ID));
                int questionIdLars = cursor.getInt(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_QUESTION_ID_LARS));
                String question = cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_QUESTION));
                String sessionId = cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_SESSION_ID));
                int lectureId = cursor.getInt(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_LECTURE_ID));
                boolean isTextResponse = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_IS_TR)));
                boolean isMultiSelect = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_IS_MULTI_SELECT)));
                boolean isAnswered = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_IS_ANSWERED)));
                String imageURL = cursor.getString(cursor.getColumnIndex(DatabaseTable.Question.COLUMN_IMAGE_URL));
                return new Question(questionId, questionIdLars, lectureId, sessionId, question, isTextResponse, isMultiSelect,isAnswered, imageURL);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    /**
     * Save received question into db.
     * */
    public void receiveQuestion(String questionId, int questionIdLars, int lectureId, String sessionId, String question, int isTr,
                                int is_multi_select, String imageURL) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insertValues = new ContentValues();
        insertValues.put(DatabaseTable.Question.COLUMN_ID, questionId);
        insertValues.put(DatabaseTable.Question.COLUMN_QUESTION_ID_LARS, questionIdLars);
        insertValues.put(DatabaseTable.Question.COLUMN_LECTURE_ID, lectureId);
        insertValues.put(DatabaseTable.Question.COLUMN_QUESTION, question);
        insertValues.put(DatabaseTable.Question.COLUMN_SESSION_ID, sessionId);
        insertValues.put(DatabaseTable.Question.COLUMN_IS_TR, isTr);    // boolean
        insertValues.put(DatabaseTable.Question.COLUMN_IS_MULTI_SELECT, is_multi_select);    // boolean
        insertValues.put(DatabaseTable.Question.COLUMN_IS_ANSWERED, 0);
        insertValues.put(DatabaseTable.Question.COLUMN_IMAGE_URL, imageURL);
        db.insert(DatabaseTable.Question.TABLE_NAME, null, insertValues);
        db.close();
    }

    /**
     * Set is_answered=true in questions table when student send his answer.
     * */
    public void questionAnswered(String questionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseTable.Question.COLUMN_IS_ANSWERED, 1);
        db.update(DatabaseTable.Question.TABLE_NAME, cv, "id = ?", new String[]{"" + questionId});
        db.close();
    }

    /**
     * Insert Answers into Database.
     * */
    public void receiveAnswers(List<Answer> answerList) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Answer answer : answerList) {
            ContentValues insertValues = new ContentValues();
            insertValues.put(DatabaseTable.Answer.COLUMN_ID, answer.getId());
            insertValues.put(DatabaseTable.Answer.COLUMN_QUESTION_ID, answer.getQuestionId());
            insertValues.put(DatabaseTable.Answer.COLUMN_ANSWER, answer.getAnswer());
            db.insert(DatabaseTable.Answer.TABLE_NAME, null, insertValues);
        }
        db.close();
    }

    /**
     * Get a list of all possible multiple choice answers of a question.
     *
     * @param questionId if of the question.
     * @return a List of object Answer
     * */
    public List<Answer> getAnswersOfQuestion(String questionId ) {
        List<Answer> answers = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ANSWERS_OF_QUESTION, new String[] {questionId});
        try {
            while (cursor.moveToNext()) {
                int answerId = cursor.getInt(cursor.getColumnIndex(DatabaseTable.Answer.COLUMN_ID));
                String idOfQuestion = cursor.getString(cursor.getColumnIndex(DatabaseTable.Answer.COLUMN_QUESTION_ID));
                String answer = cursor.getString(cursor.getColumnIndex(DatabaseTable.Answer.COLUMN_ANSWER));
                answers.add(new Answer(answerId, idOfQuestion, answer));
            }
        } finally {
            cursor.close();
        }
        return answers;
    }

    /**
     * Check if subscribed for a given lecture.
     *
     * @param id check this id if available in SUBSCRIPTION table.
     * @return true if id exits in table SUBSCRIPTION, else false.
     */
    public boolean lectureExists(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ONE_SUBSCRIBED_LECTURE, new String[] {id});
        if (cursor.moveToFirst()) {
            db.close();
            return true;
        }
        db.close();
        return false;
    }
}

