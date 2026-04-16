package ru.andrewb.charm.back.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.andrewb.charm.back.model.exception.*;

import java.util.List;
import java.util.Map;

@RestControllerAdvice(basePackages = "ru.andrewb.charm.back.controller.rest")
public class RestExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errors", List.of(e.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("errors", extractErrors(e.getBindingResult())));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindException(BindException e) {
        return ResponseEntity.badRequest().body(Map.of("errors", extractErrors(e.getBindingResult())));
    }


    @ExceptionHandler({BadRequestException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<?> handleBadRequest(Exception e) {
        String message = e instanceof BadRequestException bre ? bre.getMessage() : "error.param.invalid";
        return ResponseEntity.badRequest().body(Map.of("errors", List.of(message)));
    }

    @ExceptionHandler({DuplicateEmailException.class, OptimisticLockException.class})
    public ResponseEntity<?> handleConflict(Exception e) {
        String message = e instanceof OptimisticLockException ? "error.optimistic-lock" : e.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errors", List.of(message)));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<?> handleStorage(StorageException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errors", List.of(e.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errors", List.of("error.internal")));
    }

    private List<String> extractErrors(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .toList();
    }
}
