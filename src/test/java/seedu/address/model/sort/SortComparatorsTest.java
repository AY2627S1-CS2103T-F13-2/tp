package seedu.address.model.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.sort.SortOrder.Criterion;
import seedu.address.model.sort.SortOrder.Direction;
import seedu.address.model.sort.SortOrder.Field;
import seedu.address.testutil.PersonBuilder;

public class SortComparatorsTest {
    private static final LocalDateTime THURSDAY = LocalDateTime.of(2026, 10, 22, 14, 0);

    @Test
    public void students_mixedPriorities_comparesAmountsNumericallyAndNamesIgnoringCase() {
        Person amy = person("amy", "10", "10-Aug");
        Person bob = person("Bob", "10.00", "10-Aug");
        Person cara = person("Cara", "2", "10-Aug");
        SortOrder order = order(new Criterion(Field.OUTSTANDING_AMOUNT, Direction.DESCENDING),
                new Criterion(Field.NAME, Direction.ASCENDING));
        assertEquals(List.of(amy, bob, cara), sorted(List.of(cara, bob, amy), SortComparators.students(order)));
    }

    @Test
    public void students_dates_useCalendarOrderAndPutInvalidValuesLastInBothDirections() {
        Person january = person("January", "0", "01-Jan");
        Person leapDay = person("Leap", "0", "29-fEb");
        Person december = person("December", "0", "31-Dec");
        Person impossible = person("Impossible", "0", "31-Feb");
        Person unknown = person("Unknown", "0", "99-Abc");
        List<Person> original = List.of(impossible, december, unknown, leapDay, january);
        SortOrder ascending = order(new Criterion(Field.START_DATE, Direction.ASCENDING));
        SortOrder descending = order(new Criterion(Field.START_DATE, Direction.DESCENDING));
        assertEquals(List.of(january, leapDay, december, impossible, unknown),
                sorted(original, SortComparators.students(ascending)));
        assertEquals(List.of(december, leapDay, january, impossible, unknown),
                sorted(original, SortComparators.students(descending)));
        assertEquals("31-Feb", impossible.getStartDate().value);
    }

    @Test
    public void students_equalKeys_preserveInputOrderWithoutHiddenNameTieBreaker() {
        Person bob = person("Bob", "2", "10-Aug");
        Person amy = person("Amy", "2.00", "10-Aug");
        List<Person> input = List.of(bob, amy);
        assertEquals(input, sorted(input,
                SortComparators.students(order(new Criterion(Field.OUTSTANDING_AMOUNT, Direction.ASCENDING)))));
        assertEquals(input, sorted(input, SortComparators.students(SortOrder.NONE)));
    }

    @Test
    public void nextOccurrence_todayFutureAndPast_wrapsOnlyAfterTheStart() {
        LessonSlot past = new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(13, 0));
        LessonSlot future = new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(15, 0));
        assertEquals(LocalDateTime.of(2026, 10, 29, 13, 0), SortComparators.nextOccurrence(past, THURSDAY));
        assertEquals(LocalDateTime.of(2026, 10, 22, 15, 0), SortComparators.nextOccurrence(future, THURSDAY));
        assertEquals(THURSDAY, SortComparators.nextOccurrence(
                new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(14, 0)), THURSDAY));
        assertEquals(LocalDateTime.of(2026, 10, 29, 14, 0), SortComparators.nextOccurrence(
                new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(14, 0)), THURSDAY.plusNanos(1)));
    }

    @Test
    public void nextOccurrence_sundayToMondayAndYearBoundary_usesTheNextCalendarOccurrence() {
        assertEquals(LocalDateTime.of(2027, 1, 4, 0, 0), SortComparators.nextOccurrence(
                new LessonSlot(DayOfWeek.MONDAY, LocalTime.MIDNIGHT), LocalDateTime.of(2026, 12, 31, 23, 59)));
        assertEquals(LocalDateTime.of(2026, 10, 26, 0, 0), SortComparators.nextOccurrence(
                new LessonSlot(DayOfWeek.MONDAY, LocalTime.MIDNIGHT), LocalDateTime.of(2026, 10, 25, 23, 59)));
    }

    @Test
    public void lessons_ordersEachStudentsSlotsAndKeepsMissingLessonsLast() {
        Subject monday = new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(8, 0)));
        Subject friday = new Subject("Physics", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        Person amy = new PersonBuilder().withScheduledSubjects(monday, friday).build();
        Person bob = new PersonBuilder().withName("Bob").build();
        LessonRow maths = new LessonRow(amy, monday);
        LessonRow physics = new LessonRow(amy, friday);
        LessonRow noLesson = new LessonRow(bob, null);
        List<LessonRow> original = List.of(noLesson, maths, physics);
        assertEquals(List.of(physics, maths, noLesson), sorted(original, SortComparators.lessons(
                order(new Criterion(Field.LESSON_SLOT, Direction.ASCENDING)), THURSDAY)));
        assertEquals(List.of(maths, physics, noLesson), sorted(original, SortComparators.lessons(
                order(new Criterion(Field.LESSON_SLOT, Direction.DESCENDING)), THURSDAY)));
        assertEquals(2, amy.getSubjects().size());
    }

    @Test
    public void lessons_priorityBeforeSlotAndTieAfterSlot_areRespected() {
        Subject friday = new Subject("Maths", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        Person amy = new PersonBuilder().withName("Amy").withOutstandingAmount("2")
                .withScheduledSubjects(friday).build();
        Person bob = new PersonBuilder().withName("Bob").withOutstandingAmount("10")
                .withScheduledSubjects(friday).build();
        LessonRow first = new LessonRow(amy, friday);
        LessonRow second = new LessonRow(bob, friday);
        assertEquals(List.of(second, first), sorted(List.of(first, second), SortComparators.lessons(
                order(new Criterion(Field.OUTSTANDING_AMOUNT, Direction.DESCENDING),
                        new Criterion(Field.LESSON_SLOT, Direction.ASCENDING)), THURSDAY)));
        assertEquals(List.of(first, second), sorted(List.of(second, first), SortComparators.lessons(
                order(new Criterion(Field.LESSON_SLOT, Direction.ASCENDING),
                        new Criterion(Field.NAME, Direction.ASCENDING)), THURSDAY)));
    }

    @Test
    public void lessonRow_rejectsNullPersonAndLessonFromAnotherProfile() {
        Person amy = new PersonBuilder().build();
        Subject maths = new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.NOON));
        assertThrows(NullPointerException.class, () -> new LessonRow(null, null));
        assertThrows(IllegalArgumentException.class, () -> new LessonRow(amy, maths));
    }

    @Test
    public void representativeLesson_ascendingAndDescending_chooseOppositeUpcomingSlots() {
        Subject monday = new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0)));
        Subject friday = new Subject("Physics", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(10, 0)));
        Person student = new PersonBuilder().withScheduledSubjects(monday, friday).build();
        assertEquals(friday, SortComparators.representativeLesson(student,
                order(new Criterion(Field.LESSON_SLOT, Direction.ASCENDING)), THURSDAY).subject());
        assertEquals(monday, SortComparators.representativeLesson(student,
                order(new Criterion(Field.LESSON_SLOT, Direction.DESCENDING)), THURSDAY).subject());
        assertEquals(List.of(monday, friday), new ArrayList<>(student.getSubjects()));
    }

    @Test
    public void representativeLesson_equalSlots_keepTheFirstSubjectAndAllowNoSubjects() {
        LessonSlot friday = new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(9, 0));
        Subject physics = new Subject("Physics", friday);
        Subject maths = new Subject("Maths", friday);
        Person student = new PersonBuilder().withScheduledSubjects(physics, maths).build();
        for (Direction direction : Direction.values()) {
            SortOrder order = order(new Criterion(Field.LESSON_SLOT, direction));
            assertEquals(physics, SortComparators.representativeLesson(student, order, THURSDAY).subject());
            Person subjectless = new PersonBuilder().build();
            assertEquals(new LessonRow(subjectless, null),
                    SortComparators.representativeLesson(subjectless, order, THURSDAY));
        }
    }

    @Test
    public void students_representativeSlots_respectPriorityTiesAndMissingValues() {
        Subject monday = new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0)));
        Subject friday = new Subject("Physics", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(10, 0)));
        Subject thursday = new Subject("English", new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(15, 0)));
        Person amy = new PersonBuilder().withName("Amy").withOutstandingAmount("10")
                .withScheduledSubjects(monday, thursday).build();
        Person bob = new PersonBuilder().withName("Bob").withOutstandingAmount("20")
                .withScheduledSubjects(friday).build();
        Person zed = new PersonBuilder().withName("Zed").withOutstandingAmount("0").build();
        List<Person> original = List.of(zed, bob, amy);
        assertEquals(List.of(amy, bob, zed), sorted(original, SortComparators.students(
                order(new Criterion(Field.LESSON_SLOT, Direction.ASCENDING)), THURSDAY)));
        assertEquals(List.of(amy, bob, zed), sorted(original, SortComparators.students(
                order(new Criterion(Field.LESSON_SLOT, Direction.DESCENDING)), THURSDAY)));
        assertEquals(List.of(bob, amy, zed), sorted(original, SortComparators.students(
                order(new Criterion(Field.OUTSTANDING_AMOUNT, Direction.DESCENDING),
                        new Criterion(Field.LESSON_SLOT, Direction.ASCENDING)), THURSDAY)));
    }

    private static Person person(String name, String amount, String date) {
        return new PersonBuilder().withName(name).withOutstandingAmount(amount).withStartDate(date).build();
    }

    private static SortOrder order(Criterion... criteria) {
        return new SortOrder(List.of(criteria));
    }

    private static <T> List<T> sorted(List<T> input, java.util.Comparator<T> comparator) {
        List<T> result = new ArrayList<>(input);
        result.sort(comparator);
        return result;
    }
}
