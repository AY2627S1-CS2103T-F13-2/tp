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

import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests subject normalization across commands and real JSON storage. */
public class SubjectWhitespaceIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("add n/Amy Bee p/11111111 a/Block 1 d/10-Aug"
                + " s/H2   Chemistry day/MON time/09:00 notes/Keep  spaces");
    }

    @Test
    public void execute_addFilterAndEdit_usesCanonicalNamesAndPreservesOtherData() throws Exception {
        Person original = model.getAddressBook().getPersonList().get(0);
        assertEquals("H2 Chemistry", original.getSubjects().iterator().next().value);
        for (String filter : List.of("list s/H2 Chemistry", "list s/h2   CHEMISTRY")) {
            logic.execute(filter);
            assertEquals(List.of(original), model.getFilteredPersonList());
        }

        assertEquals("No changes made to contact: Amy Bee", logic.execute(
                "edit 1 s/h2  chemistry day/MON time/09:00").getFeedbackToUser());
        assertEquals("H2 Chemistry", model.getAddressBook().getPersonList().get(0)
                .getSubjects().iterator().next().value);
        logic.execute("edit 1 s/H2   Physics day/TUE time/10:30");
        Person updated = storage.readAddressBook().orElseThrow().getPersonList().get(0);
        assertEquals(model.getAddressBook().getPersonList(), List.of(updated));
        assertEquals(original.getPhone(), updated.getPhone());
        assertEquals("Keep  spaces", updated.getNotes().value);
        assertTrue(logic.execute("view 1").getFeedbackToUser().contains("Subjects: H2 Physics (Tue 10:30)"));
        assertTrue(Files.readString(storage.getAddressBookFilePath()).contains("H2 Physics"));
    }

    @Test
    public void execute_collidingSubjects_rejectsAddAndEditWithoutChangingDataOrFilter() throws Exception {
        logic.execute("list s/H2 Chemistry");
        List<Person> before = List.copyOf(model.getFilteredPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        for (String prefix : List.of("edit 1", "add n/Bob Choo p/22222222 a/Block 2 d/19-Sep")) {
            String command = prefix + " s/H2 Maths day/MON time/09:00 s/h2   Maths day/TUE time/10:00";
            assertThrows(ParseException.class, ParserUtil.MESSAGE_DUPLICATE_SUBJECT, () -> logic.execute(command));
            assertEquals(before, model.getFilteredPersonList());
            assertEquals(before, model.getAddressBook().getPersonList());
            assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        }
    }
}
