package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import seedu.address.testutil.PersonBuilder;

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
    public void execute_summaryWithMixedPaymentStatuses_showsCounts() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder().withName("Paid Student").build());
        addressBook.addPerson(new PersonBuilder().withName("Owing Student").withOutstandingAmount("0.01").build());
        addressBook.addPerson(new PersonBuilder().withName("Another Owing Student")
                .withOutstandingAmount("20.50").build());
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(addressBook, new UserPrefs());

        String expected = String.format(ListCommand.MESSAGE_SUMMARY_FORMAT,
                ListCommand.getMessageForContactCount(3), 2, 1);
        assertCommandSuccess(new ListCommand(true), model, expected, expectedModel);
    }

    @Test
    public void execute_summaryWithEmptyAddressBook_showsZeroCounts() {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        expectedModel = new ModelManager(new AddressBook(), new UserPrefs());
        String expected = String.format(ListCommand.MESSAGE_SUMMARY_FORMAT,
                ListCommand.MESSAGE_EMPTY, 0, 0);
        assertCommandSuccess(new ListCommand(true), model, expected, expectedModel);
    }

    @Test
    public void execute_summaryWithAllPaid_showsZeroOwing() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder().withName("Paid Student One").build());
        addressBook.addPerson(new PersonBuilder().withName("Paid Student Two").build());
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(addressBook, new UserPrefs());
        String expected = String.format(ListCommand.MESSAGE_SUMMARY_FORMAT,
                ListCommand.getMessageForContactCount(2), 0, 2);
        assertCommandSuccess(new ListCommand(true), model, expected, expectedModel);
    }

    @Test
    public void equals() {
        ListCommand listCommand = new ListCommand();

        // same object -> returns true
        assertEquals(listCommand, listCommand);

        // different ListCommand (no fields to differ on) -> returns true
        assertEquals(listCommand, new ListCommand());
        assertNotEquals(listCommand, new ListCommand(true));

        // different types -> returns false
        assertNotEquals(listCommand, "list");

        // null -> returns false
        assertNotEquals(listCommand, null);
    }

    /**
     * Returns the message expected for listing all persons in {@code model}.
     */
    private String expectedMessage(Model model) {
        return ListCommand.getMessageForContactCount(model.getFilteredPersonList().size());
    }
}
