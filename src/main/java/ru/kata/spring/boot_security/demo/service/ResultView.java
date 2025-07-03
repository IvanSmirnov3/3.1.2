package ru.kata.spring.boot_security.demo.service;

import java.util.HashMap;
import java.util.Map;

public class ResultView {
    private final String viewName;
    private final Map<String, Object> modelAttributes = new HashMap<>();

    public ResultView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, Object> getModelAttributes() {
        return modelAttributes;
    }

    public ResultView add(String key, Object value) {
        modelAttributes.put(key, value);
        return this;
    }
}

