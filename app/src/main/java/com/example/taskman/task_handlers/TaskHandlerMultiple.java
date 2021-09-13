package com.example.taskman.task_handlers;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import static com.example.taskman.common.Declarations.CALLED_FROM_MULTIPLE_EDIT_MODE;

public class TaskHandlerMultiple {
    private static List<Integer> taskIdsToEdit = new ArrayList<>();

    public static void editMultipleTasks(Activity activity, List<Integer> taskIds) {
        taskIdsToEdit.clear();
        taskIdsToEdit.addAll(taskIds);

        editNextTask(activity);
    }

    public static int pendingTasksCount() {
        return taskIdsToEdit.size();
    }

    public static void editNextTask(Activity activity) {
        TaskHandler.editTask(activity, taskIdsToEdit.remove(0), CALLED_FROM_MULTIPLE_EDIT_MODE);
    }

    public static void cancelMultiEdit() {
        taskIdsToEdit.clear();
    }
}
