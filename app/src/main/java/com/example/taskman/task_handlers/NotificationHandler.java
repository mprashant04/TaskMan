package com.example.taskman.task_handlers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.taskman.R;
import com.example.taskman.common.AudioFile;
import com.example.taskman.common.Declarations;
import com.example.taskman.common.Logs;
import com.example.taskman.db.TaskDbHelper;
import com.example.taskman.models.Task;
import com.example.taskman.utils.DateUtils;
import com.example.taskman.utils.MultimediaUtils;
import com.example.taskman.utils.Utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.example.taskman.common.Declarations.BELL_CHAR;
import static com.example.taskman.common.Declarations.BELL_CHAR_HTML;
import static com.example.taskman.common.Declarations.CALLED_FROM_NOTIFICATION;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_ERROR;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_FOREGROUND_SERVICE;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_NON_AUDIO_ALERT;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_TASK;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_NAME_FOREGROUND_SERVICE;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_NAME_NON_AUDIO_ALERT;
import static com.example.taskman.common.Declarations.NO_TASK_NOTIFICATION_ID;
import static com.example.taskman.utils.Utils.delay;

public class NotificationHandler {

    private static Object SYNC = new Object();
    private static int errorNotificationId = 0;

    private static Long chronoTimerBase = null;


    public static synchronized void refreshAll(Context context) {
        refreshAll(context, false);
    }

    public static synchronized void refreshAll(Context context, boolean enableAudioAlert) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doRefreshAll(context, enableAudioAlert);   //run asynchronously
            }
        }, 1);
    }

    private static synchronized void doRefreshAll(Context context, boolean enableAudioAlert) {
        synchronized (SYNC) {
            boolean maxTaskDisplayLimitReached = false;
            String watchMessage = "";
            int watchMessageCount = 0;

            if (enableAudioAlert || chronoTimerBase == null) {
                chronoTimerBase = SystemClock.elapsedRealtime();
            }

            TaskDbHelper db = new TaskDbHelper(context);
            List<Task> tasks = db.getActiveAndOverdue();
            sortTasks(tasks);


            cancelTaskNotifications(context);

            //------ Show tasks ----------------------------------------------
            for (int idx = 0; idx < tasks.size(); idx++) {

                boolean isLastTask = (idx == tasks.size() - 1);
                maxTaskDisplayLimitReached = !isLastTask && (idx >= Declarations.MAX_TASK_NOTIFICATIONS - 1);

                Task task = tasks.get(idx);
                showTaskNotification(context,
                        task.getId(),
                        task.getFormattedTitle() + (maxTaskDisplayLimitReached ? " ♦" : ""),
                        (isLastTask || maxTaskDisplayLimitReached ? "(" + tasks.size() + ")" : null)
                );

                if (maxTaskDisplayLimitReached) break;
            }

            if (tasks.size() <= 0)
                showTaskNotification(context, Declarations.NO_TASK_NOTIFICATION_ID, Declarations.NO_TASK_NOTIFICATION_TITLE, "(" + tasks.size() + ")");


            //----- Check if task present with audio alert ------------------
            if (enableAudioAlert) {
                boolean showAudioAlert = false;
                for (int idx = 0; idx < tasks.size(); idx++) {
                    Task task = tasks.get(idx);
                    if (task.isFlaggedForAudioAlert()) {
                        showAudioAlert = true;
                        watchMessageCount++;
                        watchMessage = watchMessageCount > 1 ? watchMessageCount + "  tasks" : task.getTitle();
                    }
                }


                if (!showAudioAlert && maxTaskDisplayLimitReached) {
                    showAudioAlert = true;
                    watchMessage = "Too many tasks....";
                }


                //  show alert notification before task alerts, because if tasks count is high, android does not allow showing alert notification
                if (showAudioAlert && !isSilentTime()) {
                    watchMessage = watchMessage + " " + BELL_CHAR;

                    delay(1000);
                    showWatchAlert(context, watchMessage, AudioFile.NOTIFICATION_TASK);
                    //delay(2000);
                }
            }

        }
    }

    public static void sortTasks(List<Task> tasks) {
        if (tasks != null && tasks.size() > 0)
            Collections.sort(tasks,
                    Comparator.comparing(Task::isFlaggedForAudioAlert).reversed()  //first show alert flagged tasks
                            .thenComparing(Task::getDueOn) //then in due date order
            );
    }


    private static boolean isSilentTime() {
        //silent between 23:00 and 7:00
        return DateUtils.getHours() < 7
                //  || DateUtils.getHours() >= 23
                ;
    }

    private static synchronized boolean isNotificationChannelPresent(Context context, String channelId) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        for (NotificationChannel channel : notificationManager.getNotificationChannels())
            if (channel.getId().equalsIgnoreCase(channelId))
                return true;

        return false;
    }

    public static synchronized void createNotificationChannels(Context context) {

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        //first delete old ones channels...
        for (NotificationChannel channel : notificationManager.getNotificationChannels()) {
            String channelId = channel.getId();
            if (!channelId.equalsIgnoreCase(NOTIFICATION_CHANNEL_ID_TASK)
                    && !channelId.equalsIgnoreCase(NOTIFICATION_CHANNEL_ID_ERROR)
                    && !channelId.equalsIgnoreCase(NOTIFICATION_CHANNEL_ID_FOREGROUND_SERVICE)
                    && !channelId.equalsIgnoreCase(NOTIFICATION_CHANNEL_ID_NON_AUDIO_ALERT)) {
                Logs.warn("Deleting obsolete notification channel - " + channelId);
                notificationManager.deleteNotificationChannel(channelId);
            }
        }


        //--------- task notification channel -----------------------------------------------------------------
        if (!isNotificationChannelPresent(context, NOTIFICATION_CHANNEL_ID_TASK)) {
            Logs.warn("Creating notification channel - " + NOTIFICATION_CHANNEL_ID_TASK);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_TASK,
                    "Tasks Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
        }

        //---------- Non-Audio alert notification channel --------------------------------------------------------
        if (!isNotificationChannelPresent(context, NOTIFICATION_CHANNEL_ID_NON_AUDIO_ALERT)) {
            Logs.warn("Creating notification channel - " + NOTIFICATION_CHANNEL_ID_NON_AUDIO_ALERT);
            NotificationChannel alertChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_NON_AUDIO_ALERT,
                    NOTIFICATION_CHANNEL_NAME_NON_AUDIO_ALERT,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(alertChannel);
        }

        //---------- Foreground serivces notification channel --------------------------------------------------------
        if (!isNotificationChannelPresent(context, NOTIFICATION_CHANNEL_ID_FOREGROUND_SERVICE)) {
            Logs.warn("Creating notification channel - " + NOTIFICATION_CHANNEL_ID_FOREGROUND_SERVICE);
            NotificationChannel serivceChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_FOREGROUND_SERVICE,
                    NOTIFICATION_CHANNEL_NAME_FOREGROUND_SERVICE,
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(serivceChannel);
        }

        //---------- Error notification channel ------------------------------------------------------------------
        if (!isNotificationChannelPresent(context, NOTIFICATION_CHANNEL_ID_ERROR)) {
            Logs.warn("Creating notification channel - " + NOTIFICATION_CHANNEL_ID_ERROR);
            NotificationChannel errorChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_ERROR,
                    "Error Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(errorChannel);
        }
    }


    private static void showTaskNotification(Context context, int taskId, String taskName, String subText) {
        // edit task intent
        PendingIntent editTaskIntent = null;
        if (taskId != NO_TASK_NOTIFICATION_ID) {
            Intent intent = Utils.createEditTaskIntent(context, taskId, CALLED_FROM_NOTIFICATION);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(intent);
            editTaskIntent = stackBuilder.getPendingIntent(taskId, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }


        //RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_small);  //upto android 11
        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_small_12);  //android 12


        notificationLayout.setTextViewText(R.id.notification_text, taskName);


        if (Build.VERSION.SDK_INT < 31) {  //android 11 only
            if (subText != null) {
                notificationLayout.setChronometer(R.id.notification_sub_text_chrono,
                        chronoTimerBase,
                        null,
                        true);
                notificationLayout.setTextViewText(R.id.notification_sub_text_str, subText);
            } else {
                notificationLayout.setViewVisibility(R.id.notification_sub_text_chrono, View.GONE);
            }
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Declarations.NOTIFICATION_CHANNEL_ID_TASK)
                .setSmallIcon(R.drawable.notification_icon)

                //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())

                .setCustomContentView(notificationLayout)

                .setStyle(new NotificationCompat.InboxStyle().setSummaryText(subText)) //android 12 onwards

                .setGroup(taskId + "")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)  //prevent dismiss
                //.setCustomBigContentView(notificationLayoutExpanded)
                .setContentIntent(editTaskIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(Declarations.NOTIFICATION_TAG_TASKS, taskId, builder.build());
    }

    private static void cancelTaskNotifications(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for (StatusBarNotification n : mNotificationManager.getActiveNotifications())
            if (Declarations.NOTIFICATION_CHANNEL_ID_TASK.equals(n.getNotification().getChannelId()))
                mNotificationManager.cancel(n.getTag(), n.getId());

    }


    public static void showErrorNotification(Context context, String msg, boolean persistent) {
        showFeedbackNotification(context, msg, R.color.notification_background_error, Color.WHITE, persistent);
    }

    public static void showFeedbackNotification(Context context, String msg, int backgroundColor, int textColor, boolean persistent) {
        errorNotificationId++;

        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        notificationLayout.setInt(R.id.notification_layout, "setBackgroundResource", backgroundColor);
        notificationLayout.setInt(R.id.notification_text, "setTextColor", textColor);

        notificationLayout.setTextViewText(R.id.notification_text, msg);
        notificationLayout.setViewVisibility(R.id.notification_sub_text_chrono, View.GONE);
        notificationLayout.setTextViewText(R.id.notification_sub_text_str, "Taskman (" + DateUtils.format("HH:mm", new Date()) + ")");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Declarations.NOTIFICATION_CHANNEL_ID_ERROR)
                .setSmallIcon(R.drawable.notification_icon)
                //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setGroup(errorNotificationId + "-error")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(persistent)  //prevent dismiss
                //.setCustomBigContentView(notificationLayoutExpanded)
                //.setContentIntent(editTaskIntent)
                ;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify("error", errorNotificationId, builder.build());
    }


    public static synchronized void showWatchAlert(Context context, String msg, AudioFile audioFile) {
        //final int notification_id = 987767;
        final String notification_tag = "audio_notification_tag";

        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        notificationLayout.setInt(R.id.notification_layout, "setBackgroundResource", R.color.transparent);

        notificationLayout.setTextViewText(R.id.notification_text, "(Watch Alert)");
        notificationLayout.setViewVisibility(R.id.notification_sub_text_chrono, View.GONE);
        notificationLayout.setTextViewText(R.id.notification_sub_text_str, "(" + DateUtils.format("HH:mm", new Date()) + ")");


        //NotificationCompat.Builder builder = new NotificationCompat.Builder(context, (withAudio ? NOTIFICATION_CHANNEL_ID_AUDIO_ALERT : NOTIFICATION_CHANNEL_ID_NON_AUDIO_ALERT))
        //UPDATE: somehow notification tone for NOTIFICATION_CHANNEL_ID_AUDIO_ALERT was not working consistently, so using NOTIFICATION_CHANNEL_ID_NON_AUDIO_ALERT
        //        and playing tone as media in following code using MultimediaUtils.playAssetSound()
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_NON_AUDIO_ALERT)
                .setSmallIcon(R.drawable.notification_icon)
                .setCustomContentView(notificationLayout)
                //.setGroup(errorNotificationId + "-error")
                //.setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setTimeoutAfter(5000)  //auto cancel after some delay
                //.setOngoing(true)  //prevent dismiss


                //--------------------------------------------
                // Amazfit watch notification compatible code
                //--------------------------------------------
                //.setSubText(msg)

                //--------------------------------------------
                // Samsung Galaxy watch compatible code
                //--------------------------------------------
                .setContentTitle(msg)  //title of notification
                //.setContentText(msg)    //message in notification


                //
                ;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notification_tag, new Random().nextInt(), builder.build());

        //play sound
        if (audioFile != null) {
            MultimediaUtils.playAssetSound(context, audioFile);
        }

        Logs.info(BELL_CHAR_HTML + " " + msg);
    }


}
