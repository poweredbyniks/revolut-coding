package com.revolut.ledger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LedgerTest {

    @ParameterizedTest
    @CsvSource({"5, 50"})
    void testConcurrentTransfers(int numThreads, long transferAmount) throws InterruptedException {
        Account acc1 = new Account(1, 1000);
        Account acc2 = new Account(2, 1000);
        long initialTotal = acc1.getBalance() + acc2.getBalance();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            threads.add(new Thread(() -> Ledger.transferMoney(acc1, acc2, transferAmount)));
        }

        long startTime = System.currentTimeMillis();
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        long endTime = System.currentTimeMillis();

        long finalTotal = acc1.getBalance() + acc2.getBalance();
        assertEquals(initialTotal, finalTotal, "Total balance should remain constant");
        assertTrue((endTime - startTime) < 5000, "Potential deadlock detected");
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
    void testTransferZeroAmount() {
        Account acc1 = new Account(1, 100);
        Account acc2 = new Account(2, 100);
        assertTrue(Ledger.transferMoney(acc1, acc2, 0));
        assertEquals(100, acc1.getBalance());
        assertEquals(100, acc2.getBalance());
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
        assertTrue(endTime - startTime < 5000, "Potential deadlock detected");
    }
}
