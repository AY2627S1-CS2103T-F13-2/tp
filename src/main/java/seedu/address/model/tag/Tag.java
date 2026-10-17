package seedu.address.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Tag in the address book.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final String MESSAGE_CONSTRAINTS =
            "Tags must contain only letters, digits, spaces, or hyphens and must not be blank.";
    public static final String VALIDATION_REGEX = "[A-Za-z0-9 -]+";

    public final String tagName;

    /**
     * Constructs a {@code Tag}.
     *
     * Surrounding spaces are removed and case is normalized for storage, display, and comparison.
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        checkArgument(isValidTagName(tagName), MESSAGE_CONSTRAINTS);
        this.tagName = tagName.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns true if a given string is a valid tag name.
     */
    public static boolean isValidTagName(String test) {
        String trimmed = test.trim();
        return !trimmed.isEmpty() && trimmed.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Tag otherTag)) {
            return false;
        }

        return tagName.equals(otherTag.tagName);
    }

    @Override
    public int hashCode() {
        return tagName.hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
