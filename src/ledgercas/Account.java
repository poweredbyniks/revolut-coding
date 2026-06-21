package com.revolut.ledgercas;

import java.util.concurrent.atomic.AtomicLong;

public class Account {
    private final int accountId;
    private final AtomicLong balance;

    public Account(int accountId, long balance) {
        this.accountId = accountId;
        this.balance = new AtomicLong(balance);
    }

    public long getBalance() {
        return balance.get();
    }

    public boolean compareAndSwap(long expected, long newValue) {
        return balance.compareAndSet(expected, newValue);
    }

    // Unconditional, so getAndAdd's built-in retry loop is sufficient (no precondition to enforce).
    public void deposit(long amount) {
        balance.getAndAdd(amount);
    }
}
