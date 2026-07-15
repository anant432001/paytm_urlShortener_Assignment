package com.paytm.urlShortener.service;

public interface RedirectService {
    String getOriginalUrl(String shortCode);
}