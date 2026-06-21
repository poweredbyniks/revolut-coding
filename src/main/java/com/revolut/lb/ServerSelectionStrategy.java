package com.revolut.lb;

import java.util.List;

public interface ServerSelectionStrategy {
    String selectServer(List<String> instances);
}
