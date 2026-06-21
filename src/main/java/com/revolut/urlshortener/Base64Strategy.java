package com.revolut.urlshortener;

import java.util.Base64;

public class Base64Strategy implements ShorteningStrategy {
    @Override
    public String generateShortUrl(String longUrl) {
        String encoded = Base64.getUrlEncoder().encodeToString(longUrl.getBytes());
        return encoded.substring(0, Math.min(8, encoded.length()));
    }
}
