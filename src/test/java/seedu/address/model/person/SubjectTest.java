package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

public class SubjectTest {

    @Test
    public void isValidSubject_lengthAndCharacterBoundaries() {
        assertTrue(Subject.isValidSubject("A"));
        assertTrue(Subject.isValidSubject("A".repeat(50)));
        assertFalse(Subject.isValidSubject("A".repeat(51)));
        assertFalse(Subject.isValidSubject("---"));
        assertFalse(Subject.isValidSubject("Maths!"));
        assertTrue(Subject.isValidSubject("C++"));
        assertTrue(Subject.isValidSubject("Maths & Science"));
        assertTrue(Subject.isValidSubject("Maths (H2)"));
        assertTrue(Subject.isValidSubject("A. Maths"));
        assertTrue(Subject.isValidSubject("O'Level English"));
        assertFalse(Subject.isValidSubject("+&()"));
        assertFalse(Subject.isValidSubject("Maths; Physics"));
        assertFalse(Subject.isValidSubject("Maths, Physics"));
        assertFalse(Subject.isValidSubject("Art/Design"));
    }

    @Test
    public void equals_caseInsensitiveSameSlotAndTypeSafe() {
        LessonSlot mondayMorning = new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0));
        LessonSlot tuesdayMorning = new LessonSlot(DayOfWeek.TUESDAY, LocalTime.of(9, 0));
        Subject maths = new Subject("Maths", mondayMorning);
        assertEquals(maths, new Subject("maths", mondayMorning));
        assertEquals(maths.hashCode(), new Subject("MATHS", mondayMorning).hashCode());
        assertTrue(maths.equals(maths));
        assertFalse(maths.equals(new Subject("Maths", tuesdayMorning)));
        assertFalse(maths.equals(new Subject("Physics", mondayMorning)));
        assertFalse(maths.equals("Maths"));
        assertFalse(maths.equals(null));
    }

    @Test
    public void toString_includesLessonSlot() {
        Subject subject = new Subject("H2 Maths",
                new LessonSlot(DayOfWeek.WEDNESDAY, LocalTime.of(15, 30)));
        assertEquals("H2 Maths (Wed 15:30)", subject.toString());
    }
}
