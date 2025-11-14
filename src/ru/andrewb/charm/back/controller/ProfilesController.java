package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.mapper.RequestToProfileFilterMapper;
import ru.andrewb.charm.back.normalizer.ProfileFilterDefaults;
import ru.andrewb.charm.back.service.ProfileService;

import java.io.IOException;

import static ru.andrewb.charm.back.utils.BeanUtils.copyProperties;
import static ru.andrewb.charm.back.utils.UrlUtils.PROFILES_URL;
import static ru.andrewb.charm.back.utils.UrlUtils.getJspPath;

@WebServlet(PROFILES_URL)
@Slf4j
public class ProfilesController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final RequestToProfileFilterMapper requestToProfileFilterMapper = RequestToProfileFilterMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var f = requestToProfileFilterMapper.map(req);
        ProfileFilterDefaults.normalize(f);

        var probe = new ProfileFilter();
        copyProperties(f, probe);
        probe.setPageSize(f.getPageSize() + 1);

        var items = service.findAll(probe);
        boolean hasNext = items.size() > f.getPageSize();
        if (hasNext) items = items.subList(0, f.getPageSize());

        boolean hasPrev = f.getPage() > 1;


        req.setAttribute("profiles", items);
        req.setAttribute("filter", f);
        req.setAttribute("hasPrev", hasPrev);
        req.setAttribute("hasNext", hasNext);

        req.getRequestDispatcher(getJspPath("/profiles")).forward(req, resp);
    }
}
