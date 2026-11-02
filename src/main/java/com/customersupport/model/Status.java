package com.customersupport.model;

public enum Status {
    OPEN("Open", "bg-primary text-white"),
    IN_PROGRESS("In Progress", "bg-info text-dark"),
    RESOLVED("Resolved", "bg-success text-white"),
    CLOSED("Closed", "bg-secondary text-white");

    private final String displayName;
    private final String badgeClass;

    Status(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
