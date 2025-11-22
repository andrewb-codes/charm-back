package ru.andrewb.charm.back.model.exception;

public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
