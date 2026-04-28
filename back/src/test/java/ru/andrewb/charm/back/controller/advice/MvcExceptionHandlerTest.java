package ru.andrewb.charm.back.controller.advice;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import ru.andrewb.charm.back.model.exception.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static ru.andrewb.charm.back.web.Views.*;

@ExtendWith(MockitoExtension.class)
class MvcExceptionHandlerTest {

    @Mock
    private HttpServletResponse response;

    private MvcExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MvcExceptionHandler();
    }

    @Test
    void handleBadRequest_shouldReturn400ViewAndModel() {
        Model model = new ExtendedModelMap();

        String view = handler.handleBadRequest(new BadRequestException("error.param.invalid"), response, model);

        assertEquals(ERROR_400, view);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, model.getAttribute("errorCode"));
        assertEquals("error.param.invalid", model.getAttribute("errorMessage"));
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void handleConflict_shouldUseOptimisticLockCode_whenOptimisticLockException() {
        Model model = new ExtendedModelMap();

        String view = handler.handleConflict(new OptimisticLockException("ignored"), response, model);

        assertEquals(ERROR_409, view);
        assertEquals(HttpServletResponse.SC_CONFLICT, model.getAttribute("errorCode"));
        assertEquals("error.optimistic-lock", model.getAttribute("errorMessage"));
        verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
    }

    @Test
    void handleInfrastructure_shouldReturn500ViewWithGenericMessage() {
        Model model = new ExtendedModelMap();

        String view = handler.handleInfrastructure(new InfrastructureException("error.internal", new RuntimeException("x")), response, model);

        assertEquals(ERROR_500, view);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, model.getAttribute("errorCode"));
        assertEquals("error.internal", model.getAttribute("errorMessage"));
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void handleDataAccess_shouldReturn500ViewWithGenericMessage() {
        Model model = new ExtendedModelMap();

        String view = handler.handleDataAccess(new DataAccessResourceFailureException("db error"), response, model);

        assertEquals(ERROR_500, view);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, model.getAttribute("errorCode"));
        assertEquals("error.internal", model.getAttribute("errorMessage"));
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
