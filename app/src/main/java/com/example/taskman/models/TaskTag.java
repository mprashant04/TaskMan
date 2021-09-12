package com.example.taskman.models;

import java.util.Arrays;

import lombok.Getter;

public enum TaskTag {
    PERSONAL("Personal"),
    CAR("Car"),
    SOCIETY("Society"),
    WORK("Work"),
    TEMP("Temp");

    @Getter
    private String value;

    TaskTag(String value) {
        this.value = value;
    }

    public static TaskTag getFromValue(String value) {
        return Arrays.stream(values())
                .filter(entry -> entry.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}

