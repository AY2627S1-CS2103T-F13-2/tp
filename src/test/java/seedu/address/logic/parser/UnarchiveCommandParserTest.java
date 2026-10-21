package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UnarchiveCommand;

public class UnarchiveCommandParserTest {
    private final UnarchiveCommandParser parser = new UnarchiveCommandParser();

    @Test
    public void parse_validName_returnsCommand() {
        assertParseSuccess(parser, " s/Alice Pauline ", new UnarchiveCommand("Alice Pauline"));
    }

    @Test
    public void parse_invalidArguments_throwsParseException() {
        assertParseFailure(parser, "", UnarchiveCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "name/Alice", UnarchiveCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "s/Alice1", UnarchiveCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "s/ Alice", UnarchiveCommand.MESSAGE_USAGE);
    }
}
