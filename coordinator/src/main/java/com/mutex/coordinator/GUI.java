package com.mutex.coordinator;

import javax.swing.*;
import java.awt.*;

public class GUI {
    private JFrame frame;
    private JTextArea logArea;

    public GUI() {
        frame = new JFrame("Coordinator - Resource Manager");
        frame.setSize(500, 300);
        frame.setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void logAction(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
}
