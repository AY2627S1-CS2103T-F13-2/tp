package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.sort.SortOrder;
import seedu.address.model.sort.SortOrder.Criterion;
import seedu.address.model.sort.SortOrder.Direction;
import seedu.address.model.sort.SortOrder.Field;

public class SortOrderParserTest {
    private final SortOrderParser parser = new SortOrderParser();

    @Test
    public void parse_singleCriterion_acceptsEitherDirection() throws Exception {
        assertEquals(new SortOrder(List.of(new Criterion(Field.NAME, Direction.ASCENDING))), parser.parse("n/asc"));
        assertEquals(new SortOrder(List.of(new Criterion(Field.LESSON_SLOT, Direction.DESCENDING))),
                parser.parse("slot/desc"));
    }

    @Test
    public void parse_allFields_preservesMixedDirectionsAndInputPriority() throws Exception {
        assertEquals(new SortOrder(List.of(
                new Criterion(Field.LESSON_SLOT, Direction.ASCENDING),
                new Criterion(Field.OUTSTANDING_AMOUNT, Direction.DESCENDING),
                new Criterion(Field.START_DATE, Direction.ASCENDING),
                new Criterion(Field.NAME, Direction.DESCENDING))),
                parser.parse("slot/asc o/desc d/asc n/desc"));
        assertEquals(new SortOrder(List.of(
                new Criterion(Field.NAME, Direction.ASCENDING),
                new Criterion(Field.OUTSTANDING_AMOUNT, Direction.DESCENDING))), parser.parse("n/asc o/desc"));
    }

    @Test
    public void parse_surroundingAndSeparatingWhitespace_ignoresWhitespace() throws Exception {
        assertEquals(parser.parse("o/desc n/asc"), parser.parse(" \t o/ desc  \t n/ asc \t "));
    }

    @Test
    public void parse_missingCriteriaOrMalformedArguments_throwsParseException() {
        for (String args : List.of("", "   ", "n/", "n/asc extra", "extra n/asc", "n/asc reset",
                "n/asc /desc", "reset", "sort n/asc", "-summary")) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
    }

    @Test
    public void parse_unknownOrUppercaseFields_throwsUsefulError() {
        for (String args : List.of("x/asc", "N/asc", "SLOT/asc", "s/asc", "p/asc", "day/asc", "time/asc",
                "n/asc for/Maths")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertEquals(SortOrderParser.MESSAGE_UNKNOWN_FIELD, error.getMessage(), args);
        }
    }

    @Test
    public void parse_invalidOrUppercaseDirection_throwsUsefulError() {
        for (String args : List.of("n/up", "n/ASC", "o/descending", "d/ascending", "slot/1", "n/asco/desc")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertEquals(SortOrderParser.MESSAGE_INVALID_DIRECTION, error.getMessage(), args);
        }
    }

    @Test
    public void parse_repeatedField_throwsUsefulError() {
        for (String args : List.of("n/asc n/asc", "n/asc o/desc n/desc", "slot/asc slot/desc")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertEquals(SortOrderParser.MESSAGE_DUPLICATE_FIELDS, error.getMessage(), args);
        }
    }

    @Test
    public void parse_invalidCall_doesNotAffectEarlierOrSubsequentResults() throws Exception {
        SortOrder original = parser.parse("o/desc n/asc");
        assertThrows(ParseException.class, () -> parser.parse("o/asc n/up"));
        assertEquals(new SortOrder(List.of(
                new Criterion(Field.OUTSTANDING_AMOUNT, Direction.DESCENDING),
                new Criterion(Field.NAME, Direction.ASCENDING))), original);
        assertEquals(original, parser.parse("o/desc n/asc"));
    }
}
