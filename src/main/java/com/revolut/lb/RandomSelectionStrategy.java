package com.revolut.lb;

import java.util.List;
import java.util.Random;

public class RandomSelectionStrategy implements ServerSelectionStrategy {
    private final Random random = new Random();

    @Override
    public String selectServer(List<String> instances) {
        if (instances.isEmpty()) throw new NoServersAvailableException();
        return instances.get(random.nextInt(instances.size()));
    }
}
