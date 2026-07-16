package com.paytm.urlShortener.service;

import com.paytm.urlShortener.dto.request.ShortenUrlRequestDTO;
import com.paytm.urlShortener.dto.response.ShortenUrlResponseDTO;
import com.paytm.urlShortener.entity.UrlEntity;
import com.paytm.urlShortener.exception.AliasAlreadyExistsException;
import com.paytm.urlShortener.exception.InvalidUrlException;
import com.paytm.urlShortener.exception.ShortCodeGenerationException;
import com.paytm.urlShortener.generator.ShortCodeGenerator;
import com.paytm.urlShortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class UrlShorteningServiceImpl implements UrlShorteningService {
    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final String baseUrl;

    public UrlShorteningServiceImpl(UrlRepository urlRepository, ShortCodeGenerator shortCodeGenerator, @Value("${app.base-url}") String baseUrl) {
        this.urlRepository = urlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.baseUrl = removeTrailingSlash(baseUrl);
    }

    @Override
    public ShortenUrlResponseDTO shortenUrl(ShortenUrlRequestDTO request) {
        validateUrl(request.getUrl());

        String shortCode = resolveShortCode(request.getCustomAlias());

        UrlEntity urlEntity = UrlEntity.builder()
                .originalUrl(request.getUrl())
                .shortCode(shortCode)
                .build();

        UrlEntity savedUrl = urlRepository.save(urlEntity);

        return toResponse(savedUrl);
    }

    private String resolveShortCode(String customAlias) {
        if (customAlias != null && !customAlias.isBlank()) {
            return validateAndReturnCustomAlias(customAlias);
        }

        return generateShortCode();
    }

    private String validateAndReturnCustomAlias(String customAlias) {
        if (urlRepository.existsByShortCode(customAlias)) {
            throw new AliasAlreadyExistsException(customAlias);
        }
        return customAlias;
    }

    private String generateShortCode() {

        String generatedCode = shortCodeGenerator.generate();

        if (urlRepository.existsByShortCode(generatedCode)) {
            throw new ShortCodeGenerationException("Generated short code already exists.");
        }

        return generatedCode;
    }

    private void validateUrl(String url) {
        try {
            URI uri = new URI(url);
            if (!uri.isAbsolute()) {
                throw new InvalidUrlException("URL must be absolute and include a scheme");
            }
        } catch (URISyntaxException exception) {
            throw new InvalidUrlException("URL is malformed");
        }
    }

    private ShortenUrlResponseDTO toResponse(UrlEntity urlEntity) {
        return new ShortenUrlResponseDTO(
                urlEntity.getOriginalUrl(),
                baseUrl + "/" + urlEntity.getShortCode(),
                urlEntity.getShortCode(),
                urlEntity.getCreatedAt()
        );
    }

    private static String removeTrailingSlash(String value) {
        if (value != null && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }
}