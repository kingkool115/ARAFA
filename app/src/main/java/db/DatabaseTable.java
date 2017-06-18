package db;

import android.provider.BaseColumns;

/**
 * This class represents different tables from database.
 */
public class DatabaseTable {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DatabaseTable() {}

    /* Inner class that defines the table contents */
    public static abstract class Lecture implements BaseColumns {
        public static final String TABLE_NAME = "lectures";

        public static final String COLUMN_LECTURE_ID = "id";
        public static final String COLUMN_LECTURE_NAME = "name";
    }
}
