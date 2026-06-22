package com.revolut.urlshortener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class URLShortener {
    private static final Pattern URL_PATTERN =
        Pattern.compile("^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$");

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, String> urlMap = new HashMap<>();
    private final Map<String, String> shortToUrl = new HashMap<>();
    private final ShorteningStrategy strategy;

    public URLShortener(ShorteningStrategy strategy) {
        this.strategy = strategy;
    }

    public String shorten(String longUrl) {
        if (!isValidUrl(longUrl)) {
            throw new IllegalArgumentException("Invalid URL format");
        }
        lock.lock();
        try {
            if (urlMap.containsKey(longUrl)) {
                return urlMap.get(longUrl);
            }
            String shortUrl = strategy.generateShortUrl(longUrl);
            urlMap.put(longUrl, shortUrl);
            shortToUrl.put(shortUrl, longUrl);
            return shortUrl;
        } finally {
            lock.unlock();
        }
    }

    public String unshorten(String shortUrl) {
        lock.lock();
        try {
            return shortToUrl.get(shortUrl);
        } finally {
            lock.unlock();
        }
    }

    private boolean isValidUrl(String url) {
        return url != null && URL_PATTERN.matcher(url).matches();
    }
}
