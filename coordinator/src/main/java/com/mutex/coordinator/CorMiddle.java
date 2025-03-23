package com.mutex.coordinator;

public class CorMiddle {
    private static final int PORT = 6000;
    private static final String RESOURCES_HOST = "127.0.0.1";
    private static final int RESOURCES_PORT = 1000;


    public static void main(String[] args) {
        RequestQueue requestQueue = new RequestQueue();
        GUI gui = new GUI();
        LogManager logManager = new LogManager();

        MutexServer server = new MutexServer(PORT, RESOURCES_HOST, RESOURCES_PORT, gui);
        server.start();
    }
}
