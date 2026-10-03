package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a person's optional remark.
 */
public class Remark {

    public final String value;
    /** Creates a remark with the given text. */
    public Remark(String remark) {
        requireNonNull(remark);
        value = remark;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof Remark && value.equals(((Remark) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
