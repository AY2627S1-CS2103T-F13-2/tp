package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

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
        Path directory = Files.createTempDirectory("export-command-test");
        Path output = directory.resolve(".").resolve("contacts.csv");
        Model model = new ModelManager(addressBookWithContacts(), new UserPrefs());

        CommandResult result = new ExportCommand(() -> output).execute(model);

        assertEquals("Exported 2 contact(s) to " + output.toAbsolutePath().normalize() + ".",
                result.getFeedbackToUser());
        assertEquals(CommandResult.ListPresentation.UNCHANGED, result.getListPresentation());
        String csv = Files.readString(output);
        assertEquals(3, csv.lines().count());
        assertEquals(true, csv.contains("Alice Pauline"));
        assertEquals(true, csv.contains("Bob Choo"));
        Files.deleteIfExists(output);
        Files.deleteIfExists(directory);
    }

    @Test
    public void execute_writeFailure_throwsCommandException() throws IOException {
        Path directory = Files.createTempDirectory("export-command-test");
        Path output = directory.resolve("missing").resolve("contacts.csv");
        Model model = new ModelManager(addressBookWithContacts(), new UserPrefs());

        assertThrows(CommandException.class, () -> new ExportCommand(() -> output).execute(model));
        Files.deleteIfExists(directory);
    }

    @Test
    public void generateOutputPath_noCollision_returnsTimestampedPath() throws IOException {
        Path directory = Files.createTempDirectory("export-command-test");
        Path output = ExportCommand.generateOutputPath(LocalDateTime.of(2026, 10, 21, 14, 30, 12, 123_000_000),
                directory);

        assertEquals(directory.resolve("contacts-20261021-143012-123.csv"), output);
        Files.deleteIfExists(directory);
    }

    @Test
    public void execute_collision_retriesWithSuffixedPath() throws Exception {
        Path directory = Files.createTempDirectory("export-command-test");
        Path existing = directory.resolve("contacts-20261021-143012-123.csv");
        Files.createFile(existing);
        Model model = new ModelManager(addressBookWithContacts(), new UserPrefs());

        CommandResult result = new ExportCommand(() -> existing).execute(model);
        Path output = directory.resolve("contacts-20261021-143012-123-1.csv");

        assertEquals("Exported 2 contact(s) to " + output.toAbsolutePath() + ".", result.getFeedbackToUser());
        assertEquals(3, Files.readString(output).lines().count());
        Files.deleteIfExists(existing);
        Files.deleteIfExists(output);
        Files.deleteIfExists(directory);
    }

    @Test
    public void execute_collisionWithoutExtension_retriesWithSuffixedPath() throws Exception {
        Path directory = Files.createTempDirectory("export-command-test");
        Path existing = directory.resolve("contacts");
        Files.createFile(existing);
        Model model = new ModelManager(addressBookWithContacts(), new UserPrefs());

        CommandResult result = new ExportCommand(() -> existing).execute(model);
        Path output = directory.resolve("contacts-1");

        assertEquals("Exported 2 contact(s) to " + output.toAbsolutePath() + ".", result.getFeedbackToUser());
        assertEquals(3, Files.readString(output).lines().count());
        Files.deleteIfExists(existing);
        Files.deleteIfExists(output);
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
