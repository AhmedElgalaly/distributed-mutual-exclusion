package com.mutex.coordinator;

import java.io.FileWriter;
import java.io.IOException;

public class LogManager {
    private static final String FILE_PATH = "coordinator_logs.csv";

    public synchronized void logToCSV(String branch, String ip, String action, int resourceId) {
        try (FileWriter writer = new FileWriter(FILE_PATH, true)) {
            writer.append(branch).append(",")
                    .append(ip).append(",")
                    .append(action).append(",")
                    .append(String.valueOf(resourceId)).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
