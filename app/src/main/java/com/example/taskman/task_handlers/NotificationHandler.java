package com.example.taskman.task_handlers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.ToneGenerator;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taskman.R;
import com.example.taskman.common.Declarations;
import com.example.taskman.common.Tasker;
import com.example.taskman.db.TaskDbHelper;
import com.example.taskman.models.Task;
import com.example.taskman.utils.DateUtils;
import com.example.taskman.utils.UserFeedbackUtils;
import com.example.taskman.utils.Utils;

import java.util.Date;
import java.util.List;

import static com.example.taskman.common.Declarations.BELL_CHAR;
import static com.example.taskman.common.Declarations.CALLED_FROM_NOTIFICATION;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_ERROR;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_SERVICE;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_TASK;

public class NotificationHandler {
    private static Object SYNC = new Object();
    private static int errorNotificationId = 0;

    public static synchronized void refreshAll(Context context) {
        refreshAll(context, false);
    }

    public static synchronized void refreshAll(Context context, boolean enableAudioAlert) {
        synchronized (SYNC) {
            boolean taskFoundWithAudioAlert = false;
            String watchMessage = "";
            int watchMessageCount = 0;

            TaskDbHelper db = new TaskDbHelper(context);

            cancelAllNotifications(context);

            List<Task> tasks = db.getActiveAndOverdue();
            for (int idx = 0; idx < tasks.size(); idx++) {
                Task task = tasks.get(idx);
                showTaskNotification(context, task, (idx == tasks.size() - 1 ? "(" + tasks.size() + ")" : null));
                if (task.isFlagged(Declarations.TASK_FLAG_AUDIO_ALERT)) {
                    taskFoundWithAudioAlert = true;
                    watchMessage = task.getTitle();
                    watchMessageCount++;
                }
            }
            if (taskFoundWithAudioAlert && enableAudioAlert && !isSilentTime()) {
                UserFeedbackUtils.playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 700);   //ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD

                if (watchMessageCount > 1) watchMessage = watchMessageCount + "  tasks";
                Tasker.sendWatchNotification(context,
                        watchMessage + " " + BELL_CHAR
                );
            }
        }
    }

    private static boolean isSilentTime() {
        //silent between 23:00 and 7:00
        return DateUtils.getHours() < 7
                //  || DateUtils.getHours() >= 23
                ;
    }

    public static synchronized void createNotificationChannels(Context context) {

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        //--------- task notification channel -------------------------------------
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_TASK,
                "Tasks Channel",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setSound(null, null);
        notificationManager.createNotificationChannel(channel);


        //---------- foreground service notification channel ---------------------------
        NotificationChannel serviceChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID_SERVICE,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_NONE
        );
        notificationManager.createNotificationChannel(serviceChannel);

        //---------- Error notification channel ---------------------------
        NotificationChannel errorChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID_ERROR,
                "Error Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(errorChannel);
    }


    private static void showTaskNotification(Context context, Task task, String subText) {
        // edit task intent
        Intent intent = Utils.createEditTaskIntent(context, task.getId(), CALLED_FROM_NOTIFICATION);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent editTaskIntent = stackBuilder.getPendingIntent(task.getId(), PendingIntent.FLAG_UPDATE_CURRENT);


        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        //RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);

        notificationLayout.setTextViewText(R.id.notification_text, task.getFormattedTitle());

        if (subText != null) {
            notificationLayout.setChronometer(R.id.notification_sub_text_chrono, SystemClock.elapsedRealtime(), null, true);
            notificationLayout.setTextViewText(R.id.notification_sub_text_str, subText);
        } else {
            notificationLayout.setViewVisibility(R.id.notification_sub_text_chrono, View.GONE);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Declarations.NOTIFICATION_CHANNEL_ID_TASK)
                .setSmallIcon(R.drawable.notification_icon)
                //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setGroup(task.getId() + "")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)  //prevent dismiss
                //.setCustomBigContentView(notificationLayoutExpanded)
                .setContentIntent(editTaskIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(task.getId() + "", Declarations.NOTIFICATION_ID, builder.build());
    }

    private static void cancelAllNotifications(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for (StatusBarNotification n : mNotificationManager.getActiveNotifications()) {
            if (Declarations.NOTIFICATION_CHANNEL_ID_TASK.equals(n.getNotification().getChannelId())) {
                mNotificationManager.cancel(n.getTag(), n.getId());
            }
        }
    }

    public static void showError(Context context, String msg) {
        errorNotificationId++;

        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        notificationLayout.setInt(R.id.notification_layout, "setBackgroundResource", R.color.notification_background_error);

        notificationLayout.setTextViewText(R.id.notification_text, msg);
        notificationLayout.setViewVisibility(R.id.notification_sub_text_chrono, View.GONE);
        notificationLayout.setTextViewText(R.id.notification_sub_text_str, "Taskman ("+ DateUtils.format("HH:mm", new Date()) + ")");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Declarations.NOTIFICATION_CHANNEL_ID_ERROR)
                .setSmallIcon(R.drawable.notification_icon)
                //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setGroup(errorNotificationId + "-error")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)  //prevent dismiss
                //.setCustomBigContentView(notificationLayoutExpanded)
                //.setContentIntent(editTaskIntent)
                ;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify("error", errorNotificationId, builder.build());
    }
}
