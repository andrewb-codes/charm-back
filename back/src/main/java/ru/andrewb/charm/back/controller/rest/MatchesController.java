package ru.andrewb.charm.back.controller.rest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.bootstrap.AppComponents;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.JsonMapper;
import ru.andrewb.charm.back.mapper.RequestToProfileFilterMapper;
import ru.andrewb.charm.back.normalizer.ProfileFilterDefaults;
import ru.andrewb.charm.back.security.AuthUtils;

import java.io.IOException;
import java.util.List;

import static ru.andrewb.charm.back.web.Urls.MATCHES_REST_URL;

@WebServlet(MATCHES_REST_URL)
public class MatchesController extends HttpServlet {

    private final ProfileService service = AppComponents.PROFILE_SERVICE;
    private final RequestToProfileFilterMapper requestToProfileFilterMapper = RequestToProfileFilterMapper.getInstance();
    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    public record MatchesResponse(List<ProfileGetDto> items, boolean hasNext) {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDetailsDto user = AuthUtils.getUserOrNull(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        var f = requestToProfileFilterMapper.map(req);
        ProfileFilterDefaults.normalize(f);

        int page = Math.max(1, f.getPage());
        int pageSize = Math.max(1, f.getPageSize());
        int limit = pageSize + 1;
        int offset = (page - 1) * pageSize;

        var items = service.findMatches(user.getId(), limit, offset);
        boolean hasNext = items.size() > f.getPageSize();
        if (hasNext) items = items.subList(0, pageSize);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=UTF-8");
        jsonMapper.writeValue(resp.getWriter(), new MatchesResponse(items, hasNext));
    }
}
