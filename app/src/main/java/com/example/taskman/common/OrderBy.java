package com.example.taskman.common;

import com.example.taskman.db.TaskContract;

import lombok.Getter;

public enum OrderBy {
    ID(TaskContract.TaskEntry._ID),
    LAST_UPDATED_ON(TaskContract.TaskEntry.COLUMN_NAME_LAST_UPDATED_ON),
    LAST_UPDATED_ON_DESC(TaskContract.TaskEntry.COLUMN_NAME_LAST_UPDATED_ON + " DESC"),
    CREATED_ON(TaskContract.TaskEntry.COLUMN_NAME_CREATED_ON),
    CREATED_ON_DESC(TaskContract.TaskEntry.COLUMN_NAME_CREATED_ON + " DESC"),
    DUE_ON(TaskContract.TaskEntry.COLUMN_NAME_DUE_ON + ", " + TaskContract.TaskEntry._ID),
    DUE_ON_DESC(TaskContract.TaskEntry.COLUMN_NAME_DUE_ON + " DESC, " + TaskContract.TaskEntry._ID),
    TAG(TaskContract.TaskEntry.COLUMN_NAME_TAG + ", " + TaskContract.TaskEntry._ID),
    STATUS(TaskContract.TaskEntry.COLUMN_NAME_STATUS + ", " + TaskContract.TaskEntry._ID),
    TYPE(TaskContract.TaskEntry.COLUMN_NAME_TYPE + ", " + TaskContract.TaskEntry._ID);

    @Getter
    private String value = "";

    OrderBy(String value) {
        this.value = value;
    }
}
