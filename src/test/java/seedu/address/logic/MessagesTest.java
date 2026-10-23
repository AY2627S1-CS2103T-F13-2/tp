package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void formatTags_emptyTags_showsNotSpecified() {
        assertEquals("Not specified", Messages.formatTags(ALICE));
    }

    @Test
    public void formatTags_multipleTags_sortsStoredLabels() {
        var person = new PersonBuilder(ALICE).withTags("Needs revision", "A-Level").build();
        assertEquals("a-level, needs revision", Messages.formatTags(person));
    }

    @Test
    public void formatSubjects_legacyProfile_showsNotSpecified() {
        assertEquals("Not specified", Messages.formatSubjects(ALICE));
        assertTrue(Messages.format(ALICE).endsWith("; Subjects: Not specified"));
    }

    @Test
    public void formatSubjects_multipleSubjects_preservesInputOrder() {
        var person = new PersonBuilder(ALICE).withSubjects("Maths", "H2 Chemistry").build();
        assertEquals("Maths (Mon 09:00), H2 Chemistry (Mon 09:00)", Messages.formatSubjects(person));
        assertTrue(Messages.format(person)
                .endsWith("; Subjects: Maths (Mon 09:00), H2 Chemistry (Mon 09:00)"));
    }
}
