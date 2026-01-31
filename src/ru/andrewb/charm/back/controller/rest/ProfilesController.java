package ru.andrewb.charm.back.controller.rest;

import com.fasterxml.jackson.databind.DatabindException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.mapper.JsonMapper;
import ru.andrewb.charm.back.mapper.RequestToProfileFilterMapper;
import ru.andrewb.charm.back.normalizer.ProfileFilterDefaults;
import ru.andrewb.charm.back.service.ProfileService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static ru.andrewb.charm.back.utils.Urls.PROFILES_REST_URL;

@Slf4j
@WebServlet(PROFILES_REST_URL)
public class ProfilesController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final RequestToProfileFilterMapper requestToProfileFilterMapper = RequestToProfileFilterMapper.getInstance();
    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ProfileFilter f = requestToProfileFilterMapper.map(req);
            ProfileFilterDefaults.normalize(f);

            resp.setContentType("application/json;charset=UTF-8");
            jsonMapper.writeValue(resp.getWriter(), service.findAll(f));

        } catch (DatabindException e) {
            req.setAttribute("errors", List.of("error.param.invalid"));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
