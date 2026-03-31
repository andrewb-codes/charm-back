package ru.andrewb.charm.back.dto.sort;

public enum SortOrder {
    ASC, DESC;

    public SortOrder flip() {
        return this == ASC ? DESC : ASC;
    }
}
