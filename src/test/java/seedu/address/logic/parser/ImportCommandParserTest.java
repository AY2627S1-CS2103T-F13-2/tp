package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ImportCommand;

public class ImportCommandParserTest {

    private final ImportCommandParser parser = new ImportCommandParser();

    @Test
    public void parse_pathOnly_returnsPreviewCommand() {
        assertParseSuccess(parser, " contacts.csv ", new ImportCommand(Path.of("contacts.csv"), false));
    }

    @Test
    public void parse_pathWithConfirm_returnsConfirmedCommand() {
        assertParseSuccess(parser, " contacts.csv confirm", new ImportCommand(Path.of("contacts.csv"), true));
    }

    @Test
    public void parse_pathWithSpacesOrQuotes_keepsWholePath() {
        assertParseSuccess(parser, " my files/term 4.csv confirm",
                new ImportCommand(Path.of("my files/term 4.csv"), true));
        assertParseSuccess(parser, " \"my files/term 4.csv\"",
                new ImportCommand(Path.of("my files/term 4.csv"), false));
        assertParseSuccess(parser, " \"my files/term 4.csv\" confirm",
                new ImportCommand(Path.of("my files/term 4.csv"), true));
    }

    @Test
    public void parse_confirmInsideFileName_notTreatedAsConfirmation() {
        assertParseSuccess(parser, " confirm.csv", new ImportCommand(Path.of("confirm.csv"), false));
        assertParseSuccess(parser, " please-confirm", new ImportCommand(Path.of("please-confirm"), false));
    }

    @Test
    public void parse_noPath_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ImportCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "   ", expectedMessage);
        assertParseFailure(parser, " \"\" ", expectedMessage);
    }

    @Test
    public void parse_invalidPathCharacter_throwsParseException() {
        assertParseFailure(parser, " bad\u0000name.csv",
                String.format(ImportCommandParser.MESSAGE_INVALID_PATH, "bad\u0000name.csv"));
    }
}
