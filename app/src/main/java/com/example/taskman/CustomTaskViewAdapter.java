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

import com.example.taskman.models.Task;
import com.example.taskman.models.TaskStatus;
import com.example.taskman.models.TaskType;
import com.example.taskman.utils.DateUtils;
import com.example.taskman.utils.DialogUtils;

import java.util.Date;
import java.util.List;

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
        title.setText(task.getFormattedTitle());

        dueOn.setText(DateUtils.toReadableString(task.getDueOn())
                + (task.getType() == TaskType.RECURSIVE ? " (" + task.getRecursiveDuration() + "-" + task.getRecursiveUnit().getValue() + ")" : ""));

        updateSectionHeader(i, view);

        //----------- task icon -------------------------------
        // icon.setImageResource(R.drawable.notification_record);
        //setMargin(icon, 0, 20, 48, 0);
        //icon.getLayoutParams().height = icon.getLayoutParams().width = 20;
        if (task.getType() == TaskType.RECURSIVE) {
            icon.setImageTintList(ColorStateList.valueOf(0xffff00ff));
        } else if (task.getType() == TaskType.ONETIME) {
            icon.setImageTintList(ColorStateList.valueOf(0xff333333));
        } else {
            DialogUtils.alertDialog(view.getContext(), "ERROR!!! invalid task type - " + task.getType());
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
        } else if (DateUtils.isSomeDay(task.getDueOn())) {
            //someday
            title.setTextColor(Color.WHITE);
        } else {
            //due tomorrow onwards
            title.setTextColor(Color.GRAY);
        }

        //------- time show/hide --------------------------------------
        dueOn.setVisibility(AppState.ListView.isShowTime() ? View.VISIBLE : View.GONE);
        // to increase row height little bit when time not being shown
        if (!AppState.ListView.isShowTime())
            setMargin(view.findViewById(R.id.bottomGap), 0, 0, 0, 120);


        return view;
    }

    private void updateSectionHeader(int idx, View view) {
        //todo logic based on current order by clause

        boolean dayChanged = isSectionChanged(idx);


        if (dayChanged) {
            TextView header = (TextView) view.findViewById(R.id.sectionHeader);
            TextView headerNote = (TextView) view.findViewById(R.id.sectionHeaderNote);

            header.getLayoutParams().height = 90;
            headerNote.getLayoutParams().height = 90;

            String headerString = getSectionHeader(idx);
            header.setText(headerString);
            if (idx == 0)
                headerNote.setText("[ " + getSectionHeaderNote() + " ]");  //show sort by info only on first section bar
        }
    }

    private String getSectionHeaderNote() {
        switch (AppState.ListView.getOrderBy()) {
            case ID:
                return context.getString(R.string.sort_by_id);
            case CREATED_ON:
                return context.getString(R.string.sort_by_created_on);
            case CREATED_ON_DESC:
                return context.getString(R.string.sort_by_created_on_desc);
            case DUE_ON:
                return context.getString(R.string.sort_by_due_on);
            case DUE_ON_DESC:
                return context.getString(R.string.sort_by_due_on_desc);
            case LAST_UPDATED_ON:
                return context.getString(R.string.sort_by_last_updated_on);
            case LAST_UPDATED_ON_DESC:
                return context.getString(R.string.sort_by_last_updated_on_desc);
            case STATUS:
                return context.getString(R.string.sort_by_status);
            case TAG:
                return context.getString(R.string.sort_by_tag);
            case TYPE:
                return context.getString(R.string.sort_by_type);
            default:
                return "error???";
        }
    }

    private boolean isSectionChanged(int idx) {
        switch (AppState.ListView.getOrderBy()) {
            case ID:
                return false;   //no section grouping in this case

            default:
                return (idx == 0 || getSectionHeader(idx).compareToIgnoreCase(getSectionHeader(idx - 1)) != 0);
        }
    }

    private String getSectionHeader(int idx) {
        switch (AppState.ListView.getOrderBy()) {
            case ID:
                return "Error?????";   //not expecting function call for this condition

            case LAST_UPDATED_ON:
            case LAST_UPDATED_ON_DESC:
                return formatSectionHeaderDate(tasks.get(idx).getLastUpdatedOn());

            case CREATED_ON:
            case CREATED_ON_DESC:
                return formatSectionHeaderDate(tasks.get(idx).getCreatedOn());

            case DUE_ON:
            case DUE_ON_DESC:
                return formatSectionHeaderDate(tasks.get(idx).getDueOn());

            case TAG:
                return tasks.get(idx).getTag().toString();

            case TYPE:
                return tasks.get(idx).getType().toString();

            case STATUS:
                return tasks.get(idx).getStatus().toString();

        }
        return "Error???";
    }

    private static String formatSectionHeaderDate(Date date) {
        String s = "Error???";

        if (DateUtils.isSomeDay(date))
            s = "SOMEDAY";
        else if (DateUtils.isSameDay(DateUtils.addDays(new Date(), -1), date))
            s = "yesterday";
        else if (DateUtils.isSameDay(new Date(), date))
            s = "TODAY";
        else if (date.getTime() <= DateUtils.addMonths(DateUtils.getThisYearEnd(), -12).getTime())
            s = "last  year  backwards";
        else if (date.getTime() < DateUtils.addMonths(DateUtils.getThisMonthBeginning(), -1).getTime())
            s = "earlier  this  year";
        else if (date.getTime() < DateUtils.getThisMonthBeginning().getTime())
            s = "last  month";
        else if (date.getTime() <= DateUtils.addDays(DateUtils.getThisSunday(), -14).getTime())
            s = "earlier  this  month";
        else if (date.getTime() <= DateUtils.addDays(DateUtils.getThisSunday(), -7).getTime())
            s = "last  WEEK";
        else if (date.getTime() < DateUtils.removeTime(DateUtils.addDays(new Date(), -1)).getTime())
            s = "earlier  this  WEEK";
        else if (DateUtils.isSameDay(DateUtils.addDays(new Date(), 1), date))
            s = "TOMORROW";
        else if (date.getTime() <= DateUtils.getThisSunday().getTime())
            s = "THIS  WEEK";
        else if (date.getTime() <= DateUtils.addDays(DateUtils.getThisSunday(), 7).getTime())
            s = "NEXT  WEEK";
        else if (date.getTime() <= DateUtils.getThisMonthEnd().getTime())
            s = "THIS  MONTH";
        else if (date.getTime() <= DateUtils.addMonths(DateUtils.getThisMonthEnd(), 1).getTime())
            s = "NEXT  MONTH";
        else if (date.getTime() <= DateUtils.getThisYearEnd().getTime())
            s = "THIS  YEAR";
        else if (date.getTime() > DateUtils.getThisYearEnd().getTime())
            s = "NEXT  YEAR  ONWARDS";
        else
            s = "Error????";

        return s.toUpperCase();
    }

    private void setMargin(View view, int left, int right, int top, int bottom) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(left, top, right, bottom);
        view.setLayoutParams(lp);
    }

//    public static void test() {
//        Date d = new Date(2019- 1900, 1, 1);
//
//        for (int i = 0; i < 1500; i++) {
//            System.out.println("************  "+ formatSectionHeaderDate(d) + "   " + d.toString() );
//            d = DateUtils.addDays(d, 1);
//        }
//    }
}