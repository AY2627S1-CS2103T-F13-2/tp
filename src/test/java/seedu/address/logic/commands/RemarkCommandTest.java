package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {
    @TempDir
    public Path temporaryFolder;

    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addReplaceRemoveRemark_success() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        assertTrue(parser.parseCommand("remark 1 r/Likes baseball").execute(model).getFeedbackToUser()
                .startsWith("Added remark"));
        Person updated = model.getFilteredPersonList().get(0);
        assertEquals(new Remark("Likes baseball"), updated.getRemark());
        assertTrue(original.isSamePerson(updated));
        assertFalse(original.equals(updated));
        parser.parseCommand("remark 1 r/Likes swimming").execute(model);
        assertEquals(new Remark("Likes swimming"), model.getFilteredPersonList().get(0).getRemark());
        assertTrue(parser.parseCommand("remark 1 r/").execute(model).getFeedbackToUser().startsWith("Removed remark"));
        assertEquals(new Remark(""), model.getFilteredPersonList().get(0).getRemark());
    }

    @Test
    public void execute_filteredList_editsDisplayedPerson() throws Exception {
        Person target = model.getFilteredPersonList().get(1);
        model.updateFilteredPersonList(person -> person.equals(target));
        parser.parseCommand("remark 1 r/Filtered contact").execute(model);
        Person expected = new PersonBuilder(target).withRemark("Filtered contact").build();
        assertTrue(model.getAddressBook().getPersonList().contains(expected));
        assertEquals(model.getAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void parse_invalidIndices_throwsParseException() {
        for (String command : new String[] {"remark", "remark 0 r/note", "remark -1 r/note", "remark abc r/note"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command));
        }
    }

    @Test
    public void execute_outOfRange_doesNotChangeModel() {
        Person original = model.getFilteredPersonList().get(0);
        assertThrows(CommandException.class, () -> parser.parseCommand("remark 999 r/note").execute(model));
        assertEquals(original, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_missingRemark_removesExistingNote() throws Exception {
        parser.parseCommand("remark 1 r/note").execute(model);
        parser.parseCommand("remark 1").execute(model);
        assertEquals(new Remark(""), model.getFilteredPersonList().get(0).getRemark());
    }

    @Test
    public void execute_editOtherFields_preservesRemark() throws Exception {
        parser.parseCommand("remark 1 r/Keep this note").execute(model);
        parser.parseCommand("edit 1 n/Updated Name").execute(model);
        assertEquals(new Remark("Keep this note"), model.getFilteredPersonList().get(0).getRemark());
    }

    @Test
    public void storage_saveAndReload_preservesRemark() throws Exception {
        parser.parseCommand("remark 1 r/Unicode note 日本語").execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void equals_andNullHandling() {
        RemarkCommand command = new RemarkCommand(Index.fromOneBased(1), new Remark("note"));
        assertEquals(command, new RemarkCommand(Index.fromOneBased(1), new Remark("note")));
        assertFalse(command.equals(new RemarkCommand(Index.fromOneBased(2), new Remark("note"))));
        assertFalse(command.equals(new RemarkCommand(Index.fromOneBased(1), new Remark("other"))));
        assertFalse(command.equals(null));
        assertThrows(NullPointerException.class, () -> new Remark(null));
        assertThrows(NullPointerException.class, () -> new RemarkCommand(null, new Remark("note")));
    }
}
