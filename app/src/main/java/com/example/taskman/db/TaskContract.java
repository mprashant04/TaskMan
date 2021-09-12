package com.example.taskman.db;

import android.provider.BaseColumns;

public class TaskContract {


    private TaskContract() {
    }

    public static class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";

        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DUE_ON = "due_on";
        public static final String COLUMN_NAME_NOTES = "notes";
        public static final String COLUMN_NAME_TAG = "tag";
        public static final String COLUMN_NAME_REC_DURATION = "rec_duration";
        public static final String COLUMN_NAME_REC_UNIT = "rec_unit";
        public static final String COLUMN_NAME_REC_FIRST_DUE_ON = "rec_first_due_on";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_XTRA_FLAGS = "xtra_flags";
        public static final String COLUMN_NAME_CREATED_ON = "createdOn";
        public static final String COLUMN_NAME_LAST_UPDATED_ON = "lastUpdatedOn";
    }
}
