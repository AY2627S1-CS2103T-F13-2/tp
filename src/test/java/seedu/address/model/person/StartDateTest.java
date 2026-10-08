package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class StartDateTest {

    @Test
    public void constructor_invalidStartDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StartDate("10-Aug-2026"));
    }

    @Test
    public void isValidStartDate() {
        assertThrows(NullPointerException.class, () -> StartDate.isValidStartDate(null));
        assertFalse(StartDate.isValidStartDate(""));
        assertFalse(StartDate.isValidStartDate("1-Aug"));
        assertFalse(StartDate.isValidStartDate("10-08"));
        assertFalse(StartDate.isValidStartDate("10-Aug-2026"));

        assertTrue(StartDate.isValidStartDate("10-Aug"));
        assertTrue(StartDate.isValidStartDate("99-Abc"));
        assertTrue(StartDate.isValidStartDate("10-aUG"));
    }
}
