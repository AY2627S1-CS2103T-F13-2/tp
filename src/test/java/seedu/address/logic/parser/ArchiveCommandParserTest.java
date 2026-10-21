package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ArchiveCommand;

public class ArchiveCommandParserTest {
    private final ArchiveCommandParser parser = new ArchiveCommandParser();

    @Test
    public void parse_validName_returnsCommand() {
        assertParseSuccess(parser, " s/Alice Pauline ", new ArchiveCommand("Alice Pauline"));
    }

    @Test
    public void parse_invalidArguments_throwsParseException() {
        assertParseFailure(parser, "", ArchiveCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "name/Alice", ArchiveCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "s/Alice1", ArchiveCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "s/ Alice", ArchiveCommand.MESSAGE_USAGE);
    }
}
