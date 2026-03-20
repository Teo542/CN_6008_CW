package com.cityfix.utils;

public class Constants {

    // Firestore collections
    public static final String COLLECTION_REPORTS = "reports";
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_COMMENTS = "comments";
    public static final String COLLECTION_STATUS_HISTORY = "statusHistory";

    // Report statuses
    public static final String STATUS_OPEN = "open";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_RESOLVED = "resolved";

    // User roles
    public static final String ROLE_CITIZEN = "citizen";
    public static final String ROLE_ADMIN = "admin";

    // Categories
    public static final String CATEGORY_POTHOLE = "pothole";
    public static final String CATEGORY_STREETLIGHT = "streetlight";
    public static final String CATEGORY_FLOODING = "flooding";
    public static final String CATEGORY_VANDALISM = "vandalism";
    public static final String CATEGORY_OTHER = "other";

    // Intent extras
    public static final String EXTRA_REPORT_ID = "report_id";

    // Firebase Storage paths
    public static final String STORAGE_REPORTS = "reports";
}
