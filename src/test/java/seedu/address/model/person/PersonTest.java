package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_START_DATE_BOB;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, another attribute different -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns false
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // different start date -> returns false
        editedBob = new PersonBuilder(BOB).withStartDate("20-Oct").build();
        assertFalse(BOB.isSamePerson(editedBob));

        // different outstanding amount -> returns false
        editedBob = new PersonBuilder(BOB).withOutstandingAmount("20").build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different start date -> returns false
        editedAlice = new PersonBuilder(ALICE).withStartDate(VALID_START_DATE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different outstanding amount -> returns false
        editedAlice = new PersonBuilder(ALICE).withOutstandingAmount("20").build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void notes_affectEqualityButNotDuplicateIdentity() {
        Person withNotes = new PersonBuilder(ALICE).withNotes("Revise fractions").build();
        assertTrue(ALICE.isSamePerson(withNotes));
        assertTrue(withNotes.isSamePerson(ALICE));
        assertFalse(ALICE.equals(withNotes));
        Person copy = new PersonBuilder(withNotes).build();
        assertEquals(withNotes, copy);
        assertEquals(withNotes.hashCode(), copy.hashCode());
        assertEquals(Notes.EMPTY, ALICE.getNotes());
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", address=" + ALICE.getAddress() + ", startDate=" + ALICE.getStartDate()
                + ", outstandingAmount=" + ALICE.getOutstandingAmount() + ", notes=" + ALICE.getNotes() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
