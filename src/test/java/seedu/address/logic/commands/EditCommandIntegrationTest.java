package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests editing through the command parser and model together.
 */
public class EditCommandIntegrationTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_partialEdit_preservesNotes() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person withNotes = new PersonBuilder(original).withNotes("Revise fractions").build();
        model.setPerson(original, withNotes);
        Person updated = new PersonBuilder(withNotes).withPhone("91234567").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(withNotes, updated);

        assertCommandSuccess(parser.parseCommand("edit 1 p/91234567"), model,
                "Edited contact: " + withNotes.getName(), expectedModel);
    }

    @Test
    public void execute_partialEdit_preservesTags() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person target = new PersonBuilder(original).withTags("A-Level", "Needs revision").build();
        model.setPerson(original, target);
        Person updated = new PersonBuilder(target).withOutstandingAmount("20").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, updated);

        Command command = parser.parseCommand("edit 1 o/20");

        assertCommandSuccess(command, model, "Edited contact: " + target.getName(), expectedModel);
    }

    @Test
    public void execute_outstandingAmountOnly_updatesAmountAndPreservesOtherFields() throws Exception {
        Person target = model.getFilteredPersonList().get(0);
        Person updated = new PersonBuilder(target).withOutstandingAmount("600").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, updated);

        Command command = parser.parseCommand("edit 1 o/600");

        assertCommandSuccess(command, model, "Edited contact: " + target.getName(), expectedModel);
    }

    @Test
    public void execute_outstandingAmountOmitted_preservesNonzeroAmount() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person target = new PersonBuilder(original).withOutstandingAmount("20.50").build();
        model.setPerson(original, target);
        Person updated = new PersonBuilder(target).withPhone("91234567").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, updated);

        Command command = parser.parseCommand("edit 1 p/91234567");

        assertCommandSuccess(command, model, "Edited contact: " + target.getName(), expectedModel);
    }

    @Test
    public void execute_numericallyUnchangedAmount_preservesFilteredList() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person target = new PersonBuilder(original).withOutstandingAmount("20").build();
        model.setPerson(original, target);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        Command command = parser.parseCommand("edit 1 o/20.00");

        assertCommandSuccess(command, model, "No changes made to contact: " + target.getName(), expectedModel);
    }

    @Test
    public void execute_amountCreatesDuplicateOutsideFilteredList_preservesDataAndFilter() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person existing = new PersonBuilder(original).withOutstandingAmount("20").build();
        model.setPerson(original, existing);
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person target = new PersonBuilder(existing).withOutstandingAmount("600").build();
        model.setPerson(secondPerson, target);
        // Both profiles have the same name, so filter by the complete profile to hide the duplicate.
        model.updateFilteredPersonList(person -> person.equals(target));
        assertEquals(List.of(target), model.getFilteredPersonList());

        Command command = parser.parseCommand("edit 1 o/20.00");

        assertCommandFailure(command, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_profilesDifferOnlyInAmount_success() throws Exception {
        Person target = model.getFilteredPersonList().get(0);
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person existing = new PersonBuilder(target).withOutstandingAmount("600").build();
        model.setPerson(secondPerson, existing);
        Person updated = new PersonBuilder(target).withOutstandingAmount("20").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, updated);

        Command command = parser.parseCommand("edit 1 o/20");

        assertCommandSuccess(command, model, "Edited contact: " + target.getName(), expectedModel);
    }

    @Test
    public void execute_outstandingAmountFilteredList_updatesDisplayedContact() throws Exception {
        Person target = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person updated = new PersonBuilder(target).withOutstandingAmount("600").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, updated);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        expectedModel.updateFilteredPersonList(model.getFilteredPersonListPredicate()::test);

        Command command = parser.parseCommand("edit 1 o/600");

        assertCommandSuccess(command, model, "Edited contact: " + target.getName(), expectedModel);
    }

    @Test
    public void execute_zeroAmount_resetsNonzeroBalance() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person target = new PersonBuilder(original).withOutstandingAmount("20.50").build();
        model.setPerson(original, target);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, original);

        Command command = parser.parseCommand("edit 1 o/0");

        assertCommandSuccess(command, model, "Edited contact: " + target.getName(), expectedModel);
    }

    @Test
    public void execute_partialEditFilteredList_updatesDisplayedContact() throws Exception {
        Person target = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person updated = new PersonBuilder(target).withPhone("91234567").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, updated);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        expectedModel.updateFilteredPersonList(model.getFilteredPersonListPredicate()::test);

        Command command = parser.parseCommand("edit 1 p/91234567");

        assertCommandSuccess(command, model, "Edited contact: " + target.getName(), expectedModel);
    }

    @Test
    public void execute_editArchivedStudent_preservesArchivedStatus() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person archived = original.withArchived(true);
        model.setPerson(original, archived);
        model.updateFilteredPersonList(Person::isArchived);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        Person updated = new PersonBuilder(archived).withPhone("91234567").build().withArchived(true);
        expectedModel.setPerson(archived, updated);
        expectedModel.updateFilteredPersonList(Person::isArchived);

        Command command = parser.parseCommand("edit 1 p/91234567");

        assertCommandSuccess(command, model, "Edited contact: " + archived.getName(), expectedModel);
        assertTrue(model.getAddressBook().getPersonList().get(0).isArchived());
    }

    @Test
    public void execute_editArchivedStudentFromArchivedList_remainsArchived() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person archived = original.withArchived(true);
        model.setPerson(original, archived);
        model.updateFilteredPersonList(Person::isArchived);

        Command command = parser.parseCommand("edit 1 o/20");
        command.execute(model);

        Person updated = model.getAddressBook().getPersonList().get(0);
        assertTrue(updated.isArchived());
        assertEquals("20", updated.getOutstandingAmount().toPlainString());

        model.updateFilteredPersonList(Person::isArchived);
        assertEquals(List.of(updated), model.getFilteredPersonList());
        model.updateFilteredPersonList(person -> !person.isArchived());
        assertFalse(model.getFilteredPersonList().contains(updated));
    }

    @Test
    public void execute_trimmedUnchangedValue_preservesFilteredList() throws Exception {
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        Person target = model.getFilteredPersonList().get(0);

        Command command = parser.parseCommand("edit 1 p/  " + target.getPhone().value + "  ");

        assertCommandSuccess(command, model, "No changes made to contact: " + target.getName(), expectedModel);
    }

    @Test
    public void execute_invalidInput_preservesDataAndFilteredList() {
        Person original = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person target = new PersonBuilder(original).withOutstandingAmount("20.50").build();
        model.setPerson(original, target);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        AddressBook originalData = new AddressBook(model.getAddressBook());
        List<Person> originalDisplayedList = new ArrayList<>(model.getFilteredPersonList());
        String[] invalidCommands = {
            "edit 1 n/New Name p/invalid",
            "edit 1 a/New address x/unknown",
            "edit 1 p/91234567 p/87654321",
            "edit 1 p/91234567 o/-1",
            "edit 1 p/91234567 o/20.123",
            "edit 1 p/91234567 o/",
            "edit 1 p/91234567 o/20 o/600",
            "edit 1 o/600 p/invalid",
            "edit 1",
            "edit 0 p/91234567"
        };

        for (String commandText : invalidCommands) {
            assertThrows(ParseException.class, () -> parser.parseCommand(commandText).execute(model));
            assertEquals(originalData, model.getAddressBook());
            assertEquals(originalDisplayedList, model.getFilteredPersonList());
        }
    }
}
