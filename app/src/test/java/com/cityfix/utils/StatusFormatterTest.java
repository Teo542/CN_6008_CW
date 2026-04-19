package com.cityfix.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StatusFormatterTest {
    @Test
    public void formatsKnownStatuses() {
        assertEquals("Open", StatusFormatter.formatStatus("open"));
        assertEquals("In Progress", StatusFormatter.formatStatus("in_progress"));
        assertEquals("Resolved", StatusFormatter.formatStatus("resolved"));
    }

    @Test
    public void unknownStatusIsSafe() {
        assertEquals("Unknown", StatusFormatter.formatStatus(null));
        assertEquals("Unknown", StatusFormatter.formatStatus(""));
        assertEquals("Unknown", StatusFormatter.formatStatus("blocked"));
    }

    @Test
    public void formatsStatusTransition() {
        assertEquals("Open -> Resolved",
                StatusFormatter.formatStatusTransition("open", "resolved"));
        assertEquals("Unknown -> In Progress",
                StatusFormatter.formatStatusTransition(null, "in_progress"));
    }
}
