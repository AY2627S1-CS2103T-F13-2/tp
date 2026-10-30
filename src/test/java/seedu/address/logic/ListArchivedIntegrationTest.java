package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks archived visibility, displayed indexes, and persistence through real commands. */
public class ListArchivedIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json"));
        logic = createLogic(model);
        logic.execute("add n/Amy Bee p/11111111 a/Block 1 d/10-Aug"
                + " s/Maths day/MON time/09:00 t/A-Level");
        logic.execute("add n/Bob Choo p/22222222 a/Block 2 d/10-Aug"
                + " s/Maths day/MON time/09:00 t/A-Level o/20");
        logic.execute("add n/Cara Dee p/33333333 a/Block 3 d/10-Aug"
                + " s/Physics day/MON time/09:00");
    }

    @Test
    public void execute_archivedList_replacesPreviousFiltersAndPreservesData() throws Exception {
        logic.execute("archive 2");
        List<Person> stored = List.copyOf(model.getAddressBook().getPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        for (String previous : List.of("find Nobody", "list s/Physics", "list t/A-Level", "list")) {
            logic.execute(previous);
            assertEquals("Listed 1 contact.", logic.execute("list -archived").getFeedbackToUser());
            assertEquals(List.of(stored.get(1)), model.getFilteredPersonList());
            assertTrue(logic.execute("view 1").getFeedbackToUser().startsWith("Name: Bob Choo\n"));
        }
        assertEquals(stored, storage.readAddressBook().orElseThrow().getPersonList());
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_activeOptionsAfterArchivedList_excludeArchivedStudents() throws Exception {
        logic.execute("archive 2");
        List<Person> stored = List.copyOf(model.getAddressBook().getPersonList());
        logic.execute("list -archived");
        logic.execute("list");
        assertEquals(List.of(stored.get(0), stored.get(2)), model.getFilteredPersonList());
        for (String command : List.of("list s/Maths", "list t/A-Level")) {
            logic.execute("list -archived");
            assertEquals("Listed 1 contact.", logic.execute(command).getFeedbackToUser());
            assertEquals(List.of(stored.get(0)), model.getFilteredPersonList());
        }
        logic.execute("list -archived");
        assertEquals(String.format(ListCommand.MESSAGE_SUMMARY_FORMAT, "Total: 2", "Paid: 2"),
                logic.execute("list -summary").getFeedbackToUser());
    }

    @Test
    public void execute_noArchivedStudents_showsSpecificEmptyFeedback() throws Exception {
        assertEquals(ListCommand.MESSAGE_NO_ARCHIVED_CONTACTS,
                logic.execute("list -archived").getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        logic.execute("clear confirm");
        assertEquals(ListCommand.MESSAGE_NO_ARCHIVED_CONTACTS,
                logic.execute("list -archived").getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_archivedOnlyBook_listsEachStudentOnceInStoredOrder() throws Exception {
        // Each archive hides that student, so the next active student moves up to index 1.
        for (int i = 0; i < 3; i++) {
            logic.execute("archive 1");
        }
        assertEquals("Listed 3 contacts.", logic.execute("list -archived").getFeedbackToUser());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_reloadAndUnarchive_updateArchivedResults() throws Exception {
        logic.execute("archive 2");
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic reloadedLogic = createLogic(reloaded);
        reloadedLogic.execute("list -archived");
        assertEquals("Bob Choo", reloaded.getFilteredPersonList().getFirst().getName().fullName);
        reloadedLogic.execute("unarchive 1");
        assertEquals(ListCommand.MESSAGE_NO_ARCHIVED_CONTACTS,
                reloadedLogic.execute("list -archived").getFeedbackToUser());
        reloadedLogic.execute("list");
        assertEquals(3, reloaded.getFilteredPersonList().size());
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().stream().noneMatch(Person::isArchived));
    }

    @Test
    public void execute_deleteArchivedIndex_targetsDisplayedStudent() throws Exception {
        logic.execute("archive 2");
        logic.execute("list -archived");
        logic.execute("delete 1");
        logic.execute("delete confirm");
        assertEquals(List.of("Amy Bee", "Cara Dee"), storage.readAddressBook().orElseThrow().getPersonList()
                .stream().map(person -> person.getName().fullName).toList());
    }

    @Test
    public void execute_invalidCombinations_preserveFilterAndStoredData() throws Exception {
        logic.execute("archive 2");
        logic.execute("list -archived");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        List<Person> stored = List.copyOf(model.getAddressBook().getPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        for (String command : List.of("list -archived -summary", "list -summary -archived",
                "list -archived s/Maths", "list t/A-Level -archived", "list -archived extra")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(displayed, model.getFilteredPersonList());
            assertEquals(stored, model.getAddressBook().getPersonList());
            assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    private Logic createLogic(Model target) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
