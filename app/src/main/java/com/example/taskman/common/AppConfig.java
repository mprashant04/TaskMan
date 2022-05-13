package com.example.taskman.common;


import androidx.appcompat.app.AppCompatActivity;

import com.example.taskman.utils.DialogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class AppConfig {
    private static Properties props = null;

    private enum SettingNames {
        BRIGHTNESS_AUTO_MINIMUM("brightness-auto-minimun"),
        BRIGHTNESS_SHOW_TOAST_DEBUG_ON_CHANGES("brightness-show-toast-on-change");

        private String value = "";

        SettingNames(String val) {
            this.value = val;
        }

        public String getValue() {
            return value;
        }
    }

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try {
            props = new Properties();
            props.load(new FileInputStream(new File(Declarations.ROOT_SD_FOLDER_PATH + "/conf.txt")));
            //Logs.error(props.toString());
        } catch (Throwable ex) {
            Logs.error(ex);
        }
    }

    public static int getBrightnessAutoMinimumValue() {
        return Integer.parseInt(props.getProperty(SettingNames.BRIGHTNESS_AUTO_MINIMUM.getValue(), "0"));
    }

    public static boolean getBrightnessShowToastDebugMessagesOnChange() {
        return Integer.parseInt(props.getProperty(SettingNames.BRIGHTNESS_SHOW_TOAST_DEBUG_ON_CHANGES.getValue(), "0")) == 1;
    }


    public static boolean validateIfAllConfigValuesPresent(final AppCompatActivity ctx) {
        String missingConf = "";
        boolean missing = false;
        for (SettingNames s : SettingNames.values()) {
            if (props.get(s.getValue()) == null) {
                missing = true;
                missingConf += "\n   - " + s.getValue();
            }
        }

        if (missing) {
            Logs.error("Missing config values...." + missingConf);
            DialogUtils.alertDialog(ctx, "Missing Config!!!", "Following settings missing in config file....\n" + missingConf);
        }

        return !missing;
    }

}
