package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

public class LessonSlotTest {

    @Test
    public void constructor_nullValue_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonSlot(null, LocalTime.NOON));
        assertThrows(NullPointerException.class, () -> new LessonSlot(DayOfWeek.MONDAY, null));
    }

    @Test
    public void equals_andToString_useDayAndStartTime() {
        LessonSlot slot = new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(8, 5));
        assertTrue(slot.equals(slot));
        assertEquals(slot, new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(8, 5)));
        assertEquals(slot.hashCode(), new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(8, 5)).hashCode());
        assertNotEquals(slot, new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(8, 6)));
        assertNotEquals(slot, new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(8, 5)));
        assertNotEquals(slot, "Fri 08:05");
        assertNotEquals(slot, null);
        assertEquals("Fri 08:05", slot.toString());
    }
}
