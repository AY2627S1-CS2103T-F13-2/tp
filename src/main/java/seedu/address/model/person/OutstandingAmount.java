package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.math.BigDecimal;

/**
 * Represents a student's outstanding tuition amount in Singapore dollars.
 */
public class OutstandingAmount {

    public static final String MESSAGE_CONSTRAINTS = "Outstanding amount must be more than or equal to 0.";
    public static final String VALIDATION_REGEX = "\\d+(?:\\.\\d{1,2})?";
    public static final OutstandingAmount ZERO = new OutstandingAmount("0");

    private final BigDecimal value;

    /**
     * Creates an {@code OutstandingAmount} from a nonnegative decimal with at most two decimal places.
     */
    public OutstandingAmount(String outstandingAmount) {
        requireNonNull(outstandingAmount);
        checkArgument(isValidOutstandingAmount(outstandingAmount), MESSAGE_CONSTRAINTS);
        value = normalize(new BigDecimal(outstandingAmount));
    }

    public static boolean isValidOutstandingAmount(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns the canonical locale-independent representation used for storage.
     */
    public String toPlainString() {
        return value.toPlainString();
    }

    /**
     * Returns the amount formatted as SGD with two decimal places.
     */
    public String toCurrencyString() {
        return "SGD " + value.setScale(2).toPlainString();
    }

    /** Returns the sum of this amount and {@code other}. */
    public OutstandingAmount add(OutstandingAmount other) {
        requireNonNull(other);
        return new OutstandingAmount(value.add(other.value).toPlainString());
    }

    /** Returns this amount less {@code other}; the result must not be negative. */
    public OutstandingAmount subtract(OutstandingAmount other) {
        requireNonNull(other);
        checkArgument(value.compareTo(other.value) >= 0, MESSAGE_CONSTRAINTS);
        return new OutstandingAmount(value.subtract(other.value).toPlainString());
    }

    /** Returns true if this amount is greater than {@code other}. */
    public boolean isGreaterThan(OutstandingAmount other) {
        requireNonNull(other);
        return value.compareTo(other.value) > 0;
    }

    private static BigDecimal normalize(BigDecimal amount) {
        BigDecimal normalized = amount.stripTrailingZeros();
        return normalized.scale() < 0 ? normalized.setScale(0) : normalized;
    }

    @Override
    public String toString() {
        return toPlainString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof OutstandingAmount otherAmount
                && value.equals(otherAmount.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
