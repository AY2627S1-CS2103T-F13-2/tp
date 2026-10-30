package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Name;
import seedu.address.model.person.Notes;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StartDate;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_START_DATE = "10-Aug-2026";
    private static final String INVALID_OUTSTANDING_AMOUNT = "10.123";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final String VALID_START_DATE = BENSON.getStartDate().toString();
    private static final String VALID_OUTSTANDING_AMOUNT = BENSON.getOutstandingAmount().toString();

    @Test
    public void toModelType_internationalNames_roundTrips() throws Exception {
        for (String name : new String[] {"Anne-Marie", "O'Connor", "O’Connor", "José", "Jose\u0301", "张伟"}) {
            Person person = new PersonBuilder(BENSON).withName(name).build();
            assertEquals(person, new JsonAdaptedPerson(person).toModelType());
        }
    }

    @Test
    public void toModelType_notes_roundTrips() throws Exception {
        Person withNotes = new PersonBuilder(BENSON).withNotes("Needs  revision: fractions!").build();
        assertEquals(withNotes, new JsonAdaptedPerson(withNotes).toModelType());
    }

    @Test
    public void toModelType_missingNotes_defaultsToEmpty() throws Exception {
        JsonAdaptedPerson legacy = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_START_DATE, VALID_OUTSTANDING_AMOUNT);
        assertEquals(Notes.EMPTY, legacy.toModelType().getNotes());
    }

    @Test
    public void toModelType_blankNotes_defaultsToEmpty() throws Exception {
        JsonAdaptedPerson blank = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_START_DATE, VALID_OUTSTANDING_AMOUNT, " \t ");
        assertEquals(Notes.EMPTY, blank.toModelType().getNotes());
    }

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_archivedPerson_preservesArchiveState() throws Exception {
        Person archived = BENSON.withArchived(true);
        assertTrue(new JsonAdaptedPerson(archived).toModelType().isArchived());
    }

    @Test
    public void toModelType_missingArchiveState_defaultsToActive() throws Exception {
        JsonAdaptedPerson legacy = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_START_DATE, VALID_OUTSTANDING_AMOUNT, null, null, null, null);
        assertTrue(!legacy.toModelType().isArchived());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_ADDRESS, VALID_START_DATE,
                        VALID_OUTSTANDING_AMOUNT);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_ADDRESS, VALID_START_DATE,
                VALID_OUTSTANDING_AMOUNT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_ADDRESS, VALID_START_DATE,
                        VALID_OUTSTANDING_AMOUNT);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_ADDRESS, VALID_START_DATE,
                VALID_OUTSTANDING_AMOUNT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidStartDate_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS, INVALID_START_DATE,
                        VALID_OUTSTANDING_AMOUNT);
        String expectedMessage = StartDate.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullStartDate_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS, null,
                VALID_OUTSTANDING_AMOUNT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, StartDate.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_ADDRESS, VALID_START_DATE,
                        VALID_OUTSTANDING_AMOUNT);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_START_DATE,
                VALID_OUTSTANDING_AMOUNT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidOutstandingAmount_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS, VALID_START_DATE,
                INVALID_OUTSTANDING_AMOUNT);
        assertThrows(IllegalValueException.class, OutstandingAmount.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullOutstandingAmount_defaultsToZero() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_START_DATE, null);
        assertEquals(OutstandingAmount.ZERO, person.toModelType().getOutstandingAmount());
    }

    @Test
    public void toModelType_missingSubjects_loadsLegacyProfile() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_START_DATE, VALID_OUTSTANDING_AMOUNT);
        assertTrue(person.toModelType().getSubjects().isEmpty());
    }

    @Test
    public void toModelType_invalidSubject_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_START_DATE, VALID_OUTSTANDING_AMOUNT, null, null,
                List.of(new JsonAdaptedSubject("Maths", "MONDAY", "09:00"),
                        new JsonAdaptedSubject("Invalid!", "TUESDAY", "10:00")));
        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullSubject_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_START_DATE, VALID_OUTSTANDING_AMOUNT, null, null,
                Arrays.asList(new JsonAdaptedSubject("Maths", "MONDAY", "09:00"), null));
        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_duplicateSubjectNames_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_START_DATE, VALID_OUTSTANDING_AMOUNT, null, null,
                List.of(new JsonAdaptedSubject("Maths", "MONDAY", "09:00"),
                        new JsonAdaptedSubject("maths", "TUESDAY", "10:00")));
        assertThrows(IllegalValueException.class, "Subject names must be unique for each student.",
                person::toModelType);
    }

    @Test
    public void toModelType_missingTags_defaultsToEmptySet() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_START_DATE, VALID_OUTSTANDING_AMOUNT);
        assertEquals(Set.of(), person.toModelType().getTags());
    }

    @Test
    public void toModelType_tagCaseVariants_normalizesAndCombines() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_START_DATE, VALID_OUTSTANDING_AMOUNT,
                List.of(new JsonAdaptedTag(" A-Level "), new JsonAdaptedTag("a-level")));
        assertEquals(Set.of(new Tag("a-level")), person.toModelType().getTags());
    }

    @Test
    public void toModelType_invalidStoredTags_throwsIllegalValueException() {
        for (String name : Arrays.asList("", " ", "invalid!", null)) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                    VALID_START_DATE, VALID_OUTSTANDING_AMOUNT, List.of(new JsonAdaptedTag(name)));
            assertThrows(IllegalValueException.class, Tag.MESSAGE_CONSTRAINTS, person::toModelType);
        }
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_START_DATE, VALID_OUTSTANDING_AMOUNT, Arrays.asList((JsonAdaptedTag) null));
        assertThrows(IllegalValueException.class, Tag.MESSAGE_CONSTRAINTS, person::toModelType);
    }

}
