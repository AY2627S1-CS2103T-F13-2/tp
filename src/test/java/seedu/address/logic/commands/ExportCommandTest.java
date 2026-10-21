package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.PersonBuilder;

/** Tests exporting contacts to CSV files. */
public class ExportCommandTest {

    @Test
    public void execute_exportsActiveAndArchivedContacts() throws Exception {
        Path output = Files.createTempFile("export-command-test", ".csv");
        Model model = new ModelManager(addressBookWithContacts(), new UserPrefs());

        CommandResult result = new ExportCommand(() -> output).execute(model);

        assertEquals("Exported 2 contact(s) to " + output.toAbsolutePath() + ".", result.getFeedbackToUser());
        String csv = Files.readString(output);
        assertEquals(3, csv.lines().count());
        assertEquals(true, csv.contains("Alice Pauline"));
        assertEquals(true, csv.contains("Bob Choo"));
        Files.deleteIfExists(output);
    }

    @Test
    public void execute_writeFailure_throwsCommandException() throws IOException {
        Path directory = Files.createTempDirectory("export-command-test");
        Model model = new ModelManager(addressBookWithContacts(), new UserPrefs());

        assertThrows(CommandException.class, () -> new ExportCommand(() -> directory).execute(model));
        Files.deleteIfExists(directory);
    }

    private static AddressBook addressBookWithContacts() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder().withName("Alice Pauline").build());
        addressBook.addPerson(new PersonBuilder().withName("Bob Choo").withPhone("22222222").build()
                .withArchived(true));
        return addressBook;
    }
}
