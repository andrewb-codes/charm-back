package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.ProfileUpdateStatusDto;
import ru.andrewb.charm.back.model.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestToProfileUpdateStatusDtoMapper implements Mapper<HttpServletRequest, List<ProfileUpdateStatusDto>> {

    private static final RequestToProfileUpdateStatusDtoMapper INSTANCE = new RequestToProfileUpdateStatusDtoMapper();

    public static RequestToProfileUpdateStatusDtoMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public List<ProfileUpdateStatusDto> map(HttpServletRequest req) {
        return map(req, new ArrayList<>());
    }

    @Override
    public List<ProfileUpdateStatusDto> map(HttpServletRequest req, List<ProfileUpdateStatusDto> dtoList) {
        Map<Long, Integer> versionById = parseVersions(req.getParameterValues("versionsWithIds"));

        String[] statusesWithIds = req.getParameterValues("statusesWithIds");
        if (statusesWithIds == null) return dtoList;

        for (String statusWithId : statusesWithIds) {
            if (statusWithId == null) continue;
            statusWithId = statusWithId.trim();
            if (statusWithId.isBlank() || "skip".equalsIgnoreCase(statusWithId)) continue;

            String[] parts = statusWithId.split("_");
            if (parts.length != 2) continue;

            try {
                Status status = Status.valueOf(parts[0].trim().toUpperCase());
                long id = Long.parseLong(parts[1].trim());

                Integer version = versionById.get(id);
                if (version == null) continue;

                var dto = new ProfileUpdateStatusDto();
                dto.setId(id);
                dto.setStatus(status);
                dto.setVersion(version);
                dtoList.add(dto);

            } catch (Exception ignored) {
            }
        }
        return dtoList;
    }

    private Map<Long, Integer> parseVersions(String[] versionsWithIds) {
        Map<Long, Integer> map = new HashMap<>();
        if (versionsWithIds == null) return map;

        for (String v : versionsWithIds) {
            if (v == null) continue;
            v = v.trim();
            if (v.isBlank()) continue;

            String[] parts = v.split("_");
            if (parts.length != 2) continue;

            try {
                long id = Long.parseLong(parts[0].trim());
                int version = Integer.parseInt(parts[1].trim());
                map.put(id, version);
            } catch (Exception ignored) {
            }
        }
        return map;
    }
}
