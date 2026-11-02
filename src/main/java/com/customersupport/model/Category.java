package com.customersupport.model;

public enum Category {
    TECHNICAL("Technical Issue"),
    BILLING("Billing & Invoicing"),
    NETWORK("Network & Connectivity"),
    ACCOUNT_ACCESS("Account Access & Security"),
    GENERAL_INQUIRY("General Inquiry");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
