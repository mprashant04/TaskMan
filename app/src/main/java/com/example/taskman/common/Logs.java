package com.example.taskman.common;

import com.example.taskman.utils.DateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class Logs {

    private final static String PATH = "/sdcard/zLogs/TaskMan";

    static {
        createLogDirectory();
    }

    private static void createLogDirectory() {
        File file = new File(PATH);
        file.mkdirs();
    }

    public static synchronized void info(String msg) {
        write("[INFO]", msg);
    }

    public static synchronized void error(String msg) {
        write("[ERRR]", msg);
    }

    private static synchronized void write(String prefix, String msg) {
        try {
            File file = new File(PATH, "" + DateUtils.format("yyyy_MM_dd", new Date()) + ".html");
            FileOutputStream stream = new FileOutputStream(file, true);

            msg = DateUtils.format("HH:mm:ss", new Date()) + " " + prefix + " " + msg;

            try {
                stream.write((msg + "\n").getBytes());
            } finally {
                stream.close();
            }
        } catch (Throwable ex) {

        }
    }

}
