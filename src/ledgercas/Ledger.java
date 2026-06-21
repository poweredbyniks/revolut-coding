package com.revolut.ledgercas;

public class Ledger {
    public static boolean transferMoney(Account from, Account to, long amount) {
        if (from == null || to == null) throw new IllegalArgumentException("Accounts must not be null");
        if (amount < 0) throw new IllegalArgumentException("Amount must not be negative");
        //without locking: optimistic retry pattern. it only loops when another thread mutates balance
        while (true) {
            long oldBalance = from.getBalance();
            if (oldBalance < amount) return false;
            // CAS fails if another thread changed the balance since the read above; retry with the fresh value.
            if (from.compareAndSwap(oldBalance, oldBalance - amount)) { // 3. swap if unchanged
                to.deposit(amount);
                return true;
            }
            // else: another thread changed balance between steps 1 and 3 — retry
        }
    }
}
