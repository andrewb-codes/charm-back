package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.RequestToCharmDtoMapper;
import ru.andrewb.charm.back.normalizer.CharmDtoDefaults;
import ru.andrewb.charm.back.service.CharmService;

import java.io.IOException;
import java.util.Optional;

import static ru.andrewb.charm.back.utils.Urls.CHARM_URL;
import static ru.andrewb.charm.back.utils.Views.getJspPath;

@WebServlet(CHARM_URL)
public class CharmController extends HttpServlet {

    private final CharmService service = CharmService.getInstance();

    private final RequestToCharmDtoMapper requestToCharmDtoMapper = RequestToCharmDtoMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var user = (UserDetailsDto) req.getSession().getAttribute("userDetails");

        var charmDto = requestToCharmDtoMapper.map(req);
        charmDto.setFromProfileId(user.getId());
        CharmDtoDefaults.normalize(charmDto);
        
        Optional<ProfileSimpleDto> nextOpt = service.getNext(charmDto);
        if (nextOpt.isPresent()) {
            req.setAttribute("next", nextOpt.get());
            req.getRequestDispatcher(getJspPath(CHARM_URL)).forward(req, resp);
        } else {
            // TODO: сделать страницу "упс, новых профилей нет, возвращайтесь позже"
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}

