package com.unipay.enums;

/**
 * Enum to define various permissions within the system.
 */
public enum PermissionName {

    // ===================== USER Permissions =====================
    VIEW_PROFILE("View Profile"),
    UPDATE_PROFILE("Update Profile"),
    VIEW_ORDERS("View Orders"),
    CREATE_ORDER("Create Order"),
    VIEW_PAYMENTS("View Payments"),
    MAKE_PAYMENT("Make Payment"),
    VIEW_INVOICES("View Invoices"),
    VIEW_SUBSCRIPTIONS("View Subscriptions"),
    MANAGE_SUBSCRIPTIONS("Manage Subscriptions"),
    VIEW_BALANCES("View Balances"),
    VIEW_TERMINALS("View Terminals"),

    // ===================== CLIENT Permissions =====================
    VIEW_DASHBOARD("View Dashboard"),
    EXPORT_DATA("Export Data"),
    IMPORT_DATA("Import Data"),
    CREATE_PAYMENT("Create Payment"),
    CANCEL_PAYMENT("Cancel Payment"),
    REFUND_PAYMENT("Refund Payment"),
    VIEW_REFUNDS("View Refunds"),
    CREATE_REFUND("Create Refund"),
    CANCEL_REFUND("Cancel Refund"),
    VIEW_CUSTOMERS("View Customers"),
    MANAGE_CUSTOMERS("Manage Customers"),
    VIEW_MANDATES("View Mandates"),
    MANAGE_MANDATES("Manage Mandates"),
    VIEW_PROFILES("View Profiles"),
    MANAGE_PROFILES("Manage Profiles"),
    CREATE_INVOICE("Create Invoice"),
    CANCEL_INVOICE("Cancel Invoice"),
    VIEW_SETTLEMENTS("View Settlements"),
    CANCEL_ORDER("Cancel Order"),
    VIEW_SHIPMENTS("View Shipments"),
    CREATE_SHIPMENT("Create Shipment"),
    CANCEL_SHIPMENT("Cancel Shipment"),
    VIEW_ORGANIZATIONS("View Organizations"),
    MANAGE_ORGANIZATIONS("Manage Organizations"),
    VIEW_ONBOARDING_STATUS("View Onboarding Status"),
    MANAGE_ONBOARDING("Manage Onboarding"),
    VIEW_PAYMENT_LINKS("View Payment Links"),
    CREATE_PAYMENT_LINK("Create Payment Link"),
    MANAGE_TERMINALS("Manage Terminals"),

    // ===================== ADMIN Permissions =====================
    MANAGE_USERS("Manage Users"),
    CREATE_USER("Create User"),
    UPDATE_USER("Update User"),
    DELETE_USER("Delete User"),
    VIEW_USER("View User"),
    MANAGE_ROLES("Manage Roles"),
    CREATE_ROLE("Create Role"),
    UPDATE_ROLE("Update Role"),
    DELETE_ROLE("Delete Role"),
    VIEW_ROLE("View Role"),
    MANAGE_PERMISSIONS("Manage Permissions"),
    CREATE_PERMISSION("Create Permission"),
    UPDATE_PERMISSION("Update Permission"),
    DELETE_PERMISSION("Delete Permission"),
    VIEW_PERMISSION("View Permission"),
    MANAGE_SETTINGS("Manage Settings"),
    ACCESS_API("Access API"),
    VIEW_AUDIT_LOGS("View Audit Logs");

    private final String description;

    PermissionName(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
