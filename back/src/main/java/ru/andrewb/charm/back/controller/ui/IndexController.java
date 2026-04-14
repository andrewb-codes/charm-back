package ru.andrewb.charm.back.controller.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static ru.andrewb.charm.back.web.Urls.INDEX_URL;
import static ru.andrewb.charm.back.web.Views.INDEX;

@Controller
@RequestMapping(INDEX_URL)
public class IndexController {

    @GetMapping
    public String getIndexPage() {
        return INDEX;
    }
}
