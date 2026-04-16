package com.sparta.spartadelivery.global.infrastructure.config.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// JWT properties 바인딩
@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank String secret,
        @Min(1) long accessTokenExpiration
) {
}
