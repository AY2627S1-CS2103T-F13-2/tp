package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Parses one row of an exported student CSV file into a {@code Person}.
 * Columns follow {@link seedu.address.commons.util.CsvExporter#HEADER}:
 * Name, Phone, Address, Start Date, Outstanding Amount, Subjects, Tags, Notes and Archived.
 */
public final class CsvPersonParser {

    public static final int COLUMN_COUNT = 9;
    public static final String MESSAGE_WRONG_COLUMN_COUNT = "Expected " + COLUMN_COUNT + " columns but found %1$d.";
    public static final String MESSAGE_INVALID_SUBJECTS = "Subjects must be written as SUBJECT (DAY HH:mm), "
            + "separated by semicolons, e.g. Maths (Mon 09:00); Physics (Tue 10:00).";
    public static final String MESSAGE_MISSING_SUBJECT = "At least one subject must be supplied.";
    public static final String MESSAGE_INVALID_ARCHIVED = "Archived must be true or false.";

    private static final String LIST_SEPARATOR = ";";
    private static final String FORMULA_PREFIXES = "=+-@";
    private static final Pattern SUBJECT_FORMAT = Pattern.compile("(?<name>.+?)\\s*\\(\\s*(?<day>\\S+)\\s+"
            + "(?<time>\\S+)\\s*\\)");

    private CsvPersonParser() {
        // Prevent instantiation.
    }

    /**
     * Returns the student described by one CSV row, using the same validation as the add command.
     *
     * @throws ParseException if the row has the wrong number of columns or any value is invalid.
     */
    public static Person parsePerson(List<String> fields) throws ParseException {
        requireNonNull(fields);
        if (fields.size() != COLUMN_COUNT) {
            throw new ParseException(String.format(MESSAGE_WRONG_COLUMN_COUNT, fields.size()));
        }

        OutstandingAmount outstandingAmount = fields.get(4).isBlank()
                ? OutstandingAmount.ZERO : ParserUtil.parseOutstandingAmount(fields.get(4));
        return new Person(ParserUtil.parseName(fields.get(0)), ParserUtil.parsePhone(fields.get(1)),
                ParserUtil.parseAddress(unescapePlainText(fields.get(2))), ParserUtil.parseStartDate(fields.get(3)),
                outstandingAmount, parseTags(fields.get(6)), ParserUtil.parseNotes(unescapePlainText(fields.get(7))),
                parseSubjects(fields.get(5)), parseArchived(fields.get(8)));
    }

    /** Removes the reversible apostrophe encoding used for formula-like address values. */
    private static String unescapePlainText(String value) {
        if (value.length() >= 2 && value.charAt(0) == '\'' && value.charAt(1) == '\'') {
            return value.substring(1);
        }
        return value.length() >= 2 && value.charAt(0) == '\''
                && FORMULA_PREFIXES.indexOf(value.charAt(1)) >= 0 ? value.substring(1) : value;
    }

    /**
     * Parses subjects such as {@code Maths (Mon 09:00); Physics (Tue 10:00)} into subjects with lesson slots.
     *
     * @throws ParseException if no subject is given, a subject is not in that format, or a name repeats.
     */
    static Set<Subject> parseSubjects(String text) throws ParseException {
        if (text.isBlank()) {
            throw new ParseException(MESSAGE_MISSING_SUBJECT);
        }
        Set<Subject> subjects = new LinkedHashSet<>();
        Set<String> names = new HashSet<>();
        for (String part : text.split(LIST_SEPARATOR, -1)) {
            Matcher matcher = SUBJECT_FORMAT.matcher(part.trim());
            if (!matcher.matches()) {
                throw new ParseException(MESSAGE_INVALID_SUBJECTS);
            }
            String name = ParserUtil.parseSubjectName(matcher.group("name"));
            if (!names.add(name.toLowerCase(Locale.ROOT))) {
                throw new ParseException(ParserUtil.MESSAGE_DUPLICATE_SUBJECT);
            }
            LessonSlot lessonSlot = new LessonSlot(ParserUtil.parseLessonDay(matcher.group("day")),
                    ParserUtil.parseLessonTime(matcher.group("time")));
            subjects.add(new Subject(name, lessonSlot));
        }
        return subjects;
    }

    /**
     * Parses tags separated by semicolons. A blank value means no tags.
     *
     * @throws ParseException if any tag is invalid.
     */
    static Set<Tag> parseTags(String text) throws ParseException {
        Set<Tag> tags = new HashSet<>();
        if (text.isBlank()) {
            return tags;
        }
        for (String part : text.split(LIST_SEPARATOR, -1)) {
            tags.add(ParserUtil.parseTag(part));
        }
        return tags;
    }

    /**
     * Parses {@code true} or {@code false}, ignoring case and surrounding spaces.
     *
     * @throws ParseException if the value is anything else.
     */
    static boolean parseArchived(String text) throws ParseException {
        String trimmed = text.trim();
        if (trimmed.equalsIgnoreCase("true")) {
            return true;
        }
        if (trimmed.equalsIgnoreCase("false")) {
            return false;
        }
        throw new ParseException(MESSAGE_INVALID_ARCHIVED);
    }
}
