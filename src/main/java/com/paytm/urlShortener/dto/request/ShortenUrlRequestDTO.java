package com.paytm.urlShortener.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShortenUrlRequestDTO {

    @NotBlank(message = "URL must not be empty")
    @Size(max = 2048, message = "URL must not exceed 2048 characters limit")
    private String url;

    @Size(
            min = 4,
            max = 20,
            message = "Custom alias must be between 4 and 20 characters limit"
    )
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+$",
            message = "Custom alias can contain only letters, numbers, '-' and '_'"
    )
    private String customAlias;
}