package com.example.taskman.common;

import lombok.Getter;

public enum AudioFile{
    NOTIFICATION_TASK("notification-task.mp3"),
    NOTIFICATION_FNO_APP_ALERT("notification-fno-app-alert.mp3");

    @Getter
    private String fileName = "";

    AudioFile(String s) {
        fileName = s;
    }
}
