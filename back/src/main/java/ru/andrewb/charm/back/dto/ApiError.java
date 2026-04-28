package ru.andrewb.charm.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API error item")
public record ApiError(
        @Schema(description = "Stable error code", example = "error.param.invalid")
        String code,
        @Schema(description = "Localized error message", example = "Invalid parameter")
        String message
) {
}
