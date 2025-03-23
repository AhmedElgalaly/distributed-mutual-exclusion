package com.mutex.branch;

import java.io.*;
import java.net.*;

public class BranchProcess {
    private final String branchName;
    private final String coordinatorHost;
    private final int coordinatorPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final BranchGUIListener guiListener;

    public BranchProcess(String branchName, String coordinatorHost, int coordinatorPort, BranchGUIListener guiListener) {
        this.branchName = branchName;
        this.coordinatorHost = coordinatorHost;
        this.coordinatorPort = coordinatorPort;
        this.guiListener = guiListener;
    }

    public boolean connect() {
        try {
            socket = new Socket(coordinatorHost, coordinatorPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        } catch (IOException e) {
            guiListener.updateStatus("Failed to connect to Coordinator");
            return false;
        }
    }

    public void requestResource(int resourceId) {
        new Thread(() -> {
            guiListener.updateStatus("Requesting Resource " + resourceId);

            if (!ensureConnected()) {
                return;
            }

            String response = sendMessage("REQUEST:" + branchName + ":" + resourceId);
            if ("GRANTED".equals(response)) {
                guiListener.updateStatus("Resource " + resourceId + " Granted");
                guiListener.updateResource(resourceId, "Held");
            } else if ("QUEUED".equals(response)) {
                guiListener.updateStatus("Waiting for Resource " + resourceId);
            }
        }).start();
    }

    public void releaseResource(int resourceId) {
        new Thread(() -> {
            guiListener.updateStatus("Releasing Resource " + resourceId);

            if (!ensureConnected()) {
                return;
            }

            String response = sendMessage("RELEASE:" + branchName + ":" + resourceId);
            if ("RELEASE_SUCCESS".equals(response)) {
                guiListener.updateStatus("Resource " + resourceId + " Released");
                guiListener.updateResource(resourceId, "Available");
            } else {
                guiListener.updateStatus("Error Releasing Resource: " + response);
            }
        }).start();
    }

    private String sendMessage(String message) {
        try {
            out.println(message);
            return in.readLine();
        } catch (IOException e) {
            return "ERROR";
        }
    }

    private boolean ensureConnected() {
        if (socket == null || socket.isClosed()) {
            return connect();
        }
        return true;
    }
}
