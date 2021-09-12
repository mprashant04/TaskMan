package com.example.taskman.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.taskman.CreateNewRecursiveTaskActivity;
import com.example.taskman.MainActivity;
import com.example.taskman.db.TaskDbHelper;
import com.example.taskman.models.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskHandler {



    public static void editTask(Context context, long taskId, String calledFrom) {
        Intent intent = Utils.createEditTaskIntent(context, taskId, calledFrom);
        context.startActivity(intent);
    }

    public static void listTasks(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }


    public static void createNewRecursiveTask(Context context) {
        Intent intent = new Intent(context, CreateNewRecursiveTaskActivity.class);
        context.startActivity(intent);
    }

    public static void createNewTask(Activity context, Task task, String calledFrom) {
        TaskDbHelper db = new TaskDbHelper(context);
        long newTaskId = -1;

        newTaskId = db.create(task);
        NotificationHandler.refreshAll(context);
        context.finish();
        TaskHandler.editTask(context, newTaskId, calledFrom);
    }
}
