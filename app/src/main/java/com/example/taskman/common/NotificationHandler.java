package com.example.taskman.common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.ToneGenerator;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taskman.R;
import com.example.taskman.db.TaskDbHelper;
import com.example.taskman.models.Task;

import java.util.Date;
import java.util.List;

import static com.example.taskman.common.Declarations.BELL_CHAR;
import static com.example.taskman.common.Declarations.CALLED_FROM_NOTIFICATION;

public class NotificationHandler {
    private static Object SYNC = new Object();

    public static void refreshAll(Context context) {
        refreshAll(context, false);
    }
    public static void refreshAll(Context context, boolean enableAudioAlert) {
        synchronized (SYNC) {
            boolean taskFoundWithAudioAlert = false;
            TaskDbHelper db = new TaskDbHelper(context);

            createNotificationChannel(context);
            cancelAllNotifications(context);

            List<Task> tasks = db.getActiveAndOverdue();
            for (Task t : tasks) {
                showNotification(context, t);
                if (t.isFlagged(Declarations.TASK_FLAG_AUDIO_ALERT)) taskFoundWithAudioAlert = true;
            }
            if (taskFoundWithAudioAlert && enableAudioAlert)
                Utils.playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 700);   //ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
        }
    }

    private static void createNotificationChannel(Context context) {

        CharSequence name = context.getString(R.string.channel_name);
        String description = context.getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(Declarations.CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    private static void showNotification(Context context, Task task) {
        // edit task intent
        Intent intent = Utils.createEditTaskIntent(context, task.getId(), CALLED_FROM_NOTIFICATION);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent editTaskIntent = stackBuilder.getPendingIntent(task.getId(), PendingIntent.FLAG_UPDATE_CURRENT);


        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        //RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);

        notificationLayout.setTextViewText(R.id.notification_text,
                task.getTitle()
                        + (task.isFlagged(Declarations.TASK_FLAG_AUDIO_ALERT) ? " " + BELL_CHAR : "")
                        + " (" + task.getId() + ") "
                        + DateUtils.format("HH:mm:ss", new Date())
                        + " ★"
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Declarations.CHANNEL_ID)
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
        mNotificationManager.cancelAll();
    }
}
