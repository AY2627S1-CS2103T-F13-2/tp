package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Age;
import seedu.address.model.person.Education;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.Note;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.StartDate;
import seedu.address.model.person.Subject;

/**
 * Shared parsers for student fields. Command parsers decide which prefixes are
 * required or omitted; these methods validate only explicitly supplied values.
 */
public final class StudentProfileParserUtil {
    private StudentProfileParserUtil() {}

    /**
     * Parses a subject using its shared validation and normalization.
     */
    public static Subject parseSubject(String value) throws ParseException {
        return parseValue(value, Subject::new);
    }

    /**
     * Parses a start date using its shared validation and normalization.
     */
    public static StartDate parseStartDate(String value) throws ParseException {
        return parseValue(value, StartDate::new);
    }

    /**
     * Parses an outstanding amount using its shared validation and normalization.
     */
    public static OutstandingAmount parseOutstandingAmount(String value) throws ParseException {
        return parseValue(value, OutstandingAmount::new);
    }

    /**
     * Parses an age using its shared validation and normalization.
     */
    public static Age parseAge(String value) throws ParseException {
        return parseValue(value, Age::new);
    }

    /**
     * Parses an education using its shared validation and normalization.
     */
    public static Education parseEducation(String value) throws ParseException {
        return parseValue(value, Education::new);
    }

    /**
     * Parses a guardian contact using its shared validation and normalization.
     */
    public static GuardianContact parseGuardianContact(String value) throws ParseException {
        return parseValue(value, GuardianContact::new);
    }

    /**
     * Parses a note using its shared validation and normalization.
     */
    public static Note parseNote(String value) throws ParseException {
        return parseValue(value, Note::new);
    }

    /**
     * Parses a non-empty subject set. Every supplied subject must be non-blank.
     */
    public static Set<Subject> parseSubjects(Collection<String> values) throws ParseException {
        requireNonNull(values);
        if (values.isEmpty()) {
            throw new ParseException(Subject.MESSAGE_CONSTRAINTS);
        }
        Set<Subject> subjects = new HashSet<>();
        for (String value : values) {
            subjects.add(parseSubject(value));
        }
        return Set.copyOf(subjects);
    }

    private static <T> T parseValue(String value, Function<String, T> constructor) throws ParseException {
        requireNonNull(value);
        try {
            return constructor.apply(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
