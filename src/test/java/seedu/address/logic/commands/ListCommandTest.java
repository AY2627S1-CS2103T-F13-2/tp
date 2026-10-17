package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import seedu.address.model.person.Subject;
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
    public void equals() {
        ListCommand listCommand = new ListCommand();

        // same object -> returns true
        assertEquals(listCommand, listCommand);

        // different ListCommand (no fields to differ on) -> returns true
        assertEquals(listCommand, new ListCommand());

        // different types -> returns false
        assertNotEquals(listCommand, "list");

        // null -> returns false
        assertNotEquals(listCommand, null);
        assertNotEquals(listCommand, new ListCommand(Set.of(new Subject("Maths"))));
    }

    @Test
    public void execute_subjects_matchesAnyCaseInsensitiveAndRestoresAll() {
        Person maths = new PersonBuilder(ALICE).withSubjects("Maths").build();
        Person physics = new PersonBuilder(ALICE).withName("Physics Student").withSubjects("Physics").build();
        Person chemistry = new PersonBuilder(ALICE).withName("Chemistry Student")
                .withSubjects("Chemistry").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(maths);
        addressBook.addPerson(physics);
        addressBook.addPerson(chemistry);
        model = new ModelManager(addressBook, new UserPrefs());

        model.updateFilteredPersonList(person -> false);
        assertEquals("Listed 2 contacts.", new ListCommand(Set.of(new Subject("maths"), new Subject("PHYSICS")))
                .execute(model).getFeedbackToUser());
        assertEquals(List.of(maths, physics), model.getFilteredPersonList());
        assertEquals("Listed 3 contacts.", new ListCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(maths, physics, chemistry), model.getFilteredPersonList());
    }

    @Test
    public void execute_subjectsNoMatch_showsEmptyList() {
        Person additionalMaths = new PersonBuilder(ALICE).withSubjects("Additional Maths").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(additionalMaths);
        model = new ModelManager(addressBook, new UserPrefs());

        assertEquals(ListCommand.MESSAGE_NO_SUBJECT_MATCHES,
                new ListCommand(Set.of(new Subject("Maths"))).execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    /**
     * Returns the message expected for listing all persons in {@code model}.
     */
    private String expectedMessage(Model model) {
        return ListCommand.getMessageForContactCount(model.getFilteredPersonList().size());
    }
}
