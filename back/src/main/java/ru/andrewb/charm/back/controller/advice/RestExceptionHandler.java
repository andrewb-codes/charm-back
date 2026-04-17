package ru.andrewb.charm.back.controller.advice;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.andrewb.charm.back.dto.ApiError;
import ru.andrewb.charm.back.dto.ApiErrorResponse;
import ru.andrewb.charm.back.model.exception.*;

import java.util.List;

@RestControllerAdvice(basePackages = "ru.andrewb.charm.back.controller.rest")
public class RestExceptionHandler {

    private final MessageSource messageSource;

    public RestExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body(validationErrorResponse(e.getBindingResult()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(BindException e) {
        return ResponseEntity.badRequest()
                .body(validationErrorResponse(e.getBindingResult()));
    }


    @ExceptionHandler({BadRequestException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception e) {
        String code = e instanceof BadRequestException bre ? bre.getMessage() : "error.param.invalid";
        return ResponseEntity.badRequest()
                .body(errorResponse(code));
    }

    @ExceptionHandler({DuplicateEmailException.class, OptimisticLockException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException e) {
        String code = e instanceof OptimisticLockException ? "error.optimistic-lock" : e.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse(code));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiErrorResponse> handleStorage(StorageException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse(e.getMessage()));
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ApiErrorResponse> handleInfrastructure(InfrastructureException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse("error.internal"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(errorResponse("error.internal"));
    }

    private ApiErrorResponse validationErrorResponse(BindingResult bindingResult) {
        List<ApiError> errors = bindingResult.getAllErrors().stream()
                .map(error -> toApiError(error.getDefaultMessage()))
                .distinct()
                .toList();

        return new ApiErrorResponse(errors);
    }

    private ApiErrorResponse errorResponse(String code) {
        return new ApiErrorResponse(List.of(toApiError(code)));
    }

    private ApiError toApiError(String code) {
        return new ApiError(code, resolve(code));
    }

    private String resolve(String code, Object... args) {
        return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
    }
}
