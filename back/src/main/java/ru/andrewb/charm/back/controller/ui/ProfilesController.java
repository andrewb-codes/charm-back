package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.andrewb.charm.back.controller.form.ProfilesFilterForm;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.dto.ProfileUpdateStatusDto;
import ru.andrewb.charm.back.dto.sort.SortBy;
import ru.andrewb.charm.back.dto.sort.SortOrder;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;
import ru.andrewb.charm.back.normalizer.ProfileFilterDefaults;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.security.SecurityRules;
import ru.andrewb.charm.back.service.ProfileService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.andrewb.charm.back.utils.BeanUtils.copyProperties;
import static ru.andrewb.charm.back.utils.StringUtils.stripToNull;
import static ru.andrewb.charm.back.web.Urls.PROFILES_URL;
import static ru.andrewb.charm.back.web.Views.ERROR_403;
import static ru.andrewb.charm.back.web.Views.PROFILES;

@Controller
public class ProfilesController {

    private final ProfileService service;

    public ProfilesController(ProfileService service) {
        this.service = service;
    }

    @GetMapping(PROFILES_URL)
    public String getProfiles(
            @ModelAttribute("form") ProfilesFilterForm form,
            HttpServletRequest req
    ) {
        if (!AuthUtils.isAuthenticatedAdmin(req)) {
            return ERROR_403;
        }

        ProfileFilter filter = toProfileFilter(form);

        var probe = new ProfileFilter();
        copyProperties(filter, probe);
        probe.setPageSize(filter.getPageSize() + 1);

        var items = service.findAll(probe);
        boolean hasNext = items.size() > filter.getPageSize();
        if (hasNext) items = items.subList(0, filter.getPageSize());

        boolean hasPrev = filter.getPage() > 1;


        req.setAttribute("profiles", items);
        req.setAttribute("filter", filter);
        req.setAttribute("hasPrev", hasPrev);
        req.setAttribute("hasNext", hasNext);

        return PROFILES;
    }

    @PutMapping(PROFILES_URL)
    public void updateStatuses(
            @RequestParam(name = "statusesWithIds", required = false) List<String> statusesWithIds,
            @RequestParam(name = "versionsWithIds", required = false) List<String> versionsWithIds,
            @RequestParam(name = "back", required = false) String back,
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {
        if (!AuthUtils.isAuthenticatedAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<ProfileUpdateStatusDto> dtoList = mapStatuses(statusesWithIds, versionsWithIds);;
        service.updateStatuses(dtoList);

        String ctx = req.getContextPath();
        if (SecurityRules.isSafeInternalRedirect(ctx, back, PROFILES_URL)) {
            resp.sendRedirect(back);
        } else {
            resp.sendRedirect(ctx + PROFILES_URL);
        }

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

    private List<ProfileUpdateStatusDto> mapStatuses(List<String> statusesWithIds, List<String> versionsWithIds) {
        Map<Long, Integer> versionById = parseVersions(versionsWithIds);
        List<ProfileUpdateStatusDto> result = new ArrayList<>();

        if (statusesWithIds == null) {
            return result;
        }

        for (String raw : statusesWithIds) {
            if (raw == null) {
                continue;
            }

            String value = raw.trim();
            if (value.isBlank() || "skip".equalsIgnoreCase(value)) {
                continue;
            }

            String[] parts = value.split("_");
            if (parts.length != 2) {
                continue;
            }

            try {
                Status status = Status.valueOf(parts[0].trim().toUpperCase());
                long id = Long.parseLong(parts[1].trim());

                Integer version = versionById.get(id);
                if (version == null) {
                    continue;
                }

                ProfileUpdateStatusDto dto = new ProfileUpdateStatusDto();
                dto.setId(id);
                dto.setStatus(status);
                dto.setVersion(version);
                result.add(dto);

            } catch (Exception ignored) {
            }
        }

        return result;
    }

    private Map<Long, Integer> parseVersions(List<String> versionsWithIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (versionsWithIds == null) {
            return result;
        }

        for (String raw : versionsWithIds) {
            if (raw == null) {
                continue;
            }

            String value = raw.trim();
            if (value.isBlank()) {
                continue;
            }

            String[] parts = value.split("_");
            if (parts.length != 2) {
                continue;
            }

            try {
                long id = Long.parseLong(parts[0].trim());
                int version = Integer.parseInt(parts[1].trim());
                result.put(id, version);
            } catch (Exception ignored) {
            }
        }

        return result;
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
