package ru.andrewb.charm.back.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.andrewb.charm.back.controller.form.ProfilesFilterForm;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.dto.sort.SortBy;
import ru.andrewb.charm.back.dto.sort.SortOrder;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;
import ru.andrewb.charm.back.normalizer.ProfileFilterDefaults;
import ru.andrewb.charm.back.service.ProfileService;

import static ru.andrewb.charm.back.utils.StringUtils.stripToNull;
import static ru.andrewb.charm.back.web.Urls.ADMIN_PROFILES_REST_URL;

@RestController
@RequestMapping(ADMIN_PROFILES_REST_URL)
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfilesRestController {

    private final ProfileService service;

    public AdminProfilesRestController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getProfiles(@ModelAttribute ProfilesFilterForm form) {
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
