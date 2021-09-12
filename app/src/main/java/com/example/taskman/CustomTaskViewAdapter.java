package com.example.taskman;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.taskman.common.AppState;
import com.example.taskman.common.DateUtils;
import com.example.taskman.common.Declarations;
import com.example.taskman.common.Utils;
import com.example.taskman.models.Task;
import com.example.taskman.models.TaskStatus;
import com.example.taskman.models.TaskType;

import java.util.Date;
import java.util.List;

import static com.example.taskman.common.Declarations.BELL_CHAR;

public class CustomTaskViewAdapter extends BaseAdapter {
    Context context;
    List<Task> tasks;
    LayoutInflater inflter;

    public CustomTaskViewAdapter(Context applicationContext, List<Task> tasks) {
        this.context = applicationContext;
        this.tasks = tasks;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Task getItem(int i) {
        return tasks.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.activity_listview, null);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView dueOn = (TextView) view.findViewById(R.id.dueOn);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);

        Task task = tasks.get(i);
        title.setText(task.getTitle()
                //+ " (" + task.getId() + ")"
                + (task.isFlagged(Declarations.TASK_FLAG_AUDIO_ALERT) ? " " + BELL_CHAR : "")  //🔔 bell icon
                + (task.getStatus() == TaskStatus.DELETED ? " [deleted] " : "")
        );
        dueOn.setText(DateUtils.toReadableString(task.getDueOn())
                + (task.getType() == TaskType.RECURSIVE ? " (" + task.getRecursiveDuration() + "-" + task.getRecursiveUnit().getValue() + ")" : ""));

        //----------- task icon -------------------------------
        icon.setImageResource(R.drawable.notification_record);
        setMargin(icon, 0, 10, 48, 0);
        icon.getLayoutParams().height = icon.getLayoutParams().width = 20;
        if (task.getType() == TaskType.RECURSIVE) {
            icon.setImageTintList(ColorStateList.valueOf(0xffff00ff));
        } else if (task.getType() == TaskType.ONETIME) {
            icon.setImageTintList(ColorStateList.valueOf(0xff333333));
        } else {
            Utils.alertDialog(view.getContext(), "ERROR!!! invalid task type - " + task.getType());
        }


        //----------- set color based on due date -----------------------------
        if (task.getStatus() == TaskStatus.DELETED) {
            //deleted task
            title.setTextColor(ColorStateList.valueOf(0xFF595959));
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else if (task.getDueOn().getTime() <= new Date().getTime()) {
            //overdue task
            title.setTextColor(view.getResources().getColor(R.color.task_overdue));
        } else if (DateUtils.isSameDay(new Date(), task.getDueOn())) {
            //due later today
            title.setTextColor(view.getResources().getColor(R.color.task_today));
        } else if (task.getDueOn().getTime() == Declarations.DATE_SOMEDAY.getTime()) {
            //someday
            title.setTextColor(Color.WHITE);
        } else {
            //due tomorrow onwards
            title.setTextColor(Color.GRAY);
        }

        //------- time show/hide --------------------------------------
        dueOn.setVisibility(AppState.showTimeInListView ? View.VISIBLE : View.GONE);
        // to increase row height little bit when time not being shown
        if (!AppState.showTimeInListView) setMargin(view.findViewById(R.id.bottomGap), 0, 0, 0, 120);


        return view;
    }

    private void setMargin(View view, int left, int right, int top, int bottom) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(left, top, right, bottom);
        view.setLayoutParams(lp);
    }
}