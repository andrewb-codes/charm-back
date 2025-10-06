package ru.andrewb.charm.back.validator;

public interface Validator<T> {

    ValidationResult validate(T obj);
}
