package ru.andrewb.charm.back.controller.advice;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.andrewb.charm.back.model.exception.*;

import static ru.andrewb.charm.back.web.Views.*;

@ControllerAdvice(basePackages = "ru.andrewb.charm.back.controller.ui")
public class MvcExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequest(BadRequestException e, HttpServletResponse resp, Model model) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        model.addAttribute("errorCode", HttpServletResponse.SC_BAD_REQUEST);
        model.addAttribute("errorMessage", e.getMessage());
        return ERROR_400;
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException e, HttpServletResponse resp, Model model) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("errorCode", HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("errorMessage", e.getMessage());
        return ERROR_404;
    }

    @ExceptionHandler({DuplicateEmailException.class, OptimisticLockException.class})
    public String handleConflict(RuntimeException e, HttpServletResponse resp, Model model) {
        resp.setStatus(HttpServletResponse.SC_CONFLICT);
        model.addAttribute("errorCode", HttpServletResponse.SC_CONFLICT);
        model.addAttribute("errorMessage", e instanceof OptimisticLockException ? "error.optimistic-lock" : e.getMessage());
        return ERROR_409;
    }

    @ExceptionHandler(InfrastructureException.class)
    public String handleInfrastructure(InfrastructureException e, HttpServletResponse resp, Model model) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorMessage", "error.internal");
        return ERROR_500;
    }

    @ExceptionHandler(StorageException.class)
    public String handleStorage(StorageException e, HttpServletResponse resp, Model model) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorMessage", "error.internal");
        return ERROR_500;
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception e, HttpServletResponse resp, Model model) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorMessage", "error.internal");
        return ERROR_500;
    }
}
