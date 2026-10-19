package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_START_DATE_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
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
    public void tags_areDataButDoNotDistinguishDuplicateProfiles() {
        Person first = new PersonBuilder(ALICE).withTags("A-Level").build();
        Person second = new PersonBuilder(ALICE).withTags("Needs revision").build();
        assertTrue(first.isSamePerson(second));
        assertTrue(first.isSamePerson(ALICE));
        assertFalse(first.equals(second));
        assertFalse(first.equals(ALICE));
    }

    @Test
    public void tags_caseAndOrderDoNotAffectEquality() {
        Person first = new PersonBuilder(ALICE).withTags("A-Level", "Needs revision").build();
        Person second = new PersonBuilder(ALICE).withTags("NEEDS REVISION", "a-level", "A-Level").build();
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void tags_defensiveCopyAndImmutableAccess() {
        Set<Tag> tags = new HashSet<>(Set.of(new Tag("A-Level")));
        Person person = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getAddress(), ALICE.getStartDate(),
                ALICE.getOutstandingAmount(), tags);
        tags.clear();
        assertEquals(Set.of(new Tag("a-level")), person.getTags());
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().clear());
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", address=" + ALICE.getAddress() + ", startDate=" + ALICE.getStartDate()
                + ", outstandingAmount=" + ALICE.getOutstandingAmount() + ", tags=" + ALICE.getTags()
                + ", notes=" + ALICE.getNotes() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
