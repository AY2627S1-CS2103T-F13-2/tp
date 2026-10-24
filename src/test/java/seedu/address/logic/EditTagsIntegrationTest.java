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
import seedu.address.logic.parser.EditCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Tests tag edits through command parsing, filtered indexes, and real JSON storage. */
public class EditTagsIntegrationTest {
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
                + " s/Maths day/MON time/09:00 o/20 t/A-Level notes/Amy note");
        logic.execute("add n/Bob Choo p/22222222 a/Block 2 d/19-Sep"
                + " s/Physics day/MON time/09:00 o/600 t/Old notes/Bob note");
    }

    @Test
    public void execute_replacementThenClear_persistsAndPreservesOtherFields() throws Exception {
        Person amy = model.getAddressBook().getPersonList().get(0);
        Person bob = model.getAddressBook().getPersonList().get(1);
        logic.execute("find Bob");

        assertEquals("Edited contact: Bob Choo",
                logic.execute("edit 1 t/A-Level t/Needs revision t/a-level").getFeedbackToUser());
        Person updated = new PersonBuilder(bob).withTags("a-level", "needs revision").build();
        assertEquals(List.of(amy, updated), model.getFilteredPersonList());
        assertEquals(List.of(amy, updated), storage.readAddressBook().orElseThrow().getPersonList());
        assertTrue(logic.execute("view 2").getFeedbackToUser()
                .endsWith("Subjects: Physics (Mon 09:00)"
                        + "\nTags: a-level, needs revision\nNotes: Bob note"));

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic reloadedLogic = createLogic(reloaded);
        reloadedLogic.execute("edit 2 t/");
        Person cleared = new PersonBuilder(bob).withTags().build();
        assertEquals(List.of(amy, cleared), storage.readAddressBook().orElseThrow().getPersonList());
        reloadedLogic.execute("edit 2 p/87654321");
        assertEquals(new PersonBuilder(cleared).withPhone("87654321").build(),
                storage.readAddressBook().orElseThrow().getPersonList().get(1));
    }

    @Test
    public void execute_sameNormalizedTagsAndClearingEmptyTags_areNoOps() throws Exception {
        logic.execute("find Amy");
        Person amy = model.getFilteredPersonList().get(0);
        assertEquals("No changes made to contact: Amy Bee",
                logic.execute("edit 1 t/ A-LEVEL t/a-level").getFeedbackToUser());
        assertEquals(List.of(amy), model.getFilteredPersonList());

        logic.execute("list");
        logic.execute("edit 2 t/");
        logic.execute("find Bob");
        Person bob = model.getFilteredPersonList().get(0);
        assertEquals("No changes made to contact: Bob Choo", logic.execute("edit 1 t/").getFeedbackToUser());
        assertEquals(List.of(bob), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidTags_preservesDataAndCurrentFilter() throws Exception {
        logic.execute("find Bob");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        List<Person> stored = List.copyOf(model.getAddressBook().getPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        for (String command : new String[] {"edit 1 t/A-Level t/", "edit 1 t/ t/A-Level",
            "edit 1 p/87654321 t/bad!", "edit 1 T/A-Level", "edit 1 t/A-Level x/value"}) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(displayed, model.getFilteredPersonList());
            assertEquals(stored, model.getAddressBook().getPersonList());
            assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        }
        String repeatedPhone = "edit 1 t/A-Level p/11111111 p/22222222";
        String repeatedFieldMessage = EditCommandParser.MESSAGE_DUPLICATE_FIELDS;
        assertThrows(ParseException.class, repeatedFieldMessage, () -> logic.execute(repeatedPhone));
    }

    @Test
    public void execute_differentTags_doNotBypassDuplicateIdentity() throws Exception {
        logic.execute("find Bob");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        String duplicate = "edit 1 n/Amy Bee p/11111111 a/Block 1 d/10-Aug o/20 t/Different";

        assertThrows(CommandException.class, EditCommand.MESSAGE_DUPLICATE_PERSON, () -> logic.execute(duplicate));
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
    }

    private Logic createLogic(Model target) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
