package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult.ListPresentation;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.sort.LessonRow;
import seedu.address.model.sort.SortOrder;
import seedu.address.model.sort.SortOrder.Criterion;
import seedu.address.model.sort.SortOrder.Direction;
import seedu.address.model.sort.SortOrder.Field;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises sorting through real commands and storage, using a fixed Thursday afternoon. */
public class SortIntegrationTest {
    @TempDir
    public Path folder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(new AddressBook(), new UserPrefs(),
                Clock.fixed(Instant.parse("2026-10-22T14:00:00Z"), ZoneOffset.UTC));
        storage = new JsonAddressBookStorage(folder.resolve("addressbook.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
        logic.execute("add n/Amy Bee p/91234567 a/Block 1 d/10-Aug o/2"
                + " s/Maths day/MON time/08:00 s/Physics day/FRI time/09:00");
        logic.execute("add n/Bob Choo p/81234567 a/Block 2 d/01-Jan o/10"
                + " s/Maths day/THU time/15:00");
    }

    @Test
    public void execute_sortQueryReset_changesDisplayButNotStoredProfilesOrTheirOrder() throws Exception {
        List<Person> stored = List.copyOf(model.getAddressBook().getPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());

        assertEquals("Sorting: outstanding amount descending, name ascending.",
                logic.execute("sort o/desc n/asc").getFeedbackToUser());
        assertEquals(List.of(stored.get(1), stored.get(0)), model.getFilteredPersonList());
        assertEquals(stored, storage.readAddressBook().orElseThrow().getPersonList());
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        SortOrder order = logic.getSortOrder();
        logic.execute("sort");
        assertSame(order, logic.getSortOrder());

        assertEquals(SortCommand.MESSAGE_DEFAULT, logic.execute("sort reset").getFeedbackToUser());
        assertEquals(stored, model.getFilteredPersonList());
    }

    @Test
    public void execute_sortRequests_refreshCurrentPresentationWhileQueryLeavesItUntouched() throws Exception {
        assertEquals(ListPresentation.REFRESH, logic.execute("sort n/asc").getListPresentation());
        assertEquals(ListPresentation.UNCHANGED, logic.execute("sort").getListPresentation());
        assertEquals(ListPresentation.REFRESH, logic.execute("sort reset").getListPresentation());
    }

    @Test
    public void execute_filterAndReset_keepEachOthersState() throws Exception {
        logic.execute("find Bob");
        logic.execute("sort n/desc");
        assertEquals(List.of("Bob Choo"), names());
        logic.execute("sort reset");
        assertEquals(List.of("Bob Choo"), names());
        logic.execute("sort n/desc");
        logic.execute("list");
        assertEquals(List.of("Bob Choo", "Amy Bee"), names());
        logic.execute("archive s/Bob Choo");
        logic.execute("list -archived");
        logic.execute("sort n/asc");
        assertEquals(List.of("Bob Choo"), names());
        assertTrue(model.getFilteredPersonList().getFirst().isArchived());
    }

    @Test
    public void execute_viewEditDelete_targetThePrintedSortedStudentIndex() throws Exception {
        logic.execute("sort n/desc");
        assertTrue(logic.execute("view 1").getFeedbackToUser().startsWith("Name: Bob Choo\n"));
        logic.execute("edit 1 n/Cara Dee");
        assertEquals(List.of("Cara Dee", "Amy Bee"), names());
        logic.execute("delete 1");
        assertEquals(List.of("Amy Bee"), names());
        assertEquals("Amy Bee", storage.readAddressBook().orElseThrow().getPersonList().getFirst().getName().fullName);
    }

    @Test
    public void execute_invalidSort_preservesOrderFilterAndSavedData() throws Exception {
        logic.execute("sort o/desc");
        logic.execute("find Bob");
        SortOrder order = model.getSortOrder();
        List<Person> shown = List.copyOf(model.getFilteredPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        for (String command : List.of("sort n/asc n/desc", "sort n/up", "sort reset o/asc",
                "sort day/asc", "sort slot/asc for/Maths")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertSame(order, model.getSortOrder());
            assertEquals(shown, model.getFilteredPersonList());
            assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_lessonRows_useUpcomingOccurrencesAndShareOneProfile() throws Exception {
        logic.execute("sort slot/asc");
        List<LessonRow> rows = logic.getLessonRows();
        assertEquals(List.of("Maths", "Physics", "Maths"), rows.stream().map(row -> row.subject().value).toList());
        assertEquals(List.of("Bob Choo", "Amy Bee", "Amy Bee"),
                rows.stream().map(row -> row.person().getName().fullName).toList());
        assertSame(rows.get(1).person(), rows.get(2).person());
        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals(2, storage.readAddressBook().orElseThrow().getPersonList().size());
        assertThrows(UnsupportedOperationException.class, () -> rows.clear());
        logic.execute("delete 1"); // Amy's shared student index is 1, even though Bob's lesson is the first row.
        assertEquals(List.of("Bob Choo"), names());
        assertEquals(1, logic.getLessonRows().size());
    }

    @Test
    public void execute_newStudentsFollowActiveSortAndEmptyListsAcceptCriteria() throws Exception {
        logic.execute("clear");
        logic.execute("sort n/desc");
        logic.execute("add n/Amy Bee p/91234567 a/Block 1 d/10-Aug s/Maths day/MON time/08:00");
        logic.execute("add n/Zara Doe p/81234567 a/Block 2 d/01-Jan s/Maths day/FRI time/09:00");
        assertEquals(List.of("Zara Doe", "Amy Bee"), names());
        assertEquals(List.of("Amy Bee", "Zara Doe"), model.getAddressBook().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }

    @Test
    public void execute_equalCriteriaAfterAnEarlierSort_restoreStoredTieOrder() throws Exception {
        logic.execute("edit 2 o/2");
        logic.execute("sort n/desc");
        logic.execute("sort o/asc");
        assertEquals(List.of("Amy Bee", "Bob Choo"), names());
    }

    private List<String> names() {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }

    @Test
    public void execute_upcomingOrder_changesOnlyWhenReferenceIsRefreshed() {
        AdjustableClock clock = new AdjustableClock();
        Model clocked = new ModelManager(new AddressBook(), new UserPrefs(), clock);
        Subject today = new Subject("Maths", new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(15, 0)));
        Subject tomorrow = new Subject("Physics", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        clocked.addPerson(new PersonBuilder().withScheduledSubjects(today, tomorrow).build());
        clocked.setSortOrder(new SortOrder(List.of(new Criterion(Field.LESSON_SLOT, Direction.ASCENDING))));
        assertEquals(today, clocked.getLessonRows().getFirst().subject());

        clock.current = Instant.parse("2026-10-22T16:00:00Z");
        assertEquals(today, clocked.getLessonRows().getFirst().subject(), "No background timer changes indexes");
        clocked.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        assertEquals(tomorrow, clocked.getLessonRows().getFirst().subject(), "A List refresh captures a new time");
    }

    /** Supplies changing time without sleeping or changing the computer's clock. */
    private static class AdjustableClock extends Clock {
        private Instant current = Instant.parse("2026-10-22T14:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(current, zone);
        }

        @Override
        public Instant instant() {
            return current;
        }
    }
}
