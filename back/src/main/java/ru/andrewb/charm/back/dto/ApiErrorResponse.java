package ru.andrewb.charm.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "API error response")
public record ApiErrorResponse(
        @Schema(description = "List of errors")
        List<ApiError> errors
) {
}
