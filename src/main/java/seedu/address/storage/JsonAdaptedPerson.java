package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Name;
import seedu.address.model.person.Notes;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StartDate;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String address;
    private final String startDate;
    private final String outstandingAmount;
    private final List<String> subjects;
    private final String notes;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final boolean archived;

    /**
     * Constructs a legacy student record without tags or notes.
     */
    public JsonAdaptedPerson(String name, String phone, String address, String startDate, String outstandingAmount) {
        this(name, phone, address, startDate, outstandingAmount, null, null, null, false);
    }

    public JsonAdaptedPerson(String name, String phone, String address, String startDate,
            String outstandingAmount, String notes) {
        this(name, phone, address, startDate, outstandingAmount, null, notes, null, false);
    }

    public JsonAdaptedPerson(String name, String phone, String address, String startDate,
            String outstandingAmount, List<JsonAdaptedTag> tags) {
        this(name, phone, address, startDate, outstandingAmount, tags, null, null, false);
    }

    /** Legacy constructor retaining the pre-archive adapter API. */
    public JsonAdaptedPerson(String name, String phone, String address, String startDate,
            String outstandingAmount, List<JsonAdaptedTag> tags, String notes, List<String> subjects) {
        this(name, phone, address, startDate, outstandingAmount, tags, notes, subjects, false);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("address") String address, @JsonProperty("startDate") String startDate,
            @JsonProperty("outstandingAmount") String outstandingAmount,
            @JsonProperty("tags") List<JsonAdaptedTag> tags, @JsonProperty("notes") String notes,
            @JsonProperty("subjects") List<String> subjects, @JsonProperty("archived") Boolean archived) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.startDate = startDate;
        this.outstandingAmount = outstandingAmount;
        this.subjects = subjects;
        this.notes = notes;
        this.archived = archived != null && archived;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        address = source.getAddress().value;
        startDate = source.getStartDate().value;
        outstandingAmount = source.getOutstandingAmount().toPlainString();
        subjects = source.getSubjects().stream().map(Object::toString).toList();
        notes = source.getNotes().value;
        archived = source.isArchived();
        source.getTags().stream().sorted((first, second) -> first.tagName.compareTo(second.tagName))
                .map(JsonAdaptedTag::new).forEach(tags::add);
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        if (startDate == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    StartDate.class.getSimpleName()));
        }
        if (!StartDate.isValidStartDate(startDate)) {
            throw new IllegalValueException(StartDate.MESSAGE_CONSTRAINTS);
        }
        final StartDate modelStartDate = new StartDate(startDate);

        final OutstandingAmount modelOutstandingAmount;
        if (outstandingAmount == null) {
            modelOutstandingAmount = OutstandingAmount.ZERO;
        } else {
            if (!OutstandingAmount.isValidOutstandingAmount(outstandingAmount)) {
                throw new IllegalValueException(OutstandingAmount.MESSAGE_CONSTRAINTS);
            }
            modelOutstandingAmount = new OutstandingAmount(outstandingAmount);
        }

        Set<Subject> modelSubjects = new LinkedHashSet<>();
        for (String subject : subjects == null ? List.<String>of() : subjects) {
            if (subject == null || !Subject.isValidSubject(subject)) {
                throw new IllegalValueException(Subject.MESSAGE_CONSTRAINTS);
            }
            modelSubjects.add(new Subject(subject));
        }

        Notes modelNotes = notes == null ? Notes.EMPTY : new Notes(notes);
        Set<Tag> modelTags = new HashSet<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException(Tag.MESSAGE_CONSTRAINTS);
            }
            modelTags.add(tag.toModelType());
        }
        return new Person(modelName, modelPhone, modelAddress, modelStartDate, modelOutstandingAmount,
                modelTags, modelNotes, modelSubjects, archived);
    }

}
