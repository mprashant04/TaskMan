package com.example.taskman.common;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import static com.example.taskman.common.Declarations.CALLED_FROM_MULTIPLE_EDIT_MODE;

public class TaskHandlerMultiple {
    private static List<Integer> taskIdsToEdit = new ArrayList<>();
    private static Context contextToUse = null;

    public static void editMultipleTasks(Context context, List<Integer> taskIds) {
        taskIdsToEdit.clear();
        taskIdsToEdit.addAll(taskIds);
        contextToUse = context;

        editNextTask();
    }

    public static int pendingTasksCount() {
        return taskIdsToEdit.size();
    }

    public static void editNextTask() {
        TaskHandler.editTask(contextToUse, taskIdsToEdit.remove(0), CALLED_FROM_MULTIPLE_EDIT_MODE);
    }

    public static void cancelMultiEdit() {
        taskIdsToEdit.clear();
    }
}
