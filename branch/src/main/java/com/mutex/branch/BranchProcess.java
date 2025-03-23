package com.mutex.branch;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BranchProcess {
    private static final String COORDINATOR_HOST = "coordinator";
    private static final int COORDINATOR_PORT = 6000;
    private static final int RESOURCE_COUNT = 2;

    private String branchName;
    private ClientSocket clientSocket;
    private Random random;
    private BranchGUIListener guiListener; // Interface for GUI updates

    public BranchProcess(String branchName, BranchGUIListener guiListener) {
        this.branchName = branchName;
        this.clientSocket = new ClientSocket(COORDINATOR_HOST, COORDINATOR_PORT);
        this.random = new Random();
        this.guiListener = guiListener;
    }

    public void requestResource() {
        new Thread(() -> {
            int resourceId = random.nextInt(RESOURCE_COUNT) + 1;
            guiListener.updateStatus("Requesting Resource " + resourceId);

            if (!clientSocket.connect()) {
                guiListener.updateStatus("Connection Failed");
                return;
            }

            String response = clientSocket.sendMessage("REQUEST:" + branchName + ":" + resourceId);
            if ("GRANTED".equals(response)) {
                guiListener.updateStatus("Access Granted");
                guiListener.updateResource("Resource: " + resourceId);

                // Simulate working on resource
                try {
                    TimeUnit.SECONDS.sleep(random.nextInt(3) + 2);
                } catch (InterruptedException ignored) {}

                clientSocket.sendMessage("RELEASE:" + branchName + ":" + resourceId);
                guiListener.updateStatus("Released Resource");
                guiListener.updateResource("Resource: None");
            } else {
                guiListener.updateStatus("Request Queued");
            }

            clientSocket.close();
        }).start();
    }

    public static void main(String[] args) {
        String branchName = (args.length > 0) ? args[0] : "bRanch#" + new Random().nextInt(100);
        BranchGUI gui = new BranchGUI(branchName);
        new BranchProcess(branchName, gui);
    }
}
