package com.revolut.loadbalancer;

import java.util.List;

public class RoundRobinSelectionStrategy implements ServerSelectionStrategy {
    private int index = -1;

    @Override
    public String selectServer(List<String> instances) {
        if (instances.isEmpty()) throw new NoServersAvailableException();
        index = (index + 1) % instances.size();
        return instances.get(index);
    }
}
