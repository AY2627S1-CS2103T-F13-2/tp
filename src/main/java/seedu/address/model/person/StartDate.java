package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents the date on which tuition starts for a student.
 */
public class StartDate {

    public static final String MESSAGE_CONSTRAINTS =
            "Start date must be a valid calendar date in DD-MMM format (e.g. 19-Sep). 29-Feb is allowed.";
    public static final String VALIDATION_REGEX = "\\d{2}-[A-Za-z]{3}";

    public final String value;

    /** Creates a {@code StartDate}. */
    public StartDate(String startDate) {
        requireNonNull(startDate);
        checkArgument(isValidStartDate(startDate), MESSAGE_CONSTRAINTS);
        value = startDate;
    }

    /** Checks the stored format; parsers additionally validate calendar dates for new input. */
    public static boolean isValidStartDate(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof StartDate otherStartDate
                && value.equals(otherStartDate.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
