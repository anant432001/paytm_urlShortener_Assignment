package com.paytm.urlShortener.exception;

public class InvalidUrlException extends RuntimeException {

    public InvalidUrlException(String message) {
        super("URL is invalid - "+message);
    }
}