package com.revolut.loadbalancer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoadBalancerTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void testLbInitRaises(int maxInstances) {
        assertThrows(IllegalArgumentException.class, () -> new LoadBalancer(maxInstances));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 100})
    void testLbInit(int maxInstances) {
        assertNotNull(new LoadBalancer(maxInstances));
    }

    @Test
    void testLbRegisterSingleServer() {
        LoadBalancer lb = new LoadBalancer(5);
        assertTrue(lb.register("server1"));
    }

    @Test
    void testLbRegisterCapacityLimit() {
        LoadBalancer lb = new LoadBalancer(2);
        assertTrue(lb.register("server1"));
        assertTrue(lb.register("server2"));
        assertFalse(lb.register("server3"));
    }

    @Test
    void testLbRegisterDuplicate() {
        LoadBalancer lb = new LoadBalancer(5);
        assertTrue(lb.register("server1"));
        assertTrue(lb.register("server2"));
        assertFalse(lb.register("server1"));
    }

    @Test
    void testLbUnregister() {
        LoadBalancer lb = new LoadBalancer();
        assertFalse(lb.unregister("server1"));
        lb.register("server1");
        assertFalse(lb.unregister("server2"));
        assertTrue(lb.unregister("server1"));
    }

    @Test
    void testLbGet() {
        LoadBalancer lb = new LoadBalancer();
        assertThrows(NoServersAvailableException.class, lb::get);
        List<String> servers = List.of("server1", "server2", "server3");
        for (String s : servers) lb.register(s);
        assertTrue(servers.contains(lb.get()));
    }

    @Test
    void testNoDeadlocks() throws InterruptedException {
        LoadBalancer lb = new LoadBalancer();
        Random random = new Random();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    String server = "server" + (random.nextInt(10) + 1);
                    lb.register(server);
                    lb.unregister(server);
                    try {
                        lb.get();
                    } catch (NoServersAvailableException ignored) {}
                }
            }));
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        assertTrue(true);
    }

    @Test
    void testRoundRobinStrategy() {
        RoundRobinSelectionStrategy strategy = new RoundRobinSelectionStrategy();
        LoadBalancer lb = new LoadBalancer(strategy);
        List<String> servers = new ArrayList<>();
        for (int i = 0; i < 10; i++) servers.add("server" + i);
        for (String s : servers) lb.register(s);

        List<String> got = new ArrayList<>();
        for (int i = 0; i < servers.size(); i++) got.add(lb.get());

        assertEquals(servers, got);
    }

    @Test
    void testRandomStrategy() {
        RandomSelectionStrategy strategy = new RandomSelectionStrategy();
        LoadBalancer lb = new LoadBalancer(strategy);
        Set<String> servers = new java.util.HashSet<>();
        Random random = new Random();

        for (int i = 0; i < 20; i++) {
            String server = "server" + (random.nextInt(10) + 1);
            servers.add(server);
            lb.register(server);
        }

        for (int i = 0; i < 10; i++) {
            assertTrue(servers.contains(lb.get()));
        }
    }

    @Test
    void testRandomStrategyEqualDistribution() {
        RandomSelectionStrategy strategy = new RandomSelectionStrategy();
        LoadBalancer lb = new LoadBalancer(strategy);
        List<String> servers = new ArrayList<>();
        for (int i = 0; i < 10; i++) servers.add("server" + i);
        for (String s : servers) lb.register(s);

        Map<String, Integer> results = new HashMap<>();
        for (String s : servers) results.put(s, 0);

        int numTrials = 100000;
        for (int i = 0; i < numTrials; i++) {
            String got = lb.get();
            results.put(got, results.get(got) + 1);
        }

        double expectedFreq = (double) numTrials / servers.size();
        double maxDeviation = results.values().stream()
            .mapToDouble(freq -> Math.abs((freq - expectedFreq) / expectedFreq))
            .max()
            .orElse(0);

        assertTrue(maxDeviation < 0.5);
    }

    @Test
    void testReRegisterAfterUnregister() {
        LoadBalancer lb = new LoadBalancer(2);
        lb.register("server1");
        lb.register("server2");
        assertFalse(lb.register("server3"));
        lb.unregister("server1");
        assertTrue(lb.register("server3"));
    }

    @Test
    void testGetWithSingleServer() {
        LoadBalancer lb = new LoadBalancer(new RoundRobinSelectionStrategy());
        lb.register("only-server");
        assertEquals("only-server", lb.get());
        assertEquals("only-server", lb.get());
        assertEquals("only-server", lb.get());
    }

    @Test
    void testGetAfterAllServersRemovedThrows() {
        LoadBalancer lb = new LoadBalancer();
        lb.register("server1");
        lb.unregister("server1");
        assertThrows(NoServersAvailableException.class, lb::get);
    }

    @Test
    void testRoundRobinAfterUnregister() {
        LoadBalancer lb = new LoadBalancer(new RoundRobinSelectionStrategy());
        lb.register("server0");
        lb.register("server1");
        lb.register("server2");
        lb.get(); // server0
        lb.get(); // server1
        lb.unregister("server1");
        String next = lb.get();
        assertTrue(List.of("server0", "server2").contains(next));
    }
}
