package ru.andrewb.charm.back.controller.rest;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.bootstrap.AppComponents;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.mapper.JsonMapper;
import ru.andrewb.charm.back.mapper.RequestToProfileFilterMapper;
import ru.andrewb.charm.back.normalizer.ProfileFilterDefaults;

import java.io.IOException;

import static ru.andrewb.charm.back.web.Urls.PROFILES_REST_URL;

@Slf4j
@WebServlet(PROFILES_REST_URL)
public class ProfilesController extends HttpServlet {

    private final ProfileService service = AppComponents.PROFILE_SERVICE;
    private final RequestToProfileFilterMapper requestToProfileFilterMapper = RequestToProfileFilterMapper.getInstance();
    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!AuthUtils.isAuthenticatedAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        ProfileFilter f = requestToProfileFilterMapper.map(req);
        ProfileFilterDefaults.normalize(f);

        resp.setContentType("application/json;charset=UTF-8");
        jsonMapper.writeValue(resp.getWriter(), service.findAll(f));
    }
}
