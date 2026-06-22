package com.revolut.urlshortener;

public interface ShorteningStrategy {
    String generateShortUrl(String longUrl);
}
