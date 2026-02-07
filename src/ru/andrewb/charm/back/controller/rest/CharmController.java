package ru.andrewb.charm.back.controller.rest;

import com.fasterxml.jackson.databind.DatabindException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.dto.CharmDto;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.JsonMapper;
import ru.andrewb.charm.back.normalizer.CharmDtoDefaults;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.CharmService;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static ru.andrewb.charm.back.web.Urls.CHARM_REST_URL;

@WebServlet(CHARM_REST_URL)
public class CharmController extends HttpServlet {

    private final CharmService service = CharmService.getInstance();
    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    public record NextResponse(ProfileSimpleDto profile) {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDetailsDto user = AuthUtils.getUserOrNull(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        var dto = new CharmDto();
        dto.setFromProfileId(user.getId());
        CharmDtoDefaults.normalize(dto);

        Optional<ProfileSimpleDto> nextOpt = service.getNext(dto);
        resp.setContentType("application/json;charset=UTF-8");
        jsonMapper.writeValue(resp.getWriter(), new NextResponse(nextOpt.orElse(null)));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDetailsDto user = AuthUtils.getUserOrNull(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try (BufferedReader reader = req.getReader()) {
            var dto = jsonMapper.readValue(reader, CharmDto.class);
            dto.setFromProfileId(user.getId());
            CharmDtoDefaults.normalize(dto);

            Optional<ProfileSimpleDto> nextOpt = service.getNext(dto);
            resp.setContentType("application/json;charset=UTF-8");
            jsonMapper.writeValue(resp.getWriter(), new NextResponse(nextOpt.orElse(null)));

        } catch (DatabindException e) {
            req.setAttribute("errors", List.of("error.param.invalid"));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
