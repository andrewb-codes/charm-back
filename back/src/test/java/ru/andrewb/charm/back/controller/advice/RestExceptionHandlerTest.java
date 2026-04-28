package ru.andrewb.charm.back.controller.advice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import ru.andrewb.charm.back.dto.ApiErrorResponse;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.model.exception.OptimisticLockException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler(messageSource);
        Map<String, String> messages = Map.of(
                "error.profile.not-found", "Profile not found",
                "error.param.invalid", "Invalid parameter",
                "error.optimistic-lock", "Optimistic lock",
                "error.field.required", "Field is required",
                "error.internal", "Internal error"
        );
        lenient().when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenAnswer(inv -> {
                    String code = inv.getArgument(0, String.class);
                    String defaultMsg = inv.getArgument(2, String.class);
                    return messages.getOrDefault(code, defaultMsg);
                });
    }

    @Test
    void handleNotFound_shouldReturn404WithResolvedError() {
        var response = handler.handleNotFound(new NotFoundException("error.profile.not-found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertEquals(1, body.errors().size());
        assertEquals("error.profile.not-found", body.errors().getFirst().code());
        assertEquals("Profile not found", body.errors().getFirst().message());
    }

    @Test
    void handleBadRequest_shouldReturn400WithBadRequestCode() {
        var response = handler.handleBadRequest(new BadRequestException("error.param.invalid"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertEquals("error.param.invalid", body.errors().getFirst().code());
        assertEquals("Invalid parameter", body.errors().getFirst().message());
    }

    @Test
    void handleConflict_shouldReturn409WithOptimisticLockCode() {
        var response = handler.handleConflict(new OptimisticLockException("whatever"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertEquals("error.optimistic-lock", body.errors().getFirst().code());
        assertEquals("Optimistic lock", body.errors().getFirst().message());
    }

    @Test
    void handleBindException_shouldReturn400WithValidationErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new ValidationTarget(), "target");
        bindingResult.rejectValue("field", "error.field.required", "error.field.required");
        BindException bindException = new BindException(bindingResult);

        var response = handler.handleBindException(bindException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertEquals(1, body.errors().size());
        assertEquals("error.field.required", body.errors().getFirst().code());
        assertEquals("Field is required", body.errors().getFirst().message());
    }

    private static final class ValidationTarget {
        private String field;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }
}
