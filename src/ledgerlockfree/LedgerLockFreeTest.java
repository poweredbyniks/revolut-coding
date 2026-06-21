package com.revolut.ledgerlockfree;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LedgerLockFreeTest {

    @Test
    void testLedgerMoneyTransferConcurrency() throws InterruptedException {
        Account acc1 = new Account(1, new BigDecimal("1000"));
        Account acc2 = new Account(2, new BigDecimal("500"));
        Ledger ledger = new Ledger();
        BigDecimal initialTotal = acc1.getBalance().add(acc2.getBalance());
        int numThreads = 20;
        BigDecimal amount = new BigDecimal("50");

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            threads.add(new Thread(() -> ledger.transferMoney(acc1, acc2, amount)));
        }

        long startTime = System.currentTimeMillis();
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        ledger.join();
        long endTime = System.currentTimeMillis();

        BigDecimal finalTotal = acc1.getBalance().add(acc2.getBalance());
        assertEquals(0, initialTotal.compareTo(finalTotal));
        assertTrue(endTime - startTime < 5000);
        ledger.shutdown();
    }

    @Test
    void testTransferInsufficientFundsPreservesBalance() throws InterruptedException {
        Account acc1 = new Account(1, new BigDecimal("30"));
        Account acc2 = new Account(2, new BigDecimal("100"));
        Ledger ledger = new Ledger();
        ledger.transferMoney(acc1, acc2, new BigDecimal("50"));
        ledger.join();
        assertEquals(0, new BigDecimal("30").compareTo(acc1.getBalance()));
        assertEquals(0, new BigDecimal("100").compareTo(acc2.getBalance()));
        ledger.shutdown();
    }

    @Test
    void testTransferNegativeAmountThrows() throws InterruptedException {
        Account acc1 = new Account(1, new BigDecimal("100"));
        Account acc2 = new Account(2, new BigDecimal("100"));
        Ledger ledger = new Ledger();
        assertThrows(IllegalArgumentException.class,
                () -> ledger.transferMoney(acc1, acc2, new BigDecimal("-10")));
        ledger.shutdown();
    }

    @Test
    void testTransferNullAccountThrows() throws InterruptedException {
        Account acc1 = new Account(1, new BigDecimal("100"));
        Ledger ledger = new Ledger();
        assertThrows(IllegalArgumentException.class,
                () -> ledger.transferMoney(null, acc1, new BigDecimal("10")));
        assertThrows(IllegalArgumentException.class,
                () -> ledger.transferMoney(acc1, null, new BigDecimal("10")));
        ledger.shutdown();
    }

    @Test
    void testTransferToSameAccountIsNoOp() throws InterruptedException {
        Account acc1 = new Account(1, new BigDecimal("100"));
        Ledger ledger = new Ledger();
        ledger.transferMoney(acc1, acc1, new BigDecimal("50"));
        ledger.join();
        assertEquals(0, new BigDecimal("100").compareTo(acc1.getBalance()));
        ledger.shutdown();
    }

    @Test
    void testBidirectionalConcurrentTransfers() throws InterruptedException {
        Account acc1 = new Account(1, new BigDecimal("1000"));
        Account acc2 = new Account(2, new BigDecimal("1000"));
        Ledger ledger = new Ledger();
        BigDecimal initialTotal = acc1.getBalance().add(acc2.getBalance());

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(() -> ledger.transferMoney(acc1, acc2, new BigDecimal("10"))));
            threads.add(new Thread(() -> ledger.transferMoney(acc2, acc1, new BigDecimal("10"))));
        }

        long startTime = System.currentTimeMillis();
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        ledger.join();
        long endTime = System.currentTimeMillis();

        assertEquals(0, initialTotal.compareTo(acc1.getBalance().add(acc2.getBalance())));
        assertTrue(endTime - startTime < 5000);
        ledger.shutdown();
    }
}
