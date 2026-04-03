package ru.andrewb.charm.back.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.model.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestParamUtilsTest {

    @Test
    void requirePositiveLong_shouldReturnParsedValue_whenParameterIsValid() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("id")).thenReturn("42");

        long result = RequestParamUtils.requirePositiveLong(req, "id");

        assertEquals(42L, result);
    }

    @Test
    void requirePositiveLong_shouldThrow_whenParameterIsMissing() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("id")).thenReturn(null);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> RequestParamUtils.requirePositiveLong(req, "id")
        );

        assertEquals("error.param.required", ex.getMessage());
    }

    @Test
    void requirePositiveLong_shouldThrow_whenParameterIsBlank() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("id")).thenReturn("   ");

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> RequestParamUtils.requirePositiveLong(req, "id")
        );

        assertEquals("error.param.required", ex.getMessage());
    }

    @Test
    void requirePositiveLong_shouldThrow_whenParameterIsZero() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("id")).thenReturn("0");

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> RequestParamUtils.requirePositiveLong(req, "id")
        );

        assertEquals("error.param.invalid", ex.getMessage());
    }

    @Test
    void requirePositiveLong_shouldThrow_whenParameterIsNegative() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("id")).thenReturn("-5");

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> RequestParamUtils.requirePositiveLong(req, "id")
        );

        assertEquals("error.param.invalid", ex.getMessage());
    }

    @Test
    void requirePositiveLong_shouldThrow_whenParameterIsNotNumeric() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("id")).thenReturn("abc");

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> RequestParamUtils.requirePositiveLong(req, "id")
        );

        assertEquals("error.param.invalid", ex.getMessage());
    }

    @Test
    void rid_shouldReturnDash_whenAttributeIsMissing() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getAttribute("rid")).thenReturn(null);

        String result = RequestParamUtils.rid(req);

        assertEquals("-", result);
    }

    @Test
    void rid_shouldReturnAttributeValue_whenAttributeExists() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getAttribute("rid")).thenReturn("req-123");

        String result = RequestParamUtils.rid(req);

        assertEquals("req-123", result);
    }
}
