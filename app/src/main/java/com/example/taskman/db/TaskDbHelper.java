package com.example.taskman.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.taskman.models.Task;
import com.example.taskman.models.TaskRecursiveUnit;
import com.example.taskman.models.TaskStatus;
import com.example.taskman.models.TaskTag;
import com.example.taskman.models.TaskType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskDbHelper extends SQLiteOpenHelper {


    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "tasks.db";

    public TaskDbHelper(Context context) {
        //super(context, DATABASE_NAME, null, DATABASE_VERSION);
        super(new DatabaseContext(context), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long create(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = buildContentValues(task, true);

        long newRowId = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }

    public long save(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = buildContentValues(task, false);

        String selection = TaskContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = {task.getId() + ""};

        long rowsUpdated = db.update(TaskContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        db.close();
        return rowsUpdated;
    }

    private ContentValues buildContentValues(Task task, boolean populateCreatedOn) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DUE_ON, task.getDueOn().getTime());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_NOTES, task.getNotes());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_TAG, task.getTag().getValue());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_REC_DURATION, task.getRecursiveDuration());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_REC_UNIT, task.getRecursiveUnit().getValue());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_REC_FIRST_DUE_ON, task.getRecursiveFirstDueOn().getTime());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_TYPE, task.getType().getValue());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_STATUS, task.getStatus().getValue());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_XTRA_FLAGS, task.getXtraFlags());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_LAST_UPDATED_ON, new Date().getTime());

        if (populateCreatedOn)
            values.put(TaskContract.TaskEntry.COLUMN_NAME_CREATED_ON, new Date().getTime());

        return values;
    }


    public Task get(long id) {
        List<Task> tasks = get(TaskContract.TaskEntry._ID + "=" + id, null);
        if (tasks.size() == 1)
            return tasks.get(0);

        return null;
    }

    public List<Task> getAll() {
        return get(null, TaskContract.TaskEntry._ID);
    }

    public List<Task> getActive() {
        return get(TaskContract.TaskEntry.COLUMN_NAME_STATUS + "='" + TaskStatus.ACTIVE.getValue() + "'", TaskContract.TaskEntry._ID);
    }

    public List<Task> getActiveAndOverdue() {
        return get(TaskContract.TaskEntry.COLUMN_NAME_STATUS + "='" + TaskStatus.ACTIVE.getValue()
                        + "' AND " + TaskContract.TaskEntry.COLUMN_NAME_DUE_ON + " <= " + new Date().getTime(),
                TaskContract.TaskEntry._ID);
    }


    public List<Task> get(String whereClause, String orderBy) {
        List<Task> tasks = new ArrayList<Task>();

        String selectQuery = "SELECT _id, title, notes, type, tag, due_on, rec_duration, rec_first_due_on, rec_unit, status, xtra_flags, createdOn, lastUpdatedOn  FROM " + TaskContract.TaskEntry.TABLE_NAME;
        if (whereClause != null && whereClause.length() > 0) selectQuery += " WHERE " + whereClause;
        if (orderBy != null && orderBy.length() > 0) selectQuery += " ORDER BY " + orderBy;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Task t = new Task(Integer.parseInt(cursor.getString(0)));
                t.setTitle(cursor.getString(1));
                t.setNotes(cursor.getString(2));
                t.setType(TaskType.getFromValue(cursor.getString(3)));
                t.setTag(TaskTag.getFromValue(cursor.getString(4)));
                t.setDueOn(new Date(Long.parseLong(cursor.getString(5))));
                t.setRecursiveDuration(Integer.parseInt(cursor.getString(6)));
                t.setRecursiveFirstDueOn(new Date(Long.parseLong(cursor.getString(7))));
                t.setRecursiveUnit(TaskRecursiveUnit.getFromValue(cursor.getString(8)));
                t.setStatus(TaskStatus.getFromValue(cursor.getString(9)));
                t.setXtraFlags(cursor.getString(10));
                t.setCreatedOn(new Date(Long.parseLong(cursor.getString(11))));
                t.setLastUpdatedOn(new Date(Long.parseLong(cursor.getString(12))));

                tasks.add(t);
            } while (cursor.moveToNext());
        }

        return tasks;
    }


}