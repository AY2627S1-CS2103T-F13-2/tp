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

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks Find through the command parser, model filtering, and real saved data. */
public class FindIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private Path savedPath;
    private List<Person> students;
    private String originalJson;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        savedPath = temporaryFolder.resolve("students.json");
        logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(savedPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("add n/Alice Tan p/91234567 a/Block 1 d/10-Aug s/Maths day/MON time/09:00");
        logic.execute("add n/Bob Lee p/92345678 a/Block 2 d/10-Aug s/English day/MON time/09:00");
        students = List.copyOf(model.getAddressBook().getPersonList());
        originalJson = Files.readString(savedPath);
    }

    @Test
    public void execute_archivedNameMatch_isExcludedAfterReload() throws Exception {
        logic.execute("archive 1");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(savedPath);
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic reloadedLogic = new LogicManager(reloaded, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("reloaded-preferences.json"))));
        String archivedJson = Files.readString(savedPath);

        assertEquals(FindCommand.MESSAGE_NO_MATCHES, reloadedLogic.execute("find Alice").getFeedbackToUser());
        assertTrue(reloadedLogic.getFilteredPersonList().isEmpty());
        assertEquals(FindCommand.MESSAGE_SINGULAR, reloadedLogic.execute("find ALICE bob").getFeedbackToUser());
        assertEquals(List.of(students.get(1)), reloadedLogic.getFilteredPersonList());
        reloadedLogic.execute("list");
        assertEquals(List.of(students.get(1)), reloadedLogic.getFilteredPersonList());
        assertTrue(reloaded.getAddressBook().getPersonList().get(0).isArchived());
        assertEquals(archivedJson, Files.readString(savedPath));
    }

    @Test
    public void execute_afterSubjectOrTagFilter_searchesAllActiveStudents() throws Exception {
        logic.execute("list s/Maths");
        logic.execute("find Bob");
        assertEquals(List.of(students.get(1)), logic.getFilteredPersonList());
        logic.execute("list t/missing");
        logic.execute("find Alice");
        assertEquals(List.of(students.get(0)), logic.getFilteredPersonList());
        assertDataUnchanged();
    }

    @Test
    public void execute_caseInsensitiveWordStarts_matchesAnyKeywordOnce() throws Exception {
        assertEquals("Found 1 contact.", logic.execute("find ALICE alice").getFeedbackToUser());
        assertEquals(List.of(students.get(0)), logic.getFilteredPersonList());
        assertEquals("Found 2 contacts.", logic.execute("find tan LEE").getFeedbackToUser());
        assertEquals(students, logic.getFilteredPersonList());
        assertEquals("Found 1 contact.", logic.execute("find Ali").getFeedbackToUser());
        assertEquals(List.of(students.get(0)), logic.getFilteredPersonList());
        assertEquals(FindCommand.MESSAGE_NO_MATCHES, logic.execute("find lic").getFeedbackToUser());
        assertTrue(logic.getFilteredPersonList().isEmpty());
        assertDataUnchanged();
    }

    @Test
    public void execute_consecutiveFinds_replacePreviousFilter() throws Exception {
        logic.execute("find Alice");
        logic.execute("find Bob");
        assertEquals(List.of(students.get(1)), logic.getFilteredPersonList());
        logic.execute("find Nobody");
        logic.execute("find Alice");
        assertEquals(List.of(students.get(0)), logic.getFilteredPersonList());
        assertDataUnchanged();
    }

    @Test
    public void execute_listAfterFind_restoresAllStudents() throws Exception {
        for (String keyword : List.of("Alice", "Nobody")) {
            logic.execute("find " + keyword);
            logic.execute("list");
            assertEquals(students, logic.getFilteredPersonList());
        }
        assertDataUnchanged();
    }

    @Test
    public void execute_missingKeywords_preservesPreviousResultsAndData() throws Exception {
        logic.execute("find Bob");
        for (String command : List.of("find", "find   ", "find \t")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(List.of(students.get(1)), logic.getFilteredPersonList());
        }
        assertDataUnchanged();
    }

    private void assertDataUnchanged() throws Exception {
        assertEquals(students, model.getAddressBook().getPersonList());
        assertEquals(originalJson, Files.readString(savedPath));
    }
}
