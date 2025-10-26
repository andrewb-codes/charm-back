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
import ru.andrewb.charm.back.service.ProfileService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static ru.andrewb.charm.back.utils.UrlUtils.PROFILES_REST_URL;

@WebServlet(PROFILES_REST_URL)
@Slf4j
public class ProfilesController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final RequestToProfileFilterMapper requestToProfileFilterMapper = RequestToProfileFilterMapper.getInstance();
    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter w = resp.getWriter()) {
            ProfileFilter filter = requestToProfileFilterMapper.map(req);
            jsonMapper.writeValue(w, service.findAll(filter));
        } catch (DatabindException e) {
            req.setAttribute("errors", List.of(e.getLocalizedMessage(), e.getOriginalMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
