package com.example.taskman;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.taskman.common.Logs;
import com.example.taskman.utils.DialogUtils;
import com.example.taskman.utils.ForegroundServiceNotificationCreator;
import com.example.taskman.utils.PushyUtils;

import me.pushy.sdk.Pushy;

public class PushyService extends Service {

    public static void init(MainActivity mainActivity) {
        mainActivity.startForegroundService(new Intent(mainActivity, PushyService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ForegroundServiceNotificationCreator.startForeground(this);

        PushyUtils.register(this);
        Pushy.listen(this);

        DialogUtils.toastLong("Pushy  service started...", this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Logs.error("PushyService.onDestroy() ===========================================");
        super.onDestroy();
    }
}
