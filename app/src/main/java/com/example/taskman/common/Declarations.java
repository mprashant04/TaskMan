package com.example.taskman.common;

import java.util.Date;

public class Declarations {

    public static final String APK_VERSION = "0.93";

    public static final int MAX_TASK_NOTIFICATIONS = 10;

    public static final String  NOTIFICATION_TAG_TASKS = "MyTasks";
    
    public static final String NOTIFICATION_CHANNEL_ID_TASK = "TasksChannel_" + APK_VERSION;
    public static final String NOTIFICATION_CHANNEL_ID_NON_AUDIO_ALERT = "TaskMan_Non_Audio_Alert_" + APK_VERSION;
    public static final String NOTIFICATION_CHANNEL_ID_ERROR = "ErrorChannel_" + APK_VERSION;
    public static final String NOTIFICATION_CHANNEL_ID_FOREGROUND_SERVICE = "TasksChannel_Foreground_Service_" + APK_VERSION;


    public static final String NOTIFICATION_CHANNEL_NAME_NON_AUDIO_ALERT = "Non-Audio Alert Channel (TaskMan)";
    public static final String NOTIFICATION_CHANNEL_NAME_FOREGROUND_SERVICE = "Foreground Services (TaskMan)";


    public static final String BELL_CHAR = "\uD83D\uDD14";
    public static final String BELL_CHAR_HTML = "&#128276;";

    public static final String CALLED_FROM_NOTIFICATION = "calledFromNotification";
    public static final String CALLED_FROM_LIST_VIEW = "calledFromListView";
    public static final String CALLED_FROM_MULTIPLE_EDIT_MODE = "calledFromMultipleEditMode";

    public static final String TASK_FLAG_AUDIO_ALERT = "_audio_";

    public static final Date DATE_SOMEDAY = new Date(2099 - 1900, 1, 2, 3, 4, 5);   //do NOT change this value, it's saved in DB for "someday" tasks
    public static final String NEW_TASK_TITLE = "New Task";

    public static final String ROOT_SD_FOLDER_PATH = "/storage/emulated/0/_TaskMan";

    public static final int NO_TASK_NOTIFICATION_ID = -99999;
    public static final String NO_TASK_NOTIFICATION_TITLE = "\uD83D\uDE42";
}
