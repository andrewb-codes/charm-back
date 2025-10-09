package ru.andrewb.charm.back.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Strings {
    public static String stripToNull(String s) {
        if (s == null) return null;
        String t = s.strip();
        return t.isEmpty() ? null : t;
    }
}
