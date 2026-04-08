package ru.andrewb.charm.back.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import ru.andrewb.charm.back.controller.form.ProfilesFilterForm;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.dto.sort.SortBy;
import ru.andrewb.charm.back.dto.sort.SortOrder;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;
import ru.andrewb.charm.back.normalizer.ProfileFilterDefaults;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;

import static ru.andrewb.charm.back.utils.StringUtils.stripToNull;
import static ru.andrewb.charm.back.web.Urls.PROFILES_REST_URL;

@RestController
public class ProfilesRestController {

    private final ProfileService service;

    public ProfilesRestController(ProfileService service) {
        this.service = service;
    }

    @GetMapping(PROFILES_REST_URL)
    public ResponseEntity<?> getProfiles(
            @ModelAttribute ProfilesFilterForm form,
            HttpServletRequest req
    ) {
        if (!AuthUtils.isAuthenticatedAdmin(req)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ProfileFilter filter = toProfileFilter(form);
        return ResponseEntity.ok(service.findAll(filter));
    }

    private ProfileFilter toProfileFilter(ProfilesFilterForm form) {
        ProfileFilter filter = new ProfileFilter();

        filter.setEmailStartsWith(stripToNull(form.getEmailStartsWith()));
        filter.setNameStartsWith(stripToNull(form.getNameStartsWith()));
        filter.setSurnameStartsWith(stripToNull(form.getSurnameStartsWith()));
        filter.setLowerAgeBound(parseInt(form.getLtAge()));
        filter.setGreaterAndEqualAgeBound(parseInt(form.getGteAge()));
        filter.setRole(parseEnum(form.getRole(), Role.class));
        filter.setStatus(parseEnum(form.getStatus(), Status.class));
        filter.setSortBy(parseEnum(form.getSortBy(), SortBy.class));
        filter.setSortOrder(parseEnum(form.getSortOrder(), SortOrder.class));
        filter.setPage(parseInt(form.getPage()));
        filter.setPageSize(parseInt(form.getPageSize()));

        return ProfileFilterDefaults.normalize(filter);
    }

    private Integer parseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass) {
        String normalized = stripToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, normalized.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
