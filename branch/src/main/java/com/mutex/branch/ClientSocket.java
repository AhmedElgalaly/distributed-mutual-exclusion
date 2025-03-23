package com.mutex.branch;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class ClientSocket {
    private final String coordinatorHost;
    private final int coordinatorPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientSocket(String coordinatorHost, int coordinatorPort) {
        this.coordinatorHost = coordinatorHost;
        this.coordinatorPort = coordinatorPort;
        connect();
    }

    public boolean connect() {
        if (isConnected()) return true;

        try {
            socket = new Socket(coordinatorHost, coordinatorPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to Coordinator: " + e.getMessage());
            return false;
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public String sendMessage(String message) {
        if (!isConnected()) return null;

        try {
            out.println(message);
            out.flush();
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            return null;
        }
    }

    public void listenForMessages(Consumer<String> messageHandler) {
        new Thread(() -> {
            try {
                String message;
                while (isConnected() && (message = in.readLine()) != null) {
                    messageHandler.accept(message);
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
            }
        }).start();
    }

    public void close() {
        try {
            if (socket != null) socket.close();
            if (out != null) out.close();
            if (in != null) in.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
