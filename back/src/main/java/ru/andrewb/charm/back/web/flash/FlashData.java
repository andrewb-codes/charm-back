package ru.andrewb.charm.back.web.flash;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class FlashData implements Serializable {
    private final List<String> errors = new ArrayList<>();
    private final Map<String, String> fields = new HashMap<>();

    public void addError(String code) {
        if (code != null) errors.add(code);
    }

    public void putField(String key, String val) {
        if (key != null) fields.put(key, val);
    }

    public Object getField(String key) {
        return fields.get(key);
    }
}
