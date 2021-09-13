package com.example.taskman.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

public enum TaskTag {
    PERSONAL("Personal"),
    CAR("Car"),
    SOCIETY("Society"),
    WORK("Work"),
    ZTEMP("zTemp");

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

    public static List<String> stringValues() {
        return Arrays.stream(TaskTag.values()).map(t -> t.getValue()).collect(Collectors.toList());
    }
}

