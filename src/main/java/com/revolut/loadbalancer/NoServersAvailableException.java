package com.revolut.loadbalancer;

public class NoServersAvailableException extends RuntimeException {
    public NoServersAvailableException() {
        super("No servers available");
    }
}
