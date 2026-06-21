package com.revolut.ledgercas;

public class Ledger {
    public static boolean transferMoney(Account from, Account to, long amount) {
        while (true) {
            long oldBalance = from.getBalance();
            if (oldBalance < amount) return false;
            if (from.compareAndSwap(oldBalance, oldBalance - amount)) {
                long toOld;
                do {
                    toOld = to.getBalance();
                } while (!to.compareAndSwap(toOld, toOld + amount));
                return true;
            }
        }
    }
}
