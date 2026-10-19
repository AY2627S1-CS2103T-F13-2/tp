package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    private final Name name;
    private final Phone phone;
    private final Address address;
    private final StartDate startDate;
    private final OutstandingAmount outstandingAmount;
    private final Set<Subject> subjects;
    private final Notes notes;
    private final Set<Tag> tags;

    /** Creates a profile without optional details. */
    public Person(Name name, Phone phone, Address address, StartDate startDate,
            OutstandingAmount outstandingAmount) {
        this(name, phone, address, startDate, outstandingAmount, Set.of(), Notes.EMPTY, Set.of());
    }

    /**
     * Creates a student profile with optional notes. All fields must be non-null.
     */
    public Person(Name name, Phone phone, Address address, StartDate startDate,
            OutstandingAmount outstandingAmount, Notes notes) {
        this(name, phone, address, startDate, outstandingAmount, Set.of(), notes, Set.of());
    }

    /**
     * Creates a profile with a defensive, immutable copy of its optional tags.
     * Every field and tag must be non-null.
     */
    public Person(Name name, Phone phone, Address address, StartDate startDate,
            OutstandingAmount outstandingAmount, Set<Tag> tags) {
        this(name, phone, address, startDate, outstandingAmount, tags, Notes.EMPTY, Set.of());
    }

    /**
     * Creates a profile with optional tags and notes. All fields must be non-null.
     */
    public Person(Name name, Phone phone, Address address, StartDate startDate,
            OutstandingAmount outstandingAmount, Set<Tag> tags, Notes notes) {
        this(name, phone, address, startDate, outstandingAmount, tags, notes, Set.of());
    }

    /**
     * Creates a profile with tags, notes, and subjects. All fields must be non-null.
     */
    public Person(Name name, Phone phone, Address address, StartDate startDate,
            OutstandingAmount outstandingAmount, Set<Tag> tags, Notes notes, Set<Subject> subjects) {
        requireAllNonNull(name, phone, address, startDate, outstandingAmount, tags, notes, subjects);
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.startDate = startDate;
        this.outstandingAmount = outstandingAmount;
        this.subjects = Collections.unmodifiableSet(new LinkedHashSet<>(subjects));
        this.notes = notes;
        this.tags = Set.copyOf(tags);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Address getAddress() {
        return address;
    }

    public StartDate getStartDate() {
        return startDate;
    }

    public OutstandingAmount getOutstandingAmount() {
        return outstandingAmount;
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public Notes getNotes() {
        return notes;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * Returns true if both persons have the same five core fields, ignoring tags, notes, and subjects.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && address.equals(otherPerson.address)
                && startDate.equals(otherPerson.startDate)
                && outstandingAmount.equals(otherPerson.outstandingAmount);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && address.equals(otherPerson.address)
                && startDate.equals(otherPerson.startDate)
                && outstandingAmount.equals(otherPerson.outstandingAmount)
                && tags.equals(otherPerson.tags)
                && notes.equals(otherPerson.notes)
                && subjects.equals(otherPerson.subjects);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, address, startDate, outstandingAmount, tags, notes, subjects);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("address", address)
                .add("startDate", startDate)
                .add("outstandingAmount", outstandingAmount)
                .add("tags", tags)
                .add("notes", notes)
                .add("subjects", subjects)
                .toString();
    }

}
