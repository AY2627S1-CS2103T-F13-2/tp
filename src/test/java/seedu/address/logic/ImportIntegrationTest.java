package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.CsvExporter;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests that students exported by one copy of TuteeDex can be imported into another and are saved. */
public class ImportIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;
    private Path importFolder;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        logic.execute("add n/Amy Bee p/11111111 a/Block 1, #01-01 d/10-Aug s/Maths day/MON time/09:00"
                + " t/A-Level notes/Revise \"fractions\", then graphs");
        logic.execute("add n/Bob Choo p/22222222 a/Block 2 d/19-Sep s/Physics day/TUE time/10:00"
                + " s/H2 Chemistry day/WED time/11:30 o/120.50");
        logic.execute("archive s/Bob Choo");

        // Import only reads files inside the folder TuteeDex runs from, so the CSV files go in a folder under it.
        Files.createDirectories(Path.of("build"));
        importFolder = Files.createTempDirectory(Path.of("build"), "import-test");
    }

    @AfterEach
    public void tearDown() throws Exception {
        try (Stream<Path> paths = Files.walk(importFolder)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }

    @Test
    public void execute_exportedFile_importsIdenticalStudentsAndSaves() throws Exception {
        List<Person> exportedStudents = List.copyOf(model.getAddressBook().getPersonList());
        Path file = importFolder.resolve("contacts.csv");
        Files.writeString(file, CsvExporter.format(exportedStudents));

        Model newModel = new ModelManager();
        JsonAddressBookStorage newStorage = new JsonAddressBookStorage(temporaryFolder.resolve("new.json"));
        Logic newLogic = new LogicManager(newModel, new StorageManager(newStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("newprefs.json"))));

        assertTrue(newLogic.execute("import " + file).getFeedbackToUser().startsWith("Ready to import 2 student(s)"));
        assertTrue(newModel.getAddressBook().getPersonList().isEmpty());

        assertEquals("Imported 2 student(s) from " + file + ".",
                newLogic.execute("import " + file + " confirm").getFeedbackToUser());
        assertEquals(exportedStudents, newModel.getAddressBook().getPersonList());
        assertEquals(exportedStudents, newStorage.readAddressBook().orElseThrow().getPersonList());
        assertEquals(List.of(exportedStudents.get(0)), newModel.getFilteredPersonList());
    }

    @Test
    public void execute_sameFileTwice_addsNothingSecondTime() throws Exception {
        Path file = importFolder.resolve("contacts.csv");
        Files.writeString(file, CsvExporter.format(model.getAddressBook().getPersonList()));
        List<Person> before = List.copyOf(model.getAddressBook().getPersonList());

        assertEquals("No new students to import. All 2 student(s) in " + file + " are already in TuteeDex.",
                logic.execute("import " + file + " confirm").getFeedbackToUser());
        assertEquals(before, model.getAddressBook().getPersonList());
    }
}
