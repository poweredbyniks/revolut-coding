package com.revolut.lb;

public class NoServersAvailableException extends RuntimeException {
    public NoServersAvailableException() {
        super("No servers available");
    }
}
