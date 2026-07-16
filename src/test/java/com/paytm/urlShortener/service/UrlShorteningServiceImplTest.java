package com.paytm.urlShortener.service;

import com.paytm.urlShortener.dto.request.ShortenUrlRequestDTO;
import com.paytm.urlShortener.dto.response.ShortenUrlResponseDTO;
import com.paytm.urlShortener.entity.UrlEntity;
import com.paytm.urlShortener.exception.AliasAlreadyExistsException;
import com.paytm.urlShortener.exception.InvalidUrlException;
import com.paytm.urlShortener.exception.ShortCodeGenerationException;
import com.paytm.urlShortener.generator.ShortCodeGenerator;
import com.paytm.urlShortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShorteningServiceImplTest {

    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    private UrlShorteningServiceImpl urlShorteningService;

    @BeforeEach
    void setUp() {
        urlShorteningService = new UrlShorteningServiceImpl(
                urlRepository,
                shortCodeGenerator,
                BASE_URL
        );
    }

    @Test
    void shouldShortenValidUrlUsingGeneratedCode() {
        // Arrange
        String originalUrl = "https://example.com/articles/123";
        String generatedCode = "abc123";
        LocalDateTime createdAt = LocalDateTime.now();

        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO(originalUrl, null);

        when(shortCodeGenerator.generate())
                .thenReturn(generatedCode);

        when(urlRepository.existsByShortCode(generatedCode))
                .thenReturn(false);

        when(urlRepository.save(any(UrlEntity.class)))
                .thenAnswer(invocation -> {
                    UrlEntity entity = invocation.getArgument(0);
                    entity.setId(1L);
                    entity.setCreatedAt(createdAt);
                    return entity;
                });

        // Act
        ShortenUrlResponseDTO response =
                urlShorteningService.shortenUrl(request);

        // Assert
        assertNotNull(response);
        assertEquals(originalUrl, response.getOriginalUrl());
        assertEquals(generatedCode, response.getShortCode());
        assertEquals(
                BASE_URL + "/" + generatedCode,
                response.getShortUrl()
        );
        assertEquals(createdAt, response.getCreatedAt());

        verify(shortCodeGenerator).generate();
        verify(urlRepository).existsByShortCode(generatedCode);
        verify(urlRepository).save(any(UrlEntity.class));
    }

    @Test
    void shouldSaveCorrectEntityWhenShorteningUrl() {
        // Arrange
        String originalUrl = "https://example.com";
        String generatedCode = "xyz789";

        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO(originalUrl, null);

        when(shortCodeGenerator.generate())
                .thenReturn(generatedCode);

        when(urlRepository.existsByShortCode(generatedCode))
                .thenReturn(false);

        when(urlRepository.save(any(UrlEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<UrlEntity> entityCaptor =
                ArgumentCaptor.forClass(UrlEntity.class);

        // Act
        urlShorteningService.shortenUrl(request);

        // Assert
        verify(urlRepository).save(entityCaptor.capture());

        UrlEntity savedEntity = entityCaptor.getValue();

        assertEquals(originalUrl, savedEntity.getOriginalUrl());
        assertEquals(generatedCode, savedEntity.getShortCode());
    }

    @Test
    void shouldUseCustomAliasWithoutGeneratingCode() {
        // Arrange
        String originalUrl = "https://example.com";
        String customAlias = "example";

        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO(originalUrl, customAlias);

        when(urlRepository.existsByShortCode(customAlias))
                .thenReturn(false);

        when(urlRepository.save(any(UrlEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ShortenUrlResponseDTO response =
                urlShorteningService.shortenUrl(request);

        // Assert
        assertEquals(customAlias, response.getShortCode());
        assertEquals(
                BASE_URL + "/" + customAlias,
                response.getShortUrl()
        );

        verify(urlRepository).existsByShortCode(customAlias);
        verify(urlRepository).save(any(UrlEntity.class));

        // Generator must not be called when an alias is supplied.
        verifyNoInteractions(shortCodeGenerator);
    }

    @Test
    void shouldThrowExceptionWhenCustomAliasAlreadyExists() {
        // Arrange
        String customAlias = "example";

        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO(
                        "https://example.com",
                        customAlias
                );

        when(urlRepository.existsByShortCode(customAlias))
                .thenReturn(true);

        // Act and assert
        assertThrows(
                AliasAlreadyExistsException.class,
                () -> urlShorteningService.shortenUrl(request)
        );

        verify(urlRepository).existsByShortCode(customAlias);
        verify(urlRepository, never()).save(any());
        verifyNoInteractions(shortCodeGenerator);
    }

    @Test
    void shouldThrowExceptionWhenGeneratedCodeAlreadyExists() {
        // Arrange
        String generatedCode = "abc123";

        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO(
                        "https://example.com",
                        null
                );

        when(shortCodeGenerator.generate())
                .thenReturn(generatedCode);

        when(urlRepository.existsByShortCode(generatedCode))
                .thenReturn(true);

        // Act and assert
        assertThrows(
                ShortCodeGenerationException.class,
                () -> urlShorteningService.shortenUrl(request)
        );

        verify(shortCodeGenerator).generate();
        verify(urlRepository).existsByShortCode(generatedCode);
        verify(urlRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionForRelativeUrl() {
        // Arrange
        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO(
                        "example.com/articles/123",
                        null
                );

        // Act and assert
        InvalidUrlException exception = assertThrows(
                InvalidUrlException.class,
                () -> urlShorteningService.shortenUrl(request)
        );

        assertEquals(
                "URL must be absolute and include a scheme",
                exception.getMessage()
        );

        verifyNoInteractions(shortCodeGenerator);
        verifyNoInteractions(urlRepository);
    }

    @Test
    void shouldThrowExceptionForMalformedUrl() {
        // Arrange
        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO(
                        "https://exa mple.com",
                        null
                );

        // Act and assert
        InvalidUrlException exception = assertThrows(
                InvalidUrlException.class,
                () -> urlShorteningService.shortenUrl(request)
        );

        assertEquals("URL is malformed", exception.getMessage());

        verifyNoInteractions(shortCodeGenerator);
        verifyNoInteractions(urlRepository);
    }

    @Test
    void shouldRemoveTrailingSlashFromConfiguredBaseUrl() {
        // Arrange
        UrlShorteningServiceImpl service =
                new UrlShorteningServiceImpl(
                        urlRepository,
                        shortCodeGenerator,
                        "http://localhost:8080/"
                );

        String generatedCode = "abc123";

        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO(
                        "https://example.com",
                        null
                );

        when(shortCodeGenerator.generate())
                .thenReturn(generatedCode);

        when(urlRepository.existsByShortCode(generatedCode))
                .thenReturn(false);

        when(urlRepository.save(any(UrlEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ShortenUrlResponseDTO response =
                service.shortenUrl(request);

        // Assert
        assertEquals(
                "http://localhost:8080/abc123",
                response.getShortUrl()
        );
    }
}