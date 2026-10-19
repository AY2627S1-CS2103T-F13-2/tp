package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class PersonHasAnyTagPredicateTest {
    @Test
    public void test_matchesAnyCompleteNormalizedTag() {
        PersonHasAnyTagPredicate predicate = new PersonHasAnyTagPredicate(
                Set.of(new Tag(" A-Level "), new Tag("Needs revision")));
        assertTrue(predicate.test(new PersonBuilder().withTags("a-level").build()));
        assertTrue(predicate.test(new PersonBuilder().withTags("needs revision", "other").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags("level", "needs  revision").build()));
        assertFalse(predicate.test(new PersonBuilder().build()));
        assertFalse(new PersonHasAnyTagPredicate(Set.of()).test(new PersonBuilder().withTags("a-level").build()));
    }

    @Test
    public void constructor_copiesInputAndRejectsNull() {
        Set<Tag> tags = new HashSet<>(Set.of(new Tag("a-level")));
        PersonHasAnyTagPredicate predicate = new PersonHasAnyTagPredicate(tags);
        tags.clear();
        assertTrue(predicate.test(new PersonBuilder().withTags("a-level").build()));
        assertThrows(NullPointerException.class, () -> new PersonHasAnyTagPredicate(null));
    }

    @Test
    public void equals_comparesRequestedTags() {
        PersonHasAnyTagPredicate predicate = new PersonHasAnyTagPredicate(Set.of(new Tag("a-level")));
        PersonHasAnyTagPredicate same = new PersonHasAnyTagPredicate(Set.of(new Tag("A-Level")));
        assertEquals(predicate, predicate);
        assertEquals(predicate, same);
        assertEquals(predicate.hashCode(), same.hashCode());
        assertNotEquals(predicate, new PersonHasAnyTagPredicate(Set.of(new Tag("other"))));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, "a-level");
    }
}
