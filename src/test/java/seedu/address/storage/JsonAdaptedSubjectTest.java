package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Subject;

public class JsonAdaptedSubjectTest {

    @Test
    public void toModelType_validSubject_success() throws Exception {
        Subject expected = new Subject("Maths",
                new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0)));
        assertEquals(expected, new JsonAdaptedSubject("Maths", "MONDAY", "09:00").toModelType());
        JsonAdaptedSubject stored = new JsonAdaptedSubject(expected);
        assertEquals("Maths", stored.getName());
        assertEquals("MONDAY", stored.getDay());
        assertEquals("09:00", stored.getTime());
    }

    @Test
    public void toModelType_legacySpacedName_normalizesWithoutChangingSlot() throws Exception {
        Subject loaded = new JsonAdaptedSubject("  H2   Maths  ", "MONDAY", "09:00").toModelType();

        assertEquals("H2 Maths", loaded.value);
        assertEquals(new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0)), loaded.getLessonSlot());
        assertEquals("H2 Maths", new JsonAdaptedSubject(loaded).getName());
    }

    @Test
    public void toModelType_invalidOrMissingValues_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedSubject("Invalid!", "MONDAY", "09:00").toModelType());
        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedSubject(null, "MONDAY", "09:00").toModelType());
        assertThrows(IllegalValueException.class, ParserUtil.MESSAGE_INVALID_LESSON_DAY, () ->
                new JsonAdaptedSubject("Maths", "MON", "09:00").toModelType());
        assertThrows(IllegalValueException.class, ParserUtil.MESSAGE_INVALID_LESSON_TIME, () ->
                new JsonAdaptedSubject("Maths", "MONDAY", "9:00").toModelType());
        assertThrows(IllegalValueException.class, ParserUtil.MESSAGE_INVALID_LESSON_DAY, () ->
                new JsonAdaptedSubject("Maths", null, "09:00").toModelType());
        assertThrows(IllegalValueException.class, ParserUtil.MESSAGE_INVALID_LESSON_TIME, () ->
                new JsonAdaptedSubject("Maths", "MONDAY", null).toModelType());
    }
}
