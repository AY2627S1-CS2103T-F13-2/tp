package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Contains tests for archiving and unarchiving student profiles. */
public class ArchiveCommandTest {

    @Test
    public void execute_archiveActiveStudent_archivesStudentAndHidesIt() throws Exception {
        Model model = modelWith(ALICE);

        CommandResult result = new ArchiveCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals("Archived Alice Pauline.", result.getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().get(0).isArchived());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_sameNameStudents_archivesOnlyTheChosenOne() throws Exception {
        Person otherAlice = new PersonBuilder(ALICE).withPhone("81234567").build();
        Model model = modelWith(ALICE, otherAlice);

        new ArchiveCommand(INDEX_FIRST_PERSON).execute(model);
        new ArchiveCommand(INDEX_FIRST_PERSON).execute(model);

        // The second archive reaches the other Alice, which a name could not tell apart.
        assertTrue(model.getAddressBook().getPersonList().stream().allMatch(Person::isArchived));
    }

    @Test
    public void execute_archiveArchivedStudent_throwsException() {
        Model model = modelWith(ALICE.withArchived(true));
        model.updateFilteredPersonList(Person::isArchived);

        assertCommandFailure(new ArchiveCommand(INDEX_FIRST_PERSON), model,
                "Alice Pauline is already archived.");
    }

    @Test
    public void execute_unarchiveArchivedStudent_restoresStudent() throws Exception {
        Model model = modelWith(ALICE.withArchived(true));
        model.updateFilteredPersonList(Person::isArchived);

        CommandResult result = new UnarchiveCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals("Unarchived Alice Pauline.", result.getFeedbackToUser());
        assertFalse(model.getAddressBook().getPersonList().get(0).isArchived());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_unarchiveActiveStudent_throwsException() {
        Model model = modelWith(ALICE);

        assertCommandFailure(new UnarchiveCommand(INDEX_FIRST_PERSON), model,
                "Alice Pauline is already active.");
    }

    @Test
    public void execute_indexOutOfRange_throwsException() {
        Model model = modelWith(ALICE);
        Index outOfRange = Index.fromOneBased(2);

        assertCommandFailure(new ArchiveCommand(outOfRange), model, ArchiveCommand.MESSAGE_CONTACT_NOT_FOUND);
        assertCommandFailure(new UnarchiveCommand(outOfRange), model, ArchiveCommand.MESSAGE_CONTACT_NOT_FOUND);
    }

    @Test
    public void equals() {
        assertEquals(new ArchiveCommand(INDEX_FIRST_PERSON), new ArchiveCommand(INDEX_FIRST_PERSON));
        assertNotEquals(new ArchiveCommand(INDEX_FIRST_PERSON), new ArchiveCommand(INDEX_SECOND_PERSON));
        assertEquals(new UnarchiveCommand(INDEX_FIRST_PERSON), new UnarchiveCommand(INDEX_FIRST_PERSON));
        assertNotEquals(new UnarchiveCommand(INDEX_FIRST_PERSON), new UnarchiveCommand(INDEX_SECOND_PERSON));
    }

    private static Model modelWith(Person... persons) {
        AddressBook addressBook = new AddressBook();
        for (Person person : persons) {
            addressBook.addPerson(person);
        }
        return new ModelManager(addressBook, new UserPrefs());
    }
}
