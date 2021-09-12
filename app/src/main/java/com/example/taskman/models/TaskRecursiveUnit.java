package com.example.taskman.models;

import java.util.Arrays;

import lombok.Getter;

public enum TaskRecursiveUnit {
    DAY("D"), MONTH("M");

    @Getter
    private String value;

    TaskRecursiveUnit(String value) {
        this.value = value;
    }

    public static TaskRecursiveUnit getFromValue(String value) {
        return Arrays.stream(values())
                .filter(entry -> entry.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
