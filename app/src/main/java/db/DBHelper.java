package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Diese Klasse hilft uns dabei Datenbankzugriffe auszuführen.
 */
public class DBHelper extends SQLiteOpenHelper{

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "lecture.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";

    /**
     * Create table 'profile'
     */
    private static final String CREATE_TABLE_LECTURES =
            "CREATE TABLE " + DatabaseTable.Lecture.TABLE_NAME + " (" +
                    DatabaseTable.Lecture.COLUMN_LECTURE_NAME + TEXT_TYPE + "," +
                    DatabaseTable.Lecture.COLUMN_LECTURE_ID + INTEGER_TYPE + " )";

    /**
     * Select all gadgets.
     */
    private static final String SELECT_ALL_SUBSCRIBED_LECTURES =
            "SELECT " + DatabaseTable.Lecture.COLUMN_LECTURE_ID+ ", "
                    + DatabaseTable.Lecture.COLUMN_LECTURE_NAME +
                    " FROM " + DatabaseTable.Lecture.TABLE_NAME +
                    " WHERE id = ?";
    /**
     * Drop table 'profile'
     */
    private static final String SQL_DELETE_PROFILES = "DROP TABLE IF EXISTS " + DatabaseTable.Lecture.TABLE_NAME;

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
        db.execSQL(CREATE_TABLE_LECTURES);
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
     * Add settings rows to SETTINGS Table.
     */
    public void insertLecture(String id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insertValues = new ContentValues();
        insertValues.put(DatabaseTable.Lecture.COLUMN_LECTURE_ID, id);
        insertValues.put(DatabaseTable.Lecture.COLUMN_LECTURE_NAME, name);
        db.insert(DatabaseTable.Lecture.TABLE_NAME, null, insertValues);
        db.close();
    }

    /**
     * Return if Sound is turned on/off.
     */
    public boolean lectureExists(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL_SUBSCRIBED_LECTURES, new String[] {id});
        if (cursor.moveToFirst()) {
            db.close();
            return true;
        }
        db.close();
        return false;
    }

    // TODO: remove lecture, wenn sie unsubscribed wird.
}

