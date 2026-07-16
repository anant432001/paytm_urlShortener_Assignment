package com.paytm.urlShortener.controller;

import com.paytm.urlShortener.dto.request.ShortenUrlRequestDTO;
import com.paytm.urlShortener.dto.response.ShortenUrlResponseDTO;
import com.paytm.urlShortener.service.RedirectService;
import com.paytm.urlShortener.service.UrlShorteningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlShorteningService urlShorteningService;

    @MockitoBean
    private RedirectService redirectService;

    @Test
    void shouldReturnCreatedWhenUrlIsShortened() throws Exception {
        // Arrange
        String originalUrl = "https://example.com/articles/123";
        String shortCode = "abc123";
        String shortUrl = "http://localhost:8080/abc123";
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 7, 16, 13, 30);

        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO(originalUrl, null);

        ShortenUrlResponseDTO response =
                new ShortenUrlResponseDTO(
                        originalUrl,
                        shortUrl,
                        shortCode,
                        createdAt
                );

        when(urlShorteningService.shortenUrl(any(ShortenUrlRequestDTO.class)))
                .thenReturn(response);

        // Act and assert
        mockMvc.perform(
                        post("/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.shortUrl").value(shortUrl))
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-07-16T13:30:00"));

        verify(urlShorteningService)
                .shortenUrl(any(ShortenUrlRequestDTO.class));

        verifyNoInteractions(redirectService);
    }

    @Test
    void shouldReturnCreatedWhenCustomAliasIsProvided() throws Exception {
        // Arrange
        String originalUrl = "https://example.com";
        String customAlias = "example";
        String shortUrl = "http://localhost:8080/example";

        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO(originalUrl, customAlias);

        ShortenUrlResponseDTO response =
                new ShortenUrlResponseDTO(
                        originalUrl,
                        shortUrl,
                        customAlias,
                        LocalDateTime.of(2026, 7, 16, 13, 30)
                );

        when(urlShorteningService.shortenUrl(any(ShortenUrlRequestDTO.class)))
                .thenReturn(response);

        // Act and assert
        mockMvc.perform(
                        post("/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.shortCode").value(customAlias))
                .andExpect(jsonPath("$.shortUrl").value(shortUrl));

        verify(urlShorteningService)
                .shortenUrl(any(ShortenUrlRequestDTO.class));
    }

    @Test
    void shouldReturnPermanentRedirectWhenCodeExists() throws Exception {
        // Arrange
        String shortCode = "abc123";
        String originalUrl = "https://example.com/articles/123";

        when(redirectService.getOriginalUrl(shortCode))
                .thenReturn(originalUrl);

        // Act and assert
        mockMvc.perform(get("/{code}", shortCode))
                .andDo(print())
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", originalUrl))
                .andExpect(content().string(""));

        verify(redirectService).getOriginalUrl(shortCode);
        verifyNoInteractions(urlShorteningService);
    }

    @Test
    void shouldReturnBadRequestWhenUrlIsBlank() throws Exception {
        // Arrange
        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO("", null);

        // Act and assert
        mockMvc.perform(
                        post("/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Validation must reject the request before service execution.
        verifyNoInteractions(urlShorteningService);
        verifyNoInteractions(redirectService);
    }

    @Test
    void shouldReturnBadRequestWhenCustomAliasContainsInvalidCharacters()
            throws Exception {

        // Arrange
        ShortenUrlRequestDTO request =
                new ShortenUrlRequestDTO(
                        "https://example.com",
                        "invalid alias!"
                );

        // Act and assert
        mockMvc.perform(
                        post("/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(urlShorteningService);
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsMissing() throws Exception {
        mockMvc.perform(
                        post("/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(urlShorteningService);
    }
}