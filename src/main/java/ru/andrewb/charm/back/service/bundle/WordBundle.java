package ru.andrewb.charm.back.service.bundle;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public abstract class WordBundle {

    private final ResourceBundle resourceBundle;

    protected WordBundle(Locale locale) {
        this.resourceBundle = ResourceBundle.getBundle("words", locale);
    }

    public String getWord(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e1) {
            try {
                return resourceBundle.getString(key.toLowerCase());
            } catch (MissingResourceException e2) {
                // fallback: last path segment
                String[] pathArray = key.split("\\.");
                return pathArray[pathArray.length - 1];
            }
        } catch (Exception e) {
            return "* empty *";
        }
    }
}
