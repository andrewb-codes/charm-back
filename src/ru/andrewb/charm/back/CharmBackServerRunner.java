package ru.andrewb.charm.back;

import ru.andrewb.charm.back.controller.ProfileController;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.service.ProfileService;

import java.io.IOException;

public class CharmBackServerRunner {

    public static void main(String[] args) throws IOException {

        ProfileController controller = new ProfileController(new ProfileService(new ProfileDao()));

        CharmHttpServer server = new CharmHttpServer(5);
        server.start();
    }
}
