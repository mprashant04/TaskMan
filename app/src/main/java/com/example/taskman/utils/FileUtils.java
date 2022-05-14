package com.example.taskman.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    public static long getFileCountRecursive(String dirPath) throws IOException {
        return Files.walk(Paths.get(dirPath))
                .parallel()
                .filter(p -> !p.toFile().isDirectory())
                .count();
    }

}
