package com.mutex.branch;

import javax.swing.*;
import java.awt.*;

public class BranchGUI implements BranchGUIListener {
    private JFrame frame;
    private JLabel statusLabel;
    private JLabel resourceLabel;
    private JButton requestButton;
    private BranchProcess branchProcess;

    public BranchGUI(String branchName) {
        branchProcess = new BranchProcess(branchName, this);

        frame = new JFrame(branchName + " - Branch Process");
        frame.setSize(400, 250);
        frame.setLayout(new GridLayout(3, 1));

        statusLabel = new JLabel("Status: Waiting", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(statusLabel);

        resourceLabel = new JLabel("Resource: None", SwingConstants.CENTER);
        resourceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        frame.add(resourceLabel);

        requestButton = new JButton("Request Resource");
        requestButton.addActionListener(e -> branchProcess.requestResource());
        frame.add(requestButton);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    @Override
    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText("Status: " + status));
    }

    @Override
    public void updateResource(String resource) {
        SwingUtilities.invokeLater(() -> resourceLabel.setText(resource));
    }

    public static void main(String[] args) {
        String branchName = (args.length > 0) ? args[0] : "bRanch#" + (int) (Math.random() * 100);
        new BranchGUI(branchName);
    }
}
