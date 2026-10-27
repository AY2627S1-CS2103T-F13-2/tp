package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.DeleteCommand;

public class ArchiveCommandParserTest {
    private final ArchiveCommandParser parser = new ArchiveCommandParser();

    @Test
    public void parse_validIndex_returnsCommand() {
        assertParseSuccess(parser, " 2 ", new ArchiveCommand(INDEX_SECOND_PERSON));
    }

    @Test
    public void parse_missingOrExtraArguments_throwsParseException() {
        assertParseFailure(parser, "", ArchiveCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseFailure(parser, "1 2", ArchiveCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseFailure(parser, "Bob Choo", ArchiveCommand.MESSAGE_INVALID_COMMAND_FORMAT);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "0", DeleteCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-1", DeleteCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "s/Bob", DeleteCommand.MESSAGE_INVALID_INDEX);
    }
}
