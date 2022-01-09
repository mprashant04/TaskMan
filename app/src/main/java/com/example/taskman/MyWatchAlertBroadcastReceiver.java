package com.example.taskman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.taskman.task_handlers.NotificationHandler;

public class MyWatchAlertBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("com.example.taskman.WatchAlert".equals(action)) {
            String sharedText = intent.getDataString();
            NotificationHandler.showAudioAlert(context, sharedText);
        }
    }
}
