package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_NULL_PERSON = "Persons list contains a null person.";

    private static final Logger logger = LogsCenter.getLogger(JsonSerializableAddressBook.class);

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons) {
        this.persons.addAll(persons);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        List<Person> originalPersons = new ArrayList<>();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            if (jsonAdaptedPerson == null) {
                throw new IllegalValueException(MESSAGE_NULL_PERSON);
            }
            Person person = jsonAdaptedPerson.toModelType();
            if (originalPersons.stream().anyMatch(existing -> existing.isSamePerson(person)
                    && existing.getOutstandingAmount().equals(person.getOutstandingAmount()))) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            originalPersons.add(person);
        }
        AddressBook addressBook = new AddressBook();
        for (Person original : originalPersons) {
            Person person = original;
            if (addressBook.hasPerson(person)) {
                // Preserve legacy balances separately; never guess whether two financial records should be merged.
                do {
                    person = new Person(new Name(person.getName().fullName + " Recovered"), person.getPhone(),
                            person.getAddress(), person.getStartDate(), person.getOutstandingAmount(), person.getTags(),
                            person.getNotes(), person.getSubjects(), person.isArchived());
                } while (addressBook.hasPerson(person) || hasIdentity(originalPersons, person));
                logger.warning("Recovered legacy duplicate student: " + original.getName() + " -> " + person.getName()
                        + ". Balance and all other details preserved. Review these profiles with edit.");
            }
            addressBook.addPerson(person);
        }
        return addressBook;
    }

    private static boolean hasIdentity(List<Person> persons, Person candidate) {
        return persons.stream().anyMatch(candidate::isSamePerson);
    }

}
