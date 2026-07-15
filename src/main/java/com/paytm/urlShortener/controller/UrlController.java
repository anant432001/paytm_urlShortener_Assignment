package com.paytm.urlShortener.controller;

import com.paytm.urlShortener.dto.request.ShortenUrlRequestDTO;
import com.paytm.urlShortener.dto.response.ShortenUrlResponseDTO;
import com.paytm.urlShortener.service.RedirectService;
import com.paytm.urlShortener.service.UrlShorteningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlController {

    private final UrlShorteningService urlShorteningService;
    private final RedirectService redirectService;

    public UrlController(UrlShorteningService urlShorteningService, RedirectService redirectService) {
        this.urlShorteningService = urlShorteningService;
        this.redirectService = redirectService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponseDTO> shortenUrl(@Valid @RequestBody ShortenUrlRequestDTO request) {
        ShortenUrlResponseDTO response = urlShorteningService.shortenUrl(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String originalUrl = redirectService.getOriginalUrl(code);
        System.out.println("originalUrl: " + originalUrl);
        return ResponseEntity
                .status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(originalUrl))
                .build();
    }
}