package seedu.address.model.sort;

import static java.util.Objects.requireNonNull;

import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/** A display reference to one student's lesson; a null subject represents a student without a lesson. */
public record LessonRow(Person person, Subject subject) {
    /** Creates a row without copying or modifying the stored student profile. */
    public LessonRow {
        requireNonNull(person);
        if (subject != null && !person.getSubjects().contains(subject)) {
            throw new IllegalArgumentException("The lesson must belong to the student.");
        }
    }
}
