package com.example.taskman.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.example.taskman.common.AudioFile;
import com.example.taskman.common.Declarations;
import com.example.taskman.common.Logs;

import java.io.File;

import static android.media.AudioManager.STREAM_RING;

public class MultimediaUtils {
    private static MediaPlayer player = null;

    public static synchronized void playAssetSound(Context ctx, AudioFile audioFile) {
        AssetManager am;
        try {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setLegacyStreamType(STREAM_RING)
                    .build();

            AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            stopPlayer();
            player = MediaPlayer.create(ctx,
                    Uri.parse(buildFilePath(audioFile)),
                    null,
                    audioAttributes,
                    audioManager.generateAudioSessionId());
            player.setLooping(false);

            player.start();
//            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    mp.release();
//                }
//            });


        } catch (Throwable e) {
            Logs.error(e);
        }
    }

    private static String buildFilePath(AudioFile file) {
        return Declarations.ROOT_SD_FOLDER_PATH + File.separator + "resources" + File.separator + "audio" + File.separator + file.getFileName();
    }


    private static void stopPlayer() {
        try {
            if (player != null) {
                player.stop();
            }
        } catch (Throwable e) {
            //Logs.error(e);
        }
    }

    public static void checkIfAllSoundFilesPresent(Context ctx) {
        try {
            String missingFiles = "";
            boolean failed = false;
            for (AudioFile s : AudioFile.values()) {
                if (!validateSoundFile(s)) {
                    failed = true;
                    missingFiles += "\n - " + s.getFileName();
                }
            }

            if (failed) {
                DialogUtils.alertDialog(ctx, "Missing sound files...", "Following sound files are missing..." + missingFiles);
            }

        } catch (Throwable e) {
            Logs.error(e);
            DialogUtils.alertDialog(ctx, "Error", "Error while validating sound files...");
        }
    }

    private static boolean validateSoundFile(AudioFile s) {
        return new File(buildFilePath(s)).exists();
    }
}
