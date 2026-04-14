package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.andrewb.charm.back.controller.form.ProfilesFilterForm;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.dto.ProfileUpdateStatusDto;
import ru.andrewb.charm.back.dto.sort.SortBy;
import ru.andrewb.charm.back.dto.sort.SortOrder;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;
import ru.andrewb.charm.back.normalizer.ProfileFilterDefaults;
import ru.andrewb.charm.back.security.SecurityRules;
import ru.andrewb.charm.back.service.ProfileService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.andrewb.charm.back.utils.StringUtils.stripToNull;
import static ru.andrewb.charm.back.web.Urls.ADMIN_PROFILES_URL;
import static ru.andrewb.charm.back.web.Views.PROFILES;

@Controller
@RequestMapping(ADMIN_PROFILES_URL)
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfilesController {

    private final ProfileService service;

    public AdminProfilesController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public String getProfiles(
            @ModelAttribute("profilesFilterForm") ProfilesFilterForm form,
            Model model
    ) {
        ProfileFilter filter = toProfileFilter(form);

        var probe = new ProfileFilter();
        copyProperties(filter, probe);
        probe.setPageSize(filter.getPageSize() + 1);

        var items = service.findAll(probe);
        boolean hasNext = items.size() > filter.getPageSize();
        if (hasNext) items = items.subList(0, filter.getPageSize());

        boolean hasPrev = filter.getPage() > 1;

        model.addAttribute("profiles", items);
        model.addAttribute("filter", filter);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("hasNext", hasNext);

        return PROFILES;
    }

    @PutMapping
    public String updateStatuses(
            @RequestParam(name = "statusesWithIds", required = false) List<String> statusesWithIds,
            @RequestParam(name = "versionsWithIds", required = false) List<String> versionsWithIds,
            @RequestParam(name = "back", required = false) String back,
            HttpServletRequest req
    ) {
        List<ProfileUpdateStatusDto> dtoList = mapStatuses(statusesWithIds, versionsWithIds);;
        service.updateStatuses(dtoList);

        String ctx = req.getContextPath();
        if (SecurityRules.isSafeInternalRedirect(ctx, back, ADMIN_PROFILES_URL)) {
            return "redirect:" + back;
        }
        return "redirect:" + ADMIN_PROFILES_URL;
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

    private void copyProperties(ProfileFilter f, ProfileFilter copy) {
        copy.setEmailStartsWith(f.getEmailStartsWith());
        copy.setNameStartsWith(f.getNameStartsWith());
        copy.setSurnameStartsWith(f.getSurnameStartsWith());
        copy.setLowerAgeBound(f.getLowerAgeBound());
        copy.setGreaterAndEqualAgeBound(f.getGreaterAndEqualAgeBound());
        copy.setRole(f.getRole());
        copy.setStatus(f.getStatus());
        copy.setSortBy(f.getSortBy());
        copy.setSortOrder(f.getSortOrder());
        copy.setPage(f.getPage());
        copy.setPageSize(f.getPageSize());
    }
}
