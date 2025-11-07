package ru.andrewb.charm.back.dto.sort;

public enum SortBy {
    ID("id"),
    EMAIL("email"),
    NAME("\"name\""),
    SURNAME("surname"),
    BIRTHDATE("birthdate"),
    GENDER("gender"),
    STATUS("status"),
    ROLE("\"role\"");

    private final String column;

    SortBy(String column) {
        this.column = column;
    }

    public String getColumn() {
        return column;
    }
}
