package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.model.Status;

import static ru.andrewb.charm.back.utils.Strings.stripToNull;

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

        Integer ltAge = parseInt(req.getParameter("ltAge"));
        Integer gteAge = parseInt(req.getParameter("gteAge"));

        filter.setLowerAgeBound(ltAge);
        filter.setGreaterAndEqualAgeBound(gteAge);

        String st = stripToNull(req.getParameter("status"));
        if (st != null) {
            try {
                filter.setStatus(Status.valueOf(st.toUpperCase()));
            } catch (IllegalArgumentException ignore) {
            }
        }

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
