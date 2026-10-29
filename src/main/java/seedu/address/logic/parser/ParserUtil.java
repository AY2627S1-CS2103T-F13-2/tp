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
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
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
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
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
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /** Parses a valid calendar month and day, trimming leading and trailing whitespace. */
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

    /** Parses an outstanding amount, trimming leading and trailing whitespace. */
    public static OutstandingAmount parseOutstandingAmount(String outstandingAmount) throws ParseException {
        requireNonNull(outstandingAmount);
        String trimmedOutstandingAmount = outstandingAmount.trim();
        if (!OutstandingAmount.isValidOutstandingAmount(trimmedOutstandingAmount)) {
            throw new ParseException(OutstandingAmount.MESSAGE_CONSTRAINTS);
        }
        return new OutstandingAmount(trimmedOutstandingAmount);
    }

    /**
     * Parses optional free-text notes, trimming only surrounding whitespace.
     * Blank input represents no note.
     */
    public static Notes parseNotes(String notes) {
        return new Notes(notes);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
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
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
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
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /** Parses and validates a subject name after normalizing its spaces. */
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

    /** Parses a weekly lesson day using the documented three-letter names. */
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

    /** Parses a lesson start time in strict 24-hour HH:mm format. */
    public static LocalTime parseLessonTime(String time) throws ParseException {
        requireNonNull(time);
        String trimmed = time.trim();
        if (!trimmed.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) {
            throw new ParseException(MESSAGE_INVALID_LESSON_TIME);
        }
        return LocalTime.parse(trimmed);
    }

    /** Parses aligned subject, day, and time values into scheduled subjects. */
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

    /** Parses subject names used by filters, retaining unique values case-insensitively. */
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

    /** Ensures each subject prefix is immediately followed by day and time prefixes. */
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
