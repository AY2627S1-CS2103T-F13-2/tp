package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

/** A student's recurring weekly lesson day and start time. */
public final class LessonSlot {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final DayOfWeek day;
    private final LocalTime startTime;

    /** Creates a weekly lesson slot with a day and start time. */
    public LessonSlot(DayOfWeek day, LocalTime startTime) {
        this.day = requireNonNull(day);
        this.startTime = requireNonNull(startTime);
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LessonSlot otherSlot
                && day == otherSlot.day && startTime.equals(otherSlot.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, startTime);
    }

    @Override
    public String toString() {
        return day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + startTime.format(TIME_FORMAT);
    }
}
