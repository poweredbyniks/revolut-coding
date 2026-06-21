package com.revolut.urlshortener;

import org.junit.jupiter.api.Test;

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
}
