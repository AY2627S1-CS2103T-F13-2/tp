package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Set;

/**
 * Immutable tuition details shared by Add, Edit, and storage.
 * Subjects and start date are required; optional text fields use empty values.
 */
public final class StudentDetails {
    private final Set<Subject> subjects;
    private final StartDate startDate;
    private final OutstandingAmount outstandingAmount;
    private final Age age;
    private final Education education;
    private final GuardianContact guardianContact;
    private final Note note;

    /**
     * Creates tuition details with defaults for all optional fields.
     */
    public StudentDetails(Set<Subject> subjects, StartDate startDate) {
        this(subjects, startDate, OutstandingAmount.ZERO, new Age(""), new Education(""),
                new GuardianContact(""), new Note(""));
    }

    /**
     * Creates tuition details, defensively copying the required non-empty subject set.
     */
    public StudentDetails(Set<Subject> subjects, StartDate startDate, OutstandingAmount outstandingAmount,
            Age age, Education education, GuardianContact guardianContact, Note note) {
        requireAllNonNull(subjects, startDate, outstandingAmount, age, education, guardianContact, note);
        checkArgument(!subjects.isEmpty(), Subject.MESSAGE_CONSTRAINTS);
        this.subjects = Set.copyOf(subjects);
        this.startDate = startDate;
        this.outstandingAmount = outstandingAmount;
        this.age = age;
        this.education = education;
        this.guardianContact = guardianContact;
        this.note = note;
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public StartDate getStartDate() {
        return startDate;
    }

    public OutstandingAmount getOutstandingAmount() {
        return outstandingAmount;
    }

    public Age getAge() {
        return age;
    }

    public Education getEducation() {
        return education;
    }

    public GuardianContact getGuardianContact() {
        return guardianContact;
    }

    public Note getNote() {
        return note;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StudentDetails details)) {
            return false;
        }
        return subjects.equals(details.subjects) && startDate.equals(details.startDate)
                && outstandingAmount.equals(details.outstandingAmount) && age.equals(details.age)
                && education.equals(details.education) && guardianContact.equals(details.guardianContact)
                && note.equals(details.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjects, startDate, outstandingAmount, age, education, guardianContact, note);
    }
}
