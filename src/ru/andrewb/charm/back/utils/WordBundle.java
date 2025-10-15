package ru.andrewb.charm.back.utils;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class WordBundle {

    private final ResourceBundle resourceBundle;

    public WordBundle(String lang) {
        String language = "en";
        if ("ru".equals(lang)) {
            language = "ru";
        }

        Locale locale = Locale.of(language);

        this.resourceBundle = ResourceBundle.getBundle("words", locale);
    }

    public String getWord(String key) {
        String result;
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e1) {
            try {
                return resourceBundle.getString(key.toLowerCase());
            } catch (MissingResourceException e2) {
                return key;
            }
        }
    }
}
