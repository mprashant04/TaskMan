package com.example.taskman.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.example.taskman.common.Logs;

import static android.media.AudioManager.STREAM_RING;

public class MultimediaUtils {
    public static synchronized void playAssetSound(Context ctx, int resourceId) {
        AssetManager am;
        try {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setLegacyStreamType(STREAM_RING)
                    .build();

            AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

            MediaPlayer player = MediaPlayer.create(ctx, resourceId, audioAttributes, audioManager.generateAudioSessionId());
            player.setLooping(false);

            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });



        } catch (Throwable e) {
            Logs.error(e);
        }
    }
}
