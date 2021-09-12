package com.example.taskman;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.taskman.common.NotificationHandler;

import java.util.Timer;
import java.util.TimerTask;

import static com.example.taskman.common.Declarations.CHANNEL_ID_SERVICE;

public class TimeService extends Service {
    public static final long NOTIFY_INTERVAL = 5 * 60 * 1000;  //5 minutes

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_SERVICE)
                .setContentTitle("Foreground Service")
                .setSmallIcon(R.drawable.notification_record)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(1, notification);

        createTimer();

        return START_STICKY;
    }


    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID_SERVICE,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_MIN
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    private void createTimer() {
        // cancel if already existed
        if (mTimer != null) {
            return;
        }
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(this), 1000, NOTIFY_INTERVAL);
    }

    class TimeDisplayTimerTask extends TimerTask {

        private Context context = null;

        public TimeDisplayTimerTask(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    NotificationHandler.refreshAll(context, true);
                }

            });
        }
    }
}
