package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

import java.util.LinkedList;
import java.util.List;

import util.Answer;
import util.Lecture;

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
                    DatabaseTable.Question.COLUMN_ID + INTEGER_TYPE + "," +
                    DatabaseTable.Question.COLUMN_LECTURE_ID + INTEGER_TYPE + "," +
                    DatabaseTable.Question.COLUMN_SESSION_ID + INTEGER_TYPE + "," +
                    DatabaseTable.Question.COLUMN_QUESTION + TEXT_TYPE + "," +
                    DatabaseTable.Question.COLUMN_IS_TR + BOOLEAN_TYPE + "," +
                    DatabaseTable.Question.COLUMN_IS_ANSWERED + BOOLEAN_TYPE + "," +
                    DatabaseTable.Question.COLUMN_IMAGE_PATH + TEXT_TYPE + " )";

    /**
     * Create table 'answers'.
     */
    private static final String CREATE_TABLE_ANSWER =
            "CREATE TABLE " + DatabaseTable.Answer.TABLE_NAME + " (" +
                    DatabaseTable.Answer.COLUMN_ID + INTEGER_TYPE + "," +
                    DatabaseTable.Answer.COLUMN_QUESTION_ID + INTEGER_TYPE + "," +
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
     * Drop table 'profile'
     */
    private static final String SQL_DELETE_PROFILES = "DROP TABLE IF EXISTS " + DatabaseTable.Subscription.TABLE_NAME;

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
     * */
    public void unsubscribeFromLecture(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DatabaseTable.Subscription.TABLE_NAME, "id=?",new String[]{id});
    }

    /**
     * Get all subscribed lectures of the user.
     *
     * @return a list of subscribed lectures.
     * */
    public List<Lecture> getSubscribedLectures() {
        List<Lecture> result = new LinkedList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ONE_SUBSCRIBED_LECTURE, null);
        try {
            while (cursor.moveToNext()) {
                String lectureId = cursor.getString(cursor.getColumnIndex(DatabaseTable.Subscription.COLUMN_LECTURE_ID));
                String lectureName = cursor.getString(cursor.getColumnIndex(DatabaseTable.Subscription.COLUMN_LECTURE_NAME));
                result.add(new Lecture(lectureName, lectureId));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    /**
     * Save received question into db.
     * */
    public void receiveQuestion(int id, int lectureId, String question, boolean isTr, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insertValues = new ContentValues();
        insertValues.put(DatabaseTable.Question.COLUMN_ID, id);
        insertValues.put(DatabaseTable.Question.COLUMN_LECTURE_ID, lectureId);
        insertValues.put(DatabaseTable.Question.COLUMN_QUESTION, question);
        insertValues.put(DatabaseTable.Question.COLUMN_IS_TR, isTr);
        insertValues.put(DatabaseTable.Question.COLUMN_IS_ANSWERED, 0);
        insertValues.put(DatabaseTable.Question.COLUMN_IMAGE_PATH, imagePath);
        db.insert(DatabaseTable.Question.TABLE_NAME, null, insertValues);
        db.close();
    }

    /**
     * Set is_answered=true in questions table when student send his answer.
     * */
    public void questionAnswered(int questionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseTable.Question.COLUMN_IS_ANSWERED, 1);
        db.update(DatabaseTable.Question.TABLE_NAME, cv, "_id = ?", new String[]{"" + questionId});
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
     * Return if Sound is turned on/off.
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

