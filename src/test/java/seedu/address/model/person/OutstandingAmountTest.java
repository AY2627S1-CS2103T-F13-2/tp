package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class OutstandingAmountTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new OutstandingAmount(null));
    }

    @Test
    public void constructor_invalidAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new OutstandingAmount("-1"));
    }

    @Test
    public void isValidOutstandingAmount() {
        assertThrows(NullPointerException.class, () -> OutstandingAmount.isValidOutstandingAmount(null));
        assertFalse(OutstandingAmount.isValidOutstandingAmount(""));
        assertFalse(OutstandingAmount.isValidOutstandingAmount("-1"));
        assertFalse(OutstandingAmount.isValidOutstandingAmount("+1"));
        assertFalse(OutstandingAmount.isValidOutstandingAmount("1.234"));
        assertFalse(OutstandingAmount.isValidOutstandingAmount("1."));
        assertFalse(OutstandingAmount.isValidOutstandingAmount(".50"));
        assertFalse(OutstandingAmount.isValidOutstandingAmount("1e2"));
        assertFalse(OutstandingAmount.isValidOutstandingAmount("1,000"));
        assertFalse(OutstandingAmount.isValidOutstandingAmount("one"));
        assertTrue(OutstandingAmount.isValidOutstandingAmount("0"));
        assertTrue(OutstandingAmount.isValidOutstandingAmount("0.00"));
        assertTrue(OutstandingAmount.isValidOutstandingAmount("20"));
        assertTrue(OutstandingAmount.isValidOutstandingAmount("20.5"));
        assertTrue(OutstandingAmount.isValidOutstandingAmount("20.50"));
        assertTrue(OutstandingAmount.isValidOutstandingAmount("000.50"));
    }

    @Test
    public void equivalentAmounts_areEqualAndHaveSameHashCode() {
        OutstandingAmount integerZero = new OutstandingAmount("0");
        OutstandingAmount decimalZero = new OutstandingAmount("0.00");
        assertEquals(integerZero, decimalZero);
        assertEquals(integerZero.hashCode(), decimalZero.hashCode());
    }

    @Test
    public void formatting() {
        OutstandingAmount amount = new OutstandingAmount("020.50");
        assertEquals("20.5", amount.toPlainString());
        assertEquals("SGD 20.50", amount.toCurrencyString());
    }
    @Test
    public void addAndSubtract_returnsNormalizedAmounts() {
        OutstandingAmount amount = new OutstandingAmount("20.50");
        assertEquals("30.5", amount.add(new OutstandingAmount("10")).toPlainString());
        assertEquals("10.5", amount.subtract(new OutstandingAmount("10")).toPlainString());
    }

    @Test
    public void arithmetic_invalidArgumentsAndNegativeResult_throws() {
        OutstandingAmount amount = new OutstandingAmount("10");
        assertThrows(NullPointerException.class, () -> amount.add(null));
        assertThrows(NullPointerException.class, () -> amount.subtract(null));
        assertThrows(NullPointerException.class, () -> amount.isGreaterThan(null));
        assertThrows(IllegalArgumentException.class, () -> amount.subtract(new OutstandingAmount("10.01")));
    }

    @Test
    public void comparisonAndRepresentation_coverOtherTypes() {
        OutstandingAmount amount = new OutstandingAmount("10");
        assertTrue(amount.equals(amount));
        assertFalse(amount.equals(new OutstandingAmount("11")));
        assertFalse(amount.equals("10"));
        assertFalse(amount.equals(null));
        assertEquals("10", amount.toString());
    }
}
