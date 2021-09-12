package com.example.taskman;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import com.example.taskman.common.AppState;
import com.example.taskman.common.TaskHandler;
import com.example.taskman.common.TaskHandlerMultiple;
import com.example.taskman.common.Utils;
import com.example.taskman.db.TaskContract;
import com.example.taskman.db.TaskDbHelper;
import com.example.taskman.models.Task;
import com.example.taskman.models.TaskStatus;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.taskman.common.Declarations.CALLED_FROM_LIST_VIEW;

public class MainActivity extends AppCompatActivity {

    final TaskDbHelper db = new TaskDbHelper(this);

    private String searchString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.checkExternalStorageAccess(this);

        startForegroundService(new Intent(this, TimeService.class));  //starting as foreground service to prevent it from getting killed

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewTask();
            }
        });


        populateTaskList();
    }

    private void populateTaskList() {
        String whereClause = "";
        String orderBy = TaskContract.TaskEntry._ID;

        //build where clause
        if (!AppState.showDeletedTasksInListView)
            whereClause += TaskContract.TaskEntry.COLUMN_NAME_STATUS + "='" + TaskStatus.ACTIVE.getValue() + "'";
        if (searchString != null && searchString.trim().length() > 0)
            whereClause += (whereClause.trim().length() > 0 ? " AND " : "") + " INSTR(lower(" + TaskContract.TaskEntry.COLUMN_NAME_TITLE + "), lower('" + searchString.trim() + "')) > 0";


        List<Task> tasks = null;
        tasks = db.get(whereClause, orderBy);

        ListView taskListView = findViewById(R.id.taskListView);
        CustomTaskViewAdapter customAdapter = new CustomTaskViewAdapter(getApplicationContext(), tasks);
        taskListView.setAdapter(customAdapter);

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task item = (Task) parent.getItemAtPosition(position);
                editTask(item.getId());
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
//        Utils.toastNew(this, "Today 09:23");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                toast("Searching - " + query);
                searchString = query;
                populateTaskList();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //adapter.getFilter().filter(newText);
                if (newText == null || newText.length() == 0) {
                    toast("Search Clear....");
                    searchString = "";
                    populateTaskList();
                }
                return false;
            }
        });

        return true;
    }

    private void toast(String newText) {
        Utils.toast(newText, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_toggleDeletedTasks) {
            AppState.showDeletedTasksInListView = !AppState.showDeletedTasksInListView;
            Utils.toastLong(AppState.showDeletedTasksInListView ? "Showing deleted tasks" : "NOT showing deleted tasks", this);
            this.finish();
            TaskHandler.listTasks(this);
            return true;
        } else if (id == R.id.action_toggleTime) {
            AppState.showTimeInListView = !AppState.showTimeInListView;
            this.finish();
            TaskHandler.listTasks(this);
            return true;
        } else if (id == R.id.action_create_new_recursive_task) {
            TaskHandler.createNewRecursiveTask(this);
            return true;
        } else if (id == R.id.action_run_overdue) {
            runAllOverdueTasks();
            return true;
        } else if (id == R.id.action_purgeDeletedTasks) {
            Utils.toastNew(this, "TODO: pending implementation...");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void runAllOverdueTasks() {
        List<Task> tasks = db.getActiveAndOverdue();
        if (tasks.size() > 0) {
            List<Integer> taskIds = tasks.stream().map(t -> t.getId()).collect(Collectors.toList());
            TaskHandlerMultiple.editMultipleTasks(this, taskIds);
            this.finish();
        }
        else
            Utils.toastLong("No overdue tasks present...", this);
    }


    private void createNewTask() {
        TaskHandler.createNewTask(this, Task.createNew(), CALLED_FROM_LIST_VIEW);
    }


    private void editTask(int taskId) {
        this.finish();
        TaskHandler.editTask(this, taskId, CALLED_FROM_LIST_VIEW);
    }

    @Override
    public void onBackPressed() {
        //prevent back button press
//        if (shouldAllowBack()) {
//            super.onBackPressed();
//        } else {
//        }
        this.finish();
    }


}