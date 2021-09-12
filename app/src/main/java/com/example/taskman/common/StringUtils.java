package com.example.taskman.common;

public class StringUtils {
    public static String makeFirstCharacterUppercase(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
