package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * An immutable, trimmed guardian contact value.
 * An empty value represents an absent optional field.
 */
public final class GuardianContact {
    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers should only contain numbers, and it should be 8 digits long.";
    public static final String VALIDATION_REGEX = "(?:[0-9]{8})?";

    public final String value;

    /**
     * Creates a validated value, preserving case and internal spaces.
     */
    public GuardianContact(String value) {
        requireNonNull(value);
        String trimmed = value.trim();
        checkArgument(isValidGuardianContact(trimmed), MESSAGE_CONSTRAINTS);
        this.value = trimmed;
    }

    /**
     * Returns whether the given trimmed value is valid.
     */
    public static boolean isValidGuardianContact(String test) {
        requireNonNull(test);
        return test.isEmpty() || Phone.isValidStudentPhone(test);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof GuardianContact field && value.equals(field.value);
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
