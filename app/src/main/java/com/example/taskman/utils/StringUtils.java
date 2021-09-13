package com.example.taskman.utils;

public class StringUtils {
    public static String capitalizeFirstCharacter(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String capitalizeFirstCharacterOfEachWord(String text) {
        if (text == null || text.length() == 0) return text;
        text = text.toLowerCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

}
