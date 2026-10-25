package seedu.address.model.sort;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Person;
import seedu.address.model.sort.SortOrder.Criterion;
import seedu.address.model.sort.SortOrder.Direction;
import seedu.address.model.sort.SortOrder.Field;

/** Builds comparisons without mutating profiles, filters or stored record order. */
public final class SortComparators {
    private static final DateTimeFormatter START_DATE_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive().appendPattern("dd-MMM").toFormatter(Locale.ENGLISH);

    private SortComparators() {}

    /** Orders unique students by one representative lesson, including the requested slot direction. */
    public static Comparator<Person> students(SortOrder order, LocalDateTime reference) {
        Comparator<LessonRow> comparison = lessons(order, reference);
        return Comparator.comparing(person -> representativeLesson(person, comparison), comparison);
    }

    /**
     * Selects the nearest lesson for ascending slots or the furthest for descending slots.
     * Equal lesson slots keep subject order; students without subjects have a null lesson.
     */
    public static LessonRow representativeLesson(Person person, SortOrder order, LocalDateTime reference) {
        return representativeLesson(person, lessons(order, reference));
    }

    private static LessonRow representativeLesson(Person person, Comparator<LessonRow> comparison) {
        requireNonNull(person);
        return person.getSubjects().stream().map(subject -> new LessonRow(person, subject))
                .min(comparison).orElseGet(() -> new LessonRow(person, null));
    }

    /** Orders lesson rows using one captured local reference time for the entire comparison. */
    public static Comparator<LessonRow> lessons(SortOrder order, LocalDateTime reference) {
        requireNonNull(order);
        requireNonNull(reference);
        Comparator<LessonRow> comparison = (first, second) -> 0;
        for (Criterion criterion : order.criteria()) {
            Comparator<LessonRow> next = criterion.field() == Field.LESSON_SLOT
                    ? values(row -> row.subject() == null ? null
                            : nextOccurrence(row.subject().getLessonSlot(), reference), criterion.direction())
                    : Comparator.comparing(LessonRow::person, studentField(criterion));
            comparison = comparison.thenComparing(next);
        }
        return comparison;
    }

    /**
     * Returns the next weekly start at or after the reference time. A start that has passed occurs next week.
     * Uses local wall-clock time, consistent with the lesson model, which stores no time zone or duration.
     */
    public static LocalDateTime nextOccurrence(LessonSlot slot, LocalDateTime reference) {
        requireNonNull(slot);
        requireNonNull(reference);
        LocalDateTime next = reference.with(TemporalAdjusters.nextOrSame(slot.getDay())).with(slot.getStartTime());
        return next.isBefore(reference) ? next.plusWeeks(1) : next;
    }

    private static Comparator<Person> studentField(Criterion criterion) {
        return switch (criterion.field()) {
            case NAME -> values(person -> person.getName().fullName.toLowerCase(Locale.ROOT), criterion.direction());
            case OUTSTANDING_AMOUNT -> values(person -> new BigDecimal(person.getOutstandingAmount().toPlainString()),
                    criterion.direction());
            case START_DATE -> values(SortComparators::startDate, criterion.direction());
            case LESSON_SLOT -> throw new IllegalArgumentException("Lesson sorting requires a lesson row.");
        };
    }

    private static MonthDay startDate(Person person) {
        try {
            return MonthDay.parse(person.getStartDate().value, START_DATE_FORMAT);
        } catch (DateTimeException exception) {
            // Existing profiles can contain format-valid but impossible dates. Leave their values unchanged.
            return null;
        }
    }

    private static <T, V extends Comparable<? super V>> Comparator<T> values(Function<T, V> key,
            Direction direction) {
        Comparator<V> presentValues = direction == Direction.ASCENDING
                ? Comparator.naturalOrder() : Comparator.reverseOrder();
        return Comparator.comparing(key, Comparator.nullsLast(presentValues));
    }
}
