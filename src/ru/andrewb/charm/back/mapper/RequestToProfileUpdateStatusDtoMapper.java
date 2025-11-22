package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.ProfileUpdateStatusDto;
import ru.andrewb.charm.back.model.Status;

import java.util.ArrayList;
import java.util.List;

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
        String[] statusesWithIds = req.getParameterValues("statusesWithIds");
        if (statusesWithIds == null) {
            return dtoList;
        }
        for (String statusWithId : statusesWithIds) {
            if ("skip".equals(statusWithId)) {
                continue;
            }
            String[] parts = statusWithId.trim().split("_");
            if (parts.length != 2) continue;
            try {
                var dto = new ProfileUpdateStatusDto();
                dto.setId(Long.parseLong(parts[1].trim()));
                dto.setStatus(Status.valueOf(parts[0].trim().toUpperCase()));
                dtoList.add(dto);
            } catch (Exception ignored) {
            }
        }
        return dtoList;
    }
}
