package com.revolut.ledger;

import java.util.concurrent.locks.ReentrantLock;

public class Account {
    private final int accountId;
    private long balance;
    final ReentrantLock lock = new ReentrantLock();

    public Account(int accountId, long balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public void deposit(long amount) {
        lock.lock();
        try {
            balance += amount;
        } finally {
            lock.unlock();
        }
    }

    public boolean withdraw(long amount) {
        lock.lock();
        try {
            if (balance >= amount) {
                balance -= amount;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public long getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public int getAccountId() {
        return accountId;
    }
}
