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
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuCompat;
import androidx.core.view.MenuItemCompat;

import com.example.taskman.common.OrderBy;
import com.example.taskman.db.TaskContract;
import com.example.taskman.db.TaskDbHelper;
import com.example.taskman.models.Task;
import com.example.taskman.models.TaskStatus;
import com.example.taskman.task_handlers.NotificationHandler;
import com.example.taskman.task_handlers.TaskHandler;
import com.example.taskman.task_handlers.TaskHandlerMultiple;
import com.example.taskman.utils.DialogUtils;
import com.example.taskman.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.taskman.common.Declarations.CALLED_FROM_LIST_VIEW;
import static com.example.taskman.utils.DialogUtils.infoDialog;

public class MainActivity extends AppCompatActivity {

    final TaskDbHelper db = new TaskDbHelper(this);

    private String searchString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.checkExternalStorageAccess(this);

        NotificationHandler.createNotificationChannels(this);
        startForegroundService(new Intent(this, TimeService.class));  //starting as foreground service to prevent it from getting killed

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.toast("Long press to create new task...", view.getContext());
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
                                       @Override
                                       public boolean onLongClick(View v) {
                                           createNewTask();
                                           return false;
                                       }
                                   }
        );


        populateTaskList();
    }

    private void populateTaskList() {
        //toast("populating...");

        String whereClause = "";
        String orderBy = AppState.ListView.getOrderBy().getValue();

        //build where clause
        if (!AppState.ListView.isShowDeletedTasks())
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
        //Utils.toastNew(this, DateUtils.getHours().toString() );
        //System.out.println("======================================================================");
//        CustomTaskViewAdapter.test();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (menu instanceof MenuBuilder)
            ((MenuBuilder) menu).setOptionalIconsVisible(true);

        MenuCompat.setGroupDividerEnabled(menu, true);

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
        DialogUtils.toast(newText, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_toggleDeletedTasks) {
            AppState.ListView.toggleShowDeletedTasks();
            DialogUtils.toastLong(AppState.ListView.isShowDeletedTasks() ? "Showing deleted tasks" : "NOT showing deleted tasks", this);
            TaskHandler.listTasks(this);
            return true;
        } else if (id == R.id.action_toggleTime) {
            AppState.ListView.toggleShowTime();
            TaskHandler.listTasks(this);
            return true;
        } else if (id == R.id.action_create_new_recursive_task) {
            TaskHandler.createNewRecursiveTask(this);
            return true;
        } else if (id == R.id.action_run_overdue) {
            runAllOverdueTasks();
            return true;
        } else if (id == R.id.action_sortby_id) {
            changeSortOrder(OrderBy.ID);
            return true;
        } else if (id == R.id.action_sortby_tag) {
            changeSortOrder(OrderBy.TAG);
            return true;
        } else if (id == R.id.action_sortby_status) {
            changeSortOrder(OrderBy.STATUS);
            return true;
        } else if (id == R.id.action_sortby_type) {
            changeSortOrder(OrderBy.TYPE);
            return true;
        } else if (id == R.id.action_sortby_last_updated_on) {
            changeSortOrder(OrderBy.LAST_UPDATED_ON);
            return true;
        } else if (id == R.id.action_sortby_last_updated_on_desc) {
            changeSortOrder(OrderBy.LAST_UPDATED_ON_DESC);
            return true;
        } else if (id == R.id.action_sortby_created_on) {
            changeSortOrder(OrderBy.CREATED_ON);
            return true;
        } else if (id == R.id.action_sortby_created_on_desc) {
            changeSortOrder(OrderBy.CREATED_ON_DESC);
            return true;
        } else if (id == R.id.action_sortby_due_on) {
            changeSortOrder(OrderBy.DUE_ON);
            return true;
        } else if (id == R.id.action_sortby_due_on_desc) {
            changeSortOrder(OrderBy.DUE_ON_DESC);
            return true;
        } else if (id == R.id.action_reset_view) {
            AppState.ListView.reset();
            TaskHandler.listTasks(this);
            return true;
        } else if (id == R.id.action_help) {
            showHelpDialog();
            return true;
        } else if (id == R.id.action_purgeDeletedTasks) {
            DialogUtils.toastNew(this, "TODO: pending implementation...");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showHelpDialog() {
        String msg = "";
        msg += "*** Manually adjust notification config *** \n"
                + "   - Long press on task notification\n"
                + "   - Disable 'Popup on screen' \n\n\n"
                + "APK build: " + Utils.getAppBuildTimeStamp(this);
        ;

        infoDialog(this, "Help", msg);
    }

    private void changeSortOrder(OrderBy order) {
        AppState.ListView.setOrderBy(order);
        TaskHandler.listTasks(this);
    }

    private void runAllOverdueTasks() {
        List<Task> tasks = db.getActiveAndOverdue();
        if (tasks.size() > 0) {
            List<Integer> taskIds = tasks.stream().map(t -> t.getId()).collect(Collectors.toList());
            TaskHandlerMultiple.editMultipleTasks(this, taskIds);
            //this.finish();
        } else
            DialogUtils.toastLong("No overdue tasks present...", this);
    }


    private void createNewTask() {
        TaskHandler.createNewTask(this, CALLED_FROM_LIST_VIEW);
    }


    private void editTask(int taskId) {
        //this.finish();
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