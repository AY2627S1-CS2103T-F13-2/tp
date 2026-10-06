package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;

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
    public void parse_argsGiven_throwsParseException() {
        assertParseFailure(parser, " 3", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, " foo", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
    }
}
