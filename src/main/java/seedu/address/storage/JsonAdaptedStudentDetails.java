package seedu.address.storage;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Age;
import seedu.address.model.person.Education;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.Note;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.StartDate;
import seedu.address.model.person.StudentDetails;
import seedu.address.model.person.Subject;

/**
 * JSON representation of student details, distinct from legacy AB3 contacts.
 */
class JsonAdaptedStudentDetails {
    private final List<String> subjects;
    private final String startDate;
    private final String outstandingAmount;
    private final String age;
    private final String education;
    private final String guardianContact;
    private final String note;

    @JsonCreator
    public JsonAdaptedStudentDetails(@JsonProperty("subjects") List<String> subjects,
            @JsonProperty("startDate") String startDate, @JsonProperty("outstandingAmount") String outstandingAmount,
            @JsonProperty("age") String age, @JsonProperty("education") String education,
            @JsonProperty("guardianContact") String guardianContact, @JsonProperty("note") String note) {
        this.subjects = subjects;
        this.startDate = startDate;
        this.outstandingAmount = outstandingAmount;
        this.age = age;
        this.education = education;
        this.guardianContact = guardianContact;
        this.note = note;
    }

    /**
     * Copies immutable profile values for serialization.
     */
    public JsonAdaptedStudentDetails(StudentDetails source) {
        subjects = source.getSubjects().stream().map(Object::toString).sorted().toList();
        startDate = source.getStartDate().value;
        outstandingAmount = source.getOutstandingAmount().toString();
        age = source.getAge().value;
        education = source.getEducation().value;
        guardianContact = source.getGuardianContact().value;
        note = source.getNote().value;
    }

    /**
     * Restores details with the same normalization and validation as command input.
     */
    public StudentDetails toModelType() throws IllegalValueException {
        if (subjects == null || startDate == null || subjects.stream().anyMatch(value -> value == null)) {
            throw new IllegalValueException("Student subjects and start date must be present and non-null.");
        }
        try {
            return new StudentDetails(subjects.stream().map(Subject::new).collect(Collectors.toSet()),
                    new StartDate(startDate), new OutstandingAmount(orEmpty(outstandingAmount)),
                    new Age(orEmpty(age)), new Education(orEmpty(education)),
                    new GuardianContact(orEmpty(guardianContact)), new Note(orEmpty(note)));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage(), e);
        }
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
