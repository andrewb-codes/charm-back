package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.dto.sort.SortBy;
import ru.andrewb.charm.back.dto.sort.SortOrder;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;

import static ru.andrewb.charm.back.utils.StringUtils.stripToNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestToProfileFilterMapper implements Mapper<HttpServletRequest, ProfileFilter> {

    private static final RequestToProfileFilterMapper INSTANCE = new RequestToProfileFilterMapper();

    public static RequestToProfileFilterMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public ProfileFilter map(HttpServletRequest req) {
        return map(req, new ProfileFilter());
    }

    @Override
    public ProfileFilter map(HttpServletRequest req, ProfileFilter filter) {
        filter.setEmailStartsWith(stripToNull(req.getParameter("emailStartsWith")));
        filter.setNameStartsWith(stripToNull(req.getParameter("nameStartsWith")));
        filter.setSurnameStartsWith(stripToNull(req.getParameter("surnameStartsWith")));
        filter.setLowerAgeBound(parseInt(req.getParameter("ltAge")));
        filter.setGreaterAndEqualAgeBound(parseInt(req.getParameter("gteAge")));

        String r = stripToNull(req.getParameter("role"));
        if (r != null) {
            try {
                filter.setRole(Role.valueOf(r.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        String st = stripToNull(req.getParameter("status"));
        if (st != null) {
            try {
                filter.setStatus(Status.valueOf(st.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        String sb = stripToNull(req.getParameter("sortBy"));
        if (sb != null) {
            try {
                filter.setSortBy(SortBy.valueOf(sb.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        String so = stripToNull(req.getParameter("sortOrder"));
        if (so != null) {
            try {
                filter.setSortOrder(SortOrder.valueOf(so.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        filter.setPage(parseInt(req.getParameter("page")));
        filter.setPageSize(parseInt(req.getParameter("pageSize")));

        return filter;
    }

    private static Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
