package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * An immutable, trimmed start date value.
 */
public final class StartDate {
    public static final String MESSAGE_CONSTRAINTS = "Start date must be in the format DD-MMM, "
            + "where DD are numbers and MMM are texts.(E.g. 19-Sep)";
    public static final String VALIDATION_REGEX = "[0-9]{2}-[A-Za-z]{3}";

    public final String value;

    /**
     * Creates a validated value, preserving case and internal spaces.
     */
    public StartDate(String value) {
        requireNonNull(value);
        String trimmed = value.trim();
        checkArgument(isValidStartDate(trimmed), MESSAGE_CONSTRAINTS);
        this.value = trimmed;
    }

    /**
     * Returns whether the given trimmed value is valid.
     */
    public static boolean isValidStartDate(String test) {
        requireNonNull(test);
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof StartDate field && value.equals(field.value);
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
