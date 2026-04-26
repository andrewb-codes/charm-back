package ru.andrewb.charm.back.controller.advice;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.andrewb.charm.back.model.exception.*;

import static ru.andrewb.charm.back.web.Views.*;

@Slf4j
@ControllerAdvice(basePackages = "ru.andrewb.charm.back.controller.ui")
public class MvcExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequest(BadRequestException e, HttpServletResponse resp, Model model) {
        log.warn("MVC bad request: {}", e.getMessage());
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        model.addAttribute("errorCode", HttpServletResponse.SC_BAD_REQUEST);
        model.addAttribute("errorMessage", e.getMessage());
        return ERROR_400;
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException e, HttpServletResponse resp, Model model) {
        log.warn("MVC not found: {}", e.getMessage());
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("errorCode", HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("errorMessage", e.getMessage());
        return ERROR_404;
    }

    @ExceptionHandler({DuplicateEmailException.class, OptimisticLockException.class})
    public String handleConflict(RuntimeException e, HttpServletResponse resp, Model model) {
        log.warn("MVC conflict: {}", e.getMessage());
        resp.setStatus(HttpServletResponse.SC_CONFLICT);
        model.addAttribute("errorCode", HttpServletResponse.SC_CONFLICT);
        model.addAttribute("errorMessage", e instanceof OptimisticLockException ? "error.optimistic-lock" : e.getMessage());
        return ERROR_409;
    }

    @ExceptionHandler(InfrastructureException.class)
    public String handleInfrastructure(InfrastructureException e, HttpServletResponse resp, Model model) {
        log.error("MVC infrastructure error", e);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorMessage", "error.internal");
        return ERROR_500;
    }

    @ExceptionHandler(StorageException.class)
    public String handleStorage(StorageException e, HttpServletResponse resp, Model model) {
        log.error("MVC storage error", e);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorMessage", "error.internal");
        return ERROR_500;
    }

    @ExceptionHandler(DataAccessException.class)
    public String handleDataAccess(DataAccessException e, HttpServletResponse resp, Model model) {
        log.error("MVC data access error", e);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorMessage", "error.internal");
        return ERROR_500;
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception e, HttpServletResponse resp, Model model) {
        log.error("MVC unexpected error", e);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorMessage", "error.internal");
        return ERROR_500;
    }
}
