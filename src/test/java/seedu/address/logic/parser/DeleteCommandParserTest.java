package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;

public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " confirm ", new DeleteCommand());
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "0", DeleteCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-1", DeleteCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "a", DeleteCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1.2", DeleteCommand.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_missingOrExtraArgs_throwsParseException() {
        assertParseFailure(parser, "", DeleteCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseFailure(parser, "   ", DeleteCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseFailure(parser, "1 2", DeleteCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseFailure(parser, "1 confirm", DeleteCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseFailure(parser, "confirm 1", DeleteCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseFailure(parser, "confirm confirm", DeleteCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseFailure(parser, "Confirm", DeleteCommand.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_indexBeyondIntegerRange_returnsOutOfRangeIndex() {
        assertParseSuccess(parser, "999999999999999999999",
                new DeleteCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }
}
