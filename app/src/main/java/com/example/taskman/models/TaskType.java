package com.example.taskman.models;

import java.util.Arrays;

import lombok.Getter;

public enum TaskType {
    ONETIME("O"), RECURSIVE("R");

    @Getter
    private String value;

    TaskType(String value) {
        this.value = value;
    }

    public static TaskType getFromValue(String value) {
        return Arrays.stream(values())
                .filter(entry -> entry.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
