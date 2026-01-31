package ru.andrewb.charm.back.service.bundle;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class WordBundleRu extends WordBundle {

    private static final WordBundleRu INSTANCE = new WordBundleRu();

    public static WordBundleRu getInstance() {
        return INSTANCE;
    }

    private WordBundleRu() {
        super(Locale.of("ru"));
    }
}
