package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.util.SampleDataUtil;

/** Verifies that the first-run examples retain their teaching details when saved. */
public class SampleDataStorageTest {
    @TempDir
    public Path testFolder;

    @Test
    public void sampleData_saveAndReload_preservesLessonsBalancesTagsAndNotes() throws Exception {
        ReadOnlyAddressBook sample = SampleDataUtil.getSampleAddressBook();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("sample.json"));
        storage.saveAddressBook(sample);
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();

        assertEquals(new AddressBook(sample), new AddressBook(loaded));
        assertEquals(6, loaded.getPersonList().size());
        assertTrue(loaded.getPersonList().stream().allMatch(person -> !person.getSubjects().isEmpty()));
        assertTrue(loaded.getPersonList().stream().allMatch(person -> !person.getTags().isEmpty()));
        assertTrue(loaded.getPersonList().stream().anyMatch(person -> !person.getNotes().value.isEmpty()));
        assertEquals(3, loaded.getPersonList().stream()
                .filter(person -> person.getOutstandingAmount().equals(OutstandingAmount.ZERO)).count());
        assertFalse(loaded.getPersonList().stream().anyMatch(person -> person.isArchived()));
    }
}
