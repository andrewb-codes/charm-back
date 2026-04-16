package ru.andrewb.charm.back.controller.ui.support;

import lombok.experimental.UtilityClass;
import org.springframework.validation.BindingResult;

import java.util.List;

@UtilityClass
public class BindingErrors {

    public static List<String> extract(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .toList();
    }
}
