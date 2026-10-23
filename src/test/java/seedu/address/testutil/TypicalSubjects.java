package seedu.address.testutil;

import java.time.DayOfWeek;
import java.time.LocalTime;

import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Subject;

/** Provides scheduled subjects for tests that do not depend on a specific lesson time. */
public final class TypicalSubjects {

    private static final LessonSlot DEFAULT_LESSON_SLOT =
            new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0));

    private TypicalSubjects() {}

    /** Returns a subject with the default Monday 09:00 lesson slot. */
    public static Subject scheduledSubject(String name) {
        return new Subject(name, DEFAULT_LESSON_SLOT);
    }
}
