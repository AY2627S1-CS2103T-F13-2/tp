package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonHasAnyTagPredicate;
import seedu.address.model.tag.Tag;
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
    public void execute_tagFilter_searchesAllStudentsAndPlainListRestoresAll() {
        Person alice = new PersonBuilder(ALICE).withTags("a-level").build();
        Person bob = new PersonBuilder().withName("Bob Choo").withTags("needs revision").build();
        Person untagged = new PersonBuilder().withName("Untagged Student").build();
        model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(bob);
        model.addPerson(untagged);
        model.updateFilteredPersonList(person -> false);

        ListCommand filter = new ListCommand(new PersonHasAnyTagPredicate(
                Set.of(new Tag("a-level"), new Tag("needs revision"))));
        assertEquals("Listed 2 contacts.", filter.execute(model).getFeedbackToUser());
        assertEquals(List.of(alice, bob), model.getFilteredPersonList());
        assertEquals(List.of(alice, bob, untagged), model.getAddressBook().getPersonList());
        new ListCommand().execute(model);
        assertEquals(List.of(alice, bob, untagged), model.getFilteredPersonList());
    }

    @Test
    public void execute_tagFilterWithNoMatches_showsSpecificFeedback() {
        ListCommand filter = new ListCommand(new PersonHasAnyTagPredicate(Set.of(new Tag("missing"))));
        assertEquals(ListCommand.MESSAGE_NO_MATCHES, filter.execute(model).getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(getTypicalAddressBook().getPersonList(), model.getAddressBook().getPersonList());
    }

    @Test
    public void equals_differentFiltersAreNotEqual() {
        ListCommand filter = new ListCommand(new PersonHasAnyTagPredicate(Set.of(new Tag("a-level"))));
        assertEquals(filter, new ListCommand(new PersonHasAnyTagPredicate(Set.of(new Tag("A-Level")))));
        assertNotEquals(filter, new ListCommand());
        assertNotEquals(filter, new ListCommand(new PersonHasAnyTagPredicate(Set.of(new Tag("other")))));
    }
    @Test
    public void execute_summaryWithMixedPaymentStatuses_showsTotalAndPaid() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder().withName("Paid Student").build());
        addressBook.addPerson(new PersonBuilder().withName("Owing Student").withOutstandingAmount("0.01").build());
        addressBook.addPerson(new PersonBuilder().withName("Another Owing Student")
                .withOutstandingAmount("20.50").build());
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(addressBook, new UserPrefs());

        String expected = String.format(ListCommand.MESSAGE_SUMMARY_FORMAT,
                "Total: 3", "Paid: 1");
        assertCommandSuccess(new ListCommand(true), model, expected, expectedModel);
    }

    @Test
    public void execute_summaryWithEmptyAddressBook_showsZeroCounts() {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        expectedModel = new ModelManager(new AddressBook(), new UserPrefs());
        String expected = String.format(ListCommand.MESSAGE_SUMMARY_FORMAT,
                "Total: 0", "Paid: 0");
        assertCommandSuccess(new ListCommand(true), model, expected, expectedModel);
    }

    @Test
    public void execute_summaryWithAllZeroOutstandingAmounts_showsAllPaid() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder().withName("Paid Student One").build());
        addressBook.addPerson(new PersonBuilder().withName("Paid Student Two").build());
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(addressBook, new UserPrefs());
        String expected = String.format(ListCommand.MESSAGE_SUMMARY_FORMAT,
                "Total: 2", "Paid: 2");
        assertCommandSuccess(new ListCommand(true), model, expected, expectedModel);
    }

    @Test
    public void execute_archivedStudents_areExcludedFromListAndSummary() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder().withName("Active Student").build());
        addressBook.addPerson(new PersonBuilder().withName("Archived Student").build().withArchived(true));
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(addressBook, new UserPrefs());
        expectedModel.updateFilteredPersonList(Model.PREDICATE_SHOW_ACTIVE_PERSONS);

        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SINGULAR, expectedModel);
        String expected = String.format(ListCommand.MESSAGE_SUMMARY_FORMAT,
                "Total: 1", "Paid: 1");
        assertCommandSuccess(new ListCommand(true), model, expected, expectedModel);
    }

    @Test
    public void execute_summaryWithFilteredList_countsAllStudents() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder().withName("Paid Student").build());
        addressBook.addPerson(new PersonBuilder().withName("Owing Student").withOutstandingAmount("0.99").build());
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(addressBook, new UserPrefs());
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        String expected = String.format(ListCommand.MESSAGE_SUMMARY_FORMAT,
                "Total: 2", "Paid: 1");
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
