package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Optional<Email> email;

    // Data fields
    private final Address address;
    private final Set<Tag> tags = new HashSet<>();
    private final Optional<StudentDetails> studentDetails;

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, Optional.of(email), address, tags, Optional.empty());
    }

    /**
     * Creates a student profile without legacy email or tags.
     */
    public Person(Name name, Phone phone, Address address, StudentDetails studentDetails) {
        this(name, phone, Optional.empty(), address, Set.of(), Optional.of(studentDetails));
    }

    /**
     * Creates a profile while retaining legacy fields during migration.
     * New student profiles must supply student details; legacy contacts require email.
     */
    public Person(Name name, Phone phone, Optional<Email> email, Address address, Set<Tag> tags,
            Optional<StudentDetails> studentDetails) {
        requireAllNonNull(name, phone, email, address, tags);
        Objects.requireNonNull(studentDetails);
        if (email.isEmpty() && studentDetails.isEmpty()) {
            throw new IllegalArgumentException("A contact must have student details or a legacy email.");
        }
        if (studentDetails.isPresent()) {
            checkArgument(Name.isValidStudentName(name.fullName), Name.STUDENT_MESSAGE_CONSTRAINTS);
            checkArgument(Phone.isValidStudentPhone(phone.value), Phone.STUDENT_MESSAGE_CONSTRAINTS);
        }
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.studentDetails = studentDetails;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Optional<Email> getEmail() {
        return email;
    }

    public Optional<StudentDetails> getStudentDetails() {
        return studentDetails;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true only if every stored field matches after field normalization.
     */
    public boolean isSamePerson(Person otherPerson) {
        return equals(otherPerson);
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
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && tags.equals(otherPerson.tags)
                && studentDetails.equals(otherPerson.studentDetails);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags, studentDetails);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .toString();
    }

}
