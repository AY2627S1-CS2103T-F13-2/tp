package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/** A subject taught to a student. */
public class Subject {
    public static final String MESSAGE_CONSTRAINTS = "Subject must be 1 to 50 characters long, contain only "
            + "English letters, digits, spaces or hyphens, and include at least one letter or digit.";
    private static final String VALIDATION_REGEX = "(?=.{1,50}$)(?=.*[A-Za-z0-9])[A-Za-z0-9 -]+";

    public final String value;

    /** Creates a validated subject. */
    public Subject(String value) {
        requireNonNull(value);
        checkArgument(isValidSubject(value), MESSAGE_CONSTRAINTS);
        this.value = value;
    }

    public static boolean isValidSubject(String value) {
        return value.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Subject subject && value.equalsIgnoreCase(subject.value);
    }

    @Override
    public int hashCode() {
        return value.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
