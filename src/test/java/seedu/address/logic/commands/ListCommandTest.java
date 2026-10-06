package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, expectedMessage(expectedModel), expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, expectedMessage(expectedModel), expectedModel);
    }

    @Test
    public void execute_emptyAddressBook_showsEmptyMessage() {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        expectedModel = new ModelManager(new AddressBook(), new UserPrefs());
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_EMPTY, expectedModel);
    }

    @Test
    public void execute_oneContact_showsSingularMessage() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(new AddressBook(addressBook), new UserPrefs());
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SINGULAR, expectedModel);
    }

    @Test
    public void equals() {
        assertEquals(new ListCommand(), new ListCommand());
    }

    /**
     * Returns the message expected for listing all persons in {@code model}.
     */
    private String expectedMessage(Model model) {
        return ListCommand.getMessageForContactCount(model.getFilteredPersonList().size());
    }
}
