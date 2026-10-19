package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents one optional free-text note about a student.
 * Surrounding whitespace is removed; internal whitespace and punctuation are preserved.
 */
public class Notes {

    public static final Notes EMPTY = new Notes("");

    public final String value;

    /**
     * Constructs notes from non-null text. Blank text represents no note.
     */
    public Notes(String notes) {
        value = requireNonNull(notes).trim();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof Notes otherNotes && value.equals(otherNotes.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
