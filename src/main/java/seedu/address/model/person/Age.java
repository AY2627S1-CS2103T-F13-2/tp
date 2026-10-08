package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * An immutable, trimmed age value.
 * An empty value represents an absent optional field.
 */
public final class Age {
    public static final String MESSAGE_CONSTRAINTS = "Age must be a whole number from 1 to 120, or blank.";
    public static final String VALIDATION_REGEX = "(?:[1-9]|[1-9][0-9]|1[01][0-9]|120)?";

    public final String value;

    /**
     * Creates a validated value, preserving case and internal spaces.
     */
    public Age(String value) {
        requireNonNull(value);
        String trimmed = value.trim();
        checkArgument(isValidAge(trimmed), MESSAGE_CONSTRAINTS);
        this.value = trimmed;
    }

    /**
     * Returns whether the given trimmed value is valid.
     */
    public static boolean isValidAge(String test) {
        requireNonNull(test);
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Age field && value.equals(field.value);
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
