package com.example.taskman.common;

import com.example.taskman.utils.DateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class Logs {

    private final static String PATH = "/sdcard/zLogs/TaskMan";

    private static final String MSG_PLACEHOLDER = "$MESSAGE$";
    private final static String TEMPLATE = "<b><p style=\"font-family:'Courier New'; font-size:25px; color:$COLOR$; padding:0px; margin:0px; white-space:nowrap;\">" + MSG_PLACEHOLDER + "</p></b>";
    private final static String TEMPLATE_INFO = TEMPLATE.replace("$COLOR$", "blue");
    private final static String TEMPLATE_WARN = TEMPLATE.replace("$COLOR$", "orange");
    private final static String TEMPLATE_ERROR = TEMPLATE.replace("$COLOR$", "red");;

    static {
        createLogDirectory();
        error("==================================================================");
    }

    private static void createLogDirectory() {
        File file = new File(PATH);
        file.mkdirs();
    }

    public static synchronized void info(String msg) {
        write(TEMPLATE_INFO, msg);
    }

    public static synchronized void warn(String msg) {
        write(TEMPLATE_WARN, msg);
    }

    public static synchronized void error(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        write(TEMPLATE_ERROR, sw.getBuffer().toString().replace("\n", "<br>"));
    }

    public static synchronized void error(String msg) {
        write(TEMPLATE_ERROR, msg);
    }

    private static synchronized void write(String template, String msg) {
        try {
            File file = new File(PATH, "" + DateUtils.format("yyyy_MM_dd", new Date()) + ".html");
            FileOutputStream stream = new FileOutputStream(file, true);

            msg = DateUtils.format("HH:mm:ss", new Date()) + " " + msg;
            msg = template.replace(MSG_PLACEHOLDER, msg);

            try {
                stream.write(msg.getBytes());
            } finally {
                stream.close();
            }
        } catch (Throwable ex) {

        }
    }

}
