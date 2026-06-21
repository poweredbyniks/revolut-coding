package com.revolut.ledger;

public class Ledger {
    public static boolean transferMoney(Account from, Account to, long amount) {
        if (from == null || to == null) throw new IllegalArgumentException("Accounts must not be null");
        if (amount < 0) throw new IllegalArgumentException("Amount must not be negative");

        Account first = from.getAccountId() < to.getAccountId() ? from : to;
        Account second = from.getAccountId() < to.getAccountId() ? to : from;

        first.lock.lock();
        try {
            second.lock.lock();
            try {
                if (from.withdraw(amount)) {
                    to.deposit(amount);
                    return true;
                }
                return false;
            } finally {
                second.lock.unlock();
            }
        } finally {
            first.lock.unlock();
        }
    }
}
