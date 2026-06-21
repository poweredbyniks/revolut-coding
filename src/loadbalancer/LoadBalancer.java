package com.revolut.loadbalancer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class LoadBalancer {
    private final int maxInstances;
    private final List<String> instances = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final ServerSelectionStrategy strategy;

    public LoadBalancer() {
        this(10, null);
    }

    public LoadBalancer(int maxInstances) {
        this(maxInstances, null);
    }

    public LoadBalancer(ServerSelectionStrategy strategy) {
        this(10, strategy);
    }

    public LoadBalancer(int maxInstances, ServerSelectionStrategy strategy) {
        if (maxInstances <= 0) throw new IllegalArgumentException("Work with Positive Numbers Only");
        this.maxInstances = maxInstances;
        this.strategy = strategy != null ? strategy : new RoundRobinSelectionStrategy();
    }

    public boolean register(String instance) {
        lock.lock();
        try {
            if (instances.size() >= maxInstances || instances.contains(instance)) return false;
            instances.add(instance);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean unregister(String instance) {
        lock.lock();
        try {
            return instances.remove(instance);
        } finally {
            lock.unlock();
        }
    }

    public String get() {
        lock.lock();
        try {
            return strategy.selectServer(instances);
        } finally {
            lock.unlock();
        }
    }
}
