package com.example.taskman.common;

import java.util.Date;

public class Declarations {

    public static final String APK_VERSION = "0.70";

    public static final int MAX_TASK_NOTIFICATIONS = 10;

    public static final int NOTIFICATION_ID = 0;

    public static final String NOTIFICATION_CHANNEL_ID_TASK = "TasksChannel_" + APK_VERSION;
    public static final String NOTIFICATION_CHANNEL_ID_AUDIO_ALERT = "TaskMan_Audio_Alert_" + APK_VERSION;
    public static final String NOTIFICATION_CHANNEL_ID_NON_AUDIO_ALERT = "TaskMan_Non_Audio_Alert_" + APK_VERSION;
    public static final String NOTIFICATION_CHANNEL_ID_ERROR = "ErrorChannel_" + APK_VERSION;

    public static final String NOTIFICATION_CHANNEL_NAME_AUDIO_ALERT = "Audio Alert Channel (TaskMan)";
    public static final String NOTIFICATION_CHANNEL_NAME_NON_AUDIO_ALERT = "Non-Audio Alert Channel (TaskMan)";


    public static final String BELL_CHAR = "\uD83D\uDD14";
    public static final String BELL_CHAR_HTML = "&#128276;";

    public static final String CALLED_FROM_NOTIFICATION = "calledFromNotification";
    public static final String CALLED_FROM_LIST_VIEW = "calledFromListView";
    public static final String CALLED_FROM_MULTIPLE_EDIT_MODE = "calledFromMultipleEditMode";

    public static final String TASK_FLAG_AUDIO_ALERT = "_audio_";

    public static final Date DATE_SOMEDAY = new Date(2099 - 1900, 1, 2, 3, 4, 5);   //do NOT change this value, it's saved in DB for "someday" tasks
    public static final String NEW_TASK_TITLE = "New Task";

    public static final String ROOT_SD_FOLDER_PATH = "/storage/emulated/0/_TaskMan";

}
