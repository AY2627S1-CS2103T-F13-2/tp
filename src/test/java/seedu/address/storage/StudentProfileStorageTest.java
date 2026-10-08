package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StudentDetails;

public class StudentProfileStorageTest {
    @TempDir
    public Path folder;

    @Test
    public void saveAndReload_mixedLegacyAndStudentProfiles_preservesEveryField() throws Exception {
        StudentDetails details = adapted("20.00", "21", "A-Level", "91234567", "Bring  worksheets!").toModelType();
        Person student = new Person(new Name("Peter Parker"), new Phone("12345678"), new Address("Clementi"), details);
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(student);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("profiles.json"));
        storage.saveAddressBook(book);
        assertEquals(book, new AddressBook(storage.readAddressBook().orElseThrow()));
        assertTrue(ALICE.getStudentDetails().isEmpty());
        assertTrue(student.getEmail().isEmpty());
    }

    @Test
    public void read_missingOptionalFields_usesAgreedDefaults() throws Exception {
        String json = "{\"subjects\":[\"Maths\"],\"startDate\":\"10-Aug\"}";
        StudentDetails details = JsonUtil.fromJsonString(json, JsonAdaptedStudentDetails.class).toModelType();
        assertEquals("0.00", details.getOutstandingAmount().toString());
        assertEquals("", details.getAge().value);
        assertEquals("", details.getEducation().value);
        assertEquals("", details.getGuardianContact().value);
        assertEquals("", details.getNote().value);
    }

    @Test
    public void read_invalidStudentDetails_rejectsRecord() {
        List<JsonAdaptedStudentDetails> invalid = List.of(
                adapted("-1", "", "", "", ""),
                adapted("0", "121", "", "", ""),
                adapted("0", "", "", "123", ""),
                adapted("0", "", "", "", "two\nlines"),
                new JsonAdaptedStudentDetails(null, "10-Aug", null, null, null, null, null),
                new JsonAdaptedStudentDetails(List.of(), "10-Aug", null, null, null, null, null),
                new JsonAdaptedStudentDetails(List.of("Maths"), null, null, null, null, null, null),
                new JsonAdaptedStudentDetails(List.of("Maths"), "10-Aug-2026", null, null, null, null, null),
                new JsonAdaptedStudentDetails(Arrays.asList("Maths", null), "10-Aug", null, null, null, null, null));
        for (JsonAdaptedStudentDetails record : invalid) {
            assertThrows(IllegalValueException.class, record::toModelType);
        }
    }

    @Test
    public void read_duplicateNormalizedStudents_rejectsBook() throws Exception {
        Person first = new Person(new Name("Peter"), new Phone("91234567"), new Address("Clementi"),
                adapted("20", "", "", "", "").toModelType());
        Person second = new Person(new Name(" Peter "), new Phone("91234567"), new Address("Clementi"),
                adapted("20.00", "", "", "", "").toModelType());
        JsonSerializableAddressBook json = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(first), new JsonAdaptedPerson(second)));
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                json::toModelType);
    }

    private JsonAdaptedStudentDetails adapted(String amount, String age, String education,
            String guardian, String note) {
        return new JsonAdaptedStudentDetails(List.of("Maths"), "10-Aug", amount, age, education, guardian, note);
    }
}
