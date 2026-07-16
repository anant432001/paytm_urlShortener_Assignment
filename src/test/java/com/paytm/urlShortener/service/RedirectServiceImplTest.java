package com.paytm.urlShortener.service;

import com.paytm.urlShortener.entity.UrlEntity;
import com.paytm.urlShortener.exception.ShortCodeNotFoundException;
import com.paytm.urlShortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedirectServiceImplTest {

    @Mock
    private UrlRepository urlRepository;
    private RedirectServiceImpl redirectService;

    @BeforeEach
    void setUp() {
        redirectService = new RedirectServiceImpl(urlRepository);
    }

    @Test
    void shouldReturnOriginalUrlWhenShortCodeExists() {
        // Arrange
        String shortCode = "abc123";
        String originalUrl = "https://example.com/articles/123";

        UrlEntity entity = UrlEntity.builder()
                .id(1L)
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .build();

        when(urlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(entity));

        // Act
        String result = redirectService.getOriginalUrl(shortCode);

        // Assert
        assertEquals(originalUrl, result);

        verify(urlRepository).findByShortCode(shortCode);
    }

    @Test
    void shouldThrowExceptionWhenShortCodeDoesNotExist() {
        // Arrange
        String shortCode = "missing";

        when(urlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        // Act
        ShortCodeNotFoundException exception = assertThrows(
                ShortCodeNotFoundException.class,
                () -> redirectService.getOriginalUrl(shortCode)
        );

        // Assert
        assertEquals(
                "No URL mapping found for short code: missing",
                exception.getMessage()
        );

        verify(urlRepository).findByShortCode(shortCode);
    }
}