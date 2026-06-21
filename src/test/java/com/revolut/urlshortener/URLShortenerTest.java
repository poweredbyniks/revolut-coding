package com.revolut.urlshortener;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class URLShortenerTest {

    private static final List<String> TEST_URLS = List.of(
        "https://example.com",
        "https://test.com/page1",
        "https://another-example.org/about"
    );

    @Test
    void testCounterStrategyUniqueness() {
        URLShortener shortener = new URLShortener(new CounterStrategy());
        Set<String> shortUrls = new HashSet<>();
        for (String url : TEST_URLS) shortUrls.add(shortener.shorten(url));
        assertEquals(TEST_URLS.size(), shortUrls.size());
    }

    @Test
    void testMd5StrategyUniqueness() {
        URLShortener shortener = new URLShortener(new MD5Strategy());
        Set<String> shortUrls = new HashSet<>();
        for (String url : TEST_URLS) shortUrls.add(shortener.shorten(url));
        assertEquals(TEST_URLS.size(), shortUrls.size());
    }

    @Test
    void testRandomStrategyCollision() {
        URLShortener shortener = new URLShortener(new RandomStrategy());
        Set<String> shortUrls = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            shortUrls.add(shortener.shorten("https://collision-test.com"));
        }
        assertEquals(1, shortUrls.size());
    }

    @Test
    void testBase64StrategyValidLength() {
        URLShortener shortener = new URLShortener(new Base64Strategy());
        for (String url : TEST_URLS) {
            String shortUrl = shortener.shorten(url);
            assertTrue(shortUrl.length() >= 1 && shortUrl.length() <= 8);
        }
    }

    @Test
    void testUnshortenCorrectness() {
        URLShortener shortener = new URLShortener(new CounterStrategy());
        for (String url : TEST_URLS) {
            String shortUrl = shortener.shorten(url);
            assertEquals(url, shortener.unshorten(shortUrl));
        }
    }

    @Test
    void testInvalidUrlHandling() {
        URLShortener shortener = new URLShortener(new CounterStrategy());
        assertThrows(IllegalArgumentException.class, () -> shortener.shorten("invalid-url"));
    }

    @Test
    void testUnshortenUnknownReturnsNull() {
        URLShortener shortener = new URLShortener(new CounterStrategy());
        assertNull(shortener.unshorten("nonexistent"));
    }

    @Test
    void testConcurrentShortenSameUrl() throws InterruptedException {
        URLShortener shortener = new URLShortener(new CounterStrategy());
        String url = "https://concurrent-test.com";
        Set<String> results = Collections.synchronizedSet(new HashSet<>());

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            threads.add(new Thread(() -> results.add(shortener.shorten(url))));
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        assertEquals(1, results.size(), "Concurrent shorten of same URL must return the same short URL");
    }

    @Test
    void testValidUrlWithTrailingSlash() {
        URLShortener shortener = new URLShortener(new CounterStrategy());
        assertDoesNotThrow(() -> shortener.shorten("https://example.com/"));
    }

    @Test
    void testShortenAlreadyShortenedUrlReturnsSameCode() {
        URLShortener shortener = new URLShortener(new CounterStrategy());
        String first = shortener.shorten("https://example.com");
        String second = shortener.shorten("https://example.com");
        assertEquals(first, second);
    }
}
