package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.UnarchiveCommand;

public class UnarchiveCommandParserTest {
    private final UnarchiveCommandParser parser = new UnarchiveCommandParser();

    @Test
    public void parse_validIndex_returnsCommand() {
        assertParseSuccess(parser, " 1 ", new UnarchiveCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidArguments_throwsParseException() {
        assertParseFailure(parser, "", UnarchiveCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseFailure(parser, "Bob Choo", UnarchiveCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseFailure(parser, "0", DeleteCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "s/Bob", DeleteCommand.MESSAGE_INVALID_INDEX);
    }
}
