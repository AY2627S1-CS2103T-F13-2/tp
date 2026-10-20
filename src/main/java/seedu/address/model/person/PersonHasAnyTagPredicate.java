package seedu.address.model.person;

import java.util.Set;
import java.util.function.Predicate;

import seedu.address.model.tag.Tag;

/**
 * Tests whether a student has at least one of the requested complete tags.
 */
public class PersonHasAnyTagPredicate implements Predicate<Person> {
    private final Set<Tag> tags;

    public PersonHasAnyTagPredicate(Set<Tag> tags) {
        this.tags = Set.copyOf(tags);
    }

    @Override
    public boolean test(Person person) {
        return person.getTags().stream().anyMatch(tags::contains);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PersonHasAnyTagPredicate otherPredicate
                && tags.equals(otherPredicate.tags);
    }

    @Override
    public int hashCode() {
        return tags.hashCode();
    }
}
