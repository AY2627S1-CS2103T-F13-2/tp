package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NotesTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Notes(null));
    }

    @Test
    public void constructor_blank_returnsEmptyNotes() {
        assertEquals(Notes.EMPTY, new Notes(""));
        assertEquals(Notes.EMPTY, new Notes(" \t\r\n "));
    }

    @Test
    public void constructor_freeText_trimsOnlySurroundingWhitespace() {
        String text = "Needs  revision: algebra, fractions & 中文.\nBring worksheets!";
        assertEquals(text, new Notes(" \t" + text + "\r\n ").value);
    }

    @Test
    public void equalsAndHashCode() {
        Notes notes = new Notes("Needs revision");
        assertEquals(notes, notes);
        assertEquals(notes, new Notes(" Needs revision "));
        assertEquals(notes.hashCode(), new Notes("Needs revision").hashCode());
        assertFalse(notes.equals(null));
        assertFalse(notes.equals("Needs revision"));
        assertFalse(notes.equals(new Notes("Different note")));
    }

    @Test
    public void toString_returnsText() {
        assertEquals("Needs revision", new Notes("Needs revision").toString());
        assertEquals("", Notes.EMPTY.toString());
    }
}
