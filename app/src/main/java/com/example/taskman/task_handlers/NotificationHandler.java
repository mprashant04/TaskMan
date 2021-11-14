package com.example.taskman.task_handlers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taskman.R;
import com.example.taskman.common.Declarations;
import com.example.taskman.common.Logs;
import com.example.taskman.db.TaskDbHelper;
import com.example.taskman.models.Task;
import com.example.taskman.utils.DateUtils;
import com.example.taskman.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.example.taskman.common.Declarations.BELL_CHAR;
import static com.example.taskman.common.Declarations.CALLED_FROM_NOTIFICATION;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_AUDIO_ALERT;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_ERROR;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_TASK;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_NAME_AUDIO_ALERT;

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
            boolean taskFoundWithAudioAlert = false;
            String watchMessage = "";
            int watchMessageCount = 0;

            if (enableAudioAlert || chronoTimerBase == null) {
                chronoTimerBase = SystemClock.elapsedRealtime();
            }

            TaskDbHelper db = new TaskDbHelper(context);
            List<Task> tasks = db.getActiveAndOverdue();

            cancelAllNotifications(context);

            //----- Check if task present with audio alert ------------------
            if (enableAudioAlert) {
                for (int idx = 0; idx < tasks.size(); idx++) {
                    Task task = tasks.get(idx);
                    if (task.isFlagged(Declarations.TASK_FLAG_AUDIO_ALERT)) {
                        taskFoundWithAudioAlert = true;
                        watchMessage = task.getTitle();
                        watchMessageCount++;
                    }
                }
                //  show alert notification before task alerts, because if tasks count is high, android does not allow showing alert notification
                if (taskFoundWithAudioAlert && !isSilentTime()) {
                    if (watchMessageCount > 1) watchMessage = watchMessageCount + "  tasks";
                    watchMessage = watchMessage + " " + BELL_CHAR;
                    playTone(context, true, watchMessage);
                }
            }

            //------ Show tasks ----------------------------------------------
            for (int idx = 0; idx < tasks.size(); idx++) {
                Task task = tasks.get(idx);
                showTaskNotification(context, task, (idx == tasks.size() - 1 ? "(" + tasks.size() + ")" : null));
            }
        }
    }

    public static synchronized void playTone(Context context, boolean afterDelay, String msg) {
        //Somehow above tone play logic was not working after phone idle for some time, hence using notification way below...
        showAudioAlert(context, msg);


//        //run asynchronously
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    //if (afterDelay)  Thread.sleep(1500);  //sometimes sound gets disturbed when played along with notifications, hence added delay
//
//
//                    //UserFeedbackUtils.playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 700);   //ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
//
//                } catch (Throwable ex) {
//                    Logs.error("playTone error!!");
//                    Logs.error(ex);
//                }
//            }
//        }).start();
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
                    && !channelId.equalsIgnoreCase(NOTIFICATION_CHANNEL_ID_AUDIO_ALERT)) {
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


        //---------- foreground service notification channel --------------------------------------------------------
        if (!isNotificationChannelPresent(context, NOTIFICATION_CHANNEL_ID_AUDIO_ALERT)) {
            Logs.warn("Creating notification channel - " + NOTIFICATION_CHANNEL_ID_AUDIO_ALERT);
            NotificationChannel alertChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_AUDIO_ALERT,
                    NOTIFICATION_CHANNEL_NAME_AUDIO_ALERT,
                    NotificationManager.IMPORTANCE_HIGH
            );
            Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getApplicationContext().getPackageName() + "/" + R.raw.audio_alert_3);  //when sound changed, change notification id also, with same id android does not update new sound i think
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            alertChannel.setSound(soundUri, audioAttributes);
            notificationManager.createNotificationChannel(alertChannel);
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
            notificationLayout.setChronometer(R.id.notification_sub_text_chrono,
                    chronoTimerBase,
                    null,
                    true);
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
        notificationLayout.setTextViewText(R.id.notification_sub_text_str, "Taskman (" + DateUtils.format("HH:mm", new Date()) + ")");

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

    public static synchronized void showAudioAlert(Context context, String msg) {
        //final int notification_id = 987767;
        final String notification_tag = "audio_notification_tag";

        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        notificationLayout.setInt(R.id.notification_layout, "setBackgroundResource", R.color.transparent);

        notificationLayout.setTextViewText(R.id.notification_text, "(Audio Alert)");
        notificationLayout.setViewVisibility(R.id.notification_sub_text_chrono, View.GONE);
        notificationLayout.setTextViewText(R.id.notification_sub_text_str, "(" + DateUtils.format("HH:mm", new Date()) + ")");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_AUDIO_ALERT)
                .setSmallIcon(R.drawable.notification_icon)
                .setCustomContentView(notificationLayout)
                //.setGroup(errorNotificationId + "-error")
                //.setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSubText(msg)  //not for display purpose, but to send info to tasker to show watch message
                .setTimeoutAfter(15000)  //auto cancel after some delay
                //.setOngoing(true)  //prevent dismiss
                ;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notification_tag, new Random().nextInt(), builder.build());

        Logs.info(BELL_CHAR + " " + msg);


        //cancel notification after delay
        //--------------------------------------
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(5000);
//                    notificationManager.cancel(notification_tag, notification_id);
//                } catch (Throwable ex) {
//                    Logs.error("playTone error!!");
//                    Logs.error(ex);
//                }
//            }
//        }).start();

//
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                notificationManager.cancel(notification_tag, notification_id);
//            }
//        }, 8000);


    }


}
