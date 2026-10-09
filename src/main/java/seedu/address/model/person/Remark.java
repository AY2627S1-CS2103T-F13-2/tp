package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * An optional, immutable note about a person. Empty remarks are allowed.
 */
public class Remark {
    public final String value;

    public Remark(String remark) {
        value = requireNonNull(remark);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Remark otherRemark && value.equals(otherRemark.value);
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
