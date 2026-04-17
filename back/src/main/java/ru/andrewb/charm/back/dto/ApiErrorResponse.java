package ru.andrewb.charm.back.dto;

import java.util.List;

public record ApiErrorResponse(
        List<ApiError> errors
) {
}
