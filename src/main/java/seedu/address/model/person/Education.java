package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * An immutable, trimmed education value.
 * An empty value represents an absent optional field.
 */
public final class Education {
    public static final String MESSAGE_CONSTRAINTS = "Education must contain only printable text.";
    public static final String VALIDATION_REGEX = "[\\x20-\\x7E]*";

    public final String value;

    /**
     * Creates a validated value, preserving case and internal spaces.
     */
    public Education(String value) {
        requireNonNull(value);
        String trimmed = value.trim();
        checkArgument(isValidEducation(trimmed), MESSAGE_CONSTRAINTS);
        this.value = trimmed;
    }

    /**
     * Returns whether the given trimmed value is valid.
     */
    public static boolean isValidEducation(String test) {
        requireNonNull(test);
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Education field && value.equals(field.value);
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
