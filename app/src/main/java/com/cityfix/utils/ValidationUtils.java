package com.cityfix.utils;

/**
 * Shared validation rules for user-facing forms.
 * Keeping these rules outside activities/fragments makes them unit-testable
 * and prevents the Android UI from drifting into different limits per screen.
 */
public final class ValidationUtils {
    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MAX_COMMENT_LENGTH = 500;
    public static final int MIN_PASSWORD_LENGTH = 6;

    private ValidationUtils() {
    }

    public static boolean isValidTitle(String title) {
        return titleError(title) == null;
    }

    public static boolean isValidDescription(String description) {
        return descriptionError(description) == null;
    }

    public static boolean isValidComment(String comment) {
        return commentError(comment) == null;
    }

    public static boolean isValidPassword(String password) {
        return passwordError(password) == null;
    }

    public static String titleError(String title) {
        String value = normalize(title);
        if (value.isEmpty()) {
            return "Please enter a title.";
        }
        if (value.length() > MAX_TITLE_LENGTH) {
            return "Title must be 100 characters or fewer.";
        }
        return null;
    }

    public static String descriptionError(String description) {
        String value = normalize(description);
        if (value.length() > MAX_DESCRIPTION_LENGTH) {
            return "Description must be 500 characters or fewer.";
        }
        return null;
    }

    public static String commentError(String comment) {
        String value = normalize(comment);
        if (value.isEmpty()) {
            return "Please enter a comment.";
        }
        if (value.length() > MAX_COMMENT_LENGTH) {
            return "Comment must be 500 characters or fewer.";
        }
        return null;
    }

    public static String passwordError(String password) {
        String value = normalize(password);
        if (value.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least 6 characters and include a letter, a number, and a symbol.";
        }
        if (!containsLetter(value) || !containsDigit(value) || !containsSymbol(value)) {
            return "Password must include at least one letter, one number, and one symbol.";
        }
        return null;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean containsLetter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLetter(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsDigit(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsSymbol(String value) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch)) {
                return true;
            }
        }
        return false;
    }
}
