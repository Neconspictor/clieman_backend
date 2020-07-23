package de.necon.dateman_backend.model;

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
}