package com.example.taskman;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.taskman.common.DateUtils;
import com.example.taskman.common.Logs;
import com.example.taskman.common.TaskHandler;
import com.example.taskman.db.TaskDbHelper;
import com.example.taskman.models.Task;
import com.example.taskman.models.TaskRecursiveUnit;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.example.taskman.common.Declarations.CALLED_FROM_LIST_VIEW;

public class CreateNewRecursiveTaskActivity extends Activity {

    final TaskDbHelper db = new TaskDbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_new_recursive_task);

            //--------- number pickers ----------------------
            NumberPicker npDuration = (NumberPicker) findViewById(R.id.recursiveDuration);
            npDuration.setMaxValue(999);
            npDuration.setMinValue(1);

            NumberPicker npHour = (NumberPicker) findViewById(R.id.timeHour);
            npHour.setMaxValue(23);
            npHour.setMinValue(0);

            NumberPicker npMinute = (NumberPicker) findViewById(R.id.timeMinute);
            npMinute.setMaxValue(59);
            npMinute.setMinValue(0);


            //--------- unit selector combo -----------------------------
            Spinner spinner = (Spinner) findViewById(R.id.recursiveUnit);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.recursiveTaskModeArray, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            //-------- unit selection action ----------------------------
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    updateUI();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    updateUI();
                }
            });

            //------- duration selection action ----------------------
            npDuration.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    updateUI();
                }
            });

            //------- time selection action ----------------------
            npHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    updateUI();
                }
            });
            npMinute.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    updateUI();
                }
            });

            //------- date selection action ----------------------
            ((DatePicker) findViewById(R.id.date_picker)).setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    updateUI();
                }
            });

            //------- create task button action ----------------------
            ((Button) findViewById(R.id.createNewTask)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createNewRecursiveTask();
                }
            });


            updateUI();

        } catch (Throwable ex) {
            Logs.exception(ex);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        //set default values after showing dialog
        ((Spinner) findViewById(R.id.recursiveUnit)).setSelection(0);
        ((NumberPicker) findViewById(R.id.recursiveDuration)).setValue(1);
    }

    private void updateUI() {
        Date startFrom = getSelectedDate();
        TaskRecursiveUnit unit = getSelectedUnit();
        int duration = getSelectedDuration();

        String s = "From " + DateUtils.format("EEE d/MMM/yy HH:mm", startFrom)
                + "   every " + duration + "-" + unit;

        ((TextView) findViewById(R.id.selectionInfo)).setText(s);

    }

    private int getSelectedDuration() {
        return ((NumberPicker) findViewById(R.id.recursiveDuration)).getValue();
    }

    private Date getSelectedDate() {
        DatePicker datePicker = (DatePicker) findViewById(R.id.date_picker);
        NumberPicker npHour = (NumberPicker) findViewById(R.id.timeHour);
        NumberPicker npMin = (NumberPicker) findViewById(R.id.timeMinute);
        Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth(),
                npHour.getValue(),
                npMin.getValue());
        return new Date(calendar.getTimeInMillis());
    }

    private TaskRecursiveUnit getSelectedUnit() {
        Spinner spinnerUnit = (Spinner) findViewById(R.id.recursiveUnit);
        return TaskRecursiveUnit.getFromValue(spinnerUnit.getItemAtPosition(spinnerUnit.getSelectedItemPosition()).toString().charAt(0) + "");
    }

    private void createNewRecursiveTask() {
        Task task = Task.createNewRecursive();
        task.setRecursiveFirstDueOn(getSelectedDate());
        task.setDueOn(task.getRecursiveFirstDueOn());
        task.setRecursiveDuration(getSelectedDuration());
        task.setRecursiveUnit(getSelectedUnit());

        TaskHandler.createNewTask(this, task, CALLED_FROM_LIST_VIEW);
    }


    @Override
    public void onBackPressed() {
        //reload task list window
        TaskHandler.listTasks(this);
    }
}
