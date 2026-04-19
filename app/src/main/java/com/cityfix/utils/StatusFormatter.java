package com.cityfix.utils;

/**
 * Converts Firestore status identifiers into safe user-facing labels.
 */
public final class StatusFormatter {
    private StatusFormatter() {
    }

    public static String formatStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Unknown";
        }

        switch (status.trim()) {
            case Constants.STATUS_OPEN:
                return "Open";
            case Constants.STATUS_IN_PROGRESS:
                return "In Progress";
            case Constants.STATUS_RESOLVED:
                return "Resolved";
            default:
                return "Unknown";
        }
    }

    public static String formatStatusTransition(String previousStatus, String newStatus) {
        return formatStatus(previousStatus) + " -> " + formatStatus(newStatus);
    }
}
