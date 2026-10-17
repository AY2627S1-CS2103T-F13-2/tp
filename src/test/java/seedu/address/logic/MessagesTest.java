package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void formatSubjects_legacyProfile_showsNotSpecified() {
        assertEquals("Not specified", Messages.formatSubjects(ALICE));
        assertTrue(Messages.format(ALICE).endsWith("; Subjects: Not specified"));
    }

    @Test
    public void formatSubjects_multipleSubjects_preservesInputOrder() {
        var person = new PersonBuilder(ALICE).withSubjects("Maths", "H2 Chemistry").build();
        assertEquals("Maths, H2 Chemistry", Messages.formatSubjects(person));
        assertTrue(Messages.format(person).endsWith("; Subjects: Maths, H2 Chemistry"));
    }
}
