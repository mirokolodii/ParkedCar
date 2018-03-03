package com.unagit.parkedcar.helpers;

import java.util.Locale;

/**
 * This is a helper getter/setter class, which receives a set of (value, text).
 * If value > 1, amends char 's' at the end of text.
 */
class DurationPart {
    private long value;
    private String text;
    DurationPart(long value, String text) {
        this.value = value;
        if (value > 1) {
            this.text = text + "s";
        } else {
            this.text = text;
        }
    }

    /**
     * Returns date/time in a format:
     * '1 day', '5 hours', '32 minutes', '1 minute' etc.
     * @return 'value text'
     */
    String getDuration() {
        return String.format(Locale.getDefault(), "%d %s", value, text);
    }

    long getValue() {
        return this.value;
    }
}