package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

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

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Address address, StartDate startDate,
            OutstandingAmount outstandingAmount) {
        requireAllNonNull(name, phone, address, startDate, outstandingAmount);
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.startDate = startDate;
        this.outstandingAmount = outstandingAmount;
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

    /**
     * Returns true if both persons have exactly the same profile details.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null && equals(otherPerson);
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
                && outstandingAmount.equals(otherPerson.outstandingAmount);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, address, startDate, outstandingAmount);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("address", address)
                .add("startDate", startDate)
                .add("outstandingAmount", outstandingAmount)
                .toString();
    }

}
