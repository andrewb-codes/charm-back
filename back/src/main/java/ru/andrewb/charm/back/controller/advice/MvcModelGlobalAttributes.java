package ru.andrewb.charm.back.controller.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;

import java.util.List;

import static ru.andrewb.charm.back.normalizer.ProfileFilterDefaults.AVAILABLE_PAGE_SIZES;

@ControllerAdvice
public class MvcModelGlobalAttributes {

    @ModelAttribute("genders")
    public Gender[] genders() {
        return Gender.values();
    }

    @ModelAttribute("statuses")
    public Status[] statuses() {
        return Status.values();
    }

    @ModelAttribute("roles")
    public Role[] roles() {
        return Role.values();
    }

    @ModelAttribute("availablePageSizes")
    public List<Integer> availablePageSizes() {
        return AVAILABLE_PAGE_SIZES;
    }
}
