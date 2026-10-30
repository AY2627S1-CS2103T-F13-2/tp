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

/** Tests subject edits with real storage, filtered indexes, and other editable fields. */
public class EditSubjectsIntegrationTest {
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
                + " s/English day/MON time/09:00 t/A-Level notes/Amy note");
        logic.execute("add n/Bob Choo p/22222222 a/Block 2 d/19-Sep"
                + " s/Maths day/MON time/09:00 s/Physics day/MON time/09:00"
                + " o/600 t/Needs revision notes/Bob note");
    }

    @Test
    public void execute_subjectReplacement_preservesOtherFieldsAndPersistsInputOrder() throws Exception {
        Person amy = model.getAddressBook().getPersonList().get(0);
        Person bob = model.getAddressBook().getPersonList().get(1);
        logic.execute("find Bob");

        assertEquals("Edited contact: Bob Choo",
                logic.execute("edit 1 s/Physics day/MON time/09:00"
                        + " s/H2 Chemistry day/MON time/09:00").getFeedbackToUser());
        Person updated = new PersonBuilder(bob).withSubjects("Physics", "H2 Chemistry").build();
        assertEquals(List.of(updated), model.getFilteredPersonList());
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(amy, updated), reloaded.getAddressBook().getPersonList());
        assertEquals(List.of("Physics", "H2 Chemistry"), reloaded.getAddressBook().getPersonList().get(1)
                .getSubjects().stream().map(subject -> subject.value).toList());
        assertTrue(createLogic(reloaded).execute("view 2").getFeedbackToUser()
                .endsWith("Subjects: Physics (Mon 09:00), H2 Chemistry (Mon 09:00)"
                        + "\nTags: needs revision\nNotes: Bob note"));
    }

    @Test
    public void execute_caseAndOrderOnlyChange_preservesFilterAndSavedSpelling() throws Exception {
        logic.execute("find Bob");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());

        assertEquals("No changes made to contact: Bob Choo",
                logic.execute("edit 1 s/PHYSICS day/MON time/09:00"
                        + " s/maths day/MON time/09:00").getFeedbackToUser());
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        assertTrue(logic.execute("view 1").getFeedbackToUser()
                .endsWith("Subjects: Maths (Mon 09:00), Physics (Mon 09:00)"
                        + "\nTags: needs revision\nNotes: Bob note"));
    }

    @Test
    public void execute_subjectsAndOtherFields_updatesOnlySuppliedValues() throws Exception {
        Person amy = model.getAddressBook().getPersonList().get(0);
        Person bob = model.getAddressBook().getPersonList().get(1);
        logic.execute("find Bob");
        logic.execute("edit 1 s/H2 Chemistry day/MON time/09:00 t/ p/87654321");
        Person updated = new PersonBuilder(bob).withSubjects("H2 Chemistry").withTags().withPhone("87654321").build();
        assertEquals(List.of(amy, updated), storage.readAddressBook().orElseThrow().getPersonList());
        logic.execute("edit 1 o/20");
        assertEquals(new PersonBuilder(updated).withOutstandingAmount("20").build(),
                storage.readAddressBook().orElseThrow().getPersonList().get(1));
    }

    @Test
    public void execute_invalidSubjects_preservesAllDataAndCurrentFilter() throws Exception {
        logic.execute("find Bob");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        List<Person> stored = List.copyOf(model.getAddressBook().getPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        String[] invalidCommands = {"edit 1 s/", "edit 1 s/Maths day/MON time/09:00 s/",
            "edit 1 p/87654321 t/A-Level s/--- day/MON time/09:00",
            "edit 1 s/" + "A".repeat(51) + " day/MON time/09:00", "edit 1 S/Maths",
            "edit 1 s/Maths day/MON time/09:00 x/value"};
        for (String command : invalidCommands) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(displayed, model.getFilteredPersonList());
            assertEquals(stored, model.getAddressBook().getPersonList());
            assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        }
        String repeatedPhone = "edit 1 s/Maths day/MON time/09:00 p/11111111 p/22222222";
        String message = EditCommandParser.MESSAGE_DUPLICATE_FIELDS;
        assertThrows(ParseException.class, message, () -> logic.execute(repeatedPhone));
    }

    @Test
    public void execute_differentSubjects_doNotBypassDuplicateIdentity() throws Exception {
        logic.execute("find Bob");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        String duplicate = "edit 1 n/Amy Bee p/11111111 a/Block 1 d/10-Aug o/0"
                + " s/Physics day/MON time/09:00";

        assertThrows(CommandException.class, EditCommand.MESSAGE_DUPLICATE_PERSON, () -> logic.execute(duplicate));
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_legacyProfile_preservesAbsentSubjectsUntilSupplied() throws Exception {
        Files.writeString(storage.getAddressBookFilePath(), """
                {"persons":[{"name":"Legacy Student","phone":"11111111","address":"Block 1",
                "startDate":"10-Aug","outstandingAmount":"0"}]}
                """);
        Model legacy = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic legacyLogic = createLogic(legacy);
        legacyLogic.execute("edit 1 p/87654321");
        Person withoutSubjects = storage.readAddressBook().orElseThrow().getPersonList().get(0);
        assertTrue(withoutSubjects.getSubjects().isEmpty());
        assertEquals("87654321", withoutSubjects.getPhone().value);
        Person expected = new PersonBuilder(withoutSubjects).withSubjects("Maths").build();
        legacyLogic.execute("edit 1 s/Maths day/MON time/09:00");
        assertEquals(List.of(expected), storage.readAddressBook().orElseThrow().getPersonList());
    }

    private Logic createLogic(Model target) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
