package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.sort.LessonRow;
import seedu.address.model.sort.SortOrder;
import seedu.address.model.sort.SortOrder.Criterion;
import seedu.address.model.sort.SortOrder.Direction;
import seedu.address.model.sort.SortOrder.Field;
import seedu.address.testutil.PersonBuilder;

/** Tests cached snapshots and stored-position updates through the public model operations. */
public class ModelSortingRefreshTest {
    private static final Subject MATHS = new Subject("Maths",
            new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
    private Person amy;
    private Person bob;
    private ModelManager model;

    @BeforeEach
    public void setUp() {
        amy = new PersonBuilder().withName("Amy Bee").withOutstandingAmount("0")
                .withScheduledSubjects(MATHS).build();
        bob = new PersonBuilder(amy).withName("Bob Choo").build();
        AddressBook data = new AddressBook();
        data.addPerson(amy);
        data.addPerson(bob);
        model = new ModelManager(data, new UserPrefs(),
                Clock.fixed(Instant.parse("2026-10-22T14:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    public void snapshots_repeatedReads_reuseImmutableRows() {
        List<LessonRow> students = model.getStudentRows();
        List<LessonRow> lessons = model.getLessonRows();
        assertSame(students, model.getStudentRows());
        assertSame(lessons, model.getLessonRows());
        model.setGuiSettings(new GuiSettings(800, 600, 0, 0));
        assertSame(students, model.getStudentRows());
        assertSame(lessons, model.getLessonRows());
    }

    @Test
    public void snapshots_sortAndFilterChanges_invalidateWithoutChangingEarlierSnapshots() {
        List<LessonRow> originalStudents = model.getStudentRows();
        List<LessonRow> originalLessons = model.getLessonRows();
        model.setSortOrder(order(Field.NAME, Direction.DESCENDING));
        assertNotSame(originalStudents, model.getStudentRows());
        assertNotSame(originalLessons, model.getLessonRows());
        assertEquals(List.of(bob, amy), model.getFilteredPersonList());
        assertEquals(List.of(amy, bob), originalStudents.stream().map(LessonRow::person).toList());

        List<LessonRow> sorted = model.getLessonRows();
        model.updateFilteredPersonList(person -> person.equals(amy));
        assertNotSame(sorted, model.getLessonRows());
        assertEquals(List.of(amy), model.getStudentRows().stream().map(LessonRow::person).toList());
        model.setSortOrder(SortOrder.NONE);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        assertEquals(List.of(amy, bob), model.getFilteredPersonList());
    }

    @Test
    public void snapshots_profileMutations_invalidateAndKeepStoredTieOrder() {
        model.setSortOrder(order(Field.OUTSTANDING_AMOUNT, Direction.ASCENDING));
        List<LessonRow> old = model.getLessonRows();
        Person editedAmy = new PersonBuilder(amy).withPhone("87654321").build();
        model.setPerson(amy, editedAmy);
        assertNotSame(old, model.getLessonRows());
        assertEquals(List.of(editedAmy, bob), model.getFilteredPersonList());
        assertEquals(amy, old.getFirst().person());

        Person cara = new PersonBuilder(bob).withName("Cara Lim").build();
        model.addPerson(cara);
        assertEquals(List.of(editedAmy, bob, cara), model.getStudentRows().stream().map(LessonRow::person).toList());
        model.deletePerson(bob);
        assertEquals(List.of(editedAmy, cara), model.getLessonRows().stream().map(LessonRow::person).toList());

        AddressBook reversed = new AddressBook();
        reversed.addPerson(cara);
        reversed.addPerson(editedAmy);
        model.setAddressBook(reversed);
        assertEquals(List.of(cara, editedAmy), model.getStudentRows().stream().map(LessonRow::person).toList());
        assertEquals(List.of(cara, editedAmy), model.getLessonRows().stream().map(LessonRow::person).toList());
    }

    @Test
    public void clockZone_nonUtcCivilTime_controlsTheNextWeeklyOccurrence() {
        Subject today = new Subject("Maths", new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(15, 0)));
        Subject tomorrow = new Subject("Physics", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        AddressBook data = new AddressBook();
        data.addPerson(new PersonBuilder(amy).withScheduledSubjects(today, tomorrow).build());
        Instant instant = Instant.parse("2026-10-22T08:00:00Z");
        ModelManager singapore = new ModelManager(data, new UserPrefs(),
                Clock.fixed(instant, ZoneId.of("Asia/Singapore")));
        ModelManager utc = new ModelManager(data, new UserPrefs(), Clock.fixed(instant, ZoneOffset.UTC));
        singapore.setSortOrder(order(Field.LESSON_SLOT, Direction.ASCENDING));
        utc.setSortOrder(order(Field.LESSON_SLOT, Direction.ASCENDING));
        assertEquals(tomorrow, singapore.getStudentRows().getFirst().subject(), "Thursday 15:00 has passed in SGT");
        assertEquals(today, utc.getStudentRows().getFirst().subject(), "It is still Thursday morning in UTC");
        assertEquals(tomorrow, singapore.getLessonRows().getFirst().subject());
    }

    private static SortOrder order(Field field, Direction direction) {
        return new SortOrder(List.of(new Criterion(field, direction)));
    }
}
