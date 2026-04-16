package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.andrewb.charm.back.controller.request.ProfilesFilterRequest;
import ru.andrewb.charm.back.dto.ProfilesFilter;
import ru.andrewb.charm.back.mapper.ProfilesFilterRequestToProfileFilterMapper;
import ru.andrewb.charm.back.model.Status;
import ru.andrewb.charm.back.security.SecurityRules;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.service.command.ProfileUpdateStatusCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.andrewb.charm.back.web.Urls.ADMIN_PROFILES_URL;
import static ru.andrewb.charm.back.web.Views.PROFILES;

@Controller
@RequestMapping(ADMIN_PROFILES_URL)
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfilesController {

    private final ProfileService service;
    private final ProfilesFilterRequestToProfileFilterMapper mapper;

    public AdminProfilesController(
            ProfileService service,
            ProfilesFilterRequestToProfileFilterMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public String getProfiles(
            @Valid @ModelAttribute("profilesFilterRequest") ProfilesFilterRequest request,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            request = new ProfilesFilterRequest();
        }

        ProfilesFilter filter = mapper.map(request);

        var probe = new ProfilesFilter();
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
        List<ProfileUpdateStatusCommand> commandsList = mapStatuses(statusesWithIds, versionsWithIds);;
        service.updateStatuses(commandsList);

        String ctx = req.getContextPath();
        if (SecurityRules.isSafeInternalRedirect(ctx, back, ADMIN_PROFILES_URL)) {
            return "redirect:" + back;
        }
        return "redirect:" + ADMIN_PROFILES_URL;
    }

    private List<ProfileUpdateStatusCommand> mapStatuses(List<String> statusesWithIds, List<String> versionsWithIds) {
        Map<Long, Integer> versionById = parseVersions(versionsWithIds);
        List<ProfileUpdateStatusCommand> result = new ArrayList<>();

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

                var command = new ProfileUpdateStatusCommand();
                command.setId(id);
                command.setStatus(status);
                command.setVersion(version);
                result.add(command);

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

    private void copyProperties(ProfilesFilter f, ProfilesFilter copy) {
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
