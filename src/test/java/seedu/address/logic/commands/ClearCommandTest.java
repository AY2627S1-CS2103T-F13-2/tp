package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        CommandResult expectedResult = new CommandResult(ClearCommand.MESSAGE_NO_STUDENTS, false, false,
                CommandResult.ListPresentation.UNCHANGED);
        assertCommandSuccess(new ClearCommand(true), model, expectedResult, expectedModel);
        assertCommandSuccess(new ClearCommand(false), model, expectedResult, expectedModel);
    }

    @Test
    public void execute_confirmed_deletesEveryStudent() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());
        int studentCount = getTypicalAddressBook().getPersonList().size();

        assertCommandSuccess(new ClearCommand(true), model,
                String.format(ClearCommand.MESSAGE_SUCCESS, studentCount), expectedModel);
    }

    @Test
    public void execute_notConfirmed_onlyAsksAndKeepsStudents() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        int studentCount = getTypicalAddressBook().getPersonList().size();
        CommandResult expectedResult = new CommandResult(String.format(ClearCommand.MESSAGE_CONFIRM, studentCount),
                false, false, CommandResult.ListPresentation.UNCHANGED);

        assertCommandSuccess(new ClearCommand(false), model, expectedResult, expectedModel);
    }

    @Test
    public void requiresAddressBookSave_onlyWhenConfirmed() {
        assertTrue(new ClearCommand(true).requiresAddressBookSave());
        assertFalse(new ClearCommand(false).requiresAddressBookSave());
    }

    @Test
    public void equals() {
        ClearCommand command = new ClearCommand(true);
        assertTrue(command.equals(command));
        assertTrue(new ClearCommand(true).equals(new ClearCommand(true)));
        assertNotEquals(new ClearCommand(true), new ClearCommand(false));
        assertNotEquals(new ClearCommand(true), null);
    }
}
