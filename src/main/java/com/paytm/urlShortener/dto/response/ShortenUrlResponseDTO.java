package com.paytm.urlShortener.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShortenUrlResponseDTO {
    private String originalUrl;
    private String shortUrl;
    private String shortCode;
    private LocalDateTime createdAt;
}