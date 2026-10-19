package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.model.person.Subject;

/**
 * As we are only doing white-box testing, our test cases do not cover path variations
 * outside of the ListCommandParser code.
 */
public class ListCommandParserTest {

    private ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_noArgs_returnsListCommand() {
        assertParseSuccess(parser, "", new ListCommand());
    }

    @Test
    public void parse_blankArgs_returnsListCommand() {
        assertParseSuccess(parser, "   ", new ListCommand());
    }

    @Test
    public void parse_summaryFlag_returnsSummaryListCommand() {
        assertParseSuccess(parser, " -summary ", new ListCommand(true));
    }

    @Test
    public void parse_argsGiven_throwsParseException() {
        assertParseFailure(parser, " 3", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, " foo", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, " -summary -summary", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, " -Summary", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
    }

    @Test
    public void parse_subjects_returnsFilteredListCommand() {
        assertParseSuccess(parser, " s/Maths s/H2 Chemistry",
                new ListCommand(Set.of(new Subject("maths"), new Subject("H2 Chemistry"))));
        assertParseSuccess(parser, " s/ Maths  s/maths s/ H2 Chemistry ",
                new ListCommand(Set.of(new Subject("Maths"), new Subject("H2 Chemistry"))));
    }

    @Test
    public void parse_invalidSubject_failure() {
        assertParseFailure(parser, " s/Maths s/", "Subject must not be blank.");
        assertParseFailure(parser, " s/Maths, Physics", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " s/---", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " t/A-Level", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
    }
}
