package com.revolut.ledgerlockfree;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Ledger {
    private record TransferRequest(Account from, Account to, BigDecimal amount) {}
    // hand-off of transfer requests to the single worker thread
    private final BlockingQueue<TransferRequest> queue = new LinkedBlockingQueue<>();
    // count of submitted-but-not-yet-applied transfers, used by join()
    private final AtomicInteger pending = new AtomicInteger(0);
    // monitor guarding pending and join()'s wait/notify
    private final Object joinLock = new Object();
    // single thread that serially applies every queued transfer
    private final Thread worker;

    public Ledger() {
        worker = new Thread(this::processTransactions);
        worker.start();
    }

    public void shutdown() throws InterruptedException {
        worker.interrupt();
        worker.join();
    }

    public void transferMoney(Account from, Account to, BigDecimal amount) {
        if (from == null || to == null) throw new IllegalArgumentException("Accounts must not be null");
        if (amount.signum() < 0) throw new IllegalArgumentException("Amount must not be negative");

        // Count the request as pending before it's queued so join() can't observe pending == 0 too early.
        pending.incrementAndGet();
        try {
            queue.put(new TransferRequest(from, to, amount));
        } catch (InterruptedException e) {
            pending.decrementAndGet();
            Thread.currentThread().interrupt();
        }
    }

    private void processTransactions() {
        // A single worker applies every transfer, so withdraw/deposit need no locking on Account itself.
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TransferRequest req = queue.take();
                if (req.from().withdraw(req.amount())) {
                    req.to().deposit(req.amount());
                }
                synchronized (joinLock) {
                    if (pending.decrementAndGet() == 0) {
                        joinLock.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void join() throws InterruptedException {
        synchronized (joinLock) {
            // Loop guards against spurious wakeups / new work queued while waiting.
            while (pending.get() > 0) {
                joinLock.wait();
            }
        }
    }
}
