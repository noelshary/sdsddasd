package com.nova.module;

public enum Category {
    MOVEMENT("Bewegung"),
    COMBAT("Kampf"),
    RENDER("Render");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
