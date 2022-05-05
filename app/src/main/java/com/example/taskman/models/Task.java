package com.example.taskman.models;

import com.example.taskman.AppState;
import com.example.taskman.common.Declarations;
import com.example.taskman.utils.DateUtils;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

import static com.example.taskman.common.Declarations.BELL_CHAR;

public class Task {


    @Getter
    private int id;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String notes;

    @Getter
    @Setter
    private Date dueOn;

    @Getter
    @Setter
    private int recursiveDuration;

    @Getter
    @Setter
    private TaskRecursiveUnit recursiveUnit;

    @Getter
    @Setter
    private Date recursiveFirstDueOn;

    @Getter
    @Setter
    private TaskStatus status;

    @Getter
    @Setter
    private TaskTag tag;

    @Getter
    @Setter
    private TaskType type;

    @Getter
    @Setter
    private String xtraFlags;  //TODO for 🔔 etc.

    @Getter
    @Setter
    private Date createdOn;

    @Getter
    @Setter
    private Date lastUpdatedOn;

    public Task(int id) {
        this.id = id;
    }

    public boolean isFlaggedForAudioAlert() {
        return isFlagged(Declarations.TASK_FLAG_AUDIO_ALERT);
    }

    public boolean isFlagged(String flagName) {
        return xtraFlags.contains(flagName);
    }

    public void setFlag(String flagName) {
        if (!isFlagged(flagName))
            xtraFlags += flagName;
    }

    public void removeFlag(String flagName) {
        xtraFlags = xtraFlags.replace(flagName, "");
    }

    public static Task createNew() {
        // do NOT keep any null values in fields
        Task t = new Task(0);
        t.setTitle(Declarations.NEW_TASK_TITLE);
        t.setDueOn(new Date());
        t.setNotes("");

        t.setStatus(TaskStatus.ACTIVE);
        t.setTag(TaskTag.PERSONAL);
        t.setType(TaskType.ONETIME);

        t.setRecursiveDuration(1);
        t.setRecursiveFirstDueOn(new Date());
        t.setRecursiveUnit(TaskRecursiveUnit.DAY);

        t.setXtraFlags("");

        return t;
    }

    public static Task createNewRecursive() {
        Task t = createNew();
        t.setType(TaskType.RECURSIVE);
        return t;
    }

    public Date getNextRecursiveDueDate() {
        if (this.getType() == TaskType.RECURSIVE) {
            Date d = this.getRecursiveFirstDueOn();
            while (d.getTime() <= new Date().getTime()) {
                if (this.getRecursiveUnit() == TaskRecursiveUnit.DAY)
                    d = DateUtils.addDays(d, this.getRecursiveDuration());
                else if (this.getRecursiveUnit() == TaskRecursiveUnit.MONTH)
                    d = DateUtils.addMonths(d, this.getRecursiveDuration());
            }
            return d;
        }
        return null;
    }

    public String getFormattedTitle() {
        return this.getTitle()
                + (this.isFlaggedForAudioAlert() ? " " + BELL_CHAR : "")
                + (AppState.ListView.isShowTaskId() ? "   [" + this.getId() + "]" : "");
    }
}
