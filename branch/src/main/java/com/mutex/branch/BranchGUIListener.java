package com.mutex.branch;

public interface BranchGUIListener {
    void updateStatus(String status);
    void updateResource(int resourceId, String message);
}
