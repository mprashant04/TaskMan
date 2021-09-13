package com.example.taskman.task_handlers;

import android.app.Activity;
import android.content.Intent;

import com.example.taskman.CreateNewRecursiveTaskActivity;
import com.example.taskman.MainActivity;
import com.example.taskman.utils.Utils;
import com.example.taskman.db.TaskDbHelper;
import com.example.taskman.models.Task;

public class TaskHandler {

    public static void editTask(Activity activity, long taskId, String calledFrom) {
        handleActivity(activity);

        Intent intent = Utils.createEditTaskIntent(activity, taskId, calledFrom);
        activity.startActivity(intent);
    }


    public static void listTasks(Activity activity) {
        handleActivity(activity);

        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }


    public static void createNewRecursiveTask(Activity activity) {
        handleActivity(activity);

        Intent intent = new Intent(activity, CreateNewRecursiveTaskActivity.class);
        activity.startActivity(intent);
    }

    public static void createNewTask(Activity activity, Task task, String calledFrom) {
        TaskDbHelper db = new TaskDbHelper(activity);
        long newTaskId = -1;

        newTaskId = db.create(task);
        NotificationHandler.refreshAll(activity);
        TaskHandler.editTask(activity, newTaskId, calledFrom);
    }

    private static void handleActivity(Activity context) {
        context.finishAffinity();  //close all activities in tree first.... so they won't be launched on back button press
    }
}
