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
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taskman.R;
import com.example.taskman.common.Declarations;
import com.example.taskman.db.TaskDbHelper;
import com.example.taskman.models.Task;
import com.example.taskman.utils.DateUtils;
import com.example.taskman.utils.DialogUtils;
import com.example.taskman.utils.Utils;

import java.util.Date;
import java.util.List;

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
        synchronized (SYNC) {
            boolean taskFoundWithAudioAlert = false;
            String watchMessage = "";
            int watchMessageCount = 0;

            if (enableAudioAlert || chronoTimerBase == null) {
                chronoTimerBase = SystemClock.elapsedRealtime();
            }

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
                if (watchMessageCount > 1) watchMessage = watchMessageCount + "  tasks";
                watchMessage = watchMessage + " " + BELL_CHAR;

                playTone(context, true, watchMessage);
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

    public static synchronized void createNotificationChannels(Context context, boolean forceRecreate) {

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        //--------- task notification channel -----------------------------------------------------------------
        if (forceRecreate)
            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID_TASK);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_TASK,
                "Tasks Channel",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setSound(null, null);
        notificationManager.createNotificationChannel(channel);


        //---------- foreground service notification channel --------------------------------------------------------
        if (forceRecreate)
            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID_AUDIO_ALERT);

        // notificationManager.deleteNotificationChannel("AudioAlert_2");   //delete later, test only


        NotificationChannel alertChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_AUDIO_ALERT,
                NOTIFICATION_CHANNEL_NAME_AUDIO_ALERT,
                NotificationManager.IMPORTANCE_HIGH
        );
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getApplicationContext().getPackageName() + "/" + R.raw.audio_alert);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .build();
        alertChannel.setSound(soundUri, audioAttributes);
        notificationManager.createNotificationChannel(alertChannel);

        //---------- Error notification channel ------------------------------------------------------------------
        if (forceRecreate)
            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID_ERROR);
        NotificationChannel errorChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_ERROR,
                "Error Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(errorChannel);


        if (forceRecreate)
            DialogUtils.infoDialog(context, "Notification channels re-created. Refer help menu for tweak instructions");
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

    public static synchronized void showAudioAlert(Context context, String msg)  {
        final int notification_id = 987767;
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
        notificationManager.notify(notification_tag, notification_id, builder.build());


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
