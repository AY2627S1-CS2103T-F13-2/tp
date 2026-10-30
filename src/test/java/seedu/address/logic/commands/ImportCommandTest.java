package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.CsvExporter;
import seedu.address.commons.util.CsvReader;
import seedu.address.logic.parser.CsvPersonParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

public class ImportCommandTest {

    private static final Person AMY = new PersonBuilder().withName("Amy Bee").withPhone("11111111")
            .withAddress("Block 1").withSubjects("Maths").withTags("a-level").build();
    private static final Person BOB = new PersonBuilder().withName("Bob Choo").withPhone("22222222")
            .withAddress("Blk 2, #02-03").withStartDate("19-Sep").withOutstandingAmount("120.50")
            .withSubjects("Physics", "H2 Chemistry").withNotes("Weak at \"moles\", needs practice").build();
    private static final Person CARA = new PersonBuilder().withName("Cara Lim").withPhone("33333333")
            .withAddress("Block 3").withSubjects("English").build().withArchived(true);

    @TempDir
    public Path temporaryFolder;

    private Model model;

    @Test
    public void execute_balanceOnlyDuplicateWithinFile_previewAndConfirmRejectAll() throws Exception {
        Person duplicate = new PersonBuilder(AMY).withOutstandingAmount("20").build();
        Path file = writeFile("students.csv", CsvExporter.format(List.of(AMY, BOB, duplicate)));
        String expected = String.format(ImportCommand.MESSAGE_ERRORS, file) + "\n"
                + String.format(ImportCommand.MESSAGE_ROW_ERROR, 4,
                        String.format(ImportCommand.MESSAGE_DUPLICATE_IN_FILE, 2));
        assertCommandFailure(new ImportCommand(file, false, temporaryFolder), model, expected);
        assertCommandFailure(new ImportCommand(file, true, temporaryFolder), model, expected);
    }

    @Test
    public void execute_storedBalanceConflict_previewAndConfirmRejectAll() throws Exception {
        Person duplicate = new PersonBuilder(CARA).withOutstandingAmount("20").build().withArchived(true);
        Path file = writeFile("students.csv", CsvExporter.format(List.of(AMY, duplicate)));
        String expected = String.format(ImportCommand.MESSAGE_ERRORS, file) + "\n"
                + String.format(ImportCommand.MESSAGE_ROW_ERROR, 3,
                        String.format(ImportCommand.MESSAGE_CONFLICT, CARA.getName()));
        assertCommandFailure(new ImportCommand(file, false, temporaryFolder), model, expected);
        assertCommandFailure(new ImportCommand(file, true, temporaryFolder), model, expected);
    }

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(CARA);
        model = new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_preview_showsStudentsAndChangesNothing() throws Exception {
        Path file = writeFile("students.csv", CsvExporter.format(List.of(AMY, BOB)));
        String expectedMessage = String.format(ImportCommand.MESSAGE_PREVIEW, 2, file, "Amy Bee, Bob Choo")
                + "\n" + String.format(ImportCommand.MESSAGE_CONFIRM_HINT, file);
        assertCommandSuccess(new ImportCommand(file, false, temporaryFolder), model, expectedMessage, copyOf(model));
    }

    @Test
    public void execute_confirm_addsToExistingStudents() throws Exception {
        Path file = writeFile("students.csv", CsvExporter.format(List.of(AMY, BOB)));
        Model expectedModel = copyOf(model);
        expectedModel.addPerson(AMY);
        expectedModel.addPerson(BOB);
        expectedModel.updateFilteredPersonList(Model.PREDICATE_SHOW_ACTIVE_PERSONS);

        assertCommandSuccess(new ImportCommand(file, true, temporaryFolder), model,
                String.format(ImportCommand.MESSAGE_SUCCESS, 2, file), expectedModel);
        assertEquals(List.of(CARA, AMY, BOB), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_exactCopyOfStoredStudent_skipped() throws Exception {
        Path file = writeFile("students.csv", CsvExporter.format(List.of(CARA, AMY)));
        Model expectedModel = copyOf(model);
        expectedModel.addPerson(AMY);
        expectedModel.updateFilteredPersonList(Model.PREDICATE_SHOW_ACTIVE_PERSONS);

        String expectedMessage = String.format(ImportCommand.MESSAGE_SUCCESS, 1, file) + "\n"
                + String.format(ImportCommand.MESSAGE_SKIPPED, 1, "Cara Lim");
        assertCommandSuccess(new ImportCommand(file, true, temporaryFolder), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_onlyStoredStudents_reportsNothingNew() throws Exception {
        Path file = writeFile("students.csv", CsvExporter.format(List.of(CARA)));
        assertCommandSuccess(new ImportCommand(file, true, temporaryFolder), model,
                String.format(ImportCommand.MESSAGE_NOTHING_NEW, 1, file), copyOf(model));
    }

    @Test
    public void execute_storedStudentWithDifferentDetails_importsNothing() throws Exception {
        Person changedCara = new PersonBuilder(CARA).withNotes("New note").build().withArchived(true);
        Path file = writeFile("students.csv", CsvExporter.format(List.of(AMY, changedCara)));
        assertCommandFailure(new ImportCommand(file, true, temporaryFolder), model,
                String.format(ImportCommand.MESSAGE_ERRORS, file) + "\n"
                        + String.format(ImportCommand.MESSAGE_ROW_ERROR, 3,
                                String.format(ImportCommand.MESSAGE_CONFLICT, "Cara Lim")));
    }

    @Test
    public void execute_exactCopyWithinFile_skippedSilently() throws Exception {
        Path file = writeFile("students.csv", CsvExporter.format(List.of(AMY, AMY)));
        Model expectedModel = copyOf(model);
        expectedModel.addPerson(AMY);
        expectedModel.updateFilteredPersonList(Model.PREDICATE_SHOW_ACTIVE_PERSONS);
        assertCommandSuccess(new ImportCommand(file, true, temporaryFolder), model,
                String.format(ImportCommand.MESSAGE_SUCCESS, 1, file), expectedModel);
    }

    @Test
    public void execute_sameStudentTwiceWithDifferentDetails_importsNothing() throws Exception {
        Person changedAmy = new PersonBuilder(AMY).withTags("needs revision").build();
        Path file = writeFile("students.csv", CsvExporter.format(List.of(AMY, BOB, changedAmy)));
        assertCommandFailure(new ImportCommand(file, true, temporaryFolder), model,
                String.format(ImportCommand.MESSAGE_ERRORS, file) + "\n"
                        + String.format(ImportCommand.MESSAGE_ROW_ERROR, 4,
                                String.format(ImportCommand.MESSAGE_DUPLICATE_IN_FILE, 2)));
    }

    @Test
    public void execute_invalidRows_reportsEveryRowAndImportsNothing() throws Exception {
        String text = CsvExporter.HEADER + "\n"
                + "Amy Bee,11111111,Block 1,10-Aug,0,Maths (Mon 09:00),,,false\n"
                + "\n"
                + "Bob Choo,123,Block 2,19-Sep,0,Physics (Tue 10:00),,,false\n"
                + "Cara Lim,33333333,Block 3,03-Mar,0,Physics,,,false\n"
                + "Dan Koh,44444444,Block 4,04-Apr,0\n";
        Path file = writeFile("students.csv", text);
        String expectedMessage = String.format(ImportCommand.MESSAGE_ERRORS, file)
                + "\nRow 4: " + Phone.MESSAGE_CONSTRAINTS
                + "\nRow 5: " + CsvPersonParser.MESSAGE_INVALID_SUBJECTS
                + "\nRow 6: " + String.format(CsvPersonParser.MESSAGE_WRONG_COLUMN_COUNT, 5);
        assertCommandFailure(new ImportCommand(file, true, temporaryFolder), model, expectedMessage);
        assertCommandFailure(new ImportCommand(file, false, temporaryFolder), model, expectedMessage);
    }

    @Test
    public void execute_manyInvalidRows_showsFirstTenErrors() throws Exception {
        StringBuilder text = new StringBuilder(CsvExporter.HEADER + "\n");
        List<String> expectedErrors = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            text.append("Bad Phone,123,Block 1,10-Aug,0,Maths (Mon 09:00),,,false\n");
            expectedErrors.add("Row " + (i + 2) + ": " + Phone.MESSAGE_CONSTRAINTS);
        }
        Path file = writeFile("students.csv", text.toString());
        String expectedMessage = String.format(ImportCommand.MESSAGE_ERRORS, file) + "\n"
                + String.join("\n", expectedErrors.subList(0, 10)) + "\n"
                + String.format(ImportCommand.MESSAGE_MORE_ERRORS, 2);
        assertCommandFailure(new ImportCommand(file, false, temporaryFolder), model, expectedMessage);
    }

    @Test
    public void execute_badFiles_throwCommandException() throws Exception {
        Path missing = temporaryFolder.resolve("missing.csv");
        assertCommandFailure(new ImportCommand(missing, false, temporaryFolder), model,
                String.format(ImportCommand.MESSAGE_FILE_NOT_FOUND, missing.toAbsolutePath().normalize()));
        assertCommandFailure(new ImportCommand(temporaryFolder, false, temporaryFolder), model,
                String.format(ImportCommand.MESSAGE_FILE_NOT_FOUND, temporaryFolder.toAbsolutePath().normalize()));
        assertCommandFailure(new ImportCommand(writeFile("students.xlsx", "x"), false, temporaryFolder), model,
                ImportCommand.MESSAGE_NOT_CSV);
        assertCommandFailure(new ImportCommand(writeFile("empty.csv", ""), false, temporaryFolder), model,
                ImportCommand.MESSAGE_WRONG_HEADER);
        Path wrongHeader = writeFile("header.csv", "Name,Phone\nAmy,1\n");
        assertCommandFailure(new ImportCommand(wrongHeader, false, temporaryFolder), model,
                ImportCommand.MESSAGE_WRONG_HEADER);

        Path headerOnly = writeFile("header-only.csv", CsvExporter.HEADER + "\n\n");
        assertCommandFailure(new ImportCommand(headerOnly, false, temporaryFolder), model,
                String.format(ImportCommand.MESSAGE_NO_STUDENTS, headerOnly));

        Path badQuote = writeFile("quote.csv", CsvExporter.HEADER + "\n\"Amy Bee,1\n");
        assertCommandFailure(new ImportCommand(badQuote, false, temporaryFolder), model,
                String.format(ImportCommand.MESSAGE_ERRORS, badQuote) + "\n"
                        + String.format(CsvReader.MESSAGE_UNCLOSED_QUOTE, 2));

        // A note typed as Needs "revision" without wrapping quotes must not lose its quotes silently.
        Path strayQuote = writeFile("stray.csv", CsvExporter.HEADER + "\n"
                + "Amy Bee,11111111,Block 1,10-Aug,0,Maths (Mon 09:00),,Needs \"revision\",false\n");
        assertCommandFailure(new ImportCommand(strayQuote, true, temporaryFolder), model,
                String.format(ImportCommand.MESSAGE_ERRORS, strayQuote) + "\n"
                        + String.format(CsvReader.MESSAGE_STRAY_QUOTE, 2));

        Path notUtf8 = temporaryFolder.resolve("latin1.csv");
        Files.write(notUtf8, (CsvExporter.HEADER + "\nJosé,1").getBytes(StandardCharsets.ISO_8859_1));
        assertCommandFailure(new ImportCommand(notUtf8, false, temporaryFolder), model,
                String.format(ImportCommand.MESSAGE_READ_FAILURE, notUtf8));
    }

    @Test
    public void execute_fileOutsideHomeFolder_rejected() throws Exception {
        Path homeFolder = Files.createDirectory(temporaryFolder.resolve("home"));
        Path outsideFile = writeFile("outside.csv", CsvExporter.format(List.of(AMY)));
        String expectedMessage = String.format(ImportCommand.MESSAGE_OUTSIDE_HOME_FOLDER, homeFolder);

        assertCommandFailure(new ImportCommand(outsideFile, true, homeFolder), model, expectedMessage);
        assertCommandFailure(new ImportCommand(Path.of("..", "outside.csv"), true, homeFolder), model,
                expectedMessage);
        assertCommandFailure(new ImportCommand(Path.of("sub", "..", "..", "outside.csv"), false, homeFolder), model,
                expectedMessage);
    }

    @Test
    public void execute_linkLeadingOutOfHomeFolder_rejected() throws Exception {
        Path homeFolder = Files.createDirectory(temporaryFolder.resolve("home"));
        Path outsideFile = writeFile("outside.csv", CsvExporter.format(List.of(AMY)));
        Path link = homeFolder.resolve("link.csv");
        try {
            Files.createSymbolicLink(link, outsideFile);
        } catch (IOException | UnsupportedOperationException e) {
            // Windows needs extra permissions to create links, so this check runs on the other platforms.
            Assumptions.assumeTrue(false, "Symbolic links are not available here");
        }
        assertCommandFailure(new ImportCommand(Path.of("link.csv"), true, homeFolder), model,
                String.format(ImportCommand.MESSAGE_OUTSIDE_HOME_FOLDER, homeFolder));
    }

    @Test
    public void execute_relativePathInsideHomeFolder_imported() throws Exception {
        Path subFolder = Files.createDirectory(temporaryFolder.resolve("term 4"));
        Files.writeString(subFolder.resolve("students.csv"), CsvExporter.format(List.of(AMY)));
        Path relativePath = Path.of("term 4", "students.csv");

        new ImportCommand(relativePath, true, temporaryFolder).execute(model);
        assertEquals(List.of(CARA, AMY), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_excelStyleFile_imported() throws Exception {
        // Excel adds a byte order mark and CRLF line endings; header case should not matter either.
        String text = "\uFEFF" + CsvExporter.format(List.of(AMY)).replace("\n", "\r\n")
                .replace(CsvExporter.HEADER, CsvExporter.HEADER.toUpperCase());
        Path file = writeFile("excel.csv", text);
        new ImportCommand(file, true, temporaryFolder).execute(model);
        assertEquals(List.of(CARA, AMY), model.getAddressBook().getPersonList());
    }

    @Test
    public void formatNames_moreThanTen_summarisesRest() {
        List<Person> students = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            students.add(AMY);
        }
        assertEquals(String.join(", ", Collections.nCopies(10, "Amy Bee")) + " and 2 more",
                ImportCommand.formatNames(students));
    }

    @Test
    public void equals() {
        ImportCommand command = new ImportCommand(Path.of("a.csv"), false);
        assertEquals(command, command);
        assertEquals(command, new ImportCommand(Path.of("a.csv"), false));
        assertNotEquals(command, new ImportCommand(Path.of("b.csv"), false));
        assertNotEquals(command, new ImportCommand(Path.of("a.csv"), true));
        assertNotEquals(command, new ImportCommand(Path.of("a.csv"), false, temporaryFolder));
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand(true));
    }

    @Test
    public void toStringMethod() {
        assertEquals(ImportCommand.class.getCanonicalName() + "{filePath=a.csv, isConfirmed=true}",
                new ImportCommand(Path.of("a.csv"), true).toString());
    }

    private Path writeFile(String name, String content) throws IOException {
        Path file = temporaryFolder.resolve(name);
        Files.writeString(file, content);
        return file;
    }

    @Test
    public void requiresAddressBookSave_onlyWhenConfirmed() {
        assertTrue(new ImportCommand(Path.of("students.csv"), true).requiresAddressBookSave());
        assertFalse(new ImportCommand(Path.of("students.csv"), false).requiresAddressBookSave());
    }

    private static Model copyOf(Model model) {
        return new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
    }
}
