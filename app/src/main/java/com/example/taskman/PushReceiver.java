package com.example.taskman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.taskman.common.Logs;
import com.example.taskman.common.Tasker;
import com.example.taskman.utils.DateUtils;

import java.util.Date;


public class PushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        logMessage(intent);
        Tasker.sendData(context, intent.getStringExtra("title"), intent.getStringExtra("data"));
    }

    private void logMessage(Intent intent) {
        Date sentOn = DateUtils.stringToDate("yyyy/MM/dd HH:mm:ss", intent.getStringExtra("sentOn"));

        boolean warn = (DateUtils.diffInSeconds(sentOn) > 5);

        String msg = "Pushy received "
                + "(" + DateUtils.diffInSeconds(sentOn) + "s)"
                + ": "
                + intent.getExtras().toString();

        if (warn)
            Logs.warn(msg);
        else
            Logs.info(msg);
    }
}