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
