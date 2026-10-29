package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_malformedStructures_throwsDataLoadingExceptionAndPreservesFile() throws IOException {
        String validPerson = JsonUtil.toJsonString(new JsonAdaptedPerson(ALICE));
        String[] invalidDocuments = {"null", "{\"persons\":[null]}",
            "{\"persons\":[" + validPerson + ",null]}", "{\"persons\":[null," + validPerson + "]}",
            "{\"persons\":null}", "{}", "{\"persons\":\"invalid\"}", "{\"persons\":[{}]}",
            "{\"persons\":[" + validPerson + "," + validPerson + "]}"};
        Path file = testFolder.resolve("invalid.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);

        for (String document : invalidDocuments) {
            Files.writeString(file, document);

            assertThrows(DataLoadingException.class, storage::readAddressBook);

            assertEquals(document, Files.readString(file));
        }
    }

    @Test
    public void readAddressBook_emptyPersons_loadsEmptyBookAndPreservesFile() throws Exception {
        Path file = testFolder.resolve("empty.json");
        String document = "{\"persons\":[]}";
        Files.writeString(file, document);

        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(file).readAddressBook().orElseThrow();

        assertEquals(new AddressBook(), new AddressBook(loaded));
        assertEquals(document, Files.readString(file));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void readAndSaveAddressBook_tags_roundTrip() throws Exception {
        AddressBook addressBook = new AddressBook();
        Person person = new PersonBuilder(ALICE).withTags("A-Level", "Needs revision", "a-level").build();
        addressBook.addPerson(person);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("tagged.json"));
        storage.saveAddressBook(addressBook);
        assertEquals(addressBook, new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void readAndSaveAddressBook_legacySubjectSpaces_normalizesWithoutRewritingOnRead() throws Exception {
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder(ALICE).withSubjects("H2 Maths").build());
        Path file = testFolder.resolve("spaced-subject.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(original);
        String legacyJson = Files.readString(file).replace("H2 Maths", "  H2   Maths  ");
        Files.writeString(file, legacyJson);

        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        assertEquals(original, new AddressBook(loaded));
        assertEquals(legacyJson, Files.readString(file));
        storage.saveAddressBook(loaded);
        assertEquals("H2 Maths", storage.readAddressBook().orElseThrow().getPersonList().get(0)
                .getSubjects().iterator().next().value);
        assertFalse(Files.readString(file).contains("H2   Maths"));
    }

    @Test
    public void readAddressBook_normalizedSubjectCollisions_rejectsWithoutDroppingLessons() throws Exception {
        Path file = testFolder.resolve("colliding-subjects.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (String secondDay : List.of("MONDAY", "TUESDAY")) {
            List<JsonAdaptedSubject> subjects = List.of(new JsonAdaptedSubject("H2 Maths", "MONDAY", "09:00"),
                    new JsonAdaptedSubject("h2  Maths", secondDay, "09:00"));
            JsonAdaptedPerson person = new JsonAdaptedPerson("Amy Bee", "11111111", "Block 1", "10-Aug", "0",
                    List.of(), "Keep note", subjects);
            String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(List.of(person)));
            Files.writeString(file, json);

            assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
            assertEquals(json, Files.readString(file));
        }
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
