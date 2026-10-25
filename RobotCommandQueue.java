package com.aura.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public final class RobotCommandQueue {

    private static final int MAX_QUEUE_SIZE = 20;

    private final Queue<RobotCommand> queue = new ArrayDeque<>();

    public synchronized boolean enqueue(RobotCommand command) {
        if (command == null) {
            return false;
        }

        if (command.isEmergencyStop()) {
            queue.clear();
            queue.offer(command);
            return true;
        }

        if (queue.size() >= MAX_QUEUE_SIZE) {
            return false;
        }

        queue.offer(command);
        return true;
    }

    public synchronized RobotCommand poll() {
        return queue.poll();
    }

    public synchronized RobotCommand peek() {
        return queue.peek();
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void clear() {
        queue.clear();
    }

    public synchronized List<RobotCommand> snapshot() {
        return new ArrayList<>(queue);
    }
}