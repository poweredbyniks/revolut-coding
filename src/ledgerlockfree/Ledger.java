package com.revolut.ledgerlockfree;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Ledger {
    private record TransferRequest(Account from, Account to, BigDecimal amount) {}

    private final BlockingQueue<TransferRequest> queue = new LinkedBlockingQueue<>();
    private final AtomicInteger pending = new AtomicInteger(0);
    private final Object joinLock = new Object();
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
        pending.incrementAndGet();
        try {
            queue.put(new TransferRequest(from, to, amount));
        } catch (InterruptedException e) {
            pending.decrementAndGet();
            Thread.currentThread().interrupt();
        }
    }

    private void processTransactions() {
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
            while (pending.get() > 0) {
                joinLock.wait();
            }
        }
    }
}
