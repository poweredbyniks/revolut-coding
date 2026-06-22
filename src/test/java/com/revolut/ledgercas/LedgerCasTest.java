package com.revolut.ledgercas;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class LedgerCasTest {

    @Test
    void testLedgerMoneyTransferConcurrency() throws InterruptedException {
        Account acc1 = new Account(1, 1000);
        Account acc2 = new Account(2, 500);
        long initialTotal = acc1.getBalance() + acc2.getBalance();
        int numThreads = 20;
        long amount = 50;

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            threads.add(new Thread(() -> Ledger.transferMoney(acc1, acc2, amount)));
        }

        long startTime = System.currentTimeMillis();
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        long endTime = System.currentTimeMillis();

        long finalTotal = acc1.getBalance() + acc2.getBalance();
        assertEquals(initialTotal, finalTotal);
        assertTrue(endTime - startTime < 5000);
    }

    @Test
    void testTransferInsufficientFunds() {
        Account acc1 = new Account(1, 30);
        Account acc2 = new Account(2, 100);
        assertFalse(Ledger.transferMoney(acc1, acc2, 50));
        assertEquals(30, acc1.getBalance());
        assertEquals(100, acc2.getBalance());
    }

    @Test
    void testTransferExactBalance() {
        Account acc1 = new Account(1, 100);
        Account acc2 = new Account(2, 0);
        assertTrue(Ledger.transferMoney(acc1, acc2, 100));
        assertEquals(0, acc1.getBalance());
        assertEquals(100, acc2.getBalance());
    }

    @Test
    void testTransferNegativeAmountThrows() {
        Account acc1 = new Account(1, 100);
        Account acc2 = new Account(2, 100);
        assertThrows(IllegalArgumentException.class, () -> Ledger.transferMoney(acc1, acc2, -10));
        assertEquals(100, acc1.getBalance());
        assertEquals(100, acc2.getBalance());
    }

    @Test
    void testTransferNullAccountThrows() {
        Account acc1 = new Account(1, 100);
        assertThrows(IllegalArgumentException.class, () -> Ledger.transferMoney(null, acc1, 10));
        assertThrows(IllegalArgumentException.class, () -> Ledger.transferMoney(acc1, null, 10));
    }

    @Test
    void testTransferToSameAccountIsNoOp() {
        Account acc1 = new Account(1, 100);
        assertTrue(Ledger.transferMoney(acc1, acc1, 50));
        assertEquals(100, acc1.getBalance());
    }

    @Test
    void testBidirectionalConcurrentTransfers() throws InterruptedException {
        Account acc1 = new Account(1, 1000);
        Account acc2 = new Account(2, 1000);
        long initialTotal = acc1.getBalance() + acc2.getBalance();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(() -> Ledger.transferMoney(acc1, acc2, 10)));
            threads.add(new Thread(() -> Ledger.transferMoney(acc2, acc1, 10)));
        }

        long startTime = System.currentTimeMillis();
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        long endTime = System.currentTimeMillis();

        assertEquals(initialTotal, acc1.getBalance() + acc2.getBalance());
        assertTrue(endTime - startTime < 5000);
    }
}
