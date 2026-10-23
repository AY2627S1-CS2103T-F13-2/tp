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
import seedu.address.logic.parser.ListCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests tag filtering through command parsing and real JSON storage. */
public class ListTagsIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = createLogic(model);
        logic.execute("add n/Amy Bee p/11111111 a/Block 1 d/10-Aug"
                + " s/Maths day/MON time/09:00 t/A-Level");
        logic.execute("add n/Bob Choo p/22222222 a/Block 2 d/19-Sep"
                + " s/Physics day/MON time/09:00 t/Needs revision");
        logic.execute("add n/Cara Dee p/33333333 a/Block 3 d/20-Oct"
                + " s/Maths day/MON time/09:00");
    }

    @Test
    public void execute_listTagsAfterFind_searchesAllSavedStudents() throws Exception {
        List<Person> original = List.copyOf(model.getAddressBook().getPersonList());
        logic.execute("find Nobody");
        assertEquals("Listed 2 contacts.", logic.execute("list t/A-LEVEL t/Needs revision").getFeedbackToUser());
        assertEquals(original.subList(0, 2), model.getFilteredPersonList());
        assertEquals(original, storage.readAddressBook().orElseThrow().getPersonList());
        logic.execute("list t/needs revision");
        assertEquals(List.of(original.get(1)), model.getFilteredPersonList());
        assertTrue(logic.execute("view 1").getFeedbackToUser().startsWith("Name: Bob Choo\n"));
        logic.execute("list");
        assertEquals(original, model.getFilteredPersonList());
    }

    @Test
    public void execute_switchBetweenSubjectAndTagFilters_keepsBothAvailable() throws Exception {
        List<Person> original = List.copyOf(model.getAddressBook().getPersonList());
        assertEquals("Listed 2 contacts.", logic.execute("list s/Maths").getFeedbackToUser());
        assertEquals(List.of(original.get(0), original.get(2)), model.getFilteredPersonList());
        assertEquals("Listed 1 contact.", logic.execute("list t/Needs revision").getFeedbackToUser());
        assertEquals(List.of(original.get(1)), model.getFilteredPersonList());
        assertEquals(String.format(ListCommand.MESSAGE_SUMMARY_FORMAT, "Total: 2", "Paid: 2"),
                logic.execute("list -summary s/Maths").getFeedbackToUser());
        assertEquals(List.of(original.get(0), original.get(2)), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidTagFilter_preservesCurrentFilterAndSavedData() throws Exception {
        logic.execute("find Bob");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        Path savedPath = temporaryFolder.resolve("addressbook.json");
        String json = Files.readString(savedPath);
        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () -> logic.execute("list t/"));
        String invalidCommand = "list t/a-level x/value";
        String expectedMessage = ListCommandParser.MESSAGE_INVALID_PARAMETERS;
        assertThrows(ParseException.class, expectedMessage, () -> logic.execute(invalidCommand));
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(json, Files.readString(savedPath));
    }

    @Test
    public void execute_reloadedData_canBeFilteredWithoutChangingProfiles() throws Exception {
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic reloadedLogic = createLogic(reloaded);
        assertEquals(ListCommand.MESSAGE_SINGULAR,
                reloadedLogic.execute("list t/a-level t/A-Level").getFeedbackToUser());
        assertEquals("Amy Bee", reloaded.getFilteredPersonList().get(0).getName().fullName);
        assertEquals(ListCommand.MESSAGE_NO_MATCHES,
                reloadedLogic.execute("list t/missing").getFeedbackToUser());
        assertTrue(reloaded.getFilteredPersonList().isEmpty());
        assertEquals(3, storage.readAddressBook().orElseThrow().getPersonList().size());
    }

    private Logic createLogic(Model target) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

}
