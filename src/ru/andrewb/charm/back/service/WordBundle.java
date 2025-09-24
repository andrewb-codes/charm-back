package ru.andrewb.charm.back.service;

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
            result = resourceBundle.getString(key.toLowerCase());
        } catch (MissingResourceException | ClassCastException e) {
            result = key;
        } catch (Exception e) {
            result = "* empty *";
        }
        return result;
    }
}
