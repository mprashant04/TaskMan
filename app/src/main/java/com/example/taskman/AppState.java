package com.example.taskman;

import com.example.taskman.common.OrderBy;

import lombok.Getter;
import lombok.Setter;

public class AppState {



    static class ListView {
        @Getter
        private static boolean showDeletedTasks;
        @Getter
        private static boolean showTime;
        @Setter
        @Getter
        private static OrderBy orderBy;

        static {
            ListView.reset();
        }

        public static void toggleShowDeletedTasks() {
            showDeletedTasks = !showDeletedTasks;
        }

        public static void toggleShowTime() {
            showTime = !showTime;
        }

        public static void reset() {
            showDeletedTasks = false;
            showTime = false;
            orderBy = OrderBy.TAG;
        }
    }


}
