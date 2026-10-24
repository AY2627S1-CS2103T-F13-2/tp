package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.sort.SortOrder;
import seedu.address.model.sort.SortOrder.Criterion;
import seedu.address.model.sort.SortOrder.Direction;
import seedu.address.model.sort.SortOrder.Field;

public class SortCommandParserTest {
    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_queryAndReset_areDifferentRequests() throws Exception {
        assertEquals(new SortCommand(), parser.parse("  "));
        assertEquals(new SortCommand(SortOrder.NONE), parser.parse(" reset "));
    }

    @Test
    public void parse_fields_delegatesToOrderedCriteriaValidation() throws Exception {
        assertEquals(new SortCommand(new SortOrder(List.of(
                new Criterion(Field.LESSON_SLOT, Direction.ASCENDING),
                new Criterion(Field.NAME, Direction.DESCENDING)))), parser.parse("slot/asc n/desc"));
    }

    @Test
    public void parse_invalidRequests_doNotCreateCommands() {
        for (String args : List.of("reset n/asc", "n/asc reset", "RESET", "n/asc n/desc", "for/Maths", "n/")) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
    }
}
