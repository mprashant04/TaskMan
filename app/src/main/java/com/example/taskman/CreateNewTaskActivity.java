package com.example.taskman;

import android.app.Activity;
import android.os.Bundle;

import com.example.taskman.task_handlers.TaskHandler;

import static com.example.taskman.common.Declarations.CALLED_FROM_NOTIFICATION;

public class CreateNewTaskActivity extends Activity {

    /**
     * -----------------------------------
     * How to launch from tasker
     * -----------------------------------
     * - create "launch app" action
     * - long press on app icon, it will show all app activities, select "create new task" activity
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TaskHandler.createNewTask(this, CALLED_FROM_NOTIFICATION);
    }

}
