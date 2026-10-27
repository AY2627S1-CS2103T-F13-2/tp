package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult.ListPresentation;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
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
    public void execute_sortWithUnwritableStorage_succeedsWhileEditStillAttemptsSaving() throws Exception {
        AtomicInteger writes = new AtomicInteger();
        JsonAddressBookStorage unwritable = new JsonAddressBookStorage(folder.resolve("read-only.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                writes.incrementAndGet();
                throw new AccessDeniedException("read-only.json");
            }
        };
        Logic readOnlyLogic = new LogicManager(model, new StorageManager(unwritable,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
        for (String command : List.of("sort slot/asc", "sort", "sort reset")) {
            readOnlyLogic.execute(command);
        }
        assertThrows(ParseException.class, () -> readOnlyLogic.execute("sort n/up"));
        assertEquals(0, writes.get(), "Sort never attempts an address-book write");
        CommandException error = assertThrows(CommandException.class, () ->
                readOnlyLogic.execute("edit 1 notes/Still saved"));
        assertTrue(error.getMessage().contains("insufficient permissions"));
        assertEquals(1, writes.get(), "Profile mutations keep the existing persistence behaviour");
        readOnlyLogic.execute("sort n/asc");
        assertEquals(1, writes.get(), "The failed save must not leave a command scope active");
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
        logic.execute("delete 1"); // Bob is first in the unique-student order and has one lesson.
        assertEquals(List.of("Amy Bee"), names());
        assertEquals(2, logic.getLessonRows().size());
    }

    @Test
    public void execute_studentRows_useOneRepresentativeAndNaturalCommandIndexes() throws Exception {
        String json = Files.readString(storage.getAddressBookFilePath());
        logic.execute("sort slot/asc");
        assertEquals(List.of("Bob Choo", "Amy Bee"), names());
        assertEquals(List.of("Maths", "Physics"), logic.getStudentRows().stream()
                .map(row -> row.subject().value).toList());
        assertEquals(model.getFilteredPersonList(), logic.getStudentRows().stream().map(LessonRow::person).toList());
        assertTrue(logic.execute("view 1").getFeedbackToUser().startsWith("Name: Bob Choo\n"));

        logic.execute("sort slot/desc");
        assertEquals(List.of("Amy Bee", "Bob Choo"), names());
        assertEquals(List.of("Maths", "Maths"), logic.getStudentRows().stream()
                .map(row -> row.subject().value).toList());
        assertEquals(2, logic.getStudentRows().size());
        assertEquals(3, logic.getLessonRows().size(), "Subject grouping still has all lessons");
        assertThrows(UnsupportedOperationException.class, () -> logic.getStudentRows().clear());
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_editingASchedule_reordersUniqueStudentsAndKeepsFilters() throws Exception {
        logic.execute("sort slot/asc");
        logic.execute("edit 1 s/History day/SAT time/10:00");
        assertEquals(List.of("Amy Bee", "Bob Choo"), names());
        assertEquals(List.of("Physics", "History"), logic.getStudentRows().stream()
                .map(row -> row.subject().value).toList());
        logic.execute("find Bob");
        logic.execute("sort slot/desc");
        assertEquals(List.of("Bob Choo"), names());
        assertEquals(1, logic.getStudentRows().size());
        logic.execute("sort reset");
        assertEquals(List.of("Bob Choo"), names());
        assertEquals("History", storage.readAddressBook().orElseThrow().getPersonList().get(1)
                .getSubjects().iterator().next().value);
    }

    @Test
    public void execute_newStudentsFollowActiveSortAndEmptyListsAcceptCriteria() throws Exception {
        logic.execute("clear confirm");
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
        assertEquals(today, clocked.getStudentRows().getFirst().subject());

        clock.current = Instant.parse("2026-10-22T16:00:00Z");
        assertEquals(today, clocked.getLessonRows().getFirst().subject(), "No background timer changes indexes");
        assertEquals(today, clocked.getStudentRows().getFirst().subject());
        clocked.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        assertEquals(tomorrow, clocked.getLessonRows().getFirst().subject(), "A List refresh captures a new time");
        assertEquals(tomorrow, clocked.getStudentRows().getFirst().subject());
    }

    @Test
    public void execute_editCrossingLessonBoundary_capturesOneTimeForTheWholeCommand() throws Exception {
        AdjustableClock clock = new AdjustableClock();
        Model clocked = new ModelManager(new AddressBook(), new UserPrefs(), clock);
        Subject today = new Subject("Maths", new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(15, 0)));
        Subject tomorrow = new Subject("Physics", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        clocked.addPerson(new PersonBuilder().withScheduledSubjects(today, tomorrow).build());
        Logic clockedLogic = new LogicManager(clocked, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("clock-prefs.json"))));
        clockedLogic.execute("sort slot/asc");
        clock.current = Instant.parse("2026-10-22T14:59:59Z");
        clock.advancesOnRead = true;
        int reads = clock.reads;

        clockedLogic.execute("edit 1 notes/Before the lesson");
        assertEquals(reads + 1, clock.reads, "The profile update and filter reset share one clock reading");
        assertEquals(today, clocked.getStudentRows().getFirst().subject());
        List<LessonRow> rows = clocked.getLessonRows();
        clockedLogic.execute("sort");
        clockedLogic.execute("view 1");
        assertThrows(CommandException.class, () -> clockedLogic.execute("delete 99"));
        assertEquals(reads + 1, clock.reads, "Queries and failed commands do not refresh lesson time");
        assertSame(rows, clocked.getLessonRows());

        clockedLogic.execute("sort slot/asc");
        assertEquals(reads + 2, clock.reads);
        assertEquals(tomorrow, clocked.getStudentRows().getFirst().subject(), "The next command sees the passed start");
    }

    /** Supplies changing time without sleeping or changing the computer's clock. */
    private static class AdjustableClock extends Clock {
        private Instant current = Instant.parse("2026-10-22T14:00:00Z");
        private int reads;
        private boolean advancesOnRead;

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
            Instant result = current;
            reads++;
            if (advancesOnRead) {
                current = current.plusSeconds(2);
            }
            return result;
        }
    }
}
