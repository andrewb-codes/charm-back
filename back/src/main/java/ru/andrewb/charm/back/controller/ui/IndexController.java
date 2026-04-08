package ru.andrewb.charm.back.controller.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import static ru.andrewb.charm.back.web.Urls.INDEX_URL;
import static ru.andrewb.charm.back.web.Views.INDEX;

@Controller
public class IndexController {

    @GetMapping(INDEX_URL)
    public String getIndexPage() {
        return INDEX;
    }
}
