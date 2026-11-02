package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Name;
import seedu.address.model.person.Notes;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StartDate;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_INVALID_LESSON_DAY =
            "Lesson day must be MON, TUE, WED, THU, FRI, SAT, or SUN.";
    public static final String MESSAGE_INVALID_LESSON_TIME =
            "Lesson time must be a valid 24-hour time in HH:mm format.";
    public static final String MESSAGE_INVALID_SUBJECT_SCHEDULE =
            "Each subject must use s/SUBJECT day/DAY time/TIME, in that order.";
    public static final String MESSAGE_DUPLICATE_SUBJECT =
            "Each student may have only one lesson for a given subject.";
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?:^|\\s)([a-zA-Z][a-zA-Z0-9_-]*/)");
    private static final DateTimeFormatter START_DATE_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive().appendPattern("dd-MMM").toFormatter(Locale.ENGLISH);

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     * @throws NullPointerException if {@code oneBasedIndex} is null.
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a name, trimming leading and trailing whitespace.
     *
     * @throws ParseException if the given {@code name} is invalid.
     * @throws NullPointerException if {@code name} is null.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a phone number, trimming leading and trailing whitespace.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     * @throws NullPointerException if {@code phone} is null.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses an address, trimming leading and trailing whitespace.
     *
     * @throws ParseException if the given {@code address} is invalid.
     * @throws NullPointerException if {@code address} is null.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a valid calendar month and day, trimming leading and trailing whitespace.
     *
     * @throws ParseException if the value is invalid or represents an impossible date.
     * @throws NullPointerException if {@code startDate} is null.
     */
    public static StartDate parseStartDate(String startDate) throws ParseException {
        requireNonNull(startDate);
        String trimmedStartDate = startDate.trim();
        if (!StartDate.isValidStartDate(trimmedStartDate)) {
            throw new ParseException(StartDate.MESSAGE_CONSTRAINTS);
        }
        // Validate new input here so legacy profiles with impossible dates can still load.
        try {
            MonthDay.parse(trimmedStartDate, START_DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new ParseException(StartDate.MESSAGE_CONSTRAINTS, exception);
        }
        return new StartDate(trimmedStartDate);
    }

    /**
     * Parses an outstanding amount, trimming leading and trailing whitespace.
     *
     * @throws ParseException if the value is invalid or exceeds the maximum amount.
     * @throws NullPointerException if {@code outstandingAmount} is null.
     */
    public static OutstandingAmount parseOutstandingAmount(String outstandingAmount) throws ParseException {
        requireNonNull(outstandingAmount);
        String trimmedOutstandingAmount = outstandingAmount.trim();
        if (!OutstandingAmount.isValidOutstandingAmount(trimmedOutstandingAmount)) {
            throw new ParseException(OutstandingAmount.MESSAGE_CONSTRAINTS);
        }
        OutstandingAmount amount = new OutstandingAmount(trimmedOutstandingAmount);
        if (amount.isGreaterThan(OutstandingAmount.MAXIMUM)) {
            throw new ParseException(OutstandingAmount.MESSAGE_TOO_LARGE);
        }
        return amount;
    }

    /**
     * Parses optional free-text notes, trimming only surrounding whitespace.
     * Blank input represents no note.
     *
     * @throws NullPointerException if {@code notes} is null.
     */
    public static Notes parseNotes(String notes) {
        return new Notes(notes);
    }

    /**
     * Parses an email address, trimming leading and trailing whitespace.
     *
     * @throws ParseException if the given {@code email} is invalid.
     * @throws NullPointerException if {@code email} is null.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a tag, trimming leading and trailing whitespace.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     * @throws NullPointerException if {@code tag} is null.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses tags into a new mutable set; duplicate tags are retained only once.
     *
     * @throws ParseException if any tag is invalid.
     * @throws NullPointerException if {@code tags} or an element is null.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses and validates a subject name after normalizing its spaces.
     *
     * @throws ParseException if the normalized subject is blank or invalid.
     * @throws NullPointerException if {@code subject} is null.
     */
    public static String parseSubjectName(String subject) throws ParseException {
        requireNonNull(subject);
        String normalized = Subject.normalizeName(subject);
        if (normalized.isEmpty()) {
            throw new ParseException("Subject must not be blank.");
        }
        if (!Subject.isValidSubject(normalized)) {
            throw new ParseException(Subject.MESSAGE_CONSTRAINTS);
        }
        return normalized;
    }

    /**
     * Parses a weekly lesson day using its three-letter name, ignoring case and surrounding whitespace.
     *
     * @throws ParseException if the day is not a valid three-letter day name.
     * @throws NullPointerException if {@code day} is null.
     */
    public static DayOfWeek parseLessonDay(String day) throws ParseException {
        requireNonNull(day);
        return switch (day.trim().toUpperCase(Locale.ROOT)) {
            case "MON" -> DayOfWeek.MONDAY;
            case "TUE" -> DayOfWeek.TUESDAY;
            case "WED" -> DayOfWeek.WEDNESDAY;
            case "THU" -> DayOfWeek.THURSDAY;
            case "FRI" -> DayOfWeek.FRIDAY;
            case "SAT" -> DayOfWeek.SATURDAY;
            case "SUN" -> DayOfWeek.SUNDAY;
            default -> throw new ParseException(MESSAGE_INVALID_LESSON_DAY);
        };
    }

    /**
     * Parses a lesson start time in strict 24-hour {@code HH:mm} format.
     *
     * @throws ParseException if the time is not in the required format.
     * @throws NullPointerException if {@code time} is null.
     */
    public static LocalTime parseLessonTime(String time) throws ParseException {
        requireNonNull(time);
        String trimmed = time.trim();
        if (!trimmed.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) {
            throw new ParseException(MESSAGE_INVALID_LESSON_TIME);
        }
        return LocalTime.parse(trimmed);
    }

    /**
     * Parses aligned subject, day, and time values into a new mutable set of scheduled subjects.
     *
     * @throws ParseException if the lists are empty, misaligned, contain invalid values, or duplicate subjects.
     * @throws NullPointerException if any list or element is null.
     */
    public static Set<Subject> parseScheduledSubjects(List<String> subjects, List<String> days, List<String> times)
            throws ParseException {
        requireNonNull(subjects);
        requireNonNull(days);
        requireNonNull(times);
        if (subjects.isEmpty() || subjects.size() != days.size() || subjects.size() != times.size()) {
            throw new ParseException(MESSAGE_INVALID_SUBJECT_SCHEDULE);
        }
        Set<Subject> parsed = new LinkedHashSet<>();
        Set<String> names = new HashSet<>();
        for (int index = 0; index < subjects.size(); index++) {
            String name = parseSubjectName(subjects.get(index));
            if (!names.add(name.toLowerCase(Locale.ROOT))) {
                throw new ParseException(MESSAGE_DUPLICATE_SUBJECT);
            }
            LessonSlot slot = new LessonSlot(parseLessonDay(days.get(index)), parseLessonTime(times.get(index)));
            parsed.add(new Subject(name, slot));
        }
        return parsed;
    }

    /**
     * Parses subject names into a new mutable set, retaining unique values case-insensitively.
     *
     * @throws ParseException if any subject name is invalid.
     * @throws NullPointerException if {@code subjects} or an element is null.
     */
    public static Set<String> parseSubjectNames(Collection<String> subjects) throws ParseException {
        requireNonNull(subjects);
        Set<String> parsed = new LinkedHashSet<>();
        Set<String> normalized = new HashSet<>();
        for (String subject : subjects) {
            String name = parseSubjectName(subject);
            if (normalized.add(name.toLowerCase(Locale.ROOT))) {
                parsed.add(name);
            }
        }
        return parsed;
    }

    /**
     * Rejects unknown prefix-shaped tokens before they can become part of a field value.
     *
     * @throws ParseException with {@code errorMessage} if {@code args} contains a prefix not in {@code knownPrefixes}
     * @throws NullPointerException if any argument is null.
     */
    public static void verifyKnownPrefixes(String args, String errorMessage, Prefix... knownPrefixes)
            throws ParseException {
        requireNonNull(args);
        List<Prefix> known = List.of(knownPrefixes);
        Matcher matcher = PREFIX_PATTERN.matcher(args);
        while (matcher.find()) {
            if (!known.contains(new Prefix(matcher.group(1)))) {
                throw new ParseException(errorMessage);
            }
        }
    }

    /**
     * Ensures each subject prefix is immediately followed by day and time prefixes.
     *
     * @throws ParseException if the subject, day, and time prefixes are not correctly ordered.
     * @throws NullPointerException if {@code args} is null.
     */
    public static void verifySubjectScheduleOrder(String args) throws ParseException {
        requireNonNull(args);
        List<String> prefixes = new java.util.ArrayList<>();
        Matcher matcher = PREFIX_PATTERN.matcher(args);
        while (matcher.find()) {
            prefixes.add(matcher.group(1));
        }
        for (int index = 0; index < prefixes.size(); index++) {
            String prefix = prefixes.get(index);
            if (prefix.equals(CliSyntax.PREFIX_SUBJECT.toString())) {
                if (index + 2 >= prefixes.size()
                        || !prefixes.get(index + 1).equals(CliSyntax.PREFIX_LESSON_DAY.toString())
                        || !prefixes.get(index + 2).equals(CliSyntax.PREFIX_LESSON_TIME.toString())) {
                    throw new ParseException(MESSAGE_INVALID_SUBJECT_SCHEDULE);
                }
                index += 2;
            } else if (prefix.equals(CliSyntax.PREFIX_LESSON_DAY.toString())
                    || prefix.equals(CliSyntax.PREFIX_LESSON_TIME.toString())) {
                throw new ParseException(MESSAGE_INVALID_SUBJECT_SCHEDULE);
            }
        }
    }
}
