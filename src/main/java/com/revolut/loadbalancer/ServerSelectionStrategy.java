package com.revolut.loadbalancer;

import java.util.List;

public interface ServerSelectionStrategy {
    String selectServer(List<String> instances);
}
