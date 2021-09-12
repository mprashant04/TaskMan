package com.example.taskman.models;

import java.util.Arrays;

import lombok.Getter;

public enum TaskStatus {
    ACTIVE("A"), DELETED("D");

    @Getter
    private String value;

    TaskStatus(String value) {
        this.value = value;
    }

    public static TaskStatus getFromValue(String value) {
        return Arrays.stream(values())
                .filter(entry -> entry.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
