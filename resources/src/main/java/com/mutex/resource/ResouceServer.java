package com.mutex.resource;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

class ResourceServer {
    private static final int PORT = 1000;
    private static ConcurrentHashMap<Integer, String> resourceOwners = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Resource Server running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ResourceHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ResourceHandler implements Runnable {
        private Socket socket;

        public ResourceHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String request = in.readLine();
                if (request == null) return;

                String[] parts = request.split(":");
                String command = parts[0];
                int resourceId = Integer.parseInt(parts[1]);
                String branchName = parts.length > 2 ? parts[2] : "";

                if ("REQUEST".equals(command)) {
                    synchronized (resourceOwners) {
                        if (!resourceOwners.containsKey(resourceId)) {
                            resourceOwners.put(resourceId, branchName);
                            System.out.println("LOG: Resource " + resourceId + " allocated to " + branchName);
                            out.println("GRANTED");
                        } else {
                            out.println("DENIED");
                        }
                    }
                } else if ("RELEASE".equals(command)) {
                    synchronized (resourceOwners) {
                        if (resourceOwners.containsKey(resourceId) && resourceOwners.get(resourceId).equals(branchName)) {
                            resourceOwners.remove(resourceId);
                            System.out.println("LOG: Resource " + resourceId + " released by " + branchName);
                            out.println("RELEASE_SUCCESS");
                        } else {
                            out.println("RELEASE_FAILED");
                        }
                    }
                }else if ("CHECK".equals(command)) {
                    synchronized (resourceOwners) {
                        if (!resourceOwners.containsKey(resourceId)) {
                            out.println("AVAILABLE");
                        } else {
                            out.println("OCCUPIED");
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
