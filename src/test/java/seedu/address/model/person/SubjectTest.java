package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SubjectTest {

    @Test
    public void isValidSubject_lengthAndCharacterBoundaries() {
        assertTrue(Subject.isValidSubject("A"));
        assertTrue(Subject.isValidSubject("A".repeat(50)));
        assertFalse(Subject.isValidSubject("A".repeat(51)));
        assertFalse(Subject.isValidSubject("---"));
        assertFalse(Subject.isValidSubject("Maths!"));
    }

    @Test
    public void equals_caseInsensitiveAndTypeSafe() {
        Subject maths = new Subject("Maths");
        assertEquals(maths, new Subject("maths"));
        assertEquals(maths.hashCode(), new Subject("MATHS").hashCode());
        assertTrue(maths.equals(maths));
        assertFalse(maths.equals(new Subject("Physics")));
        assertFalse(maths.equals("Maths"));
        assertFalse(maths.equals(null));
    }
}
