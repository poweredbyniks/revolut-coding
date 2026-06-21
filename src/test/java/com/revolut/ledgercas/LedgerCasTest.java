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
}
