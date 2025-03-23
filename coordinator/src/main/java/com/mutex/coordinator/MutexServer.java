package com.mutex.coordinator;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;
import java.util.Queue;

public class MutexServer {
    private final int port;
    private final String resourceServerHost;
    private final int resourceServerPort;
    private final GUI gui;
    private final ConcurrentHashMap<Integer, String> waitingBranches = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PrintWriter> branchConnections = new ConcurrentHashMap<>();

    public MutexServer(int port, String resourceServerHost, int resourceServerPort, GUI gui) {
        this.port = port;
        this.resourceServerHost = resourceServerHost;
        this.resourceServerPort = resourceServerPort;
        this.gui = gui;
    }

    public void start() {
        if (!checkResourceServerConnection()) {
            gui.logAction("ERROR: Resource Server is not available! Coordinator shutting down...");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            gui.logAction("Coordinator running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            gui.logAction("Error starting server: " + e.getMessage());
        }
    }

    private boolean checkResourceServerConnection() {
        try (Socket socket = new Socket(resourceServerHost, resourceServerPort)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private String branchName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String request;
                while ((request = in.readLine()) != null) {
                    String[] parts = request.split(":");
                    String command = parts[0];
                    branchName = parts[1];
                    int resourceId = Integer.parseInt(parts[2]);

                    switch (command) {
                        case "REQUEST":
                            handleRequest(out, branchName, resourceId);
                            break;
                        case "RELEASE":
                            handleRelease(out, branchName, resourceId);
                            break;
                    }
                }
            } catch (IOException e) {
                gui.logAction("Branch " + branchName + " disconnected! Releasing resources...");
                handleBranchDisconnection(branchName);
            }
        }

        private void handleRequest(PrintWriter out, String branchName, int resourceId) {
            synchronized (waitingBranches) {
                String response = communicateWithResourceServer("REQUEST:" + resourceId + ":" + branchName);

                if ("GRANTED".equals(response)) {
                    gui.logAction("Resource " + resourceId + " allocated to " + branchName);
                    out.println("GRANTED");
                } else {
                    waitingBranches.put(resourceId, branchName);
                    gui.logAction(branchName + " queued for Resource " + resourceId);
                    out.println("QUEUED");
                }
            }
        }

        private void handleRelease(PrintWriter out, String branchName, int resourceId) {
            synchronized (waitingBranches) {
                String response = communicateWithResourceServer("RELEASE:" + resourceId + ":" + branchName);

                if ("RELEASE_SUCCESS".equals(response)) {
                    gui.logAction("Resource " + resourceId + " released by " + branchName);
                    out.println("RELEASE_SUCCESS");
                    assignNextRequest(resourceId);
                } else {
                    out.println("RELEASE_FAILED");
                }
            }
        }

        private void assignNextRequest(int resourceId) {
            synchronized (waitingBranches) {
                if (waitingBranches.containsKey(resourceId)) {
                    String nextBranch = waitingBranches.remove(resourceId);
                    String response = communicateWithResourceServer("REQUEST:" + resourceId + ":" + nextBranch);

                    if ("GRANTED".equals(response)) {
                        gui.logAction("Resource " + resourceId + " granted to " + nextBranch);
                        notifyBranch(nextBranch, "GRANTED:" + resourceId);
                    } else {
                        waitingBranches.put(resourceId, nextBranch); // Requeue if request failed
                    }
                }
            }
        }

        private void handleBranchDisconnection(String branchName) {
            gui.logAction("Handling disconnection of " + branchName);
            branchConnections.remove(branchName);
            communicateWithResourceServer("RELEASE_ALL:" + branchName);
        }

        private void notifyBranch(String branchName, String message) {
            PrintWriter out = branchConnections.get(branchName);
            if (out != null) {
                out.println(message);
                out.flush();
                gui.logAction("Notified " + branchName + " with message: " + message);
            } else {
                gui.logAction("Failed to notify " + branchName + ": No connection found.");
            }
        }
    }

    private String communicateWithResourceServer(String message) {
        try (Socket socket = new Socket(resourceServerHost, resourceServerPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(message);
            return in.readLine();
        } catch (IOException e) {
            gui.logAction("ERROR: Failed to communicate with Resource Server!");
            return "ERROR";
        }
    }
}
