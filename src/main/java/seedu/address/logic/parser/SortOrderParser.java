package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.sort.SortOrder;
import seedu.address.model.sort.SortOrder.Criterion;
import seedu.address.model.sort.SortOrder.Direction;
import seedu.address.model.sort.SortOrder.Field;

/** Parses ordered field/direction arguments independently of command execution. */
public class SortOrderParser {
    public static final String MESSAGE_INVALID_FORMAT =
            "Invalid sorting criteria. Specify fields with directions, e.g. n/asc o/desc.";
    public static final String MESSAGE_UNKNOWN_FIELD = "Sorting fields must be n/, o/, d/, or slot/.";
    public static final String MESSAGE_INVALID_DIRECTION = "Sorting direction must be asc or desc.";
    public static final String MESSAGE_DUPLICATE_FIELDS = "Each sorting field may be specified only once.";

    private static final Pattern CRITERION_PATTERN =
            Pattern.compile("\\s*(?<field>[A-Za-z][A-Za-z0-9_-]*/)\\s*(?<direction>\\S+)");

    /**
     * Parses at least one criterion, preserving the supplied priority and direction.
     * Query and reset arguments belong to the command parser, rather than this criteria parser.
     *
     * @throws ParseException if any argument is malformed, unknown, or repeated
     */
    public SortOrder parse(String args) throws ParseException {
        String trimmed = requireNonNull(args).trim();
        if (trimmed.isEmpty()) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        List<Criterion> criteria = new ArrayList<>();
        Set<Field> fields = EnumSet.noneOf(Field.class);
        Matcher matcher = CRITERION_PATTERN.matcher(trimmed);
        int nextArgument = 0;
        while (nextArgument < trimmed.length()) {
            matcher.region(nextArgument, trimmed.length());
            if (!matcher.lookingAt()) {
                throw new ParseException(MESSAGE_INVALID_FORMAT);
            }
            Field field = parseField(matcher.group("field"));
            if (!fields.add(field)) {
                throw new ParseException(MESSAGE_DUPLICATE_FIELDS);
            }
            criteria.add(new Criterion(field, parseDirection(matcher.group("direction"))));
            nextArgument = matcher.end();
        }
        return new SortOrder(criteria);
    }

    private Field parseField(String prefix) throws ParseException {
        return switch (prefix) {
            case "n/" -> Field.NAME;
            case "o/" -> Field.OUTSTANDING_AMOUNT;
            case "d/" -> Field.START_DATE;
            case "slot/" -> Field.LESSON_SLOT;
            default -> throw new ParseException(MESSAGE_UNKNOWN_FIELD);
        };
    }

    private Direction parseDirection(String value) throws ParseException {
        return switch (value) {
            case "asc" -> Direction.ASCENDING;
            case "desc" -> Direction.DESCENDING;
            default -> throw new ParseException(MESSAGE_INVALID_DIRECTION);
        };
    }
}
