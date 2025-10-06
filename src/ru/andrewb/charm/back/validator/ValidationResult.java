package ru.andrewb.charm.back.validator;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    public boolean isNotValid() {
        return !errors.isEmpty();
    }

    public void addError(String code) {
        errors.add(code);
    }

    public void merge(ValidationResult other) {
        if (other != null) errors.addAll(other.errors);
    }

    public List<String> getErrors() {
        return List.copyOf(errors);
    }
}
