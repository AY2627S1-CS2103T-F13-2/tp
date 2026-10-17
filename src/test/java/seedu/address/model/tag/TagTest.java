package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
        assertTrue(Tag.isValidTagName(" A-Level "));
        assertTrue(Tag.isValidTagName("Needs revision 2"));
        String[] invalidNames = {"", " ", "A_Level", "Maths!", "a/b", "你好", "two\nlines"};
        for (String name : invalidNames) {
            assertFalse(Tag.isValidTagName(name));
            assertThrows(IllegalArgumentException.class, () -> new Tag(name));
        }
    }

    @Test
    public void constructor_validTag_normalizesCaseAndSurroundingSpaces() {
        Tag tag = new Tag("  A-Level  ");
        assertEquals("a-level", tag.tagName);
        assertEquals(new Tag("a-level"), tag);
        assertEquals(new Tag("a-level").hashCode(), tag.hashCode());
        assertEquals("[a-level]", tag.toString());
        assertEquals("needs  revision", new Tag("Needs  Revision").tagName);
    }

    @Test
    public void equals_caseVariantsCollapseInSet() {
        Set<Tag> tags = new HashSet<>();
        tags.add(new Tag("A-Level"));
        tags.add(new Tag("a-level"));
        assertEquals(Set.of(new Tag("a-level")), tags);
        assertFalse(new Tag("a-level").equals(new Tag("needs revision")));
        assertFalse(new Tag("a-level").equals(null));
    }

}
