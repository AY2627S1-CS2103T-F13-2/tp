package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_START_DATE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_balanceEdit_preservesIdentityAndOtherFields() {
        Person original = model.getFilteredPersonList().get(0);
        Person updated = new PersonBuilder(original).withOutstandingAmount("25").build();
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(original, updated);
        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withOutstandingAmount("25").build()), model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, updated.getName()), expected);
    }

    @Test
    public void execute_identityEditWithDifferentBalance_rejectedWithoutChanges() {
        Person first = model.getFilteredPersonList().get(0);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(first).withOutstandingAmount("25").build();
        assertCommandFailure(new EditCommand(INDEX_SECOND_PERSON, descriptor), model,
                EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_activeStudent_keepsArchivedStudentsHidden() throws Exception {
        Person archivedPerson = model.getFilteredPersonList().get(0).withArchived(true);
        model.setPerson(model.getFilteredPersonList().get(0), archivedPerson);
        new ListCommand().execute(model);
        Person target = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder(target).withNotes("Updated").build();

        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withNotes("Updated").build()).execute(model);

        assertTrue(model.getFilteredPersonList().contains(editedPerson));
        assertFalse(model.getFilteredPersonList().stream().anyMatch(Person::isArchived));
        assertTrue(model.getAddressBook().getPersonList().contains(archivedPerson));
        ListCommand.forArchivedStudents().execute(model);
        assertEquals(List.of(archivedPerson), model.getFilteredPersonList());
    }

    @Test
    public void execute_archivedStudent_preservesArchiveStateAndArchivedView() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person archivedPerson = original.withArchived(true);
        model.setPerson(original, archivedPerson);
        ListCommand.forArchivedStudents().execute(model);
        Person editedPerson = new PersonBuilder(original).withNotes("Updated").build().withArchived(true);

        CommandResult result = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withNotes("Updated").build()).execute(model);

        assertEquals("Edited contact: " + original.getName(), result.getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().contains(editedPerson));
        assertTrue(model.getFilteredPersonList().stream().allMatch(Person::isArchived));
        assertEquals(List.of(editedPerson), model.getFilteredPersonList());
    }

    @Test
    public void execute_archivedStudentUnchanged_preservesArchiveStateAndFilter() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person archivedPerson = original.withArchived(true);
        model.setPerson(original, archivedPerson);
        ListCommand.forArchivedStudents().execute(model);

        CommandResult result = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(original.getPhone().value).build()).execute(model);

        assertEquals("No changes made to contact: " + original.getName(), result.getFeedbackToUser());
        assertEquals(List.of(archivedPerson), model.getFilteredPersonList());
        assertTrue(model.getAddressBook().getPersonList().contains(archivedPerson));
    }

    @Test
    public void execute_replacementNotes_preservesOtherFieldsAndCommandSnapshot() {
        Person original = model.getFilteredPersonList().get(0);
        Person target = new PersonBuilder(original).withTags("a-level").withSubjects("Maths")
                .withNotes("Old note").build();
        model.setPerson(original, target);
        Person updated = new PersonBuilder(target).withNotes("Revise fractions").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNotes("Revise fractions").build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        descriptor.setNotes(Notes.EMPTY);
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(target, updated);

        assertCommandSuccess(command, model, "Edited contact: " + target.getName(), expected);
    }

    @Test
    public void execute_replacementTags_preservesOtherFieldsAndCommandSnapshot() {
        Person original = model.getFilteredPersonList().get(0);
        Person target = new PersonBuilder(original).withTags("old").withNotes("Keep these notes")
                .withSubjects("Maths").build();
        model.setPerson(original, target);
        Person updated = new PersonBuilder(target).withTags("A-Level", "Needs revision").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTags("A-Level", "Needs revision").build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        descriptor.setTags(Set.of());
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(target, updated);

        assertCommandSuccess(command, model, "Edited contact: " + target.getName(), expected);
    }

    @Test
    public void execute_emptyTags_clearsExistingTags() {
        Person original = model.getFilteredPersonList().get(0);
        Person target = new PersonBuilder(original).withTags("A-Level").build();
        model.setPerson(original, target);
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(target, original);

        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withTags().build());
        assertCommandSuccess(command, model, "Edited contact: " + target.getName(), expected);
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, editedPerson.getName());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withStartDate(VALID_START_DATE_BOB).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withStartDate(VALID_START_DATE_BOB).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, editedPerson.getName());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecified_failure() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        assertCommandFailure(editCommand, model, "At least one field must be provided to edit.");
    }

    @Test
    public void execute_samePhone_noChanges() {
        Person person = model.getFilteredPersonList().get(0);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhone(person.getPhone().value).build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                "No changes made to contact: " + person.getName(), expectedModel);
    }

    @Test
    public void execute_allValuesUnchangedFilteredList_preservesFilter() {
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        Person person = model.getFilteredPersonList().get(0);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();

        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                "No changes made to contact: " + person.getName(), expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = "Edited contact: " + editedPerson.getName() + EditCommand.MESSAGE_INDEXES_CHANGED;

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_CONTACT_NOT_FOUND);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_CONTACT_NOT_FOUND);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand(true)));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

}
