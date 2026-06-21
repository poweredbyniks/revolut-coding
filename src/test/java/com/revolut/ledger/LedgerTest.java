package com.revolut.ledger;

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
}
