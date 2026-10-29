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

import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Tests notes editing with real storage, filtered indexes, and all other profile fields. */
public class EditNotesIntegrationTest {
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
                + " s/Maths day/MON time/09:00 t/A-Level notes/Amy note");
        logic.execute("add n/Bob Choo p/22222222 a/Block 2 d/19-Sep s/Physics day/MON time/09:00"
                + " o/600 t/Needs revision notes/Bob note");
    }

    @Test
    public void execute_notesReplacementAndClear_surviveReloadAndPreserveOtherFields() throws Exception {
        Person amy = model.getAddressBook().getPersonList().get(0);
        Person bob = model.getAddressBook().getPersonList().get(1);
        String note = "Revise  fractions, \"graphs\" & 中文! See https://example.com";
        logic.execute("find Bob");

        assertEquals("Edited contact: Bob Choo", logic.execute("edit 1 notes/  " + note + "  ").getFeedbackToUser());
        Person updated = new PersonBuilder(bob).withNotes(note).build();
        assertEquals(List.of(updated), model.getFilteredPersonList());
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(amy, updated), reloaded.getAddressBook().getPersonList());
        Logic reloadedLogic = createLogic(reloaded);
        assertTrue(reloadedLogic.execute("view 2").getFeedbackToUser().endsWith("Notes: " + note));
        reloadedLogic.execute("edit 2 notes/");
        Person cleared = new PersonBuilder(bob).withNotes("").build();
        assertEquals(List.of(amy, cleared), storage.readAddressBook().orElseThrow().getPersonList());
        assertTrue(reloadedLogic.execute("view 2").getFeedbackToUser().endsWith("Notes: "));
    }

    @Test
    public void execute_sameNoteAndClearingEmptyNote_areNoOps() throws Exception {
        logic.execute("find Bob");
        Person bob = model.getFilteredPersonList().get(0);
        String json = Files.readString(storage.getAddressBookFilePath());
        assertEquals("No changes made to contact: Bob Choo",
                logic.execute("edit 1 notes/  Bob note  ").getFeedbackToUser());
        assertEquals(List.of(bob), model.getFilteredPersonList());
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));

        logic.execute("edit 1 notes/");
        logic.execute("find Bob");
        Person cleared = model.getFilteredPersonList().get(0);
        assertEquals("No changes made to contact: Bob Choo", logic.execute("edit 1 notes/   ").getFeedbackToUser());
        assertEquals(List.of(cleared), model.getFilteredPersonList());
    }

    @Test
    public void execute_allFieldsThenOmittedNotes_persistsUpdatesAndRetainsNote() throws Exception {
        Person amy = model.getAddressBook().getPersonList().get(0);
        Person bob = model.getAddressBook().getPersonList().get(1);
        logic.execute("find Bob");
        String command = "edit 1 n/Bob Tan notes/Call parent p/87654321 a/Block 9 d/20-Oct o/20.50"
                + " s/H2 Chemistry day/MON time/09:00 s/Maths day/MON time/09:00"
                + " t/A-Level t/Needs revision";
        assertEquals("Edited contact: Bob Tan", logic.execute(command).getFeedbackToUser());
        Person updated = new PersonBuilder(bob).withName("Bob Tan").withPhone("87654321").withAddress("Block 9")
                .withStartDate("20-Oct").withOutstandingAmount("20.50").withNotes("Call parent")
                .withSubjects("H2 Chemistry", "Maths").withTags("a-level", "needs revision").build();
        assertEquals(List.of(amy, updated), storage.readAddressBook().orElseThrow().getPersonList());

        logic.execute("edit 1 t/ s/Physics day/MON time/09:00 o/0");
        Person partialEdit = new PersonBuilder(updated).withTags().withSubjects("Physics")
                .withOutstandingAmount("0").build();
        assertEquals(List.of(amy, partialEdit), storage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_invalidMixedEdits_preservesAllDataAndFilter() throws Exception {
        logic.execute("find Bob");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        List<Person> stored = List.copyOf(model.getAddressBook().getPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        String[] commands = {"edit 1 notes/One notes/Two", "edit 1 notes/ notes/",
            "edit 1 notes/New p/invalid", "edit 1 notes/New s/", "edit 1 notes/New t/A-Level t/",
            "edit 1 NOTES/New", "edit 1 notes/New x/value"};
        for (String command : commands) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(displayed, model.getFilteredPersonList());
            assertEquals(stored, model.getAddressBook().getPersonList());
            assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_differentNotes_doNotBypassDuplicateIdentity() throws Exception {
        logic.execute("find Bob");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        String duplicate = "edit 1 n/Amy Bee p/11111111 a/Block 1 d/10-Aug o/0 notes/Different note";

        assertThrows(CommandException.class, EditCommand.MESSAGE_DUPLICATE_PERSON, () -> logic.execute(duplicate));
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_legacyProfile_preservesEmptyNotesUntilSupplied() throws Exception {
        Files.writeString(storage.getAddressBookFilePath(), """
                {"persons":[{"name":"Legacy Student","phone":"11111111","address":"Block 1",
                "startDate":"10-Aug","outstandingAmount":"0"}]}
                """);
        Model legacy = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic legacyLogic = createLogic(legacy);
        legacyLogic.execute("edit 1 p/87654321");
        Person withoutNotes = storage.readAddressBook().orElseThrow().getPersonList().get(0);
        assertEquals(Notes.EMPTY, withoutNotes.getNotes());
        Person expected = new PersonBuilder(withoutNotes).withNotes("Legacy note").build();
        legacyLogic.execute("edit 1 notes/Legacy note");
        assertEquals(List.of(expected), storage.readAddressBook().orElseThrow().getPersonList());
    }

    private Logic createLogic(Model target) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
