package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * An immutable, trimmed note value.
 * An empty value represents an absent optional field.
 */
public final class Note {
    public static final String MESSAGE_CONSTRAINTS = "Notes must contain only printable text.";
    public static final String VALIDATION_REGEX = "[\\x20-\\x7E]*";

    public final String value;

    /**
     * Creates a validated value, preserving case and internal spaces.
     */
    public Note(String value) {
        requireNonNull(value);
        String trimmed = value.trim();
        checkArgument(isValidNote(trimmed), MESSAGE_CONSTRAINTS);
        this.value = trimmed;
    }

    /**
     * Returns whether the given trimmed value is valid.
     */
    public static boolean isValidNote(String test) {
        requireNonNull(test);
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Note field && value.equals(field.value);
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
