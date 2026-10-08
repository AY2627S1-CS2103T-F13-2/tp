package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * An immutable, trimmed subject value.
 */
public final class Subject {
    public static final String MESSAGE_CONSTRAINTS =
            "Subject must contain only letters, digits, spaces, or hyphens and must not be blank.";
    public static final String VALIDATION_REGEX = "[A-Za-z0-9 -]+";

    public final String value;

    /**
     * Creates a validated value, preserving case and internal spaces.
     */
    public Subject(String value) {
        requireNonNull(value);
        String trimmed = value.trim();
        checkArgument(isValidSubject(trimmed), MESSAGE_CONSTRAINTS);
        this.value = trimmed;
    }

    /**
     * Returns whether the given trimmed value is valid.
     */
    public static boolean isValidSubject(String test) {
        requireNonNull(test);
        return !test.trim().isEmpty() && test.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Subject field && value.equals(field.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
