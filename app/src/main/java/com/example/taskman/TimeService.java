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

import com.example.taskman.common.Tasker;
import com.example.taskman.task_handlers.NotificationHandler;
import com.example.taskman.utils.FireBaseUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Timer;
import java.util.TimerTask;

import lombok.NonNull;

import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_SERVICE;

//Ref:  https://medium.com/nybles/sending-push-notifications-by-using-firebase-cloud-messaging-249aa34f4f4c

public class TimeService extends FirebaseMessagingService {
    public static final long NOTIFY_INTERVAL = 10 * 60 * 1000;  //10 minutes
    //public static final long NOTIFY_INTERVAL = 20 * 1000;  //for testing only

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    //@Override
    //public IBinder onBind(Intent intent) {
    //    return null;
    //}

    @Override
    public void onCreate() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_SERVICE)
                .setContentTitle("Running TaskMan Service")
                .setSmallIcon(R.drawable.notification_record)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(100001, notification);

        createTimer();
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY;
//    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        //firebase push notification received....
        super.onMessageReceived(remoteMessage);
        Tasker.sendData(this, remoteMessage.getData().get("title"), remoteMessage.getData().get("data"));
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
