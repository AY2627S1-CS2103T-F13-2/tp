package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ViewCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_studentWithAllNewFields_displaysStoredDetails() {
        Person original = model.getFilteredPersonList().get(0);
        Person updated = new PersonBuilder(original).withSubjects("Maths", "H2 Chemistry")
                .withTags("Needs revision", "A-Level").withNotes("Revise  fractions & 中文!").build();
        model.setPerson(original, updated);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model, expectedDetails(updated), expectedModel);
    }

    @Test
    public void execute_studentWithNotes_displaysNotes() {
        Person original = model.getFilteredPersonList().get(0);
        Person withNotes = new PersonBuilder(original).withNotes("Revise  fractions & algebra!").build();
        model.setPerson(original, withNotes);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model, expectedDetails(withNotes), expectedModel);
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model, expectedDetails(person), expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        // Select a different original record so that index 1 must refer to the filtered list.
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person person = model.getFilteredPersonList().get(0);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model, expectedDetails(person), expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index index = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new ViewCommand(index), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new ViewCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_throwsCommandException() {
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(new ViewCommand(INDEX_FIRST_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        ViewCommand command = new ViewCommand(INDEX_FIRST_PERSON);
        assertTrue(command.equals(command));
        assertEquals(command, new ViewCommand(INDEX_FIRST_PERSON));
        assertFalse(command.equals(new ViewCommand(INDEX_SECOND_PERSON)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new DeleteCommand(INDEX_FIRST_PERSON)));
    }

    private String expectedDetails(Person person) {
        return "Name: " + person.getName() + "\nPhone: " + person.getPhone() + "\nAddress: " + person.getAddress()
                + "\nStart date: " + person.getStartDate()
                + "\nOutstanding amount: " + person.getOutstandingAmount().toCurrencyString()
                + "\nSubjects: " + (person.getSubjects().isEmpty() ? "Not specified" : person.getSubjects().stream()
                        .map(Object::toString).collect(Collectors.joining(", ")))
                + "\nTags: " + (person.getTags().isEmpty() ? "Not specified" : person.getTags().stream()
                        .map(tag -> tag.tagName).sorted().collect(Collectors.joining(", ")))
                + "\nNotes: " + person.getNotes();
    }
}
