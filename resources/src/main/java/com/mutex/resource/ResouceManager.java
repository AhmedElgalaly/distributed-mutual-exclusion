package com.mutex.resource;

import java.util.HashMap;
import java.util.Map;

class ResourceManager {
    private final Map<Integer, String> resourceOwners = new HashMap<>(); // Resource ID → Branch Name

    public synchronized boolean allocateResource(String branchName, int resourceId) {
        if (!resourceOwners.containsKey(resourceId)) { // If resource is free
            resourceOwners.put(resourceId, branchName);
            return true;
        }
        return false;
    }

    public synchronized boolean releaseResource(String branchName, int resourceId) {
        if (resourceOwners.containsKey(resourceId) && resourceOwners.get(resourceId).equals(branchName)) {
            resourceOwners.remove(resourceId);
            return true;
        }
        return false;
    }

    public synchronized void releaseAllResources(String branchName) {
        resourceOwners.entrySet().removeIf(entry -> entry.getValue().equals(branchName));
    }

    public synchronized String getResourceOwner(int resourceId) {
        return resourceOwners.getOrDefault(resourceId, "None");
    }

    public synchronized boolean isResourceFree(int resourceId) {
        return !resourceOwners.containsKey(resourceId);
    }
}
