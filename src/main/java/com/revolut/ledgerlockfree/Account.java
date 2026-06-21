package com.revolut.ledgerlockfree;

import java.math.BigDecimal;

public class Account {
    private final int accountId;
    BigDecimal balance;

    public Account(int accountId, BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    boolean withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) >= 0) {
            balance = balance.subtract(amount);
            return true;
        }
        return false;
    }

    void deposit(BigDecimal amount) {
        balance = balance.add(amount);
    }
}
