package com.cityfix.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ValidationUtilsTest {
    @Test
    public void titleValidationRejectsEmptyAndWhitespace() {
        assertFalse(ValidationUtils.isValidTitle(""));
        assertFalse(ValidationUtils.isValidTitle("   "));
    }

    @Test
    public void titleValidationAllowsBoundaryAndRejectsTooLong() {
        assertTrue(ValidationUtils.isValidTitle(repeat("a", 100)));
        assertFalse(ValidationUtils.isValidTitle(repeat("a", 101)));
    }

    @Test
    public void descriptionValidationAllowsOptionalTextAndRejectsTooLong() {
        assertTrue(ValidationUtils.isValidDescription(""));
        assertTrue(ValidationUtils.isValidDescription(repeat("a", 500)));
        assertFalse(ValidationUtils.isValidDescription(repeat("a", 501)));
    }

    @Test
    public void commentValidationRequiresTextAndRejectsTooLong() {
        assertFalse(ValidationUtils.isValidComment(""));
        assertTrue(ValidationUtils.isValidComment(repeat("a", 500)));
        assertFalse(ValidationUtils.isValidComment(repeat("a", 501)));
    }

    @Test
    public void passwordValidationMatchesFirebaseMinimum() {
        assertFalse(ValidationUtils.isValidPassword("abcde"));
        assertTrue(ValidationUtils.isValidPassword("abcdef"));
        assertNull(ValidationUtils.passwordError("abcdef"));
    }

    private static String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
