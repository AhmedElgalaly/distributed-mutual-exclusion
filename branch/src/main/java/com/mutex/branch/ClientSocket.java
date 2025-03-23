package com.mutex.branch;

import java.io.*;
import java.net.*;

public class ClientSocket {
    private String host;
    private int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to " + host + ":" + port);
            return false;
        }
    }

    public String sendMessage(String message) {
        try {
            if (out != null) {
                out.println(message);
                return in.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error in communication: " + e.getMessage());
        }
        return null;
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}
