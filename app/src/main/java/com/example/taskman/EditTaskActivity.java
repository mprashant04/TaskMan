package com.example.taskman;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.taskman.common.Declarations;
import com.example.taskman.common.Logs;
import com.example.taskman.db.TaskDbHelper;
import com.example.taskman.models.Task;
import com.example.taskman.models.TaskStatus;
import com.example.taskman.models.TaskTag;
import com.example.taskman.models.TaskType;
import com.example.taskman.task_handlers.NotificationHandler;
import com.example.taskman.task_handlers.TaskHandler;
import com.example.taskman.task_handlers.TaskHandlerMultiple;
import com.example.taskman.utils.DateUtils;
import com.example.taskman.utils.StringUtils;
import com.example.taskman.utils.Utils;

import java.util.Date;

import static com.example.taskman.common.Declarations.CALLED_FROM_LIST_VIEW;
import static com.example.taskman.common.Declarations.CALLED_FROM_MULTIPLE_EDIT_MODE;
import static com.example.taskman.common.Declarations.CALLED_FROM_NOTIFICATION;
import static com.example.taskman.common.Declarations.TASK_FLAG_AUDIO_ALERT;

public class EditTaskActivity extends Activity {
    private static final String BTN_TAG_TIME = "time_";
    private static final String BTN_TAG_TIME_ADD_MINUTES = "add_minutes_";
    private static final String BTN_TAG_TIME_SOMEDAY = "set_time_some_day";
    private static final String BTN_TAG_ANCHOR_DAY = "anchor_day_";

    private Task task = null;
    private String calledFrom = null;
    private final TaskDbHelper db = new TaskDbHelper(this);

    private Date anchorDate = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_task);


            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                calledFrom = extras.getString("calledFrom");
                if (CALLED_FROM_MULTIPLE_EDIT_MODE.compareTo(calledFrom) != 0) Utils.vibrate(this);

                long taskId = extras.getLong("taskId");
                task = db.get(taskId);
                updateUI();
            } else {
                Utils.alertDialog(this, "Error!!!   extras = null");
            }


            findViewById(R.id.toggleAudioAlertFlag).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    toggleFlag(TASK_FLAG_AUDIO_ALERT);
                    return true;
                }
            });

            findViewById(R.id.completeTask).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    completeTask();
                    return true;
                }
            });

            findViewById(R.id.saveTask).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Utils.toast("Updated...", v.getContext());
                    updateTask();
                    return true;
                }
            });

            findViewById(R.id.deleteTask).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteTask();
                    return true;
                }
            });

            findViewById(R.id.timeSomeDay).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onClickTimeButton(findViewById(R.id.timeSomeDay));
                    return true;
                }
            });

            // task title text changed
            ((EditText) findViewById(R.id.title)).addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    task.setTitle(StringUtils.capitalizeFirstCharacter(s.toString().trim()));
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                }
            });
        } catch (Throwable ex) {
            Logs.exception(ex);
        }
    }

    private void toggleFlag(String flagName) {
        if (task.isFlagged(flagName))
            task.removeFlag(flagName);
        else
            task.setFlag(flagName);

        updateFlagButtonsUI();
    }

    private void updateTask() {
        try {
            if (validateTask())
                saveTask("Updated");
        } catch (Throwable ex) {
            Logs.exception(ex);
        }
    }

    private void deleteTask() {
        try {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Are you sure to delete task?")
                    .setMessage("")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            task.setStatus(TaskStatus.DELETED);
                            saveTask("Deleted");
                        }
                    })
                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.dialog_button_color));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.dialog_button_color));
                }
            });

            dialog.show();
        } catch (Throwable ex) {
            Logs.exception(ex);
        }
    }

    private void completeTask() {
        try {
            if (validateTask()) {
                if (task.getType() == TaskType.ONETIME) {
                    task.setStatus(TaskStatus.DELETED);
                    saveTask("Completed");
                } else if (task.getType() == TaskType.RECURSIVE) {
                    task.setDueOn(task.getNextRecursiveDueDate());
                    saveTask(DateUtils.toReadableString(task.getDueOn()));
                }

            }
        } catch (Throwable ex) {
            Logs.exception(ex);
        }
    }

    private void saveTask(String toastMsg) {
        try {
            Utils.vibrate(this);

            task.setTag(TaskTag.getFromValue(((Spinner) findViewById(R.id.tagSpinner)).getSelectedItem().toString()));

            long rowsUpdated = db.save(task);
            if (rowsUpdated == 1) {
                Utils.toastNew(this,
                        toastMsg,
                        CALLED_FROM_MULTIPLE_EDIT_MODE.compareTo(calledFrom) == 0 ? false : true);  //in multi edit mode, keep toast duration small

                NotificationHandler.refreshAll(this);
                closeActivity();
            } else {
                Utils.alertDialog(this, "DB Save Error!!!", "Expected 1 record updated, found " + rowsUpdated + " record update...");
            }
        } catch (Throwable ex) {
            Logs.exception(ex);
        }
    }

    private void closeActivity() {
        try {

            if (CALLED_FROM_LIST_VIEW.compareTo(calledFrom) == 0) {
                TaskHandler.listTasks(this);
            } else if (CALLED_FROM_NOTIFICATION.compareTo(calledFrom) == 0) {
                this.finish();  //just close activity / screen...
            } else if (CALLED_FROM_MULTIPLE_EDIT_MODE.compareTo(calledFrom) == 0) {
                if (TaskHandlerMultiple.pendingTasksCount() > 0)
                    TaskHandlerMultiple.editNextTask(this);
                else
                    TaskHandler.listTasks(this);
            } else {
                Utils.alertDialog(this, "Invalid 'calledFrom' value (" + calledFrom + ")");
            }

        } catch (Throwable ex) {
            Logs.exception(ex);
        }
    }


    private boolean validateTask() {
        try {
            if (task.getTitle() == null || task.getTitle().trim().length() <= 0) {
                Utils.alertDialog(this, "Task title cannot be empty", "");
                return false;
            }
            if (task.getType() == null) {
                Utils.alertDialog(this, "Invalid task type (" + task.getType() + ")", "");
                return false;
            }
            return true;
        } catch (Throwable ex) {
            Logs.exception(ex);
            return false;
        }
    }

    private void updateUI() {
        try {

            //------- Task attributes --------------------------------------
            EditText title = (EditText) findViewById(R.id.title);
            title.setText(task.getTitle());

            TextView dueOn = (TextView) findViewById(R.id.dueOnText);
            dueOn.setText(DateUtils.toReadableString(task.getDueOn()));
            if (task.getType() == TaskType.RECURSIVE) {
                dueOn.setText(dueOn.getText() + " (" + task.getRecursiveDuration() + "-" + task.getRecursiveUnit().getValue() + ")");
                dueOn.setTextColor(ColorStateList.valueOf(0xffff1aff));
            }

            Spinner tagSpinner = (Spinner) findViewById(R.id.tagSpinner);
            tagSpinner.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.spinner_task_tag, TaskTag.stringValues()));
            tagSpinner.setSelection(Utils.getSpinnerIndex(tagSpinner, task.getTag().getValue()));


            if (task.getDueOn().getTime() <= new Date().getTime()) {
                //overdue task
                title.setTextColor(getResources().getColor(R.color.task_overdue));

            } else if (DateUtils.isSameDay(new Date(), task.getDueOn())) {
                //due later today
                title.setTextColor(getResources().getColor(R.color.task_today));
            }


            //------- Setup UI controls --------------------------------------
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.relative_layout);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                //------- time buttons UI --------------------------------------
                if (v instanceof Button && v.getTag() != null && v.getTag().toString().startsWith(BTN_TAG_TIME)) {
                    int hr = Integer.parseInt(v.getTag().toString().replace(BTN_TAG_TIME, ""));
                    ((Button) v).setText(hr + ":00");
                    ((Button) v).setBackgroundTintList(ColorStateList.valueOf(0xff5a685a));
                }
                //------- add time buttons UI --------------------------------------
                else if (v instanceof Button && v.getTag() != null && v.getTag().toString().startsWith(BTN_TAG_TIME_ADD_MINUTES)) {
                    int minutes = Integer.parseInt(v.getTag().toString().replace(BTN_TAG_TIME_ADD_MINUTES, ""));
                    if (minutes == 0) ((Button) v).setText("Now");
                    else if (minutes < 60) ((Button) v).setText(minutes + " min");
                    else ((Button) v).setText(((int) minutes / 60) + " hr");
                    ((Button) v).setBackgroundTintList(ColorStateList.valueOf(0xff626284));
                }
                //------- someday button UI --------------------------------------
                else if (v instanceof Button && v.getTag() != null && v.getTag().toString().equals(BTN_TAG_TIME_SOMEDAY)) {
                    ((Button) v).setText("Sumday");
                    //((Button) v).setBackgroundTintList(ColorStateList.valueOf(0xff54542b));
                }
                //------- anchor day button UI --------------------------------------
                else if (v instanceof Button && v.getTag() != null && v.getTag().toString().startsWith(BTN_TAG_ANCHOR_DAY)) {
                    Date dt = resolveAnchorDate(v.getTag().toString());
                    if (DateUtils.isSameDay(new Date(), dt))
                        ((Button) v).setText("Today");
                    else
                        ((Button) v).setText(DateUtils.format("EEE d", dt));
                }
            }

            // ------------ delete button, show only if recursive task -------------------------------------------
            if (task.getType() != TaskType.RECURSIVE) {
                findViewById(R.id.deleteTask).setVisibility(View.GONE);
            }


            // ------------ title text enable word wrap -------------------------------------------
            title.setMaxLines(Integer.MAX_VALUE);  //if set these in xml, somehow they are ignored, hence setting them in code
            title.setHorizontallyScrolling(false);

            updateSelectedAnchorDayButtonUI();
            updateFlagButtonsUI();


            // ------------ multi edit mode -------------------------------------------
            if (CALLED_FROM_MULTIPLE_EDIT_MODE.compareTo(calledFrom) == 0) {
                TextView multiEditSummary = (TextView) findViewById(R.id.multiEditSummary);
                multiEditSummary.setText("Remaining  " + (TaskHandlerMultiple.pendingTasksCount() + 1));

                multiEditSummary.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        cancelMultiEditMode();
                        return true;
                    }
                });
            }

        } catch (Throwable ex) {
            Logs.exception(ex);
        }
    }


    private void updateFlagButtonsUI() {
        try {

            ImageView flagAudio = ((ImageView) findViewById(R.id.toggleAudioAlertFlag));
            if (task.isFlagged(TASK_FLAG_AUDIO_ALERT)) {
                flagAudio.setImageResource(R.drawable.bell_filled);
                flagAudio.setImageTintList(ColorStateList.valueOf(0xffffff00));
            } else {
                flagAudio.setImageResource(R.drawable.bell);
                flagAudio.setImageTintList(ColorStateList.valueOf(0xff262626));
            }


        } catch (Throwable ex) {
            Logs.exception(ex);
        }
    }


    private void updateSelectedAnchorDayButtonUI() {
        try {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.relative_layout);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                if (v instanceof Button && v.getTag() != null && v.getTag().toString().startsWith(BTN_TAG_ANCHOR_DAY)) {
                    Date dt = resolveAnchorDate(v.getTag().toString());
                    if (DateUtils.isSameDay(anchorDate, dt)) {
                        ((Button) v).setBackgroundTintList(ColorStateList.valueOf(0xFFc45a5a));
                        ((Button) v).setTextColor(ColorStateList.valueOf(Color.WHITE));
                    } else {
                        ((Button) v).setBackgroundTintList(ColorStateList.valueOf(0xff5b728b));
                        if (DateUtils.isWeekend(dt))
                            ((Button) v).setTextColor(ColorStateList.valueOf(0xff748ba4));
                        else
                            ((Button) v).setTextColor(ColorStateList.valueOf(Color.WHITE));
                    }

                }
            }

        } catch (Throwable ex) {
            Logs.exception(ex);
        }
    }

    private Date resolveAnchorDate(String tag) {
        int days = Integer.parseInt(tag.replace(BTN_TAG_ANCHOR_DAY, ""));
        return DateUtils.addDays(DateUtils.removeTime(new Date()), days);
    }

    public void onClickTimeButton(View v) {
        boolean saveAndClose = false;
        //Toast.makeText(this, "Clicked " + v.getTag(), Toast.LENGTH_SHORT).show();

        if (v.getTag() != null) {
            if (v.getTag().toString().startsWith(BTN_TAG_TIME)) {
                //----  set specific hour ------------
                int hr = Integer.parseInt(v.getTag().toString().replace(BTN_TAG_TIME, ""));
                task.setDueOn(DateUtils.addHours(DateUtils.removeTime(anchorDate), hr));
                saveAndClose = true;
            } else if (v.getTag().toString().startsWith(BTN_TAG_TIME_ADD_MINUTES)) {
                //----  add minutes to now ------------
                int minutes = Integer.parseInt(v.getTag().toString().replace(BTN_TAG_TIME_ADD_MINUTES, ""));
                task.setDueOn(DateUtils.addMinutes(new Date(), minutes));
                saveAndClose = true;
            } else if (v.getTag().toString().equals(BTN_TAG_TIME_SOMEDAY)) {
                //----  set time = someday ------------
                task.setDueOn(Declarations.DATE_SOMEDAY);
                saveAndClose = true;
            }
        }

        if (saveAndClose) {
            //task was deleted earlier, ask if wish to restore....
            if (task.getStatus() == TaskStatus.DELETED) {
                new AlertDialog.Builder(this)
                        .setTitle("Restore this deleted task?")
                        .setMessage("")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                task.setStatus(TaskStatus.ACTIVE);
                                saveTask(DateUtils.toReadableString(task.getDueOn()) + "\n(Restored)");
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            //just save the task
            else {
                saveTask(DateUtils.toReadableString(task.getDueOn()));
            }
        }
    }

    public void onClickAnchorDayButton(View v) {
        if (v.getTag() != null) {
            if (v.getTag().toString().startsWith(BTN_TAG_ANCHOR_DAY)) {
                anchorDate = resolveAnchorDate(v.getTag().toString());
                updateSelectedAnchorDayButtonUI();
            }
        }
    }


    public void onClickDueOnText(View v) throws InterruptedException {
        if (task.getType() == TaskType.RECURSIVE) {
            //CustomDialogClass cdd = new CustomDialogClass(this);
            //cdd.show();

            Utils.infoDialog(this, "Recursive Task Config",
                    "\n" + DateUtils.format("EEE  d/MMM/yy  HH:mm", task.getRecursiveFirstDueOn()) + "    (Started on) "
                            + "\n\n" + task.getRecursiveDuration() + "-" + task.getRecursiveUnit() + "     (Frequency)"
                            + "\n\n" + DateUtils.format("EEE  d/MMM/yy  HH:mm", task.getNextRecursiveDueDate()) + "    (Next due on) ");
        }
    }


    public void onClickInformAboutLongPress(View v) {
        String msg = "Press long ";
        msg += v.getId() == R.id.completeTask ? "to complete the task..." :
                v.getId() == R.id.timeSomeDay ? "to set dueOn = never, can be used to maintain note tasks...." :
                        v.getId() == R.id.saveTask ? "to save task without changing due time..." :
                                v.getId() == R.id.toggleAudioAlertFlag ? "to toggle audio alert..." :
                                        v.getId() == R.id.multiEditSummary ? "to cancel multi-edit mode..." :
                                                "...";

        Utils.toastLong(msg, this);
    }

    public void onClickCustomAnchorDayButton(View v) {
        Utils.toast("custom anchor", this);
    }

    private void cancelMultiEditMode() {
        TaskHandlerMultiple.cancelMultiEdit();
        Utils.toastNew(this, "Cancelled...");
        closeActivity();
    }


    @Override
    public void onBackPressed() {
        //prevent back button press
        closeActivity();
    }


}
