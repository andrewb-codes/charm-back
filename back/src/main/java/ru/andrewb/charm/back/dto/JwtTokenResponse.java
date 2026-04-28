package ru.andrewb.charm.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Schema(description = "JWT token response")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class JwtTokenResponse {

    @Schema(description = "JWT access token")
    final String accessToken;

    @Schema(description = "Token type", example = "Bearer")
    final String tokenType;

    @Schema(description = "Token lifetime in seconds", example = "3600")
    final long expiresIn;
}
