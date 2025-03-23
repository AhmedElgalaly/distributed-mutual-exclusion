package com.mutex.coordinator;

import java.util.LinkedList;
import java.util.Queue;

public class RequestQueue {
    private final Queue<String> queue = new LinkedList<>();

    public synchronized void addRequest(String branchName, int resourceId) {
        queue.add(branchName + ":" + resourceId);
    }

    public synchronized String getNextRequest() {
        return queue.poll();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized String getQueueState() {
        return queue.toString();
    }

    public synchronized String peekNextRequest() {
        return queue.peek();
    }
}
