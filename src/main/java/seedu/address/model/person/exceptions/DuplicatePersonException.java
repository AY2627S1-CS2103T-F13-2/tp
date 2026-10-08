package seedu.address.model.person.exceptions;

/**
 * Signals that the operation would produce contacts with identical stored fields.
 */
public class DuplicatePersonException extends RuntimeException {
    public DuplicatePersonException() {
        super("Operation would result in duplicate persons");
    }
}
