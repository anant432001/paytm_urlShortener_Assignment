package com.paytm.urlShortener.exception;

public class ShortCodeNotFoundException extends RuntimeException {

    public ShortCodeNotFoundException(String shortCode) {
        super("No URL mapping found for short code: " + shortCode);
    }
}