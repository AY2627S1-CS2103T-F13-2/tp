package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Objects;

/** A subject taught to a student. */
public final class Subject {
    public static final String MESSAGE_CONSTRAINTS = "Subject must be 1 to 50 characters long, contain only "
            + "English letters, digits, spaces or hyphens, and include at least one letter or digit.";
    private static final String VALIDATION_REGEX = "(?=.{1,50}$)(?=.*[A-Za-z0-9])[A-Za-z0-9 -]+";

    public final String value;
    private final LessonSlot lessonSlot;

    /** Creates a validated subject with its weekly lesson slot. */
    public Subject(String value, LessonSlot lessonSlot) {
        requireNonNull(value);
        requireNonNull(lessonSlot);
        checkArgument(isValidSubject(value), MESSAGE_CONSTRAINTS);
        this.value = value;
        this.lessonSlot = lessonSlot;
    }

    public static boolean isValidSubject(String value) {
        return value.matches(VALIDATION_REGEX);
    }

    public LessonSlot getLessonSlot() {
        return lessonSlot;
    }

    /** Returns whether this subject has the supplied name, ignoring case. */
    public boolean hasName(String name) {
        return value.equalsIgnoreCase(name);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Subject subject
                && value.equalsIgnoreCase(subject.value) && lessonSlot.equals(subject.lessonSlot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.toLowerCase(Locale.ROOT), lessonSlot);
    }

    @Override
    public String toString() {
        return value + " (" + lessonSlot + ")";
    }
}
