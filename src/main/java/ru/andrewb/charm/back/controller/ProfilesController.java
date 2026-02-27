package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.dto.ProfileUpdateStatusDto;
import ru.andrewb.charm.back.mapper.RequestToProfileFilterMapper;
import ru.andrewb.charm.back.mapper.RequestToProfileUpdateStatusDtoMapper;
import ru.andrewb.charm.back.normalizer.ProfileFilterDefaults;
import ru.andrewb.charm.back.security.SecurityRules;

import java.io.IOException;
import java.util.List;

import static ru.andrewb.charm.back.utils.BeanUtils.copyProperties;
import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.web.Urls.PROFILES_URL;
import static ru.andrewb.charm.back.web.Views.getJspPath;


@Slf4j
@WebServlet(PROFILES_URL)
public class ProfilesController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final RequestToProfileFilterMapper requestToProfileFilterMapper = RequestToProfileFilterMapper.getInstance();
    private final RequestToProfileUpdateStatusDtoMapper requestToProfileUpdateStatusMapper =
            RequestToProfileUpdateStatusDtoMapper.getInstance();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticatedAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

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

        req.getRequestDispatcher(getJspPath(PROFILES_URL)).forward(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!AuthUtils.isAuthenticatedAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<ProfileUpdateStatusDto> dtoList = requestToProfileUpdateStatusMapper.map(req);
        service.updateStatuses(dtoList);

        String ctx = req.getContextPath();
        String back = req.getParameter("back");
        if (SecurityRules.isSafeInternalRedirect(ctx, back, PROFILES_URL)) {
            resp.sendRedirect(back);
        } else {
            resp.sendRedirect(ctx + PROFILES_URL);
        }

    }
}
