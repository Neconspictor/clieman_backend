package de.necon.clieman_backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Sex {

    DIVERSE("diverse"),
    FEMALE("female"),
    MALE("male");

    private String name;

    Sex(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @JsonCreator
    public static Sex fromString(String name) {
        return name == null
                ? null
                : Sex.valueOf(name.toUpperCase());
    }

    @JsonValue
    public String getName() {
        return name.toLowerCase();
    }
}