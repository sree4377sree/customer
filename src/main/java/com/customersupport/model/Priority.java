package com.customersupport.model;

public enum Priority {
    LOW("Low", "bg-success text-white"),
    MEDIUM("Medium", "bg-warning text-dark"),
    HIGH("High", "bg-danger text-white");

    private final String displayName;
    private final String badgeClass;

    Priority(String displayName, String badgeClass) {
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
