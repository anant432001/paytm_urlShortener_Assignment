package com.paytm.urlShortener.service;

import com.paytm.urlShortener.entity.UrlEntity;
import com.paytm.urlShortener.exception.ShortCodeNotFoundException;
import com.paytm.urlShortener.repository.UrlRepository;
import org.springframework.stereotype.Service;

@Service
public class RedirectServiceImpl implements RedirectService {

    private final UrlRepository urlRepository;

    public RedirectServiceImpl(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Override
    public String getOriginalUrl(String shortCode) {
        UrlEntity urlEntity = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        return urlEntity.getOriginalUrl();
    }
}