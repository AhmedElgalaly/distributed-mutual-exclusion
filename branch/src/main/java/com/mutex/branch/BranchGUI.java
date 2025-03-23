package com.mutex.branch;

import javax.swing.*;
import java.awt.*;

public class BranchGUI extends JFrame implements BranchGUIListener {
    private final BranchProcess branchProcess;
    private final JLabel statusLabel;
    private final JButton requestResource1Button, requestResource2Button;
    private final JButton releaseResource1Button, releaseResource2Button;
    private final JTextField resourceField;
    private final JButton requestButton, releaseButton;
    private final DefaultListModel<String> resourceListModel;
    private final JList<String> resourceList;

    public BranchGUI(String branchName, String coordinatorHost, int coordinatorPort) {
        super("Branch: " + branchName);
        this.branchProcess = new BranchProcess(branchName, coordinatorHost, coordinatorPort, this);

        setLayout(new BorderLayout());
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Status Label
        statusLabel = new JLabel("Status: Disconnected");
        add(statusLabel, BorderLayout.NORTH);

        // Resource List
        resourceListModel = new DefaultListModel<>();
        resourceList = new JList<>(resourceListModel);
        add(new JScrollPane(resourceList), BorderLayout.CENTER);

        // Bottom Panel with Input and Buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(4, 2, 5, 5));

        resourceField = new JTextField(5);
        requestButton = new JButton("Request Resource");
        releaseButton = new JButton("Release Resource");

        requestResource1Button = new JButton("Request Resource 1");
        requestResource2Button = new JButton("Request Resource 2");
        releaseResource1Button = new JButton("Release Resource 1");
        releaseResource2Button = new JButton("Release Resource 2");

        bottomPanel.add(new JLabel("Resource ID:"));
        bottomPanel.add(resourceField);
        bottomPanel.add(requestButton);
        bottomPanel.add(releaseButton);
        bottomPanel.add(requestResource1Button);
        bottomPanel.add(releaseResource1Button);
        bottomPanel.add(requestResource2Button);
        bottomPanel.add(releaseResource2Button);

        add(bottomPanel, BorderLayout.SOUTH);

        // Connect to Coordinator
        if (branchProcess.connect()) {
            updateStatus("Connected to Coordinator");
        } else {
            updateStatus("Failed to connect");
        }

        // Button Listeners
        requestButton.addActionListener(e -> requestResource());
        releaseButton.addActionListener(e -> releaseResource());
        requestResource1Button.addActionListener(e -> branchProcess.requestResource(1));
        releaseResource1Button.addActionListener(e -> branchProcess.releaseResource(1));
        requestResource2Button.addActionListener(e -> branchProcess.requestResource(2));
        releaseResource2Button.addActionListener(e -> branchProcess.releaseResource(2));

        setVisible(true);
    }

    private void requestResource() {
        try {
            int resourceId = Integer.parseInt(resourceField.getText());
            branchProcess.requestResource(resourceId);
        } catch (NumberFormatException e) {
            updateStatus("Invalid resource ID");
        }
    }

    private void releaseResource() {
        try {
            int resourceId = Integer.parseInt(resourceField.getText());
            branchProcess.releaseResource(resourceId);
        } catch (NumberFormatException e) {
            updateStatus("Invalid resource ID");
        }
    }

    @Override
    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText("Status: " + status));
    }

    @Override
    public void updateResource(int resourceId, String status) {
        SwingUtilities.invokeLater(() -> {
            String resourceEntry = "Resource " + resourceId + ": " + status;
            for (int i = 0; i < resourceListModel.size(); i++) {
                if (resourceListModel.get(i).startsWith("Resource " + resourceId)) {
                    resourceListModel.set(i, resourceEntry);
                    return;
                }
            }
            resourceListModel.addElement(resourceEntry);
        });
    }

    // Main method to start the GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String branchName = (args.length > 0) ? args[0] : "Branch" + (int) (Math.random() * 1000);
            new BranchGUI(branchName, "127.0.0.1", 6000);
        });
    }
}
