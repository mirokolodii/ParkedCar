package com.unagit.parkedcar.Helpers;

/**
 * Created by a264889 on 02.02.2018.
 */

/**
 * Simple class to keep date or time value with corresponding text.
 * If value > 1, append "s" at the end of text.
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
    String getText() {
        return this.text;
    }
    long getValue() {
        return this.value;
    }
}
