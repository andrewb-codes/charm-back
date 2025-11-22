package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.RequestToProfileFilterMapper;
import ru.andrewb.charm.back.normalizer.ProfileFilterDefaults;
import ru.andrewb.charm.back.service.ProfileService;

import java.io.IOException;

import static ru.andrewb.charm.back.utils.UrlUtils.MATCHES_URL;
import static ru.andrewb.charm.back.utils.UrlUtils.getJspPath;

@WebServlet(MATCHES_URL)
@Slf4j
public class MatchesController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final RequestToProfileFilterMapper requestToProfileFilterMapper = RequestToProfileFilterMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var user = (UserDetailsDto) req.getSession().getAttribute("userDetails");

        var f = requestToProfileFilterMapper.map(req);
        ProfileFilterDefaults.normalize(f);

        int page = Math.max(1, f.getPage());
        int pageSize = Math.max(1, f.getPageSize());
        int limit = pageSize + 1;
        int offset = (page - 1) * pageSize;

        var items = service.findMatches(user.getId(), limit, offset);
        boolean hasNext = items.size() > f.getPageSize();
        if (hasNext) items = items.subList(0, f.getPageSize());

        boolean hasPrev = f.getPage() > 1;


        req.setAttribute("matches", items);
        req.setAttribute("filter", f);
        req.setAttribute("hasPrev", hasPrev);
        req.setAttribute("hasNext", hasNext);

        req.getRequestDispatcher(getJspPath("/matches")).forward(req, resp);
    }
}
