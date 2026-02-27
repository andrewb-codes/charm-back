package ru.andrewb.charm.back.dto;

import java.util.List;

public record Query(String sql, List<Object> args) {
}
