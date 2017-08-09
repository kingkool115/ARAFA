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
    public static abstract class Subscription implements BaseColumns {
        public static final String TABLE_NAME = "subscriptions";

        public static final String COLUMN_LECTURE_ID = "id";
        public static final String COLUMN_LECTURE_NAME = "lecture_name";
    }

    public static abstract class Question implements BaseColumns {
        public static final String TABLE_NAME = "questions";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_LECTURE_ID = "lecture_id";
        public static final String COLUMN_SESSION_ID = "session_id";
        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_IS_TR = "is_tr";
        public static final String COLUMN_IS_ANSWERED = "is_answered";
        public static final String COLUMN_IMAGE_PATH = "image_path";
    }

    public static abstract class Answer implements BaseColumns {
        public static final String TABLE_NAME = "answers";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_QUESTION_ID = "question_id";
        public static final String COLUMN_ANSWER = "answer";
    }
}
