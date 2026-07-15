package com.paytm.urlShortener.service;

import com.paytm.urlShortener.dto.request.ShortenUrlRequestDTO;
import com.paytm.urlShortener.dto.response.ShortenUrlResponseDTO;

public interface UrlShorteningService {
    ShortenUrlResponseDTO shortenUrl(ShortenUrlRequestDTO request);
}