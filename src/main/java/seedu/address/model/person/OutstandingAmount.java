package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A non-negative SGD balance with at most two input decimal places.
 * Empty input represents zero; stored values always have two decimal places.
 */
public final class OutstandingAmount {
    public static final String MESSAGE_CONSTRAINTS =
            "Outstanding amount must be more than or equal to 0, with at most 2 decimal places.";
    public static final String VALIDATION_REGEX = "[0-9]+(?:\\.[0-9]{1,2})?";
    public static final OutstandingAmount ZERO = new OutstandingAmount("0");

    public final BigDecimal value;

    /**
     * Creates a validated amount without floating-point rounding.
     */
    public OutstandingAmount(String amount) {
        requireNonNull(amount);
        String trimmed = amount.trim();
        checkArgument(isValidOutstandingAmount(trimmed), MESSAGE_CONSTRAINTS);
        value = new BigDecimal(trimmed.isEmpty() ? "0" : trimmed).setScale(2, RoundingMode.UNNECESSARY);
    }

    /**
     * Returns whether the trimmed input is blank or a non-negative decimal amount.
     */
    public static boolean isValidOutstandingAmount(String test) {
        requireNonNull(test);
        return test.isEmpty() || test.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof OutstandingAmount amount && value.equals(amount.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}
