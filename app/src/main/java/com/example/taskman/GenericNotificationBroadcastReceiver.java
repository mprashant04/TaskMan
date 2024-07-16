package com.example.taskman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.example.taskman.common.Logs;
import com.example.taskman.task_handlers.NotificationHandler;

public class GenericNotificationBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_NOTIFICATION_MANUALLY_DISMISSED = "notification_manually_dismissed";
    ;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_NOTIFICATION_MANUALLY_DISMISSED.equals(action)) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    NotificationHandler.refreshAll(context);
                }
            }, 600);
        }
    }
}