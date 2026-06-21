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
    }
}
