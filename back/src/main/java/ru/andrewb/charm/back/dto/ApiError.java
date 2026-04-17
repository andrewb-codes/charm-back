package ru.andrewb.charm.back.dto;

public record ApiError(
        String code,
        String message
) {
}
