package com.example.taskman;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.taskman.common.Logs;
import com.example.taskman.task_handlers.NotificationHandler;
import com.example.taskman.utils.DateUtils;

import java.util.Date;

public class MyAlarmManager extends BroadcastReceiver {
    private static final String LOG_PREFIX = "AlarmManager: ";
    private static boolean initiated = false;
    private static final long INTERVAL = 10 * 60 * 1000;  //10 minutes
    private static Date lastProcessedOn = null;

    //the method will be fired when the alarm is triggerred
    @Override
    public void onReceive(Context context, Intent intent) {
        process(context);
    }

    private static void process(Context context) {
        if (lastProcessedOn == null) lastProcessedOn = new Date();

        long timeDiff = DateUtils.diffInSeconds(lastProcessedOn);
        boolean intervalOk = (timeDiff < (INTERVAL / 1000) + 60);  //if run interval is ok as per definition, or big delays

        if (intervalOk)
            Logs.info(LOG_PREFIX + "Refreshing notifications (" + timeDiff + ")");
        else
            Logs.warn(LOG_PREFIX + "Refreshing notifications (" + timeDiff + ")");

        NotificationHandler.refreshAll(context, true);

        lastProcessedOn = new Date();
    }

    public static synchronized void init(Activity activity) {
        if (initiated) return;
        initiated = true;

        Logs.warn(LOG_PREFIX + "**** Init ****");

        AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(activity, MyAlarmManager.class);
        PendingIntent pi = PendingIntent.getBroadcast(activity, 0, i, 0);

        am.setRepeating(AlarmManager.RTC, new Date().getTime() + INTERVAL, INTERVAL, pi);

        process(activity); //first time processing after app start...
    }
}
