package ru.andrewb.charm.back.controller.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;
import ru.andrewb.charm.back.utils.ConnectionManager;
import ru.andrewb.charm.back.utils.RedisManager;

import static ru.andrewb.charm.back.normalizer.ProfileFilterDefaults.AVAILABLE_PAGE_SIZES;

@Slf4j
@WebListener
public class ApplicationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        ctx.setAttribute("genders", Gender.values());
        ctx.setAttribute("statuses", Status.values());
        ctx.setAttribute("roles", Role.values());
        ctx.setAttribute("availablePageSizes", AVAILABLE_PAGE_SIZES);
        log.info("Application context initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            ConnectionManager.closePool();
            RedisManager.close();
            log.info("Application context destroyed, pool closed");
        } catch (Exception e) {
            log.warn("Failed to close pool on shutdown", e);
        }
    }
}
