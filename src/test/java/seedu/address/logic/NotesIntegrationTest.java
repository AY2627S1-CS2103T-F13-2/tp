package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CliSyntax;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks Notes across Add, Edit preservation, View, and real JSON storage.
 */
public class NotesIntegrationTest {

    private static final String ADD_STUDENT =
            "add n/Amy Bee p/91234567 a/Block 1 d/10-Aug o/20 s/Maths day/MON time/15:30";

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage addressStorage;
    private StorageManager storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        addressStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        storage = new StorageManager(addressStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_addEditSaveReloadView_preservesNotes() throws Exception {
        String notes = "Revise  fractions, \"graphs\" & 中文!";
        logic.execute(ADD_STUDENT + " notes/  " + notes + "  ");
        Person added = model.getFilteredPersonList().get(0);
        assertEquals(new Notes(notes), added.getNotes());

        logic.execute("edit 1 p/87654321");
        Person expected = new PersonBuilder(added).withPhone("87654321").build();
        ReadOnlyAddressBook reloaded = addressStorage.readAddressBook().orElseThrow();
        assertEquals(List.of(expected), reloaded.getPersonList());

        Logic reloadedLogic = new LogicManager(new ModelManager(reloaded, new UserPrefs()), storage);
        assertEquals("Name: Amy Bee\nPhone: 87654321\nAddress: Block 1\nStart date: 10-Aug"
                + "\nOutstanding amount: SGD 20.00\nSubjects: Maths (Mon 15:30)"
                + "\nTags: Not specified\nNotes: " + notes,
                reloadedLogic.execute("view 1").getFeedbackToUser());
    }

    @Test
    public void execute_addEditReload_preservesTagsAndNotes() throws Exception {
        logic.execute(ADD_STUDENT + " t/A-Level t/Needs revision notes/Revise fractions");
        Person added = model.getFilteredPersonList().get(0);
        assertEquals(new Notes("Revise fractions"), added.getNotes());
        assertEquals(Set.of(new Subject("Maths",
                        new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(15, 30)))),
                added.getSubjects());
        assertEquals(Set.of(new Tag("a-level"), new Tag("needs revision")), added.getTags());

        logic.execute("edit 1 p/87654321");
        Person expected = new PersonBuilder(added).withPhone("87654321").build();
        assertEquals(List.of(expected), addressStorage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_notesOmittedOrBlank_savesEmptyNotes() throws Exception {
        for (String suffix : new String[] {"", " notes/", " notes/   "}) {
            logic.execute("clear confirm");
            logic.execute(ADD_STUDENT + suffix);
            assertEquals(Notes.EMPTY, model.getFilteredPersonList().get(0).getNotes());
            assertEquals(Notes.EMPTY, addressStorage.readAddressBook().orElseThrow()
                    .getPersonList().get(0).getNotes());
        }
    }

    @Test
    public void execute_duplicateNotesPrefixes_preservesDataAndFilter() throws Exception {
        logic.execute(ADD_STUDENT + " notes/Original note");
        Person original = model.getFilteredPersonList().get(0);
        logic.execute("find Nobody");
        Path savedPath = temporaryFolder.resolve("addressbook.json");
        String originalJson = Files.readString(savedPath);

        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NOTES);
        String invalidCommand = ADD_STUDENT + " notes/One notes/Two";
        assertThrows(ParseException.class, expectedMessage, () -> logic.execute(invalidCommand));

        assertEquals(List.of(original), model.getAddressBook().getPersonList());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(originalJson, Files.readString(savedPath));
    }

    @Test
    public void execute_differentNotesForSameStudent_rejectsDuplicate() throws Exception {
        logic.execute(ADD_STUDENT + " notes/Original note");
        Person original = model.getFilteredPersonList().get(0);
        Path savedPath = temporaryFolder.resolve("addressbook.json");
        String originalJson = Files.readString(savedPath);

        String command = ADD_STUDENT + " notes/Different note";
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> logic.execute(command));

        assertEquals(List.of(original), model.getAddressBook().getPersonList());
        assertEquals(originalJson, Files.readString(savedPath));
    }

    @Test
    public void read_legacyJsonWithoutNotes_defaultsToEmpty() throws Exception {
        Path savedPath = temporaryFolder.resolve("addressbook.json");
        Files.writeString(savedPath, """
                {"persons":[{"name":"Amy Bee","phone":"91234567","address":"Block 1",
                "startDate":"10-Aug","outstandingAmount":"20"}]}
                """);
        ReadOnlyAddressBook legacy = addressStorage.readAddressBook().orElseThrow();
        assertEquals(Notes.EMPTY, legacy.getPersonList().get(0).getNotes());
        addressStorage.saveAddressBook(legacy);
        assertEquals(legacy, addressStorage.readAddressBook().orElseThrow());
    }
}
