package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

/** Contains tests for archiving and unarchiving student profiles. */
public class ArchiveCommandTest {

    @Test
    public void execute_archiveActiveStudent_archivesStudentAndHidesIt() throws Exception {
        Model model = modelWith(ALICE);

        CommandResult result = new ArchiveCommand("alice pauline").execute(model);

        assertTrue(result.getFeedbackToUser().contains("Student archived."));
        assertTrue(findPerson(model, ALICE.getName().fullName).isArchived());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_archiveArchivedStudent_throwsException() throws Exception {
        Person archivedAlice = ALICE.withArchived(true);
        Model model = modelWith(archivedAlice);

        assertCommandFailure(new ArchiveCommand(ALICE.getName().fullName), model,
                ArchiveCommand.MESSAGE_ALREADY);
    }

    @Test
    public void execute_unarchiveArchivedStudent_restoresStudent() throws Exception {
        Person archivedAlice = ALICE.withArchived(true);
        Model model = modelWith(archivedAlice);

        CommandResult result = new UnarchiveCommand("ALICE PAULINE").execute(model);

        assertTrue(result.getFeedbackToUser().contains("Student unarchived."));
        assertFalse(findPerson(model, ALICE.getName().fullName).isArchived());
        assertTrue(model.getFilteredPersonList().contains(ALICE));
    }

    @Test
    public void execute_unarchiveActiveStudent_throwsException() throws Exception {
        Model model = modelWith(ALICE);

        assertCommandFailure(new UnarchiveCommand(ALICE.getName().fullName), model,
                UnarchiveCommand.MESSAGE_ALREADY);
    }

    @Test
    public void execute_archiveUnknownStudent_throwsException() {
        Model model = modelWith(ALICE);

        assertCommandFailure(new ArchiveCommand("Unknown Student"), model,
                ArchiveCommand.MESSAGE_NOT_FOUND);
    }

    @Test
    public void execute_unarchiveUnknownStudent_throwsException() {
        Model model = modelWith(ALICE);

        assertCommandFailure(new UnarchiveCommand("Unknown Student"), model,
                UnarchiveCommand.MESSAGE_NOT_FOUND);
    }

    @Test
    public void equals() {
        org.junit.jupiter.api.Assertions.assertEquals(new ArchiveCommand("Alice Pauline"),
                new ArchiveCommand("Alice Pauline"));
        org.junit.jupiter.api.Assertions.assertNotEquals(new ArchiveCommand("Alice Pauline"),
                new ArchiveCommand("Bob Choo"));
        org.junit.jupiter.api.Assertions.assertEquals(new UnarchiveCommand("Alice Pauline"),
                new UnarchiveCommand("Alice Pauline"));
        org.junit.jupiter.api.Assertions.assertNotEquals(new UnarchiveCommand("Alice Pauline"),
                new UnarchiveCommand("Bob Choo"));
    }

    private static Model modelWith(Person person) {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        return new ModelManager(addressBook, new UserPrefs());
    }

    private static Person findPerson(Model model, String name) {
        return model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equals(name))
                .findFirst()
                .orElseThrow();
    }
}
