package ru.andrewb.charm.back.model.exception;

public class PdfBuildException extends RuntimeException {
    public PdfBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
